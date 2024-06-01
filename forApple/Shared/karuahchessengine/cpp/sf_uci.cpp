/*
  Stockfish, a UCI chess playing engine derived from Glaurung 2.1
  Copyright (C) 2004-2024 The Stockfish developers (see AUTHORS file)

  Stockfish is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Stockfish is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "sf_uci.h"

#include <algorithm>
#include <cassert>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <deque>
#include <memory>
#include <optional>
#include <sstream>
#include <vector>
#include <cstdint>


#include "sf_evaluate.h"
#include "sf_movegen.h"
#include "nnue/evaluate_nnue.h"
#include "nnue/nnue_architecture.h"
#include "sf_position.h"
#include "sf_search.h"

#include "sf_types.h"
#include "sf_ucioption.h"


namespace Stockfish {

constexpr auto StartFEN             = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
constexpr int  NormalizeToPawnValue = 356;
constexpr int  MaxHashMB            = Is64Bit ? 33554432 : 2048;

UCI::UCI() {

    evalFiles = {{Eval::NNUE::Big, {"EvalFile", EvalFileDefaultNameBig, "None", ""}},
                 {Eval::NNUE::Small, {"EvalFileSmall", EvalFileDefaultNameSmall, "None", ""}}};

    options["Threads"] << Option(1, 1, 1024, [this](const Option&) {
        threads.set({options, threads, tt});
    });

    options["Hash"] << Option(16, 1, MaxHashMB, [this](const Option& o) {
        threads.main_thread()->wait_for_search_finished();
        tt.resize(o, options["Threads"]);
    });
    
    options["Clear Hash"] << Option([this](const Option&) { search_clear(); });
    options["Ponder"] << Option(false);
    options["MultiPV"] << Option(1, 1, MAX_MOVES);

    // Karuah Chess patch for negative skill level
    options["Skill Level"] << Option(20, -10, 20);

    options["Move Overhead"] << Option(10, 0, 5000);
    options["nodestime"] << Option(0, 0, 10000);
    options["UCI_Chess960"] << Option(false);
    options["UCI_LimitStrength"] << Option(false);
    options["UCI_Elo"] << Option(1320, 1320, 3190);
    options["UCI_ShowWDL"] << Option(false);
    
    options["SyzygyProbeDepth"] << Option(1, 1, 100);
    options["Syzygy50MoveRule"] << Option(true);
    options["SyzygyProbeLimit"] << Option(7, 0, 7);
    
    threads.set({options, threads, tt});

    search_clear();  // After threads are up
}

void UCI::go(Position& pos, std::istringstream& is, StateListPtr& states) {

    Search::LimitsType limits;
    std::string        token;
    bool               ponderMode = false;

    limits.startTime = now();  // The search starts as early as possible

    while (is >> token)
        if (token == "searchmoves")  // Needs to be the last command on the line
            while (is >> token)
                limits.searchmoves.push_back(to_move(pos, token));

        else if (token == "wtime")
            is >> limits.time[WHITE];
        else if (token == "btime")
            is >> limits.time[BLACK];
        else if (token == "winc")
            is >> limits.inc[WHITE];
        else if (token == "binc")
            is >> limits.inc[BLACK];
        else if (token == "movestogo")
            is >> limits.movestogo;
        else if (token == "depth")
            is >> limits.depth;
        else if (token == "nodes")
            is >> limits.nodes;
        else if (token == "movetime")
            is >> limits.movetime;
        else if (token == "mate")
            is >> limits.mate;
        else if (token == "perft")
            is >> limits.perft;
        else if (token == "infinite")
            limits.infinite = 1;
        else if (token == "ponder")
            ponderMode = true;

    Eval::NNUE::verify();
        
    threads.start_thinking(options, pos, states, limits, ponderMode);
}



void UCI::search_clear() {
    threads.main_thread()->wait_for_search_finished();

    tt.clear(options["Threads"]);
    threads.clear();    
}

void UCI::setoption(std::istringstream& is) {
    threads.main_thread()->wait_for_search_finished();
    options.setoption(is);
}

void UCI::position(Position& pos, std::istringstream& is, StateListPtr& states) {
    Move        m;
    std::string token, fen;

    is >> token;

    if (token == "startpos")
    {
        fen = StartFEN;
        is >> token;  // Consume the "moves" token, if any
    }
    else if (token == "fen")
        while (is >> token && token != "moves")
            fen += token + " ";
    else
        return;

    states = StateListPtr(new std::deque<StateInfo>(1));  // Drop the old state and create a new one
    pos.set(fen, options["UCI_Chess960"], &states->back());

    // Parse the move list, if any
    while (is >> token && (m = to_move(pos, token)) != Move::none())
    {
        states->emplace_back();
        pos.do_move(m, states->back());
    }
}

int UCI::to_cp(Value v) { return 100 * v / NormalizeToPawnValue; }

std::string UCI::value(Value v) {
    assert(-VALUE_INFINITE < v && v < VALUE_INFINITE);

    std::stringstream ss;

    if (std::abs(v) < VALUE_TB_WIN_IN_MAX_PLY)
        ss << "cp " << to_cp(v);
    else if (std::abs(v) <= VALUE_TB)
    {
        const int ply = VALUE_TB - std::abs(v);  // recompute ss->ply
        ss << "cp " << (v > 0 ? 20000 - ply : -20000 + ply);
    }
    else
        ss << "mate " << (v > 0 ? VALUE_MATE - v + 1 : -VALUE_MATE - v) / 2;

    return ss.str();
}

std::string UCI::square(Square s) {
    return std::string{char('a' + file_of(s)), char('1' + rank_of(s))};
}

std::string UCI::move(Move m, bool chess960) {
    if (m == Move::none())
        return "(none)";

    if (m == Move::null())
        return "0000";

    Square from = m.from_sq();
    Square to   = m.to_sq();

    if (m.type_of() == CASTLING && !chess960)
        to = make_square(to > from ? FILE_G : FILE_C, rank_of(from));

    std::string move = square(from) + square(to);

    if (m.type_of() == PROMOTION)
        move += " pnbrqk"[m.promotion_type()];

    return move;
}

namespace {
// The win rate model returns the probability of winning (in per mille units) given an
// eval and a game ply. It fits the LTC fishtest statistics rather accurately.
int win_rate_model(Value v, int ply) {

    // The fitted model only uses data for moves in [8, 120], and is anchored at move 32.
    double m = std::clamp(ply / 2 + 1, 8, 120) / 32.0;

    // The coefficients of a third-order polynomial fit is based on the fishtest data
    // for two parameters that need to transform eval to the argument of a logistic
    // function.
    constexpr double as[] = {-1.06249702, 7.42016937, 0.89425629, 348.60356174};
    constexpr double bs[] = {-5.33122190, 39.57831533, -90.84473771, 123.40620748};

    // Enforce that NormalizeToPawnValue corresponds to a 50% win rate at move 32.
    static_assert(NormalizeToPawnValue == int(0.5 + as[0] + as[1] + as[2] + as[3]));

    double a = (((as[0] * m + as[1]) * m + as[2]) * m) + as[3];
    double b = (((bs[0] * m + bs[1]) * m + bs[2]) * m) + bs[3];

    // Return the win rate in per mille units, rounded to the nearest integer.
    return int(0.5 + 1000 / (1 + std::exp((a - double(v)) / b)));
}
}

std::string UCI::wdl(Value v, int ply) {
    std::stringstream ss;

    int wdl_w = win_rate_model(v, ply);
    int wdl_l = win_rate_model(-v, ply);
    int wdl_d = 1000 - wdl_w - wdl_l;
    ss << " wdl " << wdl_w << " " << wdl_d << " " << wdl_l;

    return ss.str();
}

Move UCI::to_move(const Position& pos, std::string& str) {
    if (str.length() == 5)
        str[4] = char(tolower(str[4]));  // The promotion piece character must be lowercased

    for (const auto& m : MoveList<LEGAL>(pos))
        if (str == move(m, pos.is_chess960()))
            return m;

    return Move::none();
}

}  // namespace Stockfish

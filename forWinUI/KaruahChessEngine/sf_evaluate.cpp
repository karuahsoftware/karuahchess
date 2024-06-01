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

#include "sf_evaluate.h"

#include <algorithm>
#include <cassert>
#include <cmath>
#include <cstdlib>
#include <iomanip>
#include <optional>
#include <sstream>
#include <unordered_map>
#include <vector>


#include "sf_misc.h"
#include "nnue/evaluate_nnue.h"
#include "nnue/nnue_architecture.h"
#include "sf_position.h"
#include "sf_types.h"
#include "sf_uci.h"
#include "sf_ucioption.h"

#include "engine.h"
#include "helper.h"

namespace Stockfish {

namespace Eval {

    /// Karuah Chess patch for loading NNUE files.
    void NNUE::load_networks() {

        if (!Engine::nnueLoadedBig) {
            Engine::membuf nnueMemoryBuffer(Engine::nnueFileBufferBig, Engine::nnueFileBufferBig + Engine::nnueFileBufferSizeBig);
            std::istream nnueStream(&nnueMemoryBuffer);
            auto description = load_eval(nnueStream, Stockfish::Eval::NNUE::NetSize::Big);
            if (description.has_value()) {
                Engine::nnueLoadedBig = true;
            }
            else
            {
                Engine::engineErr.add(helper::NNUE_ERROR);
            }
        }

        if (!Engine::nnueLoadedSmall) {
            Engine::membuf nnueMemoryBuffer(Engine::nnueFileBufferSmall, Engine::nnueFileBufferSmall + Engine::nnueFileBufferSizeSmall);
            std::istream nnueStream(&nnueMemoryBuffer);
            auto description = load_eval(nnueStream, Stockfish::Eval::NNUE::NetSize::Small);
            if (description.has_value()) {
                Engine::nnueLoadedSmall = true;
            }
            else
            {
                Engine::engineErr.add(helper::NNUE_ERROR);
            }
        }
    }

    /// Karuah Chess patch
    // Verifies that the last net used was loaded successfully
    void NNUE::verify() {

        if (Engine::engineErr.exists(helper::NNUE_ERROR) ||
            Engine::engineErr.exists(helper::NNUE_FILE_OPEN_ERROR) ||            
            Engine::engineErr.exists(helper::NNUE_MEMORY_ALLOCATION_ERROR))
        {
            // This should never happen
            throw std::runtime_error("NNUE file is not loaded.");
        }
    }
}

// Returns a static, purely materialistic evaluation of the position from
// the point of view of the given color. It can be divided by PawnValue to get
// an approximation of the material advantage on the board in terms of pawns.
int Eval::simple_eval(const Position& pos, Color c) {
    return PawnValue * (pos.count<PAWN>(c) - pos.count<PAWN>(~c))
         + (pos.non_pawn_material(c) - pos.non_pawn_material(~c));
}


// Evaluate is the evaluator for the outer world. It returns a static evaluation
// of the position from the point of view of the side to move.
Value Eval::evaluate(const Position& pos, int optimism) {

    assert(!pos.checkers());

    int  simpleEval = simple_eval(pos, pos.side_to_move());
    bool smallNet   = std::abs(simpleEval) > 1050;

    int nnueComplexity;

    Value nnue = smallNet ? NNUE::evaluate<NNUE::Small>(pos, true, &nnueComplexity)
                          : NNUE::evaluate<NNUE::Big>(pos, true, &nnueComplexity);

    // Blend optimism and eval with nnue complexity and material imbalance
    optimism += optimism * (nnueComplexity + std::abs(simpleEval - nnue)) / 512;
    nnue -= nnue * (nnueComplexity + std::abs(simpleEval - nnue)) / 32768;

    int npm = pos.non_pawn_material() / 64;
    int v   = (nnue * (915 + npm + 9 * pos.count<PAWN>()) + optimism * (154 + npm)) / 1024;

    // Damp down the evaluation linearly when shuffling
    int shuffling = pos.rule50_count();
    v             = v * (200 - shuffling) / 214;

    // Guarantee evaluation does not hit the tablebase range
    v = std::clamp(v, VALUE_TB_LOSS_IN_MAX_PLY + 1, VALUE_TB_WIN_IN_MAX_PLY - 1);

    return v;
}

// Like evaluate(), but instead of returning a value, it returns
// a string (suitable for outputting to stdout) that contains the detailed
// descriptions and values of each evaluation term. Useful for debugging.
// Trace scores are from white's point of view
std::string Eval::trace(Position& pos) {

    if (pos.checkers())
        return "Final evaluation: none (in check)";

    std::stringstream ss;
    ss << std::showpoint << std::noshowpos << std::fixed << std::setprecision(2);
    ss << '\n' << NNUE::trace(pos) << '\n';

    ss << std::showpoint << std::showpos << std::fixed << std::setprecision(2) << std::setw(15);

    Value v;
    v = NNUE::evaluate<NNUE::Big>(pos, false);
    v = pos.side_to_move() == WHITE ? v : -v;
    ss << "NNUE evaluation        " << 0.01 * UCI::to_cp(v) << " (white side)\n";

    v = evaluate(pos, VALUE_ZERO);
    v = pos.side_to_move() == WHITE ? v : -v;
    ss << "Final evaluation       " << 0.01 * UCI::to_cp(v) << " (white side)";
    ss << " [with scaled NNUE, ...]";
    ss << "\n";

    return ss.str();
}

}  // namespace Stockfish

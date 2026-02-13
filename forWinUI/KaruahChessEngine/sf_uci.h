/*
  Stockfish, a UCI chess playing engine derived from Glaurung 2.1
  Copyright (C) 2004-2025 The Stockfish developers (see AUTHORS file)

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

#ifndef UCI_H_INCLUDED
#define UCI_H_INCLUDED

#include <cstdint>
#include <iostream>
#include <string>
#include <string_view>

#include "sf_engine.h"
#include "sf_misc.h"
#include "sf_search.h"

namespace Stockfish {

class Position;
class Move;
class Score;
enum Square : int;
using Value = int;

class UCIEngine {
   public:
    UCIEngine();

   
    static int         to_cp(Value v, const Position& pos);
    
    static std::string square(Square s);
    static std::string move(Move m, bool chess960);
    static std::string wdl(Value v, const Position& pos);
    static std::string to_lower(std::string str);
    static Move        to_move(const Position& pos, std::string str);

    static Search::LimitsType parse_limits(std::istream& is);

    auto& engine_options() { return engine.get_options(); }

    // Karuah Chess - Making public so it can be accessed from app
    Engine      engine;

   private:
        
    void          position(std::istringstream& is);
    void          setoption(std::istringstream& is);
    
    static void on_update_no_moves(const Engine::InfoShort& info);
    static void on_update_full(const Engine::InfoFull& info, bool showWDL);
    static void on_iter(const Engine::InfoIter& info);
    
    void init_search_update_listeners();
};

}  // namespace Stockfish

#endif  // #ifndef UCI_H_INCLUDED

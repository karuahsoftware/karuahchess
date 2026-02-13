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

#include "sf_misc.h"

#include <array>
#include <atomic>
#include <cassert>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <iomanip>
#include <iostream>
#include <iterator>
#include <limits>
#include <mutex>
#include <sstream>
#include <string_view>

#include "sf_types.h"

#include "engine.h"
#include "helper.h"

namespace Stockfish {

namespace {

// Version number or dev.
constexpr std::string_view version = "17.1";


}  // namespace


// Returns the full name of the current Stockfish version.
//
// For local dev compiles we try to append the commit SHA and
// commit date from git. If that fails only the local compilation
// date is set and "nogit" is specified:
//      Stockfish dev-YYYYMMDD-SHA
//      or
//      Stockfish dev-YYYYMMDD-nogit
//
// For releases (non-dev builds) we only include the version number:
//      Stockfish version
std::string engine_version_info() {
    std::stringstream ss;
    ss << "Stockfish " << version << std::setfill('0');

    if constexpr (version == "dev")
    {
        ss << "-";
#ifdef GIT_DATE
        ss << stringify(GIT_DATE);
#else
        constexpr std::string_view months("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec");

        std::string       month, day, year;
        std::stringstream date(__DATE__);  // From compiler, format is "Sep 21 2008"

        date >> month >> day >> year;
        ss << year << std::setw(2) << std::setfill('0') << (1 + months.find(month) / 4)
           << std::setw(2) << std::setfill('0') << day;
#endif

        ss << "-";

#ifdef GIT_SHA
        ss << stringify(GIT_SHA);
#else
        ss << "nogit";
#endif
    }

    return ss.str();
}



#ifdef NO_PREFETCH

void prefetch(const void*) {}

#else

void prefetch(const void* addr) {
      
#if defined(_MSC_VER) && (defined(_M_X64) || defined(_M_IX86))
    _mm_prefetch((char const*)addr, _MM_HINT_T0);
#elif defined(_MSC_VER) && (defined(_M_ARM) || defined(_M_ARM64))
    __prefetch(addr);
#else
    __builtin_prefetch(addr);
#endif

}

#endif


size_t str_to_size_t(const std::string& s) {
    unsigned long long value = std::stoull(s);
    if (value > std::numeric_limits<size_t>::max())
        // Karuah Chess - Lock engine without exiting
        KaruahChess::Engine::engineErr.add(KaruahChess::helper::STR_TO_SIZE_T_ERROR);
    return static_cast<size_t>(value);
}




}  // namespace Stockfish

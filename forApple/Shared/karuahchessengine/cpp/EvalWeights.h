/*
Karuah Chess is a chess playing program
Copyright (C) 2020 Karuah Software

Karuah Chess is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Karuah Chess is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

#pragma once
#include "helper.h"


	namespace EvalWeights {
				

		// Piece weights
		constexpr int PawnValueMg = 126;
		constexpr int PawnValueEg = 208;
		constexpr int KnightValueMg = 781;
		constexpr int KnightValueEg = 854;
		constexpr int BishopValueMg = 825;
		constexpr int BishopValueEg = 915;
		constexpr int RookValueMg = 1276; 
		constexpr int RookValueEg = 1380;
		constexpr int QueenValueMg = 2538;
		constexpr int QueenValueEg = 2682;
		
	}


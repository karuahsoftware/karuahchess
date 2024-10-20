/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2023 Karuah Software

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
#include <cstdint>

namespace KaruahChess {

	namespace PiecePattern {

		// Functions
		extern uint64_t Pattern(helper::PatternEnum pPattern, int pSqIndex, uint64_t pBlockers);
		extern uint64_t PatternXRay(helper::PatternEnum pPattern, int pSqIndex, uint64_t pBlockers);
		template<int Colour> extern uint64_t PawnEnpassant(const int pIndex, const int pEnpassantIndex, const uint64_t pWhitePawnPos, const uint64_t pBlackPawnPos);
		template<int Colour> extern uint64_t PawnPotentialAttack(const int pSqIndex);
		template<int Colour> extern uint64_t PawnPotentialAttackBB(const uint64_t p);
		template<int Colour> extern uint64_t PawnAttack(const int pSqIndex, const uint64_t pBlockers);
		template<int Colour> extern uint64_t PawnMove(const int pSqIndex, const uint64_t pBlockers);
		extern uint64_t Bishop(const int pIndex, const uint64_t pBlockers, const bool pXRay);
		extern uint64_t Rook(const int pIndex, const uint64_t pBlockers, const bool pXRay);
		extern uint64_t King(const int pIndex);
		template<int Colour> extern uint64_t KingCastle(const int pIndex, const uint64_t pWhitePos, const uint64_t pBlackPos, const uint64_t pUsRookPos, const uint64_t pThemAttack, const uint64_t pThemPawnPotentialAttack, const int pStateCastlingAvailability);
		extern uint64_t Knight(const int pIndex);
	}

}
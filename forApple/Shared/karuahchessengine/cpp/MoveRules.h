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
#include "bitboard.h"

namespace KaruahChess {
    namespace MoveRules {
        extern bool IsCheckMate(BitBoard& pBoard);
        extern bool IsStaleMate(BitBoard& pBoard);
        extern bool Move(const int pFromIndex, const int pToIndex, BitBoard& pBoard, const helper::PawnPromotionEnum pPawnPromotionPiece, const bool pValidateEnabled, const bool pCommit);
        extern bool Arrange(const int pFromIndex, const int pToIndex, BitBoard& pBoard);
        extern bool ArrangeUpdate(const char pFen, const int pToIndex, BitBoard& pBoard);
        extern int FindFromIndex(BitBoard& pBoard, const int pToIndex, const int pSpin, const std::vector<int> pValidFromIndexes, bool pTestMove);
        extern bool IsPawnPromotion(const int pFromIndex, const int pToIndex, BitBoard& pBoard);
    }

}

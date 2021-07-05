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
#include "BitBoard.h"



namespace PieceStructure {

	typedef bool (*featureFunction)(BitBoard&, int);


	// Feature Functions
	template<int Colour> extern bool IsPawnBackwards(BitBoard& pBoard, int pSqIndex);
	extern bool IsWhitePawnIsolated(BitBoard& pBoard, int pSqIndex);
	extern bool IsBlackPawnIsolated(BitBoard& pBoard, int pSqIndex);
	extern bool IsWhitePawnDoubled(BitBoard& pBoard, int pSqIndex);
	extern bool IsBlackPawnDoubled(BitBoard& pBoard, int pSqIndex);
	extern bool IsWhiteOutpost(BitBoard& pBoard, int pSqIndex);
	extern bool IsBlackOutpost(BitBoard& pBoard, int pSqIndex);
	extern bool IsProtectedByWhitePawn(BitBoard& pBoard, int pSqIndex);
	extern bool IsProtectedByBlackPawn(BitBoard& pBoard, int pSqIndex);
	extern bool IsBehindWhitePawn(BitBoard& pBoard, int pSqIndex);
	extern bool IsBehindBlackPawn(BitBoard& pBoard, int pSqIndex);
	extern bool IsOnOpenFile(BitBoard& pBoard, int pSqIndex);
	extern bool IsOnSemiOpenFileWhite(BitBoard& pBoard, int pSqIndex);
	extern bool IsOnSemiOpenFileBlack(BitBoard& pBoard, int pSqIndex);
	extern bool attacksPawn(BitBoard& pBoard, int pSqIndex);
	extern bool IsXRayAttackWhiteRookBishop(BitBoard& pBoard, int pSqIndex);
	extern bool IsXRayAttackBlackRookBishop(BitBoard& pBoard, int pSqIndex);
	template<int Colour> extern int PawnCountOnSameColourSquare(BitBoard& pBoard, int pSqIndex);

	extern uint64_t GetFeature(BitBoard& pBoard, const int32_t pFeatureId);

	extern uint64_t GetWhiteMobility(BitBoard& pBoard);
	extern uint64_t GetBlackMobility(BitBoard& pBoard);

	template<int Colour> extern bool isKXK(BitBoard& pBoard);
	template<int Colour> extern bool isKBPsK(BitBoard& pBoard);
	template<int Colour> extern bool isKQKRPs(BitBoard& pBoard);
	extern bool oppositeBishops(BitBoard& pBoard);

}




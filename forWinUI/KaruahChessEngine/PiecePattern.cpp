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

#include "piecepattern.h"
#include "helper.h"
#include <cstdint>


	namespace PiecePattern {

		/// <summary>
		/// Gets a move pattern for a piece type, according to the arrangement of blockers
		/// </summary>		
		uint64_t Pattern(helper::PatternEnum pPattern, int pSqIndex, uint64_t pBlockers) {
			switch (pPattern)
			{
			case helper::PatternEnum::Knight: return helper::KnightMove[pSqIndex];
			case helper::PatternEnum::Bishop: return helper::DiagonalMove[pSqIndex][((helper::DiagonalRay[pSqIndex] & pBlockers) * helper::DiagonalMagic[pSqIndex]) >> 52];
			case helper::PatternEnum::Rook: return helper::HorizontalVerticalMove[pSqIndex][((helper::HorizontalVerticalRay[pSqIndex] & pBlockers) * helper::HorizontalVerticalMagic[pSqIndex]) >> 52];
			case helper::PatternEnum::Queen: return (helper::DiagonalMove[pSqIndex][((helper::DiagonalRay[pSqIndex] & pBlockers) * helper::DiagonalMagic[pSqIndex]) >> 52] ^
				helper::HorizontalVerticalMove[pSqIndex][((helper::HorizontalVerticalRay[pSqIndex] & pBlockers) * helper::HorizontalVerticalMagic[pSqIndex]) >> 52]);
			default: return 0ULL;
			}
		}

		/// <summary>
		/// Gets an XRay move pattern for a piece type, according to the arrangement of blockers
		/// XRays are only valid for Bishops, Rooks, and Queens.
		/// </summary>		
		uint64_t PatternXRay(helper::PatternEnum pPattern, int pSqIndex, uint64_t pBlockers) {
			switch (pPattern)
			{
			case helper::PatternEnum::Bishop: return helper::DiagonalMoveXRay[pSqIndex][((helper::DiagonalRay[pSqIndex] & pBlockers) * helper::DiagonalMagic[pSqIndex]) >> 52];
			case helper::PatternEnum::Rook: return helper::HorizontalVerticalMoveXRay[pSqIndex][((helper::HorizontalVerticalRay[pSqIndex] & pBlockers) * helper::HorizontalVerticalMagic[pSqIndex]) >> 52];
			case helper::PatternEnum::Queen: return (helper::DiagonalMoveXRay[pSqIndex][((helper::DiagonalRay[pSqIndex] & pBlockers) * helper::DiagonalMagic[pSqIndex]) >> 52] ^
				helper::HorizontalVerticalMoveXRay[pSqIndex][((helper::HorizontalVerticalRay[pSqIndex] & pBlockers) * helper::HorizontalVerticalMagic[pSqIndex]) >> 52]);

			default: return 0ULL;
			}
		}

		/// <summary>
		/// White pawn enpassant
		/// </summary>		
		template<int Colour> uint64_t PawnEnpassant(const int pIndex, const int pEnpassantIndex, const uint64_t pWhitePawnPos, const uint64_t pBlackPawnPos)
		{
			if constexpr (Colour == helper::WHITEPIECE) {
				uint64_t enPassantPath = 0;
				uint64_t rowMaskA = helper::RowMask[pIndex];
				uint64_t rowMaskB = helper::RowMask[pIndex] << 8;
				uint64_t sqBinary = helper::BITMASK >> pIndex;

				uint64_t sqEnpassantBinary = 0;
				if (pEnpassantIndex > -1) {
					sqEnpassantBinary = helper::BITMASK >> pEnpassantIndex;
					if (((sqBinary >> 1) & rowMaskA & pBlackPawnPos) == sqEnpassantBinary) enPassantPath = (sqBinary << 7) & rowMaskB;
					if (((sqBinary << 1) & rowMaskA & pBlackPawnPos) == sqEnpassantBinary) enPassantPath = enPassantPath | ((sqBinary << 9) & rowMaskB);
				}

				return enPassantPath;
			}
			else {
				uint64_t enPassantPath = 0;
				uint64_t rowMaskA = helper::RowMask[pIndex];
				uint64_t rowMaskB = helper::RowMask[pIndex] >> 8;
				uint64_t sqBinary = helper::BITMASK >> pIndex;
				
				uint64_t sqEnpassantBinary = 0;
				if (pEnpassantIndex > -1) {
					sqEnpassantBinary = helper::BITMASK >> pEnpassantIndex;
					if (((sqBinary >> 1) & rowMaskA & pWhitePawnPos) == sqEnpassantBinary) enPassantPath = (sqBinary >> 9) & rowMaskB;
					if (((sqBinary << 1) & rowMaskA & pWhitePawnPos) == sqEnpassantBinary) enPassantPath = enPassantPath | ((sqBinary >> 7) & rowMaskB);
				}
				return enPassantPath;
			}
		}



		/// <summary>
		/// Pawn move pattern. This is a pawns non attacking move pattern.
		/// </summary>		
		template<int Colour> uint64_t PawnMove(const int pSqIndex, const uint64_t pBlockers)
		{
			if constexpr (Colour == helper::WHITEPIECE) {
				// Move paths            
				uint64_t sqBinary = helper::BITMASK >> pSqIndex;
				uint64_t movePath = sqBinary << 8 & ~pBlockers;
				if (pSqIndex >= 48 && pSqIndex <= 55 && movePath > 0) movePath |= sqBinary << 16 & ~pBlockers;
				return movePath;
			}
			else {
				// Move paths            
				uint64_t sqBinary = helper::BITMASK >> pSqIndex;
				uint64_t movePath = sqBinary >> 8 & ~pBlockers;
				if (pSqIndex >= 8 && pSqIndex <= 15 && movePath > 0) movePath |= sqBinary >> 16 & ~pBlockers;
				return movePath;
			}

		}



		/// <summary>
		/// Pawn attack pattern. This is what actually can be attacked by a pawn.
		/// </summary>		
		template<int Colour> uint64_t PawnAttack(const int pSqIndex, const uint64_t pBlockers)
		{
			if constexpr (Colour == helper::WHITEPIECE) {
				// Move paths            
				uint64_t sqBinary = helper::BITMASK >> pSqIndex;
				// Attack paths            
				uint64_t movePath = ((sqBinary << 9 & pBlockers) | (sqBinary << 7 & pBlockers)) & helper::RowMask[pSqIndex] << 8;
				return movePath;
			}
			else {
				// Move paths            
				uint64_t sqBinary = helper::BITMASK >> pSqIndex;
				// Attack paths            
				uint64_t movePath = ((sqBinary >> 9 & pBlockers) | (sqBinary >> 7 & pBlockers)) & helper::RowMask[pSqIndex] >> 8;
				return movePath;
			}

		}


		/// <summary>
		/// Pawn potential attack pattern. These are squares which are covered by a pawn but 
		/// may not have an opponents piece occupying them which means they cannot be immediately attacked.
		/// </summary>		
		template<int Colour> uint64_t PawnPotentialAttack(const int pSqIndex)
		{
			if constexpr (Colour == helper::WHITEPIECE) {
				// Move paths            
				uint64_t sqBinary = helper::BITMASK >> pSqIndex;
				// Attack paths            
				uint64_t movePath = ((sqBinary << 9) | (sqBinary << 7)) & helper::RowMask[pSqIndex] << 8;
				return movePath;
			}
			else {
				// Move paths            
				uint64_t sqBinary = helper::BITMASK >> pSqIndex;
				// Attack paths            
				uint64_t movePath = ((sqBinary >> 9) | (sqBinary >> 7)) & helper::RowMask[pSqIndex] >> 8;
				return movePath;
			}
		}

		/// <summary>
		/// Pawn potential attack pattern of multiple pawn positions specified by p
		/// </summary>		
		template<int Colour> uint64_t PawnPotentialAttackBB(const uint64_t p) {
			if constexpr (Colour == helper::WHITEPIECE) {
				return ((p & ~helper::FILEA) << 9) | ((p & ~helper::FILEH) << 7);
			}
			else {
				return ((p & ~helper::FILEH) >> 9) | ((p & ~helper::FILEA) >> 7);
			}
			
		}


		/// <summary>
		/// White bishop move pattern
		/// </summary>		
		uint64_t Bishop(const int pIndex, const uint64_t pBlockers, const bool pXRay)
		{
			uint64_t moves = 0ULL;
			uint64_t edge = helper::EDGEMASK_NS | helper::EDGEMASK_EW;
			if (pXRay) {
				moves = helper::NorthWestRay[pIndex] & (~(helper::NorthWestRay[63 - helper::BitScanForward((helper::NorthWestRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanForward(helper::NorthWestRay[pIndex] & (pBlockers | edge))))))]));
				moves |= helper::NorthEastRay[pIndex] & (~(helper::NorthEastRay[63 - helper::BitScanForward((helper::NorthEastRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanForward(helper::NorthEastRay[pIndex] & (pBlockers | edge))))))]));
				moves |= helper::SouthWestRay[pIndex] & (~(helper::SouthWestRay[63 - helper::BitScanReverse((helper::SouthWestRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanReverse(helper::SouthWestRay[pIndex] & (pBlockers | edge))))))]));
				moves |= helper::SouthEastRay[pIndex] & (~(helper::SouthEastRay[63 - helper::BitScanReverse((helper::SouthEastRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanReverse(helper::SouthEastRay[pIndex] & (pBlockers | edge))))))]));
			}
			else {
				moves = helper::NorthWestRay[pIndex] & (~(helper::NorthWestRay[63 - helper::BitScanForward(helper::NorthWestRay[pIndex] & (pBlockers | edge))]));
				moves |= helper::NorthEastRay[pIndex] & (~(helper::NorthEastRay[63 - helper::BitScanForward(helper::NorthEastRay[pIndex] & (pBlockers | edge))]));
				moves |= helper::SouthWestRay[pIndex] & (~(helper::SouthWestRay[63 - helper::BitScanReverse(helper::SouthWestRay[pIndex] & (pBlockers | edge))]));
				moves |= helper::SouthEastRay[pIndex] & (~(helper::SouthEastRay[63 - helper::BitScanReverse(helper::SouthEastRay[pIndex] & (pBlockers | edge))]));

			}

			return moves;

		}




		/// <summary>
		/// Rook move pattern
		/// </summary>		
		uint64_t Rook(const int pIndex, const uint64_t pBlockers, const bool pXRay)
		{

			uint64_t moves = 0ULL;
			uint64_t edge = 0ULL;
			uint64_t sqMask = helper::BITMASK >> pIndex;

			if ((sqMask & helper::EDGEMASK_N) > 0ULL) edge |= helper::EDGEMASK_S;
			else if ((sqMask & helper::EDGEMASK_S) > 0ULL) edge |= helper::EDGEMASK_N;
			else edge |= helper::EDGEMASK_NS;

			if ((sqMask & helper::EDGEMASK_E) > 0ULL) edge |= helper::EDGEMASK_W;
			else if ((sqMask & helper::EDGEMASK_W) > 0ULL) edge |= helper::EDGEMASK_E;
			else edge |= helper::EDGEMASK_EW;


			if (pXRay) {
				moves = helper::NorthRay[pIndex] & (~(helper::NorthRay[63 - helper::BitScanForward((helper::NorthRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanForward(helper::NorthRay[pIndex] & (pBlockers | edge))))))]));
				moves |= helper::EastRay[pIndex] & (~(helper::EastRay[63 - helper::BitScanReverse((helper::EastRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanReverse(helper::EastRay[pIndex] & (pBlockers | edge))))))]));
				moves |= helper::SouthRay[pIndex] & (~(helper::SouthRay[63 - helper::BitScanReverse((helper::SouthRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanReverse(helper::SouthRay[pIndex] & (pBlockers | edge))))))]));
				moves |= helper::WestRay[pIndex] & (~(helper::WestRay[63 - helper::BitScanForward((helper::WestRay[pIndex] & (pBlockers | edge)) & (~(helper::BITMASK >> (63 - helper::BitScanForward(helper::WestRay[pIndex] & (pBlockers | edge))))))]));
			}
			else {
				moves = helper::NorthRay[pIndex] & (~(helper::NorthRay[63 - helper::BitScanForward(helper::NorthRay[pIndex] & (pBlockers | edge))]));
				moves |= helper::EastRay[pIndex] & (~(helper::EastRay[63 - helper::BitScanReverse(helper::EastRay[pIndex] & (pBlockers | edge))]));
				moves |= helper::SouthRay[pIndex] & (~(helper::SouthRay[63 - helper::BitScanReverse(helper::SouthRay[pIndex] & (pBlockers | edge))]));
				moves |= helper::WestRay[pIndex] & (~(helper::WestRay[63 - helper::BitScanForward(helper::WestRay[pIndex] & (pBlockers | edge))]));
			}


			return moves;
		}


		/// <summary>
		/// King move pattern
		/// </summary>		
		uint64_t King(const int pIndex)
		{
			uint64_t sqBinary = helper::BITMASK >> pIndex;

			uint64_t northRowMask = helper::RowMask[pIndex] << 8;
			uint64_t southRowMask = helper::RowMask[pIndex] >> 8;
			uint64_t currentRowMask = helper::RowMask[pIndex];

			// Get next active square
			uint64_t nextSqN = (sqBinary << 8) & northRowMask;
			uint64_t nextSqS = (sqBinary >> 8) & southRowMask;
			uint64_t nextSqE = (sqBinary >> 1) & currentRowMask;
			uint64_t nextSqW = (sqBinary << 1) & currentRowMask;
			uint64_t nextSqNW = (sqBinary << 9) & northRowMask;
			uint64_t nextSqNE = (sqBinary << 7) & northRowMask;
			uint64_t nextSqSW = (sqBinary >> 7) & southRowMask;
			uint64_t nextSqSE = (sqBinary >> 9) & southRowMask;

			return nextSqN | nextSqS | nextSqE | nextSqW | nextSqNW | nextSqNE | nextSqSW | nextSqSE;

		}

		/// <summary>
		/// White king castle move pattern
		/// </summary>		
		template<int Colour> uint64_t KingCastle(const int pIndex, const uint64_t pWhitePos, const uint64_t pBlackPos, const uint64_t pUsRookPos, const uint64_t pThemAttack, const uint64_t pThemPawnPotentialAttack, const int pStateCastlingAvailability)
		{
			if constexpr (Colour == helper::WHITEPIECE) {
				uint64_t movePath = 0;
				bool castlingAvailableKingSide = (pStateCastlingAvailability & 0b000010) > 0;
				bool castlingAvailableQueenSide = (pStateCastlingAvailability & 0b000001) > 0;
				uint64_t sqBinary = helper::BITMASK >> pIndex;
				uint64_t pos = pWhitePos | pBlackPos;

				if (castlingAvailableKingSide && ((sqBinary >> 1) & pos) == 0 && ((sqBinary >> 2) & pos) == 0 && ((sqBinary >> 3) & pUsRookPos) > 0 && ((sqBinary & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary >> 1) & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary >> 2) & (pThemAttack | pThemPawnPotentialAttack)) == 0))
				{
					movePath = sqBinary >> 2;
				}

				if (castlingAvailableQueenSide && ((sqBinary << 1) & pos) == 0 && ((sqBinary << 2) & pos) == 0 && ((sqBinary << 3) & pos) == 0 && ((sqBinary << 4) & pUsRookPos) > 0 && ((sqBinary & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary << 1) & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary << 2) & (pThemAttack | pThemPawnPotentialAttack)) == 0))
				{

					movePath = movePath | sqBinary << 2;
				}


				return movePath;
			}
			else {
				uint64_t movePath = 0;
				bool castlingAvailableKingSide = (pStateCastlingAvailability & 0b001000) > 0;
				bool castlingAvailableQueenSide = (pStateCastlingAvailability & 0b000100) > 0;
				uint64_t sqBinary = helper::BITMASK >> pIndex;
				uint64_t pos = pWhitePos | pBlackPos;

				if (castlingAvailableKingSide && ((sqBinary >> 1) & pos) == 0 && ((sqBinary >> 2) & pos) == 0 && ((sqBinary >> 3) & pUsRookPos) > 0 && ((sqBinary & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary >> 1) & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary >> 2) & (pThemAttack | pThemPawnPotentialAttack)) == 0))
				{
					movePath = sqBinary >> 2;
				}

				if (castlingAvailableQueenSide && ((sqBinary << 1) & pos) == 0 && ((sqBinary << 2) & pos) == 0 && ((sqBinary << 3) & pos) == 0 && ((sqBinary << 4) & pUsRookPos) > 0 && ((sqBinary & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary << 1) & (pThemAttack | pThemPawnPotentialAttack)) == 0) && (((sqBinary << 2) & (pThemAttack | pThemPawnPotentialAttack)) == 0))
				{
					movePath = movePath | sqBinary << 2;
				}


				return movePath;
			}
		}





		/// <summary>
		/// Knight move pattern
		/// </summary>		
		uint64_t Knight(const int pIndex)
		{
			const uint64_t sqBinary = helper::BITMASK >> pIndex;

			uint64_t northRowMaskA = helper::RowMask[pIndex] << 8;
			uint64_t northRowMaskB = helper::RowMask[pIndex] << 16;
			uint64_t northRowMaskC = helper::RowMask[pIndex] >> 8;
			uint64_t northRowMaskD = helper::RowMask[pIndex] >> 16;

			// Get next active square            
			uint64_t nextSqNWA = (sqBinary << 10) & northRowMaskA;
			uint64_t nextSqNWB = (sqBinary << 17) & northRowMaskB;
			uint64_t nextSqNEA = (sqBinary << 6) & northRowMaskA;
			uint64_t nextSqNEB = (sqBinary << 15) & northRowMaskB;
			uint64_t nextSqSWA = (sqBinary >> 6) & northRowMaskC;
			uint64_t nextSqSWB = (sqBinary >> 15) & northRowMaskD;
			uint64_t nextSqSEA = (sqBinary >> 10) & northRowMaskC;
			uint64_t nextSqSEB = (sqBinary >> 17) & northRowMaskD;

			return nextSqNWA | nextSqNWB | nextSqNEA | nextSqNEB | nextSqSWA | nextSqSWB | nextSqSEA | nextSqSEB;

		}



		// Explicit template instantiation
		template uint64_t PawnEnpassant<helper::WHITEPIECE>(const int pIndex, const int pEnpassantIndex, const uint64_t pWhitePawnPos, const uint64_t pBlackPawnPos);
		template uint64_t PawnEnpassant<helper::BLACKPIECE>(const int pIndex, const int pEnpassantIndex, const uint64_t pWhitePawnPos, const uint64_t pBlackPawnPos);
		template uint64_t PawnPotentialAttack<helper::WHITEPIECE>(const int pSqIndex);
		template uint64_t PawnPotentialAttack<helper::BLACKPIECE>(const int pSqIndex);
		template uint64_t PawnAttack<helper::WHITEPIECE>(const int pSqIndex, const uint64_t pBlockers);
		template uint64_t PawnAttack<helper::BLACKPIECE>(const int pSqIndex, const uint64_t pBlockers);
		template uint64_t PawnMove<helper::WHITEPIECE>(const int pSqIndex, const uint64_t pBlockers);
		template uint64_t PawnMove<helper::BLACKPIECE>(const int pSqIndex, const uint64_t pBlockers);
		template uint64_t KingCastle<helper::WHITEPIECE>(const int pIndex, const uint64_t pWhitePos, const uint64_t pBlackPos, const uint64_t pUsRookPos, const uint64_t pThemAttack, const uint64_t pThemPawnPotentialAttack, const int pStateCastlingAvailability);
		template uint64_t KingCastle<helper::BLACKPIECE>(const int pIndex, const uint64_t pWhitePos, const uint64_t pBlackPos, const uint64_t pUsRookPos, const uint64_t pThemAttack, const uint64_t pThemPawnPotentialAttack, const int pStateCastlingAvailability);
		template uint64_t PawnPotentialAttackBB<helper::WHITEPIECE>(const uint64_t p);
		template uint64_t PawnPotentialAttackBB<helper::BLACKPIECE>(const uint64_t p);
	}

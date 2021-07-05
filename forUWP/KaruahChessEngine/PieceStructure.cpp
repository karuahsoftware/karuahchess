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

#include "PieceStructure.h"
#include "BitBoard.h"
#include "helper.h"
#include "EvalWeights.h"


	namespace PieceStructure {

		using namespace helper;


		/// <summary>
		/// A pawn is backwards if the pawns on adjacent files are more forward than the pawn in question and 
		/// the pawn in question cannot move forward without being attacked.
		/// </summary>		
		template<int Colour> bool IsPawnBackwards(BitBoard& pBoard, int pSqIndex) {
			static_assert(Colour == WHITEPIECE || Colour == BLACKPIECE, "Invalid piece colour");

			if (Colour == WHITEPIECE) {
				uint64_t whitePawns = pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN);
				bool hasPawnsBehind = (whitePawns & helper::PawnStructureSouthRay[pSqIndex]) > 0ULL;
				bool pawnCanSafeMove = ((pBoard.GetAllAttackPaths<BLACKPIECE>() ^ pBoard.GetPotentialMove(pSqIndex)) & pBoard.GetPotentialMove(pSqIndex)) > 0ULL;
				uint64_t pawnFront = (helper::BITMASK >> pSqIndex) << 8;
				uint64_t whitePos = pBoard.GetOccupied<WHITEPIECE>();
				bool hasWhitePieceInFront = (whitePos & pawnFront) > 0ULL;

				return (!hasPawnsBehind) && (!pawnCanSafeMove) && (!hasWhitePieceInFront);
			}
			else {
				uint64_t blackPawns = pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN);
				bool hasPawnsBehind = (blackPawns & helper::PawnStructureNorthRay[pSqIndex]) > 0ULL;
				bool pawnCanSafeMove = ((pBoard.GetAllAttackPaths<WHITEPIECE>() ^ pBoard.GetPotentialMove(pSqIndex)) & pBoard.GetPotentialMove(pSqIndex)) > 0ULL;
				uint64_t pawnFront = (helper::BITMASK >> pSqIndex) >> 8;
				uint64_t blackPos = pBoard.GetOccupied<BLACKPIECE>();
				bool hasBlackPieceInFront = (blackPos & pawnFront) > 0ULL;

				return (!hasPawnsBehind) && (!pawnCanSafeMove) && (!hasBlackPieceInFront);
			}
		}

		

		/// <summary>
		/// A pawn is isolated if it has no like pawns on adjacent files
		/// </summary>		
		bool IsWhitePawnIsolated(BitBoard& pBoard, int pSqIndex) {
			uint64_t whitePawns = pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN);
			uint64_t rayNS = helper::PawnStructureNorthRay[pSqIndex] | helper::PawnStructureSouthRay[pSqIndex];
			return (whitePawns & rayNS) == 0ULL;
		}

		/// <summary>
		/// A pawn is isolated if it has no like pawns on adjacent files
		/// </summary>		
		bool IsBlackPawnIsolated(BitBoard& pBoard, int pSqIndex) {
			uint64_t blackPawns = pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN);
			uint64_t rayNS = helper::PawnStructureNorthRay[pSqIndex] | helper::PawnStructureSouthRay[pSqIndex];

			return (blackPawns & rayNS) == 0ULL;
		}

		/// <summary>
		/// A pawn is doubled when it is on the same file as another pawn
		/// </summary>		
		bool IsWhitePawnDoubled(BitBoard& pBoard, int pSqIndex) {
			uint64_t whitePawns = pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN);
			uint64_t rayNS = helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex];

			return (rayNS & whitePawns) > 0;
		}

		/// <summary>
		/// A pawn is doubled when it is on the same file as another pawn
		/// </summary>		
		bool IsBlackPawnDoubled(BitBoard& pBoard, int pSqIndex) {
			uint64_t blackPawns = pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN);
			uint64_t rayNS = helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex];

			return (rayNS & blackPawns) > 0;
		}

		/// <summary>
		/// A white square is an outpost if it is not being attacked by the opponent and is on the 4th to 7th ranks.
		/// </summary>		
		bool IsWhiteOutpost(BitBoard& pBoard, int pSqIndex) {
			uint64_t safeSquares = ~pBoard.GetAllAttackPaths<BLACKPIECE>();
			bool isOutPostRank = pSqIndex >= 8 && pSqIndex <= 39 ? true : false;
			return isOutPostRank && ((safeSquares & (helper::BITMASK >> pSqIndex)) > 0);
		}

		/// <summary>
		/// A black square is an outpost if it is not being attacked by the opponent and is on the 2nd to 5th ranks.
		/// </summary>		
		bool IsBlackOutpost(BitBoard& pBoard, int pSqIndex) {
			uint64_t safeSquares = ~pBoard.GetAllAttackPaths<WHITEPIECE>();
			bool isOutPostRank = pSqIndex >= 24 && pSqIndex <= 55 ? true : false;
			return isOutPostRank && ((safeSquares & (helper::BITMASK >> pSqIndex)) > 0);
		}

		/// <summary>
		/// A square is pawn supported if it is protected by a pawn
		/// </summary>		
		bool IsProtectedByWhitePawn(BitBoard& pBoard, int pSqIndex) {
			uint64_t pawnAttackPaths = pBoard.GetProtectPawnPaths<WHITEPIECE>();
			return (pawnAttackPaths & (helper::BITMASK >> pSqIndex)) > 0;
		}

		/// <summary>
		/// A square is pawn supported if it is protected by a pawn
		/// </summary>		
		bool IsProtectedByBlackPawn(BitBoard& pBoard, int pSqIndex) {
			uint64_t pawnAttackPaths = pBoard.GetProtectPawnPaths<BLACKPIECE>();
			return (pawnAttackPaths & (helper::BITMASK >> pSqIndex)) > 0;
		}

		/// <summary>
		/// Returns true if the square in front of the given pSqIndex is a pawn
		/// </summary>		
		bool IsBehindWhitePawn(BitBoard& pBoard, int pSqIndex) {
			uint64_t currentSq = helper::BITMASK >> pSqIndex;
			uint64_t nextSq = currentSq << 8;
			uint64_t pawns = pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN);
			return (pawns & nextSq) > 0;
		}

		/// <summary>
		/// Returns true if the square in front of the given pSqIndex is a pawn
		/// </summary>		
		bool IsBehindBlackPawn(BitBoard& pBoard, int pSqIndex) {
			uint64_t currentSq = helper::BITMASK >> pSqIndex;
			uint64_t nextSq = currentSq >> 8;
			uint64_t pawns = pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN);
			return (pawns & nextSq) > 0;
		}

		/// <summary>
		/// Returns true if the square is on an open file
		/// </summary>		
		bool IsOnOpenFile(BitBoard& pBoard, int pSqIndex) {
			return ((helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex] | helper::BITMASK >> pSqIndex) & (pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN) | pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN))) == 0ULL;
		}

		/// <summary>
		/// Returns true if on a semi open file
		/// </summary>		
		bool IsOnSemiOpenFileWhite(BitBoard& pBoard, int pSqIndex) {
			uint64_t whitePawnOnFile = ((helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex] | helper::BITMASK >> pSqIndex) & pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN));
			uint64_t blackPawnOnFile = ((helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex] | helper::BITMASK >> pSqIndex) & pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN));

			return whitePawnOnFile == 0ULL && blackPawnOnFile > 0ULL;
		}

		/// <summary>
		/// Returns true if on a semi open file
		/// </summary>		
		bool IsOnSemiOpenFileBlack(BitBoard& pBoard, int pSqIndex) {
			uint64_t whitePawnOnFile = ((helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex] | helper::BITMASK >> pSqIndex) & pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN));
			uint64_t blackPawnOnFile = ((helper::NorthRay[pSqIndex] | helper::SouthRay[pSqIndex] | helper::BITMASK >> pSqIndex) & pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN));

			return whitePawnOnFile > 0ULL && blackPawnOnFile == 0ULL;
		}

		/// <summary>
		/// Returns true if attacks opponents pawn
		/// </summary>		
		bool attacksPawn(BitBoard& pBoard, int pSqIndex) {
			uint64_t whitePawnsUnderAttack = pBoard.GetPotentialMove(pSqIndex) & pBoard.GetOccupiedBySpin(helper::WHITE_PAWN_SPIN);
			uint64_t blackPawnsUnderAttack = pBoard.GetPotentialMove(pSqIndex) & pBoard.GetOccupiedBySpin(helper::BLACK_PAWN_SPIN);
			return whitePawnsUnderAttack > 0ULL || blackPawnsUnderAttack > 0ULL;
		}

		/// <summary>
		/// Returns true if the square is under an XRay attack from a rook or bishop
		/// </summary>		
		bool IsXRayAttackWhiteRookBishop(BitBoard& pBoard, int pSqIndex) {
			return ((pBoard.GetXRayAttackPathBySpin(helper::WHITE_ROOK_SPIN) | pBoard.GetXRayAttackPathBySpin(helper::WHITE_BISHOP_SPIN)) & (helper::BITMASK >> pSqIndex)) > 0ULL;
		}

		/// <summary>
		/// Returns true if the square is under an XRay attack from a rook or bishop
		/// </summary>		
		bool IsXRayAttackBlackRookBishop(BitBoard& pBoard, int pSqIndex) {
			return ((pBoard.GetXRayAttackPathBySpin(helper::BLACK_ROOK_SPIN) | pBoard.GetXRayAttackPathBySpin(helper::BLACK_BISHOP_SPIN)) & (helper::BITMASK >> pSqIndex)) > 0ULL;
		}

		/// <summary>
		/// Gets the white mobility area
		/// </summary>		
		uint64_t GetWhiteMobility(BitBoard& pBoard) {
			// Get blocked pawns on the first two pawn ranks
			uint64_t blockedPawns = pBoard.GetBlockedPawns<WHITEPIECE>() & (helper::RANK2 | helper::RANK3);
			// Squares occupied by our low rank blocked pawns, king, or controlled by enemy pawns are excluded from the mobility area.
			uint64_t mobilityArea = ~(blockedPawns | pBoard.GetPositionsOfSpin(helper::WHITE_KING_SPIN) | pBoard.GetProtectPawnPaths<BLACKPIECE>());
			return mobilityArea;
		}

		/// <summary>
		/// Gets the black mobility area
		/// </summary>		
		uint64_t GetBlackMobility(BitBoard& pBoard) {
			// Get blocked pawns on the first two pawn ranks
			uint64_t blockedPawns = pBoard.GetBlockedPawns<BLACKPIECE>() & (helper::RANK6 | helper::RANK7);
			// Squares occupied by our low rank blocked pawns, king, or controlled by enemy pawns are excluded from the mobility area.
			uint64_t mobilityArea = ~(blockedPawns | pBoard.GetPositionsOfSpin(helper::BLACK_KING_SPIN) | pBoard.GetProtectPawnPaths<WHITEPIECE>());
			return mobilityArea;
		}

		
		/// <summary>
		/// Gets a count of the white pawns on the same colour square
		/// </summary>
		/// <returns></returns>
		template<int Colour> int PawnCountOnSameColourSquare(BitBoard& pBoard, int pSqIndex) {		
			return popcount(pBoard.GetOccupiedBySpin<Colour>(PAWN_SPIN) & ((BLACKSQUARES & (BITMASK >> pSqIndex)) ? BLACKSQUARES : WHITESQUARES));				
		}
				

		/// <summary>
		/// Gets all the features using the specified function
		/// </summary>		
		uint64_t CollectFeature(uint64_t pSquares, BitBoard& pBoard, featureFunction pFeatureFunc) {
			uint64_t occupiedScan = pSquares;
			uint64_t result = 0ULL;

			int posScan;
			int sqIndex;
			uint64_t sqMask;

			while (occupiedScan > 0)
			{
				posScan = helper::BitScanForward(occupiedScan);
				sqMask = 1uLL << posScan;
				occupiedScan ^= sqMask;
				sqIndex = (63 - posScan);
				if ((*pFeatureFunc)(pBoard, sqIndex)) result |= sqMask;
			}

			return result;
		}

		/// <summary>
		/// Gets the feature as specified by the feature id
		/// </summary>		
		uint64_t GetFeature(BitBoard& pBoard, const int32_t pFeatureId) {
			uint64_t featureValue = 0ULL;
			uint64_t pos = 0ULL;
			switch (pFeatureId) {
			case 0:
				pos = pBoard.GetOccupiedBySpin(WHITE_PAWN_SPIN);
				featureValue = CollectFeature(pos, pBoard, IsPawnBackwards<WHITEPIECE>);
				break;
			case 1:
				pos = pBoard.GetOccupiedBySpin(BLACK_PAWN_SPIN);
				featureValue = CollectFeature(pos, pBoard, IsPawnBackwards<BLACKPIECE>);
				break;
			case 2:
				pos = pBoard.GetOccupiedBySpin(WHITE_PAWN_SPIN);
				featureValue = CollectFeature(pos, pBoard, IsWhitePawnIsolated);
				break;
			case 3:
				pos = pBoard.GetOccupiedBySpin(BLACK_PAWN_SPIN);
				featureValue = CollectFeature(pos, pBoard, IsBlackPawnIsolated);
				break;
			case 4:
				pos = pBoard.GetOccupiedBySpin(WHITE_PAWN_SPIN);
				featureValue = CollectFeature(pos, pBoard, IsWhitePawnDoubled);
				break;
			case 5:
				pos = pBoard.GetOccupiedBySpin(BLACK_PAWN_SPIN);
				featureValue = CollectFeature(pos, pBoard, IsBlackPawnDoubled);
				break;
			case 6:
				pos = pBoard.GetOccupied(WHITEPIECE) & (~pBoard.GetOccupiedBySpin(WHITE_PAWN_SPIN)) & (~pBoard.GetOccupiedBySpin(WHITE_KING_SPIN));
				featureValue = CollectFeature(pos, pBoard, IsWhiteOutpost);
				break;
			case 7:
				pos = pBoard.GetOccupied(BLACKPIECE) & (~pBoard.GetOccupiedBySpin(BLACK_PAWN_SPIN)) & (~pBoard.GetOccupiedBySpin(BLACK_KING_SPIN));
				featureValue = CollectFeature(pos, pBoard, IsBlackOutpost);
				break;
			case 8:
				pos = pBoard.GetOccupied(WHITEPIECE);
				featureValue = CollectFeature(pos, pBoard, IsProtectedByWhitePawn);
				break;
			case 9:
				pos = pBoard.GetOccupied(BLACKPIECE);
				featureValue = CollectFeature(pos, pBoard, IsProtectedByBlackPawn);
				break;
			case 10:
				pos = pBoard.GetOccupied(WHITEPIECE);
				featureValue = CollectFeature(pos, pBoard, IsBehindWhitePawn);
				break;
			case 11:
				pos = pBoard.GetOccupied(BLACKPIECE);
				featureValue = CollectFeature(pos, pBoard, IsBehindBlackPawn);
				break;
			case 12:
				pos = pBoard.GetOccupied(WHITEPIECE) | pBoard.GetOccupied(BLACKPIECE);
				featureValue = CollectFeature(pos, pBoard, IsOnOpenFile);
				break;
			case 13:
				pos = pBoard.GetOccupied(WHITEPIECE);
				featureValue = CollectFeature(pos, pBoard, IsOnSemiOpenFileWhite);
				break;
			case 14:
				pos = pBoard.GetOccupied(BLACKPIECE);
				featureValue = CollectFeature(pos, pBoard, IsOnSemiOpenFileBlack);
				break;
			case 15:
				featureValue = pBoard.GetAllAttackPaths<WHITEPIECE>();
				break;
			case 16:
				featureValue = pBoard.GetAllAttackPaths<BLACKPIECE>();
				break;
			case 17:
				pos = pBoard.GetOccupied(WHITEPIECE) | pBoard.GetOccupied(BLACKPIECE);
				featureValue = CollectFeature(pos, pBoard, attacksPawn);
				break;
			case 18:
				featureValue = pBoard.GetSliderBlockers();
				break;
			case 19:
				for (int i = 0; i < 64; i++) {
					featureValue |= IsXRayAttackWhiteRookBishop(pBoard, i) ? BITMASK >> i : 0ULL;
					featureValue |= IsXRayAttackBlackRookBishop(pBoard, i) ? BITMASK >> i : 0ULL;
				}
				break;
			case 20:
				featureValue |= pBoard.GetBlockedPawns<WHITEPIECE>();
				featureValue |= pBoard.GetBlockedPawns<BLACKPIECE>();
				break;
			case 21:
				featureValue |= GetWhiteMobility(pBoard);
				break;
			case 22:
				featureValue |= GetBlackMobility(pBoard);
				break;
			default:
				featureValue = 0ULL;
			}

			return featureValue;
		}

		// King and Plenty of material Vs a lone king
		template<int Colour> bool isKXK(BitBoard& pBoard) {
			return (!moreThanOne(pBoard.GetOccupied<-Colour>())) && (pBoard.GetMajorPieceMaterialValue<Colour>() >= EvalWeights::RookValueMg);
		}


		// Board contains King, single Bishop, and one or more Pawns of the specified colour
		template<int Colour> bool isKBPsK(BitBoard& pBoard) {
			return (pBoard.GetMajorPieceMaterialValue<Colour>() == EvalWeights::BishopValueMg) && (pBoard.Count<Colour>(PAWN_SPIN) >= 1);
		}

		// Board contains King, Queen vs Single Rook, and many pawns
		template<int Colour> bool isKQKRPs(BitBoard& pBoard) {
			return  (!pBoard.Count<Colour>(PAWN_SPIN))
				&& (pBoard.GetMajorPieceMaterialValue<Colour>() == EvalWeights::QueenValueMg)
				&& (pBoard.Count<-Colour>(ROOK_SPIN) == 1)
				&& (pBoard.Count<-Colour> (PAWN_SPIN) >= 1);
		}

		// Returns true a single bishop of each colour remains and they are on opposite board square colours
		bool oppositeBishops(BitBoard& pBoard) {
			return pBoard.Count<WHITEPIECE>(BISHOP_SPIN) == 1
				&& pBoard.Count<BLACKPIECE>(BISHOP_SPIN) == 1
				&& oppositeColors(63 - BitScanForward(pBoard.GetOccupiedBySpin<WHITEPIECE>(BISHOP_SPIN)), 63 - BitScanForward(pBoard.GetOccupiedBySpin<BLACKPIECE>(BISHOP_SPIN)));
		}


		// Explicit template instantiation
		template bool IsPawnBackwards<WHITEPIECE>(BitBoard& pBoard, int pSqIndex);
		template bool IsPawnBackwards<BLACKPIECE>(BitBoard& pBoard, int pSqIndex);
		template int PawnCountOnSameColourSquare<WHITEPIECE>(BitBoard& pBoard, int pSqIndex);
		template int PawnCountOnSameColourSquare<BLACKPIECE>(BitBoard& pBoard, int pSqIndex);
		template bool isKXK<WHITEPIECE>(BitBoard& pBoard);
		template bool isKXK<BLACKPIECE>(BitBoard& pBoard);
		template bool isKBPsK<WHITEPIECE>(BitBoard& pBoard);
		template bool isKBPsK<BLACKPIECE>(BitBoard& pBoard);
		template bool isKQKRPs<WHITEPIECE>(BitBoard& pBoard);
		template bool isKQKRPs<BLACKPIECE>(BitBoard& pBoard);

	}




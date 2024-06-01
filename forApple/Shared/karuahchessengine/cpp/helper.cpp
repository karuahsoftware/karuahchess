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

#include "helper.h"
#include "piecepattern.h"
#include "bitboard.h"
#include <cstdint>
#include <algorithm>
#include <string>
#include <map>
#include <array>
#include <vector>
#include <sstream>
#include <bitset>
#include <filesystem>


#if defined(_MSC_VER)
#include <intrin.h> // This is only valid for the msvc build
#endif


namespace helper {

	bool Initialised = false;


    uint64_t RowMask[64] { 0 };
    uint64_t NorthRay[64] { 0 };
	uint64_t SouthRay[64] { 0 };
	uint64_t EastRay[64] { 0 };;
	uint64_t WestRay[64] { 0 };
	uint64_t NorthWestRay[64] { 0 };
	uint64_t NorthEastRay[64] { 0 };
	uint64_t SouthWestRay[64] { 0 };
	uint64_t SouthEastRay[64] { 0 };

	int CastleIndex[64][2] { 0 };

	uint64_t DiagonalRay[64] { 0 };
	uint64_t HorizontalVerticalRay[64] { 0 };
	uint64_t HorizontalVerticalMove[64][4097] { 0 };
	uint64_t HorizontalVerticalMoveXRay[64][4097] { 0 };
	uint64_t DiagonalMove[64][4097] { 0 };
	uint64_t DiagonalMoveXRay[64][4097] { 0 };
	uint64_t KnightMove[64] { 0 };
	uint64_t KingMove[64] { 0 };


	constexpr uint64_t BitScanMagic = 0x37E84A99DAE458F;
	constexpr int BitScanMagicTable[] = {
			0, 1, 17, 2, 18, 50, 3, 57,
			47, 19, 22, 51, 29, 4, 33, 58,
			15, 48, 20, 27, 25, 23, 52, 41,
			54, 30, 38, 5, 43, 34, 59, 8,
			63, 16, 49, 56, 46, 21, 28, 32,
			14, 26, 24, 40, 53, 37, 42, 7,
			62, 55, 45, 31, 13, 39, 36, 6,
			61, 44, 12, 35, 60, 11, 10, 9
	};


	// Initialise function
	void init() {

		if (Initialised) return;

		// Castle Indexes
		for (int i = 0; i < 64; i++)
		{
			CastleIndex[i][0] = -1;
			CastleIndex[i][1] = -1;
		}
		CastleIndex[62][0] = 63;
		CastleIndex[62][1] = 61;
		CastleIndex[58][0] = 56;
		CastleIndex[58][1] = 59;
		CastleIndex[2][0] = 0;
		CastleIndex[2][1] = 3;
		CastleIndex[6][0] = 7;
		CastleIndex[6][1] = 5;

		// Set row mask and rays
		for (int i = 0; i < 64; i++)
		{

			// Initialise row mask
			RowMask[i] = GetRowMask(i);

			// Initialise rays
			uint64_t ray[8] { 0 };
			CreateRay(i, ray);
			NorthRay[i] = ray[0];
			SouthRay[i] = ray[1];
			EastRay[i] = ray[2];
			WestRay[i] = ray[3];
			NorthWestRay[i] = ray[4];
			NorthEastRay[i] = ray[5];
			SouthWestRay[i] = ray[6];
			SouthEastRay[i] = ray[7];

			// Initialise structure rays
			uint64_t structureRay[2] { 0 };
			CreateStructureRay(i, structureRay);
		}


		CreateMoveLookupTable();

		Initialised = true;
	}


	/// <summary>
	/// Create lookup tables
	/// </summary>
	void CreateMoveLookupTable()
	{
		const int arraySize = 4096;
		uint64_t allBlockerCombinations[arraySize] { 0 };

		for (int sqIndex = 0; sqIndex <= 63; sqIndex++)
		{

			// Rays
			DiagonalRay[sqIndex] = GetRay(sqIndex, RayTypeEnum::Diagonal, true);
			HorizontalVerticalRay[sqIndex] = GetRay(sqIndex, RayTypeEnum::HorizontalVertical, true);


			// Blocker combinations - diagonals
			int blockerComboLength = CreateBlockerCombinationForRay(DiagonalRay[sqIndex], allBlockerCombinations);
			for (int blockerIndex = 0; blockerIndex < blockerComboLength; blockerIndex++)
			{
				uint64_t blocker = allBlockerCombinations[blockerIndex];
				uint64_t blockerKey = (blocker * DiagonalMagic[sqIndex]) >> 52;

				if (DiagonalMove[sqIndex][blockerKey] == 0)
				{
					uint64_t bishopPattern = PiecePattern::Bishop(sqIndex, blocker, false);
					DiagonalMove[sqIndex][blockerKey] = bishopPattern;
					uint64_t bishopXRayPattern = PiecePattern::Bishop(sqIndex, blocker, true);
					DiagonalMoveXRay[sqIndex][blockerKey] = bishopXRayPattern;
				}
				else
				{
					throw std::runtime_error("Move key is not unique.");
				}
			}

			// Zero array
			std::fill(std::begin(allBlockerCombinations), std::end(allBlockerCombinations), 0);

			// Blocker combinations - horizontal vertical
			blockerComboLength = CreateBlockerCombinationForRay(HorizontalVerticalRay[sqIndex], allBlockerCombinations);
			for (int blockerIndex = 0; blockerIndex < blockerComboLength; blockerIndex++)
			{
				uint64_t blocker = allBlockerCombinations[blockerIndex];
				uint64_t blockerKey = (blocker * HorizontalVerticalMagic[sqIndex]) >> 52;

				if (HorizontalVerticalMove[sqIndex][blockerKey] == 0)
				{
					uint64_t rookPattern = PiecePattern::Rook(sqIndex, blocker, false);
					HorizontalVerticalMove[sqIndex][blockerKey] = rookPattern;
					uint64_t rookXRayPattern = PiecePattern::Rook(sqIndex, blocker, true);
					HorizontalVerticalMoveXRay[sqIndex][blockerKey] = rookXRayPattern;
				}
				else
				{
					throw std::runtime_error("Move key is not unique.");
				}
			}

			// Add other move patterns
			KnightMove[sqIndex] = PiecePattern::Knight(sqIndex);
			KingMove[sqIndex] = PiecePattern::King(sqIndex);

		}

	}


	/// <summary>
	/// Create a blocker combination
	/// </summary>
	/// <param name="pRays"></param>
	/// <returns></returns>
	int CreateBlockerCombinationForRay(uint64_t pRays_NoEdge, uint64_t pAllBlockerCombinations[4096])
	{
		int map[12] { 0 };
		int mapIndex = 0;

		// Create the map
		for (int sqIndex = 0; sqIndex < 64; sqIndex++)
		{
			uint64_t sqMask = BITMASK >> sqIndex;
			if ((sqMask & pRays_NoEdge) > 0)
			{
				map[mapIndex] = sqIndex;
				mapIndex++;
			}
		}

		// Set the blocker bits
		uint32_t blockerBits = 0;
		for (int i = 0; i < mapIndex; i++)
		{
			blockerBits |= BLOCKERBITMASK << i;
		}

		// Loop through all the blocker bits;
		for (unsigned int i = 0; i <= blockerBits; i++)
		{
			uint64_t possibleBlocker = 0uL;
			for (int j = 0; j < mapIndex; j++)
			{
				if ((i & (BLOCKERBITMASK << j)) > 0) possibleBlocker |= BITMASK >> map[j];
			}

			pAllBlockerCombinations[i] = possibleBlocker;
		}

		return blockerBits + 1;
	}


	/// <summary>
	/// Gets a horizontal and vertical or diagonal ray at the specified square index
	/// </summary>
	/// <param name="pSqIndex"></param>
	/// <param name="pRayType"></param>
	/// <returns></returns>
	uint64_t GetRay(int pSqIndex, RayTypeEnum pRayType, bool pExcludeEdge)
	{
		if (pRayType == RayTypeEnum::HorizontalVertical)
		{
			uint64_t rayN = NorthRay[pSqIndex];
			uint64_t rayS = SouthRay[pSqIndex];
			uint64_t rayE = EastRay[pSqIndex];
			uint64_t rayW = WestRay[pSqIndex];

			if (pExcludeEdge)
			{
				uint64_t ray_NoEdge = (rayN & (~EDGEMASK_NS)) | (rayS & (~EDGEMASK_NS)) | (rayE & (~EDGEMASK_EW)) | (rayW & (~EDGEMASK_EW));
				return ray_NoEdge;
			}
			else
			{
				uint64_t ray = rayN | rayS | rayE | rayW;
				return ray;
			}
		}
		else
		{
			uint64_t rayNW = NorthWestRay[pSqIndex];
			uint64_t rayNE = NorthEastRay[pSqIndex];
			uint64_t raySW = SouthWestRay[pSqIndex];
			uint64_t raySE = SouthEastRay[pSqIndex];

			if (pExcludeEdge)
			{
				uint64_t ray_NoEdge = (rayNW | rayNE | raySW | raySE) & (~(EDGEMASK_NS | EDGEMASK_EW));
				return ray_NoEdge;
			}
			else
			{
				uint64_t ray = (rayNW | rayNE | raySW | raySE);
				return ray;
			}
		}

	}


	/// <summary>
	/// Returns the index of the first bit set from the least significant bit
	/// </summary>
	/// <param name="b"></param>
	/// <returns></returns>
	int BitScanForward(uint64_t pNum)
	{
#if defined(_WIN64) && defined(_MSC_VER)
		unsigned long index = 0;
			if (pNum > 0) _BitScanForward64(&index, pNum);
			return index;
#elif defined(__GNUC__)
		int index = 0;
		if (pNum > 0) index = __builtin_ctzll(pNum);
		return index;
#else
		return BitScanMagicTable[((uint64_t)((int64_t)pNum & -(int64_t)pNum) * BitScanMagic) >> 58];
#endif
	}

	/// <summary>
	/// Returns the index of the first bit set from the most significant bit
	/// </summary>
	/// <param name="b"></param>
	/// <returns></returns>
	int BitScanReverse(uint64_t pNum)
	{
#if defined(_WIN64) && defined(_MSC_VER)
		unsigned long index = 0;
			if (pNum > 0) _BitScanReverse64(&index, pNum);
			return index;
#elif defined(__GNUC__)
		int index = 0;
		if (pNum > 0) index = 63 - __builtin_clzll(pNum);
		return index;
#else
		pNum |= pNum >> 1;
			pNum |= pNum >> 2;
			pNum |= pNum >> 4;
			pNum |= pNum >> 8;
			pNum |= pNum >> 16;
			pNum |= pNum >> 32;
			pNum = pNum & ~(pNum >> 1);
			return BitScanMagicTable[pNum * BitScanMagic >> 58];
#endif
	}

	/// <summary>
	/// Counts the number of ones in a 64 bit int
	/// </summary>
	/// <param name="pBits"></param>
	/// <returns></returns>
	int popcount(uint64_t pBits) {
#if defined(_WIN64) && defined(_MSC_VER) && defined(_M_X64)
		return (int)_mm_popcnt_u64(pBits);
#elif defined(__GNUC__)
		return __builtin_popcountll(pBits);
#else
		std::bitset<64> binaryBits(pBits);
            return binaryBits.count();
#endif
	}

	/// <summary>
	/// Create rays
	/// </summary>
	/// <param name="pIndex"></param>
	/// <param name="pRays"></param>
	/// <returns></returns>
	void CreateRay(int pIndex, uint64_t pRays[8])
	{

		uint64_t sqBinary = BITMASK >> pIndex;
		uint64_t nextSqN = sqBinary;
		uint64_t nextSqS = sqBinary;
		uint64_t nextSqE = sqBinary;
		uint64_t nextSqW = sqBinary;
		uint64_t nextSqNW = sqBinary;
		uint64_t nextSqNE = sqBinary;
		uint64_t nextSqSW = sqBinary;
		uint64_t nextSqSE = sqBinary;

		int offsetIndex = 8;

		int offsetA = 9;
		int offsetB = 7;
		int offsetC = 8;
		int offsetD = 1;

		do
		{
			
			uint64_t northRowMask = RowMask[pIndex] << offsetIndex;
			uint64_t southRowMask = RowMask[pIndex] >> offsetIndex;
			uint64_t currentRowMask = RowMask[pIndex];

			// Get next active square
			nextSqN = (nextSqN << offsetC) & northRowMask;
			nextSqS = (nextSqS >> offsetC) & southRowMask;
			nextSqE = (nextSqE >> offsetD) & currentRowMask;
			nextSqW = (nextSqW << offsetD) & currentRowMask;
			nextSqNW = (nextSqNW << offsetA) & northRowMask;
			nextSqNE = (nextSqNE << offsetB) & northRowMask;
			nextSqSW = (nextSqSW >> offsetB) & southRowMask;
			nextSqSE = (nextSqSE >> offsetA) & southRowMask;

			pRays[0] = pRays[0] | nextSqN;
			pRays[1] = pRays[1] | nextSqS;
			pRays[2] = pRays[2] | nextSqE;
			pRays[3] = pRays[3] | nextSqW;
			pRays[4] = pRays[4] | nextSqNW;
			pRays[5] = pRays[5] | nextSqNE;
			pRays[6] = pRays[6] | nextSqSW;
			pRays[7] = pRays[7] | nextSqSE;

			std::string g0 = GetBinaryStr(pRays[0]);
			std::string g1 = GetBinaryStr(pRays[1]);
			std::string g2 = GetBinaryStr(pRays[2]);
			std::string g3 = GetBinaryStr(pRays[3]);
			std::string g4 = GetBinaryStr(pRays[4]);
			std::string g5 = GetBinaryStr(pRays[5]);
			std::string g6 = GetBinaryStr(pRays[6]);
			std::string g7 = GetBinaryStr(pRays[7]);


			offsetIndex += 8;

		} while ((nextSqN != 0 || nextSqS != 0 || nextSqE != 0 || nextSqW != 0 || nextSqNW != 0 || nextSqNE != 0 || nextSqSW != 0 || nextSqSE != 0) && (offsetIndex < 64));


	}


	/// <summary>
	/// Create structure rays. Used to identify pawn structures
	/// </summary>
	/// <param name="pIndex"></param>
	/// <param name="pRays"></param>
	/// <returns></returns>
	void CreateStructureRay(int pIndex, uint64_t pRays[2])
	{
		// Get first square
		const uint64_t sqBinary = BITMASK >> pIndex;
		uint64_t nextSqNStart = ((sqBinary << 1) | (sqBinary >> 1)) & RowMask[pIndex];
		uint64_t nextSqSStart = ((sqBinary << 1) | (sqBinary >> 1)) & RowMask[pIndex];

		uint64_t nextSqN = 0ULL;
		uint64_t nextSqS = 0ULL;

		pRays[0] = nextSqNStart;
		pRays[1] = nextSqSStart;

		int offsetIndex = 8;
		do
		{
			// Get next squares
			nextSqN = (nextSqNStart << offsetIndex) & (RowMask[pIndex] << offsetIndex);
			nextSqS = (nextSqSStart >> offsetIndex) & (RowMask[pIndex] >> offsetIndex);

			pRays[0] = pRays[0] | nextSqN;
			pRays[1] = pRays[1] | nextSqS;

			offsetIndex += 8;

		} while ((nextSqN != 0 || nextSqS != 0) && offsetIndex < 64);
		// Ensure bitshift is always less then number of bits (64), otherwise results are undefined.


	}


	/// <summary>
	///  Gets a mask for a row
	/// </summary>
	/// <returns></returns>
	uint64_t GetRowMask(int pSqIndex)
	{

		uint64_t mask = 0;
		int shift = 0;
		bool outOfRange = false;

		if (pSqIndex >= 0 && pSqIndex <= 7) shift = 0;
		else if (pSqIndex >= 8 && pSqIndex <= 15) shift = 8;
		else if (pSqIndex >= 16 && pSqIndex <= 23) shift = 16;
		else if (pSqIndex >= 24 && pSqIndex <= 31) shift = 24;
		else if (pSqIndex >= 32 && pSqIndex <= 39) shift = 32;
		else if (pSqIndex >= 40 && pSqIndex <= 47) shift = 40;
		else if (pSqIndex >= 48 && pSqIndex <= 55) shift = 48;
		else if (pSqIndex >= 56 && pSqIndex <= 63) shift = 56;
		else outOfRange = true;


		if (!outOfRange)
		{
			mask = 0b11111111'00000000'00000000'00000000'00000000'00000000'00000000'00000000uLL >> shift;

			return mask;
		}
		else
		{
			return 0;
		}
	}


	/// <summary>
	/// Gets a binary string from a UInt, for debugging
	/// </summary>
	/// <param name="pInt"></param>
	/// <returns></returns>
	std::string GetBinaryStr(uint64_t pInt)
	{
		uint64_t mask = 0b10000000'00000000'00000000'00000000'00000000'00000000'00000000'00000000uLL;
		std::string binary = "";

		for (int i = 0; i < 64; i++)
		{
			if ((mask & pInt) > 0)
			{
				binary = binary + "1";
			}
			else
			{
				binary = binary + "0";
			}

			if ((i + 1) % 8 == 0 && i < 63) binary = binary + "_";
			mask >>= 1;

		}


		return binary;

	}


	const std::map<int, std::string> BoardCoordinateDict = {
			{0, "a8"}, { 1, "b8" }, { 2, "c8" }, { 3, "d8" }, { 4, "e8" }, { 5, "f8" }, { 6, "g8" }, { 7, "h8" },
			{ 8, "a7" }, { 9, "b7" }, { 10, "c7" }, { 11, "d7" }, { 12, "e7" }, { 13, "f7" }, { 14, "g7" }, { 15, "h7" },
			{ 16, "a6" }, { 17, "b6" }, { 18, "c6" }, { 19, "d6" }, { 20, "e6" }, { 21, "f6" }, { 22, "g6" }, { 23, "h6" },
			{ 24, "a5" }, { 25, "b5" }, { 26, "c5" }, { 27, "d5" }, { 28, "e5" }, { 29, "f5" }, { 30, "g5" }, { 31, "h5" },
			{ 32, "a4" }, { 33, "b4" }, { 34, "c4" }, { 35, "d4" }, { 36, "e4" }, { 37, "f4" }, { 38, "g4" }, { 39, "h4" },
			{ 40, "a3" }, { 41, "b3" }, { 42, "c3" }, { 43, "d3" }, { 44, "e3" }, { 45, "f3" }, { 46, "g3" }, { 47, "h3" },
			{ 48, "a2" }, { 49, "b2" }, { 50, "c2" }, { 51, "d2" }, { 52, "e2" }, { 53, "f2" }, { 54, "g2" }, { 55, "h2" },
			{ 56, "a1" }, { 57, "b1" }, { 58, "c1" }, { 59, "d1" }, { 60, "e1" }, { 61, "f1" }, { 62, "g1" }, { 63, "h1" }
	};


	/// <summary>
	/// Returns spin value from a FEN character
	/// </summary>
	/// <param name="pFENChar"></param>
	/// <returns></returns>
	int GetSpinFromChar(char pFENChar)
	{

		switch (pFENChar)
		{
			case 'p':
				return BLACK_PAWN_SPIN;
			case 'r':
				return BLACK_ROOK_SPIN;
			case 'n':
				return BLACK_KNIGHT_SPIN;
			case 'b':
				return BLACK_BISHOP_SPIN;
			case 'q':
				return BLACK_QUEEN_SPIN;
			case 'k':
				return BLACK_KING_SPIN;
			case 'P':
				return WHITE_PAWN_SPIN;
			case 'R':
				return WHITE_ROOK_SPIN;
			case 'N':
				return WHITE_KNIGHT_SPIN;
			case 'B':
				return WHITE_BISHOP_SPIN;
			case 'Q':
				return WHITE_QUEEN_SPIN;
			case 'K':
				return WHITE_KING_SPIN;
			default:
				return 0;
		}
	}

	/// <summary>
	/// Gets a FEN char from a spin value
	/// </summary>
	/// <param name="pSpin"></param>
	/// <returns></returns>
	char GetFENCharFromSpin(int pSpin)
	{

		switch (pSpin)
		{
			case BLACK_PAWN_SPIN:
				return 'p';
			case BLACK_ROOK_SPIN:
				return 'r';
			case BLACK_KNIGHT_SPIN:
				return 'n';
			case BLACK_BISHOP_SPIN:
				return 'b';
			case BLACK_QUEEN_SPIN:
				return 'q';
			case BLACK_KING_SPIN:
				return 'k';
			case WHITE_PAWN_SPIN:
				return 'P';
			case WHITE_ROOK_SPIN:
				return 'R';
			case WHITE_KNIGHT_SPIN:
				return 'N';
			case WHITE_BISHOP_SPIN:
				return 'B';
			case WHITE_QUEEN_SPIN:
				return 'Q';
			case WHITE_KING_SPIN:
				return 'K';
			default:
				return '0';
		}
	}

	/// <summary>
	/// Returns piece name from fen char
	/// </summary>
	/// <param name="pFENChar"></param>
	/// <returns></returns>
	std::string GetPieceNameFromChar(char pFENChar)
	{

		switch (pFENChar)
		{
			case 'p':
				return "Black Pawn";
			case 'r':
				return "Black Rook";
			case 'n':
				return "Black Knight";
			case 'b':
				return "Black Bishop";
			case 'q':
				return "Black Queen";
			case 'k':
				return "Black King";
			case 'P':
				return "White Pawn";
			case 'R':
				return "White Rook";
			case 'N':
				return "White Knight";
			case 'B':
				return "White Bishop";
			case 'Q':
				return "White Queen";
			case 'K':
				return "White King";
			default:
				return "";
		}

	}


	/// <summary>
	/// Returns the spin value from the piece name
	/// </summary>
	/// <param name="pPieceName"></param>
	/// <returns></returns>
	int GetSpinFromPieceName(std::string pPieceName)
	{

		if (pPieceName == "Black Pawn")
			return BLACK_PAWN_SPIN;
		else if (pPieceName == "Black Rook")
			return BLACK_ROOK_SPIN;
		else if (pPieceName == "Black Knight")
			return BLACK_KNIGHT_SPIN;
		else if (pPieceName == "Black Bishop")
			return BLACK_BISHOP_SPIN;
		else if (pPieceName == "Black Queen")
			return BLACK_QUEEN_SPIN;
		else if (pPieceName == "Black King")
			return BLACK_KING_SPIN;
		else if (pPieceName == "White Pawn")
			return WHITE_PAWN_SPIN;
		else if (pPieceName == "White Rook")
			return WHITE_ROOK_SPIN;
		else if (pPieceName == "White Knight")
			return WHITE_KNIGHT_SPIN;
		else if (pPieceName == "White Bishop")
			return WHITE_BISHOP_SPIN;
		else if (pPieceName == "White Queen")
			return WHITE_QUEEN_SPIN;
		else if (pPieceName == "White King")
			return WHITE_KING_SPIN;
		else
			return 0;
	}

	/// <summary>
	/// Splits a string
	/// </summary>
	void Split(const std::string& pStr, char pDelim, std::vector<std::string>& pReturnVector) {
		std::stringstream ss(pStr);
		std::string token;
		while (std::getline(ss, token, pDelim)) {
			pReturnVector.push_back(token);
		}
	}

}





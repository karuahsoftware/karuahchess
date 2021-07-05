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

#include "BitBoard.h"
#include "helper.h"
#include "PiecePattern.h"
#include "EvalWeights.h"
#include <cstdint>
#include <string>
#include <vector>


using namespace helper;

// Constructor
BitBoard::BitBoard()
{

	_positionWhitePawn = 0ULL;
	_positionWhiteKnight = 0ULL;
	_positionWhiteBishop = 0ULL;
	_positionWhiteRook = 0ULL;
	_positionWhiteQueen = 0ULL;
	_positionWhiteKing = 0ULL;
	_blank64 = 0ULL;
	_positionBlackPawn = 0ULL;
	_positionBlackKnight = 0ULL;
	_positionBlackBishop = 0ULL;
	_positionBlackRook = 0ULL;
	_positionBlackQueen = 0ULL;
	_positionBlackKing = 0ULL;

	// Put position addresses in to array to help performance
	_positions[0] = &_positionBlackKing;
	_positions[1] = &_positionBlackQueen;
	_positions[2] = &_positionBlackRook;
	_positions[3] = &_positionBlackBishop;
	_positions[4] = &_positionBlackKnight;
	_positions[5] = &_positionBlackPawn;
	_positions[6] = &_blank64;
	_positions[7] = &_positionWhitePawn;
	_positions[8] = &_positionWhiteKnight;
	_positions[9] = &_positionWhiteBishop;
	_positions[10] = &_positionWhiteRook;
	_positions[11] = &_positionWhiteQueen;
	_positions[12] = &_positionWhiteKing;
		
	_xRayAttacksBySpin[0] = &_blank64;
	_xRayAttacksBySpin[1] = &_blackQueenXRay;
	_xRayAttacksBySpin[2] = &_blackRookXRay;
	_xRayAttacksBySpin[3] = &_blackBishopXRay;
	_xRayAttacksBySpin[4] = &_blank64;
	_xRayAttacksBySpin[5] = &_blank64;
	_xRayAttacksBySpin[6] = &_blank64;
	_xRayAttacksBySpin[7] = &_blank64;
	_xRayAttacksBySpin[8] = &_blank64;
	_xRayAttacksBySpin[9] = &_whiteBishopXRay;
	_xRayAttacksBySpin[10] = &_whiteRookXRay;
	_xRayAttacksBySpin[11] = &_whiteQueenXRay;
	_xRayAttacksBySpin[12] = &_blank64;

	_whiteMajorPieceMaterialValue = 0;
	_blackMajorPieceMaterialValue = 0;
	

	Hash = 0ULL;
	HashPawn = 0ULL;
	_hashMaterial = 0ULL;
	StateActiveColour = 0;
	StateGameStatus = 0;
	StateCastlingAvailability = 0;
	StateEnpassantIndex = 0;
	StateHalfMoveCount = 0;
	StateFullMoveCount = 0;
	StateWhiteClockOffset = 0;
	StateBlackClockOffset = 0;
	ReturnMessage = "";

	MoveData[0] = -1;
	MoveData[1] = -1;
	MoveData[2] = 0;
	MoveData[3] = 0;
}

/// <summary>
/// Updates all the attack paths
/// </summary>
void BitBoard::CalculateAttackPaths()
{
	_whitePos = _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen | _positionWhiteKing;
	_blackPos = _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen | _positionBlackKing;
	uint64_t blockers = _whitePos | _blackPos;

	// 0 to 63 are squares.             
	int whiteKingIndex = -1;
	int blackKingIndex = -1;
	_whiteAttack = 0ULL;
	_blackAttack = 0ULL;
	_whiteAttackTwice = 0ULL;
	_blackAttackTwice = 0ULL;
	_whitePotentialAttackPawn = 0ULL;
	_blackPotentialAttackPawn = 0ULL;
	_whitePotentialAttackPawnTwice = 0ULL;
	_blackPotentialAttackPawnTwice = 0ULL;
	_blackBlockedPawn = 0ULL;
	_whiteBlockedPawn = 0ULL;
	_whiteBishopAttack = 0ULL;
	_blackBishopAttack = 0ULL;
	_whiteRookAttack = 0ULL;
	_blackRookAttack = 0ULL;
	_whiteQueenAttack = 0ULL;
	_blackQueenAttack = 0ULL;
	_whiteKnightAttack = 0ULL;
	_blackKnightAttack = 0ULL;
	_whiteKingAttack = 0ULL;
	_blackKingAttack = 0ULL;
	_whitePawnAttack = 0ULL;
	_blackPawnAttack = 0ULL;
	_whiteBishopXRay = 0ULL;
	_whiteRookXRay = 0ULL;
	_whiteQueenXRay = 0ULL;
	_blackBishopXRay = 0ULL;
	_blackRookXRay = 0ULL;
	_blackQueenXRay = 0ULL;
	uint64_t sqMask = 0ULL;

	_whiteMajorPieceMaterialValue = 0;
	_blackMajorPieceMaterialValue = 0;
	

	uint64_t occupiedScan = blockers;
	int sqIndex = 0;
	int posScan = 0;

	// Zero attack path		
	std::fill(std::begin(_attackPath), std::end(_attackPath), 0uLL);
	std::fill(std::begin(_attackPathXRay), std::end(_attackPathXRay), 0uLL);
	std::fill(std::begin(_nonAttackPawnPath), std::end(_nonAttackPawnPath), 0uLL);
	std::fill(std::begin(_castlePath), std::end(_castlePath), 0uLL);
	std::fill(std::begin(_potentialAttackPawnPath), std::end(_potentialAttackPawnPath), 0uLL);

	while (occupiedScan > 0)
	{
		posScan = helper::BitScanForward(occupiedScan);
		occupiedScan ^= 1uLL << posScan;
		sqIndex = (63 - posScan);

		sqMask = helper::BITMASK >> sqIndex;
		_attackPath[sqIndex] = 0ULL;
		_nonAttackPawnPath[sqIndex] = 0ULL;

		if ((sqMask & _positionWhitePawn) > 0)
		{
			_nonAttackPawnPath[sqIndex] = PiecePattern::PawnMove<WHITEPIECE>(sqIndex, blockers);
			_potentialAttackPawnPath[sqIndex] = PiecePattern::PawnPotentialAttack<WHITEPIECE>(sqIndex);
			_attackPath[sqIndex] = PiecePattern::PawnAttack<WHITEPIECE>(sqIndex, blockers);
			_attackPath[sqIndex] |= PiecePattern::PawnEnpassant<WHITEPIECE>(sqIndex, StateEnpassantIndex);		
			_whiteAttackTwice |= _whiteAttack & _attackPath[sqIndex];
			_whiteAttack |= _attackPath[sqIndex];
			_whitePawnAttack |= _attackPath[sqIndex];
			_whitePotentialAttackPawnTwice |= _whitePotentialAttackPawn & _potentialAttackPawnPath[sqIndex];
			_whitePotentialAttackPawn |= _potentialAttackPawnPath[sqIndex];			
			if ((_nonAttackPawnPath[sqIndex] | _attackPath[sqIndex]) == 0ULL) _whiteBlockedPawn |= sqMask;
			
		}
		else if ((sqMask & _positionWhiteBishop) > 0)
		{
			_attackPath[sqIndex] = helper::DiagonalMove[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52];
			_attackPathXRay[sqIndex] = helper::DiagonalMoveXRay[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52];
			_whiteBishopXRay |= _attackPathXRay[sqIndex];
			_whiteBishopAttack |= _attackPath[sqIndex];
			_whiteAttackTwice |= _whiteAttack & _attackPath[sqIndex];
			_whiteAttack |= _attackPath[sqIndex];
			_whiteMajorPieceMaterialValue += EvalWeights::BishopValueMg;
				
		}
		else if ((sqMask & _positionWhiteQueen) > 0)
		{
			// Queen pattern is the bishop pattern xor with rook pattern                  
			_attackPath[sqIndex] = (helper::DiagonalMove[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52] ^
				helper::HorizontalVerticalMove[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52]);
			_attackPathXRay[sqIndex] = (helper::DiagonalMoveXRay[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52] ^
				helper::HorizontalVerticalMoveXRay[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52]);
			_whiteQueenXRay |= _attackPathXRay[sqIndex];
			_whiteQueenAttack |= _attackPath[sqIndex];
			_whiteAttackTwice |= _whiteAttack & _attackPath[sqIndex];
			_whiteAttack |= _attackPath[sqIndex];
			_whiteMajorPieceMaterialValue += EvalWeights::QueenValueMg;
		}
		else if ((sqMask & _positionWhiteRook) > 0)
		{
			_attackPath[sqIndex] = helper::HorizontalVerticalMove[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52];
			_attackPathXRay[sqIndex] = helper::HorizontalVerticalMoveXRay[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52];
			_whiteRookXRay |= _attackPathXRay[sqIndex];
			_whiteRookAttack |= _attackPath[sqIndex];
			_whiteAttackTwice |= _whiteAttack & _attackPath[sqIndex];
			_whiteAttack |= _attackPath[sqIndex];
			_whiteMajorPieceMaterialValue += EvalWeights::RookValueMg;

		}
		else if ((sqMask & _positionWhiteKing) > 0)
		{
			whiteKingIndex = sqIndex;  // Doing the kings at the end
		}
		else if ((sqMask & _positionWhiteKnight) > 0)
		{
			_attackPath[sqIndex] = helper::KnightMove[sqIndex];
			_whiteAttackTwice |= _whiteAttack & _attackPath[sqIndex];
			_whiteAttack |= _attackPath[sqIndex];
			_whiteKnightAttack |= _attackPath[sqIndex];
			_whiteMajorPieceMaterialValue += EvalWeights::KnightValueMg;
		}
		else if ((sqMask & _positionBlackPawn) > 0)
		{
			_nonAttackPawnPath[sqIndex] = PiecePattern::PawnMove<BLACKPIECE>(sqIndex, blockers);
			_potentialAttackPawnPath[sqIndex] = PiecePattern::PawnPotentialAttack<BLACKPIECE>(sqIndex);
			_attackPath[sqIndex] = PiecePattern::PawnAttack<BLACKPIECE>(sqIndex, blockers);
			_attackPath[sqIndex] |= PiecePattern::PawnEnpassant<BLACKPIECE>(sqIndex, StateEnpassantIndex);
			_blackAttackTwice |= _blackAttack & _attackPath[sqIndex];
			_blackAttack |= _attackPath[sqIndex];
			_blackPawnAttack |= _attackPath[sqIndex];
			_blackPotentialAttackPawnTwice |= _blackPotentialAttackPawn & _potentialAttackPawnPath[sqIndex];
			_blackPotentialAttackPawn |= _potentialAttackPawnPath[sqIndex];
			if ((_nonAttackPawnPath[sqIndex] | _attackPath[sqIndex]) == 0ULL) _blackBlockedPawn |= sqMask;
		}
		else if ((sqMask & _positionBlackBishop) > 0)
		{
			_attackPath[sqIndex] = helper::DiagonalMove[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52];
			_attackPathXRay[sqIndex] = helper::DiagonalMoveXRay[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52];
			_blackBishopXRay |= _attackPathXRay[sqIndex];
			_blackBishopAttack |= _attackPath[sqIndex];
			_blackAttackTwice |= _blackAttack & _attackPath[sqIndex];
			_blackAttack |= _attackPath[sqIndex];
			_blackMajorPieceMaterialValue += EvalWeights::BishopValueMg;

		}
		else if (((sqMask & _positionBlackQueen)) > 0)
		{
			// Queen pattern is the bishop pattern xor with rook pattern                   
			_attackPath[sqIndex] = (helper::DiagonalMove[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52] ^
				helper::HorizontalVerticalMove[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52]);
			_attackPathXRay[sqIndex] = (helper::DiagonalMoveXRay[sqIndex][((helper::DiagonalRay[sqIndex] & blockers) * helper::DiagonalMagic[sqIndex]) >> 52] ^
				helper::HorizontalVerticalMoveXRay[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52]);
			_blackQueenXRay |= _attackPathXRay[sqIndex];
			_blackQueenAttack |= _attackPath[sqIndex];
			_blackAttackTwice |= _blackAttack & _attackPath[sqIndex];
			_blackAttack |= _attackPath[sqIndex];
			_blackMajorPieceMaterialValue += EvalWeights::QueenValueMg;

		}
		else if ((sqMask & _positionBlackRook) > 0)
		{
			_attackPath[sqIndex] = helper::HorizontalVerticalMove[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52];
			_attackPathXRay[sqIndex] = helper::HorizontalVerticalMoveXRay[sqIndex][((helper::HorizontalVerticalRay[sqIndex] & blockers) * helper::HorizontalVerticalMagic[sqIndex]) >> 52];
			_blackRookXRay |= _attackPathXRay[sqIndex];
			_blackRookAttack |= _attackPath[sqIndex];
			_blackAttackTwice |= _blackAttack & _attackPath[sqIndex];
			_blackAttack |= _attackPath[sqIndex];
			_blackMajorPieceMaterialValue += EvalWeights::RookValueMg;

		}
		else if ((sqMask & _positionBlackKing) > 0)
		{
			blackKingIndex = sqIndex;
		}
		else if ((sqMask & _positionBlackKnight) > 0)
		{
			_attackPath[sqIndex] = helper::KnightMove[sqIndex];
			_blackAttackTwice |= _blackAttack & _attackPath[sqIndex];
			_blackAttack |= _attackPath[sqIndex];
			_blackKnightAttack |= _attackPath[sqIndex];
			_blackMajorPieceMaterialValue += EvalWeights::QueenValueMg;
		}

	}

	// Calculate the king attack paths
	if (whiteKingIndex > -1 && blackKingIndex > -1) {
		_attackPath[whiteKingIndex] = helper::KingMove[whiteKingIndex] & ~(_blackAttack | helper::KingMove[blackKingIndex]);
		_attackPath[blackKingIndex] = helper::KingMove[blackKingIndex] & ~(_whiteAttack | helper::KingMove[whiteKingIndex]);
		_blackAttackTwice |= _blackAttack & _attackPath[blackKingIndex];
		_whiteAttackTwice |= _whiteAttack & _attackPath[whiteKingIndex];
		_whiteAttack |= _attackPath[whiteKingIndex];
		_blackAttack |= _attackPath[blackKingIndex];
		_whiteKingAttack |= _attackPath[whiteKingIndex];
		_blackKingAttack |= _attackPath[blackKingIndex];

		// Add in castling moves
		_castlePath[whiteKingIndex] |= PiecePattern::KingCastle<WHITEPIECE>(whiteKingIndex, _whitePos, _blackPos, _positionWhiteRook, _blackAttack, _blackPotentialAttackPawn, StateCastlingAvailability);
		_castlePath[blackKingIndex] |= PiecePattern::KingCastle<BLACKPIECE>(blackKingIndex, _whitePos, _blackPos, _positionBlackRook, _whiteAttack, _whitePotentialAttackPawn, StateCastlingAvailability);
	}


	// Do material Hash Key	
	_hashMaterial = _positionWhitePawn ? RandomBoardArray[WHITE_PAWN_INDEX_64 + (popcount(_positionWhitePawn) - 1)] : 0ULL;
	_hashMaterial ^= _positionWhiteKnight ? RandomBoardArray[WHITE_KNIGHT_INDEX_64 + (popcount(_positionWhiteKnight) - 1)] : 0ULL;
	_hashMaterial ^= _positionWhiteBishop ? RandomBoardArray[WHITE_BISHOP_INDEX_64 + (popcount(_positionWhiteBishop) - 1)] : 0ULL;
	_hashMaterial ^= _positionWhiteRook ? RandomBoardArray[WHITE_ROOK_INDEX_64 + (popcount(_positionWhiteRook) - 1)] : 0ULL;
	_hashMaterial ^= _positionWhiteQueen ? RandomBoardArray[WHITE_QUEEN_INDEX_64 + (popcount(_positionWhiteQueen) - 1)] : 0ULL;
	_hashMaterial ^= _positionWhiteKing ? RandomBoardArray[WHITE_KING_INDEX_64 + (popcount(_positionWhiteKing) - 1)] : 0ULL;
	_hashMaterial ^= _positionBlackPawn ? RandomBoardArray[BLACK_PAWN_INDEX_64 + (popcount(_positionBlackPawn) - 1)] : 0ULL;
	_hashMaterial ^= _positionBlackKnight ? RandomBoardArray[BLACK_KNIGHT_INDEX_64 + (popcount(_positionBlackKnight) - 1)] : 0ULL;
	_hashMaterial ^= _positionBlackBishop ? RandomBoardArray[BLACK_BISHOP_INDEX_64 + (popcount(_positionBlackBishop) - 1)] : 0ULL;
	_hashMaterial ^= _positionBlackRook ? RandomBoardArray[BLACK_ROOK_INDEX_64 + (popcount(_positionBlackRook) - 1)] : 0ULL;
	_hashMaterial ^= _positionBlackQueen ? RandomBoardArray[BLACK_QUEEN_INDEX_64 + (popcount(_positionBlackQueen) - 1)] : 0ULL;
	_hashMaterial ^= _positionBlackKing ? RandomBoardArray[BLACK_KING_INDEX_64 + (popcount(_positionBlackKing) - 1)] : 0ULL;

	_attackPathReady = true;
}

/// <summary>
/// Get all attack paths of a given colour
/// </summary>
template<int Colour> uint64_t BitBoard::GetAllAttackPaths() {
    if (!_attackPathReady) CalculateAttackPaths();
	if constexpr (Colour == WHITEPIECE) {
		return (_whiteAttack | _whitePotentialAttackPawn) & (~_whitePos);
	}
	else {

		return (_blackAttack | _blackPotentialAttackPawn) & (~_blackPos);
	}
}
/// <summary>
/// Get all attack paths of a given colour
/// </summary>
template<int Colour, int Spin> uint64_t BitBoard::GetPieceAttackPaths() {
	static_assert(Colour == WHITEPIECE || Colour == BLACKPIECE, "Invalid piece colour");
	static_assert(Spin == PAWN_SPIN || Spin == KNIGHT_SPIN || Spin == BISHOP_SPIN || Spin == ROOK_SPIN || Spin == QUEEN_SPIN || Spin == KING_SPIN, "Invalid piece type");

	if (!_attackPathReady) CalculateAttackPaths();
	
	if constexpr (Spin == PAWN_SPIN && Colour == WHITEPIECE) {		
		return (_whitePawnAttack | _whitePotentialAttackPawn) & (~_whitePos);
	}
	else if constexpr (Spin == PAWN_SPIN && Colour == BLACKPIECE) {
		return (_blackPawnAttack | _blackPotentialAttackPawn) & (~_blackPos);
	}
	else if constexpr (Spin == KNIGHT_SPIN && Colour == WHITEPIECE) {
		return (_whiteKnightAttack) & (~_whitePos);
	}
	else if constexpr (Spin == KNIGHT_SPIN && Colour == BLACKPIECE) {
		return (_blackKnightAttack) & (~_blackPos);
	}
	else if constexpr (Spin == BISHOP_SPIN && Colour == WHITEPIECE) {
		return (_whiteBishopAttack) & (~_whitePos);
	}
	else if constexpr (Spin == BISHOP_SPIN && Colour == BLACKPIECE) {
		return (_blackBishopAttack) & (~_blackPos);
	}
	else if constexpr (Spin == ROOK_SPIN && Colour == WHITEPIECE) {
		return (_whiteRookAttack) & (~_whitePos);
	}
	else if constexpr (Spin == ROOK_SPIN && Colour == BLACKPIECE) {
		return (_blackRookAttack) & (~_blackPos);
	}
	else if constexpr (Spin == QUEEN_SPIN && Colour == WHITEPIECE) {
		return (_whiteQueenAttack) & (~_whitePos);
	}
	else if constexpr (Spin == QUEEN_SPIN && Colour == BLACKPIECE) {
		return (_blackQueenAttack) & (~_blackPos);
	}
	else if constexpr (Spin == KING_SPIN && Colour == WHITEPIECE) {
		return (_whiteKingAttack) & (~_whitePos);
	}
	else if constexpr (Spin == KING_SPIN && Colour == BLACKPIECE) {
		return (_blackKingAttack) & (~_blackPos);
	}
	else {
		return 0ULL;
	}
}

/// <summary>
/// Get all attacks on squares by at least two pieces of the given colour
/// </summary>
template<int Colour> uint64_t BitBoard::GetAllAttackTwicePaths() {
	if (!_attackPathReady) CalculateAttackPaths();

	if constexpr (Colour == WHITEPIECE) {
		return (_whiteAttackTwice | (_whiteAttack & _whitePotentialAttackPawn)) & (~_whitePos);
	}
	else {
		return (_blackAttackTwice | (_blackAttack & _blackPotentialAttackPawn)) & (~_blackPos);
	}
}


/// <summary>
/// Gets all squares the pawns are protecting of a given colour
/// </summary>
/// <returns></returns>
template<int Colour> uint64_t BitBoard::GetProtectPawnPaths()
{
	if (!_attackPathReady) CalculateAttackPaths();
	if constexpr (Colour == WHITEPIECE) {
		return _whitePotentialAttackPawn;
	}
	else {
		return _blackPotentialAttackPawn;
	}
}



/// <summary>
/// Returns all pieces that block slider pieces
/// </summary>
/// <returns></returns>
uint64_t BitBoard::GetSliderBlockers()
{
	if (!_attackPathReady) CalculateAttackPaths();
	return (_whiteBishopAttack | _whiteRookAttack | _whiteQueenAttack | _blackBishopAttack | _blackRookAttack | _blackQueenAttack) & (GetOccupied<WHITEPIECE>() | GetOccupied<BLACKPIECE>());
}

/// <summary>
/// Gets the attacks on the square from sliders of the given colour
/// </summary>
template<int Colour> uint64_t BitBoard::GetSliderAttackers(int pSqIndex) {
    if (!_attackPathReady) CalculateAttackPaths();
	if constexpr (Colour == WHITEPIECE) {
		uint64_t blockers = GetOccupied<WHITEPIECE>() | GetOccupied<BLACKPIECE>();
		uint64_t attackers = PiecePattern::Pattern(helper::PatternEnum::Bishop, pSqIndex, blockers) & (_positionWhiteBishop | _positionWhiteQueen);
		attackers |= PiecePattern::Pattern(helper::PatternEnum::Rook, pSqIndex, blockers) & (_positionWhiteRook | _positionWhiteQueen);
		return attackers;
	}
	else {
		uint64_t blockers = GetOccupied<WHITEPIECE>() | GetOccupied<BLACKPIECE>();
		uint64_t attackers = PiecePattern::Pattern(helper::PatternEnum::Bishop, pSqIndex, blockers) & (_positionBlackBishop | _positionBlackQueen);
		attackers |= PiecePattern::Pattern(helper::PatternEnum::Rook, pSqIndex, blockers) & (_positionBlackRook | _positionBlackQueen);
		return attackers;
	}
}

/// <summary>
/// Gets piece positions that are blocking slider attacks on a given square
/// </summary>
template<int Colour> uint64_t BitBoard::GetSliderBlockers(int pSqIndex) {
	if (!_attackPathReady) CalculateAttackPaths();
	if constexpr (Colour == WHITEPIECE) {
		uint64_t blockers = GetOccupied<WHITEPIECE>() | GetOccupied<BLACKPIECE>();
		uint64_t xRayBishopPattern = PiecePattern::PatternXRay(helper::PatternEnum::Bishop, pSqIndex, blockers);
		uint64_t xRayAttackers = xRayBishopPattern & (_positionBlackBishop | _positionBlackQueen);
		uint64_t xRayRookPattern = PiecePattern::PatternXRay(helper::PatternEnum::Rook, pSqIndex, blockers);
		xRayAttackers |= xRayRookPattern & (_positionBlackRook | _positionBlackQueen);

		int sqIndex = 0;
		int posScan = 0;
		uint64_t sliderblockers = 0ULL;
		while (xRayAttackers > 0)
		{
			posScan = helper::BitScanForward(xRayAttackers);
			xRayAttackers ^= 1uLL << posScan;
			sqIndex = (63 - posScan);

			sliderblockers |= GetOccupied<WHITEPIECE>() & GetAttackPathOfSquare(sqIndex) & (xRayBishopPattern | xRayRookPattern);
		}


		return sliderblockers;
	}
	else {
		uint64_t blockers = GetOccupied<WHITEPIECE>() | GetOccupied<BLACKPIECE>();
		uint64_t xRayBishopPattern = PiecePattern::PatternXRay(helper::PatternEnum::Bishop, pSqIndex, blockers);
		uint64_t xRayAttackers = xRayBishopPattern & (_positionWhiteBishop | _positionWhiteQueen);
		uint64_t xRayRookPattern = PiecePattern::PatternXRay(helper::PatternEnum::Rook, pSqIndex, blockers);
		xRayAttackers |= xRayRookPattern & (_positionWhiteRook | _positionWhiteQueen);

		int sqIndex = 0;
		int posScan = 0;
		uint64_t sliderblockers = 0ULL;
		while (xRayAttackers > 0)
		{
			posScan = helper::BitScanForward(xRayAttackers);
			xRayAttackers ^= 1uLL << posScan;
			sqIndex = (63 - posScan);

			sliderblockers |= GetOccupied<BLACKPIECE>() & GetAttackPathOfSquare(sqIndex) & (xRayBishopPattern | xRayRookPattern);
		}


		return sliderblockers;
	}
}




/// <summary>
/// Gets the XRay attack path
/// </summary>	
/// <returns></returns>
uint64_t BitBoard::GetXRayAttackPathAll() {
	if (!_attackPathReady) CalculateAttackPaths();
	return _whiteBishopXRay | _whiteRookXRay | _whiteQueenXRay | _blackBishopXRay | _blackRookXRay | _blackQueenXRay;
}



/// <summary>
/// Gets the XRay attack path of a given spin value
/// </summary>	
/// <returns></returns>
uint64_t BitBoard::GetXRayAttackPathBySpin(int pSpin) {
	if (!_attackPathReady) CalculateAttackPaths();
	return *_xRayAttacksBySpin[pSpin + 6];
}

/// <summary>
/// Gets the XRay attack path of a given square
/// </summary>	
/// <returns></returns>
uint64_t BitBoard::GetXRayAttackPathOfSquare(int pSqIndex) {
	if (!_attackPathReady) CalculateAttackPaths();
	return _attackPathXRay[pSqIndex];
}

/// <summary>
/// Gets the attack path of a given square
/// </summary>	
/// <returns></returns>
uint64_t BitBoard::GetAttackPathOfSquare(int pSqIndex) {
	if (!_attackPathReady) CalculateAttackPaths();
	return _attackPath[pSqIndex];
}

/// <summary>
/// Gets blocked pawns
/// </summary>	
template<int Colour> uint64_t BitBoard::GetBlockedPawns() {
	if (!_attackPathReady) CalculateAttackPaths();
	
	if constexpr (Colour == WHITEPIECE) {
		return _whiteBlockedPawn;
	}
	else {
		return _blackBlockedPawn;
	}
}



/// <summary>
/// Get all squares the pawns can attack twice
/// </summary>
template<int Colour> uint64_t BitBoard::GetPotentialAttackPawnTwice() {
	if (!_attackPathReady) CalculateAttackPaths();

	if constexpr (Colour == WHITEPIECE) {
		return _whitePotentialAttackPawnTwice;
	}
	else {
		return _blackPotentialAttackPawnTwice;
	}
}




/// <summary>
/// Gets all potential moves for a given square
/// </summary>
/// <param name="pSqIndex"></param>
/// <returns></returns>
uint64_t BitBoard::GetPotentialMove(const int pSqIndex)
{
	if (!_attackPathReady) CalculateAttackPaths();

	if ((_whitePos & helper::BITMASK >> pSqIndex) > 0ULL) {
		return (_attackPath[pSqIndex] | _nonAttackPawnPath[pSqIndex] | _castlePath[pSqIndex]) & ~_whitePos;
	}
	else if ((_blackPos & helper::BITMASK >> pSqIndex) > 0ULL) {
		return (_attackPath[pSqIndex] | _nonAttackPawnPath[pSqIndex] | _castlePath[pSqIndex]) & ~_blackPos;
	}
	else {
		return 0ULL;
	}
}


/// <summary>
/// Force attack path to be recalculated
/// </summary>
void BitBoard::InvalidateAttackPath()
{
	_attackPathReady = false;
}


/// <summary>
/// Updates neuron spin information
/// </summary>
/// <param name="pNeuronId"></param>
/// <param name="pSpin"></param>
void BitBoard::Update(const int pSqIndex, const int pSpin)
{

	// Remove square from hash by XOR
	Hash = Hash ^ ZobristSquareHash(pSqIndex);
	
	// Update pawn hash key if update involves a pawn
	if (((_positionWhitePawn | _positionBlackPawn) & (helper::BITMASK >> pSqIndex)) > 0ULL) HashPawn = HashPawn ^ ZobristSquareHash(pSqIndex);

	// Set board
	SetSpin(pSqIndex, pSpin);

	// Add new move to hash
	Hash = Hash ^ ZobristSquareHash(pSqIndex);

	// Update pawn hash key if update involves a pawn
	if (pSpin == helper::WHITE_PAWN_SPIN || pSpin == helper::BLACK_PAWN_SPIN) HashPawn = HashPawn ^ ZobristSquareHash(pSqIndex);

}

/// <summary>
/// Get board positions array and hash. pData size is 366.
/// </summary>
/// <returns></returns>
void BitBoard::GetBoardArray(uint64_t pData[])
{

	pData[BLACK_PAWN_INDEX] = _positionBlackPawn;
	pData[BLACK_ROOK_INDEX] = _positionBlackRook;
	pData[BLACK_KNIGHT_INDEX] = _positionBlackKnight;
	pData[BLACK_BISHOP_INDEX] = _positionBlackBishop;
	pData[BLACK_QUEEN_INDEX] = _positionBlackQueen;
	pData[BLACK_KING_INDEX] = _positionBlackKing;

	pData[WHITE_PAWN_INDEX] = _positionWhitePawn;
	pData[WHITE_ROOK_INDEX] = _positionWhiteRook;
	pData[WHITE_KNIGHT_INDEX] = _positionWhiteKnight;
	pData[WHITE_BISHOP_INDEX] = _positionWhiteBishop;
	pData[WHITE_QUEEN_INDEX] = _positionWhiteQueen;
	pData[WHITE_KING_INDEX] = _positionWhiteKing;

	pData[12] = Hash;
	pData[13] = HashPawn;
	pData[14] = _hashMaterial;

	//  Source Start,  source end,  destination + offset
	std::copy(_attackPath, _attackPath + 64, pData + 15);

	if (_attackPathReady) pData[79] = 1ULL;
	else pData[79] = 0ULL;

	//  Source Start,  source end,  destination + offset           
	std::copy(_nonAttackPawnPath, _nonAttackPawnPath + 64, pData + 80);
	std::copy(_castlePath, _castlePath + 64, pData + 144);
	std::copy(_potentialAttackPawnPath, _potentialAttackPawnPath + 64, pData + 208);

	pData[272] = _whiteAttack;
	pData[273] = _blackAttack;

	pData[274] = _whitePawnAttack;
	pData[275] = _blackPawnAttack;
	pData[276] = _whitePotentialAttackPawn;
	pData[277] = _blackPotentialAttackPawn;
	pData[278] = _whiteKnightAttack;
	pData[279] = _blackKnightAttack;
	pData[280] = _whiteBishopAttack;
	pData[281] = _blackBishopAttack;
	pData[282] = _whiteRookAttack;
	pData[283] = _blackRookAttack;
	pData[284] = _whiteQueenAttack;
	pData[285] = _blackQueenAttack;
	pData[286] = _whiteKingAttack;
	pData[287] = _blackKingAttack;

	pData[288] = _whiteBlockedPawn;
	pData[289] = _blackBlockedPawn;
	pData[290] = _whiteAttackTwice;
	pData[291] = _blackAttackTwice;
	pData[292] = _whitePotentialAttackPawnTwice;
	pData[293] = _blackPotentialAttackPawnTwice;

	// XRay data
	pData[294] = _whiteBishopXRay;
	pData[295] = _whiteRookXRay;
	pData[296] = _whiteQueenXRay;
	pData[297] = _blackBishopXRay;
	pData[298] = _blackRookXRay;
	pData[299] = _blackQueenXRay;

	//  Source Start,  source end,  destination + offset
	std::copy(_attackPathXRay, _attackPathXRay + 64, pData + 300);

	// Major piece value data
	pData[364] = _whiteMajorPieceMaterialValue;
	pData[365] = _blackMajorPieceMaterialValue;
	
}

/// <summary>
/// Gets FEN string for the board
/// </summary>
/// <param name="pUniverseIndex"></param>
/// <returns></returns>
std::string BitBoard::GetBoard()
{
	std::string FENstr = "";
	int blankCount = 0;

	for (int i = 0; i < 64; i++)
	{
		char FENChar = helper::GetFENCharFromSpin(GetSpin(i));

		if (FENChar == '0') blankCount++;
		else
		{
			if (blankCount > 0) FENstr += std::to_string(blankCount);
			blankCount = 0;
			FENstr += FENChar;
		}

		if ((i + 1) % 8 == 0)
		{
			if (blankCount > 0) FENstr += std::to_string(blankCount);
			blankCount = 0;

			if (i < 63) FENstr += "/";
		}
	}

	return FENstr;

}

/// <summary>
/// Gets the full FEN string for a board. Used to send to UCI engine.
/// </summary>
/// <returns></returns>
std::string BitBoard::GetFullFEN()
{
	std::string boardFENStr = GetBoard();
	std::string activeStr = StateActiveColour == 1 ? "w" : "b";

	// Check that the rook and king are in valid positions for castling
	bool wKS = KingIndex<WHITEPIECE>() == 60 && GetSpin(63) == WHITE_ROOK_SPIN;
	bool wQS = KingIndex<WHITEPIECE>() == 60 && GetSpin(56) == WHITE_ROOK_SPIN;
	bool bKS = KingIndex<BLACKPIECE>() == 4 && GetSpin(7) == BLACK_ROOK_SPIN;
	bool bQS = KingIndex<BLACKPIECE>() == 4 && GetSpin(0) == BLACK_ROOK_SPIN;

	// Castling availability
	std::string whiteCastleKingSide = wKS && ((StateCastlingAvailability & 0b000010) > 0) ? "K" : "";
	std::string whiteCastleQueenSide = wQS && ((StateCastlingAvailability & 0b000001) > 0) ? "Q" : "";
	std::string blackCastleKingSide = bKS && ((StateCastlingAvailability & 0b001000) > 0) ? "k" : "";
	std::string blackCastleQueenSide = bQS && ((StateCastlingAvailability & 0b000100) > 0) ? "q" : "";


	std::string castleAvailability = whiteCastleKingSide + whiteCastleQueenSide + blackCastleKingSide + blackCastleQueenSide;
	castleAvailability = castleAvailability == "" ? "-" : castleAvailability;

	std::string enpassantFENStr = GetEnpassantFEN();

	std::string fullFENstr = boardFENStr + " " + activeStr + " " + castleAvailability + " " + enpassantFENStr + " " + std::to_string(StateHalfMoveCount) + " " + std::to_string(StateFullMoveCount);
	return fullFENstr;
}

/// <summary>
/// This gets the position of the enpassant behind the pawn. This is used in FEN strings.
/// </summary>
/// <returns></returns>
std::string BitBoard::GetEnpassantFEN()
{

	if (StateEnpassantIndex >= 0 && StateEnpassantIndex <= 63)
	{
		int enpassantTarget = -1;
		int spin = GetSpin(StateEnpassantIndex);
		if (spin == helper::WHITE_PAWN_SPIN) enpassantTarget = StateEnpassantIndex + 8;
		else if (spin == helper::BLACK_PAWN_SPIN) enpassantTarget = StateEnpassantIndex - 8;

		if (enpassantTarget >= 0 && enpassantTarget <= 63)
		{
			std::string fenStr = helper::BoardCoordinateDict.at(enpassantTarget);
			return fenStr;
		}
	}

	return "-";

}


/// <summary>
/// Set board and hash using array. Size of pData is 366.
/// </summary>
/// <returns></returns>
void BitBoard::SetBoardArray(const uint64_t pData[])
{

	_positionBlackPawn = pData[BLACK_PAWN_INDEX];
	_positionBlackRook = pData[BLACK_ROOK_INDEX];
	_positionBlackKnight = pData[BLACK_KNIGHT_INDEX];
	_positionBlackBishop = pData[BLACK_BISHOP_INDEX];
	_positionBlackQueen = pData[BLACK_QUEEN_INDEX];
	_positionBlackKing = pData[BLACK_KING_INDEX];

	_positionWhitePawn = pData[WHITE_PAWN_INDEX];
	_positionWhiteRook = pData[WHITE_ROOK_INDEX];
	_positionWhiteKnight = pData[WHITE_KNIGHT_INDEX];
	_positionWhiteBishop = pData[WHITE_BISHOP_INDEX];
	_positionWhiteQueen = pData[WHITE_QUEEN_INDEX];
	_positionWhiteKing = pData[WHITE_KING_INDEX];

	Hash = pData[12];
	HashPawn = pData[13];
	_hashMaterial = pData[14];

	// Source start + offset, source end + offset + size, destination             
	std::copy(pData + 15, pData + 15 + 64, _attackPath);

	_attackPathReady = pData[79] == 1uLL;

	// Source start + offset, source end + offset + size, destination             
	std::copy(pData + 80, pData + 80 + 64, _nonAttackPawnPath);
	std::copy(pData + 144, pData + 144 + 64, _castlePath);
	std::copy(pData + 208, pData + 208 + 64, _potentialAttackPawnPath);

	_whiteAttack = pData[272];
	_blackAttack = pData[273];	
	
	_whitePawnAttack = pData[274];
	_blackPawnAttack = pData[275];
	_whitePotentialAttackPawn = pData[276];
	_blackPotentialAttackPawn = pData[277];
	_whiteKnightAttack = pData[278];
	_blackKnightAttack = pData[279];
	_whiteBishopAttack = pData[280];
	_blackBishopAttack = pData[281];
	_whiteRookAttack = pData[282];
	_blackRookAttack = pData[283];
	_whiteQueenAttack = pData[284];
	_blackQueenAttack = pData[285];
	_whiteKingAttack = pData[286];
	_blackKingAttack = pData[287];
	
	_whiteBlockedPawn = pData[288];
	_blackBlockedPawn = pData[289];
	_whiteAttackTwice = pData[290];
	_blackAttackTwice = pData[291];
	_whitePotentialAttackPawnTwice = pData[292];
	_blackPotentialAttackPawnTwice = pData[293];

	// XRay data
	_whiteBishopXRay = pData[294];
	_whiteRookXRay = pData[295];
	_whiteQueenXRay = pData[296];
	_blackBishopXRay = pData[297];
	_blackRookXRay = pData[298];
	_blackQueenXRay = pData[299];

	// Source start + offset, source end + offset + size, destination             
	std::copy(pData + 300, pData + 300 + 64, _attackPathXRay);

	// Major piece value data
	_whiteMajorPieceMaterialValue = pData[364];
	_blackMajorPieceMaterialValue = pData[365];
}

/// <summary>
/// Sets a board using a FEN string
/// </summary>
/// <param name="pBoard"></param>
/// <param name="pUniverseIndex"></param>
void BitBoard::SetBoard(const std::string pFENstr)
{
	char boardArray[64] = {};

	int sqIndex = 0;
	for (char c : pFENstr)
	{
		char pieceChar = char(std::tolower(c));
		if (pieceChar == 'p' || pieceChar == 'r' || pieceChar == 'n' || pieceChar == 'b' || pieceChar == 'q' || pieceChar == 'k')
		{
			boardArray[sqIndex] = c;
			sqIndex++;
			if (sqIndex > 63) break;
		}
		else if (c == '/')
		{
			// Just skip
		}
		else if (c >= '1' && c <= '8')
		{
			int blankCount = c - '0';
			for (int i = 1; i <= blankCount; i++)
			{
				sqIndex++;
				if (sqIndex > 63) break;
			}

			if (sqIndex > 63) break;
		}
	}

	// Update the bitboard
	for (int i = 0; i < 64; i++)
	{
		int currentSpin = GetSpin(i);
		int newSpin = helper::GetSpinFromChar(boardArray[i]);
		if (newSpin != currentSpin)
		{
			Update(i, newSpin);
		}
	}


	// Invalidate move paths
	_attackPathReady = false;

}




/// <summary>
/// Set spin on board
/// </summary>
/// <param name="pIndex"></param>
/// <param name="pSpin"></param>
void BitBoard::SetSpin(const int pSqIndex, const int pSpin)
{
	uint64_t sqMask = helper::BITMASK >> pSqIndex;
	uint64_t sqMaskComp = ~sqMask;

	_positionBlackPawn &= sqMaskComp;
	_positionBlackRook &= sqMaskComp;
	_positionBlackKnight &= sqMaskComp;
	_positionBlackBishop &= sqMaskComp;
	_positionBlackQueen &= sqMaskComp;
	_positionBlackKing &= sqMaskComp;
	
	_positionWhitePawn &= sqMaskComp;
	_positionWhiteRook &= sqMaskComp;
	_positionWhiteKnight &= sqMaskComp;
	_positionWhiteBishop &= sqMaskComp;
	_positionWhiteQueen &= sqMaskComp;
	_positionWhiteKing &= sqMaskComp;


	// Set spin  
	*_positions[pSpin + 6] |= sqMask;

	// Invalidate move paths
	_attackPathReady = false;

}

/// <summary>
/// Gets all positions of a specified spin
/// </summary>
/// <param name="pIndex"></param>
uint64_t BitBoard::GetPositionsOfSpin(const int pSpin) {
	return *_positions[pSpin + 6];
}


/// <summary>
/// Gets a square of the specified Index
/// </summary>
/// <param name="pIndex"></param>
int BitBoard::GetSpin(const int pIndex)
{
	uint64_t sqMask = helper::BITMASK >> pIndex;


	if ((sqMask & _positionBlackPawn) > 0) return helper::BLACK_PAWN_SPIN;
	else if ((sqMask & _positionBlackRook) > 0) return helper::BLACK_ROOK_SPIN;
	else if ((sqMask & _positionBlackKnight) > 0) return helper::BLACK_KNIGHT_SPIN;
	else if ((sqMask & _positionBlackBishop) > 0) return helper::BLACK_BISHOP_SPIN;
	else if ((sqMask & _positionBlackQueen) > 0) return helper::BLACK_QUEEN_SPIN;
	else if ((sqMask & _positionBlackKing) > 0) return helper::BLACK_KING_SPIN;
	else if ((sqMask & _positionWhitePawn) > 0) return helper::WHITE_PAWN_SPIN;
	else if ((sqMask & _positionWhiteRook) > 0) return helper::WHITE_ROOK_SPIN;
	else if ((sqMask & _positionWhiteKnight) > 0) return helper::WHITE_KNIGHT_SPIN;
	else if ((sqMask & _positionWhiteBishop) > 0) return helper::WHITE_BISHOP_SPIN;
	else if ((sqMask & _positionWhiteQueen) > 0) return helper::WHITE_QUEEN_SPIN;
	else if ((sqMask & _positionWhiteKing) > 0) return helper::WHITE_KING_SPIN;


	return 0;

}


/// <summary>
/// Returns a spin only if it matches the colour
/// </summary>
/// <param name="pIndex"></param>
/// <param name="pColour"></param>
/// <returns></returns>
int BitBoard::GetSpin(const int pIndex, const int pColour)
{
	int spin = GetSpin(pIndex);
	if (spin > 0 && pColour == 1) return spin;
	else if (spin < 0 && pColour == -1) return spin;
	else return 0;
}


/// <summary>
/// Gets the hash of an individual square
/// </summary>
/// <param name="pSqIndex"></param>
/// <returns></returns>
uint64_t BitBoard::ZobristSquareHash(const int pSqIndex)
{

	uint64_t sqMask = helper::BITMASK >> pSqIndex;

	if ((sqMask & _positionBlackPawn) > 0) return helper::RandomBoardArray[BLACK_PAWN_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionBlackRook) > 0) return helper::RandomBoardArray[BLACK_ROOK_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionBlackKnight) > 0) return helper::RandomBoardArray[BLACK_KNIGHT_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionBlackBishop) > 0) return helper::RandomBoardArray[BLACK_BISHOP_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionBlackQueen) > 0) return helper::RandomBoardArray[BLACK_QUEEN_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionBlackKing) > 0) return helper::RandomBoardArray[BLACK_KING_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionWhitePawn) > 0) return helper::RandomBoardArray[WHITE_PAWN_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionWhiteRook) > 0) return helper::RandomBoardArray[WHITE_ROOK_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionWhiteKnight) > 0) return helper::RandomBoardArray[WHITE_KNIGHT_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionWhiteBishop) > 0) return helper::RandomBoardArray[WHITE_BISHOP_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionWhiteQueen) > 0) return helper::RandomBoardArray[WHITE_QUEEN_INDEX_64 + pSqIndex];
	else if ((sqMask & _positionWhiteKing) > 0) return helper::RandomBoardArray[WHITE_KING_INDEX_64 + pSqIndex];
	else return 0uLL;


}



/// <summary>
/// Includes state in hash
/// </summary>
/// <returns></returns>
uint64_t BitBoard::HashWithState()
{
	uint64_t zHash = Hash;

	// Turn
	zHash = zHash ^ helper::RandomTurnArray[StateActiveColour + 1];

	// Castling
	// zHash = zHash ^ helper.RandomCastlingAvailabilityArray[StateCastlingAvailability];

	// EnPassant
	// if (StateEnpassantIndex >= 0 && StateEnpassantIndex <= 63) zHash = zHash ^ helper.RandomEnpassantArray[StateEnpassantIndex];


	return zHash;
}


/// <summary>
/// Gets bits that represent whether a square contains piece for specified colour
/// </summary>
/// <param name="pColour"></param>
/// <returns></returns>
uint64_t BitBoard::GetOccupiedBySpin(const int pSpin)
{
	return *_positions[pSpin + 6];
}

/// <summary>
/// Gets bits that represent whether a square contains piece for specified colour
/// </summary>
/// <param name="pColour"></param>
/// <returns></returns>
template<int Colour> uint64_t BitBoard::GetOccupiedBySpin(const int pSpin)
{
	static_assert(Colour == WHITEPIECE || Colour == BLACKPIECE, "Invalid piece colour");
	
	if constexpr (Colour == WHITEPIECE) {
		return *_positions[pSpin + 6];
	}
	else {
		return *_positions[-pSpin + 6];
	}
}

/// <summary>
/// Gets occupied bits of the specified colour
/// </summary>
/// <param name="pExcludeKing"></param>
/// <returns></returns>
uint64_t BitBoard::GetOccupied(const int pColour)
{
	if (pColour == helper::WHITEPIECE) return _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen | _positionWhiteKing;
	else return _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen | _positionBlackKing;

}

/// <summary>
/// Gets occupied bits up to the specified spin value
/// </summary>
/// <param name="pExcludeKing"></param>
/// <returns></returns>
uint64_t BitBoard::GetOccupiedUpTo(const int pSpin)
{
	if (pSpin == 6) return _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen | _positionWhiteKing;
	else if (pSpin == 5) return _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen;
	else if (pSpin == 4) return _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook;
	else if (pSpin == 3) return _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop;
	else if (pSpin == 2) return _positionWhitePawn | _positionWhiteKnight;
	else if (pSpin == 1) return _positionWhitePawn;
	else if (pSpin == -6) return _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen | _positionBlackKing;
	else if (pSpin == -5) return _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen;
	else if (pSpin == -4) return _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook;
	else if (pSpin == -3) return _positionBlackPawn | _positionBlackKnight | _positionBlackBishop;
	else if (pSpin == -2) return _positionBlackPawn | _positionBlackKnight;
	else if (pSpin == -1) return _positionBlackPawn;
	else return 0ULL;
}

/// <summary>
/// Gets all bits that represent specified colour pieces
/// </summary>
/// <returns></returns>
template<int Colour> uint64_t BitBoard::GetOccupied() {
	if constexpr (Colour == helper::WHITEPIECE) {
		return _positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen | _positionWhiteKing;
	}
	else {
		return _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen | _positionBlackKing;
	}
}


/// <summary>
/// Determines if only kings left on the board
/// </summary>
bool BitBoard::OnlyKingsRemain()
{
	return (_positionWhitePawn | _positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen | _positionBlackPawn | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen) == 0;

}


/// <summary>
/// Gets major piece count 
/// </summary>
/// <param name="pColour"></param>
/// <returns></returns>
int BitBoard::MajorPieceCount()
{
	// all pieces except for pawns
	return popcount(_positionWhiteKnight | _positionWhiteBishop | _positionWhiteRook | _positionWhiteQueen | _positionWhiteKing | _positionBlackKnight | _positionBlackBishop | _positionBlackRook | _positionBlackQueen | _positionBlackKing);
	
}

/// <summary>
/// Gets the index of the king
/// </summary>
/// template<int Colour> int KingIndex();
template<int Colour> int BitBoard::KingIndex()
{
	if constexpr (Colour == WHITEPIECE) {
		return _positionWhiteKing > 0 ? 63 - helper::BitScanForward(_positionWhiteKing) : -1;
	}
	else {
		return _positionBlackKing > 0 ? 63 - helper::BitScanForward(_positionBlackKing) : -1;
	}

}


/// <summary>
/// Determins whether the specified colour is in check
/// </summary>
bool BitBoard::IsKingCheck(const int pColour)
{
	if (!_attackPathReady) CalculateAttackPaths();

	if (pColour == WHITEPIECE) {
		return (_blackAttack & _positionWhiteKing) > 0;
	}
	else {
		return (_whiteAttack & _positionBlackKing) > 0;
	}

}




/// <summary>
/// Checks that the position array is valid
/// </summary>
/// <returns></returns>
bool BitBoard::IsPositionArrayValid()
{
	uint64_t sqMask;
	int spinCount;

	for (int sqIndex = 0; sqIndex < 64; sqIndex++)
	{
		sqMask = helper::BITMASK >> sqIndex;
		spinCount = 0;

		if ((sqMask & _positionBlackPawn) > 0) spinCount++;
		else if ((sqMask & _positionBlackRook) > 0) spinCount++;
		else if ((sqMask & _positionBlackKnight) > 0) spinCount++;
		else if ((sqMask & _positionBlackBishop) > 0) spinCount++;
		else if ((sqMask & _positionBlackQueen) > 0) spinCount++;
		else if ((sqMask & _positionBlackKing) > 0) spinCount++;
		else if ((sqMask & _positionWhitePawn) > 0) spinCount++;
		else if ((sqMask & _positionWhiteRook) > 0) spinCount++;
		else if ((sqMask & _positionWhiteKnight) > 0) spinCount++;
		else if ((sqMask & _positionWhiteBishop) > 0) spinCount++;
		else if ((sqMask & _positionWhiteQueen) > 0) spinCount++;
		else if ((sqMask & _positionWhiteKing) > 0) spinCount++;

		if (spinCount > 1) return false;
	}

	return true;
}

/// <summary>
/// Gets all the current state public getters and setters.        
/// </summary>
std::string BitBoard::GetState()
{
	std::string state;
	state = std::to_string(StateActiveColour) + "|" +
		std::to_string(StateCastlingAvailability) + "|" +
		std::to_string(StateEnpassantIndex) + "|" +
		std::to_string(StateHalfMoveCount) + "|" +
		std::to_string(StateFullMoveCount) + "|" +
		std::to_string(StateGameStatus) + "|" +
		std::to_string(StateWhiteClockOffset) + "|" +
		std::to_string(StateBlackClockOffset);


	return state;
}


/// <summary>
/// Set current board state from string     
/// </summary>
void BitBoard::SetState(const std::string pState)
{
	std::vector<std::string> stateArray;
	stateArray.reserve(8);
	helper::Split(pState, '|', stateArray);

	if (stateArray.size() == 8)
	{
		StateActiveColour = std::stoi(stateArray[0]);
		StateCastlingAvailability = std::stoi(stateArray[1]);
		StateEnpassantIndex = std::stoi(stateArray[2]);
		StateHalfMoveCount = std::stoi(stateArray[3]);
		StateFullMoveCount = std::stoi(stateArray[4]);
		StateGameStatus = std::stoi(stateArray[5]);
		StateWhiteClockOffset = std::stoi(stateArray[6]);
		StateBlackClockOffset = std::stoi(stateArray[7]);
	}

	// Invalidate move paths
	_attackPathReady = false;
}

/// <summary>
/// Gets all the current board state as an array. pState size is 8.
/// </summary>
void BitBoard::GetStateArray(int pState[])
{
	pState[0] = StateActiveColour;
	pState[1] = StateCastlingAvailability;
	pState[2] = StateEnpassantIndex;
	pState[3] = StateHalfMoveCount;
	pState[4] = StateFullMoveCount;
	pState[5] = StateGameStatus;
	pState[6] = StateWhiteClockOffset;
	pState[7] = StateBlackClockOffset;

}

/// <summary>
/// Set current board state from array        
/// </summary>
void BitBoard::SetStateArray(const int pState[])
{
	// pState size is 8
	StateActiveColour = pState[0];
	StateCastlingAvailability = pState[1];
	StateEnpassantIndex = pState[2];
	StateHalfMoveCount = pState[3];
	StateFullMoveCount = pState[4];
	StateGameStatus = pState[5];
	StateWhiteClockOffset = pState[6];
	StateBlackClockOffset = pState[7];

	// Invalidate move paths
	_attackPathReady = false;

}

/// <summary>
/// Resets the board to the initial state
/// </summary>
void BitBoard::Reset()
{

	// Initialise the state
	StateActiveColour = helper::WHITEPIECE;
	StateCastlingAvailability = 0b001111;
	StateEnpassantIndex = -1;
	StateHalfMoveCount = 0;
	StateFullMoveCount = 0;
	StateGameStatus = 0;
	StateWhiteClockOffset = 0;
	StateBlackClockOffset = 0;

	// Set the default board
	SetBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

	// Invalidate move paths
	_attackPathReady = false;
}


/// <summary>
/// Creates a new bitboard object as a copy of the current object
/// </summary>
/// <returns></returns>
void BitBoard::Copy(BitBoard& pDestBoard)
{

	// Copy positions
	pDestBoard._positionBlackPawn = this->_positionBlackPawn;
	pDestBoard._positionBlackRook = this->_positionBlackRook;
	pDestBoard._positionBlackKnight = this->_positionBlackKnight;
	pDestBoard._positionBlackBishop = this->_positionBlackBishop;
	pDestBoard._positionBlackQueen = this->_positionBlackQueen;
	pDestBoard._positionBlackKing = this->_positionBlackKing;
	pDestBoard._positionWhitePawn = this->_positionWhitePawn;
	pDestBoard._positionWhiteRook = this->_positionWhiteRook;
	pDestBoard._positionWhiteKnight = this->_positionWhiteKnight;
	pDestBoard._positionWhiteBishop = this->_positionWhiteBishop;
	pDestBoard._positionWhiteQueen = this->_positionWhiteQueen;
	pDestBoard._positionWhiteKing = this->_positionWhiteKing;


	// Copy hash
	pDestBoard.Hash = this->Hash;
	pDestBoard.HashPawn = this->HashPawn;
	pDestBoard._hashMaterial = this->_hashMaterial;
	pDestBoard.StateActiveColour = this->StateActiveColour;
	pDestBoard.StateGameStatus = this->StateGameStatus;
	pDestBoard.StateCastlingAvailability = this->StateCastlingAvailability;
	pDestBoard.StateEnpassantIndex = this->StateEnpassantIndex;
	pDestBoard.StateHalfMoveCount = this->StateHalfMoveCount;
	pDestBoard.StateFullMoveCount = this->StateFullMoveCount;
	pDestBoard.StateWhiteClockOffset = this->StateWhiteClockOffset;
	pDestBoard.StateBlackClockOffset = this->StateBlackClockOffset;

	// Copy attack paths
	pDestBoard._attackPathReady = this->_attackPathReady;

	//  Source Start,  source end,  destination 
	std::copy(this->_attackPath, _attackPath + 64, pDestBoard._attackPath);

	std::copy(this->_nonAttackPawnPath, _nonAttackPawnPath + 64, pDestBoard._nonAttackPawnPath);
	std::copy(this->_castlePath, _castlePath + 64, pDestBoard._castlePath);
	std::copy(this->_potentialAttackPawnPath, _potentialAttackPawnPath + 64, pDestBoard._potentialAttackPawnPath);


	pDestBoard._whiteAttack = this->_whiteAttack;
	pDestBoard._blackAttack = this->_blackAttack;
	pDestBoard._whitePawnAttack = this->_whitePawnAttack;
	pDestBoard._blackPawnAttack = this->_blackPawnAttack;
	pDestBoard._whitePotentialAttackPawn = this->_whitePotentialAttackPawn;
	pDestBoard._blackPotentialAttackPawn = this->_blackPotentialAttackPawn;
	pDestBoard._whiteKnightAttack = this->_whiteKnightAttack;
	pDestBoard._blackKnightAttack = this->_blackKnightAttack;
	pDestBoard._whiteBishopAttack = this->_whiteBishopAttack;
	pDestBoard._blackBishopAttack = this->_blackBishopAttack;
	pDestBoard._whiteRookAttack = this->_whiteRookAttack;
	pDestBoard._blackRookAttack = this->_blackRookAttack;
	pDestBoard._whiteQueenAttack = this->_whiteQueenAttack;
	pDestBoard._blackQueenAttack = this->_blackQueenAttack;
	pDestBoard._whiteKingAttack = this->_whiteKingAttack;
	pDestBoard._blackKingAttack = this->_blackKingAttack;
	pDestBoard._whiteBlockedPawn = this->_whiteBlockedPawn;
	pDestBoard._blackBlockedPawn = this->_blackBlockedPawn;
	pDestBoard._whiteAttackTwice = this->_whiteAttackTwice;
	pDestBoard._blackAttackTwice = this->_blackAttackTwice;
	pDestBoard._whitePotentialAttackPawnTwice = this->_whitePotentialAttackPawnTwice;
	pDestBoard._blackPotentialAttackPawnTwice = this->_blackPotentialAttackPawnTwice;


	// Copy XRay values
	pDestBoard._whiteBishopXRay = this->_whiteBishopXRay;
	pDestBoard._whiteRookXRay = this->_whiteRookXRay;
	pDestBoard._whiteQueenXRay = this->_whiteQueenXRay;
	pDestBoard._blackBishopXRay = this->_blackBishopXRay;
	pDestBoard._blackRookXRay = this->_blackRookXRay;
	pDestBoard._blackQueenXRay = this->_blackQueenXRay;

	std::copy(this->_attackPathXRay, _attackPathXRay + 64, pDestBoard._attackPathXRay);

	// Copy major piece value data
	pDestBoard._whiteMajorPieceMaterialValue = this->_whiteMajorPieceMaterialValue;
	pDestBoard._blackMajorPieceMaterialValue = this->_blackMajorPieceMaterialValue;
}


/// <summary>
/// Counts the piece types on the board
/// </summary>
template<int Colour> int BitBoard::Count(int pSpin) {
	static_assert(Colour == WHITEPIECE || Colour == BLACKPIECE, "Invalid piece colour");

	if constexpr (Colour == WHITEPIECE) {
		return popcount(*_positions[pSpin + 6]);
	}
	else {
		return popcount(*_positions[-pSpin + 6]);
	}
}


/// <summary>
/// Counts all pieces of the same colour on the board
/// </summary>
template<int Colour> int BitBoard::Count() {
	static_assert(Colour == WHITEPIECE || Colour == BLACKPIECE, "Invalid piece colour");

	if constexpr (Colour == WHITEPIECE) {
		return popcount(*_positions[WHITE_PAWN_SPIN + 6]) 
				+ popcount(*_positions[WHITE_KNIGHT_SPIN + 6])
				+ popcount(*_positions[WHITE_BISHOP_SPIN + 6])
				+ popcount(*_positions[WHITE_ROOK_SPIN + 6])
				+ popcount(*_positions[WHITE_QUEEN_SPIN + 6])
				+ popcount(*_positions[WHITE_KING_SPIN + 6]);
	}
	else {
		return popcount(*_positions[BLACK_PAWN_SPIN + 6])
			+ popcount(*_positions[BLACK_KNIGHT_SPIN + 6])
			+ popcount(*_positions[BLACK_BISHOP_SPIN + 6])
			+ popcount(*_positions[BLACK_ROOK_SPIN + 6])
			+ popcount(*_positions[BLACK_QUEEN_SPIN + 6])
			+ popcount(*_positions[BLACK_KING_SPIN + 6]);
	}
}

/// <summary>
/// Gets the material hash
/// </summary>
/// <returns></returns>
uint64_t BitBoard::GetMaterialHash() {
	if (!_attackPathReady) CalculateAttackPaths();
	return _hashMaterial;
}


template<int Colour> int BitBoard::GetMajorPieceMaterialValue() {
	if (!_attackPathReady) CalculateAttackPaths();

	if constexpr (Colour == WHITEPIECE) {
		return _whiteMajorPieceMaterialValue;
	}
	else {
		return _blackMajorPieceMaterialValue;
	}
}



// Explicit template instantiation
template uint64_t BitBoard::GetOccupied<WHITEPIECE>();
template uint64_t BitBoard::GetOccupied<BLACKPIECE>();
template uint64_t BitBoard::GetAllAttackPaths<WHITEPIECE>();
template uint64_t BitBoard::GetAllAttackPaths<BLACKPIECE>();
template uint64_t BitBoard::GetPieceAttackPaths<WHITEPIECE, PAWN_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<BLACKPIECE, PAWN_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<WHITEPIECE, KNIGHT_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<BLACKPIECE, KNIGHT_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<WHITEPIECE, BISHOP_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<BLACKPIECE, BISHOP_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<WHITEPIECE, ROOK_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<BLACKPIECE, ROOK_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<WHITEPIECE, QUEEN_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<BLACKPIECE, QUEEN_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<WHITEPIECE, KING_SPIN>();
template uint64_t BitBoard::GetPieceAttackPaths<BLACKPIECE, KING_SPIN>();
template uint64_t BitBoard::GetAllAttackTwicePaths<WHITEPIECE>();
template uint64_t BitBoard::GetAllAttackTwicePaths<BLACKPIECE>();
template uint64_t BitBoard::GetPotentialAttackPawnTwice<WHITEPIECE>();
template uint64_t BitBoard::GetPotentialAttackPawnTwice<BLACKPIECE>();
template uint64_t BitBoard::GetProtectPawnPaths<WHITEPIECE>();
template uint64_t BitBoard::GetProtectPawnPaths<BLACKPIECE>();
template uint64_t BitBoard::GetSliderAttackers<WHITEPIECE>(int pSqIndex);
template uint64_t BitBoard::GetSliderAttackers<BLACKPIECE>(int pSqIndex);
template uint64_t BitBoard::GetSliderBlockers<WHITEPIECE>(int pSqIndex);
template uint64_t BitBoard::GetSliderBlockers<BLACKPIECE>(int pSqIndex);
template uint64_t BitBoard::GetBlockedPawns<WHITEPIECE>();
template uint64_t BitBoard::GetBlockedPawns<BLACKPIECE>();
template int BitBoard::KingIndex<WHITEPIECE>();
template int BitBoard::KingIndex<BLACKPIECE>();
template uint64_t BitBoard::GetOccupiedBySpin<WHITEPIECE>(const int pSpin);
template uint64_t BitBoard::GetOccupiedBySpin<BLACKPIECE>(const int pSpin);
template int BitBoard::Count<WHITEPIECE>(int pSpin);
template int BitBoard::Count<BLACKPIECE>(int pSpin);
template int BitBoard::Count<WHITEPIECE>();
template int BitBoard::Count<BLACKPIECE>();
template int BitBoard::GetMajorPieceMaterialValue<WHITEPIECE>();
template int BitBoard::GetMajorPieceMaterialValue<BLACKPIECE>();







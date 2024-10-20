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
#include <string>
#include <vector>

namespace KaruahChess {

	// Class definition
	class BitBoard {

	private:

		uint64_t _positionWhitePawn;
		uint64_t _positionWhiteRook;
		uint64_t _positionWhiteKnight;
		uint64_t _positionWhiteBishop;
		uint64_t _positionWhiteQueen;
		uint64_t _positionWhiteKing;
		uint64_t _blank64;
		uint64_t _positionBlackPawn;
		uint64_t _positionBlackRook;
		uint64_t _positionBlackKnight;
		uint64_t _positionBlackBishop;
		uint64_t _positionBlackQueen;
		uint64_t _positionBlackKing;

		uint64_t* _positions[13];


		bool _attackPathReady;
		uint64_t _attackPath[64];
		uint64_t _nonAttackPawnPath[64];
		uint64_t _potentialAttackPawnPath[64];
		uint64_t _castlePath[64];

		uint64_t _whiteAttack;
		uint64_t _blackAttack;
		uint64_t _whiteAttackXRayBlackKing;
		uint64_t _blackAttackXRayWhiteKing;
		uint64_t _whitePotentialAttackPawn;
		uint64_t _blackPotentialAttackPawn;
		uint64_t _whitePos;
		uint64_t _blackPos;



		void SetSpin(int pSqIndex, int pSpin);
		void CalculateAttackPaths();
		uint64_t ZobristSquareHash(int pSqIndex);


		uint64_t _hashMaterial;

	public:

		uint64_t Hash;
		uint64_t HashPawn;


		int MoveData[4];
		std::string ReturnMessage;

		int StateActiveColour;
		int StateGameStatus;
		int StateCastlingAvailability;
		int StateEnpassantIndex;
		int StateHalfMoveCount;
		int StateFullMoveCount;
		int StateWhiteClockOffset;
		int StateBlackClockOffset;

		// Constructor
		BitBoard();

		// Functions
		uint64_t GetPotentialMove(const int pSqIndex);
		void InvalidateAttackPath();
		void Update(const int pSqIndex, const int pSpin);
		void GetBoardArray(uint64_t pData[]);
		std::string GetBoard();
		std::string GetFullFEN();
		std::string GetEnpassantFEN();
		void SetBoardArray(const uint64_t pData[]);
		void SetBoard(const std::string pFENstr);
		int GetSpin(const int pIndex);
		uint64_t GetOccupiedBySpin(const int pSpin);
		template<int Colour> uint64_t GetOccupiedBySpin(const int pSpin);
		uint64_t GetOccupied(const int pColour);
		bool OnlyKingsRemain();
		template<int Colour> int KingIndex();
		bool IsKingCheck(const int pColour);
		std::string GetState();
		void SetState(const std::string pState);
		void GetStateArray(int pState[]);
		void SetStateArray(const int pState[]);
		void Reset();
		void Copy(BitBoard& pDestBoard);
		template<int Colour> int Count(int pSpin);
		template<int Colour> int Count();
		int VerifyBoardConfiguration();
		template<int Colour> bool setStateCastlingAvailability(const int pCastlingAvailability);


	};

}


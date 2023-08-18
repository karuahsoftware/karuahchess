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
#include <winrt/Windows.Foundation.h>
#include "bitboard.h"
#include "KaruahChessEngineClass.g.h"


namespace winrt::KaruahChessEngine::implementation
{	    
	using namespace winrt::Windows::Foundation;

	struct KaruahChessEngineClass : KaruahChessEngineClassT<KaruahChessEngineClass>
	{	
		BitBoard MainBoard;
		BitBoard SearchBoard;

		KaruahChessEngineClass();

		// Helper functions
		IAsyncAction LoadNNUE();

		// Bitboard operations
		winrt::hstring GetBoard();
		winrt::hstring GetState();
		void SetBoard(const winrt::hstring pBoardFENString);
		void SetState(const winrt::hstring pBoardStateString);
		
		void GetBoardArray(winrt::array_view<uint64_t> pBoardArray);
		void GetStateArray(winrt::array_view<int32_t> pStateArray);
		void SetBoardArray(const winrt::array_view<const uint64_t> pBoardArray);
		void SetStateArray(const winrt::array_view<const int32_t> pStateArray);

		void SetStateWhiteClockOffset(const int32_t pOffset);
		void SetStateBlackClockOffset(const int32_t pOffset);
		int32_t GetStateWhiteClockOffset();
		int32_t GetStateBlackClockOffset();

		void Reset();
		void CancelSearch();

		int32_t GetSpin(const int32_t pIndex);
		int32_t GetStateActiveColour();
		void SetStateActiveColour(const int32_t pColour);
		int32_t GetStateGameStatus();
		void SetStateGameStatus(const int32_t pStatus);
		int32_t GetStateFullMoveCount();
		int32_t GetStateCastlingAvailability();
		bool SetStateCastlingAvailability(const int32_t pCastlingAvailability, const int32_t pColour);
		int32_t GetKingIndex(const int32_t pColour);
		bool IsKingCheck(const int32_t pColour);
		uint64_t GetPotentialMove(const int32_t pSqIndex);
		uint64_t GetOccupiedBySpin(const int32_t pSpin);
		uint64_t GetOccupiedByColour(const int32_t pColour);

		// Move operations
		MoveResult Move(const int32_t pFromIndex, const int32_t pToIndex, const int32_t pPawnPromotionPiece, const bool pValidateEnabled, const bool pCommit);
		MoveResult Arrange(const int32_t pFromIndex, const int32_t pToIndex);
		MoveResult ArrangeUpdate(const char16_t pFen, const int32_t pToIndex);
		int32_t FindFromIndex(const int32_t pToIndex, int32_t pSpin, const winrt::array_view<const int32_t> pValidFromIndexes);
		int32_t GetSpinFromPieceName(const winrt::hstring pPieceName);
		winrt::hstring GetPieceNameFromChar(const char16_t pFENChar);
		char16_t GetFENCharFromSpin(const int32_t pSpin);
		bool IsPawnPromotion(const int32_t pFromIndex, const int32_t pToIndex);

		winrt::hstring GetFullFEN();
		

		SearchResult SearchStart(const SearchOptions pSearchOptions);

		
		
	};
}

namespace winrt::KaruahChessEngine::factory_implementation
{
    struct KaruahChessEngineClass : KaruahChessEngineClassT<KaruahChessEngineClass, implementation::KaruahChessEngineClass>
    {
    };
		
}

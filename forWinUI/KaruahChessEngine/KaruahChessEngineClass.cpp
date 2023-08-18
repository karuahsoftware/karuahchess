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
#include <winrt/Windows.Storage.h>
#include <winrt/Windows.Storage.Streams.h>
#include <winrt/Windows.Foundation.h>
#include <ppltasks.h>
#include "pch.h"
#include "KaruahChessEngineClass.h"
#include "KaruahChessEngineClass.g.cpp"
#include "bitboard.h"
#include "search.h"
#include "moveRules.h"
#include "engine.h"
#include "helper.h"
#include "sf_evaluate.h"


namespace winrt::KaruahChessEngine::implementation
{		
		
	using namespace helper;
	using namespace std;
	using namespace winrt::Windows;
		
	
	/// <summary>
	/// Constructor
	/// </summary>	
	KaruahChessEngineClass::KaruahChessEngineClass() {
		
		// Initialise the main components of the engine without the NNUE
		Engine::init();
						
		// Initialise with the NNUE file.
		Concurrency::create_task([this] {
			LoadNNUE().get();
			}).get();
			
		
	}

	/// <summary>
	/// Initialise with the NNUE file. 
	/// Only do this if the file has not been previously loaded, or the name has changed.
	/// </summary>	
	IAsyncAction KaruahChessEngineClass::LoadNNUE()
	{
		const char* nnueFileName = "nn-5af11540bbfe.nnue";
		if (Stockfish::Eval::currentEvalFileName != nnueFileName) {
			string nnueFilePath = "ms-appx:///Media/" + std::string(nnueFileName);
			Foundation::Uri nnUri(winrt::to_hstring(nnueFilePath));

			
			try {
				const Storage::StorageFile nnueFile = co_await Storage::StorageFile::GetFileFromApplicationUriAsync(nnUri);
				const Storage::Streams::IBuffer nnueBuffer = co_await Storage::FileIO::ReadBufferAsync(nnueFile);

				char* nnueData = (char*)nnueBuffer.data();
				long nnBufferSize = nnueBuffer.Length();

				Engine::init(nnueFileName, nnueData, nnBufferSize);
				
			}
			catch (winrt::hresult_error const& ex) {
				Engine::engineErr.add(helper::NNUE_FILE_OPEN_ERROR);
			}
			
		}

	}


	/// <summary>
	/// Gets a board FEN string
	/// </summary>
	winrt::hstring KaruahChessEngineClass::GetBoard() {

		return winrt::to_hstring(MainBoard.GetBoard());
	}

	/// <summary>
	/// Get board state string
	/// </summary>
	winrt::hstring KaruahChessEngineClass::GetState() {

		return winrt::to_hstring(MainBoard.GetState());
	}

	/// <summary>
	/// Set board from FEN string
	/// </summary>
	void KaruahChessEngineClass::SetBoard(const winrt::hstring pBoardFENString) {
		
		MainBoard.SetBoard(winrt::to_string(pBoardFENString));
	}

	/// <summary>
	/// Set board state from string
	/// </summary>
	void KaruahChessEngineClass::SetState(const winrt::hstring pBoardStateString) {				
		MainBoard.SetState(winrt::to_string(pBoardStateString));
	}
	
	/// <summary>
	/// Get board array
	/// </summary>
	void KaruahChessEngineClass::GetBoardArray(winrt::array_view<uint64_t> pBoardArray) {
		uint64_t boardArray[276];		
		MainBoard.GetBoardArray(boardArray);

		// Copy the arrays to native arrays 
		for (int i = 0; i < 276; ++i) pBoardArray[i] = boardArray[i];
		
		return;
	}

	/// <summary>
	/// Get state array
	/// </summary>
	void KaruahChessEngineClass::GetStateArray(winrt::array_view<int32_t> pStateArray) {
		int32_t stateArray[8];
		MainBoard.GetStateArray(stateArray);

		// Copy the arrays to native arrays 
		for (int i = 0; i < 8; ++i)	pStateArray[i] = stateArray[i];

		return;
	}

	/// <summary>
	/// Set board array
	/// </summary>
	void KaruahChessEngineClass::SetBoardArray(const winrt::array_view<const uint64_t> pBoardArray) {
		uint64_t boardArray[276];
		
		// Copy the arrays to native arrays 
		for (int i = 0; i < 276; ++i) boardArray[i] = pBoardArray[i];

		MainBoard.SetBoardArray(boardArray);

		return;
	}

	/// <summary>
	/// Set state array
	/// </summary>
	void KaruahChessEngineClass::SetStateArray(const winrt::array_view<const int32_t> pStateArray) {
		int32_t stateArray[8];
		
		// Copy the arrays to native arrays 
		for (int i = 0; i < 8; ++i)	stateArray[i] = pStateArray[i];
		
		MainBoard.SetStateArray(stateArray);

		return;
	}

	/// <summary>
	/// Set white clock offset
	/// </summary>
	void KaruahChessEngineClass::SetStateWhiteClockOffset(const int32_t pOffset) {
		MainBoard.StateWhiteClockOffset = pOffset;
	}

	/// <summary>
	/// Set black clock offset
	/// </summary>
	void KaruahChessEngineClass::SetStateBlackClockOffset(const int32_t pOffset) {
		MainBoard.StateBlackClockOffset = pOffset;
	}

	/// <summary>
	/// Get white clock offset
	/// </summary>
	int32_t KaruahChessEngineClass::GetStateWhiteClockOffset() {
		return MainBoard.StateWhiteClockOffset;
	}

	/// <summary>
	/// Get black clock offset
	/// </summary>
	int32_t KaruahChessEngineClass::GetStateBlackClockOffset() {
		return MainBoard.StateBlackClockOffset;
	}


	/// <summary>
	/// Resets the board
	/// </summary>
	void KaruahChessEngineClass::Reset() {
		MainBoard.Reset();
		SearchBoard.Reset();
		Search::Cancel();
		Search::ClearCache();
	}

	/// <summary>
	/// Cancels the search
	/// </summary>
	void KaruahChessEngineClass::CancelSearch() {
		Search::Cancel();
	}


	/// <summary>
	/// Get spin of an index
	/// </summary>
	int32_t KaruahChessEngineClass::GetSpin(const int32_t pIndex) {
		return MainBoard.GetSpin(pIndex);
	}

	/// <summary>
	/// Get state active colour
	/// </summary>
	int32_t KaruahChessEngineClass::GetStateActiveColour() {
		return MainBoard.StateActiveColour;
	}

	/// <summary>
	/// Set state active colour
	/// </summary>
	void KaruahChessEngineClass::SetStateActiveColour(const int32_t pColour) {
		if (pColour != MainBoard.StateActiveColour) {
			// Clear the enpassant if changing the colour
			MainBoard.StateEnpassantIndex = -1;

			// Change colour
			MainBoard.StateActiveColour = pColour;
		}
		
	}

	/// <summary>
	/// Get state game status
	/// </summary>
	int32_t KaruahChessEngineClass::GetStateGameStatus() {
		return MainBoard.StateGameStatus;
	}
	
	/// <summary>
	/// Set state game status
	/// </summary>
	void KaruahChessEngineClass::SetStateGameStatus(const int32_t pStatus) {
		MainBoard.StateGameStatus = pStatus;
	}

	/// <summary>
	/// Get full move count
	/// </summary>
	int32_t KaruahChessEngineClass::GetStateFullMoveCount() {
		return MainBoard.StateFullMoveCount;
	}

	/// <summary>
	/// Get castling availability
	/// </summary>
	int32_t KaruahChessEngineClass::GetStateCastlingAvailability() {
		return MainBoard.StateCastlingAvailability;
	}

	/// <summary>
	/// Sets castling availability
	/// </summary>	
	bool KaruahChessEngineClass::SetStateCastlingAvailability(const int32_t pCastlingAvailability, const int32_t pColour) {
			
		if (pColour == helper::WHITEPIECE) {
			return MainBoard.setStateCastlingAvailability<helper::WHITEPIECE>(pCastlingAvailability);
		}
		else {
			return MainBoard.setStateCastlingAvailability<helper::BLACKPIECE>(pCastlingAvailability);
		}
	}

	/// <summary>
	/// Get king index
	/// </summary>
	int32_t KaruahChessEngineClass::GetKingIndex(const int32_t pColour) {
		if (pColour == WHITEPIECE) {
			return MainBoard.KingIndex<WHITEPIECE>();
		}
		else {
			return MainBoard.KingIndex<BLACKPIECE>();
		}
	}

	/// <summary>
	/// Determine if king is in check
	/// </summary>
	bool KaruahChessEngineClass::IsKingCheck(const int32_t pColour) {
		return MainBoard.IsKingCheck(pColour);
	}

	/// <summary>
	/// Gets a potential move of a given square index
	/// </summary>
	uint64_t KaruahChessEngineClass::GetPotentialMove(const int32_t pSqIndex) {
		return MainBoard.GetPotentialMove(pSqIndex);
	}

	/// <summary>
	/// Gets positions occupied by a particular spin
	/// </summary>
	uint64_t KaruahChessEngineClass::GetOccupiedBySpin(const int32_t pSpin) {
		return MainBoard.GetOccupiedBySpin(pSpin);
	}
	
	/// <summary>
	/// Gets all positions occupied by the specified colour
	/// </summary>
	uint64_t KaruahChessEngineClass::GetOccupiedByColour(const int32_t pColour) {
		return MainBoard.GetOccupied(pColour);
	}


	/// <summary>
	/// Moves a piece
	/// </summary>	
	MoveResult KaruahChessEngineClass::Move(const int32_t pFromIndex, const int32_t pToIndex, const int32_t pPawnPromotionPiece, const bool pValidateEnabled, const bool pCommit)
	{	

		helper::PawnPromotionEnum pawnPromotion = static_cast<helper::PawnPromotionEnum>(pPawnPromotionPiece);
		bool success = MoveRules::Move(pFromIndex, pToIndex, MainBoard, pawnPromotion, pValidateEnabled, pCommit);
		MoveResult mResult;
		mResult.success = success;
		mResult.returnMessage = to_hstring(MainBoard.ReturnMessage);
		mResult.moveDataStr = winrt::to_hstring(MainBoard.MoveData[0]) + winrt::to_hstring("|") + winrt::to_hstring(MainBoard.MoveData[1]) + winrt::to_hstring("|") + winrt::to_hstring(MainBoard.MoveData[2]) + winrt::to_hstring("|") + winrt::to_hstring(MainBoard.MoveData[3]);
		return mResult;
	}

	/// <summary>
	/// Arrange a piece. Used for editing the board.
	/// </summary>		
	MoveResult KaruahChessEngineClass::Arrange(const int32_t pFromIndex, const int32_t pToIndex)
	{				
		bool success = MoveRules::Arrange(pFromIndex, pToIndex, MainBoard);	
		
		if (success) {
			// Clear EnPassant
			MainBoard.StateEnpassantIndex = -1; 
		}

		MoveResult mResult;
		mResult.success = success;
		mResult.returnMessage = to_hstring(MainBoard.ReturnMessage);
		
		return mResult;
		

	}

	/// <summary>
	/// Updates a piece on the board. Used for editing the board.
	/// </summary>		
	MoveResult KaruahChessEngineClass::ArrangeUpdate(const char16_t pFen, const int32_t pToIndex)
	{
		bool success = MoveRules::ArrangeUpdate((char)pFen, pToIndex, MainBoard);

		if (success) {
			// Clear EnPassant
			MainBoard.StateEnpassantIndex = -1;
		}

		MoveResult mResult;
		mResult.success = success;
		mResult.returnMessage = to_hstring(MainBoard.ReturnMessage);

		return mResult;


	}

	/// <summary>
	/// Find valid from index from a given to index
	/// </summary>	
	int32_t KaruahChessEngineClass::FindFromIndex(const int32_t pToIndex, const int32_t pSpin, const winrt::array_view<const int32_t> pValidFromIndexes) {

		std::vector<int> validFromIndexesVector;

		for (const int32_t validFromIndex : pValidFromIndexes) {
			validFromIndexesVector.push_back(validFromIndex);
		}

		int fromIndex = MoveRules::FindFromIndex(MainBoard, pToIndex, pSpin, validFromIndexesVector);
		
		return fromIndex;
	}

	/// <summary>
	/// Gets spin value from piece name
	/// </summary>	
	int32_t KaruahChessEngineClass::GetSpinFromPieceName(const winrt::hstring pPieceName) {
		return helper::GetSpinFromPieceName(winrt::to_string(pPieceName));
	}

	/// <summary>
	/// Get piece name from a FEN char
	/// </summary>
	winrt::hstring KaruahChessEngineClass::GetPieceNameFromChar(const char16_t pFENChar) {
		return winrt::to_hstring(helper::GetPieceNameFromChar((char)pFENChar));
	}

	/// <summary>
	/// Get FEN char from spin
	/// </summary>
	char16_t KaruahChessEngineClass::GetFENCharFromSpin(const int32_t pSpin) {
		return (char16_t)helper::GetFENCharFromSpin(pSpin);
	}


	/// <summary>
	/// Get full fen string of the board
	/// </summary>
	winrt::hstring KaruahChessEngineClass::GetFullFEN() {
		return winrt::to_hstring(MainBoard.GetFullFEN());
	}

	

	/// <summary>
	/// Checks if a move is a pawn promoting move
	/// </summary>
	bool KaruahChessEngineClass::IsPawnPromotion(const int32_t pFromIndex, const int32_t pToIndex) {
		return MoveRules::IsPawnPromotion(pFromIndex, pToIndex, MainBoard);		 
	}
	


	/// <summary>
	/// Searches for the best move
	/// </summary>		
	SearchResult KaruahChessEngineClass::SearchStart(const SearchOptions pSearchOptions)
	{
		uint64_t boardArray[276];
		int32_t stateArray[8];

		MainBoard.GetBoardArray(boardArray);
		MainBoard.GetStateArray(stateArray);
		SearchBoard.SetBoardArray(boardArray);
		SearchBoard.SetStateArray(stateArray);

		// Search for a move
		Search::SearchTreeNode bestMove;
		Search::SearchStatistics statistics;
		Search::SearchOptions options;
		options.randomiseFirstMove = pSearchOptions.randomiseFirstMove;
		options.limitSkillLevel = pSearchOptions.limitSkillLevel;
		options.limitDepth = pSearchOptions.limitDepth;
		options.limitNodes = pSearchOptions.limitNodes;
		options.limitMoveDuration = pSearchOptions.limitMoveDuration;
		options.limitThreads = pSearchOptions.limitThreads;
		
		Search::GetBestMove(SearchBoard, options, bestMove, statistics);

		// Copy the values to result
		SearchResult result = SearchResult();
		result.moveFromIndex = bestMove.moveFromIndex;
		result.moveToIndex = bestMove.moveToIndex;		
		result.promotionPieceType = bestMove.promotionPieceType;
		result.cancelled = bestMove.cancelled;
		result.error = bestMove.error;
		result.errorMessage = winrt::to_hstring(helper::SearchErrorMessage.at(bestMove.error).c_str());
		return result;
	}


}

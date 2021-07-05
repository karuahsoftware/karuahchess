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

#include "Search.h"
#include "MoveRules.h"
#include "BitBoard.h"
#include "PiecePattern.h"
#include "helper.h"
#include "SFposition.h"
#include "SFthread.h"
#include "SFuci.h"
#include <chrono>
#include <time.h>
#include <random>


namespace Search {

    using namespace helper;

	bool _cancel = false;

	/// <summary>
	/// Gets top move for a given board
	/// </summary>
    void GetBestMove(BitBoard& pBoard, SearchOptions pSearchOptions, SearchTreeNode& pBestMove, SearchStatistics& pStatistics)
    {
        _cancel = false;

        pStatistics.StartTime = std::chrono::steady_clock::now();

        // Don't perform a search if the engine can't cope with the board configuration
        bool searchError = false;
        if (pBoard.Count<WHITEPIECE>(KING_SPIN) != 1 || pBoard.Count<BLACKPIECE>(KING_SPIN) != 1) searchError = true;
        else if ((pBoard.Count<WHITEPIECE>() > 16) || (pBoard.Count<BLACKPIECE>() > 16)) searchError = true;
        else if (((pBoard.Count<WHITEPIECE>(QUEEN_SPIN) + pBoard.Count<WHITEPIECE>(PAWN_SPIN)) > 9) || ((pBoard.Count<BLACKPIECE>(QUEEN_SPIN) + pBoard.Count<BLACKPIECE>(PAWN_SPIN)) > 9)) searchError = true;
        else if (((pBoard.Count<WHITEPIECE>(BISHOP_SPIN) + pBoard.Count<WHITEPIECE>(PAWN_SPIN)) > 10) || ((pBoard.Count<BLACKPIECE>(BISHOP_SPIN) + pBoard.Count<BLACKPIECE>(PAWN_SPIN)) > 10)) searchError = true;
        else if (((pBoard.Count<WHITEPIECE>(KNIGHT_SPIN) + pBoard.Count<WHITEPIECE>(PAWN_SPIN)) > 10) || ((pBoard.Count<BLACKPIECE>(KNIGHT_SPIN) + pBoard.Count<BLACKPIECE>(PAWN_SPIN)) > 10)) searchError = true;
        else if (((pBoard.Count<WHITEPIECE>(ROOK_SPIN) + pBoard.Count<WHITEPIECE>(PAWN_SPIN)) > 10) || ((pBoard.Count<BLACKPIECE>(ROOK_SPIN) + pBoard.Count<BLACKPIECE>(PAWN_SPIN)) > 10)) searchError = true;
        else if ((pBoard.Count<WHITEPIECE>(PAWN_SPIN) > 8) || (pBoard.Count<BLACKPIECE>(PAWN_SPIN) > 8)) searchError = true;
        else if (((pBoard.GetOccupiedBySpin<WHITEPIECE>(PAWN_SPIN) | pBoard.GetOccupiedBySpin<BLACKPIECE>(PAWN_SPIN)) & (RANK1 | RANK8)) > 0) searchError = true;
        else if ((pBoard.IsKingCheck(WHITEPIECE) && pBoard.StateActiveColour == BLACKPIECE)) searchError = true;
        else if ((pBoard.IsKingCheck(BLACKPIECE) && pBoard.StateActiveColour == WHITEPIECE)) searchError = true;

        if (!searchError) {
            // Set options
            if (pSearchOptions.limitStrengthELO >= 1350 && pSearchOptions.limitStrengthELO < 2850) {
                // Limit engine strength
                SF::Options["UCI_LimitStrength"] << SF::UCI::Option(true);
                SF::Options["UCI_Elo"] << SF::UCI::Option(pSearchOptions.limitStrengthELO, 1350, 2850);
            }
            else {
                // Set engine to max
                SF::Options["UCI_LimitStrength"] << SF::UCI::Option(false);
                SF::Options["UCI_Elo"] << SF::UCI::Option(1350, 1350, 2850);
            }

            // Get the board position
            SF::Position pos;
            SF::StateListPtr states = SF::StateListPtr(new std::deque<SF::StateInfo>(1));
            std::string startFEN = pBoard.GetFullFEN();

            // Set the position
            SF::Thread* mainThread = SF::Threads.main();
            pos.set(startFEN, false, &states->back(), mainThread);

            // Do the search
            SF::Search::LimitsType limits;
            limits.startTime = SF::now();
            limits.depth = 10;
            SF::Threads.start_thinking(pos, states, limits, false);
            SF::Threads.main()->wait_for_search_finished();
            SF::Search::RootMoves rootMoves = mainThread->rootMoves;


            int rootIndex = 0;
            if (pBoard.StateFullMoveCount < 1 && rootMoves.size() > 4) {
                auto rd = std::random_device{};
                std::mt19937 gen(rd());
                std::uniform_int_distribution<> distrib(0, 4);
                rootIndex = distrib(gen);
            }

            SF::Move m = rootMoves[rootIndex].pv[0];

            // Mirror the result as the karuah chess board is a mirror of
            // the sf board
            const int fromIndex = helper::mirrorRank(SF::from_sq(m));
            const int toIndex = helper::mirrorRank(SF::to_sq(m));

            if (m == SF::MOVE_NONE || m == SF::MOVE_NULL) {
                // No move found
                pBestMove.moveFromIndex = -1;
                pBestMove.moveToIndex = -1;
            }
            else if (SF::type_of(m) == SF::CASTLING) {
                pBestMove.moveFromIndex = fromIndex;

                // Set up the castling move
                if (fromIndex == 4) {
                    pBestMove.moveToIndex = toIndex > fromIndex ? 6 : 2;
                }
                else if (fromIndex == 60) {
                    pBestMove.moveToIndex = toIndex > fromIndex ? 62 : 58;
                }
            }
            else if (SF::type_of(m) == SF::PROMOTION) {
                // Set the promotion piece
                pBestMove.promotionPieceType = SF::promotion_type(m);
                pBestMove.moveFromIndex = fromIndex;
                pBestMove.moveToIndex = toIndex;
            }
            else {
                pBestMove.moveFromIndex = fromIndex;
                pBestMove.moveToIndex = toIndex;
            }
        }
        else {
            pBestMove.error = true;
        }

        pStatistics.EndTime = std::chrono::steady_clock::now();
        pStatistics.DurationMS = std::chrono::duration_cast<std::chrono::milliseconds>(pStatistics.EndTime - pStatistics.StartTime);
        pBestMove.cancelled = _cancel;


    }


	/// <summary>
	/// Reset
	/// </summary>
	/// <returns></returns>
	void Cancel() {
		_cancel = true;
		SF::Threads.stop = true;
	}

	/// <summary>
	/// Clears the cache
	/// </summary>
	/// <returns></returns>
	void ClearCache()
	{
		SF::Search::clear();
	}

}



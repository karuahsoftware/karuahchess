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

#include "search.h"
#include "moverules.h"
#include "bitboard.h"
#include "piecepattern.h"
#include "helper.h"
#include "engine.h"
#include "sf_position.h"
#include "sf_thread.h"
#include "sf_uci.h"
#include "sf_types.h"
#include <chrono>
#include <time.h>
#include <random>



namespace Search {

    using namespace helper;

    bool _cancel = false;
       

    /// <summary>
    /// Sets an option in stock fish if the option is different from the current option
    /// </summary>
    void setOption(std::string name, int value) {
        
        if (Engine::mainUCI->options.count(name)) {
            double currentValue = Engine::mainUCI->options[name];
            double newValue = (double)value;
            
            if (currentValue != newValue) {
                Engine::mainUCI->options[name] = std::to_string(value);
            }
        }

    }


    /// <summary>
    /// Gets top move for a given board
    /// </summary>
    void GetBestMove(BitBoard& pBoard, SearchOptions pSearchOptions, SearchTreeNode& pBestMove, SearchStatistics& pStatistics)
    {
        
        _cancel = false;
        
        pStatistics.StartTime = std::chrono::steady_clock::now();

        // Don't perform a search if the engine can't cope with the board configuration
        int searchError = pBoard.VerifyBoardConfiguration();
        
        // Check for engine errors.
        if (Engine::engineErr.errorList.size() > 0) {
            searchError = Engine::engineErr.errorList[0]; // Get the first error.
        }
        
        if (searchError == 0) {
          
            setOption("Threads", pSearchOptions.limitThreads);
                     
            // Set options
            if (pSearchOptions.limitSkillLevel >= -10 && pSearchOptions.limitSkillLevel < 20) {
                // Set engine strength             
                setOption("Skill Level", pSearchOptions.limitSkillLevel);
            }
            else {
                // Set engine to max
                setOption("Skill Level", 20);
            }

            // Get the board position
            Stockfish::Position pos;
            Stockfish::StateListPtr states = Stockfish::StateListPtr(new std::deque<Stockfish::StateInfo>(1));
            std::string startFEN = pBoard.GetFullFEN();

            // Set the position            
            pos.set(startFEN, false, &states->back());

            // Do the search
            Stockfish::Search::LimitsType limits;
            limits.startTime = Stockfish::now();
          
            // Set the limits from the GUI
            limits.depth = pSearchOptions.limitDepth;
            limits.nodes = pSearchOptions.limitNodes;
            limits.movetime = pSearchOptions.limitMoveDuration;
                        
            
            Engine::mainUCI->threads.start_thinking(Engine::mainUCI->options, pos, states, limits, false);
            Engine::mainUCI->threads.main_thread()->wait_for_search_finished();
            Stockfish::Search::RootMoves rootMoves = Engine::mainUCI->threads.main_thread()->worker.get()->getRootMoves();
           
            int rootIndex = 0;
            if (pSearchOptions.randomiseFirstMove && pBoard.StateFullMoveCount < 1 && rootMoves.size() > 4) {
                auto rd = std::random_device{};
                std::mt19937 gen(rd());
                std::uniform_int_distribution<> distrib(0, 4);
                rootIndex = distrib(gen);
            }

            Stockfish::Move m = rootMoves[rootIndex].pv[0];

            // Mirror the result as the karuah chess board is a mirror of
            // the sf board
            const int fromIndex = helper::mirrorRank(m.from_sq());
            const int toIndex = helper::mirrorRank(m.to_sq());

            if (m == Stockfish::Move::none() || m == Stockfish::Move::null()) {
                // No move found
                pBestMove.moveFromIndex = -1;
                pBestMove.moveToIndex = -1;
            }
            else if (m.type_of() == Stockfish::CASTLING) {
                pBestMove.moveFromIndex = fromIndex;

                // Set up the castling move
                if (fromIndex == 4) {
                    pBestMove.moveToIndex = toIndex > fromIndex ? 6 : 2;
                }
                else if (fromIndex == 60) {
                    pBestMove.moveToIndex = toIndex > fromIndex ? 62 : 58;
                }
            }
            else if (m.type_of() == Stockfish::PROMOTION) {
                // Set the promotion piece
                pBestMove.promotionPieceType = m.promotion_type();
                pBestMove.moveFromIndex = fromIndex;
                pBestMove.moveToIndex = toIndex;
            }
            else {
                pBestMove.moveFromIndex = fromIndex;
                pBestMove.moveToIndex = toIndex;
            }
        }
        else {
            pBestMove.error = searchError;
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
        Engine::mainUCI->threads.stop = true;
    }

    /// <summary>
    /// Clears the cache
    /// </summary>
    /// <returns></returns>
    void ClearCache()
    {
        Engine::mainUCI->search_clear();
    }

}



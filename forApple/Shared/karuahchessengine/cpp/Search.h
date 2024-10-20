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
#include "bitboard.h"
#include <cstdint>
#include <chrono>

namespace KaruahChess {

	namespace Search {

		struct SearchTreeNode {

			int moveFromIndex = -1;
			int moveToIndex = -1;
			int promotionPieceType = 0;
			bool cancelled = false;
			int error = 0;

		};

		struct SearchStatistics {
			std::chrono::time_point<std::chrono::steady_clock> StartTime;
			std::chrono::time_point<std::chrono::steady_clock> EndTime;
			std::chrono::milliseconds DurationMS = std::chrono::milliseconds::zero();

		};

		struct SearchOptions {
			int limitSkillLevel = 0;
			int limitDepth = 0;
			int limitNodes = 0;
			int limitMoveDuration = 0;
			int limitThreads = 1;
			bool randomiseFirstMove = false;
			bool alternateMove = false;

		};

		// Functions
		extern void GetBestMove(BitBoard& pBoard, SearchOptions pSearchOptions, SearchTreeNode& pBestMove, SearchStatistics& pStatistics);


		extern void Cancel();
		extern void ClearCache();
	}

}
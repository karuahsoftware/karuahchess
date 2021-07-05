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

#include "Engine.h"
#include "helper.h"
#include "SFuci.h"
#include "SFbitboard.h"
#include "SFposition.h"
#include "SFendgame.h"
#include "SFsearch.h"
#include "SFthread.h"
#include "SFpsqt.h"

#include <thread>


	namespace Engine {

		bool isInitialised = false;

		// Initialise the engine
		void init() {
			if (!isInitialised) {
				// Set the maximum threads to run at the same time to be at least one and always one less than the maximum
				// so that some capacity is left over for the application
				const int maxThreads = std::thread::hardware_concurrency() > 1 ? std::thread::hardware_concurrency() - 1 : 1;

				helper::init();
				
				constexpr int maxHashSizeMB = 16;
				SF::UCI::init(SF::Options, maxHashSizeMB);
				
				SF::PSQT::init();
				SF::Bitboards::init();
				SF::Position::init();
				SF::Bitbases::init();
				SF::Endgames::init();
				SF::Threads.set(size_t(maxThreads));
				SF::Search::clear(); // After threads are up
											

			}

			isInitialised = true;
		}

	}

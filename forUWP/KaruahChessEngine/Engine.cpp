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
		unsigned int threadLimit = 0;

		// Initialise the engine
		void init() {
			if (!isInitialised) {
				
				helper::init();
				
				constexpr int maxHashSizeMB = 16;
				SF::UCI::init(SF::Options, maxHashSizeMB);
				
				SF::PSQT::init();
				SF::Bitboards::init();
				SF::Position::init();
				SF::Bitbases::init();
				SF::Endgames::init();
				setThreads(1);
				
			}

			isInitialised = true;
		}


		// Initialise the threads, used to set the number of threads
		void setThreads(unsigned int pRequestMaxThreads) {

			// Limit the maximum threads to at least one and always one less than the maximum
			// so that some capacity is left over for the application
			unsigned int newThreadLimit = std::thread::hardware_concurrency() > 1 ? std::min(std::thread::hardware_concurrency() - 1, pRequestMaxThreads): 1;

			// Check that thread limit is in the valid range, otherwise just set it to 1
			if (newThreadLimit < 1 || newThreadLimit > 512) newThreadLimit = 1;


			if (threadLimit != newThreadLimit) {
				SF::Threads.set(size_t(newThreadLimit));
				SF::Search::clear(); // After threads are up
			}
		}
		
		
		

	}

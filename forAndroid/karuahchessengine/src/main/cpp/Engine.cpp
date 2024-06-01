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


#include "engine.h"
#include "helper.h"
#include "sf_uci.h"
#include "sf_bitboard.h"
#include "sf_position.h"
#include "sf_search.h"
#include "sf_thread.h"

#include <thread>
#include <filesystem>
#include <istream>

	namespace Engine {

		using namespace std;

		bool SFInitialised = false;
		bool NNUEInitialised = false;
		unsigned int threadLimit = 0;
		
		char* nnueFileBufferBig;
        long nnueFileBufferSizeBig = 0;
		string nnueFileNameBig;
		bool nnueLoadedBig = false;

		char* nnueFileBufferSmall;
		long nnueFileBufferSizeSmall = 0;
		string nnueFileNameSmall;
		bool nnueLoadedSmall = false;

		EngineError engineErr;
				
		std::unique_ptr<Stockfish::UCI> mainUCI;

		// Initialise the engine
		void init() {

			// Initialise Karuah Chess
			helper::init();

			// Initialise Stockfish
			if (!SFInitialised) {

				mainUCI = std::make_unique<Stockfish::UCI>();
				Stockfish::Bitboards::init();
				Stockfish::Position::init();
				
				setThreads(1);

				SFInitialised = true;
			}

		}

		// Initialise the engine with NNUE
		void init(string pNNUEFileNameBig, char* pNNUEFileBufferBig, long pNNUEFileBufferSizeBig, string pNNUEFileNameSmall, char* pNNUEFileBufferSmall, long pNNUEFileBufferSizeSmall) {

			// Initiliase the engine
			init();
						
			nnueFileNameBig = pNNUEFileNameBig;
			nnueFileBufferBig = pNNUEFileBufferBig;
			nnueFileBufferSizeBig = pNNUEFileBufferSizeBig;

			nnueFileNameSmall = pNNUEFileNameSmall;
			nnueFileBufferSmall = pNNUEFileBufferSmall;
			nnueFileBufferSizeSmall = pNNUEFileBufferSizeSmall;

			Stockfish::Eval::NNUE::load_networks();

			
		}


		// Initialise the threads, used to set the number of threads
		void setThreads(unsigned int pRequestMaxThreads) {

			// Limit the maximum threads to at least one and always one less than the maximum
			// so that some capacity is left over for the application
			unsigned int newThreadLimit = thread::hardware_concurrency() > 1 ? std::min(std::thread::hardware_concurrency() - 1, pRequestMaxThreads): 1;

			// Check that thread limit is in the valid range, otherwise just set it to 1
			if (newThreadLimit < 1 || newThreadLimit > 512) newThreadLimit = 1;


			if (threadLimit != newThreadLimit) {
				
				Engine::mainUCI->options["Threads"] = std::to_string(newThreadLimit);
			}
		}
		

	}

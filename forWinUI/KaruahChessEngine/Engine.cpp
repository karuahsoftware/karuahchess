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

#include "engine.h"
#include "helper.h"
#include "sf_uci.h"
#include "sf_bitboard.h"
#include "sf_position.h"
#include "sf_endgame.h"
#include "sf_search.h"
#include "sf_thread.h"
#include "sf_psqt.h"

#include <thread>
#include <filesystem>
#include <istream>

	namespace Engine {

		using namespace std;

		bool SFInitialised = false;
		bool NNUEInitialised = false;
		unsigned int threadLimit = 0;
		char* nnueFileBuffer;
        long nnueFileBufferSize = 0;
		string nnueFileName;
		EngineError engineErr;

		// Initialise the engine
		void init() {

			// Initialise Karuah Chess
			helper::init();

			// Initialise Stockfish
			if (!SFInitialised) {

				Stockfish::UCI::init(Stockfish::Options);
				
				Stockfish::PSQT::init();
				Stockfish::Bitboards::init();
				Stockfish::Position::init();
				Stockfish::Bitbases::init();
				Stockfish::Endgames::init();
				setThreads(1);

				SFInitialised = true;
			}

		}

		// Initialise the engine with NNUE
		void init(string pNNUEFileName, char* pNNUEFileBuffer, long pNNUEFileBufferSize) {

			// Initiliase the engine
			init();

			// Initiliase NNUE
			// The NNUE init checks if the filename has changed and will reload if necessary
			nnueFileName = pNNUEFileName;
			nnueFileBuffer = pNNUEFileBuffer;
			nnueFileBufferSize = pNNUEFileBufferSize;
			Stockfish::Eval::NNUE::init();
		}


		// Initialise the threads, used to set the number of threads
		void setThreads(unsigned int pRequestMaxThreads) {

			// Limit the maximum threads to at least one and always one less than the maximum
			// so that some capacity is left over for the application
			unsigned int newThreadLimit = thread::hardware_concurrency() > 1 ? std::min(std::thread::hardware_concurrency() - 1, pRequestMaxThreads): 1;

			// Check that thread limit is in the valid range, otherwise just set it to 1
			if (newThreadLimit < 1 || newThreadLimit > 512) newThreadLimit = 1;


			if (threadLimit != newThreadLimit) {
				Stockfish::Threads.set(size_t(newThreadLimit));
				Stockfish::Search::clear(); // After threads are up
			}
		}
		

	}

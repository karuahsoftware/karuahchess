/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2026 Karuah Software

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

#include <vector>
#include <filesystem>
#include <istream>
#include <streambuf>

// Forward declaring class
namespace Stockfish {
	class UCIEngine;
}

namespace KaruahChess {
	namespace Engine {

		using namespace std;		

		struct EngineError {
			vector<int> errorList;

			// Add the error to the list if it does not exist
			void add(int errorNumber) {
				if (find(errorList.begin(), errorList.end(), errorNumber) == errorList.end()) {
					errorList.push_back(errorNumber);
				}
			}

			// Checks for the existance of an error
			bool exists(int errorNumber) {
				return find(errorList.begin(), errorList.end(), errorNumber) != errorList.end();
			}

		};

		struct membuf : streambuf
		{
			membuf(char* begin, char* end) {
				this->setg(begin, begin, end);
			}
		};

		extern void init(string pNNUEFileNameBig, char* pNNUEFileBufferBig, long pNNUEFileBufferSizeBig, string pNNUEFileNameSmall, char* pNNUEFileBufferSmall, long pNNUEFileBufferSizeSmall);
		extern void setThreads(unsigned int pMaxThreads);
		extern void setOption(std::string name, int value);
		extern EngineError engineErr;

		extern string nnueFileNameBig;
		extern char* nnueFileBufferBig;
		extern long nnueFileBufferSizeBig;
		extern bool nnueLoadedBig;

		extern string nnueFileNameSmall;
		extern char* nnueFileBufferSmall;
		extern long nnueFileBufferSizeSmall;
		extern bool nnueLoadedSmall;

		extern std::unique_ptr<Stockfish::UCIEngine> mainUCI;
	}

}


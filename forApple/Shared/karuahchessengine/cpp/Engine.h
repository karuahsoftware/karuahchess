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

#include <vector>
#include <filesystem>
#include <istream>
#include <streambuf>

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

		extern void init();
		extern void init(string pNNUEFileName, char* pNNUEFileBuffer, long pNNUEFileBufferSize);
		extern void setThreads(unsigned int pMaxThreads);
		extern EngineError engineErr;
		extern string nnueFileName;
		extern char* nnueFileBuffer;
        extern long nnueFileBufferSize;
	}


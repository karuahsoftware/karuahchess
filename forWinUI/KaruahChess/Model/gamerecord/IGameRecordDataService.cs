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

using System;
using System.Collections.Generic;


namespace KaruahChess.Model
{

    public interface IGameRecordDataService
    {
        void Load();
        GameRecordArray GetCurrentGame();
        GameRecordArray Get(int pId);

        GameRecordArray Get();

        int RecordGameState(int pWhiteClockOffset, int pBlackClockOffset, string pMoveSAN);
        int UpdateGameState(GameRecordArray pGameRecordArray);

        void Reset(int pWhiteClockOffset, int pBlackClockOffset);
        
       
        bool Undo();

        HashSet<int> GetBoardSquareChanges(GameRecordArray pBoardA, GameRecordArray pBoardB);

        List<int> GetAllRecordIDList();

        void newTransaction();

        int RecordCount();

        SortedList<int, GameRecordArray> GameHistory();

        int GetActiveMoveColour(int pId);
        
        int GetStateGameStatus(int pId);
    }
}

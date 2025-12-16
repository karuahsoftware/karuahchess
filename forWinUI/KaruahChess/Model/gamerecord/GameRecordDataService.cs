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

using KaruahChess.Database;
using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Data.Sqlite;
using KaruahChessEngine;



namespace KaruahChess.Model
{
    public class GameRecordDataService : IGameRecordDataService
    {

        Dictionary<int, GameRecordArray> _gameRecordDict;
        KaruahChessEngineClass _tempBoardA;
        KaruahChessEngineClass _tempBoardB;
        KaruahChessEngineClass _tempBoardC;

        public KaruahChessEngineClass CurrentGame { get; private set; }
        public long transactionId {get; private set;} 

        // Constructor
        private GameRecordDataService()
        {          
           
          
            _gameRecordDict = new Dictionary<int, GameRecordArray>();

            CurrentGame = new KaruahChessEngineClass();            
            _tempBoardA = new KaruahChessEngineClass();
            _tempBoardB = new KaruahChessEngineClass();
            _tempBoardC = new KaruahChessEngineClass();

            transactionId = 0;

            Load();
        }

        /// <summary>
        /// Singleton accessor
        /// </summary>
        public static GameRecordDataService instance { get; private set; } = new GameRecordDataService();


        /// <summary>
        /// Loads parameters
        /// </summary>
        public void Load()
        {
            KaruahChessEngineClass board = new KaruahChessEngineClass();

            // Clear records from memory
            _gameRecordDict.Clear();

            // Load game records in to memory            
            var connection = KaruahChessDB.GetDBConnection();            
            using (var command = connection.CreateCommand())
            {
                command.CommandText = $"select * from {KaruahChessDB.GameRecordTableName} order by id";
                SqliteDataReader reader = command.ExecuteReader();

               
                    while (reader.Read())
                    {
                        int Id = Convert.ToInt32(reader["Id"]);
                        String boardSquareStr = Convert.ToString(reader["BoardSquareStr"]);
                        String gameStateStr = Convert.ToString(reader["GameStateStr"]);
                        String moveSANStr = Convert.ToString(reader["MoveSANStr"]);
                        board.SetBoard(boardSquareStr);
                        board.SetState(gameStateStr);

                        GameRecordArray recArray = new GameRecordArray();
                        recArray.Id = Id;
                        board.GetBoardArray(recArray.BoardArray);
                        board.GetStateArray(recArray.StateArray);
                        recArray.MoveSAN = moveSANStr;

                        _gameRecordDict.Add(Id, recArray);
                    }
                

            }

            connection.Close();

            
      
            // If no records were loaded then create a default record
            if (_gameRecordDict.Count == 0)
            {
                Reset(0, 0);
            }

            // Set current game to latest bitboard
            var latestRecord = Get();
            CurrentGame.SetBoardArray(latestRecord.BoardArray);
            CurrentGame.SetStateArray(latestRecord.StateArray);
                        
        }

        /// <summary>
        /// Gets the current game as an array
        /// </summary>
        /// <param name="pId"></param>
        /// <returns></returns>
        public GameRecordArray GetCurrentGame()
        {
            GameRecordArray recArray = new GameRecordArray();
            recArray.Id = -1;
            CurrentGame.GetBoardArray(recArray.BoardArray);
            CurrentGame.GetStateArray(recArray.StateArray);

            return recArray;

        }

        /// <summary>
        /// Gets a record set by Id
        /// </summary>
        /// <param name="pId"></param>
        /// <returns></returns>
        public GameRecordArray Get(int pId)
        {
            if (_gameRecordDict.ContainsKey(pId)) {
                GameRecordArray rec = _gameRecordDict[pId];                
                return rec;
            }
            else {
                return null;
            }
           
        }

        /// <summary>
        /// Gets the latest game record
        /// </summary>
        /// <returns></returns>
        public GameRecordArray Get()
        {
            int latestId = GetMaxId();
            return (Get(latestId));

        }

        /// <summary>
        /// Record the current state of the game as a record
        /// </summary>
        /// <returns></returns>
        public int RecordGameState(int pWhiteClockOffset, int pBlackClockOffset, string pMoveSAN)
        {
            int result = 0;

            // Set the clocks
            CurrentGame.SetStateWhiteClockOffset(pWhiteClockOffset);
            CurrentGame.SetStateBlackClockOffset(pBlackClockOffset);


            // create game record            
            int nextId = GetMaxId() + 1;
            String boardSquareStr = CurrentGame.GetBoard();
            String gameStateStr = CurrentGame.GetState();

            var gameRecordArray = new GameRecordArray();
            gameRecordArray.Id = nextId;
            CurrentGame.GetBoardArray(gameRecordArray.BoardArray);
            CurrentGame.GetStateArray(gameRecordArray.StateArray);
            gameRecordArray.MoveSAN = pMoveSAN;

            if (!_gameRecordDict.ContainsKey(gameRecordArray.Id)) {

                // Add record to dictionary
                _gameRecordDict.Add(gameRecordArray.Id, gameRecordArray);

                // Add record to database
                var connection = KaruahChessDB.GetDBConnection();                
                    using (var command = connection.CreateCommand())
                    {
                        command.CommandText = $"INSERT INTO {KaruahChessDB.GameRecordTableName} (Id, BoardSquareStr, GameStateStr, MoveSANStr) Values (@Id, @BoardSquareStr, @GameStateStr, @MoveSANStr);";                    
                        command.Parameters.Add(new SqliteParameter("@Id", nextId));
                        command.Parameters.Add(new SqliteParameter("@BoardSquareStr", boardSquareStr));
                        command.Parameters.Add(new SqliteParameter("@GameStateStr", gameStateStr));                    
                        command.Parameters.Add(new SqliteParameter("@MoveSANStr", gameRecordArray.MoveSAN));
                        result = command.ExecuteNonQuery();
                        transactionId++;                
                    }

                    connection.Close();
                }
            

            return result;
        }

        /// <summary>
        /// Updates a board record
        /// </summary>
        /// <returns></returns>
        public int UpdateGameState(GameRecordArray pGameRecordArray)
        {
            int result = 0;
            
            if (_gameRecordDict.ContainsKey(pGameRecordArray.Id))
            {
                _tempBoardC.SetBoardArray(pGameRecordArray.BoardArray);
                _tempBoardC.SetStateArray(pGameRecordArray.StateArray);
                string boardSquareStr = _tempBoardC.GetBoard();
                string gameStateStr = _tempBoardC.GetState();

                // Add record to dictionary
                _gameRecordDict[pGameRecordArray.Id] = pGameRecordArray;
                
                // Single DB connection for both updates
                var connection = KaruahChessDB.GetDBConnection();
                using (var command = connection.CreateCommand())
                {
                    // Update current record (board/state + cleared SAN)
                    command.CommandText = $"Update {KaruahChessDB.GameRecordTableName} set BoardSquareStr=@BoardSquareStr, GameStateStr=@GameStateStr, MoveSANStr=@MoveSANStr where id=@Id;";
                    command.Parameters.Add(new SqliteParameter("@Id", pGameRecordArray.Id));
                    command.Parameters.Add(new SqliteParameter("@BoardSquareStr", boardSquareStr));
                    command.Parameters.Add(new SqliteParameter("@GameStateStr", gameStateStr));
                    command.Parameters.Add(new SqliteParameter("@MoveSANStr", string.Empty));
                    result = command.ExecuteNonQuery();
                    transactionId++;                    
                }
                connection.Close();

                // Update the current game if updating max record
                var maxId = GetMaxId();
                if (pGameRecordArray.Id == maxId)
                {
                    CurrentGame.SetBoardArray(pGameRecordArray.BoardArray);
                    CurrentGame.SetStateArray(pGameRecordArray.StateArray);
                }

            }


            return result;
        }

        /// <summary>
        /// Clears game record
        /// </summary>
        public void Reset(int pWhiteClockOffset, int pBlackClockOffset) {

            // Clear records
           using (var connection = KaruahChessDB.GetDBConnection()) {   
                using (var command = connection.CreateCommand())
                {
                    command.CommandText = $"Delete from {KaruahChessDB.GameRecordTableName};";                    
                    command.ExecuteNonQuery();
                    transactionId++;
                }
                connection.Close();
            }
           
            // Clear dictionary
            _gameRecordDict.Clear();

            // Reset the current game
            CurrentGame.Reset();

            // Create record of default setup
            RecordGameState(pWhiteClockOffset, pBlackClockOffset, string.Empty);
        }

        /// <summary>
        /// Clear start
        /// </summary>
        /// <param name="pId"></param>
        private void ClearFrom(int pId)
        {
            using (var connection = KaruahChessDB.GetDBConnection())
            {
                // Remove from database                
                using (var command = connection.CreateCommand())
                {
                    command.CommandText = $"Delete from {KaruahChessDB.GameRecordTableName} where id >= @Id;";
                    command.Parameters.Add(new SqliteParameter("Id", pId));                    
                    command.ExecuteNonQuery();
                    transactionId++;
                }

                connection.Close();
            }

            // Remove from dictionary
            foreach (var key in _gameRecordDict.Keys.ToList())
            {
                if (key >= pId) _gameRecordDict.Remove(key);
            }
                       
        }

        

        /// <summary>
        /// Undo last move
        /// </summary>
        /// <param name="pBoardSquareDS"></param>
        /// <returns></returns>
        public bool Undo()
        {
            var lastMoveId = GetMaxId();
            
            GameRecordArray previousBoard = Get(lastMoveId - 1);
            bool returnValue = false;

            if (previousBoard != null)
            { 
                // Remove record from database
                ClearFrom(lastMoveId);

                // Set the current game to a previous board
                CurrentGame.SetBoardArray(previousBoard.BoardArray);
                CurrentGame.SetStateArray(previousBoard.StateArray);

                // Return true
                returnValue = true;
            }


            return returnValue;
        }

       
        /// <summary>
        /// Compares two boards and returns sq ids that have changed
        /// </summary>
        /// <returns></returns>
        public HashSet<int> GetBoardSquareChanges(GameRecordArray pBoardA, GameRecordArray pBoardB)
        {
            HashSet<int> changedIndexes = new HashSet<int>();                       

            //Loop through bit boards and detectchanges
            if (pBoardA != null && pBoardB != null) {
                _tempBoardA.SetBoardArray(pBoardA.BoardArray);
                _tempBoardA.SetStateArray(pBoardA.StateArray);
                _tempBoardB.SetBoardArray(pBoardB.BoardArray);
                _tempBoardB.SetStateArray(pBoardB.StateArray);

                for (int i = 0; i < 64; i++)
                {
                    int sqAspin = _tempBoardA.GetSpin(i);
                    int sqBspin = _tempBoardB.GetSpin(i);
                    if(sqAspin != sqBspin) changedIndexes.Add(i);

                }
            }
            return changedIndexes;
        }

        
        /// <summary>
        /// Gets last id in the table
        /// </summary>
        /// <returns></returns>
        public int GetMaxId()
        {
            int maxId = 0;

            using (var connection = KaruahChessDB.GetDBConnection())
            {   
                // Remove from database                
                using (var command = connection.CreateCommand())
                {
                    command.CommandText = $"select max(Id) from {KaruahChessDB.GameRecordTableName};";                    
                    Object result = command.ExecuteScalar();
                    if (result != null && result != DBNull.Value) maxId = Convert.ToInt32(result);

                }

                connection.Close();            
            }

            return maxId;

        }

        /// <summary>
        /// Generates a new transacation id
        /// </summary>
        public void newTransaction()
        {
            transactionId++;
        }

        /// <summary>
        /// Gets all record ids
        /// </summary>
        /// <returns></returns>
        public List<int> GetAllRecordIDList()
        {

            List<int> recordIDList = new List<int>();            

            // Get list of Ids from the database
            var connection = KaruahChessDB.GetDBConnection();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = $"select id from {KaruahChessDB.GameRecordTableName} order by id";
                SqliteDataReader reader = command.ExecuteReader();

                while (reader.Read())
                {
                    recordIDList.Add(Convert.ToInt32(reader["Id"]));
                }

            }

            connection.Close();

            return recordIDList;

        }

        /// <summary>
        /// Returns a count of the records in the dictionary
        /// </summary>
        public int RecordCount() {
            return _gameRecordDict.Count();
        }

        /// <summary>
        /// Gets the game history
        /// </summary>        
        public SortedList<int, GameRecordArray> GameHistory()
        {            
            SortedList<int, GameRecordArray> history = new SortedList<int, GameRecordArray>();

            foreach (var kvp in _gameRecordDict)
            {
                history.Add(kvp.Key, kvp.Value);
            }

            return history;
        }


        /// <summary>
        /// Determines the active move color for a specific record
        /// </summary>  
        public int GetActiveMoveColour(int pId)
        {
            if (_gameRecordDict.ContainsKey(pId))
            {
                GameRecordArray rec = _gameRecordDict[pId];
                return rec.StateArray[0];
            }
            else
            {
                return 0;
            }           
        }



        /// <summary>
        /// Get the game status for a specific record
        /// </summary>  
        public int GetStateGameStatus(int pId)
        {
            if (_gameRecordDict.ContainsKey(pId))
            {
                GameRecordArray rec = _gameRecordDict[pId];
                return rec.StateArray[5];
            }
            else
            {
                return -1;
            }
        }

    }
}

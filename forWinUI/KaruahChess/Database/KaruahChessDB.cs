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

using System;
using System.IO;
using System.Threading.Tasks;
using Microsoft.Data.Sqlite;


namespace KaruahChess.Database
{
    public static class KaruahChessDB
    {        
        public static readonly string dbname = "KaruahChessV15.sqlite";
        public static readonly string dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, dbname);
        public static readonly string connectionString = "Filename=" + dbpath + ";";

        public static readonly int DB_OK = 0;
        public static readonly int ERROR_DB_FILEMISSING = 200;
        public static readonly int ERROR_DB_READWRITE = 201;
        public static readonly int ERROR_DB_TABLENOTEXISTS = 202;

        public static int instanceID { get; private set; }
        public static string ParameterTableName { get; private set; }
        public static string GameRecordTableName { get; private set; }

        public static int Init(int pInstanceID)
        {
            instanceID = pInstanceID;

            if (instanceID > 0) { 
                ParameterTableName = $"Parameter_{instanceID}";
                GameRecordTableName = $"GameRecord_{instanceID}";
            }
            else
            {
                ParameterTableName = "Parameter";
                GameRecordTableName = "GameRecord";
            }


            int dbStatus = CheckDB();

            if (dbStatus != DB_OK)
            {
                if (dbStatus == ERROR_DB_FILEMISSING)
                {
                    CreateFileIfNotExists();
                    CreateTablesIfNotExists();
                }
                else if (dbStatus == ERROR_DB_TABLENOTEXISTS)
                {
                    CreateTablesIfNotExists();
                }

                // Recheck DB
                return CheckDB();
            }
            else
            {
                return dbStatus;
            }

            
        }

        /// <summary>
        /// Creates the database file if it does not exist
        /// </summary>
        /// <returns></returns>
        private static void CreateFileIfNotExists()
        {
            var dbFileTask = Task.Run(async () => await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(dbname, Windows.Storage.CreationCollisionOption.OpenIfExists));
            dbFileTask.Wait();
        }

        /// <summary>
        /// Creates the database file if it does not exist
        /// </summary>
        /// <returns></returns>
        private static void CreateTablesIfNotExists()
        {
            ExecuteNonQuery($"CREATE TABLE IF NOT EXISTS {ParameterTableName} (Name TEXT PRIMARY KEY NOT NULL, Value BLOB NOT NULL);");
            ExecuteNonQuery($"CREATE TABLE IF NOT EXISTS {GameRecordTableName} (Id INTEGER PRIMARY KEY NOT NULL, BoardSquareStr TEXT NOT NULL, GameStateStr TEXT NOT NULL, MoveSANStr TEXT);");
        }


        /// <summary>
        /// Check that the DB exists and is operational
        /// </summary>        
        private static int CheckDB()
        {
            var dbFileTask = Task.Run(async () => await Windows.Storage.ApplicationData.Current.LocalFolder.TryGetItemAsync(dbname));
            
            dbFileTask.Wait();
            if (dbFileTask.Result != null)
            {
                // Check if all tables exist
                int tableCount = ExecuteScalarInt($"select count(name) from sqlite_schema where type='table' and (name='{GameRecordTableName}' or name = '{ParameterTableName}');");
                if (tableCount != 2)
                {
                    return ERROR_DB_TABLENOTEXISTS;
                }

                // Read and write test
                try
                {
                    // Read test
                    ExecuteNonQuery($"select Id from {GameRecordTableName} limit 1;");
                    ExecuteNonQuery($"select Name from {ParameterTableName} limit 1;");

                    // Write test
                    ExecuteNonQuery("pragma user_version = 0;"); 
                }
                catch (Exception ex)
                {
                    return ERROR_DB_READWRITE;

                }

                return DB_OK;
            }
            else
            {
                return ERROR_DB_FILEMISSING;
            }
            
        }

        
        /// <summary>
        /// Gets a database connection 
        /// </summary>
        /// <returns>A database connection</returns>
        public static SqliteConnection GetDBConnection()
        {
            var conn = new SqliteConnection(connectionString);
            conn.Open();

            return conn;      

        }
        
        

        /// <summary>
        /// Execute SQL helper
        /// </summary>
        /// <param name="pQuery"></param>
        private static void ExecuteNonQuery(string pQuery)
        {
            using var connection = GetDBConnection();            
            using (var command = connection.CreateCommand())
            {
                command.CommandText = pQuery;

                try
                {
                    command.ExecuteNonQuery();
                }
                finally
                {
                    connection.Close();
                }
            }
        }


        /// <summary>
        /// Execute SQL helper
        /// </summary>
        /// <param name="pQuery"></param>
        private static int ExecuteScalarInt(string pQuery)
        {
            int rtnValue = 0;
            using var connection = GetDBConnection();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = pQuery;

                try
                {                    
                    rtnValue =  Convert.ToInt32(command.ExecuteScalar());
                }
                finally
                {
                    connection.Close();
                }
            }


            return rtnValue;
        }
    }
}

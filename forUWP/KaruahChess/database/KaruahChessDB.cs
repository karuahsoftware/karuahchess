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

using System;
using System.IO;
using Microsoft.Data.Sqlite;



namespace KaruahChess.Database
{
    public static class KaruahChessDB
    {
        public static readonly string dbname = "KaruahChessV9.sqlite";
        public static readonly string dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, dbname);
        public static readonly string connectionString = "Filename=" + dbpath + ";";


        /// <summary>
        /// Creates the database if it does not exist
        /// </summary>
        /// <returns></returns>
        public async static void CreateIfNotExists()
        {
            await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(dbname, Windows.Storage.CreationCollisionOption.OpenIfExists);
            Execute(@"CREATE TABLE IF NOT EXISTS Parameter (Name STRING PRIMARY KEY NOT NULL, Value BLOB NOT NULL);");
            Execute(@"CREATE TABLE IF NOT EXISTS GameRecord (Id INTEGER PRIMARY KEY NOT NULL, BoardSquareStr STRING NOT NULL, GameStateStr STRING NOT NULL);");

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
        private static void Execute(string pQuery)
        {
            var connection = GetDBConnection();
            using (var command = connection.CreateCommand())
            {
                command.CommandText = pQuery;                
                command.ExecuteNonQuery();
            }
            connection.Close();
        }
    }
}

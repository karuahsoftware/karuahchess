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

import SwiftUI
import SQLite3

class KaruahChessDB {
    private static let dbname = "KaruahChessV5.sqlite"
    
    
    // Create the database if it does not exist
    static func createIfNotExists() {
        execute(pQuery: "CREATE TABLE IF NOT EXISTS Parameter (Name STRING PRIMARY KEY NOT NULL, Value BLOB NOT NULL);")
        execute(pQuery: "CREATE TABLE IF NOT EXISTS GameRecord (Id INTEGER PRIMARY KEY NOT NULL, BoardSquareStr STRING NOT NULL, GameStateStr STRING NOT NULL);")
    }
    
    // Get a database connection
    static func openDBConnection() -> OpaquePointer?
    {
        let fileURL = try! FileManager.default
        .url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            .appendingPathComponent(KaruahChessDB.dbname)
        
        var db: OpaquePointer?
        if sqlite3_open(fileURL.path, &db) != SQLITE_OK {
            sqlite3_close(db)
            db = nil
        }
        
        return db
    }
    
    
    // Execute SQL helper
    private static func execute(pQuery: String) {
        let db = KaruahChessDB.openDBConnection()
        
        if db != nil {
            sqlite3_exec(db, pQuery,nil,nil,nil)
            sqlite3_close(db)
        }
    }
    
    
}

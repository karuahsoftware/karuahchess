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

import SQLite3

@MainActor
class ImportDB {
    enum ImportTypeEnum {case GameXML}
    
    func importData(_ pFile: Data, _ pImportType: ImportTypeEnum) -> Int {
        var result = 0
        
        if pImportType == ImportTypeEnum.GameXML {
            let uncompressedData: Data = pFile
            
            let gameRecordDelegate = GameRecordParser()
            let parser = XMLParser(data: uncompressedData)
            parser.delegate = gameRecordDelegate
            parser.parse()
            let gameRecList = gameRecordDelegate.getResult()
            
            // Check for parser error
            if gameRecordDelegate.errorOccurred {
                return -1
            }
            
            if gameRecList.count > 0 {
                var cursor : OpaquePointer?
                let db = KaruahChessDB.openDBConnection()
                
                do {
                    // Remove all records
                    let sqlStr = "delete from GameRecord"
                    if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                        sqlite3_step(cursor)
                    }
                }
                
                do {
                    // Insert records in to the database
                    for gameRecord in gameRecList {
                        let sqlStr = "insert into GameRecord(Id, BoardSquareStr, GameStateStr, MoveSANStr) values (?,?,?,?)"
                        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                            sqlite3_bind_int(cursor, 1, Int32(gameRecord.id))
                            sqlite3_bind_text(cursor, 2, (gameRecord.boardSquareStr as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                            sqlite3_bind_text(cursor, 3, (gameRecord.gameStateStr as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                            sqlite3_bind_text(cursor, 4, (gameRecord.moveSANStr as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                            if sqlite3_step(cursor) == SQLITE_DONE {
                                result += Int(sqlite3_changes(db))
                            }
                        }
                    }
                }
                
                // Finalise and close
                sqlite3_finalize(cursor)
                sqlite3_close(db)
                
                // Reload all records
                GameRecordDataService.instance.load()
            }
            
            
        }
        
        
        return result
    }
    
}

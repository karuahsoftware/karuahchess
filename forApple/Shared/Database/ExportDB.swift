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
import AppleArchive

class ExportDB {
    enum ExportTypeEnum {case GameXML}
    
    func export(pExportType: ExportTypeEnum) -> Data? {
        
        if pExportType == ExportTypeEnum.GameXML {
            var xmlData = Data()
            
            // Start
            xmlData.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n".data(using: .utf8)!)
            xmlData.append("<Records>\n".data(using: .utf8)!)
            
            // Data
            var cursor : OpaquePointer?
            let db = KaruahChessDB.openDBConnection()
            
            let sqlStr = "select Id, BoardSquareStr, GameStateStr from GameRecord order by id"
            if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                while sqlite3_step(cursor) == SQLITE_ROW {
                 
                    if let idXml = ("    <Id>" + String(sqlite3_column_int(cursor, 0)) + "</Id>\n").data(using: .utf8),
                       let boardSquareStrXml = ("    <BoardSquareStr>" + String(cString: sqlite3_column_text(cursor, 1)) + "</BoardSquareStr>\n").data(using: .utf8),
                       let GameStateStrXml = ("    <GameStateStr>" + String(cString: sqlite3_column_text(cursor, 2)) + "</GameStateStr>\n").data(using: .utf8)
                    {
                        xmlData.append("  <GameRecord>\n".data(using: .utf8)!)
                        xmlData.append(idXml)
                        xmlData.append(boardSquareStrXml)
                        xmlData.append(GameStateStrXml)
                        xmlData.append("  </GameRecord>\n".data(using: .utf8)!)
                    }
                }
            }
            
            // Finalise and close
            sqlite3_finalize(cursor)
            sqlite3_close(db)
            
            // End tag
            xmlData.append("</Records>\n".data(using: .utf8)!)
            
            // TODO: Compress data
            
            
            return xmlData
        }
        else {
            return nil
        }
        
    }
}

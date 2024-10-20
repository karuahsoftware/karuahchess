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

class ParameterDataService : ParameterDataServiceProtocol {
    private var parameters: [String: ParamProtocol]
    
    static let instance = ParameterDataService()
  
    /// Constructor
    private init() {
        parameters = [String: ParamProtocol]()
        load()
    }
    
    
    /// Loads parameters
    private func load() {
        parameters.removeAll()
        
        // Load game records in to memory
        var cursor : OpaquePointer?
        let db = KaruahChessDB.openDBConnection()

        let sqlStr = "select Name, Value from Parameter"
        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
           
            while sqlite3_step(cursor) == SQLITE_ROW {
                let name : String =  String(cString: sqlite3_column_text(cursor, 0))
                
                if let dataBlob = sqlite3_column_blob(cursor, 1) {
                    let dataBlobLength = sqlite3_column_bytes(cursor, 1)
                    let data = Data(bytes: dataBlob, count: Int(dataBlobLength))
                    
                    switch name {
                        case String(describing: ParamArrangeBoard.self):
                            if let obj = try? JSONDecoder().decode(ParamArrangeBoard.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamBoardCoord.self):
                            if let obj = try? JSONDecoder().decode(ParamBoardCoord.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamComputerMoveFirst.self):
                            if let obj = try? JSONDecoder().decode(ParamComputerMoveFirst.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamComputerPlayer.self):
                            if let obj = try? JSONDecoder().decode(ParamComputerPlayer.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamColourDarkSquares.self):
                            if let obj = try? JSONDecoder().decode(ParamColourDarkSquares.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamHint.self):
                            if let obj = try? JSONDecoder().decode(ParamHint.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamHintMove.self):
                            if let obj = try? JSONDecoder().decode(ParamHintMove.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamLevelAuto.self):
                            if let obj = try? JSONDecoder().decode(ParamLevelAuto.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamLimitAdvanced.self):
                            if let obj = try? JSONDecoder().decode(ParamLimitAdvanced.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamLimitDepth.self):
                            if let obj = try? JSONDecoder().decode(ParamLimitDepth.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamLimitSkillLevel.self):
                            if let obj = try? JSONDecoder().decode(ParamLimitSkillLevel.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamLimitMoveDuration.self):
                            if let obj = try? JSONDecoder().decode(ParamLimitMoveDuration.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamLimitThreads.self):
                            if let obj = try? JSONDecoder().decode(ParamLimitThreads.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamMoveHighlight.self):
                            if let obj = try? JSONDecoder().decode(ParamMoveHighlight.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamMoveSpeed.self):
                            if let obj = try? JSONDecoder().decode(ParamMoveSpeed.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamMoveText.self):
                            if let obj = try? JSONDecoder().decode(ParamMoveText.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamNavigator.self):
                            if let obj = try? JSONDecoder().decode(ParamNavigator.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamPromoteAuto.self):
                            if let obj = try? JSONDecoder().decode(ParamPromoteAuto.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamRandomiseFirstMove.self):
                            if let obj = try? JSONDecoder().decode(ParamRandomiseFirstMove.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamRotateBoard.self):
                            if let obj = try? JSONDecoder().decode(ParamRotateBoard.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamSoundEffect.self):
                            if let obj = try? JSONDecoder().decode(ParamSoundEffect.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamSoundRead.self):
                            if let obj = try? JSONDecoder().decode(ParamSoundRead.self, from: data) {
                                parameters[name] = obj
                            }
                        case String(describing: ParamVoiceCommand.self):
                            if let obj = try? JSONDecoder().decode(ParamVoiceCommand.self, from: data) {
                            parameters[name] = obj
                            }
                        default:
                            break
                        }
                    
                }
            }
        }
        
        // Finalise and close
        sqlite3_finalize(cursor)
        sqlite3_close(db)
        
        
    }
    
    
    /// Gets a parameter
    /// - Parameter pParameterClass: The type of the class to get
    /// - Returns: An instance of a class
    func get<T: ParamProtocol>(pParameterClass: T.Type) -> T {
        if let paramObj = parameters[String(describing: pParameterClass.self)] {
            return paramObj as! T
        }
        else {
            let newObj: T = T()
            _ = set(pObj: newObj)
            return newObj
        }
    }
    
    
    /// Sets a parameter
    /// - Parameter pObj: The parameter object to set
    /// - Returns: Number of records updated
    func set<T: ParamProtocol>(pObj: T) -> Int {
        if let paramValue = try? JSONEncoder().encode(pObj) {
           
            let param = Parameter(pName: String(describing: type(of: pObj)), pValue: paramValue)
            return updateOrAdd(pParameter: param, pObj: pObj, pReload: false)
        }
        
        return 0
    }
    
    
    /// Updates the record if it exists. Otherwise adds a new record
    /// - Parameters:
    ///   - pParameter: The serialised parameter to write to the database
    ///   - pObj: The object being updated
    ///   - pReload: Reload all instances from the DB
    /// - Returns: Number of records updated
    private func updateOrAdd<T: ParamProtocol>(pParameter : Parameter?, pObj : T, pReload : Bool) -> Int {
        var result: Int = 0
        
        if let param = pParameter {
            // Update record in database
            var cursor : OpaquePointer?
            let db = KaruahChessDB.openDBConnection()
            
            let sqlStr = "update Parameter set Value = ? where Name = ?"
            if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                _ = param.value.withUnsafeBytes({ bufferPointer -> Int32 in
                    sqlite3_bind_blob(cursor, 1, bufferPointer.baseAddress, Int32(param.value.count), Constants.SQLITE_TRANSIENT)
                })
                sqlite3_bind_text(cursor, 2, (param.name as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                if sqlite3_step(cursor) == SQLITE_DONE {
                    result = Int(sqlite3_changes(db))
                }
            }
            
            // If there was no update then insert a new record
            if result == 0 {
                let sqlStr = "insert into Parameter (Name, Value) Values (?, ?)"
                if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                    sqlite3_bind_text(cursor, 1, (param.name as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                    _ = param.value.withUnsafeBytes({ bufferPointer -> Int32 in
                        sqlite3_bind_blob(cursor, 2, bufferPointer.baseAddress, Int32(param.value.count), Constants.SQLITE_TRANSIENT)
                    })
                    
                    if sqlite3_step(cursor) == SQLITE_DONE {
                        result = Int(sqlite3_changes(db))
                    }
                }
            }
            
            // Finalise and close
            sqlite3_finalize(cursor)
            sqlite3_close(db)
        
            if result > 0 {
                if pReload {
                    // Reload all from DB
                    load()
                }
                else {
                    // Just update the affected list item
                    parameters[param.name] = pObj
                }
            }
            
        }
        
        return result
    }
    
    
}

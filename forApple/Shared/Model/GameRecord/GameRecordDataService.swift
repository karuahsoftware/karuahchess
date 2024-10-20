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

class GameRecordDataService : GameRecordDataServiceProtocol {
    
    private var gameRecordDict: [Int: GameRecordArray]
    private(set) var currentGame: KaruahChessEngineC
    private var tempBoardA: KaruahChessEngineC
    private var tempBoardB: KaruahChessEngineC
    private var tempBoardC: KaruahChessEngineC
    
    static let instance = GameRecordDataService()
    
    /// Constructor
    init() {
        gameRecordDict = [Int: GameRecordArray]()
        currentGame = KaruahChessEngineC()
        tempBoardA = KaruahChessEngineC()
        tempBoardB = KaruahChessEngineC()
        tempBoardC = KaruahChessEngineC()
        
        load()
    }
    
    
    /// Loads game record
    func load() {
        let board = KaruahChessEngineC()
        
        // Clear records from memory
        gameRecordDict.removeAll()
        
        // Load game records in to memory
        var cursor : OpaquePointer?
        let db = KaruahChessDB.openDBConnection()
        
        let sqlStr = "select Id, BoardSquareStr, GameStateStr from GameRecord order by id"
        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
            while sqlite3_step(cursor) == SQLITE_ROW {
                let id : Int = Int(sqlite3_column_int(cursor, 0))
                let boardSquareStr : String =  String(cString: sqlite3_column_text(cursor, 1))
                let gameStateStr : String = String(cString: sqlite3_column_text(cursor, 2))
                board.setBoard(boardSquareStr)
                board.setState(gameStateStr)
                
                let recArray = GameRecordArray()
                recArray.id = id
                
                recArray.boardArray = board.getBoardArraySafe()
                recArray.stateArray = board.getStateArraySafe()
                
                gameRecordDict[recArray.id] = recArray
                
            }
        }
        
        // Finalise and close
        sqlite3_finalize(cursor)
        sqlite3_close(db)
        
        // If no records were loaded then create a default record
        if gameRecordDict.count == 0 {
            reset()
        }
        
        // Set current game to latest bitboard
        if let latestRecord = get() {
            currentGame.setBoardArray(latestRecord.boardArray)
            currentGame.setStateArray(latestRecord.stateArray)
        }
        
    }
    
    /// Gets the current game as an array
    /// - Returns: Game record array
    func getCurrentGame() -> GameRecordArray {
        let recArray = GameRecordArray()
        recArray.id = -1
        recArray.boardArray = currentGame.getBoardArraySafe()
        recArray.stateArray = currentGame.getStateArraySafe()
        return recArray
    }
    
    
    /// Gets a record set by Id
    /// - Parameter pId: Id of record to get
    /// - Returns: A record array
    func get(pId: Int) -> GameRecordArray? {
        if let rec = gameRecordDict[pId] {
            return rec
        }
        
        return nil
    }
    
    
    /// Gets latest game record
    /// - Returns: Latest game record
    func get() -> GameRecordArray? {
        let latestId = getMaxId()
        return get(pId: latestId)
    }
    
    
    /// Records the current state of the game as a record
    /// - Parameters:
    ///   - pWhiteClockOffset: White clock offset
    ///   - pBlackClockOffset: Black clock offset
    /// - Returns: Number of records inserted - should be 1
    func recordGameState(pWhiteClockOffset: Int, pBlackClockOffset: Int) -> Int {
        var result: Int = 0
        
        // Set the clocks
        currentGame.setStateWhiteClockOffset(Int32(pWhiteClockOffset))
        currentGame.setStateBlackClockOffset(Int32(pBlackClockOffset))
        
        // Create game record
        let nextId = getMaxId() + 1
        let boardSquareStr: NSString = currentGame.getBoard() as NSString
        let gameStateStr: NSString = currentGame.getState() as NSString
        
        let gameRecordArray = GameRecordArray()
        gameRecordArray.id = nextId
        gameRecordArray.boardArray = currentGame.getBoardArraySafe()
        gameRecordArray.stateArray = currentGame.getStateArraySafe()
        
        if(gameRecordDict[gameRecordArray.id] == nil) {
            // Add record to dictionary
            gameRecordDict[gameRecordArray.id] = gameRecordArray
            
            // Add record to database
            var cursor : OpaquePointer?
            let db = KaruahChessDB.openDBConnection()
            
            let sqlStr = "insert into GameRecord(Id, BoardSquareStr, GameStateStr) values (?,?,?)"
            if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                sqlite3_bind_int(cursor, 1, Int32(nextId))
                sqlite3_bind_text(cursor, 2, boardSquareStr.utf8String, -1, Constants.SQLITE_TRANSIENT)
                sqlite3_bind_text(cursor, 3, gameStateStr.utf8String, -1, Constants.SQLITE_TRANSIENT)
                if sqlite3_step(cursor) == SQLITE_DONE {
                    result = Int(sqlite3_changes(db))
                }
            }
            
            // Finalise and close
            sqlite3_finalize(cursor)
            sqlite3_close(db)
            
        }
        
        return result
    }
    
    
    /// Updates a board record
    /// - Parameter pGameRecord: A game record
    /// - Returns: Records updated - should be one
    func updateGameState(pGameRecordArray: GameRecordArray) -> Int {
        var result: Int = 0
        
        if(gameRecordDict[pGameRecordArray.id] != nil) {
            tempBoardC.setBoardArray(pGameRecordArray.boardArray)
            tempBoardC.setStateArray(pGameRecordArray.stateArray)
            let boardSquareStr: NSString = tempBoardC.getBoard() as NSString
            let gameStateStr: NSString = tempBoardC.getState() as NSString
            
            // Update record in dictionary
            gameRecordDict[pGameRecordArray.id] = pGameRecordArray
            
            // Update record in database
            var cursor : OpaquePointer?
            let db = KaruahChessDB.openDBConnection()
            
            let sqlStr = "update GameRecord set BoardSquareStr = ?, GameStateStr = ? where Id = ?"
            if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                sqlite3_bind_text(cursor, 1, boardSquareStr.utf8String, -1, Constants.SQLITE_TRANSIENT)
                sqlite3_bind_text(cursor, 2, gameStateStr.utf8String, -1, Constants.SQLITE_TRANSIENT)
                sqlite3_bind_int(cursor, 3, Int32(pGameRecordArray.id))
                if sqlite3_step(cursor) == SQLITE_DONE {
                    result = Int(sqlite3_changes(db))
                }
            }
            
            // Finalise and close
            sqlite3_finalize(cursor)
            sqlite3_close(db)
            
            // Update the current game if updating max record
            let maxId = getMaxId()
            if(pGameRecordArray.id == maxId) {
                currentGame.setBoardArray(pGameRecordArray.boardArray)
                currentGame.setStateArray(pGameRecordArray.stateArray)
            }
        }
        
        return result
    }
    
     
    
    /// Clears game record
    func reset() {
        var cursor : OpaquePointer?
        let db = KaruahChessDB.openDBConnection()
        
        let sqlStr = "delete from GameRecord"
        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
            sqlite3_step(cursor)
        }
        
        // Finalise and close
        sqlite3_finalize(cursor)
        sqlite3_close(db)
        
        // Clear the dictionary
        gameRecordDict.removeAll()
        
        // Reset the current game
        currentGame.reset()
        
        // Create record of default setup
        _ = recordGameState(pWhiteClockOffset: 0, pBlackClockOffset: 0)
        
        
    }
    
    
    /// Clear from a given PId
    /// - Parameter pId: PId to clear from
    private func clearFrom(pId: Int) {
        var cursor : OpaquePointer?
        let db = KaruahChessDB.openDBConnection()
        
        let sqlStr = "delete from GameRecord where Id >= ?"
        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
            sqlite3_bind_int(cursor, 1, Int32(pId))
            sqlite3_step(cursor)
        }
        
        // Finalise and close
        sqlite3_finalize(cursor)
        sqlite3_close(db)
        
        // Remove from dictionary
        for key in Array(gameRecordDict.keys) {
            if key >= pId {
                gameRecordDict[key] = nil
            }
        }
    }
    
    
    /// Undo last move
    /// - Returns: Success
    func undo() -> Bool {
        let lastMoveId = getMaxId()
        var returnValue = false
        
        if let previousBoard = get(pId: lastMoveId - 1) {
            // Remove record from database
            clearFrom(pId: lastMoveId)
            
            // Set the current game to a previous board
            currentGame.setBoardArray(previousBoard.boardArray)
            currentGame.setStateArray(previousBoard.stateArray)
            
            // Return true
            returnValue = true
        }
        
        return returnValue
    }
    
    
    /// Compare two boards and returns sq ids that have changed
    /// - Parameters:
    ///   - pBoardA: Board A
    ///   - pBoardB: Board B
    /// - Returns: Squares that have changed as a long binary
    func getBoardSquareChanges(pBoardA: GameRecordArray?, pBoardB: GameRecordArray?) -> UInt64 {
        var mask: UInt64 = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000
        var changedIndexes: UInt64 = 0
        
        if let boardA = pBoardA, let boardB = pBoardB {
            tempBoardA.setBoardArray(boardA.boardArray)
            tempBoardA.setStateArray(boardA.stateArray)
            tempBoardB.setBoardArray(boardB.boardArray)
            tempBoardB.setStateArray(boardB.stateArray)
        
            for i: Int32 in 0...63 {
                let sqAspin = tempBoardA.getSpin(i)
                let sqBspin = tempBoardB.getSpin(i)
                if sqAspin != sqBspin {
                    changedIndexes = changedIndexes | mask
                }
                mask = mask >> 1
            }
        }
        
        return changedIndexes
        
    }
    
    
    
    /// Gets last id in the table
    /// - Returns: last id
    func getMaxId() -> Int {
        var maxId: Int = 0
        
        var cursor : OpaquePointer?
        let db = KaruahChessDB.openDBConnection()
        
        let sqlStr = "select max(Id) from GameRecord"
        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
            if sqlite3_step(cursor) == SQLITE_ROW {
                maxId = Int(sqlite3_column_int(cursor,0))
            }
        }
        
        // Finalise and close
        sqlite3_finalize(cursor)
        sqlite3_close(db)
        
        return maxId
    }
    
    
    /// Get all record Ids
    /// - Returns: A array of record Ids
    func getAllRecordIDList() -> [Int] {
        var recordIDList = [Int]()
        
        // Load game records in to memory
        var cursor : OpaquePointer?
        let db = KaruahChessDB.openDBConnection()
        
        let sqlStr = "select Id from GameRecord order by id"
        if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
            while sqlite3_step(cursor) == SQLITE_ROW {
                let id : Int = Int(sqlite3_column_int(cursor, 0))
                recordIDList.append(id)
            }
        }
        
        // Finalise and close
        sqlite3_finalize(cursor)
        sqlite3_close(db)
        
        return recordIDList
        
    }
 
    /// Gets the game history
    /// - Returns: A array of record Ids
    func gameHistory() -> [Int: GameRecordArray] {
        return gameRecordDict
    }
    
}

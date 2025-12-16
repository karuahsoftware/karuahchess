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
class ImportPGNProcessor {
    private var board: KaruahChessEngineC
    
    /// Constructor
    init() {
        board = KaruahChessEngineC()
    }
    
    
    func importData(_ pPGNText: String) -> (success: Bool, message: String) {
        // Remove header comments
        var pgnGameFilterA = pPGNText
        pgnGameFilterA = ImportPGNProcessor.regexReplace(pattern: "(?s)\\[.*?\\]\\s*", in: pgnGameFilterA, with: "")
        
        // Remove any curly bracket comments in game string
        pgnGameFilterA = ImportPGNProcessor.regexReplace(pattern: "(?s)\\{.*?\\}", in: pgnGameFilterA, with: " ")
        
        // Remove line breaks
        let lines = pgnGameFilterA.components(separatedBy: CharacterSet.newlines)
        var pgnGameFilterB = ""
        for line in lines {
            // Remove semicolon comments
            pgnGameFilterB += ImportPGNProcessor.regexReplace(pattern: ";(.*)?$", in: line, with: "") + " "
        }
        
        // Remove Numeric Annotation Glyphs
        pgnGameFilterB = ImportPGNProcessor.regexReplace(pattern: "(\\$\\d+)|[!?]+", in: pgnGameFilterB, with: "")
        
        let pgnGame = pgnGameFilterB
                
        let result = processGame(pgnGame)
        
        return result
    }
    
    ///Processes a PGN game string
    private func processGame(_ original: String) -> (success: Bool, message: String) {
        var pPGNGameStr = original.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Remove the score from the end of the PGN string
        pPGNGameStr = ImportPGNProcessor.regexReplace(pattern: "[10][-][10]$", in: pPGNGameStr, with: "")
        pPGNGameStr = ImportPGNProcessor.regexReplace(pattern: "[1][/][2][-][1][/][2]$",in: pPGNGameStr, with: "")
        
        // Replace multi spaces
        pPGNGameStr = ImportPGNProcessor.regexReplace(pattern: "[ ]{2,}", in: pPGNGameStr, with: " " ).trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Capture move numbers as tokens too (group keeps delimiters in the result array)
        // Tokens will look like: "", "1.", "e4 e5", "2.", "Nf3", "2...", "Nc6", ...
        let moveTokens = ImportPGNProcessor.regexSplit(pattern: "([0-9]{1,3}(?:\\.{3}|\\.))", in: pPGNGameStr)
        
        board.reset()
        
        // Determine starting colour: if first move number is written with '...'
        // (e.g. "1...e5") then Black is to move; otherwise White to move.
        if ImportPGNProcessor.regexIsMatch(pattern: "^\\s*[0-9]{1,3}\\.\\.\\.", in: pPGNGameStr) {
            board.setStateActiveColour(Int32(Constants.BLACKPIECE))
        } else {
            board.setStateActiveColour(Int32(Constants.WHITEPIECE))
        }
        
        // Loop through game moves
        var id = 1
        var currentMoveNumberToken = ""
        
        var gameRecList: [GameRecordArray] = []
        
        // Add start record
        let startRecord = GameRecordArray(pId: id, pBoardArray: board.getBoardArraySafe(), pStateArray: board.getStateArraySafe(), pMoveSAN: "")
        gameRecList.append(startRecord)
        id += 1
        
        // Iterate over tokens (move numbers + move groups)
        do {
            for tokenRaw in moveTokens {
                guard !tokenRaw.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                    continue
                }
                let token = tokenRaw.trimmingCharacters(in: .whitespacesAndNewlines)
                
                // If this token IS a move number, store it and continue to next token
                if ImportPGNProcessor.regexIsMatch(pattern: "^[0-9]{1,3}(?:\\.\\.\\.|\\.)$", in: token) {
                    currentMoveNumberToken = token
                    continue
                }
                
                // Otherwise this token holds one or more SAN half-moves separated by spaces
                let halfMoves = token
                    .split(separator: " ")
                    .map { String($0) }
                    .filter { !$0.isEmpty }
                
                for halfMove in halfMoves {
                    if halfMove.isEmpty { continue }
                    let success = try movePGN(halfMove, board: board, commit: true)
                    if !success {
                        return (false, "Import failed. Error occurred at move \(currentMoveNumberToken) with movetext \(halfMove)")
                    }
                    
                    let gameRec = GameRecordArray(pId: id, pBoardArray: board.getBoardArraySafe(), pStateArray: board.getStateArraySafe(), pMoveSAN: halfMove)
                    
                    gameRecList.append(gameRec)
                    id += 1
                }
            }
            
            if gameRecList.count <= 1 {
                return (false, "Import failed - nothing to import.")
            }
            
            // Load the game into DB
            _ = loadGameIntoDatabase(pGameRecordList: gameRecList)
        } catch let error {
            return (false, error.localizedDescription)
        }
        
        return (true, "")
    }
    
    /// Moves a piece according to the PGN value
    private func movePGN(_ pPGNValue: String, board pBoard: KaruahChessEngineC, commit pCommit: Bool) throws -> Bool {
            
            if ImportPGNProcessor.regexIsMatch(pattern: "^[a-h][1-8]([=][QRBN])?[\\+]?[#]?$", in: pPGNValue) {
                // Pawn move
                var promotion = BoardViewModel.PawnPromotionEnum.Queen
                if ImportPGNProcessor.regexIsMatch(pattern: "^[a-h][1-8][=][QRBN][\\+]?[#]?$", in: pPGNValue) {
                    let promoChar = String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 3)])
                    switch promoChar {
                        case "Q": promotion = .Queen
                        case "R": promotion = .Rook
                        case "B": promotion = .Bishop
                        case "N": promotion = .Knight
                        default:  promotion = .Queen
                    }
                }
                
                let validFromIndexes = Constants.FileDict[String(pPGNValue.prefix(1))] ?? []
                let toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue.prefix(2))] ?? -1
                let fromIndex = pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour(),pValidFromIndexes: validFromIndexes.map { NSNumber(value: $0) } )
                let mResult = pBoard.move(fromIndex, Int32(toIndex), Int32(promotion.rawValue), true, pCommit) as? MoveResult ?? MoveResult()
                return mResult.success
            }
            else if ImportPGNProcessor.regexIsMatch(pattern: "^[a-h][x][a-h][1-8]([=][QRBN])?[\\+]?[#]?$", in: pPGNValue) {
                // Pawn take move
                var promotion = BoardViewModel.PawnPromotionEnum.Queen
                if ImportPGNProcessor.regexIsMatch(pattern: "^[a-h][x][a-h][1-8][=][QRBN][\\+]?[#]?$", in: pPGNValue) {
                    let idx = pPGNValue.index(pPGNValue.startIndex, offsetBy: 5)
                    let promoChar = String(pPGNValue[idx])
                    switch promoChar {
                        case "Q": promotion = .Queen
                        case "R": promotion = .Rook
                        case "B": promotion = .Bishop
                        case "N": promotion = .Knight
                        default:  promotion = .Queen
                    }
                }
                                
                let validFromIndexes = Constants.FileDict[String(pPGNValue.prefix(1))] ?? []
                let toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 2)..<pPGNValue.index(pPGNValue.startIndex, offsetBy: 4)])] ?? -1
                let fromIndex = pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour(),pValidFromIndexes: validFromIndexes.map { NSNumber(value: $0) } )
                let mResult = pBoard.move(fromIndex, Int32(toIndex), Int32(promotion.rawValue), true, pCommit) as? MoveResult ?? MoveResult()
                
                return mResult.success
            }
            else if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][a-h]?[1-8]?[x]?[a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                // King, Queen, Rook, Bishop, Knight move with from file
                let pieceChar = String(pPGNValue.prefix(1))
                let spin: Int = {
                    switch pieceChar {
                    case "K": return Constants.WHITE_KING_SPIN
                    case "Q": return Constants.WHITE_QUEEN_SPIN
                    case "R": return Constants.WHITE_ROOK_SPIN
                    case "B": return Constants.WHITE_BISHOP_SPIN
                    case "N": return Constants.WHITE_KNIGHT_SPIN
                    default:  return 0
                    }
                }()
                
                var toIndex = -1
                var fromIndex = -1
                                
                if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                    // Move
                    toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue.dropFirst().prefix(2))] ?? -1
                    fromIndex = Int(pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour() * Int32(spin),pValidFromIndexes: []))
                }
                else if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][a-h][a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                    // Move with file
                    let validFromIndexes = Constants.FileDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 1)])] ?? []
                    toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 2)..<pPGNValue.index(pPGNValue.startIndex, offsetBy: 4)])] ?? -1
                    fromIndex = Int(pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour() * Int32(spin),pValidFromIndexes: validFromIndexes.map { NSNumber(value: $0) } ))
                }
                else if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][1-8][a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                    // Move with rank
                    let validFromIndexes = Constants.RankDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 1)])] ?? []
                    toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 2)..<pPGNValue.index(pPGNValue.startIndex, offsetBy: 4)])] ?? -1
                    fromIndex = Int(pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour() * Int32(spin),pValidFromIndexes: validFromIndexes.map { NSNumber(value: $0) } ))
                }
                else if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][x][a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                    // Move
                    toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 2)..<pPGNValue.index(pPGNValue.startIndex, offsetBy: 4)])] ?? -1
                    fromIndex = Int(pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour() * Int32(spin),pValidFromIndexes: []))
                }
                else if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][a-h][x][a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                    // Move with file
                    let validFromIndexes = Constants.FileDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 1)])] ?? []
                    toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 3)..<pPGNValue.index(pPGNValue.startIndex, offsetBy: 5)])] ?? -1
                    fromIndex = Int(pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour() * Int32(spin),pValidFromIndexes: validFromIndexes.map { NSNumber(value: $0) } ))
                }
                else if ImportPGNProcessor.regexIsMatch(pattern: "^[KQRBN][1-8][x][a-h][1-8][\\+]?[#]?$", in: pPGNValue) {
                    // Move with rank
                    let validFromIndexes = Constants.RankDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 1)])] ?? []
                    toIndex = Constants.BoardCoordinateReverseDict[String(pPGNValue[pPGNValue.index(pPGNValue.startIndex, offsetBy: 3)..<pPGNValue.index(pPGNValue.startIndex, offsetBy: 5)])] ?? -1
                    fromIndex = Int(pBoard.findFromIndex(pToIndex: Int32(toIndex),pSpin: pBoard.getStateActiveColour() * Int32(spin),pValidFromIndexes: validFromIndexes.map { NSNumber(value: $0) } ))
                }
                let mResult = pBoard.move(Int32(fromIndex), Int32(toIndex), Int32(BoardViewModel.PawnPromotionEnum.Queen.rawValue), true, pCommit) as? MoveResult ?? MoveResult()
                
                return mResult.success
            }
            else if ImportPGNProcessor.regexIsMatch(pattern: "^[O][-][O][\\+]?[#]?$", in: pPGNValue) {
                // King side castle
                var castlingAvailableKingSide: Bool = false
                if pBoard.getStateActiveColour() == Constants.WHITEPIECE {
                    castlingAvailableKingSide = (pBoard.getStateCastlingAvailability() & 0b000010) > 0
                } else if pBoard.getStateActiveColour() == Constants.BLACKPIECE {
                    castlingAvailableKingSide = (pBoard.getStateCastlingAvailability() & 0b001000) > 0
                }
                if !castlingAvailableKingSide {
                    throw NSError(domain: "PGN", code: 1, userInfo: [NSLocalizedDescriptionKey: "Castling not available, invalid move."])
                }
                
                let fromIndex = pBoard.getKingIndex(pBoard.getStateActiveColour())
                let toIndex = pBoard.getStateActiveColour() == Constants.WHITEPIECE ? 62 : 6
                let mResult = pBoard.move(Int32(fromIndex), Int32(toIndex), Int32(BoardViewModel.PawnPromotionEnum.Queen.rawValue), true, pCommit) as? MoveResult ?? MoveResult()
                
                return mResult.success
            }
            else if ImportPGNProcessor.regexIsMatch(pattern: "^[O][-][O][-][O][\\+]?[#]?$", in: pPGNValue) {
                // Queen side castle
                var castlingAvailableQueenSide: Bool = false
                if pBoard.getStateActiveColour() == Constants.WHITEPIECE {
                    castlingAvailableQueenSide = (pBoard.getStateCastlingAvailability() & 0b000001) > 0
                } else if pBoard.getStateActiveColour() == Constants.BLACKPIECE {
                    castlingAvailableQueenSide = (pBoard.getStateCastlingAvailability() & 0b000100) > 0
                }
                if !castlingAvailableQueenSide  {
                    throw NSError(domain: "PGN", code: 2, userInfo: [NSLocalizedDescriptionKey: "Castling not available, invalid move."])
                }
                
                let fromIndex = pBoard.getKingIndex(pBoard.getStateActiveColour())
                let toIndex = pBoard.getStateActiveColour() == Constants.WHITEPIECE ? 58 : 2
                let mResult = pBoard.move(Int32(fromIndex), Int32(toIndex), Int32(BoardViewModel.PawnPromotionEnum.Queen.rawValue), true, pCommit) as? MoveResult ?? MoveResult()
                
                return mResult.success
            }
            
            return false
        }
    
    /// Regular expression replace
    private static func regexReplace(pattern: String, in text: String, with template: String) -> String {
        guard let regex = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) else {
            return text
        }
        let range = NSRange(text.startIndex..., in: text)
        return regex.stringByReplacingMatches(in: text, options: [], range: range, withTemplate: template)
    }
    
    /// Regular expression is match
    private static func regexIsMatch(pattern: String, in text: String) -> Bool {
        guard let regex = try? NSRegularExpression(pattern: pattern, options: []) else {
            return false
        }
        let range = NSRange(text.startIndex..., in: text)
        return regex.firstMatch(in: text, options: [], range: range) != nil
    }
    
    /// Regular expression split with delimiter
    private static func regexSplit(pattern: String, in text: String) -> [String] {
        guard let regex = try? NSRegularExpression(pattern: pattern, options: []) else {
            return [text]
        }
        let range = NSRange(text.startIndex..., in: text)
        var results: [String] = []
        var lastEnd = text.startIndex
        
        regex.enumerateMatches(in: text, options: [], range: range) { match, _, _ in
            guard let match = match else { return }
            let matchRange = Range(match.range, in: text)!
            
            if lastEnd < matchRange.lowerBound {
                results.append(String(text[lastEnd..<matchRange.lowerBound]))
            }
            // Add the delimiter group itself (first capture group)
            if match.numberOfRanges > 1,
               let groupRange = Range(match.range(at: 1), in: text) {
                results.append(String(text[groupRange]))
            }
            lastEnd = matchRange.upperBound
        }
        
        if lastEnd < text.endIndex {
            results.append(String(text[lastEnd..<text.endIndex]))
        }
        
        return results
    }
    
    // Loads the game in to the database
    private func loadGameIntoDatabase(pGameRecordList: [GameRecordArray]) -> Int {
        var result = 0
        
        if pGameRecordList.count > 0 {
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
                for gameRecord in pGameRecordList {
                    board.setBoardArray(gameRecord.boardArray);
                    board.setStateArray(gameRecord.stateArray);
                    let boardSquareStr = board.getBoard();
                    let gameStateStr = board.getState();
                    
                    let sqlStr = "insert into GameRecord(Id, BoardSquareStr, GameStateStr, MoveSANStr) values (?,?,?,?)"
                    if sqlite3_prepare_v2(db, sqlStr, -1, &cursor, nil) == SQLITE_OK {
                        sqlite3_bind_int(cursor, 1, Int32(gameRecord.id))
                        sqlite3_bind_text(cursor, 2, (boardSquareStr as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                        sqlite3_bind_text(cursor, 3, (gameStateStr as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
                        sqlite3_bind_text(cursor, 4, (gameRecord.moveSAN as NSString).utf8String, -1, Constants.SQLITE_TRANSIENT)
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
        
        return result
    }
}

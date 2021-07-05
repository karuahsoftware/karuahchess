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

import SwiftUI
import AVFoundation

class BoardViewModel : ObservableObject {
    
    static let shared = BoardViewModel()
    let gameRecordDS: GameRecordDataService
    let boardLayout: TilePanelView
    let animationLayout: TileAnimationView
    let boardAnimation: BoardAnimation
    let processingIndicator : ActivityIndicatorViewModel
    let directionIndicator : DirectionIndicatorViewModel
    let parameterDS: ParameterDataService
    var userInteracted: Bool = false
    var lockPanel: Bool = false
    static let textReader = AVSpeechSynthesizer()
    let move: Move
    var computerMoveProcessing = false
    let processingQueue = DispatchQueue(label: "karuahchess.queue.processing", qos: .userInteractive, target: .global(qos: .userInteractive))
    let animationQueue = DispatchQueue(label: "karuahchess.queue.animation", qos: .userInteractive, target: .global(qos: .userInteractive))
    let pieceEditTool : PieceEditToolViewModel
    let boardMessage : BoardMessageAlertViewModel
    
    @Published var boardRotation : Double
    
    enum MoveTypeEnum : Int { case None = 0, Normal = 1, Enpassant = 2, Castle = 3, Promotion = 4}
    enum BoardStatusEnum : Int { case Ready = 0, Checkmate = 1, Stalemate = 2, Resigned = 3}
    enum PawnPromotionEnum : Int { case Knight = 2, Bishop = 3, Rook = 4, Queen = 5}
    enum PieceTypeEnum : Int { case Empty = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5, King = 6}
   
    /// Constructor
    private init() {
        // Parameter data service
        parameterDS = ParameterDataService()
        
        // Game record data service
        gameRecordDS = GameRecordDataService()
        
        // Processing indicator
        processingIndicator = ActivityIndicatorViewModel()
        
        // Direction indicator
        directionIndicator = DirectionIndicatorViewModel()
        
        // Piece Edit Tool
        pieceEditTool = PieceEditToolViewModel()
        
        // Board layout
        boardLayout = TilePanelView()
        BoardSquareDataService.update(pTilePanel: boardLayout, pRecord: gameRecordDS.getCurrentGame())
        
        // Animation layout
        animationLayout = TileAnimationView()
        boardAnimation = BoardAnimation()
        
        // Move
        move = Move(pTilePanel: boardLayout)
        
        // Set shake
        boardLayout.editMode(pEnable: parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled)
        
        // Set board rotation
        boardRotation = Double(parameterDS.get(pParameterClass: ParamRotateBoard.self).value)
        
        // Board message
        boardMessage = BoardMessageAlertViewModel()
        
        // Set the current record position
        navigateMaxRecord()
        
        
        
        //TODO: Other init items
        
        
    }
    
    
    /// Show the last move made
    func showLastMove() {
        let currentBoard = gameRecordDS.get(pId: BoardSquareDataService.gameRecordCurrentValue)
        let previousBoard = gameRecordDS.get(pId: BoardSquareDataService.gameRecordCurrentValue - 1)
        let lastChanges = gameRecordDS.getBoardSquareChanges(pBoardA: currentBoard, pBoardB: previousBoard)
        
        if lastChanges != 0 {
            boardLayout.setHighLightFullFadeOut(pBits: lastChanges)
            readText(pText: "Last move")
        }
        else {
            showMessage(pTextFull: "No last move to show.", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
        }
    }
    
    /// Tile move action - used for dragging pieces
    func onTileMoveAction(pFromTile: TileView, pToTile: TileView) {
        let arrangeBoardEnabled: Bool = parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled
        userInteracted = true
        move.clear()
        
        if !arrangeBoardEnabled {
            userMoveAdd(pTile: pFromTile, pAnimate: false)
            userMoveAdd(pTile: pToTile, pAnimate: false)
        }
        else {
            arrangeUpdate(pFromIndex: pFromTile.index, pToIndex: pToTile.index)
        }
    }
    
    
    /// Listens for tile clicks
    /// - Parameter pTile: The tile that was clicked
    func onTileClick(pTile: TileView) {
        let arrangeBoardEnabled: Bool = parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled
        let gameFinished: Bool = Int(gameRecordDS.currentGame.getStateGameStatus()) != BoardStatusEnum.Ready.rawValue
        userInteracted = true
        
         
        if !gameFinished && !lockPanel && !arrangeBoardEnabled {
            userMoveAdd(pTile: pTile, pAnimate: true)
        }
        else if arrangeBoardEnabled {
            pieceEditTool.show(pTile: pTile)
            
        }
        
        if gameFinished {
           showMessage(pTextFull: "Cannot move as game has finished.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
        }
        
        
    }
    
    
    /// Moves a piece from one square to another
    func arrangeUpdate(pFromIndex: Int, pToIndex: Int) {
        let arrangeBoardEnabled: Bool = parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled
        if arrangeBoardEnabled {
            if let record: GameRecordArray = gameRecordDS.get(pId: BoardSquareDataService.gameRecordCurrentValue) {
                pieceEditTool.bufferBoard.setBoardArray(record.boardArray)
                pieceEditTool.bufferBoard.setStateArray(record.stateArray)
                let result : MoveResult = pieceEditTool.bufferBoard.arrange(Int32(pFromIndex), Int32(pToIndex)) as? MoveResult ?? MoveResult()
                if result.success {
                    record.boardArray = pieceEditTool.bufferBoard.getBoardArraySafe()
                    record.stateArray = pieceEditTool.bufferBoard.getStateArraySafe()
                    BoardSquareDataService.update(pTilePanel: self.boardLayout, pRecord: record)
                    _ = gameRecordDS.updateGameState(pGameRecordArray: record)
                }
                else {
                    showMessage(pTextFull: result.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                }
            }
        }
    }
    
    
    /// Updates the piece type on a square. Used for editing the board
    func arrangeUpdate(pFen: Character, pToIndex: Int) {
        let arrangeBoardEnabled: Bool = parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled
        if arrangeBoardEnabled {
            if let record: GameRecordArray = gameRecordDS.get(pId: BoardSquareDataService.gameRecordCurrentValue) {
                pieceEditTool.bufferBoard.setBoardArray(record.boardArray)
                pieceEditTool.bufferBoard.setStateArray(record.stateArray)
                let result : MoveResult = pieceEditTool.bufferBoard.arrangeUpdate(Int8(pFen.asciiValue ?? 0), Int32(pToIndex)) as? MoveResult ?? MoveResult()
                if result.success {
                    record.boardArray = pieceEditTool.bufferBoard.getBoardArraySafe()
                    record.stateArray = pieceEditTool.bufferBoard.getStateArraySafe()
                    BoardSquareDataService.update(pTilePanel: self.boardLayout, pRecord: record)
                    _ = gameRecordDS.updateGameState(pGameRecordArray: record)
                }
                else {
                    showMessage(pTextFull: result.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                }
            }
        }
    }
    
    /// Add a tile to the move when user clicks a square
    /// - Parameters:
    ///   - pTile: The tile to add
    ///   - pAnimate: Whether to animate the move or not
    private func userMoveAdd(pTile: TileView, pAnimate: Bool) {
        processingQueue.async { [self] in
            let moveHighlightEnabled = parameterDS.get(pParameterClass: ParamMoveHighlight.self).enabled
            
            // Ensure game record is set to the latest
            let maxRecId = gameRecordDS.getMaxId()
            if BoardSquareDataService.gameRecordCurrentValue != maxRecId {
                DispatchQueue.main.async {
                    navigateMaxRecord()
                    move.clear()
                }
                return
            }
            
            // Select highlight mode
            let highlight = moveHighlightEnabled ? Move.HighlightEnum.MovePath : Move.HighlightEnum.Select
            
            // Create proposed move
            let moveSelected = move.add(pBoardSquareIndex: pTile.index, pBoard: gameRecordDS.currentGame, pHighlight: highlight)
            
            // Restart the computer move (if required)
            startComputerMoveTask()
            
            if (moveSelected) {
                let boardBeforeMove = gameRecordDS.getCurrentGame()
                
                // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
                let promotionPiece: Int = PawnPromotionEnum.Queen.rawValue
                if gameRecordDS.currentGame.isPawnPromotion(Int32(move.fromIndex), Int32(move.toIndex)) {
                    //TODO: promotionPiece = showPawnPromotionDialog(gameRecordDS.currentGame.getStateActiveColour())
                }
                
                let gameStatusBeforeMove = gameRecordDS.currentGame.getStateGameStatus()
                let moveResult = gameRecordDS.currentGame.move(Int32(move.fromIndex), Int32(move.toIndex), Int32(promotionPiece), true, true) as? MoveResult ?? MoveResult()
                
                
                if moveResult.success {
                    if moveResult.returnMessage != "" {
                        showMessage(pTextFull: moveResult.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                    }
                    else {
                        let ssml = getBoardSquareSSML(pMoveDataStr: moveResult.moveDataStr)
                        readText(pText: ssml)
                    }
                    
                    let boardAfterMove = gameRecordDS.getCurrentGame()
                    
                    
                    // Update Display
                    BoardSquareDataService.update(pTilePanel: self.boardLayout, pRecord: self.gameRecordDS.getCurrentGame())
                
                    // Do animation
                    if(pAnimate) {
                        let moveAnimationList = boardAnimation.createAnimationList(pBoardRecA: boardBeforeMove, pBoardRecB: boardAfterMove)
                        startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDuration: 1.2, pClearWhenFinished: true)
                    }
                    
                    // Record the game state
                    recordCurrentGameState()
                    
                    // Update score if checkmate occurred
                    if gameStatusBeforeMove == BoardStatusEnum.Ready.rawValue && Int(gameRecordDS.currentGame.getStateGameStatus()) == BoardStatusEnum.Checkmate.rawValue {
                        let kingFallIndex: Int = Int(gameRecordDS.currentGame.getKingIndex(gameRecordDS.currentGame.getStateActiveColour()))
                        let kingFallSeq = boardAnimation.createAnimationFall(pIndex: kingFallIndex, pTilePanel: boardLayout)
                        
                        startPieceAnimation(pLockPanel: true, pAnimationList: kingFallSeq, pDuration: 0.5, pClearWhenFinished: false)
                    }
                    
                    
                    
                    // Start computer move task
                    startComputerMoveTask()
                }
                else if (!moveResult.success) {
                    showMessage(pTextFull: moveResult.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                }
                
                // Clear the move selected
                move.clear()
                
            }
        }
    }
    
    
    /// Start computer move task
    private func startComputerMoveTask() {
        processingQueue.async { [self] in
        
            let arrangeBoardEnabled = parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled
            let computerPlayerEnabled = parameterDS.get(pParameterClass: ParamComputerPlayer.self).enabled
            let computerMoveFirstEnabled = parameterDS.get(pParameterClass: ParamComputerMoveFirst.self).enabled
            let limitEngineStrengthELO = parameterDS.get(pParameterClass: ParamLimitEngineStrengthELO.self).eloRating
            
            let computerColour = computerMoveFirstEnabled ? Constants.WHITEPIECE : Constants.BLACKPIECE
            let turnColour = Int(gameRecordDS.currentGame.getStateActiveColour())
            let boardStatus = Int(gameRecordDS.currentGame.getStateGameStatus())
            
            if boardStatus == 0 && (!computerMoveProcessing) && (!arrangeBoardEnabled) && computerPlayerEnabled && computerColour == turnColour {
                lockPanel = true
                // Clear the user move since starting computer move
                move.clear()
                computerMoveProcessing = true
                
                DispatchQueue.main.async {
                    processingIndicator.enabled = true
                }
                
                let searchOptions = SearchOptions()
                searchOptions.limitStrengthELO = Int32(limitEngineStrengthELO)
                
                let topMove: SearchResult = gameRecordDS.currentGame.searchStart(searchOptions) as? SearchResult ?? SearchResult()
                
                if !(topMove.cancelled || topMove.error) {
                    let boardBeforeMove = gameRecordDS.getCurrentGame()
                    let gameStatusBeforeMove = gameRecordDS.currentGame.getStateGameStatus()
                    let moveResult: MoveResult = gameRecordDS.currentGame.move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, true, true) as? MoveResult ?? MoveResult()
                    if moveResult.success {
                        
                        //Read text, showmessage
                        if moveResult.returnMessage != "" {
                            showMessage(pTextFull: moveResult.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                        }
                        else {
                            let ssml = getBoardSquareSSML(pMoveDataStr: moveResult.moveDataStr)
                            readText(pText: ssml)
                        }
                        
                        // Do animation
                        let boardAfterMove = gameRecordDS.getCurrentGame()
                        let moveAnimationList = boardAnimation.createAnimationList(pBoardRecA: boardBeforeMove, pBoardRecB: boardAfterMove)
                    
                        BoardSquareDataService.update(pTilePanel: boardLayout, pRecord: gameRecordDS.getCurrentGame())
                        startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDuration: 1.2, pClearWhenFinished: true)
                        
                        // Record the game state
                        recordCurrentGameState()
                        
                        // Update score if checkmate occurred
                        if gameStatusBeforeMove == BoardStatusEnum.Ready.rawValue && gameRecordDS.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.rawValue {
                            let kingFallIndex: Int = Int(gameRecordDS.currentGame.getKingIndex(gameRecordDS.currentGame.getStateActiveColour()))
                            let kingFallSeq = boardAnimation.createAnimationFall(pIndex: kingFallIndex, pTilePanel: boardLayout)
                            startPieceAnimation(pLockPanel: true, pAnimationList: kingFallSeq, pDuration: 0.5, pClearWhenFinished: false)
                            
                        }
                        
                        
                        
                    }
                }
                else {
                    if topMove.error {
                        showMessage(pTextFull: "Unable to move. Invalid board configuration.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
                    }
                }
                
                DispatchQueue.main.async {
                    processingIndicator.enabled = false
                }
                computerMoveProcessing = false
                lockPanel = false
                
            }
        }
    }
    
    
    /// Get SSML for board square id
    /// - Parameter pMoveDataStr: Move Data String
    /// - Returns: SSML string
    private func getBoardSquareSSML(pMoveDataStr: String) -> String {
        let moveDataStrArray: [Substring] = pMoveDataStr.split(separator: "|")
        
        // Check move data array is the correct size
        if moveDataStrArray.count != 4 {
            return ""
        }
        
        let fromIndex: Int = Int(moveDataStrArray[0]) ?? -1
        let toIndex: Int = Int(moveDataStrArray[1]) ?? -1
        let origFromSpin: Int = Int(moveDataStrArray[2]) ?? -1
        let origToSpin: Int = Int(moveDataStrArray[3]) ?? -1
        
        // Check from and to indexes are in the correct range
        if !(0...63 ~= fromIndex && 0...63 ~= toIndex) {
            return ""
        }
        
        if origToSpin != 0 {
            return getPieceSpinSSML(pPieceSpin: origFromSpin) + " takes " + getPieceSpinSSML(pPieceSpin: origToSpin)
        }
        else {
            return getPieceSpinSSML(pPieceSpin: origFromSpin) + " to " + (Constants.BoardCoordinateDict[toIndex] ?? "")
        }
    }
    
    
    /// Get SSML for piece spin
    /// - Parameter pPieceSpin: Spin
    /// - Returns: SSML string
    private func getPieceSpinSSML(pPieceSpin: Int) -> String {
        let board = KaruahChessEngineC()
        let ssml = board.getPieceNameFromChar(pFenChar: board.getFENCharFromSpin(pSpin: Int32(pPieceSpin)))
        
        return ssml
    }
    
    
    /// Starts an animation
    /// - Parameters:
    ///   - pLockPanel: Set true to lock the panel
    ///   - pAnimationInstruction: The animation instruction
    ///   - pDuration: Duration of the animation
    private func startPieceAnimation(pLockPanel: Bool, pAnimationList: [TileAnimationInstruction], pDuration: Double, pClearWhenFinished: Bool) {
        let animationDispatchGroup = DispatchGroup()
        animationDispatchGroup.enter()
        
        animationQueue.async { [self] in
            
            if pLockPanel {
                lockPanel = true
            }
            
            // Do the animation
            DispatchQueue.main.async {
                animationLayout.tileAnimationVM.runAnimation(pAnimationList: pAnimationList, pTilePanel: self.boardLayout, pDuration: pDuration)
            }
            
            // Wait for enough time for animation to finish
            Thread.sleep(forTimeInterval: pDuration + 0.3)
            
            if pClearWhenFinished {
                DispatchQueue.main.async {
                    boardLayout.showAll()
                    animationLayout.tileAnimationVM.clear()
                }
                
            }
        
            
            if pLockPanel {
                lockPanel = false
            }
            
            animationDispatchGroup.leave()
        }
        
        animationDispatchGroup.wait()
    }
    
    
    
    
    /// Show message
    /// - Parameters:
    ///   - pTextFull: Full message to display
    ///   - pTextShort: A shorter form of the message for the text reader
    ///   - pDuration: Length of time to show message
    private func showMessage(pTextFull: String, pTextShort: String, pDurationms: Int) {
        DispatchQueue.main.async { [self] in
            if pTextFull.trimmingCharacters(in: .whitespacesAndNewlines) != "" {
                // Read message
                if pTextShort.trimmingCharacters(in: .whitespacesAndNewlines) == "" {
                    readText(pText: pTextFull)
                }
                else {
                    readText(pText: pTextShort)
                }
                
                // Show toast message
                Device.shared.toastMessage = pTextFull
                DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(pDurationms)) {
                    Device.shared.toastMessage = ""
                }
            }
        }
    }
    
    
    /// Converts a text string to speech
    /// - Parameter pText: The text to read
    private func readText(pText: String) {
        DispatchQueue.main.async { [self] in
            let soundEnabled: Bool = parameterDS.get(pParameterClass: ParamSound.self).enabled
            
            if soundEnabled {
                let utterance = AVSpeechUtterance(string: pText)
                BoardViewModel.textReader.speak(utterance)
            }
        }
        
    }
    
    
    /// Set the board to the max record
    private func navigateMaxRecord() {
            let maxId = gameRecordDS.getMaxId()
            
            navigateGameRecord(pRecId: maxId, pAnimate: false)
            
            // Refresh shake animation
            //TODO: boardLayout.shakeRefresh()
            
            // Load the move navigator
            let navParam = parameterDS.get(pParameterClass: ParamNavigator.self)
            //TODO: loadNavigator(navParam.enabled)
    }
    
    /// Navigate to requested game record
    /// - Parameters:
    ///   - pRecId: The record Id to which to navigate
    ///   - pAnimate: Animate the navigation or not
    private func navigateGameRecord(pRecId: Int, pAnimate: Bool) {
       
            if pRecId > 0 {
                
                // Update board displayed with requested record
                if let updateBoard = gameRecordDS.get(pId: pRecId) {
                    endPieceAnimation()
                    
                    //Do Animation
                    if pAnimate {
                        if let oldBoard: GameRecordArray = gameRecordDS.get(pId: BoardSquareDataService.gameRecordCurrentValue) {
                            let moveAnimationList = boardAnimation.createAnimationList(pBoardRecA: oldBoard, pBoardRecB: updateBoard)
                            
                            // Do animation
                            startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDuration: 1.2, pClearWhenFinished: true)
                            
                            DispatchQueue.main.async {
                                BoardSquareDataService.update(pTilePanel: self.boardLayout, pRecord: updateBoard)
                                BoardSquareDataService.gameRecordCurrentValue = pRecId
                                self.updateBoardIndicators(pRecord: updateBoard)
                            }
                        }
                    }
                    else {
                        DispatchQueue.main.async {
                            BoardSquareDataService.update(pTilePanel: self.boardLayout, pRecord: updateBoard)
                            BoardSquareDataService.gameRecordCurrentValue = pRecId
                            self.updateBoardIndicators(pRecord: updateBoard)
                        }
                    }
                        
                    move.clear()
                }
            }
      
    }
    
    
    /// Records the current state of the game
    private func recordCurrentGameState() {
        
        
        let success = gameRecordDS.recordGameState(pWhiteClockOffset: 0, pBlackClockOffset: 0)
        if (success > 0) {
            
            // Ensure game record position is set to max value
            navigateMaxRecord()
        }
        
        
    }
    
    
    /// Starts a new game
    func newGame() {
        endMoveJob()
        endPieceAnimation()
        lockPanel = true
        gameRecordDS.reset()
        move.clear()
        navigateMaxRecord()
        lockPanel = false
    }
    
    
    /// Undo last move
    func undoMove() {
        processingQueue.async { [self] in
        
            endMoveJob()
            endPieceAnimation()
            
            if let boardBeforeUndo = gameRecordDS.get() {
                if gameRecordDS.undo() {
                    lockPanel = true
                    if let boardAfterUndo = gameRecordDS.get() {
                        let moveAnimationList = self.boardAnimation.createAnimationList(pBoardRecA: boardBeforeUndo, pBoardRecB: boardAfterUndo)
                        startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDuration: 1.2, pClearWhenFinished: true)
                    }
                    
                }
            }
            
            move.clear()
            navigateMaxRecord()
            lockPanel = false
        }
    }
    
    
    /// Ends any running move job
    private func endMoveJob () {
        gameRecordDS.currentGame.cancelSearch()
    }
    
    
    /// Shows a board message alert
    /// - Parameters:
    ///   - pTitle: The title
    ///   - pMessage: Message content
    private func showBoardMessageAlert(pTitle: String, pMessage: String) {
        if pTitle != "" {
            readText(pText: pMessage)
            boardMessage.show(pTitle, pMessage, BoardMessageAlertViewModel.alertTypeEnum.Ok)
        }
    }
    
    /// Ends animations
    private func endPieceAnimation() {
        DispatchQueue.main.async { [self] in
            boardLayout.showAll()
            animationLayout.tileAnimationVM.clear()
        }
    }
    
    
    /// Updates all the board indicators
    /// - Parameter pRecord: The game record array
    private func updateBoardIndicators(pRecord: GameRecordArray) {
        DispatchQueue.main.async { [self] in
            _ = updateGameMessage(pRecord: pRecord)
            updateDirectionIndicator(pRecord: pRecord)
            updateCheckIndicator(pRecord: pRecord)
        }
    }
    
    
    /// Update direction indicator
    /// - Parameter pRecord: The game record array
    private func updateDirectionIndicator(pRecord: GameRecordArray) {
        DispatchQueue.main.async { [self] in
            let board = KaruahChessEngineC()
            board.setBoardArray(pRecord.boardArray)
            board.setStateArray(pRecord.stateArray)
            directionIndicator.direction = Int(board.getStateActiveColour())
            
        }
    }
    
    
    /// Set the check indicator
    /// - Parameter pRecord: The game record array
    private func updateCheckIndicator(pRecord: GameRecordArray) {
        DispatchQueue.main.async { [self] in
            let board = KaruahChessEngineC()
            board.setBoardArray(pRecord.boardArray)
            board.setStateArray(pRecord.stateArray)
            
            // Circles the king if in check
            if board.getStateActiveColour() != 0 {
                let kingCheck = board.isKingCheck(board.getStateActiveColour())
                
                if kingCheck {
                    let kingIndex = board.getKingIndex(board.getStateActiveColour())
                    if kingIndex >= 0 {
                        boardLayout.setCheckIndicator(pKingIndex: Int(kingIndex))
                        showMessage(pTextFull: "Check", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                    }
                }
                else {
                    boardLayout.setCheckIndicator(pKingIndex: -1)
                }
                
            }
        }
    }
    
    
    /// Show game messages
    func updateGameMessage(pRecord: GameRecordArray) -> Bool {
        var gameFinished = false
        
        let board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)
        
        var title = ""
        var msg = ""
        
        if board.getStateGameStatus() == BoardStatusEnum.Checkmate.rawValue && board.getStateActiveColour() == Constants.BLACKPIECE {
            gameFinished = true
            title = "Checkmate! White wins."
        }
        else if board.getStateGameStatus() == BoardStatusEnum.Checkmate.rawValue && board.getStateActiveColour() == Constants.WHITEPIECE {
            gameFinished = true
            title = "Checkmate! Black wins."
        }
        else if board.getStateGameStatus() == BoardStatusEnum.Stalemate.rawValue {
            gameFinished = true
            title = "Stalemate, no winners."
        } else if board.getStateGameStatus() == BoardStatusEnum.Resigned.rawValue && board.getStateActiveColour() == Constants.WHITEPIECE {
            gameFinished = true
            title = "White resigned. Black wins."
        }
        else if board.getStateGameStatus() == BoardStatusEnum.Resigned.rawValue && board.getStateActiveColour() == Constants.BLACKPIECE {
            gameFinished = true
            title = "Black resigned. White wins."
        }
        
        if gameFinished && parameterDS.get(pParameterClass: ParamComputerPlayer.self).enabled {
            let paramELO = parameterDS.get(pParameterClass: ParamLimitEngineStrengthELO.self)
            if let eloIndex = Constants.eloarray.firstIndex(of: paramELO.eloRating) {
                msg = "Computer Strength: " + Constants.strengthArrayLabel[eloIndex]
            }
        }
        else {
            msg = "Game over"
        }
        
        showBoardMessageAlert(pTitle: title, pMessage: msg)
        
        return gameFinished
    }
    
    
    
    
    /// Resigns the current game
    func resignGame() {
        processingQueue.async { [self] in
            let arrangeBoardEnabled = parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled
            
            if arrangeBoardEnabled {
                showMessage(pTextFull: "Cannot resign in edit mode.", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                return
            }
            
            let computerPlayerEnabled = parameterDS.get(pParameterClass: ParamComputerPlayer.self).enabled
            let computerMoveFirstEnabled = parameterDS.get(pParameterClass: ParamComputerMoveFirst.self).enabled
            let computerColour = computerMoveFirstEnabled ? Constants.WHITEPIECE : Constants.BLACKPIECE
            let turnColour = gameRecordDS.currentGame.getStateActiveColour()
            
            if computerPlayerEnabled && (turnColour == computerColour) {
                showMessage(pTextFull: "Cannot resign as it is not your turn.", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                return
            }
            
            // Ensure game record is set to the latest
            navigateMaxRecord()
            
            let status = gameRecordDS.currentGame.getStateGameStatus()
            
            // Only resign if game is not already finished
            if status == BoardStatusEnum.Ready.rawValue && gameRecordDS.currentGame.getStateFullMoveCount() > 0 {
                // Stop task if running
                endMoveJob()
                
                // End any animation if running
                endPieceAnimation()
                
                // Set the state of the game to resign
                gameRecordDS.currentGame.setStateGameStatus(Int32(BoardStatusEnum.Resigned.rawValue))
                
                // Record the current game state
                recordCurrentGameState()
                
                // Do animation, display message
                let kingFallIndex = Int(gameRecordDS.currentGame.getKingIndex(gameRecordDS.currentGame.getStateActiveColour()))
                let kingFallSeq = boardAnimation.createAnimationFall(pIndex: kingFallIndex, pTilePanel: boardLayout)
                startPieceAnimation(pLockPanel: true, pAnimationList: kingFallSeq, pDuration: 0.5, pClearWhenFinished: false)
                
            }
            else {
                showMessage(pTextFull: "Resigning not available at this stage of the game", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
        
        }
    }
    
    
    /// Increment the board rotation
    func rotateClick() {
        endPieceAnimation()
        let currentRotationParam = parameterDS.get(pParameterClass: ParamRotateBoard.self)
        var newRotation = currentRotationParam.value + 90
        if newRotation >= 360 {
            newRotation = 0
        }
        currentRotationParam.value = newRotation
        _ = BoardViewModel.shared.parameterDS.set(pObj: currentRotationParam)
        boardRotation = Double(newRotation)
        
    }
    
    
    /// Flips the direction of the current game
    func switchDirection() {
        endMoveJob()
        
        let board = KaruahChessEngineC()
        if let record = gameRecordDS.get(pId: BoardSquareDataService.gameRecordCurrentValue) {
            board.setBoardArray(record.boardArray)
            board.setStateArray(record.stateArray)
            
            // Flip direction
            board.setStateActiveColour(board.getStateActiveColour() * (-1))
            record.stateArray = board.getStateArraySafe()
            _ = gameRecordDS.updateGameState(pGameRecordArray: record)
            updateBoardIndicators(pRecord: record)
        }
    }
    
}

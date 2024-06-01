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
import AVFoundation

@MainActor class BoardViewModel : ObservableObject {
    
    static let instance = BoardViewModel()
    let tilePanelVM: TilePanelViewModel
    let tileAnimationVM: TileAnimationViewModel
    let boardAnimation: BoardAnimation
    let activityIndicatorVM : ActivityIndicatorViewModel
    let directionIndicatorVM : DirectionIndicatorViewModel
    var userInteracted: Bool = false
    var lockPanel: Bool = false
    static let textReader = AVSpeechSynthesizer()
    let move: Move
    var computerMoveProcessing = false
    var computerHintProcessing = false
    let pieceEditToolVM : PieceEditToolViewModel
    let boardMessageAlertVM : BoardMessageAlertViewModel
    let castlingRightsVM : CastlingRightsViewModel
    let navigatorVM : NavigatorViewModel 
    let pawnPromotionVM : PawnPromotionViewModel
    var audioPlayerPieceMoveSound: AVAudioPlayer?
    let hintBoardActor = HintActor()
    let srchActor = SearchActor()
    
    @Published var showFileExporter: Bool = false
    @Published var exportFileDocument: ExportFileDocument? = nil
    @Published var showFileImporter: Bool = false
    @Published var boardRotation : Double
    
    
    var coordPanelEnabled : Bool{
        willSet {
            refreshTileSize(pCoordPanelEnabled: newValue, pNavigationEnabled: navigatorVM.enabled)
            objectWillChange.send()
        }
    }
    
    enum MoveTypeEnum : Int { case None = 0, Normal = 1, Enpassant = 2, Castle = 3, Promotion = 4}
    enum BoardStatusEnum : Int { case Ready = 0, Checkmate = 1, Stalemate = 2, Resigned = 3}
    enum PawnPromotionEnum : Int { case Knight = 2, Bishop = 3, Rook = 4, Queen = 5}
    enum PieceTypeEnum : Int { case Empty = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5, King = 6}
   
    /// Constructor
    private init() {
        
        // Processing indicator
        activityIndicatorVM = ActivityIndicatorViewModel()
        
        // Direction indicator
        directionIndicatorVM = DirectionIndicatorViewModel()
        
        // Piece Edit Tool
        pieceEditToolVM = PieceEditToolViewModel()
        
        // Castling Rights
        castlingRightsVM = CastlingRightsViewModel()
        
        // Pawn Promotion
        pawnPromotionVM = PawnPromotionViewModel()
        
        // Board layout
        tilePanelVM = TilePanelViewModel()
        BoardSquareDataService.instance.update(pTilePanelVM: tilePanelVM, pRecord: GameRecordDataService.instance.getCurrentGame())
        
        // Animation layout
        tileAnimationVM = TileAnimationViewModel()
        boardAnimation = BoardAnimation()
        
        // Move Navigator
        navigatorVM = NavigatorViewModel(pEnabled: ParameterDataService.instance.get(pParameterClass: ParamNavigator.self).enabled)
        
        // Move
        move = Move(pTilePanelVM: tilePanelVM)
        
        // Set shake
        tilePanelVM.editMode(pEnable: ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled)
        
        // Set board rotation
        boardRotation = Double(ParameterDataService.instance.get(pParameterClass: ParamRotateBoard.self).value)
        
        // Board message
        boardMessageAlertVM = BoardMessageAlertViewModel()
        
        // Coordinate panel
        coordPanelEnabled = ParameterDataService.instance.get(pParameterClass: ParamBoardCoord.self).enabled
        
        // piece move sound is initialised on the first sound
        audioPlayerPieceMoveSound = nil
        
        // Set the current record position
        Task(priority: .userInitiated) {
            await navigateMaxRecord()
        }
        
        // Refresh tile size
        refreshTileSize(pCoordPanelEnabled: coordPanelEnabled, pNavigationEnabled: navigatorVM.enabled)
    }
    
    
    /// Show the last move made
    func showLastMove() {
        let currentBoard = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue)
        let previousBoard = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue - 1)
        let lastChanges = GameRecordDataService.instance.getBoardSquareChanges(pBoardA: currentBoard, pBoardB: previousBoard)
        
        if lastChanges != 0 {
            tilePanelVM.setHighLightFullFadeOut(pBits: lastChanges, pColour: Color("Magenta"))
            readText(pText: "Last move")
        }
        else {
            showMessage(pTextFull: "No last move to show.", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
        }
    }
    
    /// Tile move action - used for dragging pieces
    
    func onTileMoveAction(pFromTile: TileView, pToTile: TileView) async {
       
        let arrangeBoardEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
        userInteracted = true
        await move.clear()
        
        if !lockPanel && !arrangeBoardEnabled {
            await userMoveAdd(pTile: pFromTile, pAnimate: false)
            await userMoveAdd(pTile: pToTile, pAnimate: false)
        }
        else {
            arrangeUpdate(pFromIndex: pFromTile.index, pToIndex: pToTile.index)
        }
        
    }
    
    
    /// Listens for tile clicks
    /// - Parameter pTile: The tile that was clicked
    func onTileClick(pTile: TileView) {
        let arrangeBoardEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
        let gameFinished: Bool = Int(GameRecordDataService.instance.currentGame.getStateGameStatus()) != BoardStatusEnum.Ready.rawValue
        userInteracted = true
        
        if !lockPanel && !arrangeBoardEnabled {
            if !gameFinished {
                Task(priority: .userInitiated) {
                    await userMoveAdd(pTile: pTile, pAnimate: true)
                }
            }
            else {
                Task(priority: .userInitiated) {
                    await navigateMaxRecord()
                }
            }
        }
        else if arrangeBoardEnabled {
            if pieceEditToolVM.visible || castlingRightsVM.visible {
                pieceEditToolVM.close()
                castlingRightsVM.close()
            }
            else if pTile.tileVM.spin == Constants.WHITE_KING_SPIN || pTile.tileVM.spin == Constants.BLACK_KING_SPIN {
                if let record = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue) {
                    castlingRightsVM.show(pTile, record)
                }
                pieceEditToolVM.close()
            }
            else {
                pieceEditToolVM.show(pTile: pTile)
                castlingRightsVM.close()
            }
        }
        
        if gameFinished {
          //  showMessage(pTextFull: "Cannot move as game has finished.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
        }
    }
    
    
    /// Moves a piece from one square to another
    func arrangeUpdate(pFromIndex: Int, pToIndex: Int) {
        let arrangeBoardEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
        if arrangeBoardEnabled {
            if let record: GameRecordArray = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue) {
                pieceEditToolVM.bufferBoard.setBoardArray(record.boardArray)
                pieceEditToolVM.bufferBoard.setStateArray(record.stateArray)
                let result : MoveResult = pieceEditToolVM.bufferBoard.arrange(Int32(pFromIndex), Int32(pToIndex)) as? MoveResult ?? MoveResult()
                if result.success {
                    record.boardArray = pieceEditToolVM.bufferBoard.getBoardArraySafe()
                    record.stateArray = pieceEditToolVM.bufferBoard.getStateArraySafe()
                    BoardSquareDataService.instance.update(pTilePanelVM: tilePanelVM, pRecord: record)
                    _ = GameRecordDataService.instance.updateGameState(pGameRecordArray: record)
                }
                else {
                    showMessage(pTextFull: result.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                }
            }
        }
    }
    
    
    /// Updates the piece type on a square. Used for editing the board
    func arrangeUpdate(pFen: Character, pToIndex: Int) {
        let arrangeBoardEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
        if arrangeBoardEnabled {
            if let record: GameRecordArray = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue) {
                pieceEditToolVM.bufferBoard.setBoardArray(record.boardArray)
                pieceEditToolVM.bufferBoard.setStateArray(record.stateArray)
                let result : MoveResult = pieceEditToolVM.bufferBoard.arrangeUpdate(Int8(pFen.asciiValue ?? 0), Int32(pToIndex)) as? MoveResult ?? MoveResult()
                if result.success {
                    record.boardArray = pieceEditToolVM.bufferBoard.getBoardArraySafe()
                    record.stateArray = pieceEditToolVM.bufferBoard.getStateArraySafe()
                    BoardSquareDataService.instance.update(pTilePanelVM: tilePanelVM, pRecord: record)
                    _ = GameRecordDataService.instance.updateGameState(pGameRecordArray: record)
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
    private func userMoveAdd(pTile: TileView, pAnimate: Bool) async {
        
            
        let moveHighlightEnabled = ParameterDataService.instance.get(pParameterClass: ParamMoveHighlight.self).enabled
        
        // Ensure game record is set to the latest
        let maxRecId = GameRecordDataService.instance.getMaxId()
        if BoardSquareDataService.instance.gameRecordCurrentValue != maxRecId {
            await navigateMaxRecord()
            await move.clear()
            return
        }
        
        // Select highlight mode
        let highlight = moveHighlightEnabled ? Move.HighlightEnum.MovePath : Move.HighlightEnum.Select
        
        // Create proposed move
        let moveSelected = await move.add(pBoardSquareIndex: pTile.index, pBoard: GameRecordDataService.instance.currentGame, pHighlight: highlight)
        
        // Restart the computer move (if required)
        await startComputerMoveTask()
        
        if (moveSelected) {
            let boardBeforeMove = GameRecordDataService.instance.getCurrentGame()
            
            // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
            var promotionPiece: Int = PawnPromotionEnum.Queen.rawValue
            let promoteAutoEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamPromoteAuto.self).enabled
            let isPawnPromotion: Bool = GameRecordDataService.instance.currentGame.isPawnPromotion(Int32(move.fromIndex), Int32(move.toIndex))
            if (!promoteAutoEnabled) && isPawnPromotion {
                promotionPiece = await pawnPromotionVM.show(pColour: Int(GameRecordDataService.instance.currentGame.getStateActiveColour()))
                if promotionPiece == 0 { return };
            }
            
            let gameStatusBeforeMove = GameRecordDataService.instance.currentGame.getStateGameStatus()
            let moveResult = GameRecordDataService.instance.currentGame.move(Int32(move.fromIndex), Int32(move.toIndex), Int32(promotionPiece), true, true) as? MoveResult ?? MoveResult()
            
            if moveResult.success {
                
                // Do animation
                if(pAnimate) {
                    let moveSpeedIndex: Int = ParameterDataService.instance.get(pParameterClass: ParamMoveSpeed.self).speed
                    if Constants.moveSpeedSeconds.indices.contains(moveSpeedIndex) {
                        let boardAfterMove = GameRecordDataService.instance.getCurrentGame()
                        let moveAnimationList = boardAnimation.createAnimationList(pBoardRecA: boardBeforeMove, pBoardRecB: boardAfterMove)
                        await startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDurationSeconds: Constants.moveSpeedSeconds[moveSpeedIndex])
                    }
                }
                
                // Update Display
                BoardSquareDataService.instance.update(pTilePanelVM: self.tilePanelVM, pRecord: GameRecordDataService.instance.getCurrentGame())
                    
                playPieceMoveSoundEffect()
                
                // Record the game state
                await recordCurrentGameState()
                
                // Update score if checkmate occurred
                if gameStatusBeforeMove == BoardStatusEnum.Ready.rawValue && Int(GameRecordDataService.instance.currentGame.getStateGameStatus()) == BoardStatusEnum.Checkmate.rawValue {
                    await afterCheckMate(pBoard: GameRecordDataService.instance.currentGame)
                }
                
                // Start computer move task
                await startComputerMoveTask()
                
                
            }
            else if (!moveResult.success) {
                showMessage(pTextFull: moveResult.returnMessage, pTextShort: "", pDurationms: Constants.TOAST_SHORT)
            }
            
            // Clear the move selected
            await move.clear()
        }
        
    }
    
    
    /// Start computer move task
    private func startComputerMoveTask() async {
            
        let arrangeBoardEnabled = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
        let computerPlayerEnabled = ParameterDataService.instance.get(pParameterClass: ParamComputerPlayer.self).enabled
        let computerMoveFirstEnabled = ParameterDataService.instance.get(pParameterClass: ParamComputerMoveFirst.self).enabled
        let computerColour = computerMoveFirstEnabled ? Constants.WHITEPIECE : Constants.BLACKPIECE
        let turnColour = Int(GameRecordDataService.instance.currentGame.getStateActiveColour())
        let boardStatus = Int(GameRecordDataService.instance.currentGame.getStateGameStatus())
        
        if boardStatus == 0 && (!computerMoveProcessing) && (!computerHintProcessing) && (!arrangeBoardEnabled) && computerPlayerEnabled && computerColour == turnColour {
            lockPanel = true
            // Clear the user move since starting computer move
            await move.clear()
            computerMoveProcessing = true
            activityIndicatorVM.enabled = true
        
            let searchOptions = SearchOptions()
            searchOptions.randomiseFirstMove = ParameterDataService.instance.get(pParameterClass: ParamRandomiseFirstMove.self).enabled
            
            let limitSkillLevel = Int(ParameterDataService.instance.get(pParameterClass: ParamLimitSkillLevel.self).level)
            let strengthSetting = Constants.strengthList[min(max(limitSkillLevel, 0), Constants.strengthList.endIndex - 1)]
            
            searchOptions.limitSkillLevel = Int32(strengthSetting.skillLevel)
            
            let limitAdvancedEnabled = ParameterDataService.instance.get(pParameterClass: ParamLimitAdvanced.self).enabled
            if limitAdvancedEnabled {
                searchOptions.limitDepth = Int32(ParameterDataService.instance.get(pParameterClass: ParamLimitDepth.self).depth)
                let limitMoveDuration: Int = ParameterDataService.instance.get(pParameterClass: ParamLimitMoveDuration.self).moveDurationMS
                searchOptions.limitNodes = Int32(limitMoveDuration > 0 ? Constants.NODELIMIT_HIGH : Constants.NODELIMIT_STANDARD)
                searchOptions.limitMoveDuration = Int32(limitMoveDuration)
                searchOptions.limitThreads = Int32(ParameterDataService.instance.get(pParameterClass: ParamLimitThreads.self).threads)
            } else {
                searchOptions.limitDepth = Int32(strengthSetting.depth)
                searchOptions.limitNodes = Int32(Constants.NODELIMIT_STANDARD)
                searchOptions.limitMoveDuration = Int32(strengthSetting.timeLimitms)
                searchOptions.limitThreads = ProcessInfo.processInfo.activeProcessorCount > 1 ? Int32(ProcessInfo.processInfo.activeProcessorCount - 1) : Int32(1)
            }
            
            let topMove = await srchActor.searchStart(pSearchOptions: searchOptions)
        
            if (!topMove.cancelled) && (topMove.error == 0) {
                let boardBeforeMove = GameRecordDataService.instance.getCurrentGame()
                let gameStatusBeforeMove = GameRecordDataService.instance.currentGame.getStateGameStatus()
                let moveResult: MoveResult = GameRecordDataService.instance.currentGame.move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, true, true) as? MoveResult ?? MoveResult()
                if moveResult.success {
                    
                    // Do animation
                    let moveSpeedIndex: Int = ParameterDataService.instance.get(pParameterClass: ParamMoveSpeed.self).speed
                    if Constants.moveSpeedSeconds.indices.contains(moveSpeedIndex) {
                        let boardAfterMove = GameRecordDataService.instance.getCurrentGame()
                        let moveAnimationList = boardAnimation.createAnimationList(pBoardRecA: boardBeforeMove, pBoardRecB: boardAfterMove)
                        await startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDurationSeconds: Constants.moveSpeedSeconds[moveSpeedIndex])
                    }
                    
                    BoardSquareDataService.instance.update(pTilePanelVM: self.tilePanelVM, pRecord: GameRecordDataService.instance.getCurrentGame())
                       
                    playPieceMoveSoundEffect()
                    
                    // Record the game state
                    await recordCurrentGameState()
                    
                    // Update score if checkmate occurred
                    if gameStatusBeforeMove == BoardStatusEnum.Ready.rawValue && GameRecordDataService.instance.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.rawValue {
                        await afterCheckMate(pBoard: GameRecordDataService.instance.currentGame)
                    }
                }
            }
            else {
                if topMove.error > 0 {
                    showMessage(pTextFull: topMove.errorMessage, pTextShort: "", pDurationms: Constants.TOAST_LONG)
                }
            }
            
            self.activityIndicatorVM.enabled = false
            
            computerMoveProcessing = false
            lockPanel = false
           
        }
           
    }
    
    /// Find the best move for a board layout
    private func startHintTask(pRecord: GameRecordArray) async {
        hintBoardActor.hintBoard.setBoardArray(pRecord.boardArray)
        hintBoardActor.hintBoard.setStateArray(pRecord.stateArray)
        
        let arrangeBoardEnabled = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
        let boardStatus = Int(hintBoardActor.hintBoard.getStateGameStatus())
        
        if boardStatus == 0 && (!computerMoveProcessing) && (!computerHintProcessing) && (!arrangeBoardEnabled) {
            lockPanel = true
            computerHintProcessing = true
            
            activityIndicatorVM.enabled = true
            
            let searchOptions = SearchOptions()
            searchOptions.randomiseFirstMove = false
            
        
            searchOptions.limitSkillLevel = Int32(Constants.strengthList[Constants.strengthList.endIndex - 1].skillLevel)
            searchOptions.limitDepth = Int32(Constants.strengthList[Constants.strengthList.endIndex - 1].depth)
            searchOptions.limitNodes = Int32(Constants.NODELIMIT_STANDARD)
            searchOptions.limitMoveDuration = Int32(Constants.strengthList[Constants.strengthList.endIndex - 1].timeLimitms)
            searchOptions.limitThreads = ProcessInfo.processInfo.activeProcessorCount > 1 ? Int32(ProcessInfo.processInfo.activeProcessorCount - 1) : Int32(1)
            
            let topMove = await hintBoardActor.searchStart(pSearchOptions: searchOptions)
        
            if (!topMove.cancelled) && (topMove.error == 0) {
                let topMoveBits: UInt64 = (Constants.BITMASK >> topMove.moveFromIndex) | (Constants.BITMASK >> topMove.moveToIndex)
                if let boardColourIndex = Constants.darkSquareColourArray.firstIndex(of: ParameterDataService.instance.get(pParameterClass: ParamColourDarkSquares.self).argb()) {
                    let highlightColour = Constants.hintColourArray[boardColourIndex]
                    tilePanelVM.setHighLightFullFadeOut(pBits: topMoveBits, pColour: highlightColour)
                    
                    // On mac only read the text so message doesn't cover the highlighted move
                    #if os(macOS)
                        readText(pText: "Best move hint")
                    #else
                        showMessage(pTextFull: "Best move hint", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                    #endif
                }
            }
            else {
                if topMove.error > 0 {
                    showMessage(pTextFull: topMove.errorMessage, pTextShort: "", pDurationms: Constants.TOAST_LONG)
                }
            }
            
            self.activityIndicatorVM.enabled = false
            computerHintProcessing = false
            lockPanel = false
           
        } else {
            if boardStatus != 0 {
                showMessage(pTextFull: "Unable to show a hint as the game has finished.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
            else if arrangeBoardEnabled {
                showMessage(pTextFull: "Unable to show a hint in edit mode.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
            else if computerMoveProcessing {
                showMessage(pTextFull: "Unable to show a hint as processing a move.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
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
    private func startPieceAnimation(pLockPanel: Bool, pAnimationList: [TileAnimationInstruction], pDurationSeconds: Double) async {
        
        if pLockPanel {
            lockPanel = true
        }
        
        // Do the animation
        await tileAnimationVM.runAnimation(pAnimationList: pAnimationList, pTilePanelVM: self.tilePanelVM, pDuration: pDurationSeconds)
        
        
        if pLockPanel {
            lockPanel = false
        }
             
    }
    
    
    
    
    /// Show message
    /// - Parameters:
    ///   - pTextFull: Full message to display
    ///   - pTextShort: A shorter form of the message for the text reader
    ///   - pDuration: Length of time to show message
    internal func showMessage(pTextFull: String, pTextShort: String, pDurationms: Int) {
        Task(priority: .userInitiated) {
            if pTextFull.trimmingCharacters(in: .whitespacesAndNewlines) != "" {
                // Read message
                if pTextShort.trimmingCharacters(in: .whitespacesAndNewlines) == "" {
                    readText(pText: pTextFull)
                }
                else {
                    readText(pText: pTextShort)
                }
                
                // Show toast message
                Device.instance.toastMessage = pTextFull
                try? await Task.sleep(nanoseconds: UInt64(pDurationms) * 1000000)
                Device.instance.toastMessage = ""
                
            }
        }
    }
    
    
    /// Converts a text string to speech
    /// - Parameter pText: The text to read
    private func readText(pText: String) {
        let soundEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamSoundRead.self).enabled
        
        if soundEnabled {
            let utterance = AVSpeechUtterance(string: pText)
            BoardViewModel.textReader.speak(utterance)
        }
    }
    
    
    /// Set the board to the max record
    private func navigateMaxRecord() async {
        let maxId = GameRecordDataService.instance.getMaxId()
        
        await navigateGameRecord(pRecId: maxId, pAnimate: false)
        
        // Refresh shake animation
        //TODO: boardLayout.shakeRefresh()
        
        // Load the move navigator
        let navParam = ParameterDataService.instance.get(pParameterClass: ParamNavigator.self)
        loadNavigator(pEnabled: navParam.enabled)
    }
    
    /// Navigate to requested game record
    /// - Parameters:
    ///   - pRecId: The record Id to which to navigate
    ///   - pAnimate: Animate the navigation or not
    func navigateGameRecord(pRecId: Int, pAnimate: Bool) async {
            
        if pRecId > 0 {
        
            // Update board displayed with requested record
            if let updateBoard = GameRecordDataService.instance.get(pId: pRecId) {
                self.endPieceAnimation()
    
                //Do Animation
                if pAnimate {
                    if let oldBoard: GameRecordArray = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue) {
                        
                        // Do animation
                        let moveSpeedIndex: Int = ParameterDataService.instance.get(pParameterClass: ParamMoveSpeed.self).speed
                        if Constants.moveSpeedSeconds.indices.contains(moveSpeedIndex) {
                            let moveAnimationList = boardAnimation.createAnimationList(pBoardRecA: oldBoard, pBoardRecB: updateBoard)
                            await startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDurationSeconds: Constants.moveSpeedSeconds[moveSpeedIndex])
                        }
                        
                        BoardSquareDataService.instance.update(pTilePanelVM: self.tilePanelVM, pRecord: updateBoard)
                        BoardSquareDataService.instance.gameRecordCurrentValue = pRecId
                        updateBoardIndicators(pRecord: updateBoard)
                        
                    }
                }
                else {
                    BoardSquareDataService.instance.update(pTilePanelVM: self.tilePanelVM, pRecord: updateBoard)
                    BoardSquareDataService.instance.gameRecordCurrentValue = pRecId
                    updateBoardIndicators(pRecord: updateBoard)
                }
                
                await move.clear()
            }
        }
    }
    
    
    /// Records the current state of the game
    private func recordCurrentGameState() async {
        let success = GameRecordDataService.instance.recordGameState(pWhiteClockOffset: 0, pBlackClockOffset: 0)
        if (success > 0) {
            
            // Ensure game record position is set to max value
            await navigateMaxRecord()
        }
        
        
    }
    
    
    /// Starts a new game
    func newGame() async {
        stopSearchJob()
        endPieceAnimation()
    
        lockPanel = true
        GameRecordDataService.instance.reset()
        await move.clear()
        await navigateMaxRecord()
        lockPanel = false
    }
    
    
    /// Undo last move
    func undoMove() async {
        
        // Just close the pawn prompt if it is open
        guard await pawnPromotionVM.getWaitCount() == 0 else {
            stopSearchJob()
            return
        }
        
        stopSearchJob()
        endPieceAnimation()
            
        if let boardBeforeUndo = GameRecordDataService.instance.get() {
            if GameRecordDataService.instance.undo() {
                lockPanel = true
                if let boardAfterUndo = GameRecordDataService.instance.get() {
                    let moveSpeedIndex: Int = ParameterDataService.instance.get(pParameterClass: ParamMoveSpeed.self).speed
                    if Constants.moveSpeedSeconds.indices.contains(moveSpeedIndex) {
                        let moveAnimationList = self.boardAnimation.createAnimationList(pBoardRecA: boardBeforeUndo, pBoardRecB: boardAfterUndo)
                        await startPieceAnimation(pLockPanel: true, pAnimationList: moveAnimationList, pDurationSeconds: Constants.moveSpeedSeconds[moveSpeedIndex])
                    }
                }
            }
        }
        
        await move.clear()
        await navigateMaxRecord()
        
        lockPanel = false
        
    }
    
    
    /// Ends any running move job
    func stopSearchJob() {
        
        Task(priority: .userInitiated) {
            await pawnPromotionVM.cancel()
            await move.clear()
        }
        
        hintBoardActor.cancelSearch()
        srchActor.cancelSearch()
    }
    
    
    /// Shows a board message alert
    /// - Parameters:
    ///   - pTitle: The title
    ///   - pMessage: Message content
    private func showBoardMessageAlert(pTitle: String, pMessage: String, pAlertType: BoardMessageAlertViewModel.alertTypeEnum) {
        if !(pTitle == "" && pMessage == "") {
            readText(pText: "\(pTitle) \(pMessage)")
            boardMessageAlertVM.show(pTitle, pMessage, pAlertType)
        }
    }
    
    /// Ends animations
    private func endPieceAnimation() {
        tileAnimationVM.clear()
        BoardSquareDataService.instance.showAll(pTilePanelVM: tilePanelVM)
    }
    
    
    /// Updates all the board indicators
    /// - Parameter pRecord: The game record array
    func updateBoardIndicators(pRecord: GameRecordArray) {
        _ = updateGameMessage(pRecord: pRecord)
        updateDirectionIndicator(pRecord: pRecord)
        updateCheckIndicator(pRecord: pRecord)
    
    }
    
    
    /// Update direction indicator
    /// - Parameter pRecord: The game record array
    private func updateDirectionIndicator(pRecord: GameRecordArray) {
        let board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)
        directionIndicatorVM.direction = Int(board.getStateActiveColour())
    }
    
    
    /// Set the check indicator
    /// - Parameter pRecord: The game record array
    private func updateCheckIndicator(pRecord: GameRecordArray) {
        let board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)
        
        // Circles the king if in check
        if board.getStateActiveColour() != 0  {
            let kingCheck = board.isKingCheck(board.getStateActiveColour())
            
            if kingCheck {
                let kingIndex = board.getKingIndex(board.getStateActiveColour())
                if kingIndex >= 0 {
                    tilePanelVM.setCheckIndicator(pKingIndex: Int(kingIndex))
                    
                    if board.getStateGameStatus() == BoardStatusEnum.Ready.rawValue {
                        showMessage(pTextFull: "Check", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                    }
                }
            }
            else {
                tilePanelVM.setCheckIndicator(pKingIndex: -1)
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
        
        
        showBoardMessageAlert(pTitle: title, pMessage: "", pAlertType: BoardMessageAlertViewModel.alertTypeEnum.Ok)
    
        
        return gameFinished
    }
    
    
    
    
    /// Resigns the current game
    func resignGame() async {

            let arrangeBoardEnabled = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled
            
            if arrangeBoardEnabled {
                showMessage(pTextFull: "Cannot resign in edit mode.", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                return
            }
            
            let computerPlayerEnabled = ParameterDataService.instance.get(pParameterClass: ParamComputerPlayer.self).enabled
            let computerMoveFirstEnabled = ParameterDataService.instance.get(pParameterClass: ParamComputerMoveFirst.self).enabled
            let computerColour = computerMoveFirstEnabled ? Constants.WHITEPIECE : Constants.BLACKPIECE
            let turnColour = GameRecordDataService.instance.currentGame.getStateActiveColour()
            
            if computerPlayerEnabled && (turnColour == computerColour) {
                showMessage(pTextFull: "Cannot resign as it is not your turn.", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
                return
            }
            
            // Ensure game record is set to the latest
            await navigateMaxRecord()
            
            let status = GameRecordDataService.instance.currentGame.getStateGameStatus()
            
            // Only resign if game is not already finished
            if status == BoardStatusEnum.Ready.rawValue && GameRecordDataService.instance.currentGame.getStateFullMoveCount() > 0 {
                // Stop task if running
                stopSearchJob()
                
                // End any animation if running
                
                endPieceAnimation()
                
                
                // Set the state of the game to resign
                GameRecordDataService.instance.currentGame.setStateGameStatus(Int32(BoardStatusEnum.Resigned.rawValue))
               
                // Record the current game state
                await recordCurrentGameState()
                   
                
                // Do animation, display message
                let kingFallIndex = Int(GameRecordDataService.instance.currentGame.getKingIndex(GameRecordDataService.instance.currentGame.getStateActiveColour()))
                let kingFallSeq = boardAnimation.createAnimationFall(pIndex: kingFallIndex, pTilePanelVM: tilePanelVM)
                await startPieceAnimation(pLockPanel: true, pAnimationList: kingFallSeq, pDurationSeconds: 0.8)
               
                
            }
            else {
                showMessage(pTextFull: "Resigning not available at this stage of the game", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
        
    }
    
    /// Actions to perform after checkmate occurs
    private func afterCheckMate(pBoard: KaruahChessEngineC) async {
        
        let computerPlayerEnabled = ParameterDataService.instance.get(pParameterClass: ParamComputerPlayer.self).enabled
        let computerMoveFirstEnabled = ParameterDataService.instance.get(pParameterClass: ParamComputerMoveFirst.self).enabled
        let levelAutoEnabled = ParameterDataService.instance.get(pParameterClass: ParamLevelAuto.self).enabled
        
        var humanWinAgainstComputer = false
        if pBoard.getStateActiveColour() == Constants.BLACKPIECE {
            if computerPlayerEnabled && !computerMoveFirstEnabled {
                humanWinAgainstComputer = true
            }
            
            await doKingFallAnimation(pWinner: Constants.WHITEPIECE)
        }
        else if pBoard.getStateActiveColour() == Constants.WHITEPIECE {
            if computerPlayerEnabled && computerMoveFirstEnabled {
                humanWinAgainstComputer = true
            }
            await doKingFallAnimation(pWinner: Constants.BLACKPIECE)
        }
        
        if levelAutoEnabled && humanWinAgainstComputer {
            let nextSkillLevel = EngineSettingsViewModel.instance.limitSkillLevel + 1
            if 0 ..< Constants.strengthList.endIndex ~= nextSkillLevel {
                // Update using the view model so the level indicater on the main screen gets updated
                EngineSettingsViewModel.instance.limitSkillLevel = nextSkillLevel
                showMessage(pTextFull: "Congratulations! You have now progressed to the next level. The engine playing strength is now set to \(Constants.strengthList[nextSkillLevel].label).", pTextShort: "", pDurationms: Constants.TOAST_EXTRALONG)
            }
        }
        
        
    }
    
    // Do the endgame animation
    private func doKingFallAnimation(pWinner: Int) async {
        if (pWinner == Constants.WHITEPIECE || pWinner == Constants.BLACKPIECE) {
            let kingFallIndex: Int  = Int(GameRecordDataService.instance.currentGame.getKingIndex(Int32(pWinner * -1)))
            let kingFallSeq = boardAnimation.createAnimationFall(pIndex: kingFallIndex, pTilePanelVM: tilePanelVM)
            await startPieceAnimation(pLockPanel: true, pAnimationList: kingFallSeq, pDurationSeconds: 0.5)
        }
    }
    
    /// Increment the board rotation
    func rotateClick() {
        endPieceAnimation()
        let currentRotationParam = ParameterDataService.instance.get(pParameterClass: ParamRotateBoard.self)
        var newRotation = currentRotationParam.value + 90
        if newRotation >= 360 {
            newRotation = 0
        }
        currentRotationParam.value = newRotation
        _ = ParameterDataService.instance.set(pObj: currentRotationParam)
        boardRotation = Double(newRotation)
        
    }
    
    
    /// Flips the direction of the current game
    func switchDirection() async {
        stopSearchJob()
        
        let board = KaruahChessEngineC()
        if let record = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue) {
            board.setBoardArray(record.boardArray)
            board.setStateArray(record.stateArray)
            
            // Flip direction
            board.setStateActiveColour(board.getStateActiveColour() * (-1))
            record.stateArray = board.getStateArraySafe()
            _ = GameRecordDataService.instance.updateGameState(pGameRecordArray: record)
            updateBoardIndicators(pRecord: record)
        }
    }
    
    /// Saves a game to a file
    func saveGame() async {
        
        // End any move jobs that might be running
        stopSearchJob()
        
        // Create a filename string
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "d-MMM-yyyy HHmm"
        let filename: String = "KaruahChess-Game-" + dateFormatter.string(from: Date()) + ".gz"
      
        if let fileData: Data = ExportDB().export(pExportType: ExportDB.ExportTypeEnum.GameXML) {
            
            // Compress the data and export
            let gzFileData = CompressionC().gZip(fileData)
            exportFileDocument = ExportFileDocument(pFileName: filename, pFileData: gzFileData)
            showFileExporter = true
        }
    }
    
    /// Loads a game from a file
    func loadGame(_ pUrl: URL) async {
        
        stopSearchJob()

        if let fileData = try? Data(contentsOf: pUrl) {
            // Uncompress the file if it is compressed
            let compressor = CompressionC()
            let uncompressedFile = compressor.isGZip(fileData) ? compressor.gUnZip(fileData) : fileData
            
            // Do the import
            let importDB = ImportDB()
            let result = importDB.importData(uncompressedFile, ImportDB.ImportTypeEnum.GameXML)
            if result > 0 {
                Task(priority: .userInitiated) {
                    await navigateMaxRecord()
                }
                showMessage(pTextFull: "Game successfully loaded", pTextShort: "", pDurationms: Constants.TOAST_SHORT)
            }
            else if result == -1 {
                showMessage(pTextFull: "Invalid file", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
            else {
                showMessage(pTextFull: "Unable to load game. No records found", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
        } else
        {
            showMessage(pTextFull: "Error loading file", pTextShort: "", pDurationms: Constants.TOAST_LONG)
        }
        
    }
    
    /// Refreshes the tile size
    func refreshTileSize(pCoordPanelEnabled: Bool, pNavigationEnabled: Bool) {
    
        #if os(iOS)
        Device.instance.navigationHeight = pNavigationEnabled ? 58 : 0
        Device.instance.boardCoordPadding = pCoordPanelEnabled ? 18 : 0
        SceneDelegate.refreshTileSize()
        #elseif os(macOS)
        Device.instance.navigationHeight = pNavigationEnabled ? 35 : 0
        Device.instance.boardCoordPadding = pCoordPanelEnabled ? 14 : 0
        MainWindowController.refreshTileSize()
        #endif
    }
    
    /// Loads the move navigator
    func loadNavigator(pEnabled: Bool) {
        if pEnabled {
            navigatorVM.recordIdList = GameRecordDataService.instance.getAllRecordIDList()
            navigatorVM.enabled = true
        }
        else {
            navigatorVM.enabled = false
        }
    }
    
    /// Play piece move sound effect
    private func playPieceMoveSoundEffect() {
        let soundEffectEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamSoundEffect.self).enabled
        
        if soundEffectEnabled {
            if audioPlayerPieceMoveSound == nil {
                // Load the sound file if it is not already loaded
                if let url = Bundle.main.url(forResource: "piecesound", withExtension: "wav") {
                    audioPlayerPieceMoveSound = try? AVAudioPlayer(contentsOf: url)
                    audioPlayerPieceMoveSound?.prepareToPlay()
                }
            }
            
            audioPlayerPieceMoveSound?.play()
        }
    }
    
    /// Show hint
    func showHint() async {
        if let record = GameRecordDataService.instance.get(pId: BoardSquareDataService.instance.gameRecordCurrentValue) {
            await startHintTask(pRecord: record)
        }
    }
    
    
}

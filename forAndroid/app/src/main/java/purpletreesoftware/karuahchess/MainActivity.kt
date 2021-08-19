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

package purpletreesoftware.karuahchess

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.common.afterMeasured
import purpletreesoftware.karuahchess.common.spToPx
import purpletreesoftware.karuahchess.customcontrol.*
import purpletreesoftware.karuahchess.database.DatabaseHelper
import purpletreesoftware.karuahchess.database.ExportDB
import purpletreesoftware.karuahchess.database.ImportDB
import purpletreesoftware.karuahchess.databinding.ActivityMainBinding
import purpletreesoftware.karuahchess.engine.KaruahChessEngineC
import purpletreesoftware.karuahchess.engine.SearchOptions
import purpletreesoftware.karuahchess.model.boardsquare.BoardSquareDataService
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.*
import purpletreesoftware.karuahchess.rules.BoardAnimation
import purpletreesoftware.karuahchess.rules.Move
import purpletreesoftware.karuahchess.rules.Move.HighlightEnum
import purpletreesoftware.karuahchess.sound.TextReader
import purpletreesoftware.karuahchess.viewmodel.*
import purpletreesoftware.karuahchess.voice.VoiceRecognition
import java.io.InputStream
import android.media.AudioAttributes

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), TilePanel.OnTilePanelInteractionListener, PieceEditTool.OnPieceEditToolInteractionListener, VoiceRecognition.OnVoiceRecognitionInteractionListener, PawnPromotion.OnPawnPromotionInteractionListener {

    private var _dbHelper: DatabaseHelper? = null
    private lateinit var _move: Move
    private var _userInteracted: Boolean = false
    private var _lockPanel = false
    private var _computerMoveProcessing = false
    private lateinit var _textReader: TextReader
    private var _voiceRecognition : VoiceRecognition? = null
    private var _editMenuItem: MenuItem? = null
    private val _bufferBoard = KaruahChessEngineC()
    private val _pieceChannel = Channel<Int>()
    private val _mainjob = SupervisorJob()
    private val _uiScope = CoroutineScope(Dispatchers.Main + _mainjob)
    private var sndPool: SoundPool? = null
    private var sndMove: Int = 0
    lateinit var binding: ActivityMainBinding

    // Public variables
    enum class MoveTypeEnum(val value: Int) { None(0), Normal(1), EnPassant(2), Castle(3), Promotion(4) }
    enum class BoardStatusEnum(val value: Int) { Ready(0), Checkmate(1), Stalemate(2), Resigned(3) }
    enum class PawnPromotionEnum(val value: Int) { Knight(2), Bishop(3), Rook(4), Queen(5) }
    enum class PieceTypeEnum (val value: Int){ Empty(0), Pawn(1), Knight(2), Bishop(3), Rook(4), Queen(5), King(6) }

    // View Models
    private lateinit var castlingRightsVM: CastlingRightsViewModel
    private lateinit var aboutVM: AboutViewModel
    private lateinit var pawnPromotionVM: PawnPromotionViewModel
    private lateinit var pieceEditToolVM: PieceEditToolViewModel


    // File Picker
    private val loadGameRecordFromFileUriLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri: Uri? ->
        if (fileUri != null) loadGameRecordFromFileUri(fileUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Set content view
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarTop)

        // database helper
        _dbHelper = DatabaseHelper.getInstance(this)

        // Text reader
        _textReader = TextReader(this)
        
        // View Models
        aboutVM = ViewModelProvider(this).get(AboutViewModel::class.java)
        castlingRightsVM = ViewModelProvider(this).get(CastlingRightsViewModel::class.java)
        pawnPromotionVM = ViewModelProvider(this).get(PawnPromotionViewModel::class.java)
        pieceEditToolVM = ViewModelProvider(this).get(PieceEditToolViewModel::class.java)

        // Set floating action button orientation
        val fabLayout = binding.floatingActionButtonLayout
        val currentOrientation :Int = this.resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) { fabLayout.orientation = LinearLayout.VERTICAL }
        else { fabLayout.orientation = LinearLayout.HORIZONTAL }

        // Last move action button
        binding.showLastMoveAction.setOnClickListener {showLastMove() }

        // Set rotate action button
        binding.rotateAction.setOnClickListener{
            endPieceAnimation()
            val currentRotationParam = ParameterDataService.get(ParamRotateBoard::class.java)
            var newRotation = currentRotationParam.value + 90
            if (newRotation >= 360) newRotation = 0
            currentRotationParam.value = newRotation
            ParameterDataService.set(currentRotationParam)
            rotateBoard(newRotation)
        }

        // Set engine settings button
        binding.engineSettingsAction.setOnClickListener { showEngineSettingsDialog() }
        refreshEngineSettingsLevelIndicator()

        // Set board layout listener
        binding.boardPanelLayout.setTilePanelInteractionListener(this)

        // Move
        _move = Move(binding.boardPanelLayout)

        // Voice command visibility
        val voiceParam = ParameterDataService.get(ParamVoiceCommand::class.java)
        val voiceEnabled = voiceCommandShow(voiceParam.enabled, false)
        if(voiceParam.enabled && !voiceEnabled) {
            // Set voice command to false if voice command was enabled but fails to show
            voiceParam.enabled = false
            ParameterDataService.set(voiceParam)
        }

        // Sounds
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        sndPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        sndMove = sndPool?.load(this,R.raw.piecesound,1) ?: 0

        // Initialise board
        BoardSquareDataService.update(binding.boardPanelLayout, GameRecordDataService.getCurrentGame())

        val mainFrame = binding.mainFrame
        mainFrame.afterMeasured {
            // Draw the board
            val showCoord = ParameterDataService.get(ParamBoardCoord::class.java).enabled
            drawBoard(showCoord)

            // Move progress bar visibility
            binding.moveProgressBar.visibility = View.GONE

            // Apply board colour
            applyBoardColour()

            // Set shake
            val arrangeBoardEnabled = ParameterDataService.get(ParamArrangeBoard::class.java).enabled
            binding.boardPanelLayout.shake(arrangeBoardEnabled)

            // Set current record position
            _uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)

        menu.findItem(R.menu.menu_main)

        // Set initial values
        menu.findItem(R.id.action_highlight).isChecked = ParameterDataService.get(ParamMoveHighlight::class.java).enabled
        _editMenuItem = menu.findItem(R.id.action_edit)
        _editMenuItem?.isChecked = ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        menu.findItem(R.id.action_coordinates).isChecked = ParameterDataService.get(ParamBoardCoord::class.java).enabled
        menu.findItem(R.id.action_voice).isChecked = ParameterDataService.get(ParamVoiceCommand::class.java).enabled
        menu.findItem(R.id.action_navigator).isChecked = ParameterDataService.get(ParamNavigator::class.java).enabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_newgame -> {
                newGame()
                true
            }
            R.id.action_resign -> {
                resignGameDialog()
                true
            }
            R.id.action_highlight -> {
                val highlight = ParameterDataService.get(ParamMoveHighlight::class.java)
                highlight.enabled = !item.isChecked
                item.isChecked = highlight.enabled
                ParameterDataService.set(highlight)
                if(highlight.enabled) showMessage("${item.title} is enabled", "", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled", "", Toast.LENGTH_SHORT)
                true
            }
            R.id.action_enginesettings -> {
                showEngineSettingsDialog()
                true
            }
            R.id.action_edit -> {
                editBoardToggle()
                true
            }
            R.id.action_undo -> {
                _uiScope.launch(Dispatchers.Main) {
                    undoMove()
                }
                true
            }
            R.id.action_switchdirection -> {
                //Cancel any move tasks
                stopMoveJob()

                // Flip direction
                val board = KaruahChessEngineC()
                val record = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
                if(record != null) {
                    board.setBoardArray(record.boardArray)
                    board.setStateArray(record.stateArray)
                    // Flip direction
                    board.setStateActiveColour(board.getStateActiveColour() * (-1))
                    record.stateArray = board.getStateArray()
                    GameRecordDataService.updateGameState(record)
                    updateBoardIndicators(record)
                }

                true
            }
            R.id.action_soundcoloursettings -> {
                showSoundColourSettingsDialog()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            R.id.action_coordinates -> {
                val coordinates = ParameterDataService.get(ParamBoardCoord::class.java)
                coordinates.enabled = !item.isChecked
                item.isChecked = coordinates.enabled
                ParameterDataService.set(coordinates)
                if(coordinates.enabled) showMessage("${item.title} is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled","", Toast.LENGTH_SHORT)

                drawBoard(coordinates.enabled)
                true
            }
            R.id.action_navigator -> {
                val navParam = ParameterDataService.get(ParamNavigator::class.java)
                navParam.enabled = !item.isChecked
                item.isChecked = navParam.enabled
                ParameterDataService.set(navParam)

                loadNavigator(navParam.enabled)

                if(navParam.enabled) showMessage("${item.title} is enabled", "", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled", "", Toast.LENGTH_SHORT)
                true
            }
            R.id.action_importpgn -> {
                showImportPGNDialog()
                true
            }
            R.id.action_load -> {
                loadGameRecordFromFileUriLauncher.launch("application/gzip")
                true
            }
            R.id.action_save -> {
                saveGameRecordAsFile()
                true
            }
            R.id.action_voice -> {
                val voiceCmd = ParameterDataService.get(ParamVoiceCommand::class.java)
                val isEnabled = voiceCommandShow(!item.isChecked, true)
                voiceCmd.enabled = isEnabled
                item.isChecked = voiceCmd.enabled
                ParameterDataService.set(voiceCmd)
                true
            }
            R.id.action_voicehelp -> {
                showVoiceHelp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * Show last move made
     */
    private fun showLastMove() {
        val currentBoard = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
        val previousBoard = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue - 1)
        val lastChanges = GameRecordDataService.getBoardSquareChanges(currentBoard, previousBoard)

        if (lastChanges != 0uL) {
            binding.boardPanelLayout.setRedHighlightFullFadeOut(lastChanges)
            showMessage("Last move","", Toast.LENGTH_SHORT)
        }
        else {
            showMessage("No last move to show.","", Toast.LENGTH_SHORT)
        }

    }

    /**
     * Listens for tile move action events - used for dragging pieces
     */
    override fun onTileMoveAction(pFromTile: Tile?, pToTile: Tile?) {
        val arrangeBoardEnabled = ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        _userInteracted = true
        _move.Clear()


        if (pFromTile != null && pToTile != null && !_lockPanel) {
            if(!arrangeBoardEnabled) {
                _uiScope.launch(Dispatchers.Main) {
                    userMoveAdd(pFromTile, false)
                    userMoveAdd(pToTile, false)
                }
            }
            else {
                arrangeUpdate(pFromTile.index, pToTile.index)
            }


        }

    }

    /**
     * Listens for tile click events
     */
    override fun onTileClick(pTile: Tile?) {
        val arrangeBoardEnabled = ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        val gameFinished: Boolean = GameRecordDataService.currentGame.getStateGameStatus() != BoardStatusEnum.Ready.value
        _userInteracted = true

        if (pTile != null && !gameFinished && !_lockPanel && !arrangeBoardEnabled) {

            _uiScope.launch(Dispatchers.Main) {
                userMoveAdd(pTile, true)
            }
        }
        else if (pTile != null && arrangeBoardEnabled) {
            if (pTile.spin == Constants.WHITE_KING_SPIN || pTile.spin == Constants.BLACK_KING_SPIN) {
                showCastlingRightsDialog(pTile.spin)
            }
            else {
                showPieceEditToolDialog(pTile)
            }
        }

        if (gameFinished) {
            showMessage("Cannot move as game has finished.", "", Toast.LENGTH_LONG)
        }
    }

    /**
     * Updates the piece type on a square. Used for editing the board.
     */
    fun arrangeUpdate(pFen: Char, pToIndex: Int) {
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                _bufferBoard.setBoardArray(record.boardArray)
                _bufferBoard.setStateArray(record.stateArray)

                val mResult = _bufferBoard.arrangeUpdate(pFen, pToIndex)
                if (mResult.success) {
                    record.boardArray = _bufferBoard.getBoardArray()
                    record.stateArray = _bufferBoard.getStateArray()

                    BoardSquareDataService.update(binding.boardPanelLayout,record)
                    GameRecordDataService.updateGameState(record)
                } else {
                    showMessage(mResult.returnMessage,"", Toast.LENGTH_SHORT)
                }

                // Clear the move selected
                _move.Clear()
            }
        }
    }

    /**
     * Moves a piece from one square to another
     */
    fun arrangeUpdate(pFromIndex: Int, pToIndex: Int) {
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                _bufferBoard.setBoardArray(record.boardArray)
                _bufferBoard.setStateArray(record.stateArray)
                val mResult = _bufferBoard.arrange(pFromIndex, pToIndex)
                if (mResult.success) {
                    record.boardArray = _bufferBoard.getBoardArray()
                    record.stateArray = _bufferBoard.getStateArray()
                    BoardSquareDataService.update(binding.boardPanelLayout,record)
                    GameRecordDataService.updateGameState(record)
                } else {
                    showMessage(mResult.returnMessage,"", Toast.LENGTH_SHORT)
                }

                // Clear the move selected
                _move.Clear()
            }
        }
    }

    /**
     * Add a tile to the move when user clicks a square
     */
    private suspend fun userMoveAdd(pTile: Tile, pAnimate: Boolean) {

        val moveHighlightEnabled = ParameterDataService.get(ParamMoveHighlight::class.java).enabled

        // Ensure game record is set to the latest
        val maxRecId = GameRecordDataService.getMaxId()
        if (BoardSquareDataService.gameRecordCurrentValue != maxRecId) {
            navigateMaxRecord()
            _move.Clear()
            return
        }

        // Select highlight mode
        val highlight = if(moveHighlightEnabled) HighlightEnum.MovePath else HighlightEnum.Select

        // Create proposed move
        val moveSelected = _move.Add(pTile.index, GameRecordDataService.currentGame, highlight)

        // Restart the computer move (if required)
        startComputerMoveTask()

        if (moveSelected) {
            val boardBeforeMove = GameRecordDataService.getCurrentGame()

            // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
            var promotionPiece: Int = PawnPromotionEnum.Queen.value // default
            if (GameRecordDataService.currentGame.isPawnPromotion(_move.fromIndex, _move.toIndex)) {
                promotionPiece = showPawnPromotionDialog(GameRecordDataService.currentGame.getStateActiveColour())
            }

            val gameStatusBeforeMove = GameRecordDataService.currentGame.getStateGameStatus()
            val moveResult = GameRecordDataService.currentGame.move(_move.fromIndex, _move.toIndex, promotionPiece, true, true)

            if (moveResult.success) {

                if (moveResult.returnMessage != "") {
                    showMessage(moveResult.returnMessage,"", Toast.LENGTH_SHORT)
                }
                else {
                    val ssml = getBoardSquareSSML(moveResult.moveDataStr)
                    showMessage(ssml, "", Toast.LENGTH_SHORT)
                }

                val boardAfterMove = GameRecordDataService.getCurrentGame()

                if(pAnimate) {
                    val moveAnimationList = BoardAnimation.createAnimationList(
                        boardBeforeMove,
                        boardAfterMove,
                        binding.boardPanelLayout,
                        this.applicationContext
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationList, 1200L)
                }

                // Update display
                BoardSquareDataService.update(binding.boardPanelLayout, GameRecordDataService.getCurrentGame())

                // Record game state
                recordCurrentGameState()

                // Piece move sound effect
                playPieceMoveSoundEffect()

                // Update score if checkmate occurred
                if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && GameRecordDataService.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value)
                {
                    val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(GameRecordDataService.currentGame.getStateActiveColour())
                    val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this.applicationContext)
                    startPieceAnimation(true, kingFallSeq, 3000L)
                    afterCheckMate(GameRecordDataService.currentGame)
                }




                // Start computer move task
                startComputerMoveTask()

            }
            else if (!moveResult.success)
            {
               showMessage(moveResult.returnMessage,"", Toast.LENGTH_SHORT)
            }


            // Clear the move selected
            _move.Clear()
        }

        return
    }

    /**
     * Start Computer Move task
     */
    private suspend fun startComputerMoveTask()
    {
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        val computerPlayerEnabled =  ParameterDataService.get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  ParameterDataService.get(ParamComputerMoveFirst::class.java).enabled
        val randomiseFirstMoveEnabled = ParameterDataService.get(ParamRandomiseFirstMove::class.java).enabled
        val limitEngineStrengthELO = ParameterDataService.get(ParamLimitEngineStrengthELO::class.java).eloRating
        val limitAdvancedEnabled = ParameterDataService.get(ParamLimitAdvanced::class.java).enabled
        val limitDepth = ParameterDataService.get(ParamLimitDepth::class.java).depth
        val limitNodes = ParameterDataService.get(ParamLimitNodes::class.java).nodes
        val limitMoveDuration = ParameterDataService.get(ParamLimitMoveDuration::class.java).moveDurationMS
        val limitThreads = ParameterDataService.get(ParamLimitThreads::class.java).threads

        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE

        val turnColour = GameRecordDataService.currentGame.getStateActiveColour()
        val boardStatus = GameRecordDataService.currentGame.getStateGameStatus()

        if (boardStatus == 0 && (!_computerMoveProcessing) && (!arrangeBoardEnabled) && computerPlayerEnabled && computerColour == turnColour) {
            _lockPanel = true

            // Clear the user move since starting computer move
            _move.Clear()

            _computerMoveProcessing = true
            binding.moveProgressBar.visibility = View.VISIBLE

            val searchOptions = SearchOptions()
            searchOptions.randomiseFirstMove = randomiseFirstMoveEnabled
            searchOptions.limitStrengthELO = limitEngineStrengthELO

            if (limitAdvancedEnabled) {
                searchOptions.limitDepth = limitDepth
                searchOptions.limitNodes = limitNodes
                searchOptions.limitMoveDuration = limitMoveDuration
                searchOptions.limitThreads = limitThreads
            } else {
                searchOptions.limitDepth = 10
                searchOptions.limitNodes = 500000000
                searchOptions.limitMoveDuration = 0
                searchOptions.limitThreads = if (Runtime.getRuntime().availableProcessors() > 1) Runtime.getRuntime().availableProcessors() - 1 else 1
            }

            val topMove = withContext(Dispatchers.IO) { GameRecordDataService.currentGame.searchStart(searchOptions) }

            if ((!topMove.cancelled) && (topMove.error == 0)) {
                val boardBeforeMove = GameRecordDataService.getCurrentGame()
                val gameStatusBeforeMove = GameRecordDataService.currentGame.getStateGameStatus()
                val moveResult = GameRecordDataService.currentGame.move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, true, true)
                if (moveResult.success) {

                    // Read Text, show message
                    if (moveResult.returnMessage != "") {
                        showMessage(moveResult.returnMessage,"", Toast.LENGTH_SHORT)
                    }
                    else {
                        val ssml = getBoardSquareSSML(moveResult.moveDataStr)
                        showMessage(ssml, "", Toast.LENGTH_SHORT)
                    }

                    // Do animation
                    val boardAfterMove = GameRecordDataService.getCurrentGame()
                    val moveAnimationList = BoardAnimation.createAnimationList(
                        boardBeforeMove,
                        boardAfterMove,
                        binding.boardPanelLayout,
                        this.applicationContext
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationList, 1200L)


                    // Update display
                    BoardSquareDataService.update(binding.boardPanelLayout, GameRecordDataService.getCurrentGame())

                    // Record game state
                    recordCurrentGameState()

                    // Piece move sound effect
                    playPieceMoveSoundEffect()

                    // do if checkmate occurred
                    if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && GameRecordDataService.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value) {
                        val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(GameRecordDataService.currentGame.getStateActiveColour())
                        val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this.applicationContext)
                        startPieceAnimation(true, kingFallSeq, 3000L)
                        afterCheckMate(GameRecordDataService.currentGame)
                    }


                }
            }
            else {
                if (topMove.error > 0) {
                    showMessage("Invalid board configuration. ${topMove.errorMessage} ", "", Toast.LENGTH_LONG)
                }
            }


            // Unlock panel
            binding.moveProgressBar.visibility = View.GONE
            _computerMoveProcessing = false

            _lockPanel = false

        }

        return
    }


    /**
     * Show message
     */
    fun showMessage(pTextFull: String, pTextShort: String, pDuration: Int) {

        if (pTextFull.trim() != "") {
            if (pTextShort.trim() == "") readText(pTextFull)
            else readText(pTextShort)

            val toast = Toast.makeText(applicationContext, pTextFull, pDuration)
            toast.show()
        }
    }

    /**
     * Converts a text string to speech
     */
    private fun readText(pText: String) {
        val soundRead = ParameterDataService.get(ParamSoundRead::class.java)
        if (_textReader.ready && soundRead.enabled) {
            _textReader.tts?.speak(pText, TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    /**
     * Get SSML for board square id
     */
    private fun getBoardSquareSSML(pMoveDataStr: String): String
    {
        val moveDataStrArray  = pMoveDataStr.split('|')


        // Check move data array is the correct size
        if (moveDataStrArray.size != 4)
        {
            return ""
        }

        val fromIndex: Int = moveDataStrArray[0].toIntOrNull() ?: -1
        val toIndex: Int = moveDataStrArray[1].toIntOrNull() ?: -1
        val origFromSpin: Int = moveDataStrArray[2].toIntOrNull() ?: -1
        val origToSpin: Int = moveDataStrArray[3].toIntOrNull() ?: -1

        // Check from and to indexes are in the correct range
        if(!(fromIndex in 0..63 && toIndex >=0 && toIndex <= 63))
        {
            return ""
        }

        val ssml: String = if (origToSpin != 0)
        {
            GetPieceSpinSSML(origFromSpin) + " takes " + GetPieceSpinSSML(origToSpin);
        }
        else
        {
            GetPieceSpinSSML(origFromSpin) + " to " + Constants.BoardCoordinateDict[toIndex];
        }


        return ssml
    }

    /**
     * Get ssml for piece spin
     */
    private fun GetPieceSpinSSML(pPieceSpin: Int): String
    {
        val board = KaruahChessEngineC()
        val ssml = board.getPieceNameFromChar(board.getFENCharFromSpin(pPieceSpin))

        return ssml
    }

    /**
     * Starts the animation
     */
    private suspend fun startPieceAnimation(pLockPanel : Boolean, pAnimationList : ArrayList<TileAnimationInstruction>, pDurationMS: Long)
    {

        if (pLockPanel) {
            _lockPanel = true
        }


       binding.animationPanelLayout.runAnimation(binding.boardPanelLayout, pAnimationList, pDurationMS, binding.boardPanelLayout.framePadding)
       delay(pDurationMS)

        if (pLockPanel) {
            _lockPanel = false
        }
    }

    /**
     * Starts a new game
     */
    private fun newGame(){

        val positiveButtonClick = { _: DialogInterface, _: Int ->
            _lockPanel = true
            stopMoveJob()
            GameRecordDataService.reset()
            _uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
            _lockPanel = false

            // Show start move message
            val computerPlayerEnabled =  ParameterDataService.get(ParamComputerPlayer::class.java).enabled
            val computerMoveFirstEnabled =  ParameterDataService.get(ParamComputerMoveFirst::class.java).enabled
            if (computerPlayerEnabled && computerMoveFirstEnabled) {
                showMessage("Tap the board to start the game", "", Toast.LENGTH_LONG)
            }
        }
        val negativeButtonClick = { _: DialogInterface, _: Int ->
            // Do nothing
        }


        val builder = MaterialAlertDialogBuilder(ContextThemeWrapper(this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert))

        with(builder)
        {
            setTitle("Start a new game?")
            setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton(android.R.string.cancel, negativeButtonClick)

            show()
        }
    }

    /**
     * Undo last move
     */
    private suspend fun undoMove(){

        stopMoveJob()
        val boardBeforeUndo = GameRecordDataService.get()
        val undo = GameRecordDataService.undo()
        if (undo) {
            _lockPanel = true
            val boardAfterUndo = GameRecordDataService.get()
            if (boardBeforeUndo != null && boardAfterUndo != null) {

                val moveAnimationList = BoardAnimation.createAnimationList(
                    boardBeforeUndo,
                    boardAfterUndo,
                    binding.boardPanelLayout,
                    this.applicationContext
                )
                // Do animation
                startPieceAnimation(true, moveAnimationList, 1200L)
            }

            navigateMaxRecord()


            _lockPanel = false
        }

    }

    /**
     * Ends animations
     */
    private fun endPieceAnimation() {
        binding.boardPanelLayout.showAll()
        binding.animationPanelLayout.clear()
    }

    /**
     * Ends any running move job
     */
    fun stopMoveJob() {
        _move.Clear()
        endPieceAnimation()
        GameRecordDataService.currentGame.cancelSearch()
    }

    /**
     * Shows a board message dialog
     */
    private fun showBoardMessageDialog(pTitle: String, pMessage: String, pIcon: Int) {

        if (!(pTitle == "" && pMessage == "")) {
            readText("$pTitle $pMessage")

            val dialog = MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert)

            dialog.setCancelable(true)
            dialog.setTitle(pTitle)
            dialog.setIcon(pIcon)
            dialog.setMessage(pMessage)
            dialog.show()
        }
    }

    /**
     * Do after checkmate occurs
     */
    private fun afterCheckMate(pBoard: KaruahChessEngineC)
    {
        var humanWinAgainstComputer: Boolean = false
        val computerPlayerEnabled =  ParameterDataService.get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  ParameterDataService.get(ParamComputerMoveFirst::class.java).enabled
        val levelAutoEnabled = ParameterDataService.get(ParamLevelAuto::class.java).enabled

        if (pBoard.getStateActiveColour() == Constants.BLACKPIECE && computerPlayerEnabled && !computerMoveFirstEnabled)
        {
            humanWinAgainstComputer = true
        }
        else if (pBoard.getStateActiveColour() == Constants.WHITEPIECE && computerPlayerEnabled && computerMoveFirstEnabled)
        {
            humanWinAgainstComputer = true
        }

        // Increase strength level
        if (levelAutoEnabled && humanWinAgainstComputer)
        {
            val limitEngineStrengthELO = ParameterDataService.get(ParamLimitEngineStrengthELO::class.java)
            val nextElo = limitEngineStrengthELO.eloRating + 75
            val eloIndex = Constants.eloarray.indexOf(nextElo)
            if (eloIndex > -1)
            {
                limitEngineStrengthELO.eloRating = nextElo
                ParameterDataService.set(limitEngineStrengthELO)
                refreshEngineSettingsLevelIndicator()
                showBoardMessageDialog("Level Increase", "Congratulations, you have now progressed to the next level. The engine playing strength is now set to ${Constants.strengthArrayLabel[eloIndex]}.",R.drawable.ic_goldstar)

            }

        }

    }

    /**
     * Upates all the board indicators
     */
    fun updateBoardIndicators(pBoard: GameRecordArray) {
        updateGameMessage(pBoard)
        updateDirectionIndicator(pBoard)
        updateCheckIndicator(pBoard)

    }

    /**
     * Update direction indicator
     */
    private fun updateDirectionIndicator(pRecord: GameRecordArray) {

        val board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)

        if(board.getStateActiveColour() == Constants.WHITEPIECE) {
            binding.indicatorImageView.setImageResource(R.drawable.indicatorwhite)
        }
        else if (board.getStateActiveColour() == Constants.BLACKPIECE) {
            binding.indicatorImageView.setImageResource(R.drawable.indicatorblack)
        }
    }

    /**
     *  Set the check indicator
     */
    private fun updateCheckIndicator(pRecord: GameRecordArray) {

        val board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)

        // Circles the king if in check
        if (board.getStateActiveColour() != 0) {
            val kingCheck = board.isKingCheck(board.getStateActiveColour())

            if (kingCheck) {
                val kingIndex = board.getKingIndex(board.getStateActiveColour())
                if (kingIndex >= 0) {
                    binding.boardPanelLayout.setCheckIndicator(kingIndex)

                    if (board.getStateGameStatus() == BoardStatusEnum.Ready.value) {
                        showMessage("Check", "", Toast.LENGTH_SHORT)
                    }
                }
            }
            else {
                binding.boardPanelLayout.setCheckIndicator(-1)
            }
        }
    }

    /**
     * Shows game messages
     */
    private fun updateGameMessage(pRecord: GameRecordArray): Boolean {
        var gameFinished = false

        val board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)

        var title = ""



        // 0  Game ready, game not started, 1 game commenced, 2 CheckMate, 3 Stalemate, 4 Resign
        if (board.getStateGameStatus() == BoardStatusEnum.Checkmate.value && board.getStateActiveColour() == Constants.BLACKPIECE) {
            gameFinished = true
            title = "Checkmate! White wins."
        } else if (board.getStateGameStatus() == BoardStatusEnum.Checkmate.value && board.getStateActiveColour() == Constants.WHITEPIECE) {
            gameFinished = true
            title = "Checkmate! Black wins."
        } else if (board.getStateGameStatus() == BoardStatusEnum.Stalemate.value) {
            gameFinished = true
            title = "Stalemate, no winners"
        } else if (board.getStateGameStatus() == BoardStatusEnum.Resigned.value && board.getStateActiveColour() == Constants.WHITEPIECE) {
            gameFinished = true
            title = "White resigned. Black wins."
        } else if (board.getStateGameStatus() == BoardStatusEnum.Resigned.value && board.getStateActiveColour() == Constants.BLACKPIECE) {
            gameFinished = true
            title = "Black resigned. White wins."
        }


        showBoardMessageDialog(title, "", R.drawable.ic_info_outline)

        return gameFinished
    }


    /**
     * Draws the board
     */
    private fun drawBoard(pShowCoordinates: Boolean){

        // Make room for coordinates if enabled
        val approxBoardMarginPixels: Int = if (pShowCoordinates) 18f.spToPx() else 0

        // Draws the board
        binding.boardPanelLayout.drawTiles(binding.mainFrame.measuredWidth, binding.mainFrame.measuredHeight, approxBoardMarginPixels)

        // Draws the coordinates if enabled
        if(pShowCoordinates) {
            binding.coordPanelLayout.show(true)
            binding.coordPanelLayout.draw(binding.boardPanelLayout.frameSize, binding.boardPanelLayout.frameSize, binding.boardPanelLayout.tileSize,0)
        }
        else {
            binding.coordPanelLayout.show(false)
        }

        // Refresh shake animation
        binding.boardPanelLayout.shakeRefresh()

        // Set board rotation
        rotateBoard(ParameterDataService.get(ParamRotateBoard::class.java).value)

    }

    /**
     * Sets the board rotation to [pRotation] degrees
     */
    private fun rotateBoard(pRotation: Int) {
        binding.boardPanelLayout.rotate(pRotation)
        binding.animationPanelLayout.rotate(pRotation)
        binding.coordPanelLayout.draw(binding.boardPanelLayout.frameSize, binding.boardPanelLayout.frameSize, binding.boardPanelLayout.tileSize, pRotation)
    }


    /**
     * Gets version information
     */
    private fun getVersion(): String {
        return try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "$e"
        }

    }


    /**
     * Records the current state of the game
     */
    private suspend fun recordCurrentGameState()
    {
        // Record current game state
        val success = GameRecordDataService.recordGameState(0, 0)
        if (success > 0)
        {
            // Ensure game record position is set to max value
            navigateMaxRecord()
        }

    }

    /**
     * Show pgn import dialog
     */
    private fun showImportPGNDialog() {

        val fm = supportFragmentManager
        val importPGNDialogFragment = ImportPGN.newInstance()
        importPGNDialogFragment.show(fm, null)
    }

    /**
     * Show Castling dialog
     */
    private fun showCastlingRightsDialog(pKingSpin: Int) {
        // Cancel any move jobs first
        stopMoveJob()

        // Get the current board in view and open the castling dialog
        val record = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
        if(record != null) {
            castlingRightsVM.kingSpin.value = pKingSpin
            castlingRightsVM.record.value = record
            val fm = supportFragmentManager
            CastlingRights.newInstance().show(fm, null)
        }
    }

    /**
     * Show engine settings dialog
     */
    private fun showEngineSettingsDialog() {

        val fm = supportFragmentManager
        val engineSettingsDialogFragment = EngineSettings.newInstance()
        engineSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show sound settings dialog
     */
    private fun showSoundColourSettingsDialog() {

        val fm = supportFragmentManager
        val soundColourSettingsDialogFragment = SoundColourSettings.newInstance()
        soundColourSettingsDialogFragment.show(fm, null)
    }


    /**
     * Show piece type select dialog
     */
    private fun showPieceEditToolDialog(pTile: Tile) {

        val fm = supportFragmentManager
        pieceEditToolVM.tile.value = pTile
        if (pieceEditToolVM.pieceEditToolColour.value == null || pTile.spin != 0) {
            pieceEditToolVM.pieceEditToolColour.value = if (pTile.spin < 0) Constants.BLACKPIECE else Constants.WHITEPIECE
        }
        val pieceEditToolDialogFragment = PieceEditTool.newInstance()
        pieceEditToolDialogFragment.setPieceEditToolInteractionListener(this)
        pieceEditToolDialogFragment.show(fm, "PieceEditDialog")
    }


    /**
     * Show pawn promotion dialog
     */
    private suspend fun showPawnPromotionDialog(pColour: Int): Int {

        val fm = supportFragmentManager
        pawnPromotionVM.buttonSize.value = binding.boardPanelLayout.tileSize.toFloat()
        pawnPromotionVM.promotionColour.value = pColour
        val pawnPromotionDialogFragment = PawnPromotion.newInstance()
        pawnPromotionDialogFragment.setPawnPromotionInteractionListener(this)
        pawnPromotionDialogFragment.show(fm, "PawnPromotionDialog")

        return _pieceChannel.receive()

    }

    /**
     * Show the about dialog
     */
    private fun showAboutDialog() {
        val fm = supportFragmentManager
        aboutVM.versionStr.value = getVersion()
        val aboutDialogFragment = About.newInstance()
        aboutDialogFragment.show(fm, null)
    }

    /**
     * Pawn promotion click button
     */
    override fun onPawnPromotionClick(pFen: Char, pDialog: DialogFragment) {

         val pieceValue = when (pFen) {
            'N','n' -> PawnPromotionEnum.Knight.value
            'B','b' -> PawnPromotionEnum.Bishop.value
            'R','r' -> PawnPromotionEnum.Rook.value
            'Q','q' -> PawnPromotionEnum.Queen.value
            else -> PawnPromotionEnum.Queen.value
        }

        _pieceChannel.trySend(pieceValue)
        pDialog.dismiss()


    }



    /**
     * Loads a game record from a file uri
     */
    private fun loadGameRecordFromFileUri(pFileUri: Uri) {
        try {
            val file: InputStream? = contentResolver.openInputStream(pFileUri)
            val importDB = ImportDB()
            val fileResult = if(file != null) {
                importDB.import(file, ImportDB.ImportTypeEnum.GameXML, this.applicationContext)
            }
            else { 0 }

            if(fileResult > 0) {
                _move.Clear()
                _uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
                showMessage("Game successfully loaded.", "", Toast.LENGTH_SHORT)
            }
            else {
                showMessage("Unable to load game. No records found.", "", Toast.LENGTH_SHORT)
            }
        } catch (e: java.lang.Exception) {
            showMessage(
                "An error occurred - ${e.message}",
                "An error occurred reading file.",
                Toast.LENGTH_LONG
            )
        }

    }



    /**
     * Save game to xml file
     */
    private fun saveGameRecordAsFile() {
        try {
            // Export data
            val exportDB = ExportDB()
            val file = exportDB.export(ExportDB.ExportTypeEnum.GameXML, this.applicationContext)
            val sharedFileUri: Uri = FileProvider.getUriForFile(this.applicationContext, "purpletreesoftware.karuahchess.exportdata", file)

            // Send file via intent
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/gzip"
            intent.putExtra(Intent.EXTRA_STREAM, sharedFileUri)
            startActivity(Intent.createChooser(intent, "Send file to"))
        }
        catch (e: Throwable) {
            showMessage("Could not export file. An error occurred - ${e.message}", "Could not export file.", Toast.LENGTH_LONG)
        }
    }


    /**
     * Set the board to the max record
     */
    suspend fun navigateMaxRecord() {
        val maxId = GameRecordDataService.getMaxId()
        navigateGameRecord(maxId, false)

        // Refresh shake animation
        binding.boardPanelLayout.shakeRefresh()

        // Load the move navigator
        val navParam = ParameterDataService.get(ParamNavigator::class.java)
        loadNavigator(navParam.enabled)
    }

    /**
     * Navigate to requested game record
     */
    suspend fun navigateGameRecord(pRecId: Int, pAnimate: Boolean) {
        if (pRecId > 0)
        {
            val oldBoard : GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            val updatedBoard = GameRecordDataService.get(pRecId)

            // Update board displayed with requested record
            if (updatedBoard != null)
            {
                // End any animations
                endPieceAnimation()

                // Do animation
                if (pAnimate && oldBoard != null) {
                    val moveAnimationList = BoardAnimation.createAnimationList(
                        oldBoard,
                        updatedBoard,
                        binding.boardPanelLayout,
                        this.applicationContext
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationList, 1200L)

                    BoardSquareDataService.update(binding.boardPanelLayout, updatedBoard)
                    BoardSquareDataService.gameRecordCurrentValue = pRecId
                    updateBoardIndicators(updatedBoard)

                }
                else {
                    BoardSquareDataService.update(binding.boardPanelLayout, updatedBoard)
                    BoardSquareDataService.gameRecordCurrentValue = pRecId
                    updateBoardIndicators(updatedBoard)
                }

                _move.Clear()



            }
        }
    }

    /**
     * Loads the move navigator
     */
    private fun loadNavigator(pEnabled: Boolean) {
        // Update navigator

        if(pEnabled) {
            val recIdList = GameRecordDataService.getAllRecordIDList()
            binding.navigatorLayout.show(this,recIdList, BoardSquareDataService.gameRecordCurrentValue)
        }
        else {
            binding.navigatorLayout.hide()
        }

    }

    /**
     * Show resign dialog
     */
    private fun resignGameDialog(){

        val positiveButtonClick = { _: DialogInterface, _: Int ->
            _uiScope.launch(Dispatchers.Main) { resignGame() }
             Unit
        }
        val negativeButtonClick = { _: DialogInterface, _: Int ->
            // Do nothing
        }


        val builder = MaterialAlertDialogBuilder(ContextThemeWrapper(this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert))

        with(builder)
        {
            setTitle("Resign from current game?")
            setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton(android.R.string.cancel, negativeButtonClick)

            show()
        }
    }

    /**
     * Resigns the current game
     */
    private suspend fun resignGame()
    {
        // Check that not in edit mode
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            showMessage("Cannot resign in edit mode.","",Toast.LENGTH_SHORT)
            return
        }

        // Check that it is players turn
        val computerPlayerEnabled =  ParameterDataService.get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  ParameterDataService.get(ParamComputerMoveFirst::class.java).enabled
        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE
        val turnColour = GameRecordDataService.currentGame.getStateActiveColour()
        if(computerPlayerEnabled && turnColour == computerColour) {
            showMessage("Cannot resign as it is not your turn.","",Toast.LENGTH_SHORT)
            return
        }

        // Ensure game record is set to the latest
        navigateMaxRecord()

        val status = GameRecordDataService.currentGame.getStateGameStatus()

        // Only resign if game is not already finished
        if (status == BoardStatusEnum.Ready.value && GameRecordDataService.currentGame.getStateFullMoveCount() > 0) {

            // Stop task if running
            stopMoveJob()

            // Set the state of the game to resign
            GameRecordDataService.currentGame.setStateGameStatus(BoardStatusEnum.Resigned.value)


            // Do animation, display message
            val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(GameRecordDataService.currentGame.getStateActiveColour())
            val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this.applicationContext)
            startPieceAnimation(true, kingFallSeq, 3000L)

            // Record current game state
            recordCurrentGameState()

        }
        else {
            showMessage("Resigning not available at this stage of the game","", Toast.LENGTH_LONG)
        }
    }

    /**
     * Updates a board square when in edit mode
     */
    override fun onPieceEditToolClick(pFen: Char, pSqIndex: Int, pDialog : DialogFragment) {
        arrangeUpdate(pFen, pSqIndex)
        pDialog.dismiss()
    }

    /**
     * Enables and disables edit board
     */
    private fun editBoardToggle() {
        val edit = ParameterDataService.get(ParamArrangeBoard::class.java)
        edit.enabled = !edit.enabled
        ParameterDataService.set(edit)
        _editMenuItem?.isChecked = edit.enabled
        if (edit.enabled) showMessage("Edit board is enabled","", Toast.LENGTH_SHORT)
        else showMessage("Edit board is disabled","", Toast.LENGTH_SHORT)

        binding.boardPanelLayout.shake(edit.enabled)

    }

    /**
     * Apply board colour
     */
    fun applyBoardColour() {
        val darkSquareColourParam = ParameterDataService.get(ParamColourDarkSquares::class.java)
        val darkSqColour = Color.argb(darkSquareColourParam.a, darkSquareColourParam.r, darkSquareColourParam.g, darkSquareColourParam.b)
        binding.boardPanelLayout.applyBoardColour(darkSqColour)


    }

    /**
     * Routines to run on pause
     */
    override fun onPause() {
        // Stop any searches
        GameRecordDataService.currentGame.cancelSearch()

        // Close the promotion dialog
        supportFragmentManager.findFragmentByTag("PawnPromotionDialog")?.let {
            (it as DialogFragment).dismiss()

            // Cancel all jobs
            _uiScope.coroutineContext.cancelChildren()
        }

        super.onPause()
    }

    /**
     * Routines to run on activity destroy
     */
    override fun onDestroy() {

        // Shutdown tts
        _textReader.tts?.shutdown()


        super.onDestroy()
    }

    /**
     * Routines to run when saving instance state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }




    /**
     * Initialise voice recognition
     */
    private fun initVoiceRecognition() {

        val fm = supportFragmentManager
        val voicePermissionDialogFragment = VoicePermission.newInstance()
        if (!voicePermissionDialogFragment.hasPermission(applicationContext)) {
            voicePermissionDialogFragment.show(fm, null)
        }

        if (_voiceRecognition == null && voicePermissionDialogFragment.hasPermission(
                applicationContext
            )
        ) {
            _voiceRecognition = VoiceRecognition(this)
            _voiceRecognition?.setVoiceRecognitionInteractionListener(this)
        }

        startVoiceRecognition()


    }

    /**
     * Start voice recognition
     */
    private fun startVoiceRecognition() {
        val isListening =_voiceRecognition?.listening ?: false
        if(isListening) _voiceRecognition?.stop()
        else  _voiceRecognition?.start()
    }


    /**
     * Indicates voice recognition is listening
     */
    override fun onVoiceRecognitionActive() {
        binding.voiceRecognitionAction.backgroundTintList = resources.getColorStateList(R.color.colorSecondary, null)
    }

    /**
     * Indicates voice recognition speech has ended
     */
    override fun onVoiceRecognitionInActive() {
        binding.voiceRecognitionAction.backgroundTintList = resources.getColorStateList(R.color.colorInactive, null)
    }


    /**
     * Detects voice command spoken by user
     */
    override fun onVoiceCommandAction(pCmdText: String) {
        when (pCmdText) {
            "help" -> {
                showVoiceHelp()
            }
            "new" -> {
                newGame()
            }
            "resign" -> {
                resignGameDialog()
            }
            "undo" -> {
                _uiScope.launch(Dispatchers.Main) {
                    undoMove()
                }
            }
            "edit" -> {
                editBoardToggle()
            }
            else -> {
                showMessage("Unrecognised command. Say 'help' for voice commands", "Unrecognised command", Toast.LENGTH_LONG)
            }
        }
    }

    /**
     * Show voice help info
     */
    private fun showVoiceHelp() {

        val fm = supportFragmentManager
        val voiceHelpDialogFragment = VoiceHelp.newInstance()
        voiceHelpDialogFragment.show(fm, null)
    }

    /**
     * Initialise voice command functionality
     */
    private fun voiceCommandShow(pEnable: Boolean, pShowMsg: Boolean) : Boolean {

        if(pEnable) {
            // Set microphone button
            return if (SpeechRecognizer.isRecognitionAvailable(this)) {
                binding.voiceRecognitionAction.visibility = View.VISIBLE
                binding.voiceRecognitionAction.setOnClickListener { initVoiceRecognition() }
                if(pShowMsg) showMessage("Press the microphone button and say a command. For example, 'Help'","", Toast.LENGTH_LONG)
                true
            } else {
                binding.voiceRecognitionAction.visibility = View.GONE
                if(pShowMsg) showMessage("Voice recognition not available on this device","", Toast.LENGTH_LONG)
                false
            }
        }
        else {
            binding.voiceRecognitionAction.visibility = View.GONE
            return false
        }
    }



    /**
     * Refresh the level indicator
     */
    fun refreshEngineSettingsLevelIndicator() {
        binding.engineSettingsAction.text = (Constants.eloarray.indexOf(ParameterDataService.get(ParamLimitEngineStrengthELO::class.java).eloRating) + 1).toString()
    }

    /**
     * Play piece move sound effect
     */
    private fun playPieceMoveSoundEffect() {
        val soundEffect = ParameterDataService.get(ParamSoundEffect::class.java)
        if (soundEffect.enabled) {
            // Sounds
            sndPool?.play(sndMove,1F,1F,0,0,1F)

        }

    }
}

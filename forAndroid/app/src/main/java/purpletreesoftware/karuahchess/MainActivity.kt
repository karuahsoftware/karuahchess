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

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.MenuCompat
import androidx.fragment.app.DialogFragment
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
import purpletreesoftware.karuahchess.voice.VoiceRecognition
import java.io.InputStream

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), TilePanel.OnTilePanelInteractionListener, PieceEditTool.OnPieceEditToolInteractionListener, VoiceRecognition.OnVoiceRecognitionInteractionListener, PawnPromotionDialog.OnPawnPromotionInteractionListener {

    private var _dbHelper: DatabaseHelper? = null
    private lateinit var _gameRecordDS: GameRecordDataService
    private lateinit var _move: Move
    private var _userInteracted: Boolean = false
    private lateinit var _parameterDS: ParameterDataService
    private var _lockPanel = false
    private var _computerMoveProcessing = false
    private lateinit var _textReader: TextReader
    private var _voiceRecognition : VoiceRecognition? = null
    private var _editMenuItem: MenuItem? = null
    private val _bufferBoard = KaruahChessEngineC()
    private val _pieceChannel = Channel<Int>()
    private val _mainjob = SupervisorJob()
    private val _uiScope = CoroutineScope(Dispatchers.Main + _mainjob)
    lateinit var binding: ActivityMainBinding

    // Public variables
    enum class MoveTypeEnum(val value: Int) { None(0), Normal(1), EnPassant(2), Castle(3), Promotion(4) }
    enum class BoardStatusEnum(val value: Int) { Ready(0), Checkmate(1), Stalemate(2), Resigned(3) }
    enum class PawnPromotionEnum(val value: Int) { Knight(2), Bishop(3), Rook(4), Queen(5) }
    enum class PieceTypeEnum (val value: Int){ Empty(0), Pawn(1), Knight(2), Bishop(3), Rook(4), Queen(5), King(6) }

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

        // Parameter data service
        _parameterDS = ParameterDataService()

        // Game Record
        _gameRecordDS = GameRecordDataService()


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
            val currentRotationParam = _parameterDS.get(ParamRotateBoard::class.java)
            var newRotation = currentRotationParam.value + 90
            if (newRotation >= 360) newRotation = 0
            currentRotationParam.value = newRotation
            _parameterDS.set(currentRotationParam)
            rotateBoard(newRotation)
        }

        // Set show structure button
        binding.structureAction.setOnClickListener { showStructureMenu() }

        // Set board layout listener
        binding.boardPanelLayout.setTilePanelInteractionListener(this)

        // Move
        _move = Move(binding.boardPanelLayout)

        // Voice command visibility
        val voiceParam = _parameterDS.get(ParamVoiceCommand::class.java)
        val voiceEnabled = voiceCommandShow(voiceParam.enabled, false)
        if(voiceParam.enabled && !voiceEnabled) {
            // Set voice command to false if voice command was enabled but fails to show
            voiceParam.enabled = false
            _parameterDS.set(voiceParam)
        }

        // Initialise board
        BoardSquareDataService.update(binding.boardPanelLayout, _gameRecordDS.getCurrentGame())

        val mainFrame = binding.mainFrame
        mainFrame.afterMeasured {
            // Draw the board
            val showCoord = _parameterDS.get(ParamBoardCoord::class.java).enabled
            drawBoard(showCoord)

            // Move progress bar visibility
            binding.moveProgressBar.visibility = View.GONE

            // Set shake
            val arrangeBoardEnabled = _parameterDS.get(ParamArrangeBoard::class.java).enabled
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
        menu.findItem(R.id.action_highlight).isChecked = _parameterDS.get(ParamMoveHighlight::class.java).enabled
        _editMenuItem = menu.findItem(R.id.action_edit)
        _editMenuItem?.isChecked = _parameterDS.get(ParamArrangeBoard::class.java).enabled
        menu.findItem(R.id.action_sound).isChecked = _parameterDS.get(ParamSound::class.java).enabled
        menu.findItem(R.id.action_coordinates).isChecked = _parameterDS.get(ParamBoardCoord::class.java).enabled
        menu.findItem(R.id.action_voice).isChecked = _parameterDS.get(ParamVoiceCommand::class.java).enabled
        menu.findItem(R.id.action_navigator).isChecked = _parameterDS.get(ParamNavigator::class.java).enabled
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
                val highlight = _parameterDS.get(ParamMoveHighlight::class.java)
                highlight.enabled = !item.isChecked
                item.isChecked = highlight.enabled
                _parameterDS.set(highlight)
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
                endMoveJob()

                // Flip direction
                val board = KaruahChessEngineC()
                val record = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
                if(record != null) {
                    board.setBoardArray(record.boardArray)
                    board.setStateArray(record.stateArray)
                    // Flip direction
                    board.setStateActiveColour(board.getStateActiveColour() * (-1))
                    record.stateArray = board.getStateArray()
                    _gameRecordDS.updateGameState(record)
                    updateBoardIndicators(record)
                }

                true
            }
            R.id.action_sound -> {
                val sound = _parameterDS.get(ParamSound::class.java)
                sound.enabled = !item.isChecked
                item.isChecked = sound.enabled
                _parameterDS.set(sound)
                if(sound.enabled) showMessage("${item.title} is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled","", Toast.LENGTH_SHORT)
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            R.id.action_coordinates -> {
                val coordinates = _parameterDS.get(ParamBoardCoord::class.java)
                coordinates.enabled = !item.isChecked
                item.isChecked = coordinates.enabled
                _parameterDS.set(coordinates)
                if(coordinates.enabled) showMessage("${item.title} is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled","", Toast.LENGTH_SHORT)

                drawBoard(coordinates.enabled)
                true
            }
            R.id.action_navigator -> {
                val navParam = _parameterDS.get(ParamNavigator::class.java)
                navParam.enabled = !item.isChecked
                item.isChecked = navParam.enabled
                _parameterDS.set(navParam)

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
                loadGameRecordFromFile()
                true
            }
            R.id.action_save -> {
                saveGameRecordAsFile()
                true
            }
            R.id.action_voice -> {
                val voiceCmd = _parameterDS.get(ParamVoiceCommand::class.java)
                val isEnabled = voiceCommandShow(!item.isChecked, true)
                voiceCmd.enabled = isEnabled
                item.isChecked = voiceCmd.enabled
                _parameterDS.set(voiceCmd)
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
        val currentBoard = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
        val previousBoard = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue - 1)
        val lastChanges = _gameRecordDS.getBoardSquareChanges(currentBoard, previousBoard)

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
        val arrangeBoardEnabled = _parameterDS.get(ParamArrangeBoard::class.java).enabled
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
        val arrangeBoardEnabled = _parameterDS.get(ParamArrangeBoard::class.java).enabled
        val gameFinished: Boolean = _gameRecordDS.currentGame.getStateGameStatus() != BoardStatusEnum.Ready.value
        _userInteracted = true

        if (pTile != null && !gameFinished && !_lockPanel && !arrangeBoardEnabled) {

            _uiScope.launch(Dispatchers.Main) {
                userMoveAdd(pTile, true)
            }
        }
        else if (pTile != null && arrangeBoardEnabled) {
            showPieceEditToolDialog(pTile)
        }

        if (gameFinished) {
            showMessage("Cannot move as game has finished.", "", Toast.LENGTH_LONG)
        }
    }

    /**
     * Updates the piece type on a square. Used for editing the board.
     */
    fun arrangeUpdate(pFen: Char, pToIndex: Int) {
        val arrangeBoardEnabled =  _parameterDS.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                _bufferBoard.setBoardArray(record.boardArray)
                _bufferBoard.setStateArray(record.stateArray)

                val mResult = _bufferBoard.arrangeUpdate(pFen, pToIndex)
                if (mResult.success) {
                    record.boardArray = _bufferBoard.getBoardArray()
                    record.stateArray = _bufferBoard.getStateArray()

                    BoardSquareDataService.update(binding.boardPanelLayout,record)
                    _gameRecordDS.updateGameState(record)
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
        val arrangeBoardEnabled =  _parameterDS.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                _bufferBoard.setBoardArray(record.boardArray)
                _bufferBoard.setStateArray(record.stateArray)
                val mResult = _bufferBoard.arrange(pFromIndex, pToIndex)
                if (mResult.success) {
                    record.boardArray = _bufferBoard.getBoardArray()
                    record.stateArray = _bufferBoard.getStateArray()
                    BoardSquareDataService.update(binding.boardPanelLayout,record)
                    _gameRecordDS.updateGameState(record)
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

        val moveHighlightEnabled = _parameterDS.get(ParamMoveHighlight::class.java).enabled

        // Ensure game record is set to the latest
        val maxRecId = _gameRecordDS.getMaxId()
        if (BoardSquareDataService.gameRecordCurrentValue != maxRecId) {
            navigateMaxRecord()
            _move.Clear()
            return
        }

        // Select highlight mode
        val highlight = if(moveHighlightEnabled) HighlightEnum.MovePath else HighlightEnum.Select

        // Create proposed move
        val moveSelected = _move.Add(pTile.index, _gameRecordDS.currentGame, highlight)

        // Restart the computer move (if required)
        startComputerMoveTask()

        if (moveSelected) {
            val boardBeforeMove = _gameRecordDS.getCurrentGame()

            // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
            var promotionPiece: Int = PawnPromotionEnum.Queen.value // default
            if (_gameRecordDS.currentGame.isPawnPromotion(_move.fromIndex, _move.toIndex)) {
                promotionPiece = showPawnPromotionDialog(_gameRecordDS.currentGame.getStateActiveColour())
            }

            val gameStatusBeforeMove = _gameRecordDS.currentGame.getStateGameStatus()
            val moveResult = _gameRecordDS.currentGame.move(_move.fromIndex, _move.toIndex, promotionPiece, true, true)

            if (moveResult.success) {
                if (moveResult.returnMessage != "") {
                    showMessage(moveResult.returnMessage,"", Toast.LENGTH_SHORT)
                }
                else {
                    val ssml = getBoardSquareSSML(moveResult.moveDataStr)
                    showMessage(ssml, "", Toast.LENGTH_SHORT)
                }

                val boardAfterMove = _gameRecordDS.getCurrentGame()

                if(pAnimate) {
                    val moveAnimationSeq = BoardAnimation.createAnimationMoveSequence(
                        boardBeforeMove,
                        boardAfterMove,
                        binding.boardPanelLayout,
                        this.applicationContext
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationSeq, 1200L)
                }

                // Update display
                BoardSquareDataService.update(binding.boardPanelLayout, _gameRecordDS.getCurrentGame())

                // Update score if checkmate occurred
                if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && _gameRecordDS.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value)
                {
                    val kingFallIndex = _gameRecordDS.currentGame.getKingIndex(_gameRecordDS.currentGame.getStateActiveColour())
                    val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this.applicationContext)
                    startPieceAnimation(true, kingFallSeq, 3000L)

                }

                // Record game state
                recordCurrentGameState()

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
        val arrangeBoardEnabled =  _parameterDS.get(ParamArrangeBoard::class.java).enabled
        val computerPlayerEnabled =  _parameterDS.get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  _parameterDS.get(ParamComputerMoveFirst::class.java).enabled
        val limitEngineStrengthELO = _parameterDS.get(ParamLimitEngineStrengthELO::class.java).eloRating

        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE

        val turnColour = _gameRecordDS.currentGame.getStateActiveColour()
        val boardStatus = _gameRecordDS.currentGame.getStateGameStatus()

        if (boardStatus == 0 && (!_computerMoveProcessing) && (!arrangeBoardEnabled) && computerPlayerEnabled && computerColour == turnColour) {
            _lockPanel = true

            // Clear the user move since starting computer move
            _move.Clear()

            _computerMoveProcessing = true
            binding.moveProgressBar.visibility = View.VISIBLE

            val searchOptions = SearchOptions()
            searchOptions.limitStrengthELO = limitEngineStrengthELO

            val topMove = withContext(Dispatchers.IO) { _gameRecordDS.currentGame.searchStart(searchOptions) }

            if (!(topMove.cancelled || topMove.error)) {
                val boardBeforeMove = _gameRecordDS.getCurrentGame()
                val gameStatusBeforeMove = _gameRecordDS.currentGame.getStateGameStatus()
                val moveResult = _gameRecordDS.currentGame.move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, true, true)
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
                    val boardAfterMove = _gameRecordDS.getCurrentGame()
                    val moveAnimationSeq = BoardAnimation.createAnimationMoveSequence(
                        boardBeforeMove,
                        boardAfterMove,
                        binding.boardPanelLayout,
                        this.applicationContext
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationSeq, 1200L)


                    // Update display
                    BoardSquareDataService.update(binding.boardPanelLayout, _gameRecordDS.getCurrentGame())


                    // Update score if checkmate occurred
                    if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && _gameRecordDS.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value) {
                        val kingFallIndex = _gameRecordDS.currentGame.getKingIndex(_gameRecordDS.currentGame.getStateActiveColour())
                        val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this.applicationContext)
                        startPieceAnimation(true, kingFallSeq, 3000L)
                    }

                    // Record game state
                    recordCurrentGameState()
                }
            }
            else {
                if (topMove.error) {
                    showMessage("Computer unable to move. Invalid board configuration.", "", Toast.LENGTH_LONG)
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
        val sound = _parameterDS.get(ParamSound::class.java)
        if (_textReader.ready && sound.enabled) {
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
    private suspend fun startPieceAnimation(pLockPanel : Boolean, pAnimationSequence : TileAnimationSequence, pDurationMS: Long)
    {

        if (pLockPanel) {
            _lockPanel = true
        }


           binding.animationPanelLayout.runAnimation(pAnimationSequence.sequence, binding.boardPanelLayout, pDurationMS, binding.boardPanelLayout.framePadding)
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
            endMoveJob()
            endPieceAnimation()
            _lockPanel = true
            _gameRecordDS.reset()
            _move.Clear()
            _uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }

            _lockPanel = false

            // Show start move message
            val computerPlayerEnabled =  _parameterDS.get(ParamComputerPlayer::class.java).enabled
            val computerMoveFirstEnabled =  _parameterDS.get(ParamComputerMoveFirst::class.java).enabled
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

        endMoveJob()
        endPieceAnimation()
        val boardBeforeUndo = _gameRecordDS.get()
        val undo = _gameRecordDS.undo()
        if (undo) {
            _lockPanel = true
            val boardAfterUndo = _gameRecordDS.get()
            if (boardBeforeUndo != null && boardAfterUndo != null) {

                val moveAnimationSeq = BoardAnimation.createAnimationMoveSequence(
                    boardBeforeUndo,
                    boardAfterUndo,
                    binding.boardPanelLayout,
                    this.applicationContext
                )
                // Do animation
                startPieceAnimation(true, moveAnimationSeq, 1200L)
            }

            _move.Clear()

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
    private fun endMoveJob() {
        _gameRecordDS.currentGame.cancelSearch()
    }

    /**
     * Shows a board message dialog
     */
    private fun showBoardMessageDialog(pTitle: String, pMessage: String) {

        if (pTitle != "") {
            readText(pMessage)

            val dialog = MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert)

            dialog.setCancelable(true)
            dialog.setTitle(pTitle)
            dialog.setIcon(R.drawable.ic_info_outline)
            dialog.setMessage(pMessage)
            dialog.show()


        }
    }

    /**
     * Upates all the board indicators
     */
    private fun updateBoardIndicators(pBoard: GameRecordArray) {
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
                    showMessage("Check","", Toast.LENGTH_SHORT)
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

        val msg = if (gameFinished && _parameterDS.get(ParamComputerPlayer::class.java).enabled) {
            "Computer Strength: " + Constants.strengthArrayLabel[Constants.eloarray.indexOf(_parameterDS.get(ParamLimitEngineStrengthELO::class.java).eloRating)]
        } else {
            "Game over"
        }

        showBoardMessageDialog(title, msg)

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
        rotateBoard(_parameterDS.get(ParamRotateBoard::class.java).value)

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
        val success = _gameRecordDS.recordGameState(0, 0)
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
        val importPGNDialogFragment = ImportPGN.newInstance(_parameterDS)
        importPGNDialogFragment.show(fm, null)
    }

    /**
     * Show engine settings dialog
     */
    private fun showEngineSettingsDialog() {

        val fm = supportFragmentManager
        val engineSettingsDialogFragment = EngineSettings.newInstance(_parameterDS)
        engineSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show piece type select dialog
     */
    private fun showPieceEditToolDialog(pTile: Tile) {

        val fm = supportFragmentManager
        val pieceEditToolDialogFragment = PieceEditTool.newInstance(pTile)
        pieceEditToolDialogFragment.setPieceEditToolInteractionListener(this)
        pieceEditToolDialogFragment.show(fm, "PieceEditDialog")
    }


    /**
     * Show pawn promotion dialog
     */
    private suspend fun showPawnPromotionDialog(pColour: Int): Int {

        val fm = supportFragmentManager
        val pawnPromotionDialogFragment = PawnPromotionDialog.newInstance(binding.boardPanelLayout.tileSize.toFloat(), pColour)
        pawnPromotionDialogFragment.setPawnPromotionInteractionListener(this)
        pawnPromotionDialogFragment.show(fm, "PawnPromotionDialog")

        val piece = _pieceChannel.receive()


        return piece
    }

    /**
     * Show the about dialog
     */
    private fun showAboutDialog() {

        val fm = supportFragmentManager
        val aboutDialogFragment = About.newInstance(getVersion())
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

        _pieceChannel.offer(pieceValue)
        pDialog.dismiss()


    }

    /**
     * Loads a game record from file
     */
    private fun loadGameRecordFromFile() {
        val intent = Intent()
            .setType("application/gzip")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 10)
    }

    /**
     * Import file
     */
    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)

        if (reqCode == 10 && resCode == Activity.RESULT_OK && data != null) {
            val fileUri: Uri? = data.data
            if(fileUri != null) {
                try {
                    val file: InputStream? = contentResolver.openInputStream(fileUri)
                    val importDB = ImportDB()
                    val result = if(file != null) {
                        importDB.import(file, ImportDB.ImportTypeEnum.GameXML, this.applicationContext)
                    }
                    else { 0 }

                    if(result > 0) {
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
        val maxId = _gameRecordDS.getMaxId()
        navigateGameRecord(maxId, false)

        // Refresh shake animation
        binding.boardPanelLayout.shakeRefresh()

        // Load the move navigator
        val navParam = _parameterDS.get(ParamNavigator::class.java)
        loadNavigator(navParam.enabled)
    }

    /**
     * Navigate to requested game record
     */
    suspend fun navigateGameRecord(pRecId: Int, pAnimate: Boolean) {
        if (pRecId > 0)
        {
            val oldBoard : GameRecordArray? = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
            val updatedBoard = _gameRecordDS.get(pRecId)

            // Update board displayed with requested record
            if (updatedBoard != null)
            {
                // End any animations
                endPieceAnimation()

                // Do animation
                if (pAnimate && oldBoard != null) {
                    val moveAnimationSeq = BoardAnimation.createAnimationMoveSequence(
                        oldBoard,
                        updatedBoard,
                        binding.boardPanelLayout,
                        this.applicationContext
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationSeq, 1200L)

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
            val recIdList = _gameRecordDS.getAllRecordIDList()
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
        val arrangeBoardEnabled =  _parameterDS.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            showMessage("Cannot resign in edit mode.","",Toast.LENGTH_SHORT)
            return
        }

        // Check that it is players turn
        val computerPlayerEnabled =  _parameterDS.get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  _parameterDS.get(ParamComputerMoveFirst::class.java).enabled
        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE
        val turnColour = _gameRecordDS.currentGame.getStateActiveColour()
        if(computerPlayerEnabled && turnColour == computerColour) {
            showMessage("Cannot resign as it is not your turn.","",Toast.LENGTH_SHORT)
            return
        }

        // Ensure game record is set to the latest
        navigateMaxRecord()

        val status = _gameRecordDS.currentGame.getStateGameStatus()

        // Only resign if game is not already finished
        if (status == BoardStatusEnum.Ready.value && _gameRecordDS.currentGame.getStateFullMoveCount() > 0) {

            // Stop task if running
            endMoveJob()

            // End any animation if running
            endPieceAnimation()

            // Set the state of the game to resign
            _gameRecordDS.currentGame.setStateGameStatus(BoardStatusEnum.Resigned.value)


            // Do animation, display message
            val kingFallIndex = _gameRecordDS.currentGame.getKingIndex(_gameRecordDS.currentGame.getStateActiveColour())
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
        val edit = _parameterDS.get(ParamArrangeBoard::class.java)
        edit.enabled = !edit.enabled
        _parameterDS.set(edit)
        _editMenuItem?.isChecked = edit.enabled
        if (edit.enabled) showMessage("Edit board is enabled","", Toast.LENGTH_SHORT)
        else showMessage("Edit board is disabled","", Toast.LENGTH_SHORT)

        binding.boardPanelLayout.shake(edit.enabled)

    }

    /**
     * Routines to run on pause
     */
    override fun onPause() {
        // Stop any searches
        _gameRecordDS.currentGame.cancelSearch()

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
    override fun onVoiceRecogntionActive() {
        binding.voiceRecognitionAction.backgroundTintList = resources.getColorStateList(R.color.colorSecondary, null)
    }

    /**
     * Indicates voice recognition speech has ended
     */
    override fun onVoiceRecogntionInActive() {
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
     * Show structure menu options
     */
    private fun showStructureMenu() {
        val popup = PopupMenu(this, binding.structureAction)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_structure, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.backward_pawns_item -> {
                    val currentBoardRecord = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
                    if(currentBoardRecord != null) highlightFeature(currentBoardRecord, arrayListOf(0, 1))
                }
                R.id.isolated_pawns_item -> {
                    val currentBoardRecord = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
                    if(currentBoardRecord != null) highlightFeature(currentBoardRecord, arrayListOf(2, 3))
                }
                R.id.white_attack_item -> {
                    val currentBoardRecord = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
                    if(currentBoardRecord != null) highlightFeature(currentBoardRecord, arrayListOf(15))
                }
                R.id.black_attack_item -> {
                    val currentBoardRecord = _gameRecordDS.get(BoardSquareDataService.gameRecordCurrentValue)
                    if(currentBoardRecord != null) highlightFeature(currentBoardRecord, arrayListOf(16))
                }
            }

            true
        }

        popup.show()
    }


    /**
     * Highlight features
     */
    private fun highlightFeature(pRecord: GameRecordArray, pFeatureIdList: ArrayList<Int>) {
        val board = KaruahChessEngineC()

        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)

        // Combine features in the list
        var allFeatures: ULong  = 0UL
        for(featureId in pFeatureIdList)
        {
            allFeatures = allFeatures or board.getFeature(featureId)
        }

        if (allFeatures != 0uL) {
            binding.boardPanelLayout.setRedHighlightFullFadeOut(allFeatures)
        }
        else {
            showMessage("None found.","", Toast.LENGTH_SHORT)
        }

    }


}

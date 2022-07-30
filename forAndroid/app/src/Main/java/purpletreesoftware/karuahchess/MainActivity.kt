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
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.view.MenuCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import purpletreesoftware.karuahchess.common.*
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
import purpletreesoftware.karuahchess.R.color
import java.io.InputStream


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), TilePanel.OnTilePanelInteractionListener, PieceEditTool.OnPieceEditToolInteractionListener, PawnPromotion.OnPawnPromotionInteractionListener {

    private var dbHelper: DatabaseHelper? = null
    private lateinit var pieceMove: Move
    private var userInteracted: Boolean = false
    private var lockPanel = false
    private var computerMoveProcessing = false
    private var computerHintProcessing = false
    private var userMoveProcessing = false
    private lateinit var textReader: TextReader
    private var editMenuItem: MenuItem? = null
    private val bufferTempBoard = KaruahChessEngineC()
    private val hintBoard = KaruahChessEngineC()
    private val pieceChannel = Channel<Int>()
    private var sndPool: SoundPool? = null
    private var sndMove: Int = 0
    lateinit var binding: ActivityMainBinding
    private val navThrottler = Semaphore(1)

    // Coroutine job
    private val mainjob = SupervisorJob()
    val uiScope = CoroutineScope(Dispatchers.Main + mainjob)

    // Public variables
    enum class BoardStatusEnum(val value: Int) { Ready(0), Checkmate(1), Stalemate(2), Resigned(3), TimeExpired(4) }
    enum class PawnPromotionEnum(val value: Int) { Knight(2), Bishop(3), Rook(4), Queen(5) }

    // View Models
    private lateinit var castlingRightsVM: CastlingRightsViewModel
    private lateinit var aboutVM: AboutViewModel
    private lateinit var pawnPromotionVM: PawnPromotionViewModel
    private lateinit var pieceEditToolVM: PieceEditToolViewModel


    // Instance bundle keys for saving and restoring the state
    private val clockWhiteRemainingNanoKEY = "clockwhiteremaining"
    private val clockBlackRemainingNanoKEY = "clockblackremaining"
    private val clockPausedKEY = "clockpaused"



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
        dbHelper = DatabaseHelper.getInstance(this)

        // Text reader
        textReader = TextReader(this)
        
        // View Models
        aboutVM = ViewModelProvider(this).get(AboutViewModel::class.java)
        castlingRightsVM = ViewModelProvider(this).get(CastlingRightsViewModel::class.java)
        pawnPromotionVM = ViewModelProvider(this).get(PawnPromotionViewModel::class.java)
        pieceEditToolVM = ViewModelProvider(this).get(PieceEditToolViewModel::class.java)

        // Current orientation
        val currentOrientation :Int = this.resources.configuration.orientation

        // Adjacent control gravity and orientation
        val adjacentControlLayout = binding.adjacentControlLayout
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Change the gravity so it attaches to the right of the board
            val params = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT
            )
            params.gravity = Gravity.TOP or Gravity.END
            params.anchorGravity = Gravity.TOP or Gravity.END
            params.anchorId = binding.boardPanelLayout.id
            adjacentControlLayout.layoutParams = params

            // set the orientation to horizontal
            adjacentControlLayout.orientation = LinearLayout.HORIZONTAL
        }
        else {
            // Change the gravity so it attaches to the bottom of the board
            val mainParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            )
            mainParams.gravity = Gravity.START or Gravity.BOTTOM
            mainParams.anchorGravity = Gravity.START or Gravity.BOTTOM
            mainParams.anchorId = binding.boardPanelLayout.id
            adjacentControlLayout.layoutParams = mainParams

            // set the orientation to vertical
            adjacentControlLayout.orientation = LinearLayout.VERTICAL
        }

        // Set floating action button orientation
        val fabLayout = binding.floatingActionButtonLayout

        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) { fabLayout.orientation = LinearLayout.VERTICAL }
        else { fabLayout.orientation = LinearLayout.HORIZONTAL }

        // Last move action button
        binding.showLastMoveAction.setOnClickListener {showLastMove() }

        // Set new action button
        binding.newAction.setOnClickListener {
            newGame()
        }

        // Set hints action button
        setHint()
        binding.showHintAction.setOnClickListener {
            showHint()
        }

        // Set level indicator
        refreshEngineSettingsLevelIndicator()

        // Set board layout listener
        binding.boardPanelLayout.setTilePanelInteractionListener(this)

        // Move
        pieceMove = Move(binding.boardPanelLayout)


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

            // Clock
            val clockEnabled = ParameterDataService.get(ParamClock::class.java).enabled
            if (clockEnabled) {
                binding.clockLayout.show()
                loadChessClock(true, GameRecordDataService.getCurrentGame())

                // Restore the clock state
                if (savedInstanceState != null) {
                    binding.clockLayout.whiteClock.setNewLimitNano(savedInstanceState.getLong(clockWhiteRemainingNanoKEY,0))
                    binding.clockLayout.blackClock.setNewLimitNano(savedInstanceState.getLong(clockBlackRemainingNanoKEY,0))
                    if (!savedInstanceState.getBoolean(clockPausedKEY,false)) {
                        binding.clockLayout.start(GameRecordDataService.currentGame.getStateActiveColour())
                    }
                }
            }
            else {
                binding.clockLayout.hide()
            }

            // Set shake
            val arrangeBoardEnabled = ParameterDataService.get(ParamArrangeBoard::class.java).enabled
            binding.boardPanelLayout.shake(arrangeBoardEnabled)

            // Set current record position
            uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)

        menu.findItem(R.menu.menu_main)

        // Set initial values
        menu.findItem(R.id.action_highlight).isChecked = ParameterDataService.get(ParamMoveHighlight::class.java).enabled
        editMenuItem = menu.findItem(R.id.action_edit)
        editMenuItem?.isChecked = ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        menu.findItem(R.id.action_clock).isChecked = ParameterDataService.get(ParamClock::class.java).enabled
        menu.findItem(R.id.action_coordinates).isChecked = ParameterDataService.get(ParamBoardCoord::class.java).enabled
        menu.findItem(R.id.action_navigator).isChecked = ParameterDataService.get(ParamNavigator::class.java).enabled
        menu.findItem(R.id.action_hint).isChecked = ParameterDataService.get(ParamHint::class.java).enabled

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
                uiScope.launch(Dispatchers.Main) {
                    undoMove()
                }
                true
            }
            R.id.action_switchdirection -> {
                //Cancel any move tasks
                stopSearchJob()

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
                    checkChessClock()
                }

                true
            }
            R.id.action_soundsettings -> {
                showSoundColourSettingsDialog()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            R.id.action_clock -> {
                val clock = ParameterDataService.get(ParamClock::class.java)
                clock.enabled = !item.isChecked
                item.isChecked = clock.enabled
                ParameterDataService.set(clock)
                if (clock.enabled) {
                    binding.clockLayout.show()
                    loadChessClock(true, GameRecordDataService.getCurrentGame())
                }
                else {
                    binding.clockLayout.hide()
                }

                if(clock.enabled) showMessage("${item.title} is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled","", Toast.LENGTH_SHORT)

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

                refreshNavigation(pReload = true, pScroll = true)

                if(navParam.enabled) showMessage("${item.title} is enabled", "", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled", "", Toast.LENGTH_SHORT)
                true
            }
            R.id.action_boardsettings -> {
                showBoardSettingsDialog()
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
            R.id.action_hint -> {
                val hint = ParameterDataService.get(ParamHint::class.java)
                hint.enabled = !item.isChecked
                item.isChecked = hint.enabled
                ParameterDataService.set(hint)
                if(hint.enabled) showMessage("${item.title} button is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} button is disabled","", Toast.LENGTH_SHORT)

                setHint()
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
            binding.boardPanelLayout.setHighlightFullFadeOut(lastChanges, color.colorMagenta)
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
        userInteracted = true
        pieceMove.Clear()


        if (pFromTile != null && pToTile != null && !lockPanel) {
            if(!arrangeBoardEnabled) {
                uiScope.launch(Dispatchers.Main) {
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
        userInteracted = true

        if (pTile != null && !gameFinished && !lockPanel && !arrangeBoardEnabled) {

            uiScope.launch(Dispatchers.Main) {
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
    private fun arrangeUpdate(pFen: Char, pToIndex: Int) {
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                bufferTempBoard.setBoardArray(record.boardArray)
                bufferTempBoard.setStateArray(record.stateArray)

                val mResult = bufferTempBoard.arrangeUpdate(pFen, pToIndex)
                if (mResult.success) {
                    record.boardArray = bufferTempBoard.getBoardArray()
                    record.stateArray = bufferTempBoard.getStateArray()

                    BoardSquareDataService.update(binding.boardPanelLayout,record)
                    GameRecordDataService.updateGameState(record)
                } else {
                    showMessage(mResult.returnMessage,"", Toast.LENGTH_SHORT)
                }

                // Clear the move selected
                pieceMove.Clear()
            }
        }
    }

    /**
     * Moves a piece from one square to another
     */
    private fun arrangeUpdate(pFromIndex: Int, pToIndex: Int) {
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                bufferTempBoard.setBoardArray(record.boardArray)
                bufferTempBoard.setStateArray(record.stateArray)
                val mResult = bufferTempBoard.arrange(pFromIndex, pToIndex)
                if (mResult.success) {
                    record.boardArray = bufferTempBoard.getBoardArray()
                    record.stateArray = bufferTempBoard.getStateArray()
                    BoardSquareDataService.update(binding.boardPanelLayout,record)
                    GameRecordDataService.updateGameState(record)
                } else {
                    showMessage(mResult.returnMessage,"", Toast.LENGTH_SHORT)
                }

                // Clear the move selected
                pieceMove.Clear()
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
            pieceMove.Clear()
            return
        }

        // Check the clock
        checkChessClock()

        // Select highlight mode
        val highlight = if(moveHighlightEnabled) HighlightEnum.MovePath else HighlightEnum.Select

        // Create proposed move
        val moveSelected = pieceMove.Add(pTile.index, GameRecordDataService.currentGame, highlight)

        // Restart the computer move (if required)
        startComputerMoveTask()

        if (moveSelected) {
            userMoveProcessing = true
            val boardBeforeMove = GameRecordDataService.getCurrentGame()

            // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
            var promotionPiece: Int = PawnPromotionEnum.Queen.value // default
            if (GameRecordDataService.currentGame.isPawnPromotion(pieceMove.fromIndex, pieceMove.toIndex)) {
                promotionPiece = showPawnPromotionDialog(GameRecordDataService.currentGame.getStateActiveColour())
            }

            val gameStatusBeforeMove = GameRecordDataService.currentGame.getStateGameStatus()
            val moveResult = GameRecordDataService.currentGame.move(pieceMove.fromIndex, pieceMove.toIndex, promotionPiece, pValidateEnabled = true, pCommit = true)

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
                        this,
                        1200L
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationList)
                }

                // Update display
                BoardSquareDataService.update(binding.boardPanelLayout, GameRecordDataService.getCurrentGame())

                // Record game state
                recordCurrentGameState()

                // Check the clock
                checkChessClock()

                // Piece move sound effect
                playPieceMoveSoundEffect()

                // Update score if checkmate occurred
                if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && GameRecordDataService.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value)
                {
                    val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(GameRecordDataService.currentGame.getStateActiveColour())
                    val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
                    startPieceAnimation(true, kingFallSeq)
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
            pieceMove.Clear()
            userMoveProcessing = false
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
        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE
        val turnColour = GameRecordDataService.currentGame.getStateActiveColour()
        val boardStatus = GameRecordDataService.currentGame.getStateGameStatus()

        if (boardStatus == 0 && (!computerMoveProcessing) && (!computerHintProcessing) && (!arrangeBoardEnabled) && computerPlayerEnabled && computerColour == turnColour) {
            lockPanel = true
            computerMoveProcessing = true

            // Clear the user move since starting computer move
            pieceMove.Clear()

            binding.moveProgressBar.visibility = View.VISIBLE

            val searchOptions = SearchOptions()
            searchOptions.randomiseFirstMove = ParameterDataService.get(ParamRandomiseFirstMove::class.java).enabled
            searchOptions.limitSkillLevel =  ParameterDataService.get(ParamLimitSkillLevel::class.java).level

            val limitAdvancedEnabled = ParameterDataService.get(ParamLimitAdvanced::class.java).enabled
            if (limitAdvancedEnabled) {
                searchOptions.limitDepth = ParameterDataService.get(ParamLimitDepth::class.java).depth
                searchOptions.limitNodes = ParameterDataService.get(ParamLimitNodes::class.java).nodes
                searchOptions.limitMoveDuration = ParameterDataService.get(ParamLimitMoveDuration::class.java).moveDurationMS
                searchOptions.limitThreads = ParameterDataService.get(ParamLimitThreads::class.java).threads
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
                val moveResult = GameRecordDataService.currentGame.move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, pValidateEnabled = true, pCommit = true)
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
                        this,
                        1200L
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationList)


                    // Update display
                    BoardSquareDataService.update(binding.boardPanelLayout, GameRecordDataService.getCurrentGame())

                    // Record game state
                    recordCurrentGameState()

                    // Check the clock
                    checkChessClock()


                    // Piece move sound effect
                    playPieceMoveSoundEffect()

                    // do if checkmate occurred
                    if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && GameRecordDataService.currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value) {
                        val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(GameRecordDataService.currentGame.getStateActiveColour())
                        val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
                        startPieceAnimation(true, kingFallSeq)
                        afterCheckMate(GameRecordDataService.currentGame)
                    }


                }
            }
            else {
                if (topMove.error > 0) {
                    showMessage("Invalid board configuration. ${topMove.errorMessage} ", "", Toast.LENGTH_LONG)
                }
            }


            // Unlock panel, stop the progress indicator
            binding.moveProgressBar.visibility = View.GONE
            computerMoveProcessing = false
            lockPanel = false

        }

        return
    }


    /**
     * Find the best move for a board layout
     */
    private suspend fun startHintTask(pRecord: GameRecordArray)
    {
        hintBoard.setBoardArray(pRecord.boardArray)
        hintBoard.setStateArray(pRecord.stateArray)

        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        val boardStatus = hintBoard.getStateGameStatus()

        if (boardStatus == 0 && (!computerMoveProcessing) && (!computerHintProcessing) && (!arrangeBoardEnabled) ) {
            lockPanel = true
            computerHintProcessing = true

            binding.moveProgressBar.visibility = View.VISIBLE

            val searchOptions = SearchOptions()
            searchOptions.randomiseFirstMove = false
            searchOptions.limitSkillLevel = 20

            val limitAdvancedEnabled = ParameterDataService.get(ParamLimitAdvanced::class.java).enabled
            if (limitAdvancedEnabled) {
                searchOptions.limitDepth = ParameterDataService.get(ParamLimitDepth::class.java).depth
                searchOptions.limitNodes = ParameterDataService.get(ParamLimitNodes::class.java).nodes
                searchOptions.limitMoveDuration = ParameterDataService.get(ParamLimitMoveDuration::class.java).moveDurationMS
                searchOptions.limitThreads = ParameterDataService.get(ParamLimitThreads::class.java).threads
            } else {
                searchOptions.limitDepth = 10
                searchOptions.limitNodes = 500000000
                searchOptions.limitMoveDuration = 0
                searchOptions.limitThreads = if (Runtime.getRuntime().availableProcessors() > 1) Runtime.getRuntime().availableProcessors() - 1 else 1
            }

            val topMove = withContext(Dispatchers.IO) { hintBoard.searchStart(searchOptions) }

            if ((!topMove.cancelled) && (topMove.error == 0)) {
                val topMoveBits: ULong = (Constants.BITMASK shr topMove.moveFromIndex) or (Constants.BITMASK shr topMove.moveToIndex)
                val boardColourIndex = Constants.darkSquareColourList.indexOf(ParameterDataService.get(ParamColourDarkSquares::class.java).argb())
                var highlightColour = color.colorMagenta
                if (boardColourIndex > -1) {
                    highlightColour = Constants.hintColourList[boardColourIndex]
                }
                binding.boardPanelLayout.setHighlightFullFadeOut(topMoveBits, highlightColour)
                showMessage("Best move hint","", Toast.LENGTH_SHORT)
            }
            else {
                if (topMove.error > 0) {
                    showMessage("Invalid board configuration. ${topMove.errorMessage} ", "", Toast.LENGTH_LONG)
                }
            }

            binding.moveProgressBar.visibility = View.GONE
            computerHintProcessing = false
            lockPanel = false
        }
        else {
            when {
                boardStatus != 0 -> {
                    showMessage("Unable to show a hint as the game has finished.", "", Toast.LENGTH_LONG)
                }
                arrangeBoardEnabled -> {
                    showMessage("Unable to show a hint in edit mode.", "", Toast.LENGTH_LONG)
                }
                computerMoveProcessing -> {
                    showMessage("Unable to show a hint as processing a move.", "", Toast.LENGTH_LONG)
                }
            }
        }


    }


    /**
     * Show message
     */
    fun showMessage(pTextFull: String, pTextShort: String, pDuration: Int) {

        if (pTextFull.trim() != "") {
            if (pTextShort.trim() == "") readText(pTextFull)
            else readText(pTextShort)

            val toast = Toast.makeText(this, pTextFull, pDuration)
            toast.show()
        }
    }

    /**
     * Converts a text string to speech
     */
    private fun readText(pText: String) {
        val soundRead = ParameterDataService.get(ParamSoundRead::class.java)
        if (textReader.ready && soundRead.enabled) {
            textReader.tts?.speak(pText, TextToSpeech.QUEUE_ADD, null, "")
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
            getPieceSpinSSML(origFromSpin) + " takes " + getPieceSpinSSML(origToSpin)
        }
        else
        {
            getPieceSpinSSML(origFromSpin) + " to " + Constants.BoardCoordinateDict[toIndex]
        }


        return ssml
    }

    /**
     * Get ssml for piece spin
     */
    private fun getPieceSpinSSML(pPieceSpin: Int): String
    {
        val board = KaruahChessEngineC()
        return board.getPieceNameFromChar(board.getFENCharFromSpin(pPieceSpin))

    }

    /**
     * Starts the animation
     */
    private suspend fun startPieceAnimation(pLockPanel : Boolean, pAnimationList : ArrayList<TileAnimationInstruction>)
    {

        if (pLockPanel) {
            lockPanel = true
        }


       binding.animationPanelLayout.runAnimation(binding.boardPanelLayout, pAnimationList, binding.boardPanelLayout.framePadding)


        if (pLockPanel) {
            lockPanel = false
        }
    }

    /**
     * Starts a new game
     */
    private fun newGame(){

        val positiveButtonClick = { _: DialogInterface, _: Int ->
            lockPanel = true
            stopSearchJob()

            val clockEnabled = ParameterDataService.get(ParamClock::class.java).enabled
            if (clockEnabled) {
                val defaultSecondsIndex = ParameterDataService.get(ParamClockDefault::class.java).index
                if (defaultSecondsIndex < Constants.clockResetSeconds.size) {
                    val defaultSeconds = Constants.clockResetSeconds[defaultSecondsIndex]
                    GameRecordDataService.reset(defaultSeconds, defaultSeconds)
                    binding.clockLayout.setClock(defaultSeconds, defaultSeconds)
                }
                else {
                    GameRecordDataService.reset(0, 0)
                    binding.clockLayout.setClock(0, 0)
                }
            }
            else {
                GameRecordDataService.reset(0,0)
            }

            uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
            lockPanel = false

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

        stopSearchJob()
        val boardBeforeUndo = GameRecordDataService.get()
        val undo = GameRecordDataService.undo()
        if (undo) {
            lockPanel = true
            val boardAfterUndo = GameRecordDataService.get()
            if (boardBeforeUndo != null && boardAfterUndo != null) {
                // Update the clock
                binding.clockLayout.pauseClock()
                loadChessClock(true, boardAfterUndo)

                val moveAnimationList = BoardAnimation.createAnimationList(
                    boardBeforeUndo,
                    boardAfterUndo,
                    binding.boardPanelLayout,
                    this,
                    1200L
                )
                // Do animation
                startPieceAnimation(true, moveAnimationList)
            }

            navigateMaxRecord()


            lockPanel = false
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
    fun stopSearchJob() {
        pieceMove.Clear()
        endPieceAnimation()
        GameRecordDataService.currentGame.cancelSearch()
        hintBoard.cancelSearch()
    }

    /**
     * Shows a board message dialog
     */
    private fun showBoardMessageDialog(pTitle: String, pMessage: String, pIcon: Int) {

        // Added isFinishing check to stop bad token exception is some circumstances
        if ((!(pTitle == "" && pMessage == "")) && !isFinishing) {
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
            val limitSkillLevel = ParameterDataService.get(ParamLimitSkillLevel::class.java)
            val nextSkillLevel = limitSkillLevel.level + 1
            if (nextSkillLevel in 0..Constants.skillLevelList.lastIndex)
            {
                limitSkillLevel.level = nextSkillLevel
                ParameterDataService.set(limitSkillLevel)
                refreshEngineSettingsLevelIndicator()
                showBoardMessageDialog("Level Increase", "Congratulations, you have now progressed to the next level. The engine playing strength is now set to ${Constants.skillLevelList[nextSkillLevel]}.",R.drawable.ic_goldstar)

            }

        }

    }

    /**
     * Updates all the board indicators
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
        val clockEnabled = ParameterDataService.get(ParamClock::class.java).enabled

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
        else if (clockEnabled && board.getStateGameStatus() == BoardStatusEnum.TimeExpired.value && binding.clockLayout.whiteClock.remainingNano() == 0L && binding.clockLayout.blackClock.remainingNano() > 0L) {
            gameFinished = true
            title = "Black wins! White time has expired."
        }
        else if (clockEnabled && board.getStateGameStatus() == BoardStatusEnum.TimeExpired.value && binding.clockLayout.whiteClock.remainingNano() > 0L && binding.clockLayout.blackClock.remainingNano() == 0L) {
            gameFinished = true
            title = "White wins! Black time has expired."
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
        binding.boardPanelLayout.drawTiles(binding.mainFrame.width, binding.mainFrame.height, approxBoardMarginPixels)

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
    fun rotateBoard(pRotation: Int) {
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
        var whiteClock: Int = 0
        var blackClock: Int = 0

        // Update the clock offset before saving
        val clockEnabled = ParameterDataService.get(ParamClock::class.java).enabled
        if (clockEnabled) {
            whiteClock = binding.clockLayout.whiteClock.remainingNano().nanoSecondsToSeconds().toInt()
            blackClock = binding.clockLayout.blackClock.remainingNano().nanoSecondsToSeconds().toInt()
        }

        // Record current game state
        val success = GameRecordDataService.recordGameState(whiteClock, blackClock)
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
        stopSearchJob()
        val fm = supportFragmentManager
        val importPGNDialogFragment = ImportPGN.newInstance()
        importPGNDialogFragment.show(fm, null)
    }

    /**
     * Show Castling dialog
     */
    private fun showCastlingRightsDialog(pKingSpin: Int) {
        // Cancel any move jobs first
        stopSearchJob()

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
     * Show clock settings dialog
     */
    fun showClockSettingsDialog() {

        val fm = supportFragmentManager
        val clockSettingsDialogFragment = ClockSettings.newInstance()
        clockSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show board settings dialog
     */
    private fun showBoardSettingsDialog() {

        val fm = supportFragmentManager
        val boardSettingsDialogFragment = BoardSettings.newInstance()
        boardSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show sound settings dialog
     */
    private fun showSoundColourSettingsDialog() {

        val fm = supportFragmentManager
        val soundColourSettingsDialogFragment = SoundSettings.newInstance()
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

        return pieceChannel.receive()

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

        pieceChannel.trySend(pieceValue)
        pDialog.dismiss()
    }

    /**
     * Loads a game record from a file uri
     */
    private fun loadGameRecordFromFileUri(pFileUri: Uri) {
        try {
            stopSearchJob()
            val file: InputStream? = contentResolver.openInputStream(pFileUri)
            val importDB = ImportDB()
            val fileResult = if(file != null) {
                importDB.import(file, ImportDB.ImportTypeEnum.GameXML, this)
            }
            else { 0 }

            if(fileResult > 0) {
                uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
                loadChessClock(true, GameRecordDataService.getCurrentGame())
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
            val file = exportDB.export(ExportDB.ExportTypeEnum.GameXML, this)
            val sharedFileUri: Uri = FileProvider.getUriForFile(this, "purpletreesoftware.karuahchess.exportdata", file)

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
        navigateGameRecord(maxId, pAnimate = false, pReloadNav = true, pScrollNav = true)
    }

    /**
     * Navigate to requested game record
     */
    suspend fun navigateGameRecord(pRecId: Int, pAnimate: Boolean, pReloadNav: Boolean, pScrollNav: Boolean) {
        if (pRecId > 0)
        {
            navThrottler.acquire()

            val oldBoard : GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            val updatedBoard = GameRecordDataService.get(pRecId)

            // Update board displayed with requested record
            if (updatedBoard != null)
            {
                // Do animation
                if (pAnimate && oldBoard != null) {
                    val moveAnimationList = BoardAnimation.createAnimationList(
                        oldBoard,
                        updatedBoard,
                        binding.boardPanelLayout,
                        this,
                        400L
                    )


                    BoardSquareDataService.update(binding.boardPanelLayout, updatedBoard)
                    BoardSquareDataService.gameRecordCurrentValue = pRecId
                    updateBoardIndicators(updatedBoard)
                    loadChessClock(false, updatedBoard)
                    // Do animation
                    startPieceAnimation(true, moveAnimationList)
                }
                else {
                    // End any animations
                    endPieceAnimation()

                    BoardSquareDataService.update(binding.boardPanelLayout, updatedBoard)
                    BoardSquareDataService.gameRecordCurrentValue = pRecId
                    updateBoardIndicators(updatedBoard)
                    loadChessClock(false, updatedBoard)
                }

                pieceMove.Clear()

                // Refresh the navigation
                refreshNavigation(pReloadNav, pScrollNav)

                // Refresh shake animation
                binding.boardPanelLayout.shakeRefresh()

            }

            navThrottler.release()
        }
    }

    /**
     * Loads the move navigator
     */
    private fun refreshNavigation(pReload: Boolean, pScroll: Boolean) {
        // Refresh the navigator control
        val navParam = ParameterDataService.get(ParamNavigator::class.java)
        if(navParam.enabled) {
            binding.navigatorLayout.show()
            if (pReload) {
                val recIdList = GameRecordDataService.getAllRecordIDList()
                binding.navigatorLayout.load(recIdList, BoardSquareDataService.gameRecordCurrentValue)
            }
            else {
                binding.navigatorLayout.setSelected(BoardSquareDataService.gameRecordCurrentValue)
            }

            if (pScroll) {
                binding.navigatorLayout.scrollToSelected()
            }
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
            uiScope.launch(Dispatchers.Main) { resignGame() }
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
        if (computerPlayerEnabled && turnColour == computerColour) {
            showMessage("Cannot resign as it is not your turn.","",Toast.LENGTH_SHORT)
            return
        }

        // Ensure game record is set to the latest
        navigateMaxRecord()

        val status = GameRecordDataService.currentGame.getStateGameStatus()

        // Only resign if game is not already finished
        if (status == BoardStatusEnum.Ready.value && GameRecordDataService.currentGame.getStateFullMoveCount() > 0) {

            // Stop task if running
            stopSearchJob()

            // Set the state of the game to resign
            GameRecordDataService.currentGame.setStateGameStatus(BoardStatusEnum.Resigned.value)


            // Do animation, display message
            val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(GameRecordDataService.currentGame.getStateActiveColour())
            val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
            startPieceAnimation(true, kingFallSeq)

            // Record current game state
            recordCurrentGameState()

            // Check the chess clock
            checkChessClock()

        }
        else {
            showMessage("Resigning not available at this stage of the game","", Toast.LENGTH_LONG)
        }
    }

    /**
     * Time has expired
     */
    suspend fun timeExpired(pColourExpired: Int)
    {
        // Check that not in edit mode
        val arrangeBoardEnabled =  ParameterDataService.get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            // Cannot expire time if in edit mode
            return
        }

        // Ensure game record is set to the latest
        navigateMaxRecord()

        val status = GameRecordDataService.currentGame.getStateGameStatus()

        // Only expire time if game is not already finished
        if (status == BoardStatusEnum.Ready.value) {

            // Stop task if running
            stopSearchJob()

            // Set the state of the game to time expired
            GameRecordDataService.currentGame.setStateGameStatus(BoardStatusEnum.TimeExpired.value)

            // Do animation, display message
            val kingFallIndex = GameRecordDataService.currentGame.getKingIndex(pColourExpired)
            val kingFallSeq = BoardAnimation.createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
            startPieceAnimation(true, kingFallSeq)

            // Record current game state
            recordCurrentGameState()

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
        editMenuItem?.isChecked = edit.enabled
        if (edit.enabled) {
            stopSearchJob()
            showMessage("Edit board is enabled","", Toast.LENGTH_SHORT)
        }
        else showMessage("Edit board is disabled","", Toast.LENGTH_SHORT)

        binding.boardPanelLayout.shake(edit.enabled)

        checkChessClock()

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
     * Starts the chess clock based on current turn
     */
    fun checkChessClock() {
        val edit = ParameterDataService.get(ParamArrangeBoard::class.java).enabled

        if (edit) {
            binding.clockLayout.pauseClock()
        } else {
            // Only start the clock if the game has not finished
            val gameFinished: Boolean = GameRecordDataService.currentGame.getStateGameStatus() != BoardStatusEnum.Ready.value
            if (!gameFinished) {
                val turn: Int = GameRecordDataService.currentGame.getStateActiveColour()
                binding.clockLayout.start(turn)
            }
            else {
                binding.clockLayout.pauseClock()
            }
        }

       if ((!binding.clockLayout.isPaused()) && BoardSquareDataService.gameRecordCurrentValue == GameRecordDataService.getMaxId()) {
           binding.clockLayout.showCurrentTime()
       }

    }


    /**
     * Initialises the chess clock with values and switches between the current and historical time remaining
     */
    private fun loadChessClock(pLoadInitialOffset: Boolean, pRecord: GameRecordArray) {

        val board = KaruahChessEngineC()
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)

        // Loads the initial offset
        if (pLoadInitialOffset) {

            if (GameRecordDataService.recordCount() == 1) {
                val defaultSecondsIndex = ParameterDataService.get(ParamClockDefault::class.java).index
                if (defaultSecondsIndex < Constants.clockResetSeconds.size) {
                    val defaultSeconds = Constants.clockResetSeconds[defaultSecondsIndex]
                    if (board.getStateWhiteClockOffset() == 0 || board.getStateBlackClockOffset() == 0) {
                        initialiseClockFirstMove(defaultSeconds, defaultSeconds)
                        board.setStateWhiteClockOffset(defaultSeconds)
                        board.setStateBlackClockOffset(defaultSeconds)
                    }
                }
            }

            binding.clockLayout.whiteClock.setNewLimitNano(board.getStateWhiteClockOffset().secondsToNanoSeconds())
            binding.clockLayout.blackClock.setNewLimitNano(board.getStateBlackClockOffset().secondsToNanoSeconds())
            binding.clockLayout.showCurrentTime()
        }

        // Switches between historical and current
        if (BoardSquareDataService.gameRecordCurrentValue == GameRecordDataService.getMaxId()) {
            binding.clockLayout.showCurrentTime()
        } else {
            binding.clockLayout.showHistoricalTime(board.getStateWhiteClockOffset(), board.getStateBlackClockOffset())
        }

    }

    /**
     * Update clock for the first move
     */
    fun initialiseClockFirstMove(pWhiteSecondsRemaining: Int, pBlackSecondsRemaining: Int) {
        // Update the current record state if only one record exists
        if (GameRecordDataService.recordCount() == 1) {
            val record: GameRecordArray? = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
            if(record != null) {
                val bufferBoard = KaruahChessEngineC()
                bufferBoard.setBoardArray(record.boardArray)
                bufferBoard.setStateArray(record.stateArray)

                bufferBoard.setStateWhiteClockOffset(pWhiteSecondsRemaining)
                bufferBoard.setStateBlackClockOffset(pBlackSecondsRemaining)

                record.boardArray = bufferBoard.getBoardArray()
                record.stateArray = bufferBoard.getStateArray()

                BoardSquareDataService.update(binding.boardPanelLayout, record)
                GameRecordDataService.updateGameState(record)
            }
        }
    }

    /**
     * Refresh the level indicator
     */
    fun refreshEngineSettingsLevelIndicator() {
        val computerPlayerEnabled = ParameterDataService.get(ParamComputerPlayer::class.java).enabled
        if (computerPlayerEnabled) {
            val skillLevel = ParameterDataService.get(ParamLimitSkillLevel::class.java).level
            if (skillLevel in 0..Constants.skillLevelList.lastIndex) {
                binding.levelIndicatorText.text = "${(Constants.skillLevelList[skillLevel])}"
            }
            else {
                binding.levelIndicatorText.text = ""
            }
        }
        else {
            binding.levelIndicatorText.text = ""
        }
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


    /**
     * Show or hide the hints feature
     */
    private fun setHint() {
        val hintsEnabled = ParameterDataService.get(ParamHint::class.java).enabled
        if (hintsEnabled) {
            binding.showHintAction.visibility = View.VISIBLE
        }
        else {
            binding.showHintAction.visibility = View.GONE
        }
    }

    /**
     * Show a hint
     */
    private fun showHint() {
        val record = GameRecordDataService.get(BoardSquareDataService.gameRecordCurrentValue)
        if(record != null) {
            uiScope.launch(Dispatchers.Main) { startHintTask(record) }
        }

    }

    /**
     * Routines to run on pause
     */
    override fun onPause() {
        // Stop any searches
        GameRecordDataService.currentGame.cancelSearch()
        hintBoard.cancelSearch()

        // Close the promotion dialog
        supportFragmentManager.findFragmentByTag("PawnPromotionDialog")?.let {
            (it as DialogFragment).dismiss()

            // Cancel all jobs
            uiScope.coroutineContext.cancelChildren()
        }

        super.onPause()
    }

    /**
     * Routines to run on activity destroy
     */
    override fun onDestroy() {

        // Shutdown tts
        textReader.tts?.shutdown()


        super.onDestroy()
    }

    /**
     * Routines to run when saving instance state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the clock state if it is enabled
        val clockEnabled = ParameterDataService.get(ParamClock::class.java).enabled
        if (clockEnabled) {
            outState.putLong(clockWhiteRemainingNanoKEY, binding.clockLayout.whiteClock.remainingNano())
            outState.putLong(clockBlackRemainingNanoKEY, binding.clockLayout.blackClock.remainingNano())
            outState.putBoolean(clockPausedKEY, binding.clockLayout.isPaused())
        }


    }




}

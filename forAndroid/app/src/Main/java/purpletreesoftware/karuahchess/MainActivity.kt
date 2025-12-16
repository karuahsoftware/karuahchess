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
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.view.MenuCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import purpletreesoftware.karuahchess.R.color
import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.common.Helper
import purpletreesoftware.karuahchess.common.afterMeasured
import purpletreesoftware.karuahchess.common.dpToPx
import purpletreesoftware.karuahchess.common.nanoSecondsToSeconds
import purpletreesoftware.karuahchess.common.secondsToNanoSeconds
import purpletreesoftware.karuahchess.common.spToPx
import purpletreesoftware.karuahchess.customcontrol.About
import purpletreesoftware.karuahchess.customcontrol.ActivityFragmentFactory
import purpletreesoftware.karuahchess.customcontrol.BoardSettings
import purpletreesoftware.karuahchess.customcontrol.CastlingRights
import purpletreesoftware.karuahchess.customcontrol.ClockSettings
import purpletreesoftware.karuahchess.customcontrol.EngineSettings
import purpletreesoftware.karuahchess.customcontrol.HintSettings
import purpletreesoftware.karuahchess.customcontrol.ImportPGN
import purpletreesoftware.karuahchess.customcontrol.MoveNavigatorPGN
import purpletreesoftware.karuahchess.customcontrol.PawnPromotion
import purpletreesoftware.karuahchess.customcontrol.PieceEditTool
import purpletreesoftware.karuahchess.customcontrol.PieceSettings
import purpletreesoftware.karuahchess.customcontrol.SoundSettings
import purpletreesoftware.karuahchess.customcontrol.Tile
import purpletreesoftware.karuahchess.customcontrol.TileAnimationInstruction
import purpletreesoftware.karuahchess.customcontrol.TilePanel
import purpletreesoftware.karuahchess.database.DatabaseHelper
import purpletreesoftware.karuahchess.database.ExportDB
import purpletreesoftware.karuahchess.database.ImportDB
import purpletreesoftware.karuahchess.databinding.ActivityMainBinding
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import purpletreesoftware.karuahchess.engine.SearchOptions
import purpletreesoftware.karuahchess.engine.SearchResult
import purpletreesoftware.karuahchess.model.boardsquare.BoardSquareDataService
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.ParamArrangeBoard
import purpletreesoftware.karuahchess.model.parameterobj.ParamBoardCoord
import purpletreesoftware.karuahchess.model.parameterobj.ParamClock
import purpletreesoftware.karuahchess.model.parameterobj.ParamClockDefault
import purpletreesoftware.karuahchess.model.parameterobj.ParamColourDarkSquares
import purpletreesoftware.karuahchess.model.parameterobj.ParamComputerMoveFirst
import purpletreesoftware.karuahchess.model.parameterobj.ParamComputerPlayer
import purpletreesoftware.karuahchess.model.parameterobj.ParamHint
import purpletreesoftware.karuahchess.model.parameterobj.ParamHintMove
import purpletreesoftware.karuahchess.model.parameterobj.ParamLargePawn
import purpletreesoftware.karuahchess.model.parameterobj.ParamLevelAuto
import purpletreesoftware.karuahchess.model.parameterobj.ParamLimitAdvanced
import purpletreesoftware.karuahchess.model.parameterobj.ParamLimitDepth
import purpletreesoftware.karuahchess.model.parameterobj.ParamLimitMoveDuration
import purpletreesoftware.karuahchess.model.parameterobj.ParamLimitSkillLevel
import purpletreesoftware.karuahchess.model.parameterobj.ParamLimitThreads
import purpletreesoftware.karuahchess.model.parameterobj.ParamMoveHighlight
import purpletreesoftware.karuahchess.model.parameterobj.ParamMoveSpeed
import purpletreesoftware.karuahchess.model.parameterobj.ParamNavigator
import purpletreesoftware.karuahchess.model.parameterobj.ParamPromoteAuto
import purpletreesoftware.karuahchess.model.parameterobj.ParamRandomiseFirstMove
import purpletreesoftware.karuahchess.model.parameterobj.ParamRotateBoard
import purpletreesoftware.karuahchess.model.parameterobj.ParamSoundEffect
import purpletreesoftware.karuahchess.model.parameterobj.ParamSoundRead
import purpletreesoftware.karuahchess.rules.BoardAnimation
import purpletreesoftware.karuahchess.rules.Move
import purpletreesoftware.karuahchess.rules.Move.HighlightEnum
import purpletreesoftware.karuahchess.sound.TextReader
import purpletreesoftware.karuahchess.viewmodel.AboutViewModel
import purpletreesoftware.karuahchess.viewmodel.CastlingRightsViewModel
import purpletreesoftware.karuahchess.viewmodel.PawnPromotionViewModel
import java.io.InputStream
import java.util.SortedMap


@ExperimentalUnsignedTypes
open class MainActivity(pActivityID: Int) : AppCompatActivity(), TilePanel.OnTilePanelInteractionListener, PawnPromotion.OnPawnPromotionInteractionListener {


    // Instance ID
    private val activityID: Int = pActivityID
    private val activityFragmentFactory = ActivityFragmentFactory(pActivityID)
    private var dbHelper: DatabaseHelper? = null
    private var pieceMove: Move? = null
    private var userInteracted: Boolean = false
    private var lockPanel = false
    private var computerMoveProcessing = false
    private var computerHintProcessing = false
    private var userMoveProcessing = false
    private var textReader: TextReader? = null
    private var editMenuItem: MenuItem? = null
    private lateinit var bufferTempBoard: KaruahChessEngine
    private lateinit var hintBoard: KaruahChessEngine
    private val pieceChannel = Channel<Int>()
    private var sndPool: SoundPool? = null
    private var sndMove: Int = 0
    lateinit var binding: ActivityMainBinding
    private val navThrottler: Semaphore = Semaphore(1)
    private var resizeRunning: Boolean = false
    private val resizeHandler: Handler = Handler(Looper.getMainLooper())
    private var editSelection: ULong = 0uL
    private var editLastTapIndex: Int = -1

    // Coroutine job
    private val mainjob = SupervisorJob()
    val uiScope = CoroutineScope(Dispatchers.Main + mainjob)

    // Public variables
    enum class BoardStatusEnum(val value: Int) { Ready(0), Checkmate(1), Stalemate(2), Resigned(3), TimeExpired(4) }
    enum class PawnPromotionEnum(val value: Int) { Knight(2), Bishop(3), Rook(4), Queen(5) }

    // View Models
    private var castlingRightsVM: CastlingRightsViewModel? = null
    private var aboutVM: AboutViewModel? = null
    private var pawnPromotionVM: PawnPromotionViewModel? = null

    // Instance bundle keys for saving and restoring the state
    private val clockWhiteRemainingNanoKEY = "clockwhiteremaining"
    private val clockBlackRemainingNanoKEY = "clockblackremaining"
    private val clockPausedKEY = "clockpaused"

    // File Picker
    private var pendingExportFile: java.io.File? = null
    private val createGameExportDocLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/gzip")) { uri: Uri? ->
            val src = pendingExportFile
            pendingExportFile = null
            if (uri == null || src == null) return@registerForActivityResult
            try {
                contentResolver.openOutputStream(uri)?.use { out ->
                    src.inputStream().use { inp -> inp.copyTo(out) }
                }
                showMessage("Game saved to device.", "", Toast.LENGTH_SHORT)
            } catch (e: Exception) {
                showMessage("Could not save file. ${e.message}", "Could not save file.", Toast.LENGTH_LONG)
            }
        }
    private val loadGameRecordFromFileUriLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri: Uri? ->
        if (fileUri != null) loadGameRecordFromFileUri(fileUri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = activityFragmentFactory
        super.onCreate(savedInstanceState)

        // Set engine boards
        bufferTempBoard = KaruahChessEngine(App.appContext, activityID)
        hintBoard = KaruahChessEngine(App.appContext, activityID)


        // database helper
        dbHelper = DatabaseHelper.getInstance(this)
        dbHelper?.let {
            val status = it.initDB(activityID)

            if (status != DatabaseHelper.DB_OK) {
                // Show error activity
                val newWindowIntent = Intent(this, ErrorActivity::class.java)
                newWindowIntent.addFlags(
                     Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(newWindowIntent)

                // Exit the main activity
                finish()
                return
            }
        }

        // Text reader
        textReader = TextReader(this)

        // View binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Set content view
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarTop)

        // View Models
        aboutVM = ViewModelProvider(this).get(AboutViewModel::class.java)
        castlingRightsVM = ViewModelProvider(this).get(CastlingRightsViewModel::class.java)
        pawnPromotionVM = ViewModelProvider(this).get(PawnPromotionViewModel::class.java)

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

        // Edit tool piece button
        binding.editTileAction.setOnClickListener {
            showPieceEditToolDialog()
        }

        // Edit tool erase piece button
        binding.editEraseAction.setOnClickListener {
            editEraseSelection()
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
        BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout, GameRecordDataService.getInstance(activityID).getCurrentGame())

        val mainFrame = binding.mainFrame



        mainFrame.afterMeasured {
            // Draw the board
            resizeHandler.postDelayed({
                resizeBoard()
            }, 600)

            // Move progress bar visibility
            binding.moveProgressBar.visibility = View.GONE

            // Apply board colour
            applyBoardColour()

            // Set large pawns
            setLargePawns()

            // Clock
            val clockEnabled = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled
            if (clockEnabled) {
                binding.clockLayout.show()
                loadChessClock(true, GameRecordDataService.getInstance(activityID).getCurrentGame())

                // Restore the clock state
                if (savedInstanceState != null) {
                    binding.clockLayout.whiteClock.setNewLimitNano(savedInstanceState.getLong(clockWhiteRemainingNanoKEY,0))
                    binding.clockLayout.blackClock.setNewLimitNano(savedInstanceState.getLong(clockBlackRemainingNanoKEY,0))
                    if (!savedInstanceState.getBoolean(clockPausedKEY,false)) {
                        binding.clockLayout.start(GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour())
                    }
                }
            }
            else {
                binding.clockLayout.hide()
            }

            // Set shake
            val arrangeBoardEnabled = ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
            binding.boardPanelLayout.shake(arrangeBoardEnabled)

            // Set fab buttons
            setFabActionButtons(arrangeBoardEnabled)

            // Set current record position
            uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }


        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (!resizeRunning) {
            resizeRunning =  true
            resizeHandler.postDelayed({
                resizeBoard()
                refreshNavigation(true, false)
                resizeRunning = false
            }, 600)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //noinspection RestrictedApi
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)

        menu.findItem(R.menu.menu_main)

        // Set initial values
        menu.findItem(R.id.action_highlight).isChecked = ParameterDataService.getInstance(activityID).get(ParamMoveHighlight::class.java).enabled
        editMenuItem = menu.findItem(R.id.action_edit)
        editMenuItem?.isChecked = ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        menu.findItem(R.id.action_clock).isChecked = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled
        menu.findItem(R.id.action_coordinates).isChecked = ParameterDataService.getInstance(activityID).get(ParamBoardCoord::class.java).enabled
        menu.findItem(R.id.action_navigator).isChecked = ParameterDataService.getInstance(activityID).get(ParamNavigator::class.java).enabled

        if (activityID == 0) {
            menu.findItem(R.id.action_activitySecondWindowOpen).setVisible(true)
            menu.findItem(R.id.action_activitySecondWindowExit).setVisible(false)
        }
        else {
            menu.findItem(R.id.action_activitySecondWindowOpen).setVisible(false)
            menu.findItem(R.id.action_activitySecondWindowExit).setVisible(true)
        }
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
                val highlight = ParameterDataService.getInstance(activityID).get(ParamMoveHighlight::class.java)
                highlight.enabled = !item.isChecked
                item.isChecked = highlight.enabled
                ParameterDataService.getInstance(activityID).set(highlight)
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
                val board = KaruahChessEngine(App.appContext, activityID)
                val record = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
                if(record != null) {
                    board.setBoardArray(record.boardArray)
                    board.setStateArray(record.stateArray)
                    // Flip direction
                    board.setStateActiveColour(board.getStateActiveColour() * (-1))
                    record.stateArray = board.getStateArray()
                    GameRecordDataService.getInstance(activityID).updateGameState(record)
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
                val clock = ParameterDataService.getInstance(activityID).get(ParamClock::class.java)
                clock.enabled = !item.isChecked
                item.isChecked = clock.enabled
                ParameterDataService.getInstance(activityID).set(clock)
                if (clock.enabled) {
                    binding.clockLayout.show()
                    loadChessClock(true, GameRecordDataService.getInstance(activityID).getCurrentGame())
                }
                else {
                    binding.clockLayout.hide()
                }

                if(clock.enabled) showMessage("${item.title} is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled","", Toast.LENGTH_SHORT)

                resizeBoard()
                true
            }
            R.id.action_coordinates -> {
                val coordinates = ParameterDataService.getInstance(activityID).get(ParamBoardCoord::class.java)
                coordinates.enabled = !item.isChecked
                item.isChecked = coordinates.enabled
                ParameterDataService.getInstance(activityID).set(coordinates)
                if(coordinates.enabled) showMessage("${item.title} is enabled","", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled","", Toast.LENGTH_SHORT)

                resizeBoard()
                true
            }
            R.id.action_navigator -> {
                val isChecked = !item.isChecked
                item.isChecked = isChecked
                setMoveNavigatorPanel(isChecked)

                if(isChecked) showMessage("${item.title} is enabled", "", Toast.LENGTH_SHORT)
                else showMessage("${item.title} is disabled", "", Toast.LENGTH_SHORT)

                true
            }
            R.id.action_boardsettings -> {
                showBoardSettingsDialog()
                true
            }
            R.id.action_piecesettings -> {
                showPieceSettingsDialog()
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
                showHintSettingsDialog()
                true
            }
            R.id.action_activitySecondWindowOpen -> {
                if (activityID == 0) {
                    val newWindowIntent = Intent(this, MainActivity1::class.java)
                    newWindowIntent.addFlags(
                        Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    )

                    startActivity(newWindowIntent)
                }

                true
            }
            R.id.action_activitySecondWindowExit -> {
                if (activityID == 1) {
                    finish()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * Show last move made
     */
    private fun showLastMove() {
        val currentBoard = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
        val previousBoard = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue - 1)
        val lastChanges = GameRecordDataService.getInstance(activityID).getBoardSquareChanges(currentBoard, previousBoard)

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
        val arrangeBoardEnabled = ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        userInteracted = true
        pieceMove?.Clear()

        if (pFromTile != null && pToTile != null && !lockPanel) {
            if(!arrangeBoardEnabled) {
                uiScope.launch(Dispatchers.Main) {
                    userMoveAdd(pFromTile, false)
                    userMoveAdd(pToTile, false)
                }
            }
            else {
                editLastTapIndex = -1
                arrangeUpdate(pFromTile.index, pToTile.index)
            }

        }
    }

    /**
     * Listens for tile click events
     */
    override fun onTileClick(pTile: Tile?) {

        val arrangeBoardEnabled = ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        val gameFinished: Boolean = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus() != BoardStatusEnum.Ready.value
        userInteracted = true

        if (pTile != null && !lockPanel && !arrangeBoardEnabled) {
            if (!gameFinished) {
                uiScope.launch(Dispatchers.Main) {
                    userMoveAdd(pTile, true)
                }
            }
            else {
                uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
            }
        }
        else if (pTile != null && arrangeBoardEnabled) {
            if (pTile.spin == Constants.WHITE_KING_SPIN || pTile.spin == Constants.BLACK_KING_SPIN) {
                showCastlingRightsDialog(pTile.spin)
            }
            else {
                val tileIndexBit: ULong  = Constants.BITMASK shr pTile.index
                if (editSelection and tileIndexBit == 0uL) {
                    // Select the tile
                    editSelection = editSelection or tileIndexBit
                    editLastTapIndex = pTile.index
                }
                else if (pTile.spin != 0 && editLastTapIndex == pTile.index && ((editSelection and tileIndexBit) > 0uL )) {
                    // Attempt to select all pieces of the same type
                    var found = false
                    for (tileIndex in 0..63) {
                        if (pTile.index != tileIndex && binding.boardPanelLayout.getTile(tileIndex)?.spin != 0 && binding.boardPanelLayout.getTile(tileIndex)?.spin == pTile.spin && (editSelection and (Constants.BITMASK shr tileIndex) == 0uL)) {
                           editSelection = editSelection or (Constants.BITMASK shr tileIndex)
                           found = true
                        }
                    }

                    // If no similar pieces found just toggle the selection
                    if (!found) {
                        editSelection = editSelection xor tileIndexBit
                    }

                    editLastTapIndex = -1
                } else {
                    // Toggle the selection
                    editSelection = editSelection xor tileIndexBit
                    editLastTapIndex = -1
                }
                binding.boardPanelLayout.setHighlightEdit(editSelection)
            }
        }
    }

    /**
     * Updates the piece type on a square. Used for editing the board.
     */
    private fun arrangeUpdate(pFen: Char, pToIndex: Int) {
        val arrangeBoardEnabled =  ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
            if(record != null) {
                bufferTempBoard.setBoardArray(record.boardArray)
                bufferTempBoard.setStateArray(record.stateArray)

                val mResult = bufferTempBoard.arrangeUpdate(pFen, pToIndex)
                if (mResult.success) {
                    record.boardArray = bufferTempBoard.getBoardArray()
                    record.stateArray = bufferTempBoard.getStateArray()
                    record.moveSAN = ""
                    BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout,record)
                    GameRecordDataService.getInstance(activityID).updateGameState(record)
                    binding.boardPanelLayout.shakeRefresh()
                } else {
                    showMessage(mResult.returnMessage,"", Toast.LENGTH_SHORT)
                }

                // Clear the move selected
                pieceMove?.Clear()
            }
        }
    }

    /**
     * Moves a piece from one square to another
     */
    private fun arrangeUpdate(pFromIndex: Int, pToIndex: Int) {
        val arrangeBoardEnabled =  ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            val record: GameRecordArray? = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
            if(record != null) {
                bufferTempBoard.setBoardArray(record.boardArray)
                bufferTempBoard.setStateArray(record.stateArray)
                val mResult = bufferTempBoard.arrange(pFromIndex, pToIndex)
                if (mResult.success) {
                    record.boardArray = bufferTempBoard.getBoardArray()
                    record.stateArray = bufferTempBoard.getStateArray()
                    record.moveSAN = ""
                    BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout,record)
                    GameRecordDataService.getInstance(activityID).updateGameState(record)
                    binding.boardPanelLayout.shakeRefresh()
                } else {
                    showMessage(mResult.returnMessage,"", Toast.LENGTH_SHORT)
                }

                // Clear the move selected
                pieceMove?.Clear()
            }
        }
    }

    /**
     * Add a tile to the move when user clicks a square
     */
    private suspend fun userMoveAdd(pTile: Tile, pAnimate: Boolean) {

        val moveHighlightEnabled = ParameterDataService.getInstance(activityID).get(ParamMoveHighlight::class.java).enabled

        // Ensure game record is set to the latest
        val maxRecId = GameRecordDataService.getInstance(activityID).getMaxId()
        if (BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue != maxRecId) {
            navigateMaxRecord()
            pieceMove?.Clear()
            return
        }

        // Check the clock
        checkChessClock()

        // Select highlight mode
        val highlight = if(moveHighlightEnabled) HighlightEnum.MovePath else HighlightEnum.Select

        // Create proposed move
        val moveSelected: Boolean = pieceMove?.Add(pTile.index, GameRecordDataService.getInstance(activityID).currentGame, highlight) ?: false

        // Restart the computer move (if required)
        startComputerMoveTask()

        if (moveSelected) {
            userMoveProcessing = true
            val boardBeforeMove = GameRecordDataService.getInstance(activityID).getCurrentGame()

            // Ask user what pawn promotion piece they wish to use, if a promoting pawn move
            var promotionPiece: Int = PawnPromotionEnum.Queen.value // default
            val isPawnPromotion: Boolean = GameRecordDataService.getInstance(activityID).currentGame.isPawnPromotion(pieceMove?.fromIndex ?: -1, pieceMove?.toIndex ?: -1)
            val promoteAutoEnabled: Boolean = ParameterDataService.getInstance(activityID).get(ParamPromoteAuto::class.java).enabled
            if ((!promoteAutoEnabled) && isPawnPromotion) {
                promotionPiece = showPawnPromotionDialog(GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour())
            }


            val gameStatusBeforeMove = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus()
            val moveResult = GameRecordDataService.getInstance(activityID).currentGame.move(pieceMove?.fromIndex ?: -1, pieceMove?.toIndex ?: -1, promotionPiece, pValidateEnabled = true, pCommit = true)

            if (moveResult.success) {

                val boardAfterMove = GameRecordDataService.getInstance(activityID).getCurrentGame()

                if(pAnimate) {
                    val moveSpeedIndex: Int = ParameterDataService.getInstance(activityID).get(ParamMoveSpeed::class.java).speed
                    val moveSpeedMS: Long = (Constants.moveSpeedSeconds[moveSpeedIndex.coerceIn(0, Constants.strengthList.lastIndex)] * 1000).toLong()
                    val moveAnimationList = BoardAnimation.getInstance(activityID).createAnimationList(
                        boardBeforeMove,
                        boardAfterMove,
                        binding.boardPanelLayout,
                        this,
                        moveSpeedMS
                    )

                    // Do animation
                    startPieceAnimation(true, moveAnimationList)
                }

                // Update display
                BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout, GameRecordDataService.getInstance(activityID).getCurrentGame())

                // Record game state
                recordCurrentGameState(moveResult.moveSAN)

                // Check the clock
                checkChessClock()

                // Piece move sound effect
                playPieceMoveSoundEffect()

                // Update score if checkmate occurred
                if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value)
                {
                    val kingFallIndex = GameRecordDataService.getInstance(activityID).currentGame.getKingIndex(GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour())
                    val kingFallSeq = BoardAnimation.getInstance(activityID).createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
                    startPieceAnimation(true, kingFallSeq)
                    afterCheckMate(GameRecordDataService.getInstance(activityID).currentGame)
                }

                // Start computer move task
                startComputerMoveTask()

            }
            else if (!moveResult.success)
            {
                showMessage(moveResult.returnMessage,"", Toast.LENGTH_SHORT)
            }


            // Clear the move selected
            pieceMove?.Clear()
            userMoveProcessing = false
        }

        return
    }

    /**
     * Start Computer Move task
     */
    private suspend fun startComputerMoveTask()
    {
        val arrangeBoardEnabled =  ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        val computerPlayerEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerMoveFirst::class.java).enabled
        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE
        val turnColour = GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour()
        val boardStatus = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus()

        if (boardStatus == 0 && (!computerMoveProcessing) && (!computerHintProcessing) && (!arrangeBoardEnabled) && computerPlayerEnabled && computerColour == turnColour) {
            lockPanel = true
            computerMoveProcessing = true

            // Clear the user move since starting computer move
            pieceMove?.Clear()

            binding.moveProgressBar.visibility = View.VISIBLE

            val searchOptions = SearchOptions()

            val limitSkillLevel = ParameterDataService.getInstance(activityID).get(ParamLimitSkillLevel::class.java).level
            val strengthSetting = Constants.strengthList[limitSkillLevel.coerceIn(0, Constants.strengthList.lastIndex)]

            searchOptions.limitSkillLevel =  strengthSetting.pSkillLevel

            val limitAdvancedEnabled = ParameterDataService.getInstance(activityID).get(ParamLimitAdvanced::class.java).enabled
            if (limitAdvancedEnabled) {
                searchOptions.limitDepth = ParameterDataService.getInstance(activityID).get(ParamLimitDepth::class.java).depth
                val limitMoveDuration = ParameterDataService.getInstance(activityID).get(ParamLimitMoveDuration::class.java).moveDurationMS
                searchOptions.limitNodes = if (limitMoveDuration > 0) Constants.NODELIMIT_HIGH else Constants.NODELIMIT_STANDARD
                searchOptions.limitMoveDuration = limitMoveDuration
                searchOptions.limitThreads = ParameterDataService.getInstance(activityID).get(ParamLimitThreads::class.java).threads
            } else {
                searchOptions.limitDepth = strengthSetting.pDepth
                searchOptions.limitNodes = Constants.NODELIMIT_STANDARD
                searchOptions.limitMoveDuration = strengthSetting.pTimeLimitms
                searchOptions.limitThreads = if (Runtime.getRuntime().availableProcessors() > 1) Runtime.getRuntime().availableProcessors() - 1 else 1
            }

            searchOptions.randomiseFirstMove = ParameterDataService.getInstance(activityID).get(ParamRandomiseFirstMove::class.java).enabled
            searchOptions.alternateMove = isRepeatMove();

            val topMove = withContext(Dispatchers.IO) { GameRecordDataService.getInstance(activityID).currentGame.searchStart(searchOptions) }
            doMoveOnBoard(topMove)

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

        val arrangeBoardEnabled =  ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        val boardStatus = hintBoard.getStateGameStatus()

        if (boardStatus == 0 && (!computerMoveProcessing) && (!computerHintProcessing) && (!arrangeBoardEnabled) ) {
            lockPanel = true
            computerHintProcessing = true

            binding.moveProgressBar.visibility = View.VISIBLE

            val searchOptions = SearchOptions()
            searchOptions.limitSkillLevel = Constants.strengthList[Constants.strengthList.lastIndex].pSkillLevel
            searchOptions.limitDepth = Constants.strengthList[Constants.strengthList.lastIndex].pDepth
            searchOptions.limitNodes = Constants.NODELIMIT_STANDARD
            searchOptions.limitMoveDuration = Constants.strengthList[Constants.strengthList.lastIndex].pTimeLimitms
            searchOptions.limitThreads = if (Runtime.getRuntime().availableProcessors() > 1) Runtime.getRuntime().availableProcessors() - 1 else 1
            searchOptions.randomiseFirstMove = false
            searchOptions.alternateMove = false

            val topMove = withContext(Dispatchers.IO) { hintBoard.searchStart(searchOptions) }

            var moveSuccess: Boolean = false
            if ((!topMove.cancelled) && (topMove.error == 0)) {
                val hintMove =  ParameterDataService.getInstance(activityID).get(ParamHintMove::class.java).enabled
                if (hintMove) {
                    moveSuccess = doMoveOnBoard(topMove)
                }
                else {
                    val topMoveBits: ULong = (Constants.BITMASK shr topMove.moveFromIndex) or (Constants.BITMASK shr topMove.moveToIndex)
                    val boardColourIndex = Constants.darkSquareColourList.indexOf(ParameterDataService.getInstance(activityID).get(ParamColourDarkSquares::class.java).argb()
                    )
                    var highlightColour = color.colorMagenta
                    if (boardColourIndex > -1) {
                        highlightColour = Constants.hintColourList[boardColourIndex]
                    }
                    binding.boardPanelLayout.setHighlightFullFadeOut(topMoveBits, highlightColour)

                    if (topMove.moveFromIndex in 0..63 && topMove.moveToIndex in 0..63) {
                        val fromCoord = Constants.BoardCoordinateDict[topMove.moveFromIndex]
                        val toCoord = Constants.BoardCoordinateDict[topMove.moveToIndex]
                        showMessage("Best move is $fromCoord to $toCoord", "", Toast.LENGTH_SHORT)
                    }
                }
            }
            else {
                if (topMove.error > 0) {
                    showMessage(topMove.errorMessage, "", Toast.LENGTH_LONG)
                    Helper.LogError(topMove.error)
                }
            }

            binding.moveProgressBar.visibility = View.GONE
            computerHintProcessing = false
            lockPanel = false

            if (moveSuccess) {
                startComputerMoveTask()
            }
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
     * Moves a piece on the board
     */
    private suspend fun doMoveOnBoard(topMove: SearchResult): Boolean {
        var success: Boolean = false

        if ((!topMove.cancelled) && (topMove.error == 0)) {
            val boardBeforeMove = GameRecordDataService.getInstance(activityID).getCurrentGame()
            val gameStatusBeforeMove = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus()
            val moveResult = GameRecordDataService.getInstance(activityID).currentGame.move(topMove.moveFromIndex, topMove.moveToIndex, topMove.promotionPieceType, pValidateEnabled = true, pCommit = true)
            if (moveResult.success) {

                // Do animation
                val moveSpeedIndex: Int = ParameterDataService.getInstance(activityID).get(ParamMoveSpeed::class.java).speed
                val moveSpeedMS: Long = (Constants.moveSpeedSeconds[moveSpeedIndex.coerceIn(0, Constants.strengthList.lastIndex)] * 1000).toLong()
                val boardAfterMove = GameRecordDataService.getInstance(activityID).getCurrentGame()
                val moveAnimationList = BoardAnimation.getInstance(activityID).createAnimationList(
                    boardBeforeMove,
                    boardAfterMove,
                    binding.boardPanelLayout,
                    this,
                    moveSpeedMS
                )

                // Do animation
                startPieceAnimation(true, moveAnimationList)

                // Update display
                BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout, GameRecordDataService.getInstance(activityID).getCurrentGame())

                // Record game state
                recordCurrentGameState(moveResult.moveSAN)

                // Check the clock
                checkChessClock()

                // Piece move sound effect
                playPieceMoveSoundEffect()

                // do if checkmate occurred
                if (gameStatusBeforeMove == BoardStatusEnum.Ready.value && GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus() == BoardStatusEnum.Checkmate.value) {
                    val kingFallIndex = GameRecordDataService.getInstance(activityID).currentGame.getKingIndex(GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour())
                    val kingFallSeq = BoardAnimation.getInstance(activityID).createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
                    startPieceAnimation(true, kingFallSeq)
                    afterCheckMate(GameRecordDataService.getInstance(activityID).currentGame)
                }

                success = true
            }
            else
            {
                if (topMove.moveFromIndex > -1 && topMove.moveToIndex > -1)
                {
                    showMessage("Engine attempted an invalid move.", "", Toast.LENGTH_LONG)
                }
                else
                {
                    showMessage("Move not received from Engine.", "", Toast.LENGTH_LONG)

                }
            }
        }
        else {
            if (topMove.error > 0) {
                showMessage("${topMove.errorMessage} ", "", Toast.LENGTH_LONG)
                Helper.LogError(topMove.error)
            }
        }

        return success
    }

    /**
     * Show message
     */
    fun showMessage(pTextFull: String, pTextShort: String, pDuration: Int) {

        if (pTextFull.trim() != "") {
            // Read message
            if (pTextShort.trim() == "") readText(pTextFull)
            else readText(pTextShort)

            // Display message
            val toast = Toast.makeText(this, pTextFull, pDuration)
            toast.show()

        }
    }

    /**
     * Converts a text string to speech
     */
    private fun readText(pText: String) {
        val soundRead = ParameterDataService.getInstance(activityID).get(ParamSoundRead::class.java)
        if ((textReader?.ready ?: false) && soundRead.enabled) {
            textReader?.tts?.speak(pText, TextToSpeech.QUEUE_ADD, null, "")
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
        val board = KaruahChessEngine(App.appContext, activityID)

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


        binding.animationPanelLayout.runAnimation(binding.boardPanelLayout, pAnimationList)


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

            val clockEnabled = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled
            if (clockEnabled) {
                val defaultSecondsIndex = ParameterDataService.getInstance(activityID).get(ParamClockDefault::class.java).index
                if (defaultSecondsIndex < Constants.clockResetSeconds.size) {
                    val defaultSeconds = Constants.clockResetSeconds[defaultSecondsIndex]
                    GameRecordDataService.getInstance(activityID).reset(defaultSeconds, defaultSeconds)
                    binding.clockLayout.setClock(defaultSeconds, defaultSeconds)
                }
                else {
                    GameRecordDataService.getInstance(activityID).reset(0, 0)
                    binding.clockLayout.setClock(0, 0)
                }
            }
            else {
                GameRecordDataService.getInstance(activityID).reset(0,0)
            }

            uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
            lockPanel = false

            // Show start move message
            val computerPlayerEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java).enabled
            val computerMoveFirstEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerMoveFirst::class.java).enabled
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
        val boardBeforeUndo = GameRecordDataService.getInstance(activityID).get()
        val undo = GameRecordDataService.getInstance(activityID).undo()
        if (undo) {
            lockPanel = true
            val boardAfterUndo = GameRecordDataService.getInstance(activityID).get()
            if (boardBeforeUndo != null && boardAfterUndo != null) {
                // Update the clock
                binding.clockLayout.pauseClock()
                loadChessClock(true, boardAfterUndo)

                val moveSpeedIndex: Int = ParameterDataService.getInstance(activityID).get(ParamMoveSpeed::class.java).speed
                val moveSpeedMS: Long = (Constants.moveSpeedSeconds[moveSpeedIndex.coerceIn(0, Constants.strengthList.lastIndex)] * 1000).toLong()
                val moveAnimationList = BoardAnimation.getInstance(activityID).createAnimationList(
                    boardBeforeUndo,
                    boardAfterUndo,
                    binding.boardPanelLayout,
                    this,
                    moveSpeedMS
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
        pieceMove?.Clear()
        endPieceAnimation()
        GameRecordDataService.getInstance(activityID).currentGame.cancelSearch()
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
    private fun afterCheckMate(pBoard: KaruahChessEngine)
    {
        var humanWinAgainstComputer: Boolean = false
        val computerPlayerEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerMoveFirst::class.java).enabled
        val levelAutoEnabled = ParameterDataService.getInstance(activityID).get(ParamLevelAuto::class.java).enabled

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
            val limitSkillLevel = ParameterDataService.getInstance(activityID).get(ParamLimitSkillLevel::class.java)
            val nextSkillLevel = limitSkillLevel.level + 1
            if (nextSkillLevel in 0..Constants.strengthList.lastIndex)
            {
                limitSkillLevel.level = nextSkillLevel
                ParameterDataService.getInstance(activityID).set(limitSkillLevel)
                refreshEngineSettingsLevelIndicator()
                showBoardMessageDialog("Level Increase", "Congratulations, you have now progressed to the next level. The engine playing strength is now set to ${Constants.strengthList[nextSkillLevel].pLabel}.",R.drawable.ic_goldstar)

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

        val board = KaruahChessEngine(App.appContext, activityID)
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

        val board = KaruahChessEngine(App.appContext, activityID)
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
        val clockEnabled = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled

        val board = KaruahChessEngine(App.appContext, activityID)
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
     * Sets the size of the board and position of controls
     */
    private fun resizeBoard(){
        val mainFrameWidth = binding.mainFrame.width
        val mainFrameHeight = binding.mainFrame.height
        val isPortrait: Boolean = mainFrameWidth < mainFrameHeight

        val isCoordinatesEnabled = ParameterDataService.getInstance(activityID).get(ParamBoardCoord::class.java).enabled
        val isNavigationEnabled = ParameterDataService.getInstance(activityID).get(ParamNavigator::class.java).enabled
        val isClockEnabled = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled

        // Set floating action button orientation
        val mainFabLayout = binding.floatingActionButtonMainLayout
        val editToolFabLayout = binding.floatingActionButtonEditToolLayout
        if(isPortrait) {
            mainFabLayout.orientation = LinearLayout.HORIZONTAL
            editToolFabLayout.orientation = LinearLayout.HORIZONTAL
        }
        else {
            mainFabLayout.orientation = LinearLayout.VERTICAL
            editToolFabLayout.orientation = LinearLayout.VERTICAL
        }

        // Calculate padding
        val coordMargin: Int = if (isCoordinatesEnabled) 18f.spToPx() else 0

        val navVerticalMargin: Int = if (isNavigationEnabled && isPortrait) resources.getDimension(R.dimen.navButtonWidth).toInt() else 0
        val navHorizontalMargin: Int = if (isNavigationEnabled && !isPortrait) resources.getDimension(R.dimen.navButtonWidth).toInt() else 0

        val fabVerticalMargin: Int = if (isPortrait) 56f.dpToPx() + resources.getDimension(R.dimen.fabMargin).toInt() else 0
        val fabHorizontalMargin: Int = if (!isPortrait) 56f.dpToPx() + resources.getDimension(R.dimen.fabMargin).toInt() else 0

        val clockVerticalMargin: Int = if (isClockEnabled && isPortrait) resources.getDimension(R.dimen.clockButtonWidth).toInt() else 0
        val clockHorizontalMargin: Int = if (isClockEnabled && !isPortrait) resources.getDimension(R.dimen.clockButtonWidth).toInt() else 0

        // Calculate margins and board dimensions
        val verticalMargin = coordMargin + navVerticalMargin + fabVerticalMargin + clockVerticalMargin
        val horizontalMargin = coordMargin + navHorizontalMargin + fabHorizontalMargin + clockHorizontalMargin
        val boardHeight = mainFrameHeight - verticalMargin
        val boardWidth = mainFrameWidth - horizontalMargin

        // Calculate the tile size
        val tileSize = if (boardHeight < boardWidth) boardHeight / 8 else boardWidth / 8

        // Layout parameters for board and animation panel
        val lp = CoordinatorLayout.LayoutParams(tileSize * 8, tileSize * 8)
        lp.setMargins(coordMargin,0,0,0)

        // Draws the board
        binding.boardPanelLayout.layoutParams = lp
        binding.boardPanelLayout.drawTiles(tileSize)

        // Draws the coordinates if enabled
        if(isCoordinatesEnabled) {
            binding.coordPanelLayout.show(true)
            binding.coordPanelLayout.draw(tileSize, coordMargin)
        }
        else {
            binding.coordPanelLayout.show(false)
        }

        // Set up the Animation panel
        binding.animationPanelLayout.layoutParams = lp

        // Set up the total board boundary view including the coordinates panel if enabled
        val totalBoardLP = CoordinatorLayout.LayoutParams(tileSize * 8 + coordMargin, tileSize * 8 + coordMargin)
        binding.totalBoardBoundaryView.layoutParams = totalBoardLP

        // Refresh shake animation
        binding.boardPanelLayout.shakeRefresh()

        // Set board rotation
        rotateBoard(ParameterDataService.getInstance(activityID).get(ParamRotateBoard::class.java).value)

        // Set navigator orientation
        binding.navigatorLayout.setLayout(isPortrait)

        // Adjacent control gravity and orientation
        if(!isPortrait) {
            // Change the gravity so it attaches to the right of the board
            val params = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT
            )
            params.gravity = Gravity.TOP or Gravity.END
            params.anchorGravity = Gravity.TOP or Gravity.END
            params.anchorId = binding.totalBoardBoundaryView.id
            binding.adjacentControlLayout.layoutParams = params

            // set the orientation to horizontal
            binding.adjacentControlLayout.orientation = LinearLayout.HORIZONTAL
        }
        else {
            // Change the gravity so it attaches to the bottom of the board
            val mainParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            )
            mainParams.gravity = Gravity.START or Gravity.BOTTOM
            mainParams.anchorGravity = Gravity.START or Gravity.BOTTOM
            mainParams.anchorId = binding.totalBoardBoundaryView.id
            binding.adjacentControlLayout.layoutParams = mainParams

            // set the orientation to vertical
            binding.adjacentControlLayout.orientation = LinearLayout.VERTICAL
        }

        // Clock orientation
        binding.clockLayout.setOrientation(isPortrait)

    }

    /**
     * Sets the board rotation to [pRotation] degrees
     */
    fun rotateBoard(pRotation: Int) {
        binding.boardPanelLayout.rotate(pRotation)
        binding.animationPanelLayout.rotate(pRotation)
        binding.coordPanelLayout.setCoordLabels(pRotation)
    }


    /**
     * Gets version information
     */
    private fun getVersion(): String? {
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
    private suspend fun recordCurrentGameState(pMoveSAN: String)
    {
        var whiteClock: Int = 0
        var blackClock: Int = 0

        // Update the clock offset before saving
        val clockEnabled = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled
        if (clockEnabled) {
            whiteClock = binding.clockLayout.whiteClock.remainingNano().nanoSecondsToSeconds().toInt()
            blackClock = binding.clockLayout.blackClock.remainingNano().nanoSecondsToSeconds().toInt()
        }

        // Record current game state
        val success = GameRecordDataService.getInstance(activityID).recordGameState(whiteClock, blackClock, pMoveSAN)
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
        val importPGNDialogFragment = ImportPGN.newInstance(activityID)
        importPGNDialogFragment.show(fm, null)
    }

    /**
     * Show Castling dialog
     */
    private fun showCastlingRightsDialog(pKingSpin: Int) {
        // Cancel any move jobs first
        stopSearchJob()

        // Get the current board in view and open the castling dialog
        val record = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
        if(record != null) {
            castlingRightsVM?.kingSpin?.value = pKingSpin
            castlingRightsVM?.record?.value = record
            val fm = supportFragmentManager
            CastlingRights.newInstance(activityID).show(fm, null)
        }
    }

    /**
     * Show engine settings dialog
     */
    private fun showEngineSettingsDialog() {

        val fm = supportFragmentManager
        val engineSettingsDialogFragment = EngineSettings.newInstance(activityID)
        engineSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show clock settings dialog
     */
    fun showClockSettingsDialog() {

        val fm = supportFragmentManager
        val clockSettingsDialogFragment = ClockSettings.newInstance(activityID)
        clockSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show board settings dialog
     */
    private fun showBoardSettingsDialog() {

        val fm = supportFragmentManager
        val boardSettingsDialogFragment = BoardSettings.newInstance(activityID)
        boardSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show piece settings dialog
     */
    private fun showPieceSettingsDialog() {

        val fm = supportFragmentManager
        val pieceSettingsDialogFragment = PieceSettings.newInstance(activityID)
        pieceSettingsDialogFragment.show(fm, null)
    }

    /**
     * Show hint settings dialog
     */
    private fun showHintSettingsDialog() {

        val fm = supportFragmentManager
        val hintSettingsDialogFragment = HintSettings.newInstance(activityID)
        hintSettingsDialogFragment.show(fm, null)
    }


    /**
     * Show sound settings dialog
     */
    private fun showSoundColourSettingsDialog() {

        val fm = supportFragmentManager
        val soundColourSettingsDialogFragment = SoundSettings.newInstance(activityID)
        soundColourSettingsDialogFragment.show(fm, null)
    }


    /**
     * Show piece type select dialog
     */
    private fun showPieceEditToolDialog() {
        if (editSelection > 0uL) {
            val fm = supportFragmentManager
            val pieceEditToolDialogFragment =
                PieceEditTool.newInstance(binding.boardPanelLayout.tileSize)
            pieceEditToolDialogFragment.show(fm, null)
        }
        else {
            showMessage("Cannot add piece, no squares selected", "", Toast.LENGTH_SHORT)
        }
    }


    /**
     * Show pawn promotion dialog
     */
    private suspend fun showPawnPromotionDialog(pColour: Int): Int {

        val fm = supportFragmentManager
        pawnPromotionVM?.buttonSize?.value = binding.boardPanelLayout.tileSize.toFloat()
        pawnPromotionVM?.promotionColour?.value = pColour
        val pawnPromotionDialogFragment = PawnPromotion.newInstance()
        pawnPromotionDialogFragment.setPawnPromotionInteractionListener(this)
        pawnPromotionDialogFragment.show(fm, "PawnPromotionDialog")

        return pieceChannel.receive()

    }

    /**
     * Show move navigator pgn dialog
     */
    fun showMoveNavigatorPGNDialog(pPGN: String) {

        val fm = supportFragmentManager
        val moveNavigatorPGN = MoveNavigatorPGN.newInstance(activityID, pPGN)
        moveNavigatorPGN.show(fm, null)
    }

    /**
     * Show the about dialog
     */
    private fun showAboutDialog() {
        val fm = supportFragmentManager
        aboutVM?.versionStr?.value = getVersion()
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

            // Validate mime type
            val mime = contentResolver.getType(pFileUri)
            val allowed = setOf("application/gzip", "application/x-gzip", "text/xml", "application/xml")
            if (!mime.isNullOrBlank() && mime !in allowed) {
                showMessage(
                    "Unsupported file type: $mime",
                    "Unsupported file type: $mime",
                    Toast.LENGTH_LONG
                )
                return
            }

            // Do the import
            stopSearchJob()
            val file: InputStream? = contentResolver.openInputStream(pFileUri)
            val importDB = ImportDB()
            val fileResult = if(file != null) {
                importDB.import(file, ImportDB.ImportTypeEnum.GameXML, this, activityID)
            }
            else { 0 }

            if(fileResult > 0) {
                uiScope.launch(Dispatchers.Main) { navigateMaxRecord() }
                loadChessClock(true, GameRecordDataService.getInstance(activityID).getCurrentGame())
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
            // Export to a temp file as before
            val exportDB = ExportDB()
            val file = exportDB.export(ExportDB.ExportTypeEnum.GameXML, this, activityID)

            // Offer options: Share or Save locally
            MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert)
                .setTitle("Save game")
                .setItems(arrayOf("Share...", "Save locally...")) { _, which ->
                    when (which) {
                        0 -> {
                            // Share (existing behavior)
                            val sharedFileUri: Uri =
                                FileProvider.getUriForFile(this, "purpletreesoftware.karuahchess.exportdata", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/gzip"
                                putExtra(Intent.EXTRA_STREAM, sharedFileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(Intent.createChooser(intent, "Send file to"))
                        }
                        1 -> {
                            // Save locally (SAF)
                            pendingExportFile = file
                            createGameExportDocLauncher.launch(file.name) // suggests filename
                        }
                    }
                }
                .show()
        } catch (e: Throwable) {
            showMessage("Could not export file. An error occurred - ${e.message}", "Could not export file.", Toast.LENGTH_LONG)
        }
    }


    /**
     * Set the board to the max record
     */
    suspend fun navigateMaxRecord() {
        val maxId = GameRecordDataService.getInstance(activityID).getMaxId()
        navigateGameRecord(maxId, pAnimate = false, pReloadNav = true, pScrollNav = true)
    }

    /**
     * Navigate to requested game record
     */
    suspend fun navigateGameRecord(pRecId: Int, pAnimate: Boolean, pReloadNav: Boolean, pScrollNav: Boolean) {
        if (pRecId > 0)
        {
            navThrottler.acquire()

            val oldBoard : GameRecordArray? = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
            val updatedBoard = GameRecordDataService.getInstance(activityID).get(pRecId)

            // Update board displayed with requested record
            if (updatedBoard != null)
            {
                // Do animation
                if (pAnimate && oldBoard != null) {
                    val moveSpeedIndex: Int = ParameterDataService.getInstance(activityID).get(ParamMoveSpeed::class.java).speed
                    val moveSpeedMS: Long = (Constants.moveSpeedSeconds[moveSpeedIndex.coerceIn(0, Constants.strengthList.lastIndex)] * 1000).toLong()
                    val moveAnimationList = BoardAnimation.getInstance(activityID).createAnimationList(
                        oldBoard,
                        updatedBoard,
                        binding.boardPanelLayout,
                        this,
                        moveSpeedMS
                    )


                    BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout, updatedBoard)
                    BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue = pRecId
                    updateBoardIndicators(updatedBoard)
                    loadChessClock(false, updatedBoard)
                    // Do animation
                    startPieceAnimation(true, moveAnimationList)
                }
                else {
                    // End any animations
                    endPieceAnimation()

                    BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout, updatedBoard)
                    BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue = pRecId
                    updateBoardIndicators(updatedBoard)
                    loadChessClock(false, updatedBoard)
                }

                pieceMove?.Clear()

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
        val navParam = ParameterDataService.getInstance(activityID).get(ParamNavigator::class.java)
        if(navParam.enabled) {
            binding.navigatorLayout.show()
            if (pReload) {
                binding.navigatorLayout.syncNavButtons()
                binding.navigatorLayout.setSelected(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
            }
            else {
                binding.navigatorLayout.setSelected(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
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
        val arrangeBoardEnabled =  ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            showMessage("Cannot resign in edit mode.","",Toast.LENGTH_SHORT)
            return
        }

        // Check that it is players turn
        val computerPlayerEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java).enabled
        val computerMoveFirstEnabled =  ParameterDataService.getInstance(activityID).get(ParamComputerMoveFirst::class.java).enabled
        val computerColour = if (computerMoveFirstEnabled) Constants.WHITEPIECE else Constants.BLACKPIECE
        val turnColour = GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour()
        if (computerPlayerEnabled && turnColour == computerColour) {
            showMessage("Cannot resign as it is not your turn.","",Toast.LENGTH_SHORT)
            return
        }

        // Ensure game record is set to the latest
        navigateMaxRecord()

        val status = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus()

        // Only resign if game is not already finished
        if (status == BoardStatusEnum.Ready.value && GameRecordDataService.getInstance(activityID).currentGame.getStateFullMoveCount() > 0) {

            // Stop task if running
            stopSearchJob()

            // Set the state of the game to resign
            GameRecordDataService.getInstance(activityID).currentGame.setStateGameStatus(BoardStatusEnum.Resigned.value)


            // Do animation, display message
            val kingFallIndex = GameRecordDataService.getInstance(activityID).currentGame.getKingIndex(GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour())
            val kingFallSeq = BoardAnimation.getInstance(activityID).createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
            startPieceAnimation(true, kingFallSeq)

            // Record current game state
            var turn = GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour()
            var comment = if (turn == 1) { "{White resigns.}" } else { "{Black resigns.}" }
            recordCurrentGameState(comment)

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
        val arrangeBoardEnabled =  ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled
        if (arrangeBoardEnabled) {
            // Cannot expire time if in edit mode
            return
        }

        // Ensure game record is set to the latest
        navigateMaxRecord()

        val status = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus()

        // Only expire time if game is not already finished
        if (status == BoardStatusEnum.Ready.value) {

            // Stop task if running
            stopSearchJob()

            // Set the state of the game to time expired
            GameRecordDataService.getInstance(activityID).currentGame.setStateGameStatus(BoardStatusEnum.TimeExpired.value)

            // Do animation, display message
            val kingFallIndex = GameRecordDataService.getInstance(activityID).currentGame.getKingIndex(pColourExpired)
            val kingFallSeq = BoardAnimation.getInstance(activityID).createAnimationFall(kingFallIndex, binding.boardPanelLayout, this, 3000L)
            startPieceAnimation(true, kingFallSeq)

            // Record current game state
            var turn = GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour()
            var comment = if (turn == 1) { "{White lost on time.}" } else { "{Black lost on time.}" }
            recordCurrentGameState(comment)

        }

    }

    /**
     * Updates selected board squares when in edit mode
     */
    fun editToolUpdateSelectedTiles(pFen: Char) {

        var updated: Boolean = false
        var sqId: ULong = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL
        for (i in 0..63) {
            if ((sqId and editSelection) != 0uL) {
                arrangeUpdate(pFen, i)
                updated = true
            }
            sqId = sqId shr 1
        }

        // Clear any edit selections
        editSelection = 0uL
        binding.boardPanelLayout.setHighlightEdit(editSelection)

        if (!updated) {
            showMessage("Cannot update, no squares are selected","", Toast.LENGTH_SHORT)
        }
    }

    /**
     * Enables and disables edit board
     */
    private fun editBoardToggle() {
        val edit = ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java)
        edit.enabled = !edit.enabled
        ParameterDataService.getInstance(activityID).set(edit)
        editMenuItem?.isChecked = edit.enabled
        if (edit.enabled) {
            stopSearchJob()
            showMessage("Edit board is enabled","", Toast.LENGTH_SHORT)
        }
        else {
            showMessage("Edit board is disabled","", Toast.LENGTH_SHORT)

            // Clear any edit selections
            editSelection = 0uL
            binding.boardPanelLayout.setHighlightEdit(editSelection)
        }

        binding.boardPanelLayout.shake(edit.enabled)

        setFabActionButtons(edit.enabled)
        checkChessClock()

    }

    /**
     * Removed selected pieces from the board
     */
    private fun editEraseSelection() {
        if (editSelection > 0uL) {
            var sqId: ULong =
                0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL
            for (i in 0..63) {
                if ((sqId and editSelection) != 0uL) {
                    arrangeUpdate(' ', i)
                }
                sqId = sqId shr 1
            }

            // Clear any edit selections
            editSelection = 0uL
            binding.boardPanelLayout.setHighlightEdit(editSelection)
        }
        else {
            showMessage("Cannot remove pieces, no squares selected", "", Toast.LENGTH_SHORT)
        }

    }

    /**
     * Set move navigator panel
     */
    fun setMoveNavigatorPanel(pEnabled: Boolean) {
        val navParam = ParameterDataService.getInstance(activityID).get(ParamNavigator::class.java)
        if (navParam.enabled != pEnabled) {
            navParam.enabled = pEnabled
            ParameterDataService.getInstance(activityID).set(navParam)
            refreshNavigation(pReload = true, pScroll = true)
            resizeBoard()
        }
    }

    /**
     * Apply board colour
     */
    fun applyBoardColour() {
        val darkSquareColourParam = ParameterDataService.getInstance(activityID).get(ParamColourDarkSquares::class.java)
        val darkSqColour = Color.argb(darkSquareColourParam.a, darkSquareColourParam.r, darkSquareColourParam.g, darkSquareColourParam.b)
        binding.boardPanelLayout.applyBoardColour(darkSqColour)
    }

    /**
     * Set large pawns
     */
    fun setLargePawns() {
        val largePawn: Boolean = ParameterDataService.getInstance(activityID).get(ParamLargePawn::class.java).enabled
        binding.boardPanelLayout.setLargePawns(largePawn)
    }

    /**
     * Starts the chess clock based on current turn
     */
    fun checkChessClock() {
        val edit = ParameterDataService.getInstance(activityID).get(ParamArrangeBoard::class.java).enabled

        if (edit) {
            binding.clockLayout.pauseClock()
        } else {
            // Only start the clock if the game has not finished
            val gameFinished: Boolean = GameRecordDataService.getInstance(activityID).currentGame.getStateGameStatus() != BoardStatusEnum.Ready.value
            if (!gameFinished) {
                val turn: Int = GameRecordDataService.getInstance(activityID).currentGame.getStateActiveColour()
                binding.clockLayout.start(turn)
            }
            else {
                binding.clockLayout.pauseClock()
            }
        }

        if ((!binding.clockLayout.isPaused()) && BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue == GameRecordDataService.getInstance(activityID).getMaxId()) {
            binding.clockLayout.showCurrentTime()
        }

    }


    /**
     * Initialises the chess clock with values and switches between the current and historical time remaining
     */
    private fun loadChessClock(pLoadInitialOffset: Boolean, pRecord: GameRecordArray) {

        val board = KaruahChessEngine(App.appContext, activityID)
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)

        // Loads the initial offset
        if (pLoadInitialOffset) {

            if (GameRecordDataService.getInstance(activityID).recordCount() == 1) {
                val defaultSecondsIndex = ParameterDataService.getInstance(activityID).get(ParamClockDefault::class.java).index
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
        if (BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue == GameRecordDataService.getInstance(activityID).getMaxId()) {
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
        if (GameRecordDataService.getInstance(activityID).recordCount() == 1) {
            val record: GameRecordArray? = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
            if(record != null) {
                val bufferBoard = KaruahChessEngine(App.appContext, activityID)
                bufferBoard.setBoardArray(record.boardArray)
                bufferBoard.setStateArray(record.stateArray)

                bufferBoard.setStateWhiteClockOffset(pWhiteSecondsRemaining)
                bufferBoard.setStateBlackClockOffset(pBlackSecondsRemaining)

                record.boardArray = bufferBoard.getBoardArray()
                record.stateArray = bufferBoard.getStateArray()

                BoardSquareDataService.getInstance(activityID).update(binding.boardPanelLayout, record)
                GameRecordDataService.getInstance(activityID).updateGameState(record)
            }
        }
    }

    /**
     * Refresh the level indicator
     */
    fun refreshEngineSettingsLevelIndicator() {
        val computerPlayerEnabled = ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java).enabled
        if (computerPlayerEnabled) {
            val skillLevel = ParameterDataService.getInstance(activityID).get(ParamLimitSkillLevel::class.java).level
            if (skillLevel in 0..Constants.strengthList.lastIndex) {
                binding.levelIndicatorText.text = "${(Constants.strengthList[skillLevel].pLabel)}"
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
        val soundEffect = ParameterDataService.getInstance(activityID).get(ParamSoundEffect::class.java)
        if (soundEffect.enabled) {
            // Sounds
            sndPool?.play(sndMove,1F,1F,0,0,1F)

        }
    }


    /**
     * Show or hide the hints feature
     */
    fun setHint() {
        val hintsEnabled = ParameterDataService.getInstance(activityID).get(ParamHint::class.java).enabled
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
        val record = GameRecordDataService.getInstance(activityID).get(BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue)
        if(record != null) {
            uiScope.launch(Dispatchers.Main) { startHintTask(record) }
        }

    }

    /**
     * Sets the visibility of the fab action buttons
     */
    fun setFabActionButtons(pArrangeboardEnabled: Boolean) {
        if (pArrangeboardEnabled) {
            binding.floatingActionButtonMainLayout.visibility = View.GONE
            binding.floatingActionButtonEditToolLayout.visibility = View.VISIBLE
        }
        else {
            binding.floatingActionButtonMainLayout.visibility = View.VISIBLE
            binding.floatingActionButtonEditToolLayout.visibility = View.GONE
        }
    }

    fun getActivityID(): Int {
        return activityID
    }

    /**
     * Detect a repeat move
     */
    private fun isRepeatMove(): Boolean {
        var repeated: Boolean = false
        val history: SortedMap<Int, GameRecordArray> = GameRecordDataService.getInstance(activityID).gameHistory()

        val endIndex: Int = history.size - 1
        if (endIndex >= 4) {
            val currentBoardArray: ULongArray = history.values.elementAt(endIndex).boardArray

            var historyIndex = endIndex - 2
            while (historyIndex >= 0) {
                val historyBoardArray: ULongArray = history.values.elementAt(historyIndex).boardArray
                var differenceFound: Boolean = false

                if (currentBoardArray.size === historyBoardArray.size && historyBoardArray.size > 0) {
                    // Compare all elements in both arrays and if one of them different,
                    // then they are different boards
                    for (i in 0 until currentBoardArray.size) {
                        if (currentBoardArray[i] != historyBoardArray[i]) {
                            differenceFound = true
                            break
                        }
                    }

                    // Break if difference not found as no need to search further
                    if (!differenceFound) {
                        repeated = true
                        break
                    }
                }
                historyIndex -= 2
            }
        }

        return repeated
    }

    /**
     * Routines to run on pause
     */
    override fun onPause() {
        // Stop any searches
        GameRecordDataService.getInstance(activityID).currentGame.cancelSearch()
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
        textReader?.tts?.shutdown()


        super.onDestroy()
    }

    /**
     * Routines to run when saving instance state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the clock state if it is enabled
        val clockEnabled = ParameterDataService.getInstance(activityID).get(ParamClock::class.java).enabled
        if (clockEnabled) {
            outState.putLong(clockWhiteRemainingNanoKEY, binding.clockLayout.whiteClock.remainingNano())
            outState.putLong(clockBlackRemainingNanoKEY, binding.clockLayout.blackClock.remainingNano())
            outState.putBoolean(clockPausedKEY, binding.clockLayout.isPaused())
        }

    }

}

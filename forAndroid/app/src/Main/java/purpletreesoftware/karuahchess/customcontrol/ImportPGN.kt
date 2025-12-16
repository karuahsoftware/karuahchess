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

package purpletreesoftware.karuahchess.customcontrol

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.database.DatabaseHelper
import purpletreesoftware.karuahchess.database.TableName
import purpletreesoftware.karuahchess.databinding.FragmentImportpgnBinding
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import purpletreesoftware.karuahchess.engine.MoveResult
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService
import kotlin.text.Regex


@ExperimentalUnsignedTypes
class ImportPGN(pActivityID: Int) : DialogFragment() {
    private var _binding: FragmentImportpgnBinding? = null
    private val binding get() = _binding!!
    private val _board = KaruahChessEngine(App.appContext, pActivityID)
    private val activityID = pActivityID

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val fragmentBinding = FragmentImportpgnBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }
        binding.importButton.setOnClickListener { import() }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    /**
     * Imports a pgn game a closes the popup
     */
    private fun import()
    {
        binding.importErrorTextView.text = ""

        var pgnGameFilterA: String = binding.importPGNEditText.text.toString()

        // Remove header comments
        pgnGameFilterA =  Regex("(?s)\\[.*?\\]\\s*").replace(pgnGameFilterA, "")

        // Remove any curly bracket comments in game string
        pgnGameFilterA =  Regex("(?s)\\{.*?\\}").replace(pgnGameFilterA, "")

        // Remove line breaks
        val lines = pgnGameFilterA.split( "\r\n", "\r", "\n")
        var pgnGameFilterB = ""
        for(line in lines)
        {
            // Remove semicolon comments
            pgnGameFilterB = pgnGameFilterB + Regex(";(.*)?$").replace(line, "") + " "
        }

        // Remove Numeric Annotation Glyphs
        pgnGameFilterB = Regex("(\\$\\d+)|[!?]+").replace(pgnGameFilterB, "")

        val pgnGame = pgnGameFilterB
        val success = processGame(pgnGame)
        if (success) {
            val mainActivity = activity as MainActivity
            mainActivity.uiScope.launch(Dispatchers.Main) {
                mainActivity.binding.clockLayout.setClock(0, 0)
                mainActivity.navigateMaxRecord()
                mainActivity.setMoveNavigatorPanel(true)
                dismiss()
            }
        }

    }

    /**
     * Processes a PGN game string
     */
    private fun processGame(pGameStr: String) :Boolean
    {
        var pgnGameStr = pGameStr.trim()

        // Remove the score from the end of the PGN string
        pgnGameStr = Regex("[10][-][10]$").replace(pgnGameStr, "")
        pgnGameStr = Regex("[1][/][2][-][1][/][2]$").replace(pgnGameStr, "")

        // Replace multi spaces
        pgnGameStr = Regex("[ ]{2,}").replace(pgnGameStr, " ")
        pgnGameStr = pgnGameStr.trim()

        // Capture move numbers as tokens too (group keeps delimiters in the result array)
        // Tokens will look like: "", "1.", "e4 e5", "2.", "Nf3", "2...", "Nc6", ...
        val moveTokens = Regex("([0-9]{1,3}(?:\\.{3}|\\.))").split(pgnGameStr)

        _board.reset()

        // Determine starting colour: if first move number is written with '...'
        // (e.g. "1...e5") then Black is to move; otherwise White to move.
        if (Regex("^\\s*[0-9]{1,3}\\.\\.\\.").containsMatchIn(pgnGameStr))
        {
            _board.setStateActiveColour(Constants.BLACKPIECE);
        }
        else
        {
            _board.setStateActiveColour(Constants.WHITEPIECE);
        }


        // Loop through game moves
        var id = 1
        var currentMoveNumberToken = "";

        // Add start record
        val gameRecList = ArrayList<GameRecordArray>()
        val startRecord = GameRecordArray()
        startRecord.id = id
        startRecord.boardArray = _board.getBoardArray()
        startRecord.stateArray = _board.getStateArray()
        startRecord.moveSAN = "";
        gameRecList.add(startRecord)
        id++

        // Iterate over tokens (move numbers + move groups)
        try {
            for (tokenRaw in moveTokens)
            {
                if (tokenRaw.isBlank()) continue;
                val token = tokenRaw.trim();

                // If this token IS a move number, store it and continue to next token
                if (Regex("^[0-9]{1,3}(?:\\.{3}|\\.)$").containsMatchIn(pgnGameStr))
                {
                    currentMoveNumberToken = token; // e.g. "12." or "12..."
                    continue;
                }

                // Otherwise this token holds one or more SAN half-moves separated by spaces
                val halfMoveArray = token.trim().split(' ').toTypedArray()
                for (halfMove in halfMoveArray)
                {
                    if (halfMove.length == 0) continue
                    val moveResult = movePGN(halfMove, _board, true)
                    if (!moveResult.success)
                    {
                        binding.importErrorTextView.append("Import failed. Error occurred at move $currentMoveNumberToken : $halfMove")
                        binding.importErrorTextView.append(moveResult.returnMessage)

                        return false
                    }

                    val gamerec = GameRecordArray()
                    gamerec.id = id
                    gamerec.boardArray = _board.getBoardArray()
                    gamerec.stateArray = _board.getStateArray()
                    gamerec.moveSAN = halfMove
                    gameRecList.add(gamerec)

                    id++
                }
            }

            if (gameRecList.count() <= 1)
            {
                binding.importErrorTextView.text = "Import failed - nothing to import."
                return false
            }

            // Load the game into DB
            loadGameIntoDatabase(gameRecList)
        }
        catch(ex: Exception)
        {
            binding.importErrorTextView.append(ex.message)
            return false
        }

        return true
    }

    /**
     * Moves a piece according to the PGN value
     */
    private fun movePGN(pPGNValue: String, pBoard: KaruahChessEngine, pCommit: Boolean): MoveResult
    {
        if (Regex("^[a-h][1-8]([=][QRBN])?[\\+]?[#]?$").matches(pPGNValue))
        {
            // Pawn move
            var promotion = MainActivity.PawnPromotionEnum.Queen
            if (Regex("^[a-h][1-8][=][QRBN][\\+]?[#]?$").matches(pPGNValue))
            {
                val promotionStr = pPGNValue.substring(3, 4)
                when (promotionStr) {
                    "Q" -> promotion = MainActivity.PawnPromotionEnum.Queen
                    "R" -> promotion = MainActivity.PawnPromotionEnum.Rook
                    "B" -> promotion = MainActivity.PawnPromotionEnum.Bishop
                    "N" -> promotion = MainActivity.PawnPromotionEnum.Knight
                }
            }

            val validFromIndexes = Constants.FileDict[pPGNValue.substring(0, 1)]
            val toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(0, 2)] ?: -1
            val fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * Constants.WHITE_PAWN_SPIN, validFromIndexes)
            return pBoard.move(fromIndex, toIndex, promotion.value, true, pCommit)

        }
        else if (Regex("^[a-h][x][a-h][1-8]([=][QRBN])?[\\+]?[#]?$").matches(pPGNValue))
        {
            // Pawn take move
            var promotion = MainActivity.PawnPromotionEnum.Queen
            if (Regex("^[a-h][x][a-h][1-8][=][QRBN][\\+]?[#]?$").matches(pPGNValue))
            {
                val promotionStr = pPGNValue.substring(5, 6)
                when (promotionStr) {
                    "Q" -> promotion = MainActivity.PawnPromotionEnum.Queen
                    "R" -> promotion = MainActivity.PawnPromotionEnum.Rook
                    "B" -> promotion = MainActivity.PawnPromotionEnum.Bishop
                    "N" -> promotion = MainActivity.PawnPromotionEnum.Knight
                }
            }

            val validFromIndexes = Constants.FileDict[pPGNValue.substring(0, 1)]
            val toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(2, 4)] ?: -1
            val fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * Constants.WHITE_PAWN_SPIN, validFromIndexes)
            return pBoard.move(fromIndex, toIndex, promotion.value, true, pCommit)
        }
        else if (Regex("^[KQRBN][a-h]?[1-8]?[x]?[a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
        {
            // King, Queen, Rook, Bishop, Knight move with from file
            var spin = 0
            val piece = pPGNValue.substring(0, 1)// Move with rank

            // Move
            when (piece) {
                "K" -> spin = Constants.WHITE_KING_SPIN
                "Q" -> spin = Constants.WHITE_QUEEN_SPIN
                "R" -> spin = Constants.WHITE_ROOK_SPIN
                "B" -> spin = Constants.WHITE_BISHOP_SPIN
                "N" -> spin = Constants.WHITE_KNIGHT_SPIN
            }

            var toIndex = -1
            var fromIndex = -1

            if (Regex("^[KQRBN][a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
            {
                // Move
                toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(1, 3)] ?: -1
                fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * spin, null)
            }
            else if (Regex("^[KQRBN][a-h][a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
            {
                // Move with file
                val validFromIndexes = Constants.FileDict[pPGNValue.substring(1, 2)]
                toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(2, 4)] ?: -1
                fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * spin, validFromIndexes)
            }
            else if (Regex("^[KQRBN][1-8][a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
            {
                // Move with rank
                val validFromIndexes = Constants.RankDict[pPGNValue.substring(1, 2)]
                toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(2, 4)] ?: -1
                fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * spin, validFromIndexes)
            }
            else if (Regex("^[KQRBN][x][a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
            {
                // Move
                toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(2, 4)] ?: -1
                fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * spin, null)
            }
            else if (Regex("^[KQRBN][a-h][x][a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
            {
                // Move with file
                val validFromIndexes = Constants.FileDict[pPGNValue.substring(1, 2)]
                toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(3, 5)] ?: -1
                fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * spin, validFromIndexes)
            }
            else if (Regex("^[KQRBN][1-8][x][a-h][1-8][\\+]?[#]?$").matches(pPGNValue))
            {
                // Move with rank
                val validFromIndexes = Constants.RankDict[pPGNValue.substring(1, 2)]
                toIndex = Constants.BoardCoordinateReverseDict[pPGNValue.substring(3, 5)] ?: -1
                fromIndex = pBoard.findFromIndex(toIndex, pBoard.getStateActiveColour() * spin, validFromIndexes)
            }

            return pBoard.move(fromIndex, toIndex, MainActivity.PawnPromotionEnum.Queen.value, true, pCommit)
        }
        else if (Regex("^[O][-][O][\\+]?[#]?$").matches(pPGNValue))
        {
            // King side castle
            var castlingAvailableKingSide = false
            if (pBoard.getStateActiveColour() == Constants.WHITEPIECE) castlingAvailableKingSide = (pBoard.getStateCastlingAvailability() and 0b000010) > 0
            else if (pBoard.getStateActiveColour() == Constants.BLACKPIECE) castlingAvailableKingSide = (pBoard.getStateCastlingAvailability() and 0b001000) > 0

            if (!castlingAvailableKingSide) throw Exception("Castling not available, invalid move.")

            val fromIndex = pBoard.getKingIndex(pBoard.getStateActiveColour())
            val toIndex = if (pBoard.getStateActiveColour() == Constants.WHITEPIECE) 62 else 6

            return pBoard.move(fromIndex, toIndex, MainActivity.PawnPromotionEnum.Queen.value, true, pCommit)
        }
        else if (Regex("^[O][-][O][-][O][\\+]?[#]?$").matches(pPGNValue))
        {
            // Queen side castle
            var castlingAvailableQueenSide = false
            if (pBoard.getStateActiveColour() == Constants.WHITEPIECE) castlingAvailableQueenSide = (pBoard.getStateCastlingAvailability() and 0b000001) > 0
            else if (pBoard.getStateActiveColour() == Constants.BLACKPIECE) castlingAvailableQueenSide = (pBoard.getStateCastlingAvailability() and 0b000100) > 0

            if (!castlingAvailableQueenSide) throw Exception("Castling not available, invalid move.")

            val fromIndex = pBoard.getKingIndex(pBoard.getStateActiveColour())
            val toIndex = if(pBoard.getStateActiveColour() == Constants.WHITEPIECE) 58 else 2

            return pBoard.move(fromIndex, toIndex, MainActivity.PawnPromotionEnum.Queen.value, true, pCommit)
        }

        return MoveResult()

    }

    /**
     * Loads the game in to the database
     */
    private fun loadGameIntoDatabase(pGameRecordList: ArrayList<GameRecordArray>)
    {
        // Insert records in to database
        if (pGameRecordList.count() > 0) {

            val table = TableName(activityID)
            val db = DatabaseHelper.getInstance(context).writableDatabase
            db.delete("${table.GameRecord}", null, null)

            for(gameRecord in pGameRecordList) {
                _board.setBoardArray(gameRecord.boardArray)
                _board.setStateArray(gameRecord.stateArray)
                val boardSquareStr = _board.getBoard()
                val gameStateStr = _board.getState()
                val contentValues = ContentValues()
                contentValues.put("Id", gameRecord.id)
                contentValues.put("BoardSquareStr", boardSquareStr)
                contentValues.put("GameStateStr", gameStateStr)
                contentValues.put("MoveSANStr", gameRecord.moveSAN)
                db.insert("${table.GameRecord}", null, contentValues)

            }

            GameRecordDataService.getInstance(activityID).load()

            val mainActivity = activity as MainActivity
            mainActivity.uiScope.launch(Dispatchers.Main) {
                mainActivity.navigateMaxRecord()
            }
        }

    }

    companion object {

        // A constructor containing pActivityID is in the ActivityFragmentFactory

        fun newInstance(pActivityID: Int): ImportPGN {
            val frag = ImportPGN(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
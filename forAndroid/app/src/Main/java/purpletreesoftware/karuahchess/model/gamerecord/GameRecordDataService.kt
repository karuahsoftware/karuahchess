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

package purpletreesoftware.karuahchess.model.gamerecord

import android.content.ContentValues
import android.content.Context
import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.database.DatabaseHelper
import purpletreesoftware.karuahchess.database.TableName
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import java.util.*
import kotlin.collections.ArrayList


@ExperimentalUnsignedTypes
class GameRecordDataService(pActivityID: Int): IGameRecordDataService{

    private val gameRecordDict: HashMap<Int, GameRecordArray>

    val currentGame: KaruahChessEngine
    private val tempBoardA: KaruahChessEngine
    private val tempBoardB: KaruahChessEngine
    private val tempBoardC: KaruahChessEngine
    private val activityID: Int
    private val table: TableName

    init {
        activityID = pActivityID
        table = TableName(activityID)
        gameRecordDict = HashMap()
        currentGame = KaruahChessEngine(App.appContext, activityID)
        tempBoardA = KaruahChessEngine(App.appContext, activityID)
        tempBoardB = KaruahChessEngine(App.appContext, activityID)
        tempBoardC = KaruahChessEngine(App.appContext, activityID)

        load()
    }

    /**
     * Load game records
     */
    override fun load()
    {
        val board = KaruahChessEngine(App.appContext, activityID)

        // Clear records from memory
        gameRecordDict.clear()

        // Load game records in to memory
        val db = DatabaseHelper.getInstance(App.appContext).readableDatabase
        db.rawQuery("select * from ${table.GameRecord} order by id", null).use { dbCursor ->
            while (dbCursor.moveToNext()) {
                val id = dbCursor.getInt(dbCursor.getColumnIndex("Id"))
                val boardSquareStr = dbCursor.getString(dbCursor.getColumnIndex("BoardSquareStr"))
                val gameStateStr = dbCursor.getString(dbCursor.getColumnIndex("GameStateStr"))
                board.setBoard(boardSquareStr)
                board.setState(gameStateStr)

                val recArray = GameRecordArray()
                recArray.id = id
                recArray.boardArray = board.getBoardArray()
                recArray.stateArray = board.getStateArray()
                gameRecordDict.put(id, recArray)
            }
        }

        // If no records were loaded then create a default record
        if (gameRecordDict.count() == 0)
        {
            reset(0,0)
        }

        // Set current game to latest bitboard
        val latestRecord = get()
        if(latestRecord != null) {
            currentGame.setBoardArray(latestRecord.boardArray)
            currentGame.setStateArray(latestRecord.stateArray)
        }

    }

    /**
     * Gets the current game as an array
     */
    override fun getCurrentGame(): GameRecordArray {
        val recArray = GameRecordArray()
        recArray.id = -1
        recArray.boardArray = currentGame.getBoardArray()
        recArray.stateArray = currentGame.getStateArray()
        return recArray
    }


    /**
     * Gets a record set by Id
     */
    override fun get(pId: Int): GameRecordArray? {
        if (gameRecordDict.containsKey(pId)) {
            val rec = gameRecordDict[pId]
            return rec
        }

        return null
    }


    /**
     * Gets the latest game record
     */
    override fun get(): GameRecordArray? {
        val latestId = getMaxId()
        return get(latestId)
    }

    /**
     * Record the current state of the game as a record
     */
    override fun recordGameState(pWhiteClockOffset: Int, pBlackClockOffset: Int): Long
    {
        var result = 0L

        // Set the clocks
        currentGame.setStateWhiteClockOffset(pWhiteClockOffset)
        currentGame.setStateBlackClockOffset(pBlackClockOffset)

        // create game record
        val nextId = getMaxId() + 1
        val boardSquareStr = currentGame.getBoard()
        val gameStateStr = currentGame.getState()

        val gameRecordArray = GameRecordArray()
        gameRecordArray.id = nextId
        gameRecordArray.boardArray = currentGame.getBoardArray()
        gameRecordArray.stateArray = currentGame.getStateArray()

        if (!gameRecordDict.containsKey(gameRecordArray.id)) {

            // Add record to dictionary
            gameRecordDict.put(gameRecordArray.id, gameRecordArray)

            // Add record to database
            val db = DatabaseHelper.getInstance(App.appContext).writableDatabase
            val contentValues = ContentValues()
            contentValues.put("Id", nextId)
            contentValues.put("BoardSquareStr", boardSquareStr)
            contentValues.put("GameStateStr", gameStateStr)

            result = db.insert("${table.GameRecord}", null, contentValues)

        }

        return result
    }

    /**
     * Updates a board record
     */
    override fun updateGameState(pGameRecordArray: GameRecordArray): Int
    {
        var result = 0

        if (gameRecordDict.containsKey(pGameRecordArray.id))
        {
            tempBoardC.setBoardArray(pGameRecordArray.boardArray)
            tempBoardC.setStateArray(pGameRecordArray.stateArray)
            val boardSquareStr: String = tempBoardC.getBoard()
            val gameStateStr: String = tempBoardC.getState()

            // Add record to dictionary
            gameRecordDict[pGameRecordArray.id] = pGameRecordArray

            // Add record to database
            val db = DatabaseHelper.getInstance(App.appContext).writableDatabase
            val contentValues = ContentValues()
            contentValues.put("Id", pGameRecordArray.id)
            contentValues.put("BoardSquareStr", boardSquareStr)
            contentValues.put("GameStateStr", gameStateStr)
            result = db.update("${table.GameRecord}", contentValues, "Id=?", arrayOf(pGameRecordArray.id.toString()))


            // Update the current game if updating max record
            val maxId = getMaxId()
            if (pGameRecordArray.id == maxId)
            {
                currentGame.setBoardArray(pGameRecordArray.boardArray);
                currentGame.setStateArray(pGameRecordArray.stateArray);
            }
        }

        return result
    }


    /**
     * Clears game record
     */
    override fun reset(pWhiteClockOffset: Int, pBlackClockOffset: Int) {


        val db = DatabaseHelper.getInstance(App.appContext).writableDatabase

        db.delete("${table.GameRecord}", null, null)

        // Clear dictionary
        gameRecordDict.clear()

        // Reset the current game
        currentGame.reset()

        // Create record of default setup
        recordGameState(pWhiteClockOffset, pBlackClockOffset)
    }

    /**
     * Clear from a given [pId]
     */
    private fun clearFrom(pId: Int)
    {


        val db = DatabaseHelper.getInstance(App.appContext).writableDatabase

        db.delete("${table.GameRecord}", "Id >= ?", arrayOf(pId.toString()))


        // Remove from dictionary
        for (key in gameRecordDict.keys.toList())
        {
            if (key >= pId) gameRecordDict.remove(key)
        }

    }


    /**
     * Undo last move
     */
    override fun undo(): Boolean {
        val lastMoveId = getMaxId()

        val previousBoard = get(lastMoveId - 1)
        var returnValue = false

        if (previousBoard != null) {
            // Remove record from database
            clearFrom(lastMoveId)

            // Set the current game to a previous board
            currentGame.setBoardArray(previousBoard.boardArray);
            currentGame.setStateArray(previousBoard.stateArray);

            // Return true
            returnValue = true
        }

        return returnValue
    }

    /**
     * Compares two boards and returns sq ids that have changed
     */
    override fun getBoardSquareChanges(pBoardA: GameRecordArray?, pBoardB: GameRecordArray?): ULong {

        var mask: ULong = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL
        var changedIndexes = 0UL

        //Loop through bit boards and detectchanges
        if (pBoardA != null && pBoardB != null) {
            tempBoardA.setBoardArray(pBoardA.boardArray);
            tempBoardA.setStateArray(pBoardA.stateArray);
            tempBoardB.setBoardArray(pBoardB.boardArray);
            tempBoardB.setStateArray(pBoardB.stateArray);

            for (i in 0..63) {
                val sqAspin = tempBoardA.getSpin(i)
                val sqBspin = tempBoardB.getSpin(i)
                if (sqAspin != sqBspin) changedIndexes = changedIndexes or mask
                mask = mask shr 1
            }
        }
        return changedIndexes
    }


    /**
     * Gets last id in the table
     */
    override fun getMaxId(): Int
    {
        var maxId = 0

        val db = DatabaseHelper.getInstance(App.appContext).readableDatabase
        db.rawQuery("select max(Id) from ${table.GameRecord}", null).use { dbCursor ->
            if (dbCursor.count > 0) {
                dbCursor.moveToFirst()
                maxId = dbCursor.getInt(0)
            }
        }

        return maxId

    }


    /**
     * Get all record Ids as an ArrayList
     */
    override fun getAllRecordIDList(): ArrayList<Int>
    {

        val recordIDList = ArrayList<Int>()

        // Get list of Ids from the database
        val db = DatabaseHelper.getInstance(App.appContext).readableDatabase
        db.rawQuery("select id from ${table.GameRecord} order by id", null).use { dbCursor ->
            while (dbCursor.moveToNext()) {
                val Id = dbCursor.getInt(0)
                recordIDList.add(Id)
            }
        }

        return recordIDList

    }

    /**
     * Returns a count of the records in the hashmap
     */
    override fun recordCount(): Int {
        return gameRecordDict.count()
    }

    /**
    * Gets the game history
    */
    override fun gameHistory(): SortedMap<Int, GameRecordArray>
    {
        val history: SortedMap<Int, GameRecordArray> = gameRecordDict.toSortedMap();
        return history;
    }


    companion object  {
        private val instanceMap = mutableMapOf<Int, GameRecordDataService>()

        fun getInstance(pActivityID: Int): GameRecordDataService {
            val instance: GameRecordDataService = instanceMap.get(pActivityID) ?: run {
                val newInstance = GameRecordDataService(pActivityID)
                instanceMap.put(pActivityID, newInstance)
                newInstance
            }

            return instance

        }

    }

}
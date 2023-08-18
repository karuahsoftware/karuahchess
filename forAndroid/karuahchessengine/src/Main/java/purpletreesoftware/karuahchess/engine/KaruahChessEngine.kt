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

package purpletreesoftware.karuahchess.engine

import android.content.Context
import android.content.res.AssetManager

@ExperimentalUnsignedTypes
class KaruahChessEngine(pContext: Context?, pActivityID: Int) {

    private val id: Int
    private val activityID: Int
    private val kce: KaruahChessEngineC
    private val kce1: KaruahChessEngineC1

    fun getBoard() : String {
        if (activityID == 0) {
            return kce.getBoard(id)
        }
        else if (activityID == 1) {
            return kce1.getBoard(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getOccupiedByColour(pColour: Int): ULong {
        if (activityID == 0) {
            return kce.getOccupiedByColour(pColour, id).toULong()
        }
        else if (activityID == 1) {
            return kce1.getOccupiedByColour(pColour, id).toULong()
        }
        else {
            throw Exception("Invalid activity id.")
        }

    }

    fun getState(): String {
        if (activityID == 0) {
            return kce.getState(id)
        }
        else if (activityID == 1) {
            return kce1.getState(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }


    fun setBoard(pBoardFENString : String) {
        if (activityID == 0) {
            kce.setBoard(pBoardFENString, id)
        }
        else if (activityID == 1) {
            kce1.setBoard(pBoardFENString, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setState(pBoardStateString : String) {
        if (activityID == 0) {
            kce.setState(pBoardStateString, id)
        }
        else if (activityID == 1) {
            kce1.setState(pBoardStateString, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getBoardArray(): ULongArray {
        if (activityID == 0) {
            return kce.getBoardArrayL(id).toULongArray()
        }
        else if (activityID == 1) {
            return kce1.getBoardArrayL(id).toULongArray()
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateArray() : IntArray {
        if (activityID == 0) {
            return kce.getStateArray(id)
        }
        else if (activityID == 1) {
            return kce1.getStateArray(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setBoardArray(pBoardArray : ULongArray) {
        if (activityID == 0) {
            kce.setBoardArrayL(pBoardArray.toLongArray(), id)
        }
        else if (activityID == 1) {
            kce1.setBoardArrayL(pBoardArray.toLongArray(), id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setStateArray(pStateArray : IntArray) {
        if (activityID == 0) {
            kce.setStateArray(pStateArray, id)
        }
        else if (activityID == 1) {
            kce1.setStateArray(pStateArray, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setStateWhiteClockOffset(pOffset : Int) {
        if (activityID == 0) {
            kce.setStateWhiteClockOffset(pOffset, id)
        }
        else if (activityID == 1) {
            kce1.setStateWhiteClockOffset(pOffset, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setStateBlackClockOffset(pOffset : Int) {
        if (activityID == 0) {
            kce.setStateBlackClockOffset(pOffset, id)
        }
        else if (activityID == 1) {
            kce1.setStateBlackClockOffset(pOffset, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateWhiteClockOffset(): Int {
        if (activityID == 0) {
            return kce.getStateWhiteClockOffset(id)
        }
        else if (activityID == 1) {
            return kce1.getStateWhiteClockOffset(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateBlackClockOffset(): Int {
        if (activityID == 0) {
            return kce.getStateBlackClockOffset(id)
        }
        else if (activityID == 1) {
            return kce1.getStateBlackClockOffset(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun reset(){
        if (activityID == 0) {
            kce.reset(id)
        }
        else if (activityID == 1) {
            kce1.reset(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }


    fun cancelSearch() {
        if (activityID == 0) {
            kce.cancelSearch()
        }
        else if (activityID == 1) {
            kce1.cancelSearch()
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getSpin(pIndex: Int): Int {
        if (activityID == 0) {
            return kce.getSpin(pIndex, id)
        }
        else if (activityID == 1) {
            return kce1.getSpin(pIndex, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateActiveColour(): Int {
        if (activityID == 0) {
            return kce.getStateActiveColour(id)
        }
        else if (activityID == 1) {
            return kce1.getStateActiveColour(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setStateActiveColour(pColour: Int) {
        if (activityID == 0) {
            kce.setStateActiveColour(pColour, id)
        }
        else if (activityID == 1) {
            kce1.setStateActiveColour(pColour, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateGameStatus(): Int {
        if (activityID == 0) {
            return kce.getStateGameStatus(id)
        }
        else if (activityID == 1) {
            return kce1.getStateGameStatus(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setStateGameStatus(pStatus: Int) {
        if (activityID == 0) {
            kce.setStateGameStatus(pStatus, id)
        }
        else if (activityID == 1) {
            kce1.setStateGameStatus(pStatus, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateFullMoveCount(): Int {
        if (activityID == 0) {
            return kce.getStateFullMoveCount(id)
        }
        else if (activityID == 1) {
            return kce1.getStateFullMoveCount(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getStateCastlingAvailability(): Int {
        if (activityID == 0) {
            return kce.getStateCastlingAvailability(id)
        }
        else if (activityID == 1) {
            return kce1.getStateCastlingAvailability(id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getKingIndex(pColour: Int): Int {
        if (activityID == 0) {
            return kce.getKingIndex(pColour, id)
        }
        else if (activityID == 1) {
            return kce1.getKingIndex(pColour, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun isKingCheck(pColour: Int): Boolean {
        if (activityID == 0) {
            return kce.isKingCheck(pColour, id)
        }
        else if (activityID == 1) {
            return kce1.isKingCheck(pColour, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getPotentialMove(pSqIndex: Int): ULong {
        if (activityID == 0) {
            return kce.getPotentialMoveL(pSqIndex, id).toULong()
        }
        else if (activityID == 1) {
            return kce1.getPotentialMoveL(pSqIndex, id).toULong()
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun move(pFromIndex: Int, pToIndex: Int, pPawnPromotionPiece: Int, pValidateEnabled: Boolean, pCommit: Boolean): MoveResult {
        if (activityID == 0) {
            return kce.move(pFromIndex, pToIndex, pPawnPromotionPiece, pValidateEnabled, pCommit, id)
        }
        else if (activityID == 1) {
            return kce1.move(pFromIndex, pToIndex, pPawnPromotionPiece, pValidateEnabled, pCommit, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun arrange(pFromIndex: Int, pToIndex: Int): MoveResult {
        if (activityID == 0) {
            return kce.arrange(pFromIndex, pToIndex, id)
        }
        else if (activityID == 1) {
            return kce1.arrange(pFromIndex, pToIndex, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun arrangeUpdate(pFen: Char, pToIndex: Int): MoveResult {
        if (activityID == 0) {
            return kce.arrangeUpdate(pFen, pToIndex, id)
        }
        else if (activityID == 1) {
            return kce1.arrangeUpdate(pFen, pToIndex, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun isPawnPromotion(pFromIndex: Int, pToIndex: Int): Boolean {
        if (activityID == 0) {
            return kce.isPawnPromotion(pFromIndex, pToIndex, id)
        }
        else if (activityID == 1) {
            return kce1.isPawnPromotion(pFromIndex, pToIndex, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun findFromIndex(pToIndex: Int, pSpin: Int, pValidFromIndexes: IntArray?): Int {
        if (activityID == 0) {
            return kce.findFromIndex(pToIndex, pSpin, pValidFromIndexes, id)
        }
        else if (activityID == 1) {
            return kce1.findFromIndex(pToIndex, pSpin, pValidFromIndexes, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getSpinFromPieceName(pPieceName: String): Int {
        if (activityID == 0) {
            return kce.getSpinFromPieceName(pPieceName)
        }
        else if (activityID == 1) {
            return kce1.getSpinFromPieceName(pPieceName)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getPieceNameFromChar(pFENChar: Char): String {
        if (activityID == 0) {
            return kce.getPieceNameFromChar(pFENChar)
        }
        else if (activityID == 1) {
            return kce1.getPieceNameFromChar(pFENChar)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun getFENCharFromSpin(pSpin: Int): Char {
        if (activityID == 0) {
            return kce.getFENCharFromSpin(pSpin)
        }
        else if (activityID == 1) {
            return kce1.getFENCharFromSpin(pSpin)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }


    fun searchStart(pSearchOptions: SearchOptions): SearchResult {
        if (activityID == 0) {
            return kce.searchStart(pSearchOptions, id)
        }
        else if (activityID == 1) {
            return kce1.searchStart(pSearchOptions, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun setStateCastlingAvailability(pCastlingAvailability: Int, pColour: Int): Boolean {
        if (activityID == 0) {
            return kce.setStateCastlingAvailability(pCastlingAvailability, pColour, id)
        }
        else if (activityID == 1) {
            return kce1.setStateCastlingAvailability(pCastlingAvailability, pColour, id)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    fun cleanup(pId: Int) {
        if (activityID == 0) {
            return kce.cleanup(pId)
        }
        else if (activityID == 1) {
            return kce1.cleanup(pId)
        }
        else {
            throw Exception("Invalid activity id.")
        }
    }

    /**
     * Called by java finalizer. Cleans up native objects.
     */
    protected fun finalize() {
        cleanup(id)
    }

    init {
        activityID = pActivityID
        kce = KaruahChessEngineC()
        kce1 = KaruahChessEngineC1()

        // Load asset manager if not loaded previously
        if (KaruahChessEngine.assetMgr == null) {
            KaruahChessEngine.assetMgr = pContext?.getResources()?.getAssets();
        }

        // Use id to keep track of all engine objects that are loaded
        id = KaruahChessEngine.idCounter
        KaruahChessEngine.assetMgr?.let {
            if (activityID == 0) {
                kce.initialise(it, id)
            }
            else if (activityID == 1) {
                kce1.initialise(it, id)
            }
            else {
                throw Exception("Invalid activity id.")
            }

        }
        KaruahChessEngine.idCounter++
    }

    companion object {
        private var idCounter = 0
        private var assetMgr: AssetManager? = null
    }

}
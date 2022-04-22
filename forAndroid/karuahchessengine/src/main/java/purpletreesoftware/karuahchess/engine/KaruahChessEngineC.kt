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

package purpletreesoftware.karuahchess.engine

@ExperimentalUnsignedTypes
class KaruahChessEngineC {

    private val id: Int

    private external fun initialise(pId: Int)

    fun getBoard() : String {
        return getBoard(id)
    }
    private external fun getBoard(pId: Int): String

    fun getOccupiedByColour(pColour: Int): ULong {
        return getOccupiedByColour(pColour, id).toULong()
    }
    private external fun getOccupiedByColour(pColour: Int, pId: Int): Long

    fun getState(): String {
        return getState(id)
    }
    private external fun getState(pId: Int): String

    fun setBoard(pBoardFENString : String) {
        setBoard(pBoardFENString, id)
    }
    private external fun setBoard(pBoardFENString : String, pId: Int)

    fun setState(pBoardStateString : String) {
        setState(pBoardStateString, id)
    }
    external fun setState(pBoardStateString : String, pId: Int)

    fun getBoardArray(): ULongArray {
        return getBoardArrayL(id).toULongArray()
    }
    private external fun getBoardArrayL(pId: Int) : LongArray

    fun getStateArray() : IntArray {
        return getStateArray(id)
    }
    private external fun getStateArray(pId: Int) : IntArray

    fun setBoardArray(pBoardArray : ULongArray) {
        setBoardArrayL(pBoardArray.toLongArray(), id)
    }
    private external fun setBoardArrayL(pBoardArray : LongArray, pId: Int)

    fun setStateArray(pStateArray : IntArray) {
        setStateArray(pStateArray, id)
    }

    private external fun setStateArray(pStateArray : IntArray, pId: Int)

    fun setStateWhiteClockOffset(pOffset : Int) {
        setStateWhiteClockOffset(pOffset, id)
    }
    private external fun setStateWhiteClockOffset(pOffset : Int, pId: Int)

    fun setStateBlackClockOffset(pOffset : Int) {
        setStateBlackClockOffset(pOffset, id)
    }
    private external fun setStateBlackClockOffset(pOffset : Int, pId: Int)

    fun getStateWhiteClockOffset(): Int {
        return getStateWhiteClockOffset(id)
    }
    private external fun getStateWhiteClockOffset(pId: Int): Int

    fun getStateBlackClockOffset(): Int {
        return getStateBlackClockOffset(id)
    }
    private external fun getStateBlackClockOffset(pId: Int): Int

    fun reset(){
        reset(id)
    }

    private external fun reset(pId: Int)

    external fun cancelSearch()

    fun getSpin(pIndex: Int): Int {
        return getSpin(pIndex, id)
    }
    private external fun getSpin(pIndex: Int, pId: Int): Int

    fun getStateActiveColour(): Int {
        return getStateActiveColour(id)
    }
    private external fun getStateActiveColour(pId: Int): Int

    fun setStateActiveColour(pColour: Int) {
        setStateActiveColour(pColour, id)
    }
    private external fun setStateActiveColour(pColour: Int, pId: Int)

    fun getStateGameStatus(): Int {
        return getStateGameStatus(id)
    }
    private external fun getStateGameStatus(pId: Int): Int

    fun setStateGameStatus(pStatus: Int) {
        setStateGameStatus(pStatus, id)
    }
    private external fun setStateGameStatus(pStatus: Int, pId: Int)

    fun getStateFullMoveCount(): Int {
        return getStateFullMoveCount(id)
    }
    private external fun getStateFullMoveCount(pId: Int): Int

    fun getStateCastlingAvailability(): Int {
        return getStateCastlingAvailability(id)
    }
    private external fun getStateCastlingAvailability(pId: Int): Int

    fun getKingIndex(pColour: Int): Int {
        return getKingIndex(pColour, id)
    }
    private external fun getKingIndex(pColour: Int, pId: Int): Int

    fun isKingCheck(pColour: Int): Boolean {
        return isKingCheck(pColour, id)
    }
    private external fun isKingCheck(pColour: Int, pId: Int): Boolean

    fun getPotentialMove(pSqIndex: Int): ULong {
        return getPotentialMoveL(pSqIndex, id).toULong()
    }
    private external fun getPotentialMoveL(pSqIndex: Int, pId: Int): Long


    fun move(pFromIndex: Int, pToIndex: Int, pPawnPromotionPiece: Int, pValidateEnabled: Boolean, pCommit: Boolean): MoveResult {
        return move(pFromIndex, pToIndex, pPawnPromotionPiece, pValidateEnabled, pCommit, id)
    }
    private external fun move(pFromIndex: Int, pToIndex: Int, pPawnPromotionPiece: Int, pValidateEnabled: Boolean, pCommit: Boolean, pId: Int): MoveResult

    fun arrange(pFromIndex: Int, pToIndex: Int): MoveResult {
        return arrange(pFromIndex, pToIndex, id)
    }
    private external fun arrange(pFromIndex: Int, pToIndex: Int, pId: Int): MoveResult

    fun arrangeUpdate(pFen: Char, pToIndex: Int): MoveResult {
        return arrangeUpdate(pFen, pToIndex, id)
    }
    private external fun arrangeUpdate(pFen: Char, pToIndex: Int, pId: Int): MoveResult


    fun isPawnPromotion(pFromIndex: Int, pToIndex: Int): Boolean {
        return isPawnPromotion(pFromIndex, pToIndex, id)
    }
    private external fun isPawnPromotion(pFromIndex: Int, pToIndex: Int, pId: Int): Boolean


    fun findFromIndex(pToIndex: Int, pSpin: Int, pValidFromIndexes: IntArray?): Int {
        return findFromIndex(pToIndex, pSpin, pValidFromIndexes, id)
    }
    private external fun findFromIndex(pToIndex: Int, pSpin: Int, pValidFromIndexes: IntArray?, pId: Int): Int

    external fun getSpinFromPieceName(pPieceName: String): Int

    external fun getPieceNameFromChar(pFENChar: Char): String

    external fun getFENCharFromSpin(pSpin: Int): Char


    fun searchStart(pSearchOptions: SearchOptions): SearchResult {
        return searchStart(pSearchOptions, id)
    }
    external fun searchStart(pSearchOptions: SearchOptions, pId: Int): SearchResult

        fun setStateCastlingAvailability(pCastlingAvailability: Int, pColour: Int): Boolean {
        return setStateCastlingAvailability(pCastlingAvailability, pColour, id)
    }
    private external fun setStateCastlingAvailability(pCastlingAvailability: Int, pColour: Int, pId: Int): Boolean


    private external fun cleanup(pId: Int)

    /**
     * Called by java finalizer. Cleans up native objects.
     */
    protected fun finalize() {
        cleanup(id)
    }

    init {
        id = idCounter
        initialise(id)
        idCounter++
    }

    companion object {

        private var idCounter = 0
        init {
             System.loadLibrary("KaruahChessEngine-C")
        }
    }
}
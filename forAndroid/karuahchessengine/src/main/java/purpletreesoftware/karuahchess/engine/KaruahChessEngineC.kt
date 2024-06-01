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

import android.content.res.AssetManager

@ExperimentalUnsignedTypes
class KaruahChessEngineC() {

    external fun initialise(pAssetMgr: AssetManager, pId: Int)

    external fun getBoard(pId: Int): String

    external fun getOccupiedByColour(pColour: Int, pId: Int): Long

    external fun getState(pId: Int): String

    external fun setBoard(pBoardFENString : String, pId: Int)

    external fun setState(pBoardStateString : String, pId: Int)

    external fun getBoardArrayL(pId: Int) : LongArray

    external fun getStateArray(pId: Int) : IntArray

    external fun setBoardArrayL(pBoardArray : LongArray, pId: Int)

    external fun setStateArray(pStateArray : IntArray, pId: Int)

    external fun setStateWhiteClockOffset(pOffset : Int, pId: Int)

    external fun setStateBlackClockOffset(pOffset : Int, pId: Int)

    external fun getStateWhiteClockOffset(pId: Int): Int

    external fun getStateBlackClockOffset(pId: Int): Int

    external fun reset(pId: Int)

    external fun cancelSearch()

    external fun getSpin(pIndex: Int, pId: Int): Int

    external fun getStateActiveColour(pId: Int): Int

    external fun setStateActiveColour(pColour: Int, pId: Int)

    external fun getStateGameStatus(pId: Int): Int

    external fun setStateGameStatus(pStatus: Int, pId: Int)

    external fun getStateFullMoveCount(pId: Int): Int

    external fun getStateCastlingAvailability(pId: Int): Int

    external fun getKingIndex(pColour: Int, pId: Int): Int

    external fun isKingCheck(pColour: Int, pId: Int): Boolean

    external fun getPotentialMoveL(pSqIndex: Int, pId: Int): Long

    external fun move(pFromIndex: Int, pToIndex: Int, pPawnPromotionPiece: Int, pValidateEnabled: Boolean, pCommit: Boolean, pId: Int): MoveResult

    external fun arrange(pFromIndex: Int, pToIndex: Int, pId: Int): MoveResult

    external fun arrangeUpdate(pFen: Char, pToIndex: Int, pId: Int): MoveResult

    external fun isPawnPromotion(pFromIndex: Int, pToIndex: Int, pId: Int): Boolean

    external fun findFromIndex(pToIndex: Int, pSpin: Int, pValidFromIndexes: IntArray?, pId: Int): Int

    external fun getSpinFromPieceName(pPieceName: String): Int

    external fun getPieceNameFromChar(pFENChar: Char): String

    external fun getFENCharFromSpin(pSpin: Int): Char

    external fun searchStart(pSearchOptions: SearchOptions, pId: Int): SearchResult

    external fun setStateCastlingAvailability(pCastlingAvailability: Int, pColour: Int, pId: Int): Boolean

    external fun cleanup(pId: Int)


    companion object {
        init {
            System.loadLibrary("KaruahChessEngine-C")
        }
    }
}
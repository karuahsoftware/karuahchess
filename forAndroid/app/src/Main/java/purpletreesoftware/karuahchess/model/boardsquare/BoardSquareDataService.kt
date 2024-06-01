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

package purpletreesoftware.karuahchess.model.boardsquare

import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.customcontrol.TilePanel
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService

@ExperimentalUnsignedTypes
class BoardSquareDataService(pActivityID: Int) {

    var gameRecordCurrentValue = 0
    private val activityID: Int

    private val _tempBoard: KaruahChessEngine

    init {
        activityID = pActivityID
        _tempBoard = KaruahChessEngine(App.appContext, activityID)
    }

    /**
     * Set [pTilePanel] to match [pBoard]
     */
    fun update(pTilePanel: TilePanel, pRecord: GameRecordArray) {
        _tempBoard.setBoardArray(pRecord.boardArray)
        _tempBoard.setStateArray(pRecord.stateArray)

        for (index in 0..63) {
            val spin = _tempBoard.getSpin(index)
            pTilePanel.setPiece(index, spin)
        }
    }

    companion object {

        private val instanceMap = mutableMapOf<Int, BoardSquareDataService>()

        fun getInstance(pActivityID: Int): BoardSquareDataService {
            val instance: BoardSquareDataService = instanceMap.get(pActivityID) ?: run {
                val newInstance = BoardSquareDataService(pActivityID)
                instanceMap.put(pActivityID, newInstance)
                newInstance
            }

            return instance

        }


    }
}
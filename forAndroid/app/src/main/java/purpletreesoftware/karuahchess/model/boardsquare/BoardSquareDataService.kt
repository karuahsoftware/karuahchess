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

package purpletreesoftware.karuahchess.model.boardsquare

import purpletreesoftware.karuahchess.engine.KaruahChessEngineC
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.customcontrol.TilePanel

@ExperimentalUnsignedTypes
class BoardSquareDataService {

    companion object {

        var gameRecordCurrentValue = 0

        private val _tempBoard: KaruahChessEngineC

        init {
                _tempBoard = KaruahChessEngineC()
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


    }
}
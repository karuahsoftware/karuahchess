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

import purpletreesoftware.karuahchess.database.TableName
import java.util.SortedMap


@ExperimentalUnsignedTypes
interface IGameRecordDataService {

    fun load()

    fun getCurrentGame(): GameRecordArray

    fun get(pId: Int): GameRecordArray?

    fun get(): GameRecordArray?

    fun recordGameState(pWhiteClockOffset: Int, pBlackClockOffset: Int, pMoveSAN: String): Long

    fun updateGameState(pGameRecordArray: GameRecordArray): Int

    fun reset(pWhiteClockOffset: Int, pBlackClockOffset: Int)

    fun undo(): Boolean

    fun getBoardSquareChanges(pBoardA: GameRecordArray?, pBoardB: GameRecordArray?): ULong

    fun getMaxId(): Int

    fun getAllRecordIDList(): ArrayList<Int>

    fun recordCount(): Int

    fun gameHistory(): SortedMap<Int, GameRecordArray>

    fun getActiveMoveColour(pId: Int): Int

    fun getStateGameStatus(pId: Int): Int

}
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

package purpletreesoftware.karuahchess.rules

import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import purpletreesoftware.karuahchess.customcontrol.TilePanel


@ExperimentalUnsignedTypes
class Move {

    private val tilepanel: TilePanel

    var fromIndex: Int = -1

    var toIndex: Int = -1


    enum class HighlightEnum() {None, MovePath, Select}


    constructor(pTilePanel: TilePanel){
        tilepanel = pTilePanel
    }

    /**
     * Add a square to the move
     */
    fun Add(pBoardSquareIndex:Int, pBoard: KaruahChessEngine, pHighlight: HighlightEnum): Boolean
    {
        var complete = false

        if (fromIndex == pBoardSquareIndex)
        {
            Clear()
        }
        else if (fromIndex ==-1 && toIndex == -1)
        {
            fromIndex = pBoardSquareIndex;

            // Highlight squares
            if (pHighlight == HighlightEnum.MovePath)
            {
                var sqMark = pBoard.getPotentialMove(pBoardSquareIndex)
                if (sqMark and (Constants.BITMASK shr fromIndex) <= 0uL) sqMark = sqMark or (Constants.BITMASK shr fromIndex)

                tilepanel.setHighLight(sqMark)
            }
            else if(pHighlight == HighlightEnum.Select)
            {
                val sqMark =  Constants.BITMASK shr fromIndex
                tilepanel.setHighLight(sqMark)

            }
        }
        else if (fromIndex > -1 && toIndex == -1)
        {
            toIndex = pBoardSquareIndex
            complete = true
            tilepanel.setHighLight(0uL)
        }
        else
        {
            Clear()

        }

        return complete

    }


    /**
     * Clear the move
     */
    fun Clear() {
        fromIndex = -1
        toIndex = -1
        tilepanel.setHighLight(0uL)
    }

}
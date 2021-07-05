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

package purpletreesoftware.karuahchess.rules

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import purpletreesoftware.karuahchess.engine.KaruahChessEngineC
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.customcontrol.TileAnimationInstruction
import purpletreesoftware.karuahchess.customcontrol.TileAnimationSequence
import purpletreesoftware.karuahchess.customcontrol.TilePanel

@ExperimentalUnsignedTypes
class BoardAnimation {

    companion object {

        val _tempBoardFrom: KaruahChessEngineC = KaruahChessEngineC()
        val _tempBoardTo: KaruahChessEngineC = KaruahChessEngineC()
        /**
         * Creates a move animation sequence
         */
        fun createAnimationMoveSequence(pBoardRecA : GameRecordArray, pBoardRecB : GameRecordArray, pTilePanel: TilePanel, pContext: Context) : TileAnimationSequence
        {
            val moveDict = getAnimationMoveList(pBoardRecA, pBoardRecB)
            var animationNumber = 0
            val animationSequence: TileAnimationSequence = TileAnimationSequence()

            for (move in moveDict)
            {
                val fromIndex = move.value[0]
                val toIndex = move.value[1]
                val fromSpin = move.value[2]
                val toSpin = move.key

                if (fromIndex > -1 && toIndex >-1 && fromSpin != 0)
                {
                    // Piece move animation
                    val pieceImage = ContextCompat.getDrawable(pContext, pTilePanel.getImage(fromSpin))
                    if (pieceImage != null) {
                        val animationInstruction =
                            getAnimationInstruction(fromIndex, toIndex, pTilePanel, pieceImage, pContext)
                        if (animationInstruction != null) {
                            if (animationNumber == 0) animationSequence.add(
                                TileAnimationSequence.AnimationSeqEnum.A,
                                animationInstruction
                            )
                            else if (animationNumber == 1) animationSequence.add(
                                TileAnimationSequence.AnimationSeqEnum.B,
                                animationInstruction
                            )
                        }
                        animationNumber++
                        if (animationNumber > 1) break
                    }
                }
                else if (fromIndex > -1 && toIndex == -1 && toSpin != 0)
                {
                    // Piece take animation
                    val pieceImage = ContextCompat.getDrawable(pContext, pTilePanel.getImage(fromSpin))
                    if (pieceImage != null) {
                        val animationInstruction = getAnimationInstruction(fromIndex, fromIndex, pTilePanel, pieceImage, pContext)
                        if (animationInstruction != null) {
                            animationSequence.add(TileAnimationSequence.AnimationSeqEnum.C, animationInstruction)
                        }
                    }
                }
                else if (fromIndex == -1 && toIndex > -1 && toSpin != 0)
                {
                    // Piece return animation
                    val pieceImage = ContextCompat.getDrawable(pContext, pTilePanel.getImage(toSpin))
                    if (pieceImage != null) {
                        val animationInstruction = getAnimationInstruction(toIndex, toIndex, pTilePanel, pieceImage, pContext)
                        if (animationInstruction != null) {
                            animationSequence.add(TileAnimationSequence.AnimationSeqEnum.D, animationInstruction)
                        }
                    }
                }

            }

            return animationSequence
        }

        /**
         * Create fall animation
         */
        fun createAnimationFall(pIndex: Int, pTilePanel: TilePanel, pContext: Context) : TileAnimationSequence
        {

            val animationSequence = TileAnimationSequence()
            val spin = pTilePanel.getTile(pIndex)?.spin

            if (pIndex > -1 && spin != null && spin != 0)
            {
                // fall animation
                val pieceImage = ContextCompat.getDrawable(pContext, pTilePanel.getImage(spin))
                if (pieceImage != null) {
                    val animationInstruction = getAnimationInstruction(pIndex, pIndex, pTilePanel, pieceImage, pContext)
                    if (animationInstruction != null) {
                        animationSequence.add(TileAnimationSequence.AnimationSeqEnum.E, animationInstruction)
                    }
                }
            }

            return animationSequence
        }

        /**
         * Creates an animation instruction
         */
        private fun getAnimationInstruction(
            pFromIndex: Int,
            pToIndex: Int,
            pTilePanel: TilePanel,
            pPieceImage: Drawable,
            pContext: Context
        ): TileAnimationInstruction? {
            if (pFromIndex > -1 && pFromIndex < 64 && pToIndex > -1 && pToIndex < 64) {
                val fromTile = pTilePanel.getTile(pFromIndex)
                val toTile = pTilePanel.getTile(pToIndex)

                if (fromTile?.piece != null && toTile?.piece != null) {
                    val animInstruct = TileAnimationInstruction(pContext)
                    animInstruct.imageData.setImageDrawable(pPieceImage)
                    animInstruct.moveFromX = fromTile.x
                    animInstruct.moveFromY = fromTile.y
                    animInstruct.moveToX = toTile.x
                    animInstruct.moveToY = toTile.y

                    animInstruct.hiddenSquareIndexes.add(pFromIndex)
                    animInstruct.hiddenSquareIndexes.add(pToIndex)

                    return animInstruct
                }

            }

            return null
        }

        /**
         * Detects changes between two bitboards and returns list of moves. Only works up to two different pieces types. If
         *  more than two pieces changed, then the list returned is empty.
         */
        fun getAnimationMoveList(pBoardRecFrom: GameRecordArray, pBoardRecTo: GameRecordArray): HashMap<Int, IntArray>
        {


            _tempBoardFrom.setBoardArray(pBoardRecFrom.boardArray)
            _tempBoardFrom.setStateArray(pBoardRecFrom.stateArray)

            _tempBoardTo.setBoardArray(pBoardRecTo.boardArray)
            _tempBoardTo.setStateArray(pBoardRecTo.stateArray)


            val moveDict = HashMap<Int, IntArray>(2)
            var tooManyChanges = false

            //Loop through bit boards and detectchanges

            // Find from index move
            for (i in 0..63)
            {
                val fromSpin = _tempBoardFrom.getSpin(i)
                val toSpin = _tempBoardTo.getSpin(i)
                if (toSpin != fromSpin && fromSpin != 0)
                {
                    if (moveDict.containsKey(fromSpin))
                    {
                        tooManyChanges = true
                        moveDict.clear()
                        break
                    }
                    else
                    {
                        val move = intArrayOf( -1, -1, 0 ) //from, to, from spin, to spin
                        move[0] = i
                        move[2] = fromSpin
                        moveDict[fromSpin] = move
                    }
                }
            }

            // If too many changes just clear and return empty dictionary
            if (tooManyChanges)
            {
                moveDict.clear()
                return moveDict
            }

            // Find to index move
            for (i in 0..63)
            {
                val fromSpin = _tempBoardFrom.getSpin(i)
                val toSpin = _tempBoardTo.getSpin(i)
                if (fromSpin != toSpin && toSpin != 0)
                {
                    if (moveDict.containsKey(toSpin))
                    {
                        val move = moveDict[toSpin]
                        if (move != null) move[1] = i
                    }
                    else
                    {
                        val move = IntArray(3)
                        move[0] = -1
                        move[1] = i
                        move[2] = toSpin
                        moveDict[toSpin] = move
                    }
                }
            }

            // Limit to two changes
            if (moveDict.count() > 2) moveDict.clear()



            return moveDict
        }




    }

}
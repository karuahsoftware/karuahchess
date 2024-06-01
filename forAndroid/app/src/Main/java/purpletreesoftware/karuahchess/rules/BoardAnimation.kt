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

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.common.Helper
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import purpletreesoftware.karuahchess.customcontrol.TileAnimationInstruction
import purpletreesoftware.karuahchess.customcontrol.TilePanel
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService
import java.util.ArrayList


@ExperimentalUnsignedTypes
class BoardAnimation(pActivityID: Int) {

    val _tempBoardBefore: KaruahChessEngine = KaruahChessEngine(App.appContext, pActivityID)
    val _tempBoardAfter: KaruahChessEngine = KaruahChessEngine(App.appContext, pActivityID)

    /**
     * Creates a move animation sequence
     */
    fun createAnimationList(pBoardRecA: GameRecordArray, pBoardRecB: GameRecordArray, pTilePanel: TilePanel, pContext: Context, pDuration: Long) : ArrayList<TileAnimationInstruction>
    {
        val animationList: ArrayList<TileAnimationInstruction> = ArrayList(4)
        val moveList = getAnimationMoveList(pBoardRecA, pBoardRecB)

        for (move in moveList) {
            val spin = move[0]
            val fromIndex = move[1]
            val toIndex = move[2]

            if (fromIndex > -1 && toIndex > -1) {
                // Piece move animation
                val pieceImgData = ContextCompat.getDrawable(pContext, pTilePanel.getImage(spin))
                if (pieceImgData != null) {
                    val instruction = getAnimationInstruction(fromIndex, toIndex, pTilePanel, pieceImgData, pContext)
                    if (instruction != null) {
                        instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Move
                        instruction.duration = pDuration
                        animationList.add(instruction)
                    }
                }
            }
            else if (fromIndex > -1 && toIndex == -1) {
                // Look for a pawn promotion move
                var promotionIndex = -1
                if (spin == 1 || spin == -1) {
                    for (nestedMove in moveList) {
                        val nestedSpin = nestedMove[0]
                        val nestedFromIndex = nestedMove[1]
                        val nestedToIndex = nestedMove[2]
                        if ((spin == 1 && nestedSpin > 1 && nestedFromIndex == -1 && nestedToIndex > -1) || (spin == -1 && nestedSpin < -1 && nestedFromIndex == -1 && nestedToIndex > -1)) promotionIndex = nestedToIndex
                    }
                }

                if (promotionIndex > -1) {
                    // Pawn promotion animation
                    val pieceImgData = ContextCompat.getDrawable(pContext, pTilePanel.getImage(spin))
                    if (pieceImgData != null) {
                        val instruction = getAnimationInstruction(fromIndex, promotionIndex, pTilePanel, pieceImgData, pContext)
                        if (instruction != null) {
                            instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.MoveFade
                            instruction.duration = pDuration
                            animationList.add(instruction)
                        }
                    }
                }
                else
                {
                    // Piece take animation
                    val pieceImgData = ContextCompat.getDrawable(pContext, pTilePanel.getImage(spin))
                    if (pieceImgData != null) {
                        val instruction = getAnimationInstruction(fromIndex, fromIndex, pTilePanel, pieceImgData, pContext)
                        if (instruction != null) {
                            instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Take
                            instruction.duration = pDuration
                            animationList.add(instruction)
                        }
                    }
                }
            }
            else if (fromIndex == -1 && toIndex > -1)
            {
                // Piece return animation
                val pieceImgData = ContextCompat.getDrawable(pContext, pTilePanel.getImage(spin))
                if (pieceImgData != null) {
                    val instruction = getAnimationInstruction(toIndex, toIndex, pTilePanel, pieceImgData, pContext)
                    if (instruction != null) {
                        instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Put
                        instruction.duration = pDuration
                        animationList.add(instruction)
                    }
                }
            }
        }

        return animationList
    }



    /**
     * Create fall animation
     */
    fun createAnimationFall(pIndex: Int, pTilePanel: TilePanel, pContext: Context, pDuration: Long) : ArrayList<TileAnimationInstruction>
    {
        val animationList: ArrayList<TileAnimationInstruction> = ArrayList(4)
        val spin = pTilePanel.getTile(pIndex)?.spin

        if (pIndex > -1 && spin != null && spin != 0) {
            // fall animation
            val pieceImage = ContextCompat.getDrawable(pContext, pTilePanel.getImage(spin))
            if (pieceImage != null) {
                val instruction = getAnimationInstruction(pIndex, pIndex, pTilePanel, pieceImage, pContext)
                if (instruction != null) {
                    instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Fall
                    instruction.duration = pDuration
                    animationList.add(instruction)
                }
            }
        }

        return animationList
    }



    /**
     * Detects changes between two bitboards and returns list of moves. Only works up to two different piece types. If
     * more than two pieces changed, then the list returned is empty.
     */
    private fun getAnimationMoveList(pBoardRecBefore: GameRecordArray, pBoardRecAfter: GameRecordArray ): ArrayList<IntArray>
    {
        _tempBoardBefore.setBoardArray(pBoardRecBefore.boardArray)
        _tempBoardBefore.setStateArray(pBoardRecBefore.stateArray)
        _tempBoardAfter.setBoardArray(pBoardRecAfter.boardArray)
        _tempBoardAfter.setStateArray(pBoardRecAfter.stateArray)

        val allBeforeWhitePos: ULong = _tempBoardBefore.getOccupiedByColour(Constants.WHITEPIECE)
        val allBeforeBlackPos: ULong = _tempBoardBefore.getOccupiedByColour(Constants.BLACKPIECE)
        val allAfterWhitePos: ULong = _tempBoardAfter.getOccupiedByColour(Constants.WHITEPIECE)
        val allAfterBlackPos: ULong = _tempBoardAfter.getOccupiedByColour(Constants.BLACKPIECE)

        val allChangeWhitePos: ULong = allBeforeWhitePos xor allAfterWhitePos
        val allChangeBlackPos: ULong = allBeforeBlackPos xor allAfterBlackPos


        var spinChange: Array<IntArray> = Array(13) { intArrayOf(0,0) }
        spinChange = createChangeArray(allChangeWhitePos, spinChange)
        spinChange = createChangeArray(allChangeBlackPos, spinChange)
        val spinChangeList: ArrayList<IntArray> = convertSpinChangeArrayToList(spinChange)

        // Too many changes to animate so just clear the list
        if (spinChangeList.size > 3)
        {
            spinChangeList.clear()
        }

        return spinChangeList
    }



    /**
     * Converts a spin change array to a list of changes
     */
    private fun convertSpinChangeArrayToList(pSpinChange: Array<IntArray>): ArrayList<IntArray>
    {
        val spinOffset: Int = 6
        val sqIndexOffset: Int = 1

        // Build the list of changes
        val pieceChangeList: ArrayList<IntArray> = arrayListOf()
        for (i in 0..12)
        {
            if (pSpinChange[i][0] > 0 || pSpinChange[i][1] > 0)
            {
                val change = intArrayOf(i - spinOffset, -1, -1 )
                if (pSpinChange[i][0] > 0) change[1] = pSpinChange[i][0] - sqIndexOffset
                if (pSpinChange[i][1] > 0) change[2] = pSpinChange[i][1] - sqIndexOffset
                pieceChangeList.add(change)
            }
        }

        return pieceChangeList
    }

    /**
     * Creates an array of spin changes
     */
    private fun createChangeArray(pChangedPositions: ULong, pSpinChange: Array<IntArray>) : Array<IntArray>
    {
        // [Spin offset, {From Index, To Index}]

        val spinOffset: Int = 6
        val sqIndexOffset: Int = 1
        var changedPositions: ULong = pChangedPositions
        val spinChange: Array<IntArray> = pSpinChange

        // Changes

        while (changedPositions > 0uL)
        {

            val pos: Int = Helper.bitScanForward(changedPositions)
            val sqMask: ULong = 1uL shl pos
            changedPositions = changedPositions xor sqMask
            val sqIndex = 63 - pos

            val beforeSpin = _tempBoardBefore.getSpin(sqIndex)
            val afterSpin = _tempBoardAfter.getSpin(sqIndex)

            if (beforeSpin != 0) spinChange[beforeSpin + spinOffset][0] = sqIndex + sqIndexOffset
            if (afterSpin != 0) spinChange[afterSpin + spinOffset][1] = sqIndex + sqIndexOffset

        }

        return spinChange
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

        return null
    }

    companion object {
        private val instanceMap = mutableMapOf<Int, BoardAnimation>()

        fun getInstance(pActivityID: Int): BoardAnimation {
            val instance: BoardAnimation = instanceMap.get(pActivityID) ?: run {
                val newInstance = BoardAnimation(pActivityID)
                instanceMap.put(pActivityID, newInstance)
                newInstance
            }

            return instance

        }


    }

}
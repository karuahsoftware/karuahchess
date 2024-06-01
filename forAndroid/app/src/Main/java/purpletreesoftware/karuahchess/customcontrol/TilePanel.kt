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

package purpletreesoftware.karuahchess.customcontrol

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import android.view.animation.AnimationUtils
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Drawable
import android.view.DragEvent
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.R.color
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.TilepanelBinding


@ExperimentalUnsignedTypes
class TilePanel: ConstraintLayout {
    private val _tileList = ArrayList<Tile>(64)
    private var _tileListener: OnTilePanelInteractionListener? = null
    private var _shakeEnabled = false
    private val _shakeAnimation = AnimationUtils.loadAnimation(this.context, R.anim.shake)
    private var _binding: TilepanelBinding? = null
    private val binding get() = _binding!!


    // Creates a new drag event listener
    private val _tileDragListen = View.OnDragListener { v, event ->

        // Handles each of the expected events
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                val tile = (v as? Tile)
                tile?.panel?.setHighlightFull(Constants.BITMASK shr tile.index)
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {

                true
            }
            DragEvent.ACTION_DROP -> {
                val item: ClipData.Item = event.clipData.getItemAt(0)
                val fromIndex: Int = item.text.toString().toIntOrNull() ?: -1
                val toIndex: Int = (v as? Tile)?.index ?: -1
                val fromTile = getTile(fromIndex)
                val toTile = getTile(toIndex)

                if (fromTile != null && toTile != null) {
                    _tileListener?.onTileMoveAction(fromTile, toTile)
                    shake(toTile, true)
                    shake(fromTile, true)
                }
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                val tile = (v as? Tile)
                tile?.piece?.visibility = View.VISIBLE
                tile?.panel?.setHighlightFull(0UL)

                true
            }
            else -> {
                // An unknown action type was received.
                false
            }
        }
    }

    // Size of a single board tile
    var tileSize: Int = 0
        private set

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context) {
        // Inflate view
        _binding = TilepanelBinding.inflate(LayoutInflater.from(context), this, true)

        val darkSquareColour = ContextCompat.getColor(context, color.colorBlackSquare)
        val lightSquareColour = ContextCompat.getColor(context, color.colorWhiteSquare)

        // Create tiles
        for(tileIndex in 0..63) {
            val tileId = tileIndex + 1
            val tile = Tile(context, tileIndex, this)
            val sqColour = if (isDarkSquare(tileIndex)) darkSquareColour else lightSquareColour

            tile.id = tileId
            tile.setBackgroundColor(sqColour)
            _tileList.add(tile)

            // Set click listener
            tile.setOnClickListener {
                _tileListener?.onTileClick(tile)
            }

            tile.setOnLongClickListener { v: View ->
                val fromIndexStr = tile.index.toString()
                val fromIndexClipItem = ClipData.Item(fromIndexStr)

                tile.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                val dragData = ClipData(
                    fromIndexStr,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    fromIndexClipItem)

                // Instantiates the drag shadow builder.
                val tileShadow = TileShadowBuilder(this, tile.piece)

                // Starts the drag
                v.startDragAndDrop(
                    dragData,   // the data to be dragged
                    tileShadow,   // the drag shadow builder
                    null,       // no need to use local data
                    0           // flags (not currently used, set to 0)
                )

                // Hides the piece, so only the shadow is displayed
                shake(tile, false) // This is done as the animation doesn't update unless it is restarted
                tile.piece.visibility = View.GONE


                false
            }

            tile.setOnDragListener(_tileDragListen)
        }

    }


    /**
     * Gets a tile as specified by its [pIndex]
     */
    fun getTile(pIndex: Int) : Tile? {
        return if (pIndex in 0..63 && _tileList.size == 64) {
            _tileList[pIndex]
        } else null
    }

    /**
     * Sets tile [pIndex] to show image of [pSpin]
     */
    fun setPiece(pIndex: Int, pSpin: Int) {
        if(pIndex in 0..63 && pSpin in -6..6) {
            val tile = _tileList[pIndex]
            // Update if different from current value
            if(tile.spin != pSpin) {
                tile.piece.setImageResource(getImage(pSpin))
                tile.spin = pSpin
            }
        }
    }

    /**
     * Turn shake animation on or off [pEnable]
     */
    fun shake(pEnable: Boolean) {
        if (_tileList.count() > 0) {

            if(pEnable) {
                for (tile in _tileList) {
                    if(tile.spin != 0) tile.piece.startAnimation(_shakeAnimation)
                }
                _shakeEnabled = true
            }
            else {
                for (tile in _tileList) {
                     tile.piece.clearAnimation()
                }
                _shakeEnabled = false
            }
        }
    }

    /**
     * Set shake status on individual tile
     */
    private fun shake(pTile : Tile, pEnable : Boolean) {
        if (_shakeEnabled && pEnable) {
            pTile.piece.startAnimation(_shakeAnimation)
        }
        else {
            pTile.piece.clearAnimation()
        }
    }

    /**
     * Refresh the shake animation on all tiles
     */
    fun shakeRefresh() {
        if (_tileList.count() > 0) {

            if(_shakeEnabled) {
                for (tile in _tileList) {
                    if(tile.spin != 0) tile.piece.startAnimation(_shakeAnimation)
                }
            }
            else {
                for (tile in _tileList) {
                    tile.piece.clearAnimation()
                }
            }
        }
    }

    /**
     * Show squares that moved last
     */
    fun setHighlightFullFadeOut(pBits: ULong, pColour: Int) {
        if (tileSize > 0) {
            val size = (tileSize - 2)
            var sqId: ULong = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL

            for (i in 0..63) {
                if ((sqId and pBits) != 0uL) {
                    val square: Drawable = getRectangle(size, pColour)
                    _tileList[i].highlight.setImageDrawable(square)
                    val fadeInAnimation = AnimationUtils.loadAnimation(this.context, R.anim.fadeout)
                    fadeInAnimation.fillAfter = true
                    _tileList[i].highlight.startAnimation(fadeInAnimation)
                } else {
                    _tileList[i].highlight.setImageResource(0)
                }
                sqId = sqId shr 1
            }
        }
    }

    /**
     * Highlights squares as per [pBits] binary list
     */
    fun setHighLight(pBits: ULong) {

        if (tileSize > 0) {
            var sqId: ULong = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL
            val circleColour = ContextCompat.getColor(this.context, color.colorDarkGreen)

            for (i in 0..63) {
                val size = if (_tileList[i].piece.drawable == null) (tileSize * 0.3).toInt() else (tileSize * 0.95).toInt()

                if ((sqId and pBits) > 0uL) {
                    val circle: Drawable = getCircle(size, circleColour)
                    _tileList[i].highlight.setImageDrawable(circle)

                    val fadeInAnimation = AnimationUtils.loadAnimation(this.context, R.anim.fadein)
                    _tileList[i].highlight.startAnimation(fadeInAnimation)
                } else {
                    _tileList[i].highlight.setImageResource(0)
                }
                sqId = sqId shr 1
            }
        }
    }

    /**
     * Show drag destination square
     */
    fun setHighlightFull(pBits: ULong) {
        if (tileSize > 0) {
            val size = (tileSize - 2)
            var sqId: ULong = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL

            for (i in 0..63) {
                if ((sqId and pBits) != 0uL) {
                    val square: Drawable = getRectangle(size, color.colorMagenta)
                    _tileList[i].highlight.setImageDrawable(square)
                } else {
                    _tileList[i].highlight.setImageResource(0)
                }
                sqId = sqId shr 1
            }
        }
    }

    /**
     * Set check indicator
     */
    fun setCheckIndicator(pKingIndex: Int) {
        if (tileSize > 0) {

            val circleColour = ContextCompat.getColor(this.context, color.colorRed)

            for (i in 0..63) {
                val size = (tileSize * 0.95).toInt()

                if (i == pKingIndex) {
                    val circle: Drawable = getCircle(size, circleColour)
                    _tileList[i].checkIndicator.setImageDrawable(circle)
                    _tileList[i].checkIndicator.alpha = 0.7f
                    val fadeInAnimation = AnimationUtils.loadAnimation(this.context, R.anim.fadein)
                    _tileList[i].checkIndicator.startAnimation(fadeInAnimation)
                } else {
                    _tileList[i].checkIndicator.setImageResource(0)
                }
            }
        }
    }

    /**
     * Draw tiles on layout. Dimensions are the available area that the board sits on
     */
    fun drawTiles(pTileSize: Int) {
        tileSize = pTileSize

        // Set up tile list
        val cset = ConstraintSet()

        this.removeAllViews()

        for(tile in _tileList) {
            this.addView(tile, tile.index)

            // Set constraints
            cset.constrainWidth(tile.id, tileSize)
            cset.constrainHeight(tile.id, tileSize)
            cset.setHorizontalChainStyle(tile.id, ConstraintSet.CHAIN_PACKED)
            cset.setVerticalChainStyle(tile.id, ConstraintSet.CHAIN_PACKED)


            val leftID = getConnection(tile.index, ConstraintSet.START) + 1
            if (leftID == 0) cset.connect(tile.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            else cset.connect(tile.id, ConstraintSet.START, leftID, ConstraintSet.END, 0)

            val topID = getConnection(tile.index, ConstraintSet.TOP) + 1
            if (topID == 0) cset.connect(tile.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            else cset.connect(tile.id, ConstraintSet.TOP, topID, ConstraintSet.BOTTOM, 0)

        }

        cset.applyTo(this)


    }

    /**
     * Unhide all pieces
     */
    fun showAll(){
        for (tile in _tileList) {
            tile.piece.visibility = View.VISIBLE
        }

    }

    /**
     * Rotates board to specified [pRotation] degrees and rotates padding
     */
    fun rotate(pRotation: Int) {

        // Apply board rotation
        this.rotation = pRotation.toFloat()

        // Apply counter rotation to pieces
        for(tile in _tileList) {
            tile.piece.rotation = -pRotation.toFloat()
        }



    }

    /**
     * Gets a circle
     */
    private fun getCircle(pSize: Int, pColour: Int): Drawable {

        val circle = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(pColour, pColour)
        )

        circle.shape = GradientDrawable.OVAL
        circle.setStroke(1, Color.WHITE)
        circle.alpha = 170
        circle.setSize(pSize,pSize)
        return circle
    }

    /**
     * Gets a square shape filled in
     */
    private fun getRectangle(pSize: Int, pColour: Int): Drawable {

        val colour = ContextCompat.getColor(this.context, pColour)
        val rect = GradientDrawable()
        rect.shape = GradientDrawable.RECTANGLE
        rect.setStroke(2, colour)
        rect.setColor(colour)
        rect.setSize(pSize,pSize)
        return rect
    }


    /**
     * Gets tile image from [pSpin]
     */
    fun getImage(pSpin: Int): Int {
        return when (pSpin) {
            1 -> R.drawable.whitepawn
            2 -> R.drawable.whiteknight
            3 -> R.drawable.whitebishop
            4 -> R.drawable.whiterook
            5 -> R.drawable.whitequeen
            6 -> R.drawable.whiteking0
            -1 -> R.drawable.blackpawn
            -2 -> R.drawable.blackknight
            -3 -> R.drawable.blackbishop
            -4 -> R.drawable.blackrook
            -5 -> R.drawable.blackqueen
            -6 -> R.drawable.blackking0
            else -> 0
        }

    }

    /**
     * Determines if the board square [pIndex] is a dark colour (black square)
     */
    private fun isDarkSquare(pIndex: Int): Boolean {

        return if ((pIndex + 1) % 2 == 0) {
            (pIndex in 0..7) || (pIndex in 16..23) || (pIndex in 32..39) || (pIndex in 48..55)
        } else {
            !((pIndex in 0..7) || (pIndex in 16..23) || (pIndex in 32..39) || (pIndex in 48..55))
        }
    }

    /**
     * Gets connection data for a given [pTileIndex] and [pDirection]
     */
    private fun getConnection(pTileIndex: Int, pDirection: Int): Int {
        var index: ULong = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000u
        val rowMask = getRowMask(pTileIndex)
        var returnValue: Int = -1

        // Set the binary index to tile index
        index = index shr pTileIndex

        // Get left right
        val connectionBinary: ULong
        if (pDirection == ConstraintSet.START) {
            connectionBinary = (index shl 1) and rowMask
            if (connectionBinary > 0u) returnValue = pTileIndex - 1

        }
        else if(pDirection == ConstraintSet.TOP) {
            connectionBinary = index shl 8
            if (connectionBinary > 0u) returnValue = pTileIndex - 8
        }
        else if(pDirection == ConstraintSet.END) {
            connectionBinary = (index shr 1) and rowMask
            if (connectionBinary > 0u) returnValue = pTileIndex + 1
        }
        else if (pDirection == ConstraintSet.BOTTOM) {
            connectionBinary = index shr 8
            if (connectionBinary > 0u) returnValue = pTileIndex + 8
        }
        else {
            returnValue = -1
        }


        return returnValue
    }


    /**
     * Gets a row mask for an index
     */
    private fun getRowMask(pSqIndex: Int): ULong
    {
        val mask: ULong
        var shift: Int = 0
        var outOfRange: Boolean = false

        if (pSqIndex in 0..7) shift = 0
        else if (pSqIndex in 8..15) shift = 8
        else if (pSqIndex in 16..23) shift = 16
        else if (pSqIndex in 24..31) shift = 24
        else if (pSqIndex in 32..39) shift = 32
        else if (pSqIndex in 40..47) shift = 40
        else if (pSqIndex in 48..55) shift = 48
        else if (pSqIndex in 56..63) shift = 56
        else outOfRange = true


        if (!outOfRange) {
            mask = 0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000u shr shift

            return mask
        }
        else
        {
            return 0u
        }
    }

    /**
     *
     * Apply specified [pDarkSqColour] to the board
     */
    fun applyBoardColour(pDarkSqColour: Int) {
        val lightSquareColour = ContextCompat.getColor(context, color.colorWhiteSquare)

        for(tileIndex in 0..63) {
            val sqColour = if (isDarkSquare(tileIndex)) pDarkSqColour else lightSquareColour
            _tileList[tileIndex].setBackgroundColor(sqColour)
        }

        val border = GradientDrawable()
        border.setStroke(1, pDarkSqColour) //black border with full opacity
        this.foreground = border

    }

    /**
     * Sets the tile panel interaction listener
     */
    fun setTilePanelInteractionListener(pEventListener: OnTilePanelInteractionListener) {
        _tileListener = pEventListener
    }


    interface OnTilePanelInteractionListener {
        fun onTileClick(pTile: Tile?)
        fun onTileMoveAction(pFromTile: Tile?,pToTile: Tile?)
    }


}
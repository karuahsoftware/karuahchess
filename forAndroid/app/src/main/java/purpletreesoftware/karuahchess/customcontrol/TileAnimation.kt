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

package purpletreesoftware.karuahchess.customcontrol

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.*
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.customcontrol.TileAnimationSequence.AnimationSeqEnum
import purpletreesoftware.karuahchess.databinding.AnimationpanelBinding


@ExperimentalUnsignedTypes
class TileAnimation: ConstraintLayout {


    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context) {
        // Inflate view
        AnimationpanelBinding.inflate(LayoutInflater.from(context), this, true)
    }



    /**
     * Runs an animation sequence
     */
    fun runAnimation(pAnimItems: HashMap<AnimationSeqEnum, TileAnimationInstruction>, pTilePanel: TilePanel, pDurationMS: Long, pPanelMargin: Int)
    {

            // Set frame size to match board size
            val frameSize = pTilePanel.boardSize

            val params = CoordinatorLayout.LayoutParams(frameSize, frameSize)
            params.setMargins(pPanelMargin, 0, 0, 0)
            this.layoutParams = params

            // Set up animation
            this.removeAllViews()

            if (pAnimItems.containsKey(AnimationSeqEnum.A)) {
                val instruction = pAnimItems[AnimationSeqEnum.A]

                if (instruction?.imageData != null) {

                    // Set initial image position
                    instruction.imageData.layoutParams = FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                    instruction.imageData.x = instruction.moveFromX
                    instruction.imageData.y = instruction.moveFromY

                    // Set counter rotation
                    instruction.imageData.rotation = -this.rotation

                    // Add the image to the view for animation
                    this.addView(instruction.imageData)

                    // Calculate the move coordinates (as a delta from the current position)
                    val moveToDeltaX = instruction.moveToX - instruction.moveFromX
                    val moveToDeltaY = instruction.moveToY - instruction.moveFromY

                    // Set up the animation
                    val animation = TranslateAnimation(0f, moveToDeltaX, 0f, moveToDeltaY)
                    animation.duration = pDurationMS
                    animation.fillAfter = true
                    animation.fillBefore = false
                    animation.isFillEnabled = false
                    animation.interpolator = AccelerateDecelerateInterpolator()

                    val tileAnimationListener = TileAnimationListener(instruction,pTilePanel,this, true, instruction.imageData)
                    animation.setAnimationListener(tileAnimationListener)

                    instruction.imageData.startAnimation(animation)

                }


            }

            if (pAnimItems.containsKey(AnimationSeqEnum.B)) {
                val instruction = pAnimItems[AnimationSeqEnum.B]

                if (instruction?.imageData != null) {

                    // Set initial image position
                    instruction.imageData.layoutParams = FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                    instruction.imageData.x = instruction.moveFromX
                    instruction.imageData.y = instruction.moveFromY

                    // Set counter rotation
                    instruction.imageData.rotation = -this.rotation

                    // Add the image to the view for animation
                    this.addView(instruction.imageData)

                    // Calculate the move coordinates (as a delta from the current position)
                    val moveToDeltaX = instruction.moveToX - instruction.moveFromX
                    val moveToDeltaY = instruction.moveToY - instruction.moveFromY

                    // Set up the animation
                    val animation = TranslateAnimation(0f, moveToDeltaX, 0f, moveToDeltaY)
                    animation.duration = pDurationMS
                    animation.fillAfter = true
                    animation.fillBefore = false
                    animation.isFillEnabled = false
                    animation.interpolator = AccelerateDecelerateInterpolator()

                    val tileAnimationListener = TileAnimationListener(instruction,pTilePanel,this, true, instruction.imageData)
                    animation.setAnimationListener(tileAnimationListener)

                    instruction.imageData.startAnimation(animation)

                }


            }

        if (pAnimItems.containsKey(AnimationSeqEnum.C)) {
            val instruction = pAnimItems[AnimationSeqEnum.C]

            if (instruction?.imageData != null) {

                // Set initial image position
                instruction.imageData.layoutParams = FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                instruction.imageData.x = instruction.moveFromX
                instruction.imageData.y = instruction.moveFromY

                // Set counter rotation
                instruction.imageData.rotation = -this.rotation

                // Add the image to the view for animation
                this.addView(instruction.imageData)

                // Set up the animation
                val animation = AnimationUtils.loadAnimation(this.context, R.anim.piecetake)

                animation.duration = pDurationMS
                animation.fillAfter = true
                animation.fillBefore = false
                animation.isFillEnabled = false
                animation.interpolator = AccelerateDecelerateInterpolator()

                val tileAnimationListener = TileAnimationListener(instruction,pTilePanel,this, true, instruction.imageData)
                animation.setAnimationListener(tileAnimationListener)

                instruction.imageData.startAnimation(animation)

            }


        }

        if (pAnimItems.containsKey(AnimationSeqEnum.D)) {
            val instruction = pAnimItems[AnimationSeqEnum.D]

            if (instruction?.imageData != null) {

                // Set initial image position
                instruction.imageData.layoutParams = FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                instruction.imageData.x = instruction.moveFromX
                instruction.imageData.y = instruction.moveFromY

                // Set counter rotation
                instruction.imageData.rotation = -this.rotation

                // Add the image to the view for animation
                this.addView(instruction.imageData)

                // Set up the animation
                val animation = AnimationUtils.loadAnimation(this.context, R.anim.piecereturn)

                animation.duration = pDurationMS
                animation.fillAfter = true
                animation.fillBefore = false
                animation.isFillEnabled = false
                animation.interpolator = AccelerateDecelerateInterpolator()

                val tileAnimationListener = TileAnimationListener(instruction,pTilePanel,this, true, instruction.imageData)
                animation.setAnimationListener(tileAnimationListener)

                instruction.imageData.startAnimation(animation)

            }


        }

        if (pAnimItems.containsKey(AnimationSeqEnum.E)) {
            val instruction = pAnimItems[AnimationSeqEnum.E]

            if (instruction?.imageData != null) {

                // Set initial image position
                instruction.imageData.layoutParams = FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                instruction.imageData.x = instruction.moveFromX
                instruction.imageData.y = instruction.moveFromY

                // Set counter rotation
                instruction.imageData.rotation = -this.rotation

                // Add the image to the view for animation
                this.addView(instruction.imageData)

                var offsetX = 0f
                var offsetY = 0f

                when {
                    this.rotation == 0f -> {
                        offsetX = 0.5f
                        offsetY = 0.8f
                    }
                    this.rotation == 90f -> {
                        offsetX = 0.8f
                        offsetY = 0.5f
                    }
                    this.rotation == 180f -> {
                        offsetX = 0.5f
                        offsetY = 0.2f
                    }
                    this.rotation == 270f -> {
                        offsetX = 0.2f
                        offsetY = 0.5f
                    }

                    // Set up the animation
                }


                val pvX = instruction.moveFromX + (pTilePanel.tileSize * offsetX)
                val pvY = instruction.moveFromY + (pTilePanel.tileSize * offsetY)

                // Set up the animation
                val animation = RotateAnimation(0f, 90f, pvX, pvY)

                animation.duration = pDurationMS
                animation.fillAfter = true
                animation.fillBefore = false
                animation.isFillEnabled = false
                animation.interpolator = BounceInterpolator()

                val tileAnimationListener = TileAnimationListener(instruction,pTilePanel,this, false, instruction.imageData)
                animation.setAnimationListener(tileAnimationListener)

                instruction.imageData.startAnimation(animation)


            }


        }

    }


    /**
     * Clear animations
     */
    fun clear() {
        this.removeAllViews()
    }

    /**
     * Rotates board to specified [pRotation] degrees
     */
    fun rotate(pRotation: Int) {

        // Apply board rotation
        this.rotation = pRotation.toFloat()

    }

}


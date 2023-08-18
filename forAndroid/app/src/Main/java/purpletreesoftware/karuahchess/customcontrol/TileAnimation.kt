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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.*
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.AnimationpanelBinding


@ExperimentalUnsignedTypes
class TileAnimation: ConstraintLayout {

    val animationThrottle = Semaphore(1)

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
    suspend fun runAnimation(pTilePanel: TilePanel, pAnimationList: ArrayList<TileAnimationInstruction>)
    {
            // Only let one animation set run at a time
            animationThrottle.acquire()

            // Set up animation
            this.removeAllViews()

            var maxDuration: Long = 0L

            for(instruction in pAnimationList) {

                if (instruction.duration > maxDuration) maxDuration = instruction.duration

                when (instruction.animationType) {
                    TileAnimationInstruction.AnimationTypeEnum.Move -> {
                        // Set initial image position
                        instruction.imageData.layoutParams =
                            FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
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
                        animation.duration = instruction.duration
                        animation.fillAfter = true
                        animation.fillBefore = false
                        animation.isFillEnabled = false
                        animation.interpolator = AccelerateDecelerateInterpolator()

                        val tileAnimationListener = TileAnimationListener(
                            instruction,
                            pTilePanel,
                            this,
                            true,
                            instruction.imageData
                        )
                        animation.setAnimationListener(tileAnimationListener)

                        instruction.imageData.startAnimation(animation)

                    }
                    TileAnimationInstruction.AnimationTypeEnum.MoveFade -> {
                        // Set initial image position
                        instruction.imageData.layoutParams =
                            FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                        instruction.imageData.x = instruction.moveFromX
                        instruction.imageData.y = instruction.moveFromY

                        // Set counter rotation
                        instruction.imageData.rotation = -this.rotation

                        // Add the image to the view for animation
                        this.addView(instruction.imageData)

                        // Calculate the move coordinates (as a delta from the current position)
                        val moveToDeltaX = instruction.moveToX - instruction.moveFromX
                        val moveToDeltaY = instruction.moveToY - instruction.moveFromY

                        // Create animation set
                        val animationSet = AnimationSet(true)

                        // Set up the move animation
                        val animationMove = TranslateAnimation(0f, moveToDeltaX, 0f, moveToDeltaY)
                        animationMove.duration = instruction.duration
                        animationMove.fillAfter = false
                        animationMove.fillBefore = false
                        animationMove.isFillEnabled = false
                        animationMove.interpolator = AccelerateDecelerateInterpolator()
                        animationSet.addAnimation(animationMove)

                        // Set up the fade animation
                        val animationFade = AlphaAnimation(1.0f, 0f)
                        animationFade.duration = instruction.duration
                        animationFade.fillAfter = false
                        animationFade.fillBefore = false
                        animationFade.isFillEnabled = false
                        animationFade.interpolator = AccelerateDecelerateInterpolator()
                        animationSet.addAnimation(animationFade)

                        val tileAnimationListener = TileAnimationListener(
                            instruction,
                            pTilePanel,
                            this,
                            true,
                            instruction.imageData
                        )
                        animationSet.setAnimationListener(tileAnimationListener)

                        instruction.imageData.startAnimation(animationSet)

                    }
                    TileAnimationInstruction.AnimationTypeEnum.Take -> {

                        // Set initial image position
                        instruction.imageData.layoutParams =
                            FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                        instruction.imageData.x = instruction.moveFromX
                        instruction.imageData.y = instruction.moveFromY

                        // Set counter rotation
                        instruction.imageData.rotation = -this.rotation

                        // Add the image to the view for animation
                        this.addView(instruction.imageData)

                        // Set up the animation
                        val animation = AnimationUtils.loadAnimation(this.context, R.anim.piecetake)
                        animation.duration = instruction.duration
                        animation.fillAfter = false
                        animation.fillBefore = false
                        animation.isFillEnabled = false
                        animation.interpolator = AccelerateDecelerateInterpolator()

                        val tileAnimationListener = TileAnimationListener(
                            instruction,
                            pTilePanel,
                            this,
                            true,
                            instruction.imageData
                        )
                        animation.setAnimationListener(tileAnimationListener)

                        instruction.imageData.startAnimation(animation)

                    }
                    TileAnimationInstruction.AnimationTypeEnum.Put -> {
                        // Set initial image position
                        instruction.imageData.layoutParams =
                            FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
                        instruction.imageData.x = instruction.moveFromX
                        instruction.imageData.y = instruction.moveFromY

                        // Set counter rotation
                        instruction.imageData.rotation = -this.rotation

                        // Add the image to the view for animation
                        this.addView(instruction.imageData)

                        // Set up the animation
                        val animation =
                            AnimationUtils.loadAnimation(this.context, R.anim.piecereturn)

                        animation.duration = instruction.duration
                        animation.fillAfter = true
                        animation.fillBefore = true
                        animation.isFillEnabled = false
                        animation.interpolator = AccelerateDecelerateInterpolator()

                        val tileAnimationListener = TileAnimationListener(
                            instruction,
                            pTilePanel,
                            this,
                            true,
                            instruction.imageData
                        )
                        animation.setAnimationListener(tileAnimationListener)

                        instruction.imageData.startAnimation(animation)
                    }
                    TileAnimationInstruction.AnimationTypeEnum.Fall -> {
                        // Set initial image position
                        instruction.imageData.layoutParams =
                            FrameLayout.LayoutParams(pTilePanel.tileSize, pTilePanel.tileSize)
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

                        animation.duration = instruction.duration
                        animation.fillAfter = true
                        animation.fillBefore = false
                        animation.isFillEnabled = false
                        animation.interpolator = BounceInterpolator()

                        val tileAnimationListener = TileAnimationListener(
                            instruction,
                            pTilePanel,
                            this,
                            false,
                            instruction.imageData
                        )
                        animation.setAnimationListener(tileAnimationListener)

                        instruction.imageData.startAnimation(animation)

                    }
                    else -> {
                        // Do nothing
                    }
                }

            }


            if (maxDuration > 0L) delay(maxDuration)

            animationThrottle.release()
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


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

import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView

@ExperimentalUnsignedTypes
class TileAnimationListener(
    pInstruction: TileAnimationInstruction,
    pTilePanel: TilePanel,
    pTileAnimation: TileAnimation,
    pClearEnd: Boolean,
    pAnimatedImage: ImageView
) : Animation.AnimationListener {
    private val instruction = pInstruction
    private val tilePanel = pTilePanel
    private val tileAnimation = pTileAnimation
    private val clearEnd = pClearEnd
    private val animatedImage = pAnimatedImage

    override fun onAnimationStart(animation: Animation) {
        instruction.hiddenSquareIndexes.forEach { index ->
            val tile = tilePanel.getTile(index)
            tile?.piece?.visibility = View.GONE
        }
    }

    override fun onAnimationEnd(animation: Animation) {
        if (clearEnd) {
            instruction.hiddenSquareIndexes.forEach { index ->
                val tile = tilePanel.getTile(index)
                tile?.piece?.visibility = View.VISIBLE
            }

            android.os.Handler(Looper.getMainLooper()).post { tileAnimation.removeView(animatedImage) }
        }


    }

    override fun onAnimationRepeat(animation: Animation) {

    }

}
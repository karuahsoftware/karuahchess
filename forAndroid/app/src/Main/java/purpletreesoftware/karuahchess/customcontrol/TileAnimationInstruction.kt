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
import android.widget.ImageView

class TileAnimationInstruction (pContext: Context) {

    enum class AnimationTypeEnum(val value: Int) { Move(0), Take(1), Put(2), Fall(3), MoveFade(4)}
    var animationType: AnimationTypeEnum? = null

    private val _context: Context = pContext
    /**
     * Image
     */
    val imageData: ImageView = ImageView(_context)


    /**
     * Move from point
     */
    var moveFromX : Float = 0f
    var moveFromY : Float = 0f

    /**
     * Move to point
     */
    var moveToX : Float = 0f
    var moveToY : Float = 0f

    /**
     * Duration of the animation
     */
    var duration: Long = 1200

    /**
     * List of square indexes that should be hidden for the animation
     */
    val hiddenSquareIndexes : MutableList<Int> = mutableListOf()

}
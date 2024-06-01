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

import android.graphics.Canvas
import android.graphics.Point
import android.view.View
import android.widget.ImageView

class TileShadowBuilder(v: View, pImage: ImageView) : View.DragShadowBuilder(v)  {

    private val shadowImg = pImage

    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        val width: Int = shadowImg.width
        val height: Int = shadowImg.height
        size.set(width, height)
        touch.set(width / 2, height)
    }

    // Defines a callback that draws the drag shadow in a Canvas.
    override fun onDrawShadow(canvas: Canvas) {
        shadowImg.draw(canvas)
    }
}
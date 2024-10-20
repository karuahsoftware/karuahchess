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
import android.widget.FrameLayout
import android.widget.ImageView

@ExperimentalUnsignedTypes
class Tile: FrameLayout  {
    constructor(context: Context, pIndex: Int, pTilePanel: TilePanel) : super(context) {
        index = pIndex

        piece = ImageView(context)
        highlight = ImageView(context)
        highlightEdit = ImageView(context)
        checkIndicator = ImageView(context)
        panel = pTilePanel

        piece.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        highlight.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        highlightEdit.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        highlight.scaleType = ImageView.ScaleType.CENTER
        highlightEdit.scaleType = ImageView.ScaleType.CENTER
        checkIndicator.scaleType = ImageView.ScaleType.CENTER

        this.layoutParams = FrameLayout.LayoutParams(0,0)

        this.addView(checkIndicator)
        this.addView(highlight)
        this.addView(highlightEdit)
        this.addView(piece)

    }

    val index: Int
    var spin: Int = 0
    val piece: ImageView
    val highlight: ImageView
    val highlightEdit: ImageView
    val checkIndicator: ImageView
    val panel: TilePanel


}
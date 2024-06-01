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
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.CoordpanelBinding


class CoordPanel: ConstraintLayout {

    enum class AxisEnum(val value: Int) { X(0), Y(1)}



    private val rankFileList = ArrayList<TextView>(16)
    private val fileArray = arrayOf("a", "b", "c", "d", "e", "f", "g", "h")
    private val fileArrayRev = arrayOf("h", "g", "f", "e", "d", "c", "b", "a")
    private val rankArray = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
    private val rankArrayRev = arrayOf("8", "7", "6", "5", "4", "3", "2", "1")

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context) {
        // Inflate view
        CoordpanelBinding.inflate(LayoutInflater.from(context), this, true)

        var rankFileId = 1
        for (i in 0..7) {
            val rank = TextView(this.context)
            rank.id = rankFileId
            rank.text = rankArray[i]
            rank.gravity = Gravity.CENTER
            rank.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            rank.setTypeface(null, Typeface.NORMAL)
            rank.setPadding(5, 0, 0, 0)
            rankFileList.add(rank)
            rankFileId++
        }


        // File List

        for (i in 0..7) {
            val file = TextView(this.context)
            file.id = rankFileId
            file.text = fileArray[i]
            file.gravity = Gravity.CENTER
            file.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            file.setTypeface(null, Typeface.NORMAL)
            file.setPadding(0, 0, 0, 0)
            rankFileList.add(file)
            rankFileId++
        }
    }


    /**
     * Show or hide the control
     */
    fun show(pEnabled: Boolean) {
        if(pEnabled) {
            this.visibility = View.VISIBLE
        }
        else {
            this.visibility = View.GONE
        }
    }

    /**
     * Set coordinate labels based on board rotation
     */
    fun setCoordLabels(pRotation: Int) {

        for (index in 0..7) {
            if (pRotation == 0) {
                rankFileList[index].text = rankArrayRev[index]
                rankFileList[index + 8].text = fileArray[index]
            }
            else if (pRotation == 90) {
                rankFileList[index].text = fileArray[index]
                rankFileList[index + 8].text = rankArray[index]
            }
            else if (pRotation == 180) {
                rankFileList[index].text = rankArray[index]
                rankFileList[index + 8].text = fileArrayRev[index]
            }
            else if (pRotation == 270) {
                rankFileList[index].text = fileArrayRev[index]
                rankFileList[index + 8].text = rankArrayRev[index]
            }
        }
    }

    /**
     * Get coordinate list
     */
    private fun getCoordList(pAxis: AxisEnum): ArrayList<TextView> {

        val coordList = ArrayList<TextView>(8)

        // Rank List
        if (pAxis == AxisEnum.Y)  {
            for (index in 0..7) {
                coordList.add(rankFileList[index])
            }
        }
        else  {
            for (index in 8..15) {
                coordList.add(rankFileList[index])
            }
        }


        return coordList

    }



    /**
     * Draw layout.
     */
    fun draw(pTileSize: Int, pMargin: Int) {

        // Only run this function if board is visible
        if (this.visibility != View.VISIBLE) return

        // Set up tile list
        val coordLayout = findViewById<View>(R.id.coordLayout) as ConstraintLayout
        val cset = ConstraintSet()

        // Clear current layout
        coordLayout.removeAllViews()

        // Set layout size
        val lp = CoordinatorLayout.LayoutParams(pTileSize * 8 + pMargin, pTileSize * 8 + pMargin)
        this.layoutParams = lp

        // Connect up the layout
        val coordListX = getCoordList(AxisEnum.X)
        val coordListY = getCoordList(AxisEnum.Y)

        for(index in 0..7) {
            val coordY = coordListY[index]
            val coordX = coordListX[index]

            coordLayout.addView(coordY)
            cset.setHorizontalChainStyle(coordY.id, ConstraintSet.CHAIN_PACKED)
            cset.setVerticalChainStyle(coordY.id, ConstraintSet.CHAIN_PACKED)

            coordLayout.addView(coordX)
            cset.setHorizontalChainStyle(coordX.id, ConstraintSet.CHAIN_PACKED)
            cset.setVerticalChainStyle(coordX.id, ConstraintSet.CHAIN_PACKED)

            // Set constraints
            cset.constrainHeight(coordY.id, pTileSize)
            cset.setHorizontalChainStyle(coordY.id, ConstraintSet.CHAIN_PACKED)
            cset.setVerticalChainStyle(coordY.id, ConstraintSet.CHAIN_PACKED)

            cset.constrainWidth(coordX.id, pTileSize)
            cset.setHorizontalChainStyle(coordX.id, ConstraintSet.CHAIN_PACKED)
            cset.setVerticalChainStyle(coordX.id, ConstraintSet.CHAIN_PACKED)


            cset.connect(coordY.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)

            if (coordY.id == 1) {
                cset.connect(coordY.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            } else if (coordY.id in 2..8) {
                cset.connect(coordY.id, ConstraintSet.TOP, coordY.id - 1, ConstraintSet.BOTTOM, 0)
            }

            cset.connect(coordX.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
            if (coordX.id == 16) {
                cset.connect(coordX.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
            } else if (coordX.id in 9..15){
                cset.connect(coordX.id, ConstraintSet.END, coordX.id + 1, ConstraintSet.START, 0)
            }

        }


        cset.applyTo(coordLayout)

    }

}
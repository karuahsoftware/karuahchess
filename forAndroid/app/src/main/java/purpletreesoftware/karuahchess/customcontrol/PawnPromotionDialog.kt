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

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentPawnpromotionBinding

@ExperimentalUnsignedTypes
class PawnPromotionDialog : DialogFragment() {

    private var _promotionColour : Int  = 0
    private var _buttonSize : Float = 0f
    private var _pawnPromotionListener: OnPawnPromotionInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,0)
        retainInstance = true
        isCancelable = false


    }

    override fun onStart() {
        super.onStart()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val fragmentBinding = FragmentPawnpromotionBinding.inflate(inflater, container, false)
        val promotionLayout = fragmentBinding.pawnPromotionLinearLayout

        // Create the buttons
        createPieceButtons(_promotionColour, promotionLayout)

        return fragmentBinding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onDestroyView() {

        if (retainInstance) {
            dialog?.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    /**
     * Create piece buttons
     */
    private fun createPieceButtons(pColour : Int, pLayout: LinearLayout) {
        val promotionList : List<Char>

        pLayout.removeAllViews()

        if (pColour == Constants.BLACKPIECE) {
            promotionList = listOf('r', 'n', 'b', 'q')
        }
        else {
            promotionList = listOf('R', 'N', 'B', 'Q')
        }

        val params = LinearLayout.LayoutParams(_buttonSize.toInt(), _buttonSize.toInt())

        for (fen in promotionList)
        {
            val pieceBtn = ImageButton(this.context, null, R.style.ButtonCustomStyle)
            pieceBtn.layoutParams = params
            pieceBtn.setImageResource(getImage(fen))
            pieceBtn.adjustViewBounds = false
            pieceBtn.scaleType = ImageView.ScaleType.FIT_CENTER
            pieceBtn.setPadding(0,0,0,0)
            pieceBtn.tag = fen
            pieceBtn.setOnClickListener {
                _pawnPromotionListener?.onPawnPromotionClick(fen,this)
            }
            pLayout.addView(pieceBtn)
        }

    }


    /**
     * Gets tile image from [pSpin]
     */
    private fun getImage(pFen: Char): Int {

        return when (pFen) {
            'N' -> R.drawable.whiteknight
            'B' -> R.drawable.whitebishop
            'R' -> R.drawable.whiterook
            'Q' -> R.drawable.whitequeen
            'n' -> R.drawable.blackknight
            'b' -> R.drawable.blackbishop
            'r' -> R.drawable.blackrook
            'q' -> R.drawable.blackqueen
            else -> 0
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    /**
     * Sets the interaction listener
     */
    fun setPawnPromotionInteractionListener(pEventListener: OnPawnPromotionInteractionListener) {
        _pawnPromotionListener = pEventListener
    }

    interface OnPawnPromotionInteractionListener {
        fun onPawnPromotionClick(pFen: Char, pDialog : DialogFragment)
    }

    companion object {

        fun newInstance(pButtonSize: Float, pPromotionColour: Int): PawnPromotionDialog {
            val frag = PawnPromotionDialog()
            val args = Bundle()
            frag.arguments = args
            frag._promotionColour = pPromotionColour
            frag._buttonSize = pButtonSize

            return frag
        }
    }
}
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

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentPawnpromotionBinding
import purpletreesoftware.karuahchess.viewmodel.PawnPromotionViewModel

@ExperimentalUnsignedTypes
class PawnPromotion : DialogFragment() {
    private var _pawnPromotionListener: OnPawnPromotionInteractionListener? = null
    private lateinit var fragmentBinding: FragmentPawnpromotionBinding
    private lateinit var pawnPromotionVM: PawnPromotionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,0)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentBinding = FragmentPawnpromotionBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pawnPromotionVM = ViewModelProvider(requireActivity()).get(PawnPromotionViewModel::class.java)

        val promotionColour = pawnPromotionVM.promotionColour.value
        val buttonSize = pawnPromotionVM.buttonSize.value

        if (promotionColour != null && buttonSize != null) {
            createPieceButtons(promotionColour, buttonSize)
        }
    }

    /**
     * Create piece buttons
     */
    private fun createPieceButtons(pColour : Int, pButtonSize: Float) {
        val promotionList : List<Char>
        val promotionLayout = fragmentBinding.pawnPromotionLinearLayout
        promotionLayout.removeAllViews()

        if (pColour == Constants.BLACKPIECE) {
            promotionList = listOf('r', 'n', 'b', 'q')
        }
        else {
            promotionList = listOf('R', 'N', 'B', 'Q')
        }

        val params = LinearLayout.LayoutParams(pButtonSize.toInt(), pButtonSize.toInt())

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
            promotionLayout.addView(pieceBtn)
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

        fun newInstance(): PawnPromotion {
            val frag = PawnPromotion()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
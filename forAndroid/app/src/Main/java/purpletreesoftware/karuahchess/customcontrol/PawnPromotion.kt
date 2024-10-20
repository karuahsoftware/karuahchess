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
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentPawnpromotionBinding
import purpletreesoftware.karuahchess.viewmodel.PawnPromotionViewModel

@ExperimentalUnsignedTypes
class PawnPromotion : DialogFragment() {
    private var _binding: FragmentPawnpromotionBinding? = null
    private val binding get() = _binding!!
    private var _pawnPromotionListener: OnPawnPromotionInteractionListener? = null
    private lateinit var pawnPromotionVM: PawnPromotionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,0)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentPawnpromotionBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pawnPromotionVM = ViewModelProvider(requireActivity()).get(PawnPromotionViewModel::class.java)

        val promotionColour = pawnPromotionVM.promotionColour.value
        val buttonSize = pawnPromotionVM.buttonSize.value

        if (promotionColour != null && buttonSize != null) {
            setPieceButtons(buttonSize)
            if (promotionColour == Constants.BLACKPIECE) {
                binding.blackPieceLinearLayout.visibility = View.VISIBLE
                binding.whitePieceLinearLayout.visibility = View.GONE
            }
            else {
                binding.blackPieceLinearLayout.visibility = View.GONE
                binding.whitePieceLinearLayout.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Create piece buttons
     */
    private fun setPieceButtons(pButtonSize: Float) {

        // Set Button size
        val params = LinearLayout.LayoutParams(pButtonSize.toInt(), pButtonSize.toInt())
        params.setMargins(3,3,3,3)

        binding.blackRookImageButton.layoutParams = params
        binding.blackKnightImageButton.layoutParams = params
        binding.blackBishopImageButton.layoutParams = params
        binding.blackQueenImageButton.layoutParams = params

        binding.whiteRookImageButton.layoutParams = params
        binding.whiteKnightImageButton.layoutParams = params
        binding.whiteBishopImageButton.layoutParams = params
        binding.whiteQueenImageButton.layoutParams = params

        // Set button listeners
        // Black Pieces
        binding.blackRookImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('r',this)
        }

        binding.blackKnightImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('n',this)
        }

        binding.blackBishopImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('b',this)
        }

        binding.blackQueenImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('q',this)
        }

        // White Pieces
        binding.whiteRookImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('R',this)
        }

        binding.whiteKnightImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('N',this)
        }

        binding.whiteBishopImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('B',this)
        }

        binding.whiteQueenImageButton.setOnClickListener{
            _pawnPromotionListener?.onPawnPromotionClick('Q',this)
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
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

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentPieceedittoolBinding
import purpletreesoftware.karuahchess.viewmodel.PieceEditToolViewModel


@ExperimentalUnsignedTypes
class PieceEditTool : DialogFragment() {


    private var _pieceEditToolListener: OnPieceEditToolInteractionListener? = null
    private val _tileRect : Rect = Rect()
    private lateinit var pieceEditToolVM: PieceEditToolViewModel
    private lateinit var fragmentBinding: FragmentPieceedittoolBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        fragmentBinding = FragmentPieceedittoolBinding.inflate(inflater, container, false)

        return fragmentBinding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        pieceEditToolVM.tile.value?.panel?.setHighlightFull(0UL)
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pieceEditToolVM = ViewModelProvider(requireActivity()).get(PieceEditToolViewModel::class.java)
        val tile = pieceEditToolVM.tile.value
        val pieceEditToolColour = pieceEditToolVM.pieceEditToolColour.value

        if (tile != null && pieceEditToolColour != null) {
            // Get rectangle coordinates of tile (this is done as it is always the same no matter what the tile rotation is)
            tile.getGlobalVisibleRect(_tileRect)

            // Calculate buttons size
            val buttonSize = calcButtonSize(tile.height.toFloat())

            // Highlight square being modified
            tile.panel.setHighlightFull(Constants.BITMASK shr tile.index)

            // Create the buttons
            createButtonsEditMode(tile, buttonSize)
            createPieceButtons(tile, buttonSize, pieceEditToolColour)

            // Calculate position
            val totalButtonCount =
                fragmentBinding.toolSelectLinearLayout.childCount + fragmentBinding.pieceEditToolLinearLayout.childCount
            var posX: Float =
                (_tileRect.left + (_tileRect.width() / 2)) - totalButtonCount * buttonSize / 2
            if (posX < 0f) posX = 0f

            val posY: Float
            if (_tileRect.top - (_tileRect.height()) > _tileRect.height()) posY =
                _tileRect.top.toFloat() - (_tileRect.height().toFloat() * 1.07.toFloat())
            else posY = _tileRect.bottom.toFloat() - (_tileRect.height().toFloat() * 0.20.toFloat())

            val param: WindowManager.LayoutParams? = dialog?.window?.attributes
            if (param != null) {
                param.alpha = 1f
                param.horizontalMargin = 0f
                param.verticalMargin = 0f
                param.gravity = Gravity.TOP or Gravity.START
                param.x = posX.toInt()
                param.y = posY.toInt()

                dialog?.window?.attributes = param
            }

        }

    }


    /**
     * Create the edit buttons
     */
    private fun createButtonsEditMode(pTile: Tile, pButtonSize: Float) {

        // Button size
        val params = LinearLayout.LayoutParams(pButtonSize.toInt(), pButtonSize.toInt())
        params.setMargins(0,0,0,0)

        // Delete button
        val deleteBtn = ImageButton(this.context, null, R.style.ButtonCustomStyle)
        deleteBtn.layoutParams = params
        deleteBtn.setImageResource(R.drawable.ic_delete)
        deleteBtn.adjustViewBounds = false
        deleteBtn.scaleType = ImageView.ScaleType.FIT_CENTER
        deleteBtn.setPadding(0,0,0,0)
        deleteBtn.tag = ' '
        deleteBtn.setOnClickListener{
            _pieceEditToolListener?.onPieceEditToolClick(' ',pTile.index, this)
        }

        fragmentBinding.toolSelectLinearLayout.addView(deleteBtn)

        // Colour select button
        val colourSelectBtn = ImageButton(this.context, null, R.style.ButtonCustomStyle)
        colourSelectBtn.layoutParams = params
        colourSelectBtn.setImageResource(R.drawable.ic_chevron_right)
        colourSelectBtn.adjustViewBounds = false
        colourSelectBtn.scaleType = ImageView.ScaleType.FIT_CENTER
        colourSelectBtn.setPadding(0,0,0,0)
        colourSelectBtn.setOnClickListener{
            var pieceEditToolColour = pieceEditToolVM.pieceEditToolColour.value
            if (pieceEditToolColour != null) {
                pieceEditToolColour *= -1
                pieceEditToolVM.pieceEditToolColour.value = pieceEditToolColour
                createPieceButtons(pTile, pButtonSize, pieceEditToolColour,)
            }
        }
        fragmentBinding.toolSelectLinearLayout.addView(colourSelectBtn)

    }

    /**
     * Create piece buttons
     */
    private fun createPieceButtons(pTile: Tile, pButtonSize: Float, pColour : Int) {
        val pieceEditList : List<Char>

        // Set the layout variables
        fragmentBinding.pieceEditToolLinearLayout.removeAllViews()

        if (pColour == Constants.BLACKPIECE) {
            pieceEditList = listOf('p', 'r', 'n', 'b', 'q')
        }
        else {
            pieceEditList = listOf('P', 'R', 'N', 'B', 'Q')
        }

        val params = LinearLayout.LayoutParams(pButtonSize.toInt(), pButtonSize.toInt())
        params.setMargins(0,0,0,0)

        for (fen in pieceEditList)
        {
            val pieceBtn = ImageButton(this.context, null, R.style.ButtonCustomStyle)
            pieceBtn.layoutParams = params
            pieceBtn.setImageResource(getImage(fen))
            pieceBtn.adjustViewBounds = false
            pieceBtn.scaleType = ImageView.ScaleType.FIT_CENTER
            pieceBtn.setPadding(0,0,0,0)
            pieceBtn.tag = fen
            pieceBtn.setOnClickListener {
                _pieceEditToolListener?.onPieceEditToolClick(fen, pTile.index, this)
            }
            fragmentBinding.pieceEditToolLinearLayout.addView(pieceBtn)
        }

    }

    /**
     * Calculates the size of the button based on the tile size
     */
    private fun calcButtonSize(pTileSize : Float) : Float {
        return if (pTileSize > 90) {
            pTileSize * 0.85f
        } else {
            pTileSize
        }
    }

    /**
     * Gets tile image from [pSpin]
     */
    private fun getImage(pFen: Char): Int {

        return when (pFen) {
            'P' -> R.drawable.whitepawn
            'N' -> R.drawable.whiteknight
            'B' -> R.drawable.whitebishop
            'R' -> R.drawable.whiterook
            'Q' -> R.drawable.whitequeen
            'K' -> R.drawable.whiteking0
            'p' -> R.drawable.blackpawn
            'n' -> R.drawable.blackknight
            'b' -> R.drawable.blackbishop
            'r' -> R.drawable.blackrook
            'q' -> R.drawable.blackqueen
            'k' -> R.drawable.blackking0
            else -> 0
        }

    }

    /**
     * Sets the interaction listener
     */
    fun setPieceEditToolInteractionListener(pEventListener: OnPieceEditToolInteractionListener) {
        _pieceEditToolListener = pEventListener
    }

    interface OnPieceEditToolInteractionListener {
        fun onPieceEditToolClick(pFen: Char, pSqIndex: Int, pDialog : DialogFragment)
    }

    companion object {
        fun newInstance(): PieceEditTool {
            val frag = PieceEditTool()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
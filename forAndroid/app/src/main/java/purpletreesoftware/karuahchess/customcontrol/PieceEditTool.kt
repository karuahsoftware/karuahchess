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
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentPieceedittoolBinding


@ExperimentalUnsignedTypes
class PieceEditTool : DialogFragment() {

    private lateinit var _tile : Tile
    private var _buttonSize : Float = 0f
    private var _pieceEditToolListener: OnPieceEditToolInteractionListener? = null
    private val _tileRect : Rect = Rect()
    private lateinit var _toolSelectLinearLayout : LinearLayout
    private lateinit var _pieceEditToolLinearLayout : LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
        retainInstance = true

        // Get rectangle coordinates of tile (this is done as it is always the same no matter what the tile rotation is)
        _tile.getGlobalVisibleRect(_tileRect)

        // Calculate buttons size
        _buttonSize = calcButtonSize(_tile.height.toFloat())

        // Highlight square being modified
        _tile.panel.setHighlightFull(Constants.BITMASK shr _tile.index)

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
        val fragmentBinding = FragmentPieceedittoolBinding.inflate(inflater, container, false)

        // Set the layout variables
        _toolSelectLinearLayout = fragmentBinding.toolSelectLinearLayout
        _pieceEditToolLinearLayout = fragmentBinding.pieceEditToolLinearLayout

        // Create the buttons
        createButtonsEditMode()
        createPieceButtons(pieceEditToolColour)

        // Calculate position
        val totalButtonCount = _toolSelectLinearLayout.childCount + _pieceEditToolLinearLayout.childCount
        var posX : Float = (_tileRect.left + (_tileRect.width() / 2)) - totalButtonCount * _buttonSize / 2
        if (posX < 0f) posX = 0f

        val posY : Float
        if(_tileRect.top - _buttonSize > 0) posY = _tileRect.top - _buttonSize
        else posY = _tileRect.top + _buttonSize

        val param: WindowManager.LayoutParams? = dialog?.window?.attributes
        if(param != null) {
            param.alpha = 1f
            param.horizontalMargin = 0f
            param.verticalMargin = 0f
            param.gravity = Gravity.TOP or Gravity.START
            param.x = posX.toInt()
            param.y = posY.toInt()

            dialog?.window?.attributes = param
        }


        return fragmentBinding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        _tile.panel.setHighlightFull(0UL)
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

    }


    override fun onDestroyView() {
        if (retainInstance) {
            dialog?.setDismissMessage(null)
        }

        super.onDestroyView()
    }


    /**
     * Create the edit buttons
     */
    private fun createButtonsEditMode() {

        // Button size
        val params = LinearLayout.LayoutParams(_buttonSize.toInt(), _buttonSize.toInt())
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
            _pieceEditToolListener?.onPieceEditToolClick(' ',_tile.index, this)
        }

        _toolSelectLinearLayout.addView(deleteBtn)

        // Colour select button
        val colourSelectBtn = ImageButton(this.context, null, R.style.ButtonCustomStyle)
        colourSelectBtn.layoutParams = params
        colourSelectBtn.setImageResource(R.drawable.ic_chevron_right)
        colourSelectBtn.adjustViewBounds = false
        colourSelectBtn.scaleType = ImageView.ScaleType.FIT_CENTER
        colourSelectBtn.setPadding(0,0,0,0)
        colourSelectBtn.setOnClickListener{
            pieceEditToolColour *= -1
            createPieceButtons(pieceEditToolColour)
        }
        _toolSelectLinearLayout.addView(colourSelectBtn)


    }

    /**
     * Create piece buttons
     */
    private fun createPieceButtons(pColour : Int) {
        val pieceEditList : List<Char>

        _pieceEditToolLinearLayout.removeAllViews()

        if (pColour == Constants.BLACKPIECE) {
            pieceEditList = listOf('p', 'r', 'n', 'b', 'q')
        }
        else {
            pieceEditList = listOf('P', 'R', 'N', 'B', 'Q')
        }

        val params = LinearLayout.LayoutParams(_buttonSize.toInt(), _buttonSize.toInt())
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
                _pieceEditToolListener?.onPieceEditToolClick(fen,_tile.index, this)
            }
            _pieceEditToolLinearLayout.addView(pieceBtn)
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

        var pieceEditToolColour: Int = Constants.WHITEPIECE


        fun newInstance(pTile: Tile): PieceEditTool {
            val frag = PieceEditTool()
            val args = Bundle()
            frag.arguments = args
            frag._tile =  pTile

            if (pTile.spin > 0) pieceEditToolColour = Constants.WHITEPIECE
            else if (pTile.spin < 0) pieceEditToolColour = Constants.BLACKPIECE

            return frag
        }
    }
}
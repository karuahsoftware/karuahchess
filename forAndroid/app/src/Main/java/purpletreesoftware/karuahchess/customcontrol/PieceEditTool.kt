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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.databinding.FragmentPieceedittoolBinding

class PieceEditTool : DialogFragment() {

    private var _binding: FragmentPieceedittoolBinding? = null
    private val binding get() = _binding!!
    private val tileSize get() = requireArguments().getInt("pTileSize")


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val fragmentBinding = FragmentPieceedittoolBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set Button size
        val params = LinearLayout.LayoutParams(tileSize.toFloat().toInt(), tileSize.toFloat().toInt())
        params.setMargins(3,3,3,3)

        binding.blackPawnImageButton.layoutParams = params
        binding.blackRookImageButton.layoutParams = params
        binding.blackKnightImageButton.layoutParams = params
        binding.blackBishopImageButton.layoutParams = params
        binding.blackQueenImageButton.layoutParams = params

        binding.whitePawnImageButton.layoutParams = params
        binding.whiteRookImageButton.layoutParams = params
        binding.whiteKnightImageButton.layoutParams = params
        binding.whiteBishopImageButton.layoutParams = params
        binding.whiteQueenImageButton.layoutParams = params

        val mainActivity = activity as MainActivity

        // Set button listeners
        // Black Pieces
        binding.blackPawnImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('p')
            this.dismiss()
        }

        binding.blackRookImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('r')
            this.dismiss()
        }

        binding.blackKnightImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('n')
            this.dismiss()
        }

        binding.blackBishopImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('b')
            this.dismiss()
        }

        binding.blackQueenImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('q')
            this.dismiss()
        }

        // White Pieces
        binding.whitePawnImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('P')
            this.dismiss()
        }

        binding.whiteRookImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('R')
            this.dismiss()
        }

        binding.whiteKnightImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('N')
            this.dismiss()
        }

        binding.whiteBishopImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('B')
            this.dismiss()
        }

        binding.whiteQueenImageButton.setOnClickListener{
            mainActivity.editToolUpdateSelectedTiles('Q')
            this.dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(pTileSize: Int): PieceEditTool {
            val frag = PieceEditTool()
            val args = Bundle()
            args.putInt("pTileSize", pTileSize)
            frag.arguments = args
            return frag
        }
    }
}
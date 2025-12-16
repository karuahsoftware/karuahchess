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
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.databinding.MovenavigatorpgnBinding

@ExperimentalUnsignedTypes
class MoveNavigatorPGN(pActivityID: Int, pPGN: String) : DialogFragment() {

    private var _binding: MovenavigatorpgnBinding? = null
    private val binding get() = _binding!!
    private val activityID = pActivityID
    private val pgn = pPGN

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val fragmentBinding = MovenavigatorpgnBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onDismiss(dialog: DialogInterface) {

        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        binding.pgnTextArea.text = pgn


        // Connect button events
        binding.closeButton.setOnClickListener { dismiss() }
        binding.copyButton.setOnClickListener {
            val text = binding.pgnTextArea.text?.toString().orEmpty()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("PGN", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    companion object {

        // A constructor containing pActivityID is in the ActivityFragmentFactory

        fun newInstance(pActivityID: Int, pPGN: String): MoveNavigatorPGN {
            val frag = MoveNavigatorPGN(pActivityID, pPGN)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.databinding.FragmentAboutBinding
import purpletreesoftware.karuahchess.R



class About : DialogFragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var versionStr: String

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val fragmentBinding = FragmentAboutBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        binding.versionText.text = "Version: $versionStr \n"

        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }

    }


    override fun onDestroyView() {
        if (retainInstance) {
            dialog?.setDismissMessage(null)
        }

        super.onDestroyView()

        _binding = null
    }




    companion object {
        fun newInstance(pVersionStr: String): About {
            val frag = About()
            val args = Bundle()
            frag.arguments = args
            frag.versionStr = pVersionStr
            return frag
        }
    }
}
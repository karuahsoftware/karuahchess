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

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import android.view.*
import androidx.core.content.ContextCompat
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.FragmentVoicepermissionBinding

@ExperimentalUnsignedTypes
class VoicePermission : DialogFragment() {
    private var _binding: FragmentVoicepermissionBinding? = null
    private val binding get() = _binding!!

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
        val fragmentBinding = FragmentVoicepermissionBinding.inflate(inflater, container, false)
        _binding = fragmentBinding

        return fragmentBinding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val theContext: Context? = context

        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }
        if(theContext != null) {
            binding.permissionButton.setOnClickListener { changePermission(theContext) }
        }
    }


    override fun onDestroyView() {
        if (retainInstance) {
            dialog?.setDismissMessage(null)
        }

        super.onDestroyView()

        _binding = null
    }

    /**
     * Check for required permission
     */
    fun hasPermission(pContext: Context) : Boolean {
        return ContextCompat.checkSelfPermission(pContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Starts the application details settings so app permission can be changed by user
     */
    private fun changePermission(pContext: Context) {
        dismiss()

        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + pContext.getPackageName())
        )
        ContextCompat.startActivity(pContext, intent, null)
    }



    companion object {
        fun newInstance(): VoicePermission {
            val frag = VoicePermission()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
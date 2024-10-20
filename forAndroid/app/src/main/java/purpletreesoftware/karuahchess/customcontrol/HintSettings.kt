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
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.databinding.FragmentHintsettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.ParamHint
import purpletreesoftware.karuahchess.model.parameterobj.ParamHintMove


@ExperimentalUnsignedTypes
class HintSettings(pActivityID: Int) : DialogFragment() {

    private var _binding: FragmentHintsettingsBinding? = null
    private val binding get() = _binding!!
    private val activityID = pActivityID

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
        val fragmentBinding = FragmentHintsettingsBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        save()
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hintCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamHint::class.java).enabled

        binding.hintMoveCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamHintMove::class.java).enabled

        // Done button listener
        binding.doneButton.setOnClickListener { dismiss() }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Saves form values
     */
    private fun save(){
        val mainActivity = activity as MainActivity

        val hint = ParameterDataService.getInstance(activityID).get(ParamHint::class.java)
        if (hint.enabled != binding.hintCheckBox.isChecked) {
            hint.enabled = binding.hintCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(hint)
            mainActivity.setHint()
        }

        val hintMove = ParameterDataService.getInstance(activityID).get(ParamHintMove::class.java)
        if (hintMove.enabled != binding.hintMoveCheckBox.isChecked) {
            hintMove.enabled = binding.hintMoveCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(hintMove)
        }

    }


    companion object {

        // A constructor containing pActivityID is in the ActivityFragmentFactory

        fun newInstance(pActivityID: Int): HintSettings {
            val frag = HintSettings(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
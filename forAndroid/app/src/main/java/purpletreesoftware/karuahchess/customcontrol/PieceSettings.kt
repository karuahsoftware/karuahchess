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
import purpletreesoftware.karuahchess.databinding.FragmentPiecesettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.ParamComputerMoveFirst
import purpletreesoftware.karuahchess.model.parameterobj.ParamMoveSpeed
import purpletreesoftware.karuahchess.model.parameterobj.ParamPromoteAuto

class PieceSettings(pActivityID: Int) : DialogFragment() {

    private var _binding: FragmentPiecesettingsBinding? = null
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
        val fragmentBinding = FragmentPiecesettingsBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        save()
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial values
        val moveSpeed = ParameterDataService.getInstance(activityID).get(ParamMoveSpeed::class.java).speed.toFloat()
        if (moveSpeed in binding.moveSpeedSlider.valueFrom .. binding.moveSpeedSlider.valueTo) {
            binding.moveSpeedSlider.value = moveSpeed
        }

        binding.promoteAutoCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamPromoteAuto::class.java).enabled


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

        // Save the move speed parameter
        val moveSpeed = ParameterDataService.getInstance(activityID).get(ParamMoveSpeed::class.java)
        if (moveSpeed.speed != binding.moveSpeedSlider.value.toInt()) {
            moveSpeed.speed = binding.moveSpeedSlider.value.toInt()
            ParameterDataService.getInstance(activityID).set(moveSpeed)
        }

        val promoteAuto = ParameterDataService.getInstance(activityID).get(ParamPromoteAuto::class.java)
        if (promoteAuto.enabled != binding.promoteAutoCheckBox.isChecked) {
            promoteAuto.enabled = binding.promoteAutoCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(promoteAuto)
        }

    }


    companion object {
        fun newInstance(pActivityID: Int): PieceSettings {
            val frag = PieceSettings(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
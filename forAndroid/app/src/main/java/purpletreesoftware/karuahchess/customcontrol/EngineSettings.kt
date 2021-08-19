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


import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.common.InputFilterRange
import purpletreesoftware.karuahchess.databinding.FragmentEnginesettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.*
import java.text.NumberFormat


@ExperimentalUnsignedTypes
class EngineSettings : DialogFragment() {

    private var _binding: FragmentEnginesettingsBinding? = null
    private val binding get() = _binding!!

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
        val fragmentBinding = FragmentEnginesettingsBinding.inflate(inflater, container, false)
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
        binding.computerPlayerEnabledCheckBox.isChecked = ParameterDataService.get(ParamComputerPlayer::class.java).enabled
        binding.computerPlayerEnabledCheckBox.setOnClickListener { setControlState() }

        binding.computerMoveFirstCheckBox.isChecked = ParameterDataService.get(ParamComputerMoveFirst::class.java).enabled

        binding.randomiseFirstMoveCheckBox.isChecked = ParameterDataService.get(ParamRandomiseFirstMove::class.java).enabled

        binding.levelAutoCheckBox.isChecked = ParameterDataService.get(ParamLevelAuto::class.java).enabled

        binding.computerAdvancedSettingsCheckBox.isChecked = ParameterDataService.get(ParamLimitAdvanced::class.java).enabled
        binding.computerAdvancedSettingsCheckBox.setOnClickListener { setControlState() }


        // Set elo spinner
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.strengthArrayLabel
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.elospinner.adapter = adapter

        // Set the spinner position
        val eloIndex = Constants.eloarray.indexOf(ParameterDataService.get(ParamLimitEngineStrengthELO::class.java).eloRating)
        if (eloIndex > -1) {
            binding.elospinner.setSelection(eloIndex)
        }

        // Advanced settings
        binding.depthLimitValueText.text = getValueZeroOff(binding.depthLimitSlider.value.toInt())
        binding.depthLimitSlider.addOnChangeListener { slider, value, fromUser ->
            binding.depthLimitValueText.text = getValueZeroOff(value.toInt())
        }
        binding.depthLimitSlider.value = ParameterDataService.get(ParamLimitDepth::class.java).depth.toFloat()

        binding.nodeLimitValueEditText.error = null
        binding.nodeLimitValueEditText.filters = arrayOf<InputFilter>(
            InputFilterRange(0, 2000000000),
            InputFilter.LengthFilter(10))
        binding.nodeLimitValueEditText.doAfterTextChanged { text ->
            val inputNumber: Int = text.toString().toIntOrNull() ?: 0
            if (inputNumber < 10 || inputNumber > 2000000000) {
                val maxStr = NumberFormat.getInstance().format(2000000000)
                binding.nodeLimitValueEditText.error = "Value should be between 10 and $maxStr."
            }
        }

        val nodes = ParameterDataService.get(ParamLimitNodes::class.java).nodes
        // Leave blank if zero
        if (nodes in 1..2000000000) {
            binding.nodeLimitValueEditText.setText(nodes.toString())
        }

        binding.moveDurationLimitValueEditText.error = null
        binding.moveDurationLimitValueEditText.filters = arrayOf<InputFilter>(
            InputFilterRange(0, 600000),
            InputFilter.LengthFilter(6)
        )
        binding.moveDurationLimitValueEditText.doAfterTextChanged { text ->
            val inputNumber: Int = text.toString().toIntOrNull() ?: 0
            if (inputNumber < 0 || inputNumber > 600000) {
                val maxStr = NumberFormat.getInstance().format(600000)
                binding.moveDurationLimitValueEditText.error = "Value should be between 0 and $maxStr."
            }
        }
        val moveDuration = ParameterDataService.get(ParamLimitMoveDuration::class.java).moveDurationMS
        // Leave blank if zero
        if(moveDuration in 1..600000) {
            binding.moveDurationLimitValueEditText.setText(moveDuration.toString())
        }

        binding.threadsLimitValueText.text = getValueZeroOff(binding.threadsLimitSlider.value.toInt())
        binding.threadsLimitSlider.addOnChangeListener { slider, value, fromUser ->
            binding.threadsLimitValueText.text = getValueZeroOff(value.toInt())
        }

        binding.threadsLimitSlider.valueTo = if (Runtime.getRuntime().availableProcessors() > 1) (Runtime.getRuntime().availableProcessors() - 1).toFloat() else 1.toFloat()
        binding.threadsLimitSlider.value = ParameterDataService.get(ParamLimitThreads::class.java).threads.toFloat()



        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }
        binding.defaultButton.setOnClickListener { resetToDefault() }
        binding.stopSearchButton.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.stopMoveJob()
            dismiss()
        }

        // Set initial state of controls
        setControlState()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Saves form values
      */
    private fun save(){

        val computerPlayer = ParameterDataService.get(ParamComputerPlayer::class.java)
        if (computerPlayer.enabled != binding.computerPlayerEnabledCheckBox.isChecked) {
            computerPlayer.enabled = binding.computerPlayerEnabledCheckBox.isChecked
            ParameterDataService.set(computerPlayer)
        }

        val computerMoveFirst = ParameterDataService.get(ParamComputerMoveFirst::class.java)
        if (computerMoveFirst.enabled != binding.computerMoveFirstCheckBox.isChecked) {
            computerMoveFirst.enabled = binding.computerMoveFirstCheckBox.isChecked
            ParameterDataService.set(computerMoveFirst)
        }

        val randomiseFirstMove = ParameterDataService.get(ParamRandomiseFirstMove::class.java)
        if (randomiseFirstMove.enabled != binding.randomiseFirstMoveCheckBox.isChecked) {
            randomiseFirstMove.enabled = binding.randomiseFirstMoveCheckBox.isChecked
            ParameterDataService.set(randomiseFirstMove)
        }

        val levelAuto = ParameterDataService.get(ParamLevelAuto::class.java)
        if (levelAuto.enabled != binding.levelAutoCheckBox.isChecked) {
            levelAuto.enabled = binding.levelAutoCheckBox.isChecked
            ParameterDataService.set(levelAuto)
        }

        val limitEngineStrengthELO = ParameterDataService.get(ParamLimitEngineStrengthELO::class.java)
        if (limitEngineStrengthELO.eloRating != Constants.eloarray[binding.elospinner.selectedItemPosition]) {
            limitEngineStrengthELO.eloRating =
                Constants.eloarray[binding.elospinner.selectedItemPosition]
            ParameterDataService.set(limitEngineStrengthELO)
        }

        val computerAdvancedSettings = ParameterDataService.get(ParamLimitAdvanced::class.java)
        if (computerAdvancedSettings.enabled != binding.computerAdvancedSettingsCheckBox.isChecked) {
            computerAdvancedSettings.enabled = binding.computerAdvancedSettingsCheckBox.isChecked
            ParameterDataService.set(computerAdvancedSettings)
        }

        val limitDepth = ParameterDataService.get(ParamLimitDepth::class.java)
        if (limitDepth.depth != binding.depthLimitSlider.value.toInt()) {
            limitDepth.depth = binding.depthLimitSlider.value.toInt()
            ParameterDataService.set(limitDepth)
        }

        val limitNodes = ParameterDataService.get(ParamLimitNodes::class.java)
        var limitNodesValue: Int = binding.nodeLimitValueEditText.text.toString().toIntOrNull() ?: 0
        if (limitNodesValue < 10 || limitNodesValue > 2000000000) limitNodesValue = 10 // Ensure within limits
        if (limitNodes.nodes != limitNodesValue) {
            limitNodes.nodes = limitNodesValue
            ParameterDataService.set(limitNodes)
        }

        val limitMoveDuration = ParameterDataService.get(ParamLimitMoveDuration::class.java)
        val limitMoveDurationValue: Int = binding.moveDurationLimitValueEditText.text.toString().toIntOrNull() ?: 0
        if (limitMoveDuration.moveDurationMS != limitMoveDurationValue) {
            limitMoveDuration.moveDurationMS = limitMoveDurationValue
            ParameterDataService.set(limitMoveDuration)
        }

        val limitThreads = ParameterDataService.get(ParamLimitThreads::class.java)
        if (limitThreads.threads != binding.threadsLimitSlider.value.toInt()) {
            limitThreads.threads = binding.threadsLimitSlider.value.toInt()
            ParameterDataService.set(limitThreads)
        }

        // Refresh data in main activity after save
        val mainActivity = activity as MainActivity
        mainActivity.refreshEngineSettingsLevelIndicator()

    }

    /**
     * Enables and disables controls depending on options set
     */
    private fun setControlState() {
        var advanced: Boolean = false
        var advancedOpacity: Float = 1.0F

        val computerPlayer = binding.computerPlayerEnabledCheckBox.isChecked
        if (computerPlayer) {
            binding.computerMoveFirstCheckBox.isEnabled = true
            binding.randomiseFirstMoveCheckBox.isEnabled = true
            binding.levelAutoCheckBox.isEnabled = true
            binding.eloStrengthTitleText.isEnabled = true
            binding.elospinner.isEnabled = true
            binding.computerAdvancedSettingsCheckBox.isEnabled = true
            advanced = binding.computerAdvancedSettingsCheckBox.isEnabled && binding.computerAdvancedSettingsCheckBox.isChecked == true
            advancedOpacity = if (advanced) 1.0F else 0.38F
        } else {
            binding.computerMoveFirstCheckBox.isEnabled = false
            binding.randomiseFirstMoveCheckBox.isEnabled = false
            binding.levelAutoCheckBox.isEnabled = false
            binding.eloStrengthTitleText.isEnabled = false
            binding.elospinner.isEnabled = false
            binding.computerAdvancedSettingsCheckBox.isEnabled = false
            advanced = false
            advancedOpacity = 0.38F
        }

        // Advanced Settings
        binding.depthLimitSlider.isEnabled = advanced
        binding.depthLimitSlider.alpha = advancedOpacity
        binding.depthLimitText.isEnabled = advanced
        binding.depthLimitValueText.isEnabled = advanced
        binding.nodeLimitValueEditText.isEnabled = advanced
        binding.nodeLimitText.isEnabled = advanced
        binding.nodeLimitValueEditText.isEnabled = advanced
        binding.moveDurationLimitValueEditText.isEnabled = advanced
        binding.moveDurationLimitText.isEnabled = advanced
        binding.moveDurationLimitValueEditText.isEnabled = advanced
        binding.threadsLimitSlider.isEnabled = advanced
        binding.threadsLimitSlider.alpha = advancedOpacity
        binding.threadsLimitText.isEnabled = advanced
        binding.threadsLimitValueText.isEnabled = advanced

    }

    /**
     * Reset to default values
     */
    private fun resetToDefault() {
        binding.computerPlayerEnabledCheckBox.isChecked = ParamComputerPlayer().enabled
        binding.computerMoveFirstCheckBox.isChecked = ParamComputerMoveFirst().enabled
        binding.randomiseFirstMoveCheckBox.isChecked = ParamRandomiseFirstMove().enabled
        binding.levelAutoCheckBox.isChecked = ParamLevelAuto().enabled
        binding.elospinner.setSelection(Constants.eloarray.indexOf(ParamLimitEngineStrengthELO().eloRating))
        binding.computerAdvancedSettingsCheckBox.isChecked = ParamLimitAdvanced().enabled
        binding.depthLimitSlider.value = ParamLimitDepth().depth.toFloat()
        binding.nodeLimitValueEditText.setText(if (ParamLimitNodes().nodes > 0)  ParamLimitNodes().nodes.toString() else "")
        binding.nodeLimitValueEditText.error = null
        binding.moveDurationLimitValueEditText.setText(if (ParamLimitMoveDuration().moveDurationMS > 0) ParamLimitMoveDuration().moveDurationMS.toString() else "")
        binding.moveDurationLimitValueEditText.error = null
        binding.threadsLimitSlider.value = ParamLimitThreads().threads.toFloat()

        setControlState()
    }

    /**
     * Gets the value as string, or return off if zero
     */
    private fun getValueZeroOff(pValue: Int): String {
        return if (pValue > 0) NumberFormat.getIntegerInstance().format(pValue) else "off"

    }

    companion object {
        fun newInstance(): EngineSettings {
            val frag = EngineSettings()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
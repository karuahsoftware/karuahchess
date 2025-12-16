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
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.common.InputFilterRange
import purpletreesoftware.karuahchess.common.Strength
import purpletreesoftware.karuahchess.databinding.FragmentEnginesettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.*
import java.text.NumberFormat


@ExperimentalUnsignedTypes
class EngineSettings(pActivityID: Int) : DialogFragment() {

    private var _binding: FragmentEnginesettingsBinding? = null
    private val binding get() = _binding!!
    private val activityID = pActivityID
    private var computerMoveFirstDialogValue: Boolean = false

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

        val mainActivity = activity as MainActivity
        // Set initial values
        binding.computerPlayerEnabledCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java).enabled
        binding.computerPlayerEnabledCheckBox.setOnClickListener { setControlState() }

        computerMoveFirstDialogValue = ParameterDataService.getInstance(activityID).get(ParamComputerMoveFirst::class.java).enabled
        setComputerMoveFirstButton()
        binding.computerMoveFirstButton.setOnClickListener {
            computerMoveFirstDialogValue = !computerMoveFirstDialogValue
            setComputerMoveFirstButton()
        }

        binding.randomiseFirstMoveCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamRandomiseFirstMove::class.java).enabled
        binding.levelAutoCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamLevelAuto::class.java).enabled
        binding.computerAdvancedSettingsCheckBox.isChecked = ParameterDataService.getInstance(activityID).get(ParamLimitAdvanced::class.java).enabled
        binding.computerAdvancedSettingsCheckBox.setOnClickListener { setControlState() }


        // Set skill level spinner
        val adapter: ArrayAdapter<Strength> = ArrayAdapter<Strength>(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.strengthList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.skillLevelSpinner.adapter = adapter

        // Set the spinner position
        val skillLevel = ParameterDataService.getInstance(activityID).get(ParamLimitSkillLevel::class.java).level
        if (skillLevel in 0..Constants.strengthList.lastIndex) {
            binding.skillLevelSpinner.setSelection(skillLevel)
        }

        // Advanced settings
        binding.depthLimitValueText.text = getValueZeroOff(binding.depthLimitSlider.value.toInt())
        binding.depthLimitSlider.addOnChangeListener { slider, value, fromUser ->
            binding.depthLimitValueText.text = getValueZeroOff(value.toInt())
        }
        val limitDepth = ParameterDataService.getInstance(activityID).get(ParamLimitDepth::class.java).depth.toFloat()
        if (limitDepth in binding.depthLimitSlider.valueFrom .. binding.depthLimitSlider.valueTo) {
            binding.depthLimitSlider.value = limitDepth
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
        val moveDuration = ParameterDataService.getInstance(activityID).get(ParamLimitMoveDuration::class.java).moveDurationMS
        // Leave blank if zero
        if(moveDuration in 1..600000) {
            binding.moveDurationLimitValueEditText.setText(moveDuration.toString())
        }

        binding.threadsLimitValueText.text = getValueZeroOff(binding.threadsLimitSlider.value.toInt())
        binding.threadsLimitSlider.addOnChangeListener { slider, value, fromUser ->
            binding.threadsLimitValueText.text = getValueZeroOff(value.toInt())
        }

        binding.threadsLimitSlider.valueTo = if (Runtime.getRuntime().availableProcessors() > 1) (Runtime.getRuntime().availableProcessors()).toFloat() else 1.toFloat()
        val threadLimit = ParameterDataService.getInstance(activityID).get(ParamLimitThreads::class.java).threads.toFloat()
        if (threadLimit in binding.threadsLimitSlider.valueFrom .. binding.threadsLimitSlider.valueTo) {
            binding.threadsLimitSlider.value = threadLimit
        }



        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }
        binding.defaultButton.setOnClickListener { resetToDefault() }
        binding.stopSearchButton.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.stopSearchJob()
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

        val computerPlayer = ParameterDataService.getInstance(activityID).get(ParamComputerPlayer::class.java)
        if (computerPlayer.enabled != binding.computerPlayerEnabledCheckBox.isChecked) {
            computerPlayer.enabled = binding.computerPlayerEnabledCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(computerPlayer)
        }

        val computerMoveFirst = ParameterDataService.getInstance(activityID).get(ParamComputerMoveFirst::class.java)
        if (computerMoveFirst.enabled != computerMoveFirstDialogValue) {
            computerMoveFirst.enabled = computerMoveFirstDialogValue
            ParameterDataService.getInstance(activityID).set(computerMoveFirst)
        }

        val randomiseFirstMove = ParameterDataService.getInstance(activityID).get(ParamRandomiseFirstMove::class.java)
        if (randomiseFirstMove.enabled != binding.randomiseFirstMoveCheckBox.isChecked) {
            randomiseFirstMove.enabled = binding.randomiseFirstMoveCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(randomiseFirstMove)
        }

        val levelAuto = ParameterDataService.getInstance(activityID).get(ParamLevelAuto::class.java)
        if (levelAuto.enabled != binding.levelAutoCheckBox.isChecked) {
            levelAuto.enabled = binding.levelAutoCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(levelAuto)
        }

        val limitSkillLevel = ParameterDataService.getInstance(activityID).get(ParamLimitSkillLevel::class.java)
        if (limitSkillLevel.level != binding.skillLevelSpinner.selectedItemPosition) {
            limitSkillLevel.level = binding.skillLevelSpinner.selectedItemPosition
            ParameterDataService.getInstance(activityID).set(limitSkillLevel)
        }

        val computerAdvancedSettings = ParameterDataService.getInstance(activityID).get(ParamLimitAdvanced::class.java)
        if (computerAdvancedSettings.enabled != binding.computerAdvancedSettingsCheckBox.isChecked) {
            computerAdvancedSettings.enabled = binding.computerAdvancedSettingsCheckBox.isChecked
            ParameterDataService.getInstance(activityID).set(computerAdvancedSettings)
        }

        val limitDepth = ParameterDataService.getInstance(activityID).get(ParamLimitDepth::class.java)
        if (limitDepth.depth != binding.depthLimitSlider.value.toInt()) {
            limitDepth.depth = binding.depthLimitSlider.value.toInt()
            ParameterDataService.getInstance(activityID).set(limitDepth)
        }

        val limitMoveDuration = ParameterDataService.getInstance(activityID).get(ParamLimitMoveDuration::class.java)
        val limitMoveDurationValue: Int = binding.moveDurationLimitValueEditText.text.toString().toIntOrNull() ?: 0
        if (limitMoveDuration.moveDurationMS != limitMoveDurationValue) {
            limitMoveDuration.moveDurationMS = limitMoveDurationValue
            ParameterDataService.getInstance(activityID).set(limitMoveDuration)
        }

        val limitThreads = ParameterDataService.getInstance(activityID).get(ParamLimitThreads::class.java)
        if (limitThreads.threads != binding.threadsLimitSlider.value.toInt()) {
            limitThreads.threads = binding.threadsLimitSlider.value.toInt()
            ParameterDataService.getInstance(activityID).set(limitThreads)
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
            binding.randomiseFirstMoveCheckBox.isEnabled = true
            binding.levelAutoCheckBox.isEnabled = true
            binding.skillLevelTitleText.isEnabled = true
            binding.skillLevelSpinner.isEnabled = true
            binding.computerAdvancedSettingsCheckBox.isEnabled = true
            advanced = binding.computerAdvancedSettingsCheckBox.isEnabled && binding.computerAdvancedSettingsCheckBox.isChecked == true
            advancedOpacity = if (advanced) 1.0F else 0.38F
            binding.computerColourTitleText.isEnabled = true
            binding.computerMoveFirstButton.isEnabled = true
            binding.computerMoveFirstButton.alpha = 1.0F
        } else {
            binding.randomiseFirstMoveCheckBox.isEnabled = false
            binding.levelAutoCheckBox.isEnabled = false
            binding.skillLevelTitleText.isEnabled = false
            binding.skillLevelSpinner.isEnabled = false
            binding.computerAdvancedSettingsCheckBox.isEnabled = false
            advanced = false
            advancedOpacity = 0.38F
            binding.computerColourTitleText.isEnabled = false
            binding.computerMoveFirstButton.isEnabled = false
            binding.computerMoveFirstButton.alpha = 0.38F
        }

        // Advanced Settings
        binding.depthLimitSlider.isEnabled = advanced
        binding.depthLimitSlider.alpha = advancedOpacity
        binding.depthLimitText.isEnabled = advanced
        binding.depthLimitValueText.isEnabled = advanced
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

        computerMoveFirstDialogValue = ParamComputerMoveFirst().enabled

        binding.computerPlayerEnabledCheckBox.isChecked = ParamComputerPlayer().enabled
        binding.randomiseFirstMoveCheckBox.isChecked = ParamRandomiseFirstMove().enabled
        binding.levelAutoCheckBox.isChecked = ParamLevelAuto().enabled
        binding.skillLevelSpinner.setSelection(ParamLimitSkillLevel().level)
        binding.computerAdvancedSettingsCheckBox.isChecked = ParamLimitAdvanced().enabled
        binding.depthLimitSlider.value = ParamLimitDepth().depth.toFloat()
        binding.moveDurationLimitValueEditText.setText(if (ParamLimitMoveDuration().moveDurationMS > 0) ParamLimitMoveDuration().moveDurationMS.toString() else "")
        binding.moveDurationLimitValueEditText.error = null
        binding.threadsLimitSlider.value = ParamLimitThreads().threads.toFloat()

        setComputerMoveFirstButton()
        setControlState()
    }

    /**
     * Sets the computer move first image
     */
    private fun setComputerMoveFirstButton() {
        if (computerMoveFirstDialogValue) {
            binding.computerMoveFirstButton.setImageResource(R.drawable.whitepawnlarge)
        }
        else {
            binding.computerMoveFirstButton.setImageResource(R.drawable.blackpawnlarge)
        }
    }

    /**
     * Gets the value as string, or return off if zero
     */
    private fun getValueZeroOff(pValue: Int): String {
        return if (pValue > 0) NumberFormat.getIntegerInstance().format(pValue) else "off"

    }

    companion object {

        // A constructor containing pActivityID is in the ActivityFragmentFactory

        fun newInstance(pActivityID: Int): EngineSettings {
            val frag = EngineSettings(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
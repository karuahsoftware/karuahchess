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
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.common.*
import purpletreesoftware.karuahchess.databinding.FragmentClocksettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.*


@ExperimentalUnsignedTypes
class ClockSettings(pActivityID: Int) : DialogFragment() {

    private var _binding: FragmentClocksettingsBinding? = null
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
        val fragmentBinding = FragmentClocksettingsBinding.inflate(inflater, container, false)
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

        // Pause the clocks
        mainActivity.binding.clockLayout.pauseClock()

        // Hour spinner
        val hourAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.clockHour
        )
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.whiteTimeHourSpinner.adapter = hourAdapter
        binding.blackTimeHourSpinner.adapter = hourAdapter


        // Black hour spinner 
        val whiteHours = mainActivity.binding.clockLayout.whiteClock.remainingNano().nanoSecondsToSeconds().hoursFromSeconds()
        if (whiteHours in 0..10) { binding.whiteTimeHourSpinner.setSelection(whiteHours) }
        else { binding.whiteTimeHourSpinner.setSelection(0) }

        // White hour spinner 
        val blackHours: Int = mainActivity.binding.clockLayout.blackClock.remainingNano().nanoSecondsToSeconds().hoursFromSeconds()
        if (blackHours in 0..10) { binding.blackTimeHourSpinner.setSelection(blackHours) }
        else { binding.blackTimeHourSpinner.setSelection(0) }


        // Minute spinner
        val minSecAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.clockMinSec
        )
        minSecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.whiteTimeMinuteSpinner.adapter = minSecAdapter
        binding.blackTimeMinuteSpinner.adapter = minSecAdapter

        // White minute spinner
        val whiteMinutes: Int = mainActivity.binding.clockLayout.whiteClock.remainingNano().nanoSecondsToSeconds().minutesFromSeconds()
        if (whiteMinutes in 0..59) { binding.whiteTimeMinuteSpinner.setSelection(whiteMinutes) }
        else { binding.whiteTimeMinuteSpinner.setSelection(0) }

        // Black minute spinner
        val blackMinutes: Int = mainActivity.binding.clockLayout.blackClock.remainingNano().nanoSecondsToSeconds().minutesFromSeconds()
        if (blackMinutes in 0..59) { binding.blackTimeMinuteSpinner.setSelection(blackMinutes) }
        else { binding.blackTimeMinuteSpinner.setSelection(0) }

        // Second spinner
        binding.whiteTimeSecondSpinner.adapter = minSecAdapter
        binding.blackTimeSecondSpinner.adapter = minSecAdapter

        // Second spinner set selected value
        val whiteSeconds: Int = mainActivity.binding.clockLayout.whiteClock.remainingNano().nanoSecondsToSeconds().secondsFromSeconds()
        if (whiteSeconds in 0..59) { binding.whiteTimeSecondSpinner.setSelection(whiteSeconds) }
        else { binding.whiteTimeSecondSpinner.setSelection(0) }

        val blackSeconds: Int = mainActivity.binding.clockLayout.blackClock.remainingNano().nanoSecondsToSeconds().secondsFromSeconds()
        if (blackSeconds in 0..59) { binding.blackTimeSecondSpinner.setSelection(blackSeconds) }
        else { binding.blackTimeSecondSpinner.setSelection(0) }

        // Reset spinner
        val defaultResetAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.clockResetLabel
        )
        defaultResetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.resetSpinner.adapter = defaultResetAdapter

        val defaultClockIndex: Int = ParameterDataService.getInstance(mainActivity.getActivityID()).get(ParamClockDefault::class.java).index
        if (defaultClockIndex < Constants.clockResetSeconds.size) { binding.resetSpinner.setSelection(defaultClockIndex) }
        else { binding.resetSpinner.setSelection(0) }

        // Connect button events
        binding.closeButton.setOnClickListener { dismiss() }
        binding.resetButton.setOnClickListener { reset() }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Save values
      */
    private fun save(){
        val mainActivity = activity as MainActivity

        val whiteSecondsRemaining: Int = getWhiteSecondsFromSpinner()
        val blackSecondsRemaining: Int = getBlackSecondsFromSpinner()

        // Set the clock
        mainActivity.binding.clockLayout.setClock(whiteSecondsRemaining, blackSecondsRemaining)

        // Save the default
        val clockDefaultParam = ParameterDataService.getInstance(activityID).get(ParamClockDefault::class.java)
        if (clockDefaultParam.index != binding.resetSpinner.selectedItemPosition) {
            clockDefaultParam.index = binding.resetSpinner.selectedItemPosition
            ParameterDataService.getInstance(activityID).set(clockDefaultParam)
        }

        // Update the clock if first move
        mainActivity.initialiseClockFirstMove(whiteSecondsRemaining, blackSecondsRemaining)

    }

    /**
     * Get white seconds from spinner
     */
    private fun getWhiteSecondsFromSpinner() : Int {
       return (binding.whiteTimeHourSpinner.selectedItemPosition * 3600 +
                binding.whiteTimeMinuteSpinner.selectedItemPosition * 60 +
                binding.whiteTimeSecondSpinner.selectedItemPosition)
    }

    /**
     * Get black seconds from spinner
     */
    private fun getBlackSecondsFromSpinner() : Int {
        return (binding.blackTimeHourSpinner.selectedItemPosition * 3600 +
                binding.blackTimeMinuteSpinner.selectedItemPosition * 60 +
                binding.blackTimeSecondSpinner.selectedItemPosition)
    }

    /**
     * Reset to default
     */
    private fun reset() {
        val resetSeconds = Constants.clockResetSeconds[binding.resetSpinner.selectedItemPosition]

        // White hour spinner
        binding.whiteTimeHourSpinner.setSelection(resetSeconds.hoursFromSeconds())
        binding.blackTimeHourSpinner.setSelection(resetSeconds.hoursFromSeconds())

        binding.whiteTimeMinuteSpinner.setSelection(resetSeconds.minutesFromSeconds())
        binding.blackTimeMinuteSpinner.setSelection(resetSeconds.minutesFromSeconds())

        binding.whiteTimeSecondSpinner.setSelection(resetSeconds.secondsFromSeconds())
        binding.blackTimeSecondSpinner.setSelection(resetSeconds.secondsFromSeconds())

    }

    companion object {
        fun newInstance(pActivityID: Int): ClockSettings {
            val frag = ClockSettings(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
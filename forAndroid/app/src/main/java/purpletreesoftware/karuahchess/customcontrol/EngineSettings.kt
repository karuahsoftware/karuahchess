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


import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentEnginesettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.ParamComputerMoveFirst
import purpletreesoftware.karuahchess.model.parameterobj.ParamComputerPlayer
import purpletreesoftware.karuahchess.model.parameterobj.ParamLimitEngineStrengthELO

@ExperimentalUnsignedTypes
class EngineSettings : DialogFragment() {

    private var _binding: FragmentEnginesettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var _parameterDS:ParameterDataService


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
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
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        // Set initial values
        binding.computerPlayerEnabledCheckBox.isChecked = _parameterDS.get(ParamComputerPlayer::class.java).enabled
        binding.computerPlayerEnabledCheckBox.setOnClickListener { setControlState() }

        binding.computerMoveFirstCheckBox.isChecked = _parameterDS.get(ParamComputerMoveFirst::class.java).enabled
        binding.computerMoveFirstCheckBox.setOnClickListener { setControlState()}

        // Set elo spinner
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(activity as Context, android.R.layout.simple_spinner_item, Constants.strengthArrayLabel)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.elospinner.adapter = adapter

        // Set the spinner position
        val eloIndex = Constants.eloarray.indexOf(_parameterDS.get(ParamLimitEngineStrengthELO::class.java).eloRating)
        if (eloIndex > -1) {
            binding.elospinner.setSelection(eloIndex)
        }

        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }

        // Set initial state of controls
        setControlState()
    }


    override fun onDestroyView() {
        if (retainInstance) {
            dialog?.setDismissMessage(null)
        }

        super.onDestroyView()

        _binding = null
    }


    /**
     * Saves form values
      */
    private fun save(){

        val computerPlayer = _parameterDS.get(ParamComputerPlayer::class.java)
        if (computerPlayer.enabled != binding.computerPlayerEnabledCheckBox.isChecked) {
            computerPlayer.enabled = binding.computerPlayerEnabledCheckBox.isChecked
            _parameterDS.set(computerPlayer)
        }

        val computerMoveFirst = _parameterDS.get(ParamComputerMoveFirst::class.java)
        if (computerMoveFirst.enabled != binding.computerMoveFirstCheckBox.isChecked) {
            computerMoveFirst.enabled = binding.computerMoveFirstCheckBox.isChecked
            _parameterDS.set(computerMoveFirst)
        }

        val limitEngineStrengthELO = _parameterDS.get(ParamLimitEngineStrengthELO::class.java)
        if (limitEngineStrengthELO.eloRating != Constants.eloarray[binding.elospinner.selectedItemPosition]) {
            limitEngineStrengthELO.eloRating = Constants.eloarray[binding.elospinner.selectedItemPosition]
            _parameterDS.set(limitEngineStrengthELO)
        }

    }

    /**
     * Enables and disables controls depending on options set
     */
    private fun setControlState() {

        val computerPlayer = binding.computerPlayerEnabledCheckBox.isChecked
        binding.computerMoveFirstCheckBox.isEnabled = computerPlayer
        binding.elospinner.isEnabled = computerPlayer
        binding.eloStrengthTitleText.isEnabled = computerPlayer
    }


    companion object {
        fun newInstance(pParameterDS: ParameterDataService): EngineSettings {
            val frag = EngineSettings()
            val args = Bundle()
            frag.arguments = args
            frag._parameterDS = pParameterDS
            return frag
        }
    }
}
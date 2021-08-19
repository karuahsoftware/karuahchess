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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.common.ColourARGB
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentSoundcoloursettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.*


@ExperimentalUnsignedTypes
class SoundColourSettings : DialogFragment() {

    private var _binding: FragmentSoundcoloursettingsBinding? = null
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
        val fragmentBinding = FragmentSoundcoloursettingsBinding.inflate(inflater, container, false)
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
        binding.soundReadCheckBox.isChecked = ParameterDataService.get(ParamSoundRead::class.java).enabled
        binding.soundEffectCheckBox.isChecked = ParameterDataService.get(ParamSoundEffect::class.java).enabled

        // Set board colour spinner
        val adapter = DarkSquareColourAdapter(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.darkSquareColourList
        )

        binding.darksquarecolourspinner.adapter = adapter

        // Set the spinner position
        val boardColourIndex = Constants.darkSquareColourList.indexOf(ParameterDataService.get(ParamColourDarkSquares::class.java).argb())
        if (boardColourIndex > -1) {
            binding.darksquarecolourspinner.setSelection(boardColourIndex)
        }

        // Connect button events
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

        val soundRead = ParameterDataService.get(ParamSoundRead::class.java)
        if (soundRead.enabled != binding.soundReadCheckBox.isChecked) {
            soundRead.enabled = binding.soundReadCheckBox.isChecked
            ParameterDataService.set(soundRead)
        }

        val soundEffect = ParameterDataService.get(ParamSoundEffect::class.java)
        if (soundEffect.enabled != binding.soundEffectCheckBox.isChecked) {
            soundEffect.enabled = binding.soundEffectCheckBox.isChecked
            ParameterDataService.set(soundEffect)
        }

        val darkSquareColour = ParameterDataService.get(ParamColourDarkSquares::class.java)
        val darkSquareColourSelectedItem = binding.darksquarecolourspinner.selectedItem as ColourARGB
        if (darkSquareColour.argb() != darkSquareColourSelectedItem) {
            darkSquareColour.a = darkSquareColourSelectedItem.a
            darkSquareColour.r = darkSquareColourSelectedItem.r
            darkSquareColour.g = darkSquareColourSelectedItem.g
            darkSquareColour.b = darkSquareColourSelectedItem.b
            ParameterDataService.set(darkSquareColour)

            // Apply the new colour to the board
            val mainActivity = activity as MainActivity
            mainActivity.applyBoardColour()
        }
    }

    companion object {
        fun newInstance(): SoundColourSettings {
            val frag = SoundColourSettings()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
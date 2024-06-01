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
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.common.ColourARGB
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentBoardsettingsBinding
import purpletreesoftware.karuahchess.model.parameter.ParameterDataService
import purpletreesoftware.karuahchess.model.parameterobj.ParamColourDarkSquares
import purpletreesoftware.karuahchess.model.parameterobj.ParamRotateBoard


@ExperimentalUnsignedTypes
class BoardSettings(pActivityID: Int) : DialogFragment() {

    private var rotation: Int = 0
    private var _binding: FragmentBoardsettingsBinding? = null
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
        val fragmentBinding = FragmentBoardsettingsBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        save()
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set board colour spinner
        val adapter = DarkSquareColourAdapter(
            activity as Context,
            android.R.layout.simple_spinner_item,
            Constants.darkSquareColourList
        )

        binding.darksquarecolourspinner.adapter = adapter

        // Set the spinner position
        val boardColourIndex = Constants.darkSquareColourList.indexOf(ParameterDataService.getInstance(activityID).get(ParamColourDarkSquares::class.java).argb())
        if (boardColourIndex > -1) {
            binding.darksquarecolourspinner.setSelection(boardColourIndex)
        }

        // Listen for colour selection changes in the spinner
        binding.darksquarecolourspinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val darkSquareColourSelectedItem = binding.darksquarecolourspinner.selectedItem as ColourARGB
                binding.orientationImage.setBackgroundColor(Color.argb(darkSquareColourSelectedItem.a, darkSquareColourSelectedItem.r, darkSquareColourSelectedItem.g, darkSquareColourSelectedItem.b))
            }

        }

        // Set orientation indicator
        setOrientationIndicator(ParameterDataService.getInstance(activityID).get(ParamRotateBoard::class.java).value)

        // Set orientation colour
        val darkSquareColourSelectedItem = binding.darksquarecolourspinner.selectedItem as ColourARGB
        binding.orientationImage.setBackgroundColor(Color.argb(darkSquareColourSelectedItem.a, darkSquareColourSelectedItem.r, darkSquareColourSelectedItem.g, darkSquareColourSelectedItem.b))

        // Orientation button listener
        binding.rotateButton.setOnClickListener {
            val newRotation = if (rotation + 90 >= 360) 0 else rotation + 90
            setOrientationIndicator(newRotation)
        }


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

        val darkSquareColour = ParameterDataService.getInstance(activityID).get(ParamColourDarkSquares::class.java)
        val darkSquareColourSelectedItem = binding.darksquarecolourspinner.selectedItem as ColourARGB
        if (darkSquareColour.argb() != darkSquareColourSelectedItem) {
            darkSquareColour.a = darkSquareColourSelectedItem.a
            darkSquareColour.r = darkSquareColourSelectedItem.r
            darkSquareColour.g = darkSquareColourSelectedItem.g
            darkSquareColour.b = darkSquareColourSelectedItem.b
            ParameterDataService.getInstance(activityID).set(darkSquareColour)

            // Apply the new colour to the board
            mainActivity.applyBoardColour()
        }

        // Save the rotation parameter
        val currentRotationParam = ParameterDataService.getInstance(activityID).get(ParamRotateBoard::class.java)
        if (rotation != currentRotationParam.value) {
            currentRotationParam.value = rotation
            ParameterDataService.getInstance(activityID).set(currentRotationParam)
            mainActivity.rotateBoard(rotation)
        }

    }

    /**
     * Sets the layout of the orientation indicator
     */
    private fun setOrientationIndicator(pRotation: Int) {
        rotation = pRotation
        binding.orientationImage.rotation = pRotation.toFloat()
    }

    companion object {
        fun newInstance(pActivityID: Int): BoardSettings {
            val frag = BoardSettings(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
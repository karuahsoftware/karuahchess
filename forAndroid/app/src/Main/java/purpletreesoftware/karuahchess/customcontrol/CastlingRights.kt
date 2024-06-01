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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.common.Constants
import purpletreesoftware.karuahchess.databinding.FragmentCastlingrightsBinding
import purpletreesoftware.karuahchess.engine.KaruahChessEngine
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordArray
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService
import purpletreesoftware.karuahchess.viewmodel.CastlingRightsViewModel

@ExperimentalUnsignedTypes
class CastlingRights(pActivityID: Int) : DialogFragment() {
    private var _binding: FragmentCastlingrightsBinding? = null
    private val binding get() = _binding!!
    private val board = KaruahChessEngine(App.appContext, pActivityID)
    private lateinit var castlingRightsVM: CastlingRightsViewModel
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
        val fragmentBinding = FragmentCastlingrightsBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castlingRightsVM = ViewModelProvider(requireActivity()).get(CastlingRightsViewModel::class.java)

        // Connect button events
        binding.doneButton.setOnClickListener { dismiss() }

        // Set initial state of controls
        setControlState()
    }

    override fun onDismiss(dialog: DialogInterface) {
        save()
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * Saves form values
     */
    private fun save(){

        // Load the record in to a board
        var success: Boolean = false

        // Check that castling selection is valid
        if (castlingRightsVM.kingSpin.value == Constants.WHITE_KING_SPIN) {
            var stateCastlingAvailability: Int = 0
            if (binding.KingSideCastleCheckBox.isChecked) stateCastlingAvailability = stateCastlingAvailability or 0b000010
            if (binding.QueenSideCastleCheckBox.isChecked) stateCastlingAvailability = stateCastlingAvailability or 0b000001
            success = board.setStateCastlingAvailability(stateCastlingAvailability, Constants.WHITEPIECE)
        }
        else if (castlingRightsVM.kingSpin.value == Constants.BLACK_KING_SPIN) {
            var stateCastlingAvailability: Int = 0
            if (binding.KingSideCastleCheckBox.isChecked) stateCastlingAvailability = stateCastlingAvailability or 0b001000
            if (binding.QueenSideCastleCheckBox.isChecked) stateCastlingAvailability = stateCastlingAvailability or 0b000100
            success = board.setStateCastlingAvailability(stateCastlingAvailability, Constants.BLACKPIECE)
        }

        // Save the values
        if (success) {
            val updatedRecord = GameRecordArray()
            updatedRecord.id = castlingRightsVM.record.value?.id ?: -1
            updatedRecord.boardArray = board.getBoardArray()
            updatedRecord.stateArray = board.getStateArray()

            val mainActivity = activity as MainActivity
            GameRecordDataService.getInstance(activityID).updateGameState(updatedRecord)
            mainActivity.updateBoardIndicators(updatedRecord)
        }
        else {
            val mainActivity = activity as MainActivity
            mainActivity.showMessage("Error, cannot set castling rights as Rook or King position is not valid for castling.", "", Toast.LENGTH_LONG)
        }
    }

    /**
     * Enables and disables controls depending on options set
     */
    private fun setControlState() {
         val record = castlingRightsVM.record.value

         if (record != null) {
             board.setBoardArray(record.boardArray)
             board.setStateArray(record.stateArray)

             val stateCastlingAvailability: Int = board.getStateCastlingAvailability()
             if (castlingRightsVM.kingSpin.value == Constants.WHITE_KING_SPIN) {
                 binding.kingImageView.setImageResource(R.drawable.whiteking0)
                 binding.KingSideCastleCheckBox.isChecked =
                     (stateCastlingAvailability and 0b000010) > 0
                 binding.QueenSideCastleCheckBox.isChecked =
                     (stateCastlingAvailability and 0b000001) > 0
             } else {
                 binding.kingImageView.setImageResource(R.drawable.blackking0)
                 binding.KingSideCastleCheckBox.isChecked =
                     (stateCastlingAvailability and 0b001000) > 0
                 binding.QueenSideCastleCheckBox.isChecked =
                     (stateCastlingAvailability and 0b000100) > 0
             }
         }
    }

    companion object {
        fun newInstance(pActivityID: Int): CastlingRights {
            val frag = CastlingRights(pActivityID)
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
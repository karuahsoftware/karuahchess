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

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.databinding.MovenavigatorpanelBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import purpletreesoftware.karuahchess.model.boardsquare.BoardSquareDataService
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService


@ExperimentalUnsignedTypes
class MoveNavigator : LinearLayout {
    private var _binding: MovenavigatorpanelBinding? = null
    private val binding get() = _binding!!
    private var recordList: ArrayList<Int>? = null
    private var moveNavAdapter: MoveNavigatorAdapter? = null
    private var isPortrait: Boolean = false
    private var isLandscape: Boolean = false

    private val activityID: Int
        get() = (context as? MainActivity)?.getActivityID() ?: -1

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)
    }


    private fun initialize(context: Context) {
        val mainActivity = context as MainActivity

        // Inflate view
        _binding = MovenavigatorpanelBinding.inflate(LayoutInflater.from(context), this, true)
        binding.leftNavPreviousButton.setOnClickListener { navigateToPrevious(mainActivity) }
        binding.rightNavNextButton.setOnClickListener { navigateToNext(mainActivity) }
        binding.viewPGNTextButton.setOnClickListener {
            val pgnText = moveNavAdapter?.buildPGNFromNavigator() ?: ""
            mainActivity.showMoveNavigatorPGNDialog(pgnText)
        }

    }

    /**
     * Navigate to next record
     */
    private fun navigateToNext(mainActivity: MainActivity) {
        mainActivity.uiScope.launch(Dispatchers.Main) {
            val current = BoardSquareDataService.getInstance(mainActivity.getActivityID()).gameRecordCurrentValue
            val nextId = getNextRecordId(current)
            if (nextId > -1) mainActivity.navigateGameRecord(nextId, true, false, true)
        }
    }

    /**
     * Navigate to previous record
     */
    private fun navigateToPrevious(mainActivity: MainActivity) {
        mainActivity.uiScope.launch(Dispatchers.Main) {
            val current = BoardSquareDataService.getInstance(mainActivity.getActivityID()).gameRecordCurrentValue
            val prevId = getPreviousRecordId(current)
            if (prevId > -1) mainActivity.navigateGameRecord(prevId, true, false, true)
        }
    }

    /**
     * Set the selected button
     */
    fun setSelected(pSelectedId: Int) {
        // Save and restore state ensures the recycler maintains its scroll position
        val recyclerViewState = binding.moveNavRecyclerView.layoutManager?.onSaveInstanceState()
        moveNavAdapter?.setSelectedId(pSelectedId)

        if (recyclerViewState != null) {
            binding.moveNavRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }

    }

    /**
     * Scroll to the selected button
     */
    fun scrollToSelected() {
        if (visibility != View.VISIBLE) return

        val rv = binding.moveNavRecyclerView
       rv.adapter ?: return

        val selectedId = BoardSquareDataService.getInstance(activityID).gameRecordCurrentValue
        val position = getPosition(selectedId)
        if (position < 0) return

        rv.post {
            val lm = rv.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
            if (lm == null) {
                // Fallback if not a LinearLayoutManager
                rv.scrollToPosition(position)
                return@post
            }

            // Choose partial or complete visibility methods as needed
            val first = lm.findFirstVisibleItemPosition()
            val last = lm.findLastVisibleItemPosition()

            if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) {
                // Not laid out yet; perform a safe scroll
                lm.scrollToPositionWithOffset(position, 0)
                return@post
            }

            val start = kotlin.math.min(first, last)
            val end = kotlin.math.max(first, last)

            // Only scroll if target is outside the current visible window
            if (position < start || position > end) {
                lm.scrollToPositionWithOffset(position, 0)
            }
        }
    }


    /**
     * Sets the orientation of the control
     */
    fun setLayout(pIsPortrait: Boolean) {

        // Exit straight away if orientation is not changing
        if (pIsPortrait == isPortrait && isPortrait != isLandscape) return

        val mainActivity = context as MainActivity

        isPortrait = pIsPortrait
        isLandscape = !pIsPortrait

        // Set to vertical or horizontal layout depending on screen rotation
        if(!pIsPortrait) {


            binding.moveNavRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                mainActivity,
                RecyclerView.VERTICAL,
                true)

            binding.mainNavLayout.orientation = LinearLayout.VERTICAL

            (binding.moveNavRecyclerView.adapter as? MoveNavigatorAdapter)?.setLabelLayout(pIsPortrait)

            // Set next and previous navigation button
            binding.leftNavPreviousButton.rotation = 90f
            binding.rightNavNextButton.rotation = 90f
        }
        else {


            binding.moveNavRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                mainActivity,
                RecyclerView.HORIZONTAL,
                false)

            binding.mainNavLayout.orientation = LinearLayout.HORIZONTAL

            (binding.moveNavRecyclerView.adapter as? MoveNavigatorAdapter)?.setLabelLayout(pIsPortrait)

            // Set next and previous navigation buttons
            binding.leftNavPreviousButton.rotation = 0f
            binding.rightNavNextButton.rotation = 0f
        }

        scrollToSelected()
    }

    /**
     * Synchronise the navigation buttons with the current records.
     * Layout rules:
     * - For a white move of move N: insert a TextBlock "N." BEFORE the white move button.
     * - If there is NO white move for move N but there is a black move, insert "N..." BEFORE that black move button.
     * - Handles edge cases where multiple consecutive moves of the same colour appear due to missing records.
     * Start position remains first (Id == 1).
     */
    fun syncNavButtons() {
        if (visibility != View.VISIBLE) return

        val adapter = moveNavAdapter ?: MoveNavigatorAdapter(this, mutableListOf(), -1).also {
            moveNavAdapter = it
            binding.moveNavRecyclerView.adapter = it
        }

        if (binding.moveNavRecyclerView.adapter !== adapter) {
            binding.moveNavRecyclerView.adapter = adapter
        }

        // Get all record ids (must match your data service)
        val navList: List<Int> =
            GameRecordDataService.getInstance(activityID).getAllRecordIDList() // replace with your actual service

        recordList = ArrayList(navList)

        if (navList.isEmpty()) {
            adapter.setItems(emptyList())
            return
        }

        val items = mutableListOf<MoveNavigatorAdapter.NavItem>()

        // Always first: Start position (Id == 1)
        if (navList.first() == 1) {
            items += MoveNavigatorAdapter.NavItem.Button(recId = 1)
        }

        var listIndex = 1
        while (listIndex < navList.size) {
            val recId = navList[listIndex]
            listIndex++

            if (recId <= 1) continue // safety

            val ply = recId - 2
            if (ply < 0) continue

            val status = GameRecordDataService.getInstance(activityID).getStateGameStatus(recId)
            val isResignedOrTimeExpired =
                status == MainActivity.BoardStatusEnum.Resigned.value ||
                        status == MainActivity.BoardStatusEnum.TimeExpired.value

            val activeColour = GameRecordDataService.getInstance(activityID).getActiveMoveColour(recId)
            val isWhitePly = activeColour == -1
            val moveNumber = (ply / 2) + 1

            if (isResignedOrTimeExpired) {
                items += MoveNavigatorAdapter.NavItem.Button(recId)
                continue
            }

            if (isWhitePly) {
                // Insert "N."
                items += MoveNavigatorAdapter.NavItem.Label(text = "$moveNumber.")
                // White move
                items += MoveNavigatorAdapter.NavItem.Button(recId)

                // Attempt to pair with immediately following black reply
                if (listIndex < navList.size) {
                    val nextRecId = navList[listIndex]
                    val nextColour = GameRecordDataService.getInstance(activityID).getActiveMoveColour(nextRecId)
                    val blackReplyExists = nextColour == 1
                    if (blackReplyExists) {
                        items += MoveNavigatorAdapter.NavItem.Button(nextRecId)
                        listIndex++ // consume black
                    }
                }
            } else {
                // Insert "N..."
                items += MoveNavigatorAdapter.NavItem.Label(text = "$moveNumber...")
                // Black move
                items += MoveNavigatorAdapter.NavItem.Button(recId)
            }
        }

        adapter.setItems(items)
        setSelected(adapter.selectedId)

    }


    /**
     * Show the control
     */
    fun show() {
        this.visibility = View.VISIBLE
    }

    /**
     * Hide the control
     */
    fun hide() {
        this.visibility = View.GONE
        binding.moveNavRecyclerView.adapter = null
    }

    /**
     * Returns the content for a navigation button (SAN only, or Start).
     */
    fun getNavLabel(pRecId: Int): String
    {
        if (pRecId == 1) return "Start"

        var rec = GameRecordDataService.getInstance(activityID).get(pRecId)
        val san = rec?.moveSAN
        return if (san.isNullOrBlank() ) "" else san
    }

    /**
     * Get the next record in the list
     */
    private fun getNextRecordId(pRecId: Int): Int {
        if (recordList != null) {
            val rList: ArrayList<Int> = recordList ?: ArrayList()

            var found: Boolean = false
            for (recId in rList) {
                if (found) return recId
                if (recId == pRecId) found = true
            }
            return -1
        }
        else {
            return -1
        }
    }

    /**
     * Get the previous record in the list
     */
    private fun getPreviousRecordId(pRecId: Int): Int {
        if (recordList != null) {
            val rList: ArrayList<Int> = recordList ?: ArrayList()

            var previousId = -1
            for (recId in rList) {
                if (recId == pRecId) return previousId
                previousId = recId
            }
            return -1
        }
        else {
            return -1
        }
    }

    /**
     * Get the position of a record button in the recycler view
     */
    private fun getPosition(pRecId: Int): Int {
        val adapter = binding.moveNavRecyclerView.adapter as? MoveNavigatorAdapter
            ?: moveNavAdapter
            ?: return -1
        return adapter.getPositionForRecord(pRecId)
    }


    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }



}
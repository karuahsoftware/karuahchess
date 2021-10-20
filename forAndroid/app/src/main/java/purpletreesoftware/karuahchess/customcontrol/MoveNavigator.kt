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
import android.content.res.Configuration
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.databinding.MovenavigatorpanelBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import purpletreesoftware.karuahchess.model.boardsquare.BoardSquareDataService


@ExperimentalUnsignedTypes
class MoveNavigator : LinearLayout {
    private var _binding: MovenavigatorpanelBinding? = null
    private val binding get() = _binding!!
    private var recordList: ArrayList<Int>? = null
    private var moveNavAdapter: MoveNavigatorAdapter? = null

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)
    }



    private fun initialize(context: Context) {
        val activity = context as MainActivity

        // Inflate view
        _binding = MovenavigatorpanelBinding.inflate(LayoutInflater.from(context), this, true)

        // Set listener to refresh the scroll indicators
        binding.moveNavRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                showScrollIndicator()
            }
        })

        // Set the navigate previous button listener
        binding.leftNavPreviousButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val prevId = getPreviousRecordId(BoardSquareDataService.gameRecordCurrentValue)
                if (prevId > -1) {
                    activity.navigateGameRecord(prevId, true, false, true)

                    // Scroll to current position
                    val gameRecIndex = getRecordIndex(prevId)
                    if (gameRecIndex > -1) {
                        binding.moveNavRecyclerView.layoutManager?.scrollToPosition(gameRecIndex)
                    }
                }
            }
        }

        // Set the navigate next button listener
        binding.rightNavNextButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val nextId = getNextRecordId(BoardSquareDataService.gameRecordCurrentValue)

                if (nextId > -1) {
                    activity.navigateGameRecord(nextId, true, false, true)

                    // Scroll to current position
                    val gameRecIndex = getRecordIndex(nextId)
                    if (gameRecIndex > -1) {
                        binding.moveNavRecyclerView.layoutManager?.scrollToPosition(gameRecIndex)
                    }
                }
            }
        }

        // Set the navigate next button listener
        binding.upNavNextButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val nextId = getNextRecordId(BoardSquareDataService.gameRecordCurrentValue)

                if (nextId > -1) {
                    activity.navigateGameRecord(nextId, true, false, true)

                    // Scroll to current position
                    val gameRecIndex = getRecordIndex(nextId)
                    if (gameRecIndex > -1) {
                        binding.moveNavRecyclerView.layoutManager?.scrollToPosition(gameRecIndex)
                    }
                }
            }
        }

        // Set the navigate previous button listener
        binding.downNavPreviousButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val prevId = getPreviousRecordId(BoardSquareDataService.gameRecordCurrentValue)
                if (prevId > -1) {
                    activity.navigateGameRecord(prevId, true, false, true)

                    // Scroll to current position
                    val gameRecIndex = getRecordIndex(prevId)
                    if (gameRecIndex > -1) {
                        binding.moveNavRecyclerView.layoutManager?.scrollToPosition(gameRecIndex)
                    }
                }
            }
        }
    }



    /**
     * Show the control and load the data
     */
    fun load(pNavList: ArrayList<Int>, pSelectedId: Int) {
        if (this.visibility == View.VISIBLE) {
            recordList = pNavList

            // Set orientation
            setOrientation()

            // Fill data
            moveNavAdapter = MoveNavigatorAdapter(this, pNavList, pSelectedId)
            binding.moveNavRecyclerView.adapter = moveNavAdapter

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
        // Scroll to current position
        if (this.visibility == View.VISIBLE) {
            val gameRecIndex = getRecordIndex(BoardSquareDataService.gameRecordCurrentValue)
            if (gameRecIndex > -1) {
                binding.moveNavRecyclerView.layoutManager?.scrollToPosition(gameRecIndex)
            }
        }
    }

    /**
     * Shows the scroll bar indicator
     */
    private fun showScrollIndicator(){
        // Left scroll
        if(binding.moveNavRecyclerView.canScrollHorizontally(-1)) {
            binding.leftNavImage.visibility = View.VISIBLE
        }
        else {
            binding.leftNavImage.visibility = View.GONE
        }

        // Right scroll
        if(binding.moveNavRecyclerView.canScrollHorizontally(1)) {
            binding.rightNavImage.visibility = View.VISIBLE
        }
        else {
            binding.rightNavImage.visibility = View.GONE
        }

        // Up scroll
        if(binding.moveNavRecyclerView.canScrollVertically(-1)) {
            binding.upNavImage.visibility = View.VISIBLE
        }
        else {
            binding.upNavImage.visibility = View.GONE
        }

        // Down scroll
        if(binding.moveNavRecyclerView.canScrollVertically(1)) {
            binding.downNavImage.visibility = View.VISIBLE
        }
        else {
            binding.downNavImage.visibility = View.GONE
        }

    }

    /**
     * Sets the orientation of the control
     */
    private fun setOrientation() {

        val activity = context as MainActivity

        // Set to vertical or horizontal layout depending on screen rotation
        if(this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.moveNavRecyclerView.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(
                activity,
                RecyclerView.VERTICAL,
                true
            ) {
                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                    showScrollIndicator()
                }
            }

            // Change the gravity so it attaches to the right of the board
            val params =  CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.TOP or Gravity.END
            params.anchorGravity = Gravity.TOP or Gravity.END
            params.anchorId = activity.binding.boardPanelLayout.id
            this.layoutParams = params


            binding.mainNavLayout.orientation = LinearLayout.VERTICAL

            // Set next and previous navigation buttons
            binding.leftNavPreviousButton.visibility = View.GONE
            binding.rightNavNextButton.visibility = View.GONE
            binding.upNavNextButton.visibility = View.VISIBLE
            binding.downNavPreviousButton.visibility = View.VISIBLE

        }
        else {
            binding.moveNavRecyclerView.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(
                activity,
                RecyclerView.HORIZONTAL,
                false
            ){
                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                    showScrollIndicator()
                }
            }

            // Change the gravity so it attaches to the right of the board
            val mainParams =  CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
            mainParams.gravity = Gravity.START or Gravity.BOTTOM
            mainParams.anchorGravity = Gravity.START or Gravity.BOTTOM
            mainParams.anchorId = activity.binding.boardPanelLayout.id
            this.layoutParams = mainParams

            binding.mainNavLayout.orientation = LinearLayout.HORIZONTAL

            // Set next and previous navigation buttons
            binding.leftNavPreviousButton.visibility = View.VISIBLE
            binding.rightNavNextButton.visibility = View.VISIBLE
            binding.upNavNextButton.visibility = View.GONE
            binding.downNavPreviousButton.visibility = View.GONE
        }
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
     * Get the index of the record
     */
    private fun getRecordIndex(pRecId: Int): Int {
        if (recordList != null) {
            val rList: ArrayList<Int> = recordList ?: ArrayList()
            for ((index, recId) in rList.withIndex()) {
                if (recId == pRecId) return index
            }
            return -1
        }
        else {
            return -1
        }
    }


    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }



}
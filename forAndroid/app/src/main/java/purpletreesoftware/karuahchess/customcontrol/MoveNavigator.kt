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
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.MovenavigatorpanelBinding


@ExperimentalUnsignedTypes
class MoveNavigator : FrameLayout {
    private var _binding: MovenavigatorpanelBinding? = null
    private val binding get() = _binding!!
    var gameRecCurrentId: Int = -1


    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initialize(context)
    }



    private fun initialize(context: Context) {
        // Inflate view
        _binding = MovenavigatorpanelBinding.inflate(LayoutInflater.from(context), this, true)

        // Set listener to refresh the scroll indicators
        binding.moveNavRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                showScrollIndicator()
            }
        })


    }

    /**
     * Show the control and load the data
     */
    fun show(pMainActivity: MainActivity, pGameRecIdList: ArrayList<Int>, pGameRecCurrentId: Int) {
        this.visibility = View.VISIBLE
        gameRecCurrentId = pGameRecCurrentId

        // Set orientation
        setOrientation(pMainActivity)

        // Fill data
        binding.moveNavRecyclerView.adapter = MoveNavigatorAdapter(pMainActivity, this, pGameRecIdList, pGameRecCurrentId)

        // Scroll to current position
        binding.moveNavRecyclerView.layoutManager?.scrollToPosition(pGameRecCurrentId - 1)

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
    private fun setOrientation(pMainActivity: MainActivity) {

        // Set to vertical or horizontal layout depending on screen rotation
        if(this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.moveNavRecyclerView.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(
                pMainActivity,
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
            params.anchorId = pMainActivity.binding.boardPanelLayout.id
            this.layoutParams = params

        }
        else {
            binding.moveNavRecyclerView.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(
                pMainActivity,
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
            mainParams.anchorId = pMainActivity.binding.boardPanelLayout.id
            this.layoutParams = mainParams


        }
    }

    /**
     * Hide the control
     */
    fun hide() {
        this.visibility = View.GONE
        binding.moveNavRecyclerView.adapter = null
    }


    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }



}
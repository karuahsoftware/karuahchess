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

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.MovenavigatorItemBinding



@ExperimentalUnsignedTypes
class MoveNavigatorAdapter(private val pMoveNav: MoveNavigator, private val pGameRecIdList: ArrayList<Int>, private val pSelectedId: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<MoveNavigatorAdapter.MoveNavViewHolder>()  {

    var selectedId = pSelectedId
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveNavViewHolder {
        val itemBinding = MovenavigatorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoveNavViewHolder(itemBinding)
    }

    override fun onBindViewHolder(pHolder: MoveNavViewHolder, pPosition: Int) {
        pHolder.setNavId(pGameRecIdList[pPosition])
    }

    override fun getItemCount(): Int = pGameRecIdList.size

    fun setSelectedId(pNavId: Int) {
        selectedId = pNavId
        notifyDataSetChanged()
    }


    inner class MoveNavViewHolder(pItemBinding: MovenavigatorItemBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(pItemBinding.root) {
        private var navId : Int = -1
        private val recButton = pItemBinding.root

        init {
            recButton.setOnClickListener {
                if (navId > -1) {
                    GlobalScope.launch(Dispatchers.Main) {
                        val mainActivity = recButton.context as MainActivity
                        val distance = navId - selectedId
                        val animate: Boolean = distance == 1 || distance == -1
                        mainActivity.navigateGameRecord(navId, animate, false, false)
                    }
                }
            }

            setButtonStyle()
        }

        /**
         * Set the navigation ID
         */
        fun setNavId(pNavId: Int) {
            navId = pNavId
            recButton.text = pNavId.toString()
            setButtonStyle()
        }

        /**
         * Set the button style based on the selected ID
         */
        private fun setButtonStyle() {
            val mainActivity = recButton.context as MainActivity
            if (selectedId == navId) {
                recButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.colorButtonSelected
                    )
                )
                recButton.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorButtonDefaultText))
                recButton.alpha = 1.0f
            } else {
                recButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.colorButtonDefault
                    )
                )
                recButton.setTextColor(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.colorButtonDefaultTextFade
                    )
                )
                recButton.alpha = 0.8f
            }



        }
    }
}
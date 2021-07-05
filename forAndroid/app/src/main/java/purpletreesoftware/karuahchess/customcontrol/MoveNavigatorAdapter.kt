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

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat.getColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.MovenavigatorItemBinding

@ExperimentalUnsignedTypes
class MoveNavigatorAdapter(private val pMainActivity: MainActivity, private val pMoveNav: MoveNavigator, private val pGameRecIdList: ArrayList<Int>, private val pGameRecCurrentId: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<MoveNavigatorAdapter.ViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemBinding = MovenavigatorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)

    }

    override fun onBindViewHolder(pHolder: ViewHolder, pPosition: Int) {
        val navId = pGameRecIdList[pPosition]
        pHolder.recordButton?.text = navId.toString()
        pHolder.recordButton?.setOnClickListener {
            pMoveNav.gameRecCurrentId = navId
            GlobalScope.launch(Dispatchers.Main) {
                pMainActivity.navigateGameRecord(navId, false)
            }
            notifyDataSetChanged()
        }

        if (navId == pMoveNav.gameRecCurrentId) {
            pHolder.recordButton?.backgroundTintList = ColorStateList.valueOf(getColor(pMainActivity, R.color.colorButtonSelected))
            pHolder.recordButton?.setTextColor(getColor(pMainActivity, R.color.colorButtonDefaultText))
            pHolder.recordButton?.alpha = 1.0f
        }
        else {
            pHolder.recordButton?.backgroundTintList = ColorStateList.valueOf(getColor(pMainActivity, R.color.colorButtonDefault))
            pHolder.recordButton?.setTextColor(getColor(pMainActivity, R.color.colorButtonDefaultTextFade))
            pHolder.recordButton?.alpha = 0.8f
        }


    }

    override fun getItemCount(): Int = pGameRecIdList.size


    inner class ViewHolder(pItemBinding: MovenavigatorItemBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(pItemBinding.root) {
        var recordButton : Button? = pItemBinding.recordIdButton
    }




}
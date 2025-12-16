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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import purpletreesoftware.karuahchess.MainActivity
import purpletreesoftware.karuahchess.R
import purpletreesoftware.karuahchess.databinding.MovenavigatorItemBinding
import android.view.Gravity

@ExperimentalUnsignedTypes
class MoveNavigatorAdapter(
    private val pMoveNav: MoveNavigator,
    private val pItems: MutableList<NavItem>,
    pSelectedId: Int
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private enum class LabelLayout { Portrait, Landscape }
    private var labelLayout: LabelLayout = LabelLayout.Portrait

    var selectedId = pSelectedId
        private set

    fun setLabelLayout(isPortrait: Boolean) {
        val newLayout = if (isPortrait) LabelLayout.Portrait else LabelLayout.Landscape
        if (labelLayout != newLayout) {
            labelLayout = newLayout
            notifyDataSetChanged() // rebinding will re-apply layout
        }
    }

    sealed class NavItem {
        data class Label(val text: String) : NavItem()
        data class Button(val recId: Int) : NavItem()
    }

    override fun getItemViewType(position: Int): Int = when (pItems[position]) {
        is NavItem.Label -> 0
        is NavItem.Button -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val tv = TextView(parent.context).apply {
                isEnabled = false
                isClickable = false
                setTextColor(ContextCompat.getColor(context, R.color.colorButtonDefaultText))
                background = null
            }
            applyLabelLayout(tv, parent)
            LabelViewHolder(tv)
        } else {
            val itemBinding = MovenavigatorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            // Ensure the button expands to fit text and never wraps
            itemBinding.root.apply {
                maxLines = 1
                isSingleLine = true
            }

            MoveBtnViewHolder(itemBinding)
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (val item = pItems[position]) {
            is NavItem.Label -> (holder as LabelViewHolder).bind(item.text)
            is NavItem.Button -> (holder as MoveBtnViewHolder).bind(item.recId)
        }
    }

    override fun getItemCount(): Int = pItems.size


    fun getPositionForRecord(recId: Int): Int {
        for ((index, item) in pItems.withIndex()) {
            if (item is NavItem.Button && item.recId == recId) return index
        }
        return -1
    }

    fun setItems(newItems: List<NavItem>) {
        pItems.clear()
        pItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setSelectedId(pNavId: Int) {
        selectedId = pNavId
        notifyDataSetChanged()
    }

    private fun applyLabelLayout(tv: TextView, parent: ViewGroup? = null) {
        val density = tv.resources.displayMetrics.density

        tv.alpha = 0.5f

        when (labelLayout) {
            LabelLayout.Portrait -> {
                tv.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                tv.minWidth = (18f * density).toInt()
                tv.minHeight = 0
                tv.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
            LabelLayout.Landscape -> {
                tv.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                tv.minWidth = 0
                tv.minHeight = (18f * density).toInt()
                tv.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }

        }

    }

    private fun styleButton(view: TextView, isSelected: Boolean) {
        view.backgroundTintList = null
        if (view.background == null || view.background !is android.graphics.drawable.LayerDrawable) {
            view.setBackgroundResource(R.drawable.button_navigator_border)
        }

        val ctx = view.context

        // Fill background when selected, otherwise clear tint
        view.backgroundTintList = if (isSelected) {
            ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorButtonNavSelected))
        } else {
            null
        }

        val txtColour = if (isSelected) R.color.colorButtonNavTextSelected else R.color.colorButtonNavText
        view.setTextColor(ContextCompat.getColor(ctx, txtColour))

    }

    fun buildPGNFromNavigator(): String {
        if (pItems.isEmpty()) return ""

        val parts = ArrayList<String>(pItems.size)

        for (item in pItems) {
            when (item) {
                is NavItem.Label -> {
                    val text = item.text.trim()
                    if (text.isNotEmpty()) {
                        parts.add(text)
                    }
                }
                is NavItem.Button -> {
                    if (item.recId == 1) continue

                    val san = pMoveNav.getNavLabel(item.recId).trim()
                    if (san.isNotEmpty()) {
                        parts.add("$san ")
                    }
                }
            }
        }

        return parts.joinToString(separator = "")
    }

    inner class LabelViewHolder(
        private val textView: TextView
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(textView) {

        fun bind(text: String) {
            applyLabelLayout(textView)
            textView.text = text
        }
    }

    inner class MoveBtnViewHolder(
        private val binding: MovenavigatorItemBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        private var navId: Int = -1
        private val recButton = binding.root

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
        }

        fun bind(pNavId: Int) {
            navId = pNavId

            // Use MoveNavigator to resolve the label (can be customized there)
            recButton.text = pMoveNav.getNavLabel(pNavId)
            recButton.isEnabled = true
            recButton.isClickable = true
            styleButton(recButton, selectedId == navId)
        }

    }
}
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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import purpletreesoftware.karuahchess.common.ColourARGB
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import purpletreesoftware.karuahchess.databinding.DarksquarecolourItemBinding

class DarkSquareColourAdapter(pContext: Context,
                              @LayoutRes private val pLayoutResource: Int,
                              private val pValues: ArrayList<ColourARGB>) : ArrayAdapter<ColourARGB>(pContext, pLayoutResource, pValues) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DarksquarecolourItemBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.displayName.text = pValues[position].text
        binding.icon.setBackgroundColor(Color.argb(pValues[position].a, pValues[position].r, pValues[position].g, pValues[position].b))

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DarksquarecolourItemBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.displayName.text = pValues[position].text
        binding.icon.setBackgroundColor(Color.rgb(pValues[position].r, pValues[position].g, pValues[position].b))

        return binding.root
    }

}
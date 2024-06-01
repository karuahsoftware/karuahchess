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

package purpletreesoftware.karuahchess.common

import android.text.InputFilter
import android.text.Spanned

class InputFilterRange(var pMin:Int, var pMax:Int): InputFilter {

    override fun filter(
        pSource: CharSequence?,
        pSourceStart: Int,
        pSourceEnd: Int,
        pDest: Spanned?,
        pDestStart: Int,
        pDestEnd: Int
    ): CharSequence? {
        try
        {

            val input = (pDest?.subSequence(0, pDestStart).toString() + pSource + pDest?.subSequence(pDestEnd, pDest.length)).toInt()
            if ((1 .. 2000000000).contains(input)) {
                return null
            }
        }
        catch (nfe:NumberFormatException) {}
        return ""
    }



}
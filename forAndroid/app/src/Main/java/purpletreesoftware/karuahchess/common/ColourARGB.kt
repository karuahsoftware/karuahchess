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

import java.util.*

data class ColourARGB(val a: Int , val r: Int , val g: Int , val b: Int, val text: String)
{
    override fun equals(other: Any?): Boolean {
        return if (other == null || javaClass != other.javaClass) {
            false
        } else {
            val otherColourARGB = other as ColourARGB
            a == otherColourARGB.a && r == otherColourARGB.r && g == otherColourARGB.g && b == otherColourARGB.b
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(a,r,g,b)
    }

    override fun toString(): String {
        return text
    }
}
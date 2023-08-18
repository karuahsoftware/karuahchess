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

package purpletreesoftware.karuahchess.model.parameterobj


import purpletreesoftware.karuahchess.common.ColourARGB

class ParamColourDarkSquares :  java.io.Serializable{
    // Default value is green
    var a: Int = 255
    var r: Int = 100
    var g: Int = 153
    var b: Int = 100

    fun argb(): ColourARGB? {
        return ColourARGB(a, r, g, b, "")
    }
}
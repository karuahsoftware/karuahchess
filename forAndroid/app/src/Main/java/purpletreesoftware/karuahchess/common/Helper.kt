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


class Helper {

    @ExperimentalUnsignedTypes
    companion object {

        private val bitScanMagic: ULong = 251784493209109903u
        private val bitScanMagicTable = intArrayOf (
            0, 1, 17, 2, 18, 50, 3, 57,
            47, 19, 22, 51, 29, 4, 33, 58,
            15, 48, 20, 27, 25, 23, 52, 41,
            54, 30, 38, 5, 43, 34, 59, 8,
            63, 16, 49, 56, 46, 21, 28, 32,
            14, 26, 24, 40, 53, 37, 42, 7,
            62, 55, 45, 31, 13, 39, 36, 6,
            61, 44, 12, 35, 60, 11, 10, 9,
        )

        /**
         * Returns the index of the first bit set from the least significant bit
         */
        fun bitScanForward(pNum: ULong): Int {
            return bitScanMagicTable[((pNum.toLong() and -pNum.toLong()).toULong() * bitScanMagic shr 58).toInt()]
        }



        fun LogError(errorID: Int) {

        }

    }


}
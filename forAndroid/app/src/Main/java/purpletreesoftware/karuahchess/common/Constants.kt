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

import purpletreesoftware.karuahchess.R.color

class Constants {

    @ExperimentalUnsignedTypes
    companion object {

        const val BITMASK = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000UL

        // Black pieces
        const val BLACK_PAWN_SPIN: Int = -1
        const val BLACK_ROOK_SPIN: Int = -4
        const val BLACK_KNIGHT_SPIN: Int = -2
        const val BLACK_BISHOP_SPIN: Int = -3
        const val BLACK_QUEEN_SPIN: Int = -5
        const val BLACK_KING_SPIN: Int = -6

        // White Pieces
        const val WHITE_PAWN_SPIN: Int = 1
        const val WHITE_ROOK_SPIN: Int = 4
        const val WHITE_KNIGHT_SPIN: Int = 2
        const val WHITE_BISHOP_SPIN: Int = 3
        const val WHITE_QUEEN_SPIN: Int = 5
        const val WHITE_KING_SPIN: Int = 6

        // Colours
        const val WHITEPIECE = 1
        const val BLACKPIECE = -1

        // Node Limits
        const val NODELIMIT_STANDARD = 2000000
        const val NODELIMIT_HIGH = 100000000

        // Activity parameter name
        const val ACTIVITY_PARAMETER_NAME = "activityID"

        val FileDict = mapOf<String, IntArray>(
            "a" to intArrayOf( 0, 8, 16, 24, 32, 40, 48, 56 ),
            "b" to intArrayOf( 1, 9, 17, 25, 33, 41, 49, 57 ),
            "c" to intArrayOf( 2, 10, 18, 26, 34, 42, 50, 58 ),
            "d" to intArrayOf( 3, 11, 19, 27, 35, 43, 51, 59 ),
            "e" to intArrayOf( 4, 12, 20, 28, 36, 44, 52, 60 ),
            "f" to intArrayOf( 5, 13, 21, 29, 37, 45, 53, 61 ),
            "g" to intArrayOf( 6, 14, 22, 30, 38, 46, 54, 62 ),
            "h" to intArrayOf( 7, 15, 23, 31, 39, 47, 55, 63 )
        )


        val BoardCoordinateReverseDict = mapOf<String, Int>(
            "a8" to 0, "b8" to 1, "c8" to 2,"d8" to 3,"e8" to 4 ,"f8" to 5 ,"g8" to 6 ,"h8" to 7,
            "a7" to 8 , "b7" to 9, "c7" to 10,"d7" to 11,"e7" to 12,"f7" to 13,"g7" to 14,"h7" to 15,
            "a6" to 16, "b6" to 17, "c6" to 18,"d6" to 19,"e6" to 20,"f6" to 21,"g6" to 22,"h6" to 23,
            "a5" to 24, "b5" to 25, "c5" to 26,"d5" to 27,"e5" to 28,"f5" to 29,"g5" to 30,"h5" to 31,
            "a4" to 32, "b4" to 33, "c4" to 34,"d4" to 35,"e4" to 36,"f4" to 37,"g4" to 38,"h4" to 39,
            "a3" to 40, "b3" to 41, "c3" to 42,"d3" to 43,"e3" to 44,"f3" to 45,"g3" to 46,"h3" to 47,
            "a2" to 48, "b2" to 49, "c2" to 50,"d2" to 51,"e2" to 52,"f2" to 53,"g2" to 54,"h2" to 55,
            "a1" to 56, "b1" to 57, "c1" to 58,"d1" to 59,"e1" to 60,"f1" to 61,"g1" to 62,"h1" to 63
        )

        val BoardCoordinateDict = mapOf<Int, String>(
            0 to "a8" , 1 to "b8", 2 to "c8",3 to "d8",4 to "e8",5 to "f8", 6 to "g8",7 to "h8",
            8 to "a7" , 9 to "b7", 10 to "c7",11 to "d7",12 to "e7",13 to "f7",14 to "g7",15 to "h7",
            16 to "a6", 17 to "b6", 18 to "c6",19 to "d6", 20 to "e6",21 to "f6",22 to "g6",23 to "h6",
            24 to "a5", 25 to "b5", 26 to "c5",27 to "d5",28 to "e5",29 to "f5",30 to "g5",31 to "h5",
            32 to "a4", 33 to "b4", 34 to "c4",35 to "d4",36 to "e4",37 to "f4",38 to "g4",39 to "h4",
            40 to "a3", 41 to "b3", 42 to "c3",43 to "d3",44 to "e3",45 to "f3",46 to "g3",47 to "h3",
            48 to "a2", 49 to "b2", 50 to "c2",51 to "d2",52 to "e2",53 to "f2",54 to "g2",55 to "h2",
            56 to "a1", 57 to "b1", 58 to "c1",59 to "d1",60 to "e1",61 to "f1",62 to "g1",63 to "h1"
        )

        val RankDict = mapOf<String, IntArray>(
            "8" to intArrayOf( 0, 1, 2, 3, 4, 5, 6, 7 ),
            "7" to intArrayOf( 8, 9, 10, 11, 12, 13, 14, 15),
            "6" to intArrayOf( 16, 17, 18, 19, 20, 21, 22, 23),
            "5" to intArrayOf( 24, 25, 26, 27, 28, 29, 30, 31),
            "4" to intArrayOf( 32, 33, 34, 35, 36, 37, 38, 39),
            "3" to intArrayOf( 40, 41, 42, 43, 44, 45, 46, 47),
            "2" to intArrayOf( 48, 49, 50, 51, 52, 53, 54, 55),
            "1" to intArrayOf( 56, 57, 58, 59, 60, 61, 62, 63)
        )

        // Skill level
        val strengthList = arrayListOf(
            Strength("Beginner", -9, 5, 80),
            Strength("Level 2", -5 ,5, 100),
            Strength("Level 3", -1, 5, 200),
            Strength("Level 4", 1, 5, 300),
            Strength("Level 5", 2, 5, 400),
            Strength("Level 6", 3, 5, 500),
            Strength("Level 7", 5, 5, 600),
            Strength("Level 8", 7, 5, 700),
            Strength("Level 9", 8, 5, 1000),
            Strength("Level 10", 9, 6, 2000),
            Strength("Level 11", 10, 6, 2000),
            Strength("Level 12", 11, 7, 2000),
            Strength("Level 13", 12, 7, 2000),
            Strength("Level 14", 13, 8, 2000),
            Strength("Level 15", 14, 9, 4000),
            Strength("Level 16", 15, 10, 8000),
            Strength("Level 17", 16, 13, 10000),
            Strength("Level 18", 17, 15, 10000),
            Strength("Level 19", 18, 18, 10000),
            Strength("Level 20", 19, 20, 10000),
            Strength("Level 21",  20, 22, 10000));

        // Board square colour
        val darkSquareColourList = arrayListOf(
            ColourARGB(255,90,120,153, "Blue"),
            ColourARGB(255,222,184,135, "Brown"),
            ColourARGB(255,100,153,100, "Green"),
            ColourARGB(255,153,153,153, "Grey")
        )

        // Hint highlight colour for corresponding (by list index) board square colour
        val hintColourList = arrayListOf(
            color.colorMango,
            color.colorEmeraldGreen,
            color.colorMango,
            color.colorMango
        )


        val clockHour = arrayListOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10")
        val clockMinSec = arrayListOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                                        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                                        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                                        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                                        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                                        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59")

        val clockResetLabel = arrayListOf("1 min", "3 min", "5 min", "10 min", "15 min", "30 min", "45 min", "60 min", "90 min")
        val clockResetSeconds = arrayListOf(60, 180, 300, 600, 900, 1800, 2700, 3600, 5400)

        val moveSpeedSeconds: ArrayList<Double> = arrayListOf(2.0, 1.7, 1.4, 1.1, 0.8, 0.5, 0.3)
    }


}
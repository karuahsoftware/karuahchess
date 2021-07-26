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

package purpletreesoftware.karuahchess.common

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

        val eloarray = arrayListOf(1350, 1425, 1500, 1575, 1650, 1725, 1800, 1875, 1950, 2025, 2100, 2175, 2250, 2325, 2400, 2475, 2550, 2625, 2700, 2775, 2850)
        val strengthArrayLabel = arrayListOf(
            "Beginner",
            "Level 2, Elo 1425",
            "Level 3, Elo 1500",
            "Level 4, Elo 1575",
            "Level 5, Elo 1650",
            "Level 6, Elo 1725",
            "Level 7, Elo 1800",
            "Level 8, Elo 1875",
            "Level 9, Elo 1950",
            "Level 10, Elo 2025",
            "Level 11, Elo 2100",
            "Level 12, Elo 2175",
            "Level 13, Elo 2250",
            "Level 14, Elo 2325",
            "Level 15, Elo 2400",
            "Level 16, Elo 2475",
            "Level 17, Elo 2550",
            "Level 18, Elo 2625",
            "Level 19, Elo 2700",
            "Level 20, Elo 2775",
            "Level 21, Elo 2850")
    }
}
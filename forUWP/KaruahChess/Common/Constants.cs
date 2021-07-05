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

using System;
using System.Collections.Generic;

namespace KaruahChess.Common
{
    public static class Constants
    {
        public const UInt64 BITMASK = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000uL;

        public const int BLACK_PAWN_SPIN = -1;
        public const int BLACK_ROOK_SPIN = -4;
        public const int BLACK_KNIGHT_SPIN = -2;
        public const int BLACK_BISHOP_SPIN = -3;
        public const int BLACK_QUEEN_SPIN = -5;
        public const int BLACK_KING_SPIN = -6;


        // White Pieces
        public const int WHITE_PAWN_SPIN = 1;
        public const int WHITE_ROOK_SPIN = 4;
        public const int WHITE_KNIGHT_SPIN = 2;
        public const int WHITE_BISHOP_SPIN = 3;
        public const int WHITE_QUEEN_SPIN = 5;
        public const int WHITE_KING_SPIN = 6;


        // Colours
        public const int WHITEPIECE = 1;
        public const int BLACKPIECE = -1;

        public static List<int> eloList = new List<int> { 1350, 1425, 1500, 1575, 1650, 1725, 1800, 1875, 1950, 2025, 2100, 2175, 2250, 2325, 2400, 2475, 2550, 2625, 2700, 2775, 2850 };
        public static List<string> strengthLabelList = new List<String> {
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
            "Level 21, Elo 2850"};


    }
}

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
using Windows.UI;
using Windows.UI.Xaml.Media;

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

        // Skill level       
        public static List<string> skillLevelList = new List<string> {
            "Beginner",
            "Level 2",
            "Level 3",
            "Level 4",
            "Level 5",
            "Level 6",
            "Level 7",
            "Level 8",
            "Level 9",
            "Level 10",
            "Level 11",
            "Level 12",
            "Level 13",
            "Level 14",
            "Level 15",
            "Level 16",
            "Level 17",
            "Level 18",
            "Level 19",
            "Level 20",
            "Level 21"};

        
        // Colours in the format alpha, red, green, blue
        public static List<ColourARGB> darkSquareColourList = new List<ColourARGB> {
            new ColourARGB(255,90,120,153, "Blue"),
            new ColourARGB(255,222,184,135, "Brown"),
            new ColourARGB(255,100,153,100, "Green"),
            new ColourARGB(255,153,153,153, "Grey")
            };

        public static List<SolidColorBrush> hintColourList = new List<SolidColorBrush> {
            new SolidColorBrush(Color.FromArgb(255,244,187,68)),
            new SolidColorBrush(Color.FromArgb(255,3,166,90)),
            new SolidColorBrush(Color.FromArgb(255,244,187,68)),
            new SolidColorBrush(Color.FromArgb(255,244,187,68))
            };

        public static List<string> clockResetLabel = new List<string> { "1 min", "3 min", "5 min", "10 min", "15 min", "30 min", "45 min", "60 min", "90 min" };
        public static List<int> clockResetSeconds = new List<int> { 60, 180, 300, 600, 900, 1800, 2700, 3600, 5400 };
        public static List<string> clockHour = new List<string> { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10" };
        public static List<string> clockMinSec = new List<string> { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                                                                    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                                                                    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                                                                    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                                                                    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                                                                    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};
    }
}

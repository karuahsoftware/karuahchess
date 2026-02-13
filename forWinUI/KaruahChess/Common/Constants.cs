/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2026 Karuah Software

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
using Microsoft.UI.Xaml.Media;

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

        // Node Limits
        public const int NODELIMIT_STANDARD = 2000000;
        public const int NODELIMIT_HIGH = 100000000;

        // Skill level       
        public static List<Strength> strengthList = new List<Strength> {
            new Strength("Beginner", -9, 5, 80),
            new Strength("Level 2", -5 ,5, 100),
            new Strength("Level 3", -1, 5, 200),
            new Strength("Level 4", 1, 5, 300),
            new Strength("Level 5", 2, 5, 400),
            new Strength("Level 6", 3, 5, 500),
            new Strength("Level 7", 5, 5, 600),
            new Strength("Level 8", 7, 5, 700),
            new Strength("Level 9", 8, 5, 1000),
            new Strength("Level 10", 9, 6, 2000),
            new Strength("Level 11", 10, 6, 2000),
            new Strength("Level 12", 11, 7, 2000),
            new Strength("Level 13", 12, 7, 2000),
            new Strength("Level 14", 13, 8, 2000),
            new Strength("Level 15", 14, 9, 4000),
            new Strength("Level 16", 15, 10, 8000),
            new Strength("Level 17", 16, 13, 10000),
            new Strength("Level 18", 17, 15, 10000),
            new Strength("Level 19", 18, 18, 10000),
            new Strength("Level 20", 19, 20, 10000),
            new Strength("Level 21",  20, 22, 10000)};

        
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

        public static List<double> movespeedseconds = new List<double> { 2, 1.7, 1.4, 1.1, 0.8, 0.5, 0.3 };
    }
}

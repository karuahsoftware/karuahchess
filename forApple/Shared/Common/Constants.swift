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

import SQLite3
import SwiftUI

class Constants  {
    
    
    static let SQLITE_STATIC = unsafeBitCast(0, to: sqlite3_destructor_type.self)
    static let SQLITE_TRANSIENT = unsafeBitCast(-1, to: sqlite3_destructor_type.self)
    static let TOAST_SHORT = 2000
    static let TOAST_LONG = 3500
    static let TOAST_EXTRALONG = 8000
    
    static let BITMASK: UInt64 = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000
    
    // Black pieces
    static let BLACK_PAWN_SPIN: Int = -1
    static let BLACK_ROOK_SPIN: Int = -4
    static let BLACK_KNIGHT_SPIN: Int = -2
    static let BLACK_BISHOP_SPIN: Int = -3
    static let BLACK_QUEEN_SPIN: Int = -5
    static let BLACK_KING_SPIN: Int = -6
    
    // White Pieces
    static let WHITE_PAWN_SPIN: Int = 1
    static let WHITE_ROOK_SPIN: Int = 4
    static let WHITE_KNIGHT_SPIN: Int = 2
    static let WHITE_BISHOP_SPIN: Int = 3
    static let WHITE_QUEEN_SPIN: Int = 5
    static let WHITE_KING_SPIN: Int = 6
    
    // Colours
    static let WHITEPIECE = 1
    static let BLACKPIECE = -1
    
    // Node Limits
    static let NODELIMIT_STANDARD = 2000000
    static let NODELIMIT_HIGH = 100000000
    
    static let FileDict : [String: [Int]] =
        ["a" : [ 0, 8, 16, 24, 32, 40, 48, 56 ],
        "b" : [ 1, 9, 17, 25, 33, 41, 49, 57 ],
        "c" : [ 2, 10, 18, 26, 34, 42, 50, 58 ],
        "d" : [ 3, 11, 19, 27, 35, 43, 51, 59 ],
        "e" : [ 4, 12, 20, 28, 36, 44, 52, 60 ],
        "f" : [ 5, 13, 21, 29, 37, 45, 53, 61 ],
        "g" : [ 6, 14, 22, 30, 38, 46, 54, 62 ],
        "h" : [ 7, 15, 23, 31, 39, 47, 55, 63 ]]

    static let BoardCoordinateReverseDict : [String: Int] =
        ["a8" : 0, "b8" : 1, "c8" : 2,"d8" : 3,"e8" : 4 ,"f8" : 5 ,"g8" : 6 ,"h8" : 7,
        "a7" : 8 , "b7" : 9, "c7" : 10,"d7" : 11,"e7" : 12,"f7" : 13,"g7" : 14,"h7" : 15,
        "a6" : 16, "b6" : 17, "c6" : 18,"d6" : 19,"e6" : 20,"f6" : 21,"g6" : 22,"h6" : 23,
        "a5" : 24, "b5" : 25, "c5" : 26,"d5" : 27,"e5" : 28,"f5" : 29,"g5" : 30,"h5" : 31,
        "a4" : 32, "b4" : 33, "c4" : 34,"d4" : 35,"e4" : 36,"f4" : 37,"g4" : 38,"h4" : 39,
        "a3" : 40, "b3" : 41, "c3" : 42,"d3" : 43,"e3" : 44,"f3" : 45,"g3" : 46,"h3" : 47,
        "a2" : 48, "b2" : 49, "c2" : 50,"d2" : 51,"e2" : 52,"f2" : 53,"g2" : 54,"h2" : 55,
        "a1" : 56, "b1" : 57, "c1" : 58,"d1" : 59,"e1" : 60,"f1" : 61,"g1" : 62,"h1" : 63]
    

    static let BoardCoordinateDict : [Int: String] =
        [0 : "a8" , 1 : "b8", 2 : "c8",3 : "d8",4 : "e8",5 : "f8", 6 : "g8",7 : "h8",
        8 : "a7" , 9 : "b7", 10 : "c7",11 : "d7",12 : "e7",13 : "f7",14 : "g7",15 : "h7",
        16 : "a6", 17 : "b6", 18 : "c6",19 : "d6", 20 : "e6",21 : "f6",22 : "g6",23 : "h6",
        24 : "a5", 25 : "b5", 26 : "c5",27 : "d5",28 : "e5",29 : "f5",30 : "g5",31 : "h5",
        32 : "a4", 33 : "b4", 34 : "c4",35 : "d4",36 : "e4",37 : "f4",38 : "g4",39 : "h4",
        40 : "a3", 41 : "b3", 42 : "c3",43 : "d3",44 : "e3",45 : "f3",46 : "g3",47 : "h3",
        48 : "a2", 49 : "b2", 50 : "c2",51 : "d2",52 : "e2",53 : "f2",54 : "g2",55 : "h2",
        56 : "a1", 57 : "b1", 58 : "c1",59 : "d1",60 : "e1",61 : "f1",62 : "g1",63 : "h1"]
    
    static let RankDict : [String: [Int]] =
        ["8" : [0, 1, 2, 3, 4, 5, 6, 7],
         "7" : [8, 9, 10, 11, 12, 13, 14, 15],
         "6" : [16, 17, 18, 19, 20, 21, 22, 23],
         "5" : [24, 25, 26, 27, 28, 29, 30, 31],
         "4" : [32, 33, 34, 35, 36, 37, 38, 39],
         "3" : [40, 41, 42, 43, 44, 45, 46, 47],
         "2" : [48, 49, 50, 51, 52, 53, 54, 55],
         "1" : [56, 57, 58, 59, 60, 61, 62, 63]]
           
    
    static let strengthList: [Strength] =
    [Strength(label: "Beginner", skillLevel: -9, depth: 5, timeLimitms: 80),
     Strength(label:"Level 2", skillLevel: -5 ,depth: 5, timeLimitms: 100),
     Strength(label:"Level 3", skillLevel: -1, depth: 5, timeLimitms: 200),
     Strength(label:"Level 4", skillLevel: 1, depth: 5, timeLimitms: 300),
     Strength(label:"Level 5", skillLevel: 2, depth: 5, timeLimitms: 400),
     Strength(label:"Level 6", skillLevel: 3, depth: 5, timeLimitms: 500),
     Strength(label:"Level 7", skillLevel: 5, depth: 5, timeLimitms: 600),
     Strength(label:"Level 8", skillLevel: 7, depth: 5, timeLimitms: 700),
     Strength(label:"Level 9", skillLevel: 8, depth: 5, timeLimitms: 1000),
     Strength(label:"Level 10", skillLevel: 9, depth: 6, timeLimitms: 2000),
     Strength(label:"Level 11", skillLevel: 10, depth: 6, timeLimitms: 2000),
     Strength(label:"Level 12", skillLevel: 11, depth: 7, timeLimitms: 2000),
     Strength(label:"Level 13", skillLevel: 12, depth: 7, timeLimitms: 2000),
     Strength(label:"Level 14", skillLevel: 13, depth: 8, timeLimitms: 2000),
     Strength(label:"Level 15", skillLevel: 14, depth: 9, timeLimitms: 4000),
     Strength(label:"Level 17", skillLevel: 16, depth: 13, timeLimitms: 10000),
     Strength(label:"Level 18", skillLevel: 17, depth: 15, timeLimitms: 10000),
     Strength(label:"Level 19", skillLevel: 18, depth: 18, timeLimitms: 10000),
     Strength(label:"Level 20", skillLevel: 19, depth: 20, timeLimitms: 10000),
     Strength(label:"Level 21",  skillLevel: 20, depth: 22, timeLimitms: 10000)]
    
    static let darkSquareColourArray: [ColourARGB] =
    [ColourARGB(a: 255, r: 90, g: 120, b: 153, text: "Blue"),
     ColourARGB(a: 255, r: 222, g: 184, b: 135, text: "Brown"),
     ColourARGB(a: 255, r: 100, g: 153, b: 100, text: "Green"),
     ColourARGB(a: 255, r: 153, g: 153, b: 153, text: "Grey")]
    
    static let hintColourArray: [Color] =
    [Color(red: 244 / 255, green: 187 / 255, blue: 68 / 255),
     Color(red: 3 / 255, green: 166 / 255, blue: 90 / 255),
     Color(red: 244 / 255, green: 187 / 255, blue: 68 / 255),
     Color(red: 244 / 255, green: 187 / 255, blue: 68 / 255)]
    
    static let moveSpeedSeconds: [Double] = [2.0, 1.7, 1.4, 1.1, 0.8, 0.5, 0.3]
}

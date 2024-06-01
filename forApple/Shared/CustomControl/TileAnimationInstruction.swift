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

import SwiftUI

struct TileAnimationInstruction: Identifiable  {
    let id = UUID()
    
    enum AnimationTypeEnum: Int {case Move = 0, Take = 1, Put = 2, Fall = 3, MoveFade = 4}
    
    // Type
    var animationType: AnimationTypeEnum
    
    // Image
    let imageData: Image
    
    // Move from to point
    let moveFrom: CGPoint
    let moveTo: CGPoint
    
    // List of indexes that should be hidden for the animation
    let hiddenSquareIndexes: [Int]
    
    
}

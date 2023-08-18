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

struct TileShakeEffect: GeometryEffect {
    
    var complete: CGFloat
        
    var animatableData: CGFloat {
        get { complete }
        set { complete = newValue }
    }
    
    func effectValue(size: CGSize) -> ProjectionTransform {
        if complete > 0 {
        let angle = (CGFloat.pi * complete * 0.057 - 0.087) // -5 to +5 degrees
        
        let affineTransform = CGAffineTransform(translationX: size.width * 0.5, y: size.height * 0.5)
            .rotated(by: angle)
            .translatedBy(x: -size.width * 0.5, y: -size.height * 0.5)
            
            
            return ProjectionTransform(affineTransform)
            
        }
        else {
        
            return ProjectionTransform()
        }
    }
    

}

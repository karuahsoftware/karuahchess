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

struct TileAnimationTakeModifier: AnimatableModifier {
    var complete: CGFloat
    
    nonisolated var animatableData: CGFloat {
        get {complete}
        set {
            complete = newValue
        }
    }
    
    func body(content: Content) -> some View {
        let transform = CGAffineTransform(translationX: complete * Device.instance.tileSize * 0.3, y: -complete * Device.instance.tileSize * 0.3)
        return content.opacity(1 - Double(complete)).transformEffect(transform)
    }

}

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

struct TileAnimationPutModifier: AnimatableModifier {
    @ObservedObject private var device : Device = Device.instance
    var complete: CGFloat
    
    nonisolated var animatableData: CGFloat {
        get {complete}
        set {
            complete = newValue
        }
    }
    
    func body(content: Content) -> some View {
        let transform = CGAffineTransform(translationX: (1 - complete) * device.tileSize * 0.3, y: -(1 - complete) * device.tileSize * 0.3)
        return content.opacity(Double(complete)).transformEffect(transform)
    }

}

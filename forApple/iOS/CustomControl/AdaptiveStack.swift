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

struct AdaptiveStack<Content: View>: View {
    @ObservedObject var device : Device = Device.instance
    let horizontalAlignment: HorizontalAlignment
    let verticalAlignment: VerticalAlignment
    let spacing: CGFloat?
    let content: () -> Content
    let reverse: Bool
    
    init(reverse: Bool = false , horizontalAlignment: HorizontalAlignment = .center, verticalAlignment: VerticalAlignment = .center, spacing: CGFloat? = nil, @ViewBuilder content: @escaping () -> Content) {
        self.horizontalAlignment = horizontalAlignment
        self.verticalAlignment = verticalAlignment
        self.spacing = spacing
        self.content = content
        self.reverse = reverse
    }
    
    var body: some View {
                
        if device.isLandScape && !reverse {
            VStack(alignment: horizontalAlignment, spacing: spacing, content: content)
        }
        else if device.isLandScape && reverse {
            HStack(alignment: verticalAlignment, spacing: spacing, content: content)
        }
        else if !device.isLandScape && !reverse{
            HStack(alignment: verticalAlignment, spacing: spacing, content: content)
        }
        else {
            VStack(alignment: horizontalAlignment, spacing: spacing, content: content)
        }
           
        }
 
}

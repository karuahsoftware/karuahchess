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

struct TilePanelView: View {
    
    
    @ObservedObject var tilePanelVM : TilePanelViewModel
    
    /// View
    var body: some View {
        
        
        VStack(spacing: 0) {
            ForEach(0 ..< 8) {row in
                HStack(spacing: 0){
                    ForEach(0 ..< 8) {col -> TileView in
                        tilePanelVM.getTile(pIndex: (row * 8 + col))
                    }
                }
            }
        }.onPreferenceChange(TileFrameBoardPreferenceKey.self){preferences in
            for p in preferences {
                Task {@MainActor in
                    tilePanelVM.tileList[p.tileIndex].tileVM.boardFrame = p.tileFrame
                }
            }
        }
        .onPreferenceChange(TileFrameMainPreferenceKey.self){preferences in
            for p in preferences {
                Task {@MainActor in
                    tilePanelVM.tileList[p.tileIndex].tileVM.mainFrame = p.tileFrame
                }
            }
        }
    }
}




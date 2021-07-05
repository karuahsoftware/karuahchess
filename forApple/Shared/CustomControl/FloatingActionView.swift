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

import SwiftUI

struct FloatingActionView: View {

    @StateObject private var device : Device = Device.shared
   
    
    var body: some View {
        
        AdaptiveStack(reverse: true, horizontalAlignment: .trailing, verticalAlignment: .bottom, spacing: 0) {
            [self] in
            
            Spacer()
            AdaptiveStack(reverse: false, horizontalAlignment: .trailing, verticalAlignment: .bottom, spacing: 0) {
                Spacer()
                
                Button(action: {
                    BoardViewModel.shared.rotateClick()
                       }){
                    Image(systemName: "arrow.clockwise")
                        .resizable()
                        .padding()
                        .aspectRatio(contentMode: .fill)
                        .background(Color("ActionGreen"))
                        .foregroundColor(Color.black)
                        .clipShape(Circle())
                        .shadow(radius: 8)
                        .frame(width: calcButtonSize(device.tileSize), height: calcButtonSize(device.tileSize))
                }
                .padding(8)
                
                Button(action: {
                    BoardViewModel.shared.showLastMove()
                       }){
                    Image(systemName: "eye.fill")
                        .resizable()
                        .padding()
                        .aspectRatio(contentMode: .fill)
                        .background(Color("ActionGreen"))
                        .foregroundColor(Color.black)
                        .clipShape(Circle())
                        .shadow(radius: 8)
                        .frame(width: calcButtonSize(device.tileSize), height: calcButtonSize(device.tileSize))
                }
                .padding(8)
            }.padding()
            
            
        }
        
    }
    
    
    
     
    /// Caclulates the size of the action button
    /// - Parameter pTileSize: Size of a board tile
    func calcButtonSize(_ pTileSize: CGFloat) -> CGFloat {
        if pTileSize < 50 {
            return 50
        }
        else if pTileSize > 70 {
            return 70
        }
        else {
            return pTileSize
        }
    }
}







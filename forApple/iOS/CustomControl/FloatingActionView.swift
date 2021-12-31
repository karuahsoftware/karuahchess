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

    @ObservedObject private var device : Device = Device.instance
    @ObservedObject private var engineSettingsVM : EngineSettingsViewModel = EngineSettingsViewModel.instance
    @ObservedObject private var menuSettingsVM : MenuSettingsViewModel = MenuSettingsViewModel.instance
    @Binding var showMenu: Bool
    
    var body: some View {
        
        AdaptiveStack(reverse: true, horizontalAlignment: .trailing, verticalAlignment: .bottom, spacing: 12) {
            
            
            Spacer()
            AdaptiveStack(reverse: false, horizontalAlignment: .trailing, verticalAlignment: .bottom, spacing: 12) {
                Spacer()
                
                Button(action: {
                    showMenu = true
                    menuSettingsVM.isShowingEngineSettings = true
                    
                       }){
                           HStack(spacing: 0) {
                            Image(systemName: "gear")
                                .resizable()
                                .padding()
                                .aspectRatio(contentMode: .fit)
                            Text("\(engineSettingsVM.value.limitEngineStrengthELOIndex + 1)")
                                .padding(.trailing)
                        }.background(Color("ActionGreen"))
                         .foregroundColor(Color.black)
                         .clipShape(Capsule())
                         .shadow(radius: 8)
                         .frame(width: calcButtonSize(device.tileSize) * 1.8, height: calcButtonSize(device.tileSize) * 1.1)
                }
                
                
                Button(action: {
                    BoardViewModel.instance.rotateClick()
                       }){
                            Image(systemName: "arrow.clockwise")
                                .resizable()
                                .padding()
                                .aspectRatio(contentMode: .fill)
                                .background(Color("ActionGreen"))
                                .foregroundColor(Color.black)
                                .clipShape(Circle())
                                .shadow(radius: 8)
                                .frame(width: calcButtonSize(device.tileSize) * 1.1, height: calcButtonSize(device.tileSize) * 1.1)
                       }
                
                Button(action: {
                    BoardViewModel.instance.showLastMove()
                       }){
                         Image(systemName: "eye")
                             .resizable()
                             .padding()
                             .aspectRatio(contentMode: .fill)
                             .background(Color("ActionGreen"))
                             .foregroundColor(Color.black)
                             .clipShape(Circle())
                             .shadow(radius: 8)
                             .frame(width: calcButtonSize(device.tileSize) * 1.1, height: calcButtonSize(device.tileSize) * 1.1)
                       }
                
                
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







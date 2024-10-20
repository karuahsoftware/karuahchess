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

struct FloatingActionView: View {
    @State private var showingNewGameAlert = false
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject private var hintSettingsVM : HintSettingsViewModel = HintSettingsViewModel.instance
    @ObservedObject private var menuSettingsVM : MenuSettingsViewModel = MenuSettingsViewModel.instance
    
    @Binding var showMenu: Bool
    
    var body: some View {
        
        AdaptiveStack(reverse: true, horizontalAlignment: .trailing, verticalAlignment: .bottom, spacing: 12) {
            
            
            Spacer()
            AdaptiveStack(reverse: false, horizontalAlignment: .trailing, verticalAlignment: .bottom, spacing: 12) {
                Spacer()
                
                
                if !menuSettingsVM.arrangeBoardEnabled {
                    Button(action: {
                        self.showingNewGameAlert = true
                    }){
                        Image(systemName: "target")
                            .resizable()
                            .padding()
                            .aspectRatio(contentMode: .fill)
                            .background(Color("ActionGreen"))
                            .foregroundColor(Color.black)
                            .clipShape(Circle())
                            .shadow(radius: 8)
                            .frame(width: calcButtonSize(device.tileSize) * 1.1, height: calcButtonSize(device.tileSize) * 1.1)
                    }.alert(isPresented: $showingNewGameAlert) {
                        Alert(title: Text("New"), message: Text("Start a new game?"), primaryButton: .default(Text("Yes")) {
                            Task(priority: .userInitiated)  {
                                self.showMenu = false
                                await BoardViewModel.instance.newGame()
                            }
                        } , secondaryButton: .cancel())
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
                    
                    if hintSettingsVM.hintEnabled {
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await BoardViewModel.instance.showHint()
                            }
                        }){
                            Image(systemName: "lightbulb")
                                .resizable()
                                .padding()
                                .aspectRatio(contentMode: .fill)
                                .background(Color("ActionGreen"))
                                .foregroundColor(Color.black)
                                .clipShape(Circle())
                                .shadow(radius: 8)
                                .frame(width: calcButtonSize(device.tileSize) * 1.1, height: calcButtonSize(device.tileSize) * 1.1)
                        }
                    }
                }
                else {
                    Button(action: {
                        BoardViewModel.instance.pieceEditSelectVM.show()
                    }){
                        Image(systemName: "person")
                            .resizable()
                            .padding()
                            .aspectRatio(contentMode: .fill)
                            .background(Color("ActionMagenta"))
                            .foregroundColor(Color.white)
                            .clipShape(Circle())
                            .shadow(radius: 8)
                            .frame(width: calcButtonSize(device.tileSize) * 1.1, height: calcButtonSize(device.tileSize) * 1.1)
                    }
                    
                    Button(action: {
                        Task(priority: .userInitiated) {
                            await BoardViewModel.instance.editEraseSelection()
                        }
                    }){
                        Image(systemName: "trash.fill")
                            .resizable()
                            .padding()
                            .aspectRatio(contentMode: .fill)
                            .background(Color("ActionMagenta"))
                            .foregroundColor(Color.white)
                            .clipShape(Circle())
                            .shadow(radius: 8)
                            .frame(width: calcButtonSize(device.tileSize) * 1.1, height: calcButtonSize(device.tileSize) * 1.1)
                    }
                    
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







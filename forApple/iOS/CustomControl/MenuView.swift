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

struct MenuView: View {
    @State private var showingNewGameAlert = false
    @State private var showingResignAlert = false
    @Binding var showMenu: Bool
    @ObservedObject var menuSettingsVM : MenuSettingsViewModel = MenuSettingsViewModel.instance
    
    
    var body: some View {
        Form {
            
                Section {
                    Button(action: {
                        self.showingNewGameAlert = true
                    }){
                        Label() {
                            Text("New Game")
                        } icon: {
                            Image(systemName: "target")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.green)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                            
                        
                    }
                    .alert(isPresented: $showingNewGameAlert) {
                        Alert(title: Text("New"), message: Text("Start a new game?"), primaryButton: .destructive(Text("Yes")) {
                            Task(priority: .userInitiated)  {
                                self.showMenu = false
                                await BoardViewModel.instance.newGame()
                            }
                        } , secondaryButton: .cancel())
                    }
         
                    
                    
                    Button(action: {
                        self.showingResignAlert = true
                    }){
                        Label() {
                            Text("Resign")
                        } icon: {
                            Image(systemName: "flag")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.orange)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                    .alert(isPresented: $showingResignAlert) {
                        Alert(title: Text("Resign"), message: Text("Resign from current game?"), primaryButton: .destructive(Text("Yes")) {
                            Task(priority: .userInitiated) {
                                self.showMenu = false
                                await BoardViewModel.instance.resignGame()
                            }
                        } , secondaryButton: .cancel())
                    }
                    
                    
                    NavigationLink(destination: EngineSettings(pShowMenu: $showMenu), isActive: $menuSettingsVM.isShowingEngineSettings) {
                        Label() {
                            Text("Engine Settings")
                        } icon: {
                            Image(systemName: "gear")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.gray)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                    
                }
                   
                Section {
                    
                    Toggle(isOn: $menuSettingsVM.coordinatesEnabled) {
                        Label() {
                            Text("Coordinates")
                        } icon: {
                            Image(systemName: "globe")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.blue)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                    
                    Toggle(isOn: $menuSettingsVM.moveHighlightEnabled) {
                        Label() {
                            Text("Highlight Moves")
                        } icon: {
                            Image(systemName: "circle.hexagonpath")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.blue)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                    
                    Toggle(isOn: $menuSettingsVM.navigatorEnabled) {
                        Label() {
                            Text("Navigator")
                        } icon: {
                            Image(systemName: "play")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.blue)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                            
                    }
                    
                    Toggle(isOn: $menuSettingsVM.hintEnabled) {
                        Label() {
                            Text("Hint Button")
                        } icon: {
                            Image(systemName: "lightbulb")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.orange)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                            
                    }
                }
            
                Section {
                    NavigationLink(destination: BoardSettings(showMenu: $showMenu)) {
                        Label() {
                            Text("Board")
                        } icon: {
                            Image(systemName: "square.grid.3x3.square")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.green)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                    
                    NavigationLink(destination: PieceSettings(showMenu: $showMenu)) {
                        Label() {
                            Text("Piece")
                        } icon: {
                            Image(systemName: "person")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.green)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                    
                    NavigationLink(destination: SoundSettings(showMenu: $showMenu)) {
                        Label() {
                            Text("Sound")
                        } icon: {
                            Image(systemName: "music.note")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.indigo)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                    }
                }
                
                    
                Section {
                    Toggle(isOn: $menuSettingsVM.arrangeBoardEnabled) {
                        Label() {
                            Text("Edit")
                        } icon: {
                            Image(systemName: "pencil")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .font(Font.system(.headline))
                                .padding(3)
                                .frame(width: 28, height: 28)
                                .background(Color.brown)
                                .foregroundColor(Color.white)
                                .cornerRadius(6)
                        }
                            
                    }
                    
                    Button(action: {
                           Task(priority: .userInitiated) {
                              self.showMenu = false
                              await BoardViewModel.instance.undoMove()
                           }
                       }){
                           Label() {
                               Text("Undo")
                           } icon: {
                               Image(systemName: "arrow.uturn.left")
                                   .resizable()
                                   .aspectRatio(contentMode: .fit)
                                   .font(Font.system(.headline))
                                   .padding(3)
                                   .frame(width: 28, height: 28)
                                   .background(Color.pink)
                                   .foregroundColor(Color.white)
                                   .cornerRadius(6)
                           }
                           
                       }
                                             
                                               
                    Button(action: {
                            Task(priority: .userInitiated) {
                                self.showMenu = false
                                await BoardViewModel.instance.switchDirection()
                               }
                           }){
                               Label() {
                                   Text("Switch Direction")
                               } icon: {
                                   Image(systemName: "arrow.up.arrow.down")
                                       .resizable()
                                       .aspectRatio(contentMode: .fit)
                                       .font(Font.system(.headline))
                                       .padding(3)
                                       .frame(width: 28, height: 28)
                                       .background(Color.cyan)
                                       .foregroundColor(Color.white)
                                       .cornerRadius(6)
                               }
                               
                           }
                    
                    }

                
            Section {
                Button(action: {
                               self.showMenu = false
                               BoardViewModel.instance.showFileImporter = true
                        
                           }){
                               Label() {
                                   Text("Load Game")
                               } icon: {
                                   Image(systemName: "doc")
                                       .resizable()
                                       .aspectRatio(contentMode: .fit)
                                       .font(Font.system(.headline))
                                       .padding(3)
                                       .frame(width: 28, height: 28)
                                       .background(Color.black)
                                       .foregroundColor(Color.white)
                                       .cornerRadius(6)
                               }
                           }
                
                    Button(action: {
                                Task(priority: .userInitiated) {[self] in
                                    showMenu = false
                                    await BoardViewModel.instance.saveGame()
                                }
                              
                           }){
                               HStack {
                                   Label() {
                                       Text("Save Game")
                                   } icon: {
                                       Image(systemName: "opticaldiscdrive")
                                           .resizable()
                                           .aspectRatio(contentMode: .fit)
                                           .font(Font.system(.headline))
                                           .padding(3)
                                           .frame(width: 28, height: 28)
                                           .background(Color.black)
                                           .foregroundColor(Color.white)
                                           .cornerRadius(6)
                                   }
                               }
                           }
                
            }
            
            
            Section {
                NavigationLink(destination: AboutView(showMenu: $showMenu)) {
                    Label() {
                        Text("About")
                    } icon: {
                        Image(systemName: "questionmark")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .font(Font.system(.headline))
                            .padding(3)
                            .frame(width: 28, height: 28)
                            .background(Color.yellow)
                            .foregroundColor(Color.white)
                            .cornerRadius(6)
                    }
                }
            }
            
            
                
        }
    
    }
     
}

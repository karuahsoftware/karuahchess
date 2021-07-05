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

struct MenuView: View {
    @State private var showingNewGameAlert = false
    @State private var showingResignAlert = false
    @Binding var showMenu: Bool
    @ObservedObject var menuSettingsVM : MenuSettingsViewModel = MenuSettingsViewModel()
    
    
    var body: some View {
        
        ScrollView(.vertical) {
        VStack(alignment: .leading) {
            
                Group {
                    Button(action: {
                        self.showingNewGameAlert = true
                    }){
                        Image(systemName: "plus")
                        .foregroundColor(Color(.label))
                        .imageScale(.large)
                        Text("New Game")
                        .foregroundColor(Color(.label))
                        .font(.body)
                    }
                    .padding(.top, 10)
                    .alert(isPresented: $showingNewGameAlert) {
                        Alert(title: Text("New"), message: Text("Start a new game?"), primaryButton: .destructive(Text("Yes")) {
                            self.showMenu = false
                            BoardViewModel.shared.newGame()
                        } , secondaryButton: .cancel())
                    }
         
                    
                    
                    Button(action: {
                        self.showingResignAlert = true
                    }){
                        Image(systemName: "flag")
                        .foregroundColor(Color(.label))
                        .imageScale(.large)
                        Text("Resign")
                        .foregroundColor(Color(.label))
                        .font(.body)
                    }
                    .padding(.top, 10)
                    .alert(isPresented: $showingResignAlert) {
                        Alert(title: Text("Resign"), message: Text("Resign from current game?"), primaryButton: .destructive(Text("Yes")) {
                            self.showMenu = false
                            BoardViewModel.shared.resignGame()
                        } , secondaryButton: .cancel())
                    }
                    
                    
                    NavigationLink(destination: EngineSettings(showMenu: $showMenu)) {
                        Image(systemName: "gear")
                        .foregroundColor(Color(.label))
                        .imageScale(.large)
                        Text("Engine Settings")
                        .foregroundColor(Color(.label))
                        .font(.body)
                    }.padding(.top, 10)
                    
                    Divider()
                    
                }
                   
                Group {
                    Toggle(isOn: $menuSettingsVM.value.moveHighlightEnabled) {
                        
                        Button(action: {
                            menuSettingsVM.value.moveHighlightEnabled.toggle()
                            
                               }){
                            Image(systemName: "target")
                            .foregroundColor(Color(.label))
                            .imageScale(.large)
                            
                            Text("Highlight Moves")
                                .font(.body)
                                .foregroundColor(Color(.label))
                               }
                               .padding(.top, 10)
                    }
                    
                    Divider()
                }
                
                Group {
                    Toggle(isOn: $menuSettingsVM.value.soundEnabled) {
                        
                        Button(action: {
                            menuSettingsVM.value.soundEnabled.toggle()
                            
                               }){
                            Image(systemName: "speaker.wave.2")
                            .foregroundColor(Color(.label))
                            .imageScale(.large)
                            
                            Text("Sound")
                                .font(.body)
                                .foregroundColor(Color(.label))
                               }
                               .padding(.top, 10)
                    }
                        
                
                    Divider()
                
                }
                
                Group {
                    Toggle(isOn: $menuSettingsVM.value.arrangeBoardEnabled) {
                        
                        Button(action: {
                            menuSettingsVM.value.arrangeBoardEnabled.toggle()
                            
                               }){
                            Image(systemName: "pencil")
                            .foregroundColor(Color(.label))
                            .imageScale(.large)
                            
                            Text("Edit")
                                .font(.body)
                                .foregroundColor(Color(.label))
                               }
                               .padding(.top, 10)
                    }
                        
                    
                    Button(action: {
                               self.showMenu = false
                               BoardViewModel.shared.undoMove()
                           }){
                               Image(systemName: "arrow.uturn.left")
                               .foregroundColor(Color(.label))
                               .imageScale(.large)
                               Text("Undo")
                                .foregroundColor(Color(.label))
                               .font(.body)
                           }
                           .padding(.top, 10)
                     
                       
                    Button(action: {
                               self.showMenu = false
                               BoardViewModel.shared.switchDirection()
                           }){
                               Image(systemName: "arrow.up.arrow.down")
                               .foregroundColor(Color(.label))
                               .imageScale(.large)
                               Text("Switch Direction")
                                .foregroundColor(Color(.label))
                               .font(.body)
                           }
                           .padding(.top, 10)
                    
                        Divider()
                    }
                    
                    Group {
                        NavigationLink(destination: AboutView(showMenu: $showMenu)) {
                            Image(systemName: "questionmark")
                            .foregroundColor(Color(.label))
                            .imageScale(.large)
                                .font(.system(size: 16))
                            Text("About")
                            .foregroundColor(Color(.label))
                            .font(.body)
                        }.padding(.top, 10)
                    }
                
                }
                Spacer()
            }
            .padding()
            .frame(maxWidth: 428, alignment: .leading)
            .background(Color(.systemGray6))
        
        
    }
}

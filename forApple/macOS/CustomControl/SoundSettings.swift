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

struct SoundSettings: View {
    
    @ObservedObject private var soundSettingsVM : SoundSettingsViewModel = SoundSettingsViewModel.instance
    private let menuSheet : MenuSheet
    
    // Initialisation
    init(pMenuSheet: MenuSheet) {
        menuSheet = pMenuSheet
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            ScrollView([.vertical]) {
                VStack(alignment: .leading) {
                    HStack {
                        Image(systemName: "music.note")
                        .imageScale(.large)
                        Text("Sound").font(.headline)
                    }.padding(.bottom, 10)
                    
                    Toggle(isOn: $soundSettingsVM.soundReadEnabled) {
                        Text("Read messages out loud")
                            .font(.body)
                    }
                    
                    Toggle(isOn: $soundSettingsVM.soundEffectEnabled) {
                        Text("Piece move sound")
                            .font(.body)
                    }
                    
                    Spacer().frame(maxWidth: .infinity)
                    
                }.frame(minWidth: 200, maxWidth: .infinity, minHeight: 50, maxHeight: .infinity)
                
            } // Scrollview
            
            Divider()
            
            HStack {
                Button(action: {
                    menuSheet.active = nil
                }){
                    Text("Close")
                }
            }
               
        }
        
    }
    
    
    
}


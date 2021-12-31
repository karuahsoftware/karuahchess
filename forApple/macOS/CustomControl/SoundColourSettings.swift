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

struct SoundColourSettings: View {
    
    @ObservedObject private var soundColourSettingsVM : SoundColourSettingsViewModel = SoundColourSettingsViewModel.instance
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
                        Text("Sound and Colour").font(.headline)
                    }.padding(.bottom, 10)
                    
                    Toggle(isOn: $soundColourSettingsVM.value.soundReadEnabled) {
                        Text("Read messages out loud")
                            .font(.body)
                    }
                    
                    Toggle(isOn: $soundColourSettingsVM.value.soundEffectEnabled) {
                        Text("Sound effects")
                            .font(.body)
                    }
                    
                    
                    Picker(selection: $soundColourSettingsVM.value.darkSquareColour, label: Text("Board colour")) {
                        ForEach(Constants.darkSquareColourArray, id: \.self) {colourItem in
                            Text(colourItem.text)
                        }
                    }
                    .pickerStyle(DefaultPickerStyle())
                    
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


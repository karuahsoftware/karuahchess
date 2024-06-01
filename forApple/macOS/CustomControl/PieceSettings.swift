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

struct PieceSettings: View {
    
    @ObservedObject private var pieceSettingsVM : PieceSettingsViewModel = PieceSettingsViewModel.instance
    
    private let menuSheet : MenuSheet
    
    // Initialisation
    init(pMenuSheet: MenuSheet) {
        menuSheet = pMenuSheet
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            ScrollView([.vertical]) {
                
                VStack(alignment: .leading) {
                    Slider(value: $pieceSettingsVM.moveSpeed, in: 0...6, step:1) {
                        Text("Move Speed \(Int($pieceSettingsVM.moveSpeed.wrappedValue))")
                            .font(.body)
                    }
                    
                    Toggle(isOn: $pieceSettingsVM.promoteAuto) {
                        Text("Promote Pawn automatically")
                            .font(.body)
                    }.padding(.top)
                    
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


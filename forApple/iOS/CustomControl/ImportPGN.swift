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

struct ImportPGN: View {
    @Binding var showMenu: Bool
    private let processor = ImportPGNProcessor()
    @State private var pgnText: String = ""
    @State private var importMessage: String = ""
    @FocusState private var isPGNFocused: Bool
    
    var body: some View {
        Form {
            
            TextEditor(text: $pgnText)
                .font(.system(.body, design: .monospaced))
                .frame(maxWidth: .infinity, minHeight:60, alignment: .topLeading)
                .padding(.vertical, 6)
                .padding(.horizontal, 4)
                .overlay(RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.secondary, lineWidth: 1)
                )
                .focused($isPGNFocused)
            
            Button(action: {
                let result = processor.importData(pgnText)
                importMessage = result.message
                if result.success {
                    showMenu = false                    
                    
                    Task { @MainActor in
                        MenuSettingsViewModel.instance.navigatorEnabled = true
                        let maxId = GameRecordDataService.instance.getMaxId()
                        await BoardViewModel.instance.navigateGameRecord(pRecId: maxId, pAnimate: false)
                    }
                    
                }
                
            }){ Text("Import") }
                .buttonStyle(.bordered)
            
            // Error message
            if !importMessage.isEmpty {
                Text(importMessage)
                    .font(.footnote)
                    .foregroundColor(.red)
                    .padding(.top, 4)
            }
            
            Spacer()
                .frame(maxWidth: .infinity)
                    
                
        }
        .padding(0)
        .navigationBarTitle(Text("Import PGN"), displayMode: .inline)
        .navigationBarItems(trailing: Button("Close") {
            self.showMenu = false
            })
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isPGNFocused = true
            }
        }
    
        
    }
    
    
}


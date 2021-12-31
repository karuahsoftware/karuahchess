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

struct TitlebarAccessoryView: View {
    @ObservedObject private var engineSettingsVM : EngineSettingsViewModel = EngineSettingsViewModel.instance
    
    var body: some View {
        HStack {
            DirectionIndicatorView(directionIndicatorVM: BoardViewModel.instance.directionIndicatorVM).frame(width: 20, height: 20)
            ActivityIndicatorView(activityIndicatorVM: BoardViewModel.instance.activityIndicatorVM).frame(width:35, height:35)
            
            Spacer()
            
            Button(action: {
                MenuSheet.shared.active = .engineSettings
                   }){
                HStack {
                    Image(systemName: "gear").foregroundColor(Color(.textColor))
                    Text("\(engineSettingsVM.value.limitEngineStrengthELOIndex + 1)")
                }
            }
            .help("Engine Settings")
            
            Button(action: {
                BoardViewModel.instance.rotateClick()
                   }){
                Image(systemName: "arrow.clockwise").foregroundColor(Color(.textColor))
            }.keyboardShortcut("r", modifiers: [.command])
            .help("Rotate ⌘R")
            
            Button(action: {
                BoardViewModel.instance.showLastMove()
                   }){
                Image(systemName: "eye.fill").foregroundColor(Color(.textColor))
                    
            }.keyboardShortcut("l", modifiers: [.command])
            .help("Last move ⌘L")
        }.padding(.init(top: 0, leading: 7, bottom: 0, trailing: 7))
    }
    
}

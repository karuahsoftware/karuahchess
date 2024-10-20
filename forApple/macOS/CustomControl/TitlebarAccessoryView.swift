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

struct TitlebarAccessoryView: View {
    @ObservedObject private var hintSettingsVM : HintSettingsViewModel = HintSettingsViewModel.instance
    @ObservedObject private var menuSettingsVM : MenuSettingsViewModel = MenuSettingsViewModel.instance
    
    var body: some View {
        HStack {
            DirectionIndicatorView(directionIndicatorVM: BoardViewModel.instance.directionIndicatorVM).frame(width: 20, height: 20)
            LevelIndicatorView()
            ActivityIndicatorView(activityIndicatorVM: BoardViewModel.instance.activityIndicatorVM).frame(width:35, height:35)
            
            Spacer()
            
            if !menuSettingsVM.arrangeBoardEnabled {
                Button(action: {
                    let action : ()->Void = {
                        Task(priority: .userInitiated) {
                            await BoardViewModel.instance.newGame()
                        }
                    }
                    BoardViewModel.instance.boardMessageAlertVM.show("Start a new game?", "", BoardMessageAlertViewModel.alertTypeEnum.YesNo, action)
                }){
                    Image(systemName: "target").foregroundColor(Color(.textColor))
                }
                .help("New Game ⌘N")
                
                
                Button(action: {
                    BoardViewModel.instance.showLastMove()
                }){
                    Image(systemName: "eye.fill").foregroundColor(Color(.textColor))
                    
                }.keyboardShortcut("l", modifiers: [.command])
                    .help("Last move ⌘L")
                
                if hintSettingsVM.hintEnabled {
                    Button(action: {
                        Task(priority: .userInitiated) {
                            await BoardViewModel.instance.showHint()
                        }
                    }){
                        Image(systemName: "lightbulb").foregroundColor(Color(.textColor))
                        
                    }.keyboardShortcut("h", modifiers: [.command])
                        .help("Hint ⌘H")
                }
            }
            else {
                Button(action: {
                    BoardViewModel.instance.pieceEditSelectVM.show()
                }){
                    Image(systemName: "person").foregroundColor(Color(.textColor))
                }
                    
                
                Button(action: {
                    Task(priority: .userInitiated) {
                        await BoardViewModel.instance.editEraseSelection()
                    }
                }){
                    Image(systemName: "trash.fill").foregroundColor(Color(.textColor))
                }
                
            }
        }.padding(.init(top: 0, leading: 7, bottom: 0, trailing: 7))
    }
    
}

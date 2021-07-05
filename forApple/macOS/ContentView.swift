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

struct ContentView: View {
    @ObservedObject private var boardVM : BoardViewModel = BoardViewModel.shared
    @ObservedObject private var menuSheet : MenuSheet = MenuSheet.shared
    
    var body: some View {

        ZStack(alignment: Alignment(horizontal: .leading, vertical: .top)) {
            
            // Main Board
            BoardViewModel.shared.boardLayout
                .coordinateSpace(name: "BoardCoordinateSpace")
                .rotationEffect(Angle(degrees: boardVM.boardRotation))
                .zIndex(1)
                
                                        
            // Animation Layout
            BoardViewModel.shared.animationLayout
                .rotationEffect(Angle(degrees: boardVM.boardRotation))
                .zIndex(2)
                
    
            // Piece Edit Tool
            PieceEditToolView(pieceEditToolVM: BoardViewModel.shared.pieceEditTool)
                .zIndex(3)
                
            // Board Message
            BoardMessageAlertView(BoardMessageAlertVM: BoardViewModel.shared.boardMessage)
                .zIndex(5)
    
            // Messages to show
            ToastView()
                .frame(minWidth:0 , maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .bottom)
                .zIndex(100)
    
            Spacer()
                
        }
        .sheet(item: $menuSheet.active, onDismiss: {menuSheet.active = nil}) { item in
            VStack{
                ScrollView([.vertical]) {
                    switch item {
                    case .engineSettings:
                        EngineSettings()
                    case .about:
                        AboutView()
                    }
                }
                .frame(minWidth: 50, maxWidth: .infinity, minHeight: 50, maxHeight: .infinity)
                
                Divider().padding(.top,25)
                
                Button(action: {
                    menuSheet.active = nil
                }){
                    Text("Close")
                }
                
            }.padding(10)
        }
        
    }
    
}



struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}



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
    @ObservedObject var boardVM : BoardViewModel = BoardViewModel.shared
    @State private var showMenu : Bool = false
    
    
    var body: some View {
        
        let drag = DragGesture()
            .onEnded {
                if $0.translation.width > 20 {
                    withAnimation {
                        self.showMenu = false
                    }
                }
        }
        
        
        return ZStack(alignment: Alignment(horizontal: .leading, vertical: .top)) {
            
            
                    // Main Board
                    BoardViewModel.shared.boardLayout
                        .coordinateSpace(name: "BoardCoordinateSpace")
                        .rotationEffect(Angle(degrees: boardVM.boardRotation))
                        .disabled(self.showMenu ? true : false)
                        .zIndex(1)
                        
                                                
                    // Animation Layout
                    BoardViewModel.shared.animationLayout
                        .rotationEffect(Angle(degrees: boardVM.boardRotation))
                        .zIndex(2)
                        
                    // Piece Edit Tool
                    PieceEditToolView(pieceEditToolVM: BoardViewModel.shared.pieceEditTool)
                        .zIndex(3)
                        
                    // Action buttons
                    FloatingActionView()
                        .zIndex(4)
            
                    // Board Message
                    BoardMessageAlertView(BoardMessageAlertVM: BoardViewModel.shared.boardMessage)
                        .zIndex(5)
            
                    // Show menu
                    if self.showMenu {
                        
                        // Dismiss menu when rectangle is clicked
                        Rectangle()
                            .zIndex(9)
                            .opacity(0.3)
                            .onTapGesture {
                                withAnimation {
                                    self.showMenu = false
                                }
                        }
                        
                        // Show the menu
                        MenuView(showMenu: $showMenu)
                        .transition(.move(edge: .trailing))
                        .zIndex(10)
                        .gesture(drag)
                        .frame(minWidth:0 , maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .trailing)
                        
                    }
            
                    // Messages to show
                    ToastView()
                        .frame(minWidth:0 , maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .bottom)
                        .zIndex(100)
            
                    Spacer()
                        
                }
                .navigationBarTitle("", displayMode: .inline)
                .navigationBarItems(
                    leading: NavLogoItem(),
                    trailing: Button(action: {
                        withAnimation {
                            self.showMenu.toggle()
                        }
                    }){
                        Image(systemName: "line.horizontal.3")
                            
                    }.font(.title)
                )
                .navigationViewStyle(StackNavigationViewStyle())
        
        
    }
    
}


/// Navigation logo
struct NavLogoItem: View {
    var body: some View {
        HStack(alignment: .center) {
            Text("Karuah Chess").font(.body)
            DirectionIndicatorView(directionIndicatorVM: BoardViewModel.shared.directionIndicator).frame(width: 20, height: 20)
            ActivityIndicatorView(activityIndicatorVM: BoardViewModel.shared.processingIndicator).frame(width:35, height:35)
        }
        
    }
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}



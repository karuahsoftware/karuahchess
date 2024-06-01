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
import UniformTypeIdentifiers
import CoreMedia

struct ContentView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject private var boardVM : BoardViewModel = BoardViewModel.instance
    @ObservedObject private var navigateVM : NavigatorViewModel = BoardViewModel.instance.navigatorVM
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
        
        
        return
            ZStack(alignment: Alignment(horizontal: .leading, vertical: .top))  {
                VStack(alignment: .leading, spacing: 0) {
                    ZStack(alignment: Alignment(horizontal: .leading, vertical: .top)) {
                    
                        if boardVM.coordPanelEnabled {
                            CoordPanelView(boardRotation: $boardVM.boardRotation)
                                .zIndex(1)
                        }
                
               
                        // Main Board
                        TilePanelView(tilePanelVM: boardVM.tilePanelVM)
                            .coordinateSpace(name: "BoardCoordinateSpace")
                            .rotationEffect(Angle(degrees: boardVM.boardRotation))
                            .border(device.tileDarkSquareColour, width: 1)
                            .offset(x: device.boardCoordPadding)
                            .disabled(self.showMenu ? true : false)
                            .zIndex(2)
                            
                            
                            
                        
                            
                        // Animation Layout
                        TileAnimationView(tileAnimationVM: boardVM.tileAnimationVM)
                            .rotationEffect(Angle(degrees: boardVM.boardRotation))
                            .offset(x: device.boardCoordPadding)
                            .zIndex(3)
                        
                            
                        // Piece Edit Tool
                        PieceEditToolView(pieceEditToolVM: boardVM.pieceEditToolVM)
                            .offset(x: device.boardCoordPadding)
                            .zIndex(4)
                        
                
                        // Castling Rights
                        CastlingRightsView(castlingRightsVM: boardVM.castlingRightsVM)
                            .offset(x: device.boardCoordPadding)
                            .zIndex(5)
                        
                        // Castling Rights
                        PawnPromotionView(pawnPromotionVM: boardVM.pawnPromotionVM)
                            .offset(x: device.boardCoordPadding)
                            .zIndex(6)
                    }
                    
                    if navigateVM.enabled {
                        NavigatorView(navigatorVM: navigateVM, boardVM: boardVM)
                            .padding(.top,1)
                            .frame(width: device.tileSize * 8 + device.boardCoordPadding, alignment: .top)
                    }
                }
            // Action buttons
            FloatingActionView(showMenu: $showMenu)
                .zIndex(7)
    
            // Board Message
            BoardMessageAlertView(BoardMessageAlertVM: BoardViewModel.instance.boardMessageAlertVM)
                .zIndex(8)
    
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
        .coordinateSpace(name: "MainCoordinateSpace")
        .fileExporter(isPresented: $boardVM.showFileExporter, document: $boardVM.exportFileDocument.wrappedValue, contentType: UTType.gzip, defaultFilename: $boardVM.exportFileDocument.wrappedValue?.fileName) { result in
            switch result {
            case .success:
                boardVM.showMessage(pTextFull: "File successfully saved", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            case .failure (let error):
                boardVM.showMessage(pTextFull: "Error saving file - \(error.localizedDescription)", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
            boardVM.showFileExporter = false
        }
        .fileImporter(isPresented: $boardVM.showFileImporter, allowedContentTypes: [UTType.gzip, UTType.xml], allowsMultipleSelection: false) { result in
            Task(priority: .userInitiated) {
                do {
                    guard let fileUrl: URL = try result.get().first else {
                        boardVM.showMessage(pTextFull: "Error importing file", pTextShort: "", pDurationms: Constants.TOAST_LONG)
                        return
                    }
                    
                    if fileUrl.startAccessingSecurityScopedResource() {
                        await boardVM.loadGame(fileUrl)
                        fileUrl.stopAccessingSecurityScopedResource()
                    }
                    
                }
                catch let error {
                    boardVM.showMessage(pTextFull: "Error importing file - \(error.localizedDescription)", pTextShort: "", pDurationms: Constants.TOAST_LONG)
                }
                
                boardVM.showFileImporter = false
            }
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
       .clipped()   // Clipping stops the board from going in to the inset areas
        
    }
    
}


/// Navigation logo
struct NavLogoItem: View {
    var body: some View {
        HStack(alignment: .center) {
            Text("Karuah Chess").font(.body)
            DirectionIndicatorView(directionIndicatorVM: BoardViewModel.instance.directionIndicatorVM).frame(width: 20, height: 20)
            LevelIndicatorView()
            ActivityIndicatorView(activityIndicatorVM: BoardViewModel.instance.activityIndicatorVM).frame(width:35, height:35)
        }
        
    }
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}



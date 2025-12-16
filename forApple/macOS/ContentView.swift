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

struct ContentView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject private var boardVM : BoardViewModel = BoardViewModel.instance
    @ObservedObject private var menuSheet : MenuSheet = MenuSheet.shared
    @ObservedObject private var navigateVM : NavigatorViewModel = BoardViewModel.instance.navigatorVM
    
    var body: some View {
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
                    .zIndex(2)
                    
                                            
                // Animation Layout
                TileAnimationView(tileAnimationVM: boardVM.tileAnimationVM)
                    .rotationEffect(Angle(degrees: boardVM.boardRotation))
                    .offset(x: device.boardCoordPadding)
                    .zIndex(3)
                    
        
                // Piece Edit Tool
                PieceEditSelectView(pieceEditSelectVM: boardVM.pieceEditSelectVM)
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
                
                // Board Message
                BoardMessageAlertView(BoardMessageAlertVM: boardVM.boardMessageAlertVM)
                    .offset(x: device.boardCoordPadding)
                    .zIndex(7)
        
                // Messages to show
                ToastView()
                    .offset(x: device.boardCoordPadding)
                    .frame(minWidth:0 , maxWidth: device.tileSize * 8, minHeight: 0, maxHeight: .infinity, alignment: .bottom)
                    .zIndex(100)
                    
            }
            
            if navigateVM.enabled {
                NavigatorView(navigatorVM: navigateVM, boardVM: boardVM)
                    .padding(.top,1)
                    .frame(width: device.tileSize * 8 + device.boardCoordPadding, alignment: .top)
            }
            
        }
        .coordinateSpace(name: "MainCoordinateSpace")
        .sheet(item: $menuSheet.active, onDismiss: {menuSheet.active = nil}) { item in
            VStack{
                switch item {
                case .engineSettings:
                    EngineSettings(pMenuSheet: menuSheet)
                case .boardSettings:
                    BoardSettings(pMenuSheet: menuSheet)
                case .pieceSettings:
                    PieceSettings(pMenuSheet: menuSheet)
                case .hintSettings:
                    HintSettings(pMenuSheet: menuSheet)
                case .soundSettings:
                    SoundSettings(pMenuSheet: menuSheet)
                case .importPGNView:
                    ImportPGN(pMenuSheet: menuSheet)
                case .about:
                    AboutView(pMenuSheet: menuSheet)
                }
            }.padding(10)
        }
        .fileExporter(isPresented: $boardVM.showFileExporter, document: $boardVM.exportFileDocument.wrappedValue, contentType: .gzip, defaultFilename: $boardVM.exportFileDocument.wrappedValue?.fileName) { result in
            switch result {
            case .success:
                boardVM.showMessage(pTextFull: "File successfully saved", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            case .failure (let error):
                boardVM.showMessage(pTextFull: "Error saving file - \(error.localizedDescription)", pTextShort: "", pDurationms: Constants.TOAST_LONG)
            }
            boardVM.showFileExporter = false
        }
        .fileImporter(isPresented: $boardVM.showFileImporter, allowedContentTypes: [.gzip, .xml], allowsMultipleSelection: false) { result in
            Task(priority: .userInitiated) {
                do {
                    let fileUrl = try result.get()
                    await boardVM.loadGame(fileUrl[0])
                }
                catch let error {
                    boardVM.showMessage(pTextFull: "Error importing file - \(error.localizedDescription)", pTextShort: "", pDurationms: Constants.TOAST_LONG)
                }
                
                boardVM.showFileImporter = false
            }
        }
    }

    
}



struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}



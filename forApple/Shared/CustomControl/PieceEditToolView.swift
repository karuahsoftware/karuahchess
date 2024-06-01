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

struct PieceEditToolView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var pieceEditToolVM : PieceEditToolViewModel
    @State private var pieceEditListWhite : [Character] = ["P", "R", "N", "B" ,"Q"]
    @State private var pieceEditListBlack : [Character] = ["p", "r", "n", "b" ,"q"]
    
    
    var body: some View {
        HStack(alignment: .center, spacing: 0) {
        
        if pieceEditToolVM.visible {
            
            // Delete Button
            Button(action: {
                BoardViewModel.instance.arrangeUpdate(pFen: " ", pToIndex: pieceEditToolVM.lastTileIndexTapped)
                pieceEditToolVM.close()
            }) {
                Image(systemName: "trash.fill")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .padding(.init(top: 5, leading: 5, bottom: 5, trailing: 0))
                    .clipped()
                    .foregroundColor(Color("Rust"))
            }.buttonStyle(BoardButtonStyle(buttonSize: pieceEditToolVM.calcButtonSize(pTileSize: device.tileSize)))
        
           
            // Colour select button
            Button(action: {
                pieceEditToolVM.colour = pieceEditToolVM.colour * -1
            }) {
                Image(systemName: "chevron.right")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .padding(.init(top: 8, leading: 0, bottom: 8, trailing: 0))
                    .clipped()
                    .foregroundColor(Color("Rust"))
            }.buttonStyle(BoardButtonStyle(buttonSize: pieceEditToolVM.calcButtonSize(pTileSize: device.tileSize)))
            
            
            // Piece Buttons
            ForEach(pieceEditToolVM.colour == Constants.WHITEPIECE ? pieceEditListWhite : pieceEditListBlack, id: \.self) {fen in
                Button(action: {
                    BoardViewModel.instance.arrangeUpdate(pFen: fen, pToIndex: pieceEditToolVM.lastTileIndexTapped)
                    pieceEditToolVM.close()
                }) {
                    getImage(pFen: fen)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .padding(0)
                        .clipped()
                }
                .buttonStyle(BoardButtonStyle(buttonSize: pieceEditToolVM.calcButtonSize(pTileSize: device.tileSize)))
                
            }
             
        
        }
 
        
    }
    .background(Color("GrayDarkLight"))
    .clipped()
    .shadow(radius: 6)
    .shadow(radius: 10)
    .offset(x: pieceEditToolVM.posXY.x, y: pieceEditToolVM.posXY.y)
    
    }
    
}





/// Gets a tile image
/// - Parameter pFen: The fen character to get
/// - Returns: An image
private func getImage(pFen: Character) -> Image {
    switch pFen {
    case "P":
        return Image("WhitePawn")
    case "N":
        return Image("WhiteKnight")
    case "B":
        return Image("WhiteBishop")
    case "R":
        return Image("WhiteRook")
    case "Q":
        return Image("WhiteQueen")
    case "K":
        return Image("WhiteKing")
    case "p":
        return Image("BlackPawn")
    case "n":
        return Image("BlackKnight")
    case "b":
        return Image("BlackBishop")
    case "r":
        return Image("BlackRook")
    case "q":
        return Image("BlackQueen")
    case "k":
        return Image("BlackKing")
    default:
        return Image("")
    }
}

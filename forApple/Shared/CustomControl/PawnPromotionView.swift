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

struct PawnPromotionView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var pawnPromotionVM : PawnPromotionViewModel
    let maxButtonSize = 45
    
    var body: some View {
        
        ZStack(alignment: Alignment(horizontal: .center, vertical: .center)) {
            let buttonSize = min(45, device.tileSize)
            if pawnPromotionVM.visible {
            
                VStack(alignment: .leading) {
                    
                    Text("Pawn promotion -> Select a piece").font(.headline).padding(.bottom, 10)
                    
                    HStack(alignment: .center) {
                       
                        Button(action: {
                            Task(priority: .userInitiated) {
                                // Only positive values required here as pawn promotion value is positive
                                pawnPromotionVM.promotionPiece = Constants.WHITE_QUEEN_SPIN
                                await pawnPromotionVM.close()
                            }
                        }, label: {
                            
                            Image(pawnPromotionVM.colour == Constants.WHITEPIECE ? "WhiteQueen" : "BlackQueen").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PlainButtonStyle())
                        .background(Color("ButtonBackground"))
                        .cornerRadius(8)
                        
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                // Only positive values required here as pawn promotion value is positive
                                pawnPromotionVM.promotionPiece = Constants.WHITE_ROOK_SPIN
                                await pawnPromotionVM.close()
                            }
                        }, label: {
                            
                            Image(pawnPromotionVM.colour == Constants.WHITEPIECE ? "WhiteRook" : "BlackRook").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PlainButtonStyle())
                        .background(Color("ButtonBackground"))
                        .cornerRadius(8)
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                // Only positive values required here as pawn promotion value is positive
                                pawnPromotionVM.promotionPiece = Constants.WHITE_BISHOP_SPIN
                                await pawnPromotionVM.close()
                            }
                        }, label: {
                            
                            Image(pawnPromotionVM.colour == Constants.WHITEPIECE ? "WhiteBishop" : "BlackBishop").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PlainButtonStyle())
                        .background(Color("ButtonBackground"))
                        .cornerRadius(8)
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                // Only positive values required here as pawn promotion value is positive
                                pawnPromotionVM.promotionPiece = Constants.WHITE_KNIGHT_SPIN
                                await pawnPromotionVM.close()
                            }
                        }, label: {
                            
                            Image(pawnPromotionVM.colour == Constants.WHITEPIECE ? "WhiteKnight" : "BlackKnight").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PlainButtonStyle())
                        .background(Color("ButtonBackground"))
                        .cornerRadius(8)
                        
                    }
                    
                    
                }
                .padding(10)
                .background(Color("FormBackground"))
                .padding(10)
                .clipped()
                .shadow(radius: 6)
                .shadow(radius: 10)
                
            }
        }.frame(maxWidth: device.tileSize * 8, maxHeight: device.tileSize * 8)
        .background(pawnPromotionVM.visible ? Color.gray.opacity(0.4) : Color.clear)
        
    }
}






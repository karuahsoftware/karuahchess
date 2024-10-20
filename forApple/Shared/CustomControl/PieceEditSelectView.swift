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

struct PieceEditSelectView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var pieceEditSelectVM : PieceEditSelectViewModel
    @State private var buttonPressedArray = Array(repeating: false, count: 10)
    @State private var contentWidth: CGFloat = 0
    
    var body: some View {
        
        ZStack(alignment: Alignment(horizontal: .center, vertical: .center)) {
            let buttonSize = max(40, device.tileSize * 1.1)
            if pieceEditSelectVM.visible {
            
                VStack(alignment: .leading) {
                    
                    Text("Add piece to selected squares").font(.headline).padding(.bottom, 10)
                    
                    HStack(alignment: .center) {
                       
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "p")
                            }
                        }, label: {
                            Image("BlackPawn").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "r")
                            }
                        }, label: {
                            
                            Image("BlackRook").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "n")
                            }
                        }, label: {
                            
                            Image("BlackKnight").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "b")
                            }
                        }, label: {
                            
                            Image("BlackBishop").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "q")
                            }
                        }, label: {
                            
                            Image("BlackQueen").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                    }
                    
                    HStack(alignment: .center) {
                       
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "P")
                            }
                        }, label: {
                            
                            Image("WhitePawn").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "R")
                            }
                        }, label: {
                            
                            Image("WhiteRook").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "N")
                            }
                        }, label: {
                            
                            Image("WhiteKnight").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "B")
                            }
                        }, label: {
                            
                            Image("WhiteBishop").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await pieceEditSelectVM.updateSelectedTiles(pFen: "Q")
                            }
                        }, label: {
                            
                            Image("WhiteQueen").resizable()
                                .frame(width:buttonSize, height: buttonSize)
                                .padding(2)
                                
                        })
                        .buttonStyle(PieceSelectButtonStyle())
                        
                    }.background(
                        GeometryReader { geometry in
                            Color.clear.onAppear {
                                self.contentWidth = geometry.size.width
                            }
                        }
                    )
                    
                    
                    Divider()
                        .frame(width: contentWidth)
                        .padding(.top)
                    
                    HStack {
                        Button(action: {
                            pieceEditSelectVM.close()
                        }){
                            Text("Close")
                        }
                     }
                }
                .padding(10)
                .background(RoundedRectangle(cornerRadius: 10).fill(Color("FormBackground")))
                .clipped()
                .shadow(radius: 6)
                .shadow(radius: 10)
                
            }
        }.frame(maxWidth: device.tileSize * 8, maxHeight: device.tileSize * 8)
        .background(pieceEditSelectVM.visible ? Color.gray.opacity(0.4) : Color.clear)
        
    }
}

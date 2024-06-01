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

struct CastlingRightsView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var castlingRightsVM : CastlingRightsViewModel 
 
    
    var body: some View {
        
        ZStack(alignment: Alignment(horizontal: .center, vertical: .center)) {
            if castlingRightsVM.visible {
            
                VStack(alignment: .leading) {
                    
                    Text("Castling Rights").font(.headline).padding(.bottom, 10)
                    
                    HStack(alignment: .center) {
                        if castlingRightsVM.kingSpin == Constants.WHITE_KING_SPIN{
                            Image("WhiteKing").resizable().frame(width:55, height: 55)
                        }
                        else  {
                            Image("BlackKing").resizable().frame(width:55, height: 55)
                        }
                        VStack(alignment: .leading) {
                            Toggle(isOn: $castlingRightsVM.queenSideCastle) {
                                Text("Can Castle Queen side")
                            }
                            
                            Toggle(isOn: $castlingRightsVM.kingSideCastle) {
                                Text("Can Castle King side")
                            }
                            
                        }
                        
                    }
                    
                    HStack(alignment: .center) {
                        Spacer()
                        Button(action: {
                            castlingRightsVM.close()
                        }){
                            Text("Close")
                        }.padding(.top)
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
        
        
    }
}






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

struct BoardSettings: View {
    
    @ObservedObject private var boardSettingsVM : BoardSettingsViewModel = BoardSettingsViewModel.instance
    @ObservedObject var boardVM : BoardViewModel = BoardViewModel.instance
    
    private let menuSheet : MenuSheet
    
    // Initialisation
    init(pMenuSheet: MenuSheet) {
        menuSheet = pMenuSheet
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            ScrollView([.vertical]) {
                
                VStack(alignment: .leading) {
                    HStack {
                        Image(systemName: "square.grid.3x3.square")
                        .imageScale(.large)
                        Text("Board").font(.headline)
                    }.padding(.bottom, 10)
                    
                    Picker(selection: $boardSettingsVM.darkSquareColour, label: Text("Colour")) {
                        ForEach(Constants.darkSquareColourArray, id: \.self) {colourItem in
                            HStack {
                                // At this time had to use an image instead of a shape like Rectangle() as
                                // the picker seems to only display images. Also the height of a picker
                                // doesn't seem to be adjustable so had to use a small image.
                                Image("BoardColour" + colourItem.text)
                                Text(colourItem.text)
                            }
                        }
                    }
                    .pickerStyle(DefaultPickerStyle())
                    
                    
                    VStack(alignment: .leading, spacing: 3) {
                        Text("Orientation").font(.body).padding(.top, 10).padding(.leading, 0)
                        HStack(alignment: .top, spacing: 3) {
                            Image("Orientation")
                                .resizable()
                                .frame(width: 80, height: 80)
                                .rotationEffect(Angle(degrees: boardVM.boardRotation))
                                .background(Device.instance.tileDarkSquareColour)
                            Button(action: {
                                BoardViewModel.instance.rotateClick()
                                   }){
                                       Image(systemName: "arrow.clockwise")
                                           .resizable()
                                           .aspectRatio(contentMode: .fit)
                                          
                                   }
                                   .controlSize(.large)
                                   
                        }
                    }
                    
                    Spacer().frame(maxWidth: .infinity)
                 
                    
                }.frame(minWidth: 200, maxWidth: .infinity, minHeight: 50, maxHeight: .infinity)
            } // Scrollview
            
            Divider()
            
            HStack {
                Button(action: {
                    menuSheet.active = nil
                }){
                    Text("Close")
                }
            }
               
        }
        
    }
    
    
    
}


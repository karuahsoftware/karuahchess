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
    @Binding var showMenu: Bool
    
    @ObservedObject var boardSettingsVM : BoardSettingsViewModel = BoardSettingsViewModel.instance
    @ObservedObject var boardVM : BoardViewModel = BoardViewModel.instance
    
    
    var body: some View {
        Form {
            
            Picker(selection: $boardSettingsVM.darkSquareColour, label: Text("Colour")) {
                ForEach(Constants.darkSquareColourArray, id: \.self) {colourItem in
                    HStack {
                        // At this time had to use an image instead of a shape like Rectangle() as
                        // the picker seems to only display images. Also the height of a picker
                        // doesn't seem to be adjustable to had to use a small image.
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
                                   .frame(width:40, height: 40)
                                  
                           }
                           
                           
                }
            }
            
            
            
            Spacer()
                .frame(maxWidth: .infinity)
                    
                
            }
            .padding(0)
            .navigationBarTitle(Text("Board"), displayMode: .inline)
            .navigationBarItems(trailing: Button("Close") {
                self.showMenu = false
            })
            
    
        
    }
    
    
}


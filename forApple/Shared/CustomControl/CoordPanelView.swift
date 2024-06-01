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

struct CoordPanelView: View {
    @ObservedObject private var device : Device = Device.instance
    @Binding var boardRotation : Double
    
    private let verticalArray = [["8", "7", "6", "5", "4", "3", "2", "1"], ["a", "b", "c", "d", "e", "f", "g", "h"], ["1", "2", "3", "4", "5", "6", "7", "8"], ["h", "g", "f", "e", "d", "c", "b", "a"]]
    private let horizontalArray = [["a", "b", "c", "d", "e", "f", "g", "h"], ["1", "2", "3", "4", "5", "6", "7", "8"], ["h", "g", "f", "e", "d", "c", "b", "a"], ["8", "7", "6", "5", "4", "3", "2", "1"]]
    
    var body: some View {
        let index = boardRotation == 0 ? 0 : boardRotation == 90 ? 1 : boardRotation == 180 ? 2 : 3
        
        VStack(alignment: .leading, spacing: 1) {
            VStack(spacing: 0) {
                ForEach(verticalArray[index], id: \.self) {item in
                    Text(item).frame(height: device.tileSize)
                }
            }.offset(x: 1)
                
            
            HStack(spacing: 0) {
               ForEach(horizontalArray[index], id: \.self) {item in
                    Text(item).frame(width: device.tileSize)
               }
            }.offset(x: device.boardCoordPadding)
                
        }
        
        
    }
}






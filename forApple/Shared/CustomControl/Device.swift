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

final internal class Device: ObservableObject {
    static let instance = Device()
    
    private init() {
        // private ensures only once instance of the class is created
        
        // Load tile colour
        let tileColour = ParameterDataService.instance.get(pParameterClass: ParamColourDarkSquares.self).argb()
        tileDarkSquareColour = Color(red: Double(tileColour.r) / 255, green: Double(tileColour.g) / 255, blue: Double(tileColour.b) / 255)
    }
    
    @Published var tileSize: CGFloat = 0
    @Published var tileDarkSquareColour: Color
    @Published var boardCoordPadding: CGFloat = 0
    @Published var toastMessage: String = ""
    @Published var navigationHeight: CGFloat = 0
    
    #if os(iOS)
    @Published var isLandScape: Bool = false
    #endif
    
}

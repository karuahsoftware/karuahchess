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

@MainActor class TileViewModel: ObservableObject {
    
    @Published var pieceName = ""
    @Published var spin = 0
    @Published var highlight = false
    @Published var highlightFull = false
    @Published var highlightFullFadeOut = false
    @Published var hightlightFullColor = Color("Magenta")
    @Published var highlightEdit = false
    @Published var checkIndicator = false
    @Published var boardFrame = CGRect()
    @Published var mainFrame = CGRect()
    @Published var visible = true
    @Published var editMode = false
    
    
    func setPiece(pName: String, pSpin: Int) {
        self.pieceName = pName
        self.spin = pSpin
    }
    
    func setHighLight(pActive: Bool) {
        highlight = pActive
    }
    
    func setHighLightFull(pActive: Bool, pColour: Color) {
        highlightFull = pActive
        hightlightFullColor = pColour
    }
    
    func setHighLightFullFadeOut(pActive: Bool, pColour: Color) {
        highlightFullFadeOut = pActive
        hightlightFullColor = pColour
    }
    
    func setHighLightEdit(pActive: Bool) {
        highlightEdit = pActive
    }
    
    func setCheckIndicator(pActive: Bool) {
        checkIndicator = pActive
    }
    
    func setEditMode(pActive: Bool) {
        editMode = pActive
    }
}

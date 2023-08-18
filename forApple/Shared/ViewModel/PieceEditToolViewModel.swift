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

@MainActor class PieceEditToolViewModel: ObservableObject {
    
    @ObservedObject private var device : Device = Device.instance
    @Published var bufferBoard: KaruahChessEngineC = KaruahChessEngineC()
    @Published var colour : Int = Constants.WHITEPIECE
    @Published var visible : Bool = false
    @Published var posXY : CGPoint = CGPoint(x: 0, y: 0)
    @Published var lastTileIndexTapped : Int = -1
    
    
    /// Displays the piece edit tool
    /// - Parameter pTile: The tile that was clicked
    func show(pTile: TileView) {
        // Ensure nothing is higlighted
        BoardViewModel.instance.tilePanelVM.setHighLightFull(pBits: 0, pColour: Color("Magenta"))
        
        if visible == true {
            close()
        }
        else {
            // Show the view and put it in the correct position
            lastTileIndexTapped = pTile.index
            if pTile.tileVM.spin > 0 {
                colour = Constants.WHITEPIECE
            }
            else if pTile.tileVM.spin < 0 {
                colour = Constants.BLACKPIECE
            }
            
            // Calculate position
            let boardWidth = Device.instance.tileSize * 8
            let buttonSize = calcButtonSize(pTileSize: device.tileSize)
            
            let limitUpperPosX = boardWidth - buttonSize * 7
            let posX : CGFloat =  max(0, min(pTile.tileVM.mainFrame.origin.x - (buttonSize * 7 / 2) , limitUpperPosX))
            
            var posY : CGFloat
            if pTile.tileVM.mainFrame.origin.y - device.tileSize > 0 {
                posY = pTile.tileVM.mainFrame.origin.y - buttonSize
            }
            else {
                posY = pTile.tileVM.mainFrame.origin.y + device.tileSize
            }
            
            posXY = CGPoint(x: posX, y: posY)
            
            // Highlight square being modified
            pTile.tileVM.setHighLightFull(pActive: true, pColour: Color("Magenta"))
            
            visible = true
        }
        
        
    }
    
    
    /// Close the view
    
    func close() {
        if visible {
            BoardViewModel.instance.tilePanelVM.setHighLightFull(pBits: 0, pColour: Color("Magenta"))
            lastTileIndexTapped = -1
            visible = false
        }
    }
    
    
    /// Calculates the size of the button based on the tile size
    /// - Parameter pTileSize: The size of the tile
    func calcButtonSize(pTileSize: CGFloat) -> CGFloat {
        if (pTileSize > 40) {
            return pTileSize * 0.85
        }
        else {
            return pTileSize
        }
    }
}



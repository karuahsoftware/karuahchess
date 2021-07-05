/*
Karuah Chess is a chess playing program
Copyright (C) 2020 Karuah Software

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

class PieceEditToolViewModel: ObservableObject {
    
    @Published var bufferBoard: KaruahChessEngineC = KaruahChessEngineC()
    @Published var colour : Int = Constants.WHITEPIECE
    @Published var visible : Bool = false
    @Published var posXY : CGPoint = CGPoint(x: 0, y: 0)
    @Published var lastTileIndexTapped : Int = -1
    
    /// Displays the piece edit tool
    /// - Parameter pTile: The tile that was clicked
    func show(pTile: TileView) {
        // Ensure nothing is higlighted
        BoardViewModel.shared.boardLayout.setHighLightFull(pBits: 0)
        
        if visible == true && lastTileIndexTapped == pTile.index {
            Close()
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
            let boardWidth = Device.shared.tileSize * 8
            
            let buttonSize = calcButtonSize(pTileSize: pTile.device.tileSize)
            
            let marginX : CGFloat = 10
            let limitUpperPosX = boardWidth - buttonSize * 7 / 2 - marginX
            let limitLowerPosX = buttonSize * 7 / 2 + marginX
            let posX : CGFloat =  max(min(pTile.tileVM.frame.origin.x, limitUpperPosX), limitLowerPosX)
            
            var posY : CGFloat
            if pTile.tileVM.frame.origin.y - pTile.device.tileSize / 2 > 0 {
                posY = pTile.tileVM.frame.origin.y - pTile.device.tileSize / 2
            }
            else {
                posY = pTile.tileVM.frame.origin.y + pTile.device.tileSize * 1.5
            }
            
            posXY = CGPoint(x: posX, y: posY)
            
            // Highlight square being modified
            pTile.tileVM.setHighLightFull(pActive: true)
            
            visible = true
        }
        
        
    }
    
    
    /// Close the view
    
    func Close() {
        BoardViewModel.shared.boardLayout.setHighLightFull(pBits: 0)
        lastTileIndexTapped = -1
        visible = false
    }
    
    
    /// Calculates the size of the button based on the tile size
    /// - Parameter pTileSize: The size of the tile
    func calcButtonSize(pTileSize: CGFloat) -> CGFloat {
        if (pTileSize > 90) {
            return pTileSize * 0.85
        }
        else {
            return pTileSize * 0.95
        }
    }
}



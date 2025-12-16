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

@MainActor class TilePanelViewModel: ObservableObject {
    
    @Published var tileList : [TileView]
  

    /// Constructor
    init() {
        tileList = [TileView]()
        for index in 0...63 {
            tileList.append(TileView(index: index))
        }
    }
    
    /// Gets a tile
    /// - Parameter pIndex: Index of tile to get
    /// - Returns: A tile
    func getTile(pIndex: Int) -> TileView {
        return tileList[pIndex]
    }
    
    
    /// Sets tile to show image of spin
    /// - Parameters:
    ///   - pIndex: Square Index
    ///   - pSpin: Spin value of piece
    func setPiece(pIndex: Int, pSpin: Int) {
        if(pIndex >= 0 && pIndex <= 63 && pSpin >= -6 && pSpin <= 6){
            let tile = tileList[pIndex]
            if (tile.tileVM.spin != pSpin) {
                tile.tileVM.pieceName = getImageName(pSpin: pSpin)
                tile.tileVM.spin = pSpin
            }
        }
    }
    
    
    /// Turn edit mode on or off
    func editMode(pEnable: Bool) {
        if (pEnable) {
            for tile in tileList {
                tile.tileVM.editMode = true
            }
        } else {
            for tile in tileList {
                tile.tileVM.editMode = false
            }
        }
    }
    
    /// Get image name of spin
    /// - Parameter pSpin: Spin to get
    func getImageName(pSpin: Int) -> String {
        let largePawn = PieceSettingsViewModel.instance.largePawn
        
        switch pSpin {
            case 6: return "WhiteKing"
            case 5: return "WhiteQueen"
            case 4: return "WhiteRook"
            case 3: return "WhiteBishop"
            case 2: return "WhiteKnight"
            case 1: return largePawn ? "WhitePawnLarge" : "WhitePawn"
            case -6: return "BlackKing"
            case -5: return "BlackQueen"
            case -4: return "BlackRook"
            case -3: return "BlackBishop"
            case -2: return "BlackKnight"
            case -1: return largePawn ? "BlackPawnLarge" : "BlackPawn"
            default: return ""
        }
    }
    
    
    /// Highlights squares as per pBits
    /// - Parameter pBits: Bits to highlight
    func setHighLight(pBits: UInt64) {
        if Device.instance.tileSize > 0 {
            
            for i in 0...63 {
                if ((Constants.BITMASK >> i) & pBits) > 0 {
                    tileList[i].tileVM.highlight = true
                }
                else {
                    tileList[i].tileVM.highlight = false
                }
            }
        }
    }
    
    /// Full square highlight - magenta
    /// - Parameter pBits: Bits to highlight
    func setHighLightFull(pBits: UInt64, pColour: Color) {
        if Device.instance.tileSize > 0 {
            
            for i in 0...63 {
                if ((Constants.BITMASK >> i) & pBits) > 0 {
                    tileList[i].tileVM.setHighLightFull(pActive: true, pColour: pColour)
                }
                else {
                    tileList[i].tileVM.setHighLightFull(pActive: false, pColour: pColour)
                }
            }
        }
    }
    
    /// Full square highlight - magenta with fade out
    /// - Parameter pBits: Bits to highlight
    func setHighLightFullFadeOut(pBits: UInt64, pColour: Color) {
        if Device.instance.tileSize > 0 {
            
            for i in 0...63 {
                if ((Constants.BITMASK >> i) & pBits) > 0 {
                    tileList[i].tileVM.setHighLightFullFadeOut(pActive: true, pColour: pColour)
                }
                else {
                    tileList[i].tileVM.setHighLightFullFadeOut(pActive: false, pColour: pColour)
                }
                
                
            }
        }
    }
    
    /// Highlights squares in edit mode as per pBits
    /// - Parameter pBits: Bits to highlight
    func setHighLightEdit(pBits: UInt64) {
        if Device.instance.tileSize > 0 {
            
            for i in 0...63 {
                if ((Constants.BITMASK >> i) & pBits) > 0 {
                    tileList[i].tileVM.highlightEdit = true
                }
                else {
                    tileList[i].tileVM.highlightEdit = false
                }
            }
        }
    }
    
    /// Set check indicator
    /// - Parameter pKingIndex: The index of the king
    func setCheckIndicator(pKingIndex: Int) {
        for i in 0...63 {
            if i == pKingIndex {
                tileList[i].tileVM.checkIndicator = true
            }
            else {
                tileList[i].tileVM.checkIndicator = false
            }
        }
    }
    
    /// Refresh board piece images
    func refreshPawns() {
        for i in 0...63{
            let tile = tileList[i]
            if tile.tileVM.spin == 1 || tile.tileVM.spin == -1 {
               tile.tileVM.pieceName = getImageName(pSpin: tile.tileVM.spin)
            }
        }
    }
    
    
}

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

struct TilePanelView: View {
    
    @State private var tileList : [TileView]
    
    
    /// Constructor
    init() {
        var tileListInit = [TileView]()
        for index in 0...63 {
            tileListInit.append(TileView(index: index))
        }
        
        _tileList = State(initialValue: tileListInit)
        
    }
    
    
    /// View
    var body: some View {
        
        VStack(spacing: 0) {
            ForEach(0 ..< 8) {row in
                HStack(spacing: 0){
                    ForEach(0 ..< 8) {col -> TileView in
                        self.getTile(pIndex: (row * 8 + col))
                    }
                }
            }
        }.onPreferenceChange(TileFramePreferenceKey.self){preferences in
            for p in preferences {
                self.tileList[p.tileIndex].tileVM.frame = p.tileFrame
            }
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
    func setPiece(pIndex: Int, pSpin: Int){
        if(pIndex >= 0 && pIndex <= 63 && pSpin >= -6 && pSpin <= 6){
            let tile = tileList[pIndex]
            if(tile.tileVM.spin != pSpin) {
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
        switch pSpin {
            case 6: return "WhiteKing"
            case 5: return "WhiteQueen"
            case 4: return "WhiteRook"
            case 3: return "WhiteBishop"
            case 2: return "WhiteKnight"
            case 1: return "WhitePawn"
            case -6: return "BlackKing"
            case -5: return "BlackQueen"
            case -4: return "BlackRook"
            case -3: return "BlackBishop"
            case -2: return "BlackKnight"
            case -1: return "BlackPawn"
            default: return ""
        }
    }
    
    
    /// Highlights squares as per pBits
    /// - Parameter pBits: Bits to highlight
    func setHighLight(pBits: UInt64) {
        if Device.shared.tileSize > 0 {
            
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
    func setHighLightFull(pBits: UInt64) {
        if Device.shared.tileSize > 0 {
            
            for i in 0...63 {
                if ((Constants.BITMASK >> i) & pBits) > 0 {
                    tileList[i].tileVM.highlightFull = true
                }
                else {
                    tileList[i].tileVM.highlightFull = false
                }
                
                
            }
        }
    }
    
    /// Full square highlight - magenta with fade out
    /// - Parameter pBits: Bits to highlight
    func setHighLightFullFadeOut(pBits: UInt64) {
        if Device.shared.tileSize > 0 {
            
            for i in 0...63 {
                if ((Constants.BITMASK >> i) & pBits) > 0 {
                    tileList[i].tileVM.setHighLightFullFadeOut(pActive: true)
                }
                else {
                    tileList[i].tileVM.setHighLightFullFadeOut(pActive: false)
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
    
    
    /// Set all tiles to visible
    func showAll() {
        for i in 0...63 {
            tileList[i].tileVM.visible = true
        }
    }
    

}




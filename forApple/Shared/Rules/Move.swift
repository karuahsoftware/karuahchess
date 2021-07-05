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

class Move {
    
    private let tilePanel: TilePanelView
    var fromIndex: Int = -1
    var toIndex: Int = -1
    
    enum HighlightEnum : Int { case None, MovePath, Select}
    
    init(pTilePanel: TilePanelView) {
        tilePanel = pTilePanel
    }
    
    
    /// Add a square to the move
    /// - Parameters:
    ///   - pBoardSquareIndex: Square Index to add
    ///   - pBoard: The chess engine
    ///   - pHighlight: Type of highlight to perform
    /// - Returns: True when both from and to squares have been selected
    func add(pBoardSquareIndex: Int, pBoard: KaruahChessEngineC, pHighlight: HighlightEnum) -> Bool {
        var complete = false
        
        if fromIndex == pBoardSquareIndex {
            clear()
        }
        else if fromIndex == -1 && toIndex == -1 {
            fromIndex = pBoardSquareIndex
            if pHighlight == HighlightEnum.MovePath {
                var sqMark = pBoard.getPotentialMove(Int32(pBoardSquareIndex))
                if sqMark & (Constants.BITMASK >> fromIndex) <= 0 {
                    sqMark = sqMark | (Constants.BITMASK >> fromIndex)
                }
                
                DispatchQueue.main.async {
                    self.tilePanel.setHighLight(pBits: sqMark)
                }
            }
            else if pHighlight == HighlightEnum.Select {
                let sqMark = Constants.BITMASK >> fromIndex
                DispatchQueue.main.async {
                    self.tilePanel.setHighLight(pBits: sqMark)
                }
            }
        }
        else if fromIndex > -1 && toIndex == -1 {
            toIndex = pBoardSquareIndex
            complete = true
            DispatchQueue.main.async {
                self.tilePanel.setHighLight(pBits: 0)
            }
        }
        else {
            clear()
        }
        
        return complete
    }
    
    
    /// Clear the move
    func clear() {
       fromIndex = -1
        toIndex = -1
        DispatchQueue.main.async {
            self.tilePanel.setHighLight(pBits: 0)
        }
    }
}

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

class BoardSquareDataService {
    static var gameRecordCurrentValue = 0
    private static let tempBoard = KaruahChessEngineC()
    
    static func update(pTilePanel: TilePanelView, pRecord: GameRecordArray) {
        tempBoard.setBoardArray(pRecord.boardArray)
        tempBoard.setStateArray(pRecord.stateArray)
        
        for index: Int in 0...63  {
            let spin = tempBoard.getSpin(Int32(index))
            
            DispatchQueue.main.async {
                pTilePanel.setPiece(pIndex: index, pSpin: Int(spin))
            }
        }
    }
}

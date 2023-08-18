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

@MainActor class BoardSquareDataService : ObservableObject {
    static let instance = BoardSquareDataService()
    
    @Published var gameRecordCurrentValue = 0
    private let tempBoard = KaruahChessEngineC()
    
    private init() {
        // Ensures only once instance of the class is created
    }
    
    /// Updates a tile panel with a given record
    func update(pTilePanelVM: TilePanelViewModel, pRecord: GameRecordArray) {
        tempBoard.setBoardArray(pRecord.boardArray)
        tempBoard.setStateArray(pRecord.stateArray)
    
        for index: Int in 0...63  {
            let spin = tempBoard.getSpin(Int32(index))
            pTilePanelVM.setPiece(pIndex: index, pSpin: Int(spin))
            pTilePanelVM.getTile(pIndex: index).tileVM.visible = true
        }
    }
    
    /// Show all tiles
    func showAll(pTilePanelVM: TilePanelViewModel) {
        for index: Int in 0...63  {
            pTilePanelVM.getTile(pIndex: index).tileVM.visible = true
        }
    }
    
    
}

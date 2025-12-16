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

@MainActor class CastlingRightsViewModel: ObservableObject {
    @Published var kingSpin : Int = 0
    @Published var visible : Bool = false
    @Published var lastTileIndexTapped : Int = -1
    @Published var queenSideCastle : Bool = false
    @Published var kingSideCastle : Bool = false
    let board: KaruahChessEngineC = KaruahChessEngineC()
    var recordId: Int = 0
    
    /// Displays the castling rights view
    /// - Parameter pTile: The tile that was clicked
    func show(_ pTile: TileView, _ pRecord: GameRecordArray) {
        if visible == true {
            close()
        }
        else {
            // Show the view and put it in the correct position
            lastTileIndexTapped = pTile.index
            kingSpin = pTile.tileVM.spin
            
            // Set the control state
            setControlState(pRecord)
            
            visible = true
        }
        
        
    }
    
    
    /// Close the view
    
    func close() {
        // save before closing
        if visible {save()
            lastTileIndexTapped = -1
            visible = false
        }
        
    }
    
    
    /// Enables and disables controls depending on options set
    private func setControlState(_ pRecord: GameRecordArray) {
        recordId = pRecord.id
        board.setBoardArray(pRecord.boardArray)
        board.setStateArray(pRecord.stateArray)
        
        let stateCastlingAvailability: Int = Int(board.getStateCastlingAvailability())
        if kingSpin == Constants.WHITE_KING_SPIN {
            kingSideCastle = (stateCastlingAvailability & 0b000010) > 0
            queenSideCastle = (stateCastlingAvailability & 0b000001) > 0
        }
        else {
            kingSideCastle = (stateCastlingAvailability & 0b001000) > 0
            queenSideCastle = (stateCastlingAvailability & 0b000100) > 0
        }
    }
    
    
    /// Saves form values
    private func save(){

       // Load the record in to a board
       var success: Bool = false

       // Check that castling selection is valid
       if (kingSpin == Constants.WHITE_KING_SPIN) {
            var stateCastlingAvailability: Int = 0
            if (kingSideCastle) {stateCastlingAvailability = stateCastlingAvailability | 0b000010 }
            if (queenSideCastle) {stateCastlingAvailability = stateCastlingAvailability | 0b000001 }
            success = board.setStateCastlingAvailability(Int32(stateCastlingAvailability), Int32(Constants.WHITEPIECE))
       }
       else if (kingSpin == Constants.BLACK_KING_SPIN) {
           var stateCastlingAvailability: Int = 0
            if (kingSideCastle) {stateCastlingAvailability = stateCastlingAvailability | 0b001000}
            if (queenSideCastle) {stateCastlingAvailability = stateCastlingAvailability | 0b000100}
            success = board.setStateCastlingAvailability(Int32(stateCastlingAvailability), Int32(Constants.BLACKPIECE))
       }

       // Save the values
       if (success) {
           let updatedRecord = GameRecordArray(pId: recordId, pBoardArray: board.getBoardArraySafe(), pStateArray: board.getStateArraySafe(), pMoveSAN: "")
           _ = GameRecordDataService.instance.updateGameState(pGameRecordArray: updatedRecord)
           BoardViewModel.instance.updateBoardIndicators(pRecord: updatedRecord)
       }
       else {
            BoardViewModel.instance.showMessage(pTextFull: "Error, cannot set castling rights as Rook or King position is not valid for castling.", pTextShort: "", pDurationms: Constants.TOAST_LONG)
       }

    }
    
}





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

class PawnPromotionViewModel: ObservableObject {
    @Published var visible : Bool = false
    var promotionPiece: Int = 0
    var colour: Int = 0
    
    private var waitCount = 0
    private var waiter: CheckedContinuation<Void, Never>?

    
    /// Displays the piece edit tool
    /// - Parameter pTile: The tile that was clicked
    func show(pColour: Int) async -> Int {
        
        if (waitCount == 0) {
            colour = pColour
            
            // Ensure nothing is higlighted
            DispatchQueue.main.async {
                self.visible = true
            }
            
            await withCheckedContinuation { continuation in
                waitCount += 1
                waiter = continuation
            }
            
        }
        
        return promotionPiece
    }
    
    
    /// Close the view
    
   func close() async {
    
       DispatchQueue.main.async {
           self.visible = false
       }
       
       waiter?.resume()
       waiter = nil
       waitCount = 0
       
   }
    
    // Cancel without selecting a piece
    func cancel() async {
        promotionPiece = 0
        await close()
    }
    
    // Gets the wait count
    func getWaitCount() async -> Int {
        return waitCount
    }
    
    
    
    
    
}





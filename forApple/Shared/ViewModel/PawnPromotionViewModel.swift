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

class PawnPromotionViewModel: ObservableObject {
    @Published var visible : Bool = false
    var promotionPiece: Int = 0
    var colour: Int = 0
    
    private let semaphore = DispatchSemaphore(value: 0)
    private var waitCount = 0
    //private var waitTask: Int
    
    
    /// Displays the piece edit tool
    /// - Parameter pTile: The tile that was clicked
    func show(pColour: Int) async -> Int {
        
        colour = pColour
        
        // Ensure nothing is higlighted
        self.visible = true
        
        // Switch to a background thread
        let waitTask = Task.detached(priority: .background) { [self] in
            waitCount -= 1
            semaphore.wait()
        }
        
        _ = await waitTask.result
        
        return promotionPiece
    }
    
    
    /// Close the view
    
   func close() async {
       visible = false
       let waitTask = Task.detached(priority: .userInitiated) { [self] in
            while waitCount < 0 {
                waitCount += 1
                semaphore.signal()
            }
       }
       
       _ = await waitTask.result
       
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





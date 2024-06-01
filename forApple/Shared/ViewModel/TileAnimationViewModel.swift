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

@MainActor class TileAnimationViewModel: ObservableObject {
    
    @Published var animationInstructionArray: [TileAnimationInstruction] = []
    @Published var visible: Bool = false
    @Published var duration: Double = 0
    @Published var waiter: CheckedContinuation<Void, Never>?
    @Published var waitCount = 0
    
    /// Clear all animation instructions
   func clear() {
        visible = false
        animationInstructionArray.removeAll()
        
   }
       
       
       /// Runs the next animation
       /// - Parameters:
       ///   - pBoardSquareDS: Board square data service
       ///   - pAnimationList: The list of animation instructions to run
    func runAnimation(pAnimationList: [TileAnimationInstruction], pTilePanelVM: TilePanelViewModel, pDuration: Double) async {
        if (waitCount == 0) {
            clear()
            duration = pDuration
            
            // Loop through all the animation instructions
            for animInstruct in pAnimationList {
                
                // Add the animation to the array
                animationInstructionArray.append(animInstruct)
                
                // Clear the squares being animated
                for index in animInstruct.hiddenSquareIndexes {
                    pTilePanelVM.getTile(pIndex: index).tileVM.visible = false
                    
                }
                
            }
            
            visible = true
            
            await withCheckedContinuation { continuation in
                waitCount += 1
                waiter = continuation
            }
        }
    }
    
    
    
}

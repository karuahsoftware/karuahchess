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

extension KaruahChessEngineC {
    func getBoardArraySafe() -> [UInt64] {
        let arrayPointer = UnsafeMutablePointer<UInt64>.allocate(capacity: 276)
        self.getBoardArray(arrayPointer)
        let array = Array(UnsafeBufferPointer(start: arrayPointer, count: 276))
        arrayPointer.deinitialize(count: 276)
        arrayPointer.deallocate()
        return array
    }
    
    func getStateArraySafe() -> [Int32] {
        let arrayPointer = UnsafeMutablePointer<Int32>.allocate(capacity: 8)
        self.getStateArray(arrayPointer)
        let array = Array(UnsafeBufferPointer(start: arrayPointer, count: 8))
        arrayPointer.deinitialize(count: 8)
        arrayPointer.deallocate()
        return array
    }
    
}


extension View {
    func modify<T: View>(@ViewBuilder _ modifier: (Self) -> T) -> some View {
        return modifier(self)
    }
    
    func onAnimationCompleted<Value: VectorArithmetic>(for value: Value, completion: @escaping () -> Void) -> ModifiedContent<Self, AnimationCompletionObserverModifier<Value>> where Value: Sendable {
        return modifier(AnimationCompletionObserverModifier(observedValue: value, completion: completion))
        
    }
    
}



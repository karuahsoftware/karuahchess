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

@objc final class SearchResult: NSObject, Sendable {
    @objc let moveFromIndex: Int32
    @objc let moveToIndex: Int32
    @objc let promotionPieceType: Int32
    @objc let cancelled: Bool
    @objc let error: Int
    @objc let errorMessage: String
    
    @objc init(pMoveFromIndex: Int32, pMoveToIndex: Int32, pPromotionPieceType: Int32, pCancelled: Bool, pError: Int, pErrorMessage: String) {
        moveFromIndex = pMoveFromIndex
        moveToIndex = pMoveToIndex
        promotionPieceType = pPromotionPieceType
        cancelled = pCancelled
        error = pError
        errorMessage = pErrorMessage
    }
}

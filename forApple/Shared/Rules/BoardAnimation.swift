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

@MainActor class BoardAnimation {
    private let tempBoardBefore = KaruahChessEngineC()
    private let tempBoardAfter = KaruahChessEngineC()
    
    
    /// Creates a move animation sequence
    /// - Parameters:
    ///   - pBoardRecA: The first board record
    ///   - pBoardRecB: The second board record
    ///   - pBoardSquareDS: Board square data service
    /// - Returns: A list of tile animation instructions
    func createAnimationList(pBoardRecA: GameRecordArray, pBoardRecB: GameRecordArray) -> [TileAnimationInstruction]{
        var animationList = [TileAnimationInstruction]()
        let moveList = getAnimationMoveList(pBoardRecBefore: pBoardRecA, pBoardRecAfter: pBoardRecB)
        
        for move in moveList {
            let spin = move[0]
            let fromIndex = move[1]
            let toIndex = move[2]
            
            if fromIndex > -1 && toIndex > -1 {
                // Move animation
                let imageData = Image(BoardViewModel.instance.tilePanelVM.getImageName(pSpin: spin))
                if var instruction = getAnimationInstruction(pFromIndex: fromIndex, pToIndex: toIndex, pTilePanelVM: BoardViewModel.instance.tilePanelVM, pImage: imageData) {
                    instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Move
                    animationList.append(instruction)
                }
            }
            else if fromIndex > -1 && toIndex == -1 {
                // Look for a pawn promotion move
                var promotionIndex = -1
                if spin == 1 || spin == -1 {
                    for nestedMove in moveList {
                        let nestedSpin = nestedMove[0]
                        let nestedFromIndex = nestedMove[1]
                        let nestedToIndex = nestedMove[2]
                        if (spin == 1 && nestedSpin > 1 && nestedFromIndex == -1 && nestedToIndex > -1) || (spin == -1 && nestedSpin < -1 && nestedFromIndex == -1 && nestedToIndex > -1) {
                            promotionIndex = nestedToIndex
                        }
                    }
                }
                
                if promotionIndex > -1 {
                    // Pawn promotion animation
                    let imageData = Image(BoardViewModel.instance.tilePanelVM.getImageName(pSpin: spin))
                    if var instruction = getAnimationInstruction(pFromIndex: fromIndex, pToIndex: promotionIndex, pTilePanelVM: BoardViewModel.instance.tilePanelVM, pImage: imageData) {
                        instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.MoveFade
                        animationList.append(instruction)
                    }
                }
                else {
                    // Piece take animation
                    let imageData = Image(BoardViewModel.instance.tilePanelVM.getImageName(pSpin: spin))
                    if var instruction = getAnimationInstruction(pFromIndex: fromIndex, pToIndex: fromIndex, pTilePanelVM: BoardViewModel.instance.tilePanelVM, pImage: imageData) {
                        instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Take
                        animationList.append(instruction)
                    }
                }
            }
            else if fromIndex == -1 && toIndex > -1 {
                // Piece return animation
                let imageData = Image(BoardViewModel.instance.tilePanelVM.getImageName(pSpin: spin))
                if var instruction = getAnimationInstruction(pFromIndex: toIndex, pToIndex: toIndex, pTilePanelVM: BoardViewModel.instance.tilePanelVM, pImage: imageData) {
                    instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Put
                    animationList.append(instruction)
                }
                
            }
            
        }
        
        return animationList
    }
    
    
    /// Creates fall animation
    /// - Parameters:
    ///   - pIndex: The piece the animate
    ///   - pTilePanel: The board
    /// - Returns: An animation instruction
    func createAnimationFall(pIndex: Int, pTilePanelVM: TilePanelViewModel) -> [TileAnimationInstruction] {
        var animationList = [TileAnimationInstruction]()
        let spin = pTilePanelVM.getTile(pIndex: pIndex).tileVM.spin
        
        if 0...63~=pIndex && spin != 0 {
            let imageData = Image(BoardViewModel.instance.tilePanelVM.getImageName(pSpin: spin))
            if var instruction = getAnimationInstruction(pFromIndex: pIndex, pToIndex: pIndex, pTilePanelVM: BoardViewModel.instance.tilePanelVM, pImage: imageData) {
                instruction.animationType = TileAnimationInstruction.AnimationTypeEnum.Fall
                animationList.append(instruction)
            }
        }
        
        return animationList
        
    }
    
    
    /// Detects changes between two bitboards and returns list of moves. Only works up to two different
    /// piece types. If more than two pieces changes, then the list returned is empty
    /// - Parameters:
    ///   - pBoardRecBefore: Game record before the change
    ///   - pBoardRecAfter: Game record after the change
    /// - Returns: A list of animations
    private func getAnimationMoveList(pBoardRecBefore: GameRecordArray, pBoardRecAfter: GameRecordArray) -> [[Int]] {
        
        tempBoardBefore.setBoardArray(pBoardRecBefore.boardArray)
        tempBoardBefore.setStateArray(pBoardRecBefore.stateArray)
        tempBoardAfter.setBoardArray(pBoardRecAfter.boardArray)
        tempBoardAfter.setStateArray(pBoardRecAfter.stateArray)
        
        let allBeforeWhitePos: UInt64 = tempBoardBefore.getOccupiedByColour(Int32(Constants.WHITEPIECE))
        let allBeforeBlackPos: UInt64 = tempBoardBefore.getOccupiedByColour(Int32(Constants.BLACKPIECE))
        let allAfterWhitePos: UInt64 = tempBoardAfter.getOccupiedByColour(Int32(Constants.WHITEPIECE))
        let allAfterBlackPos: UInt64 = tempBoardAfter.getOccupiedByColour(Int32(Constants.BLACKPIECE))
        
        let allChangeWhitePos = allBeforeWhitePos ^ allAfterWhitePos
        let allChangeBlackPos = allBeforeBlackPos ^ allAfterBlackPos
        
        var spinChange = Array(repeating: Array(repeating: 0, count: 2), count: 13)
        createChangeArray(pChangedPositions: allChangeWhitePos, pSpinChange: &spinChange)
        createChangeArray(pChangedPositions: allChangeBlackPos, pSpinChange: &spinChange)
        var spinChangeList = convertSpinChangeArrayToList(pSpinChange: spinChange)
        
        // Too many changes to animate so just clear the list
        if spinChangeList.count > 3 {
            spinChangeList.removeAll()
        }
        
        return spinChangeList
        
    }
    
    
    /// Converts a spin change array to a list of changes
    /// - Parameter pSpinChange: The spin changes
    /// - Returns: A list of the changes
    private func convertSpinChangeArrayToList(pSpinChange: [[Int]]) -> [[Int]] {
        let spinOffset = 6
        let sqIndexOffset = 1
        
        // Build the list of changes
        var pieceChangeList: [[Int]] = []
        for i in 0...12 {
            if pSpinChange[i][0] > 0 || pSpinChange[i][1] > 0 {
                var change: [Int] = [i - spinOffset, -1, -1]
                if pSpinChange[i][0] > 0 {
                    change[1] = pSpinChange[i][0] - sqIndexOffset
                }
                
                if pSpinChange[i][1] > 0 {
                    change[2] = pSpinChange[i][1] - sqIndexOffset
                }
                
                pieceChangeList.append(change)
            }
        }
        
        return pieceChangeList
    }
    
    /// Creates an array of spin changes
    /// - Parameters:
    ///   - pChangedPositions: The positions that have changed
    ///   - pSpinChange: The spin changes to return
    private func createChangeArray(pChangedPositions: UInt64, pSpinChange: inout [[Int]]) {
        let spinOffset = 6
        let sqIndexOffset = 1
        
        var changedPositions: UInt64 = pChangedPositions
        while changedPositions > 0 {
            let pos: Int = Int(tempBoardBefore.bitscanForward(changedPositions))
            let sqMask: UInt64 = 1 << pos
            changedPositions ^= sqMask
            let sqIndex: Int = 63 - pos
            
            let beforeSpin: Int = Int(tempBoardBefore.getSpin(Int32(sqIndex)))
            let afterSpin: Int = Int(tempBoardAfter.getSpin(Int32(sqIndex)))
            
            if (beforeSpin != 0) {
                pSpinChange[beforeSpin + spinOffset][0] = sqIndex + sqIndexOffset
            }
            
            if (afterSpin != 0) {
                pSpinChange[afterSpin + spinOffset][1] = sqIndex + sqIndexOffset
            }
            
        }
        
    }
    
    
    
    
    
    /// Creates an animation instruction
    /// - Parameters:
    ///   - pFromIndex: From Index
    ///   - pToIndex: To Index
    ///   - pImage: Image
    /// - Returns: An animation instruction
    private func getAnimationInstruction(pFromIndex: Int, pToIndex: Int, pTilePanelVM: TilePanelViewModel, pImage: Image) -> TileAnimationInstruction? {
        
        if  0...63 ~= pFromIndex && 0...63 ~= pToIndex {
        
            let fromTile: TileView = pTilePanelVM.getTile(pIndex: pFromIndex)
            let toTile: TileView = pTilePanelVM.getTile(pIndex: pToIndex)
            let fromPoint: CGPoint = CGPoint(x: fromTile.tileVM.boardFrame.midX, y: fromTile.tileVM.boardFrame.midY)
            let toPoint: CGPoint = CGPoint(x: toTile.tileVM.boardFrame.midX, y: toTile.tileVM.boardFrame.midY)
            
            let animInstruct = TileAnimationInstruction(animationType: TileAnimationInstruction.AnimationTypeEnum.Move, imageData: pImage, moveFrom: fromPoint, moveTo: toPoint, hiddenSquareIndexes: [fromTile.index, toTile.index])
            
            return animInstruct
        }
        else {
            return nil
        }
        
    }
 
}

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

#import <Foundation/Foundation.h>
#import "KaruahChessEngineC.h"
#import "cpp/engine.h"
#import "cpp/bitboard.h"
#import "cpp/search.h"
#import "cpp/moverules.h"
#import "cpp/helper.h"
#import "cpp/sf_evaluate.h"

#if TARGET_OS_IPHONE
    #import <UIKit/UIKit.h>
#else
    #import <AppKit/AppKit.h>
#endif


#import <Speech/Speech.h>  // Required for auto generated bridging header
#import "Karuah_Chess-Swift.h"  // Auto generated bridging header

@implementation KaruahChessEngineC

using namespace helper;

// Constructor
- (id) init {
    
    if (self = [super init]) {
        Engine::init();
        
        if (!(Engine::nnueLoadedBig && Engine::nnueLoadedSmall))
        {
            const char* nnueFileNameBig = "nn-b1a57edbea57";
            const char* nnueFileNameSmall = "nn-baff1ede1f90";
            
            NSString *nnueFilePathBig = [[NSBundle mainBundle] pathForResource:[NSString stringWithUTF8String:nnueFileNameBig] ofType:@"nnue"];
            NSData *nnueNSDataBig = [NSData dataWithContentsOfFile:nnueFilePathBig];

            NSString *nnueFilePathSmall = [[NSBundle mainBundle] pathForResource:[NSString stringWithUTF8String:nnueFileNameSmall] ofType:@"nnue"];
            NSData *nnueNSDataSmall = [NSData dataWithContentsOfFile:nnueFilePathSmall];

            if (nnueNSDataBig != nil && nnueNSDataSmall != Nil) {
                char *nnueDataBig = (char *)[nnueNSDataBig bytes];
                long nnueDataSizeBig =  (long)[nnueNSDataBig length];
                char *nnueDataSmall = (char *)[nnueNSDataSmall bytes];
                long nnueDataSizeSmall =  (long)[nnueNSDataSmall length];
                
                Engine::init(nnueFileNameBig, nnueDataBig, nnueDataSizeBig, nnueFileNameSmall, nnueDataSmall, nnueDataSizeSmall);
                
            } else {
                Engine::engineErr.add(helper::NNUE_FILE_OPEN_ERROR);
            }
        }
    }
    
    return self;
}

// Gets a board FEN string
- (NSString * _Nonnull) getBoard {
    return [NSString stringWithUTF8String:MainBoard.GetBoard().c_str()];
}

// Get board state string
- (NSString * _Nonnull) getState {
    return [NSString stringWithUTF8String:MainBoard.GetState().c_str()];
}

// Set board from FEN string
- (void) setBoard:(const NSString * _Nonnull) pBoardFENString {
    MainBoard.SetBoard(std::string([pBoardFENString UTF8String]));
}

// Set board state from string
- (void) setState:(const NSString * _Nonnull) pBoardStateString {
    MainBoard.SetState(std::string([pBoardStateString UTF8String]));
}

// Get board array
- (void) getBoardArray:(uint64_t * _Nonnull)pBoardArray {
    MainBoard.GetBoardArray(pBoardArray);
}

// Get state array
- (void) getStateArray:(int32_t * _Nonnull) pStateArray {
    MainBoard.GetStateArray(pStateArray);
}

// Set board array
- (void) setBoardArray:(const uint64_t * _Nonnull) pBoardArray {
    MainBoard.SetBoardArray(pBoardArray);
}

// Set state array
- (void) setStateArray:(const int32_t * _Nonnull) pStateArray {
    MainBoard.SetStateArray(pStateArray);
}

// Set white clock offset
- (void) setStateWhiteClockOffset:(const int32_t) pOffset {
    MainBoard.StateWhiteClockOffset = pOffset;
}

// Set black clock offset
- (void) setStateBlackClockOffset:(const int32_t) pOffset {
    MainBoard.StateBlackClockOffset = pOffset;
}


// Get white clock offset
- (int32_t) getStateWhiteClockOffset {
    return MainBoard.StateWhiteClockOffset;
}

// Get black clock offset
- (int32_t) getStateBlackClockOffset {
    return MainBoard.StateBlackClockOffset;
}

// Reset the board
- (void) reset {
    MainBoard.Reset();
    SearchBoard.Reset();
    Search::Cancel();
    Search::ClearCache();
}

// Cancels the search
- (void) cancelSearch {
    Search::Cancel();
}

// Get spin of an index
- (int32_t) getSpin:(const int32_t) pIndex {
    return MainBoard.GetSpin(pIndex);
}

// Get state active colour
- (int32_t) getStateActiveColour {
    return MainBoard.StateActiveColour;
}

// Set state active colour
- (void) setStateActiveColour:(const int32_t) pColour {
    if (pColour != MainBoard.StateActiveColour) {
        // Clear the enpassant if changing the colour
        MainBoard.StateEnpassantIndex = -1;
        
        // Change colour
        MainBoard.StateActiveColour = pColour;
    }
}

// Get state game status
- (int32_t) getStateGameStatus {
    return MainBoard.StateGameStatus;
}

// Set state game status
- (void) setStateGameStatus:(const int32_t) pStatus {
    MainBoard.StateGameStatus = pStatus;
}

// Get full move count
- (int32_t) getStateFullMoveCount {
    return MainBoard.StateFullMoveCount;
}

// Get castling availability
- (int32_t) getStateCastlingAvailability {
    return MainBoard.StateCastlingAvailability;
}

// Set castling availability
- (bool) setStateCastlingAvailability:(const int32_t) pCastlingAvailability :(const int32_t) pColour {
    if (pColour == helper::WHITEPIECE) return MainBoard.setStateCastlingAvailability<helper::WHITEPIECE>(pCastlingAvailability);
    else return MainBoard.setStateCastlingAvailability<helper::BLACKPIECE>(pCastlingAvailability);
}

// Get king index
- (int32_t) getKingIndex:(const int32_t) pColour {
    if (pColour == WHITEPIECE) {
        return MainBoard.KingIndex<WHITEPIECE>();
    }
    else {
        return MainBoard.KingIndex<BLACKPIECE>();
    }
}

// Determine if king is in check
- (bool) isKingCheck:(const int32_t) pColour {
    return MainBoard.IsKingCheck(pColour);
}

// Get potential move of a given square index
- (uint64_t) getPotentialMove:(const int32_t) pSqIndex {
    return MainBoard.GetPotentialMove(pSqIndex);
}

// Get positions occupied by a particular spin
- (uint64_t) getOccupiedBySpin:(const int32_t) pSpin {
    return MainBoard.GetOccupiedBySpin(pSpin);
}

// Get positions occupied by a particular colour
- (uint64_t) getOccupiedByColour:(const int32_t) pColour {
    return MainBoard.GetOccupied(pColour);
}

// Moves a piece
- (NSObject * _Nonnull) move:(const int32_t)pFromIndex :(const int32_t)pToIndex :(const int32_t)pPawnPromotionPiece :(const bool)pValidateEnabled :(const bool)pCommit{
    helper::PawnPromotionEnum pawnPromotion = static_cast<helper::PawnPromotionEnum>(pPawnPromotionPiece);
    bool success = MoveRules::Move(pFromIndex, pToIndex, MainBoard, pawnPromotion, pValidateEnabled, pCommit);
    MoveResult *mResult = [[MoveResult alloc]init];
    mResult.success = success;
    mResult.returnMessage = [NSString stringWithUTF8String:MainBoard.ReturnMessage.c_str()];
    mResult.moveDataStr = [NSString stringWithFormat:@"%d|%d|%d|%d", MainBoard.MoveData[0], MainBoard.MoveData[1], MainBoard.MoveData[2], MainBoard.MoveData[3]];
    return mResult;
}

// Arrange a piece. Used for editing the board.
- (NSObject * _Nonnull) arrange:(const int32_t)pFromIndex :(const int32_t)pToIndex {
    bool success = MoveRules::Arrange(pFromIndex, pToIndex, MainBoard);
    
    if (success) {
        // Clear EnPassant
        MainBoard.StateEnpassantIndex = -1;
    }
    
    MoveResult *mResult = [[MoveResult alloc]init];
    mResult.success = success;
    mResult.returnMessage = [NSString stringWithUTF8String:MainBoard.ReturnMessage.c_str()];
    return mResult;
}

// Updates a piece on the board. Used for editing the board
- (NSObject * _Nonnull) arrangeUpdate:(const char)pFen :(const int32_t)pToIndex {
    bool success = MoveRules::ArrangeUpdate(pFen, pToIndex, MainBoard);
    
    if (success) {
        // Clear EnPassant
        MainBoard.StateEnpassantIndex = -1;
    }
    
    MoveResult *mResult = [[MoveResult alloc]init];
    mResult.success = success;
    mResult.returnMessage = [NSString stringWithUTF8String:MainBoard.ReturnMessage.c_str()];
    return mResult;

}

// Find valid from index from a given to index
- (int32_t) findFromIndex:(const int32_t)pToIndex :(const int32_t)pSpin :(NSArray<NSNumber *> * _Nonnull)pValidFromIndexes{
    std::vector<int> validFromIndexesVector;
    for(id validFromIndexObj in pValidFromIndexes) {
        validFromIndexesVector.push_back([validFromIndexObj intValue]);
    }
    int fromIndex = MoveRules::FindFromIndex(MainBoard, pToIndex, pSpin, validFromIndexesVector);
    return fromIndex;
}

// Get spin value from piece name
- (int32_t) getSpinFromPieceName:(const NSString * _Nonnull)pPieceName {
    return helper::GetSpinFromPieceName(std::string([pPieceName UTF8String]));
}

// Get piece name from a FEN char
- (NSString * _Nonnull) getPieceNameFromChar:(const char)pFenChar {
    return [NSString stringWithUTF8String:helper::GetPieceNameFromChar(pFenChar).c_str()];
}

// Get FEN char from spin
- (char) getFENCharFromSpin:(const int32_t)pSpin {
    return helper::GetFENCharFromSpin(pSpin);
}

// Get full FEN string of the board
- (NSString * _Nonnull) getFullFEN {
    return [NSString stringWithUTF8String:MainBoard.GetFullFEN().c_str()];
}

// Checks if a move is a pawn promoting move
- (bool) isPawnPromotion:(const int32_t)pFromIndex :(const int32_t)pToIndex {
    return MoveRules::IsPawnPromotion(pFromIndex, pToIndex, MainBoard);
}

// Searches for the best move
- (NSObject * _Nonnull) searchStart:(SearchOptions * _Nonnull)pSearchOptions {
    uint64_t boardArray[276];
    int32_t stateArray[8];
    
    MainBoard.GetBoardArray(boardArray);
    MainBoard.GetStateArray(stateArray);
    SearchBoard.SetBoardArray(boardArray);
    SearchBoard.SetStateArray(stateArray);
    
    Search::SearchTreeNode bestMove;
    Search::SearchStatistics statistics;
    Search::SearchOptions options;
    options.randomiseFirstMove = pSearchOptions.randomiseFirstMove;
    options.limitSkillLevel = pSearchOptions.limitSkillLevel;
    options.limitDepth = pSearchOptions.limitDepth;
    options.limitNodes = pSearchOptions.limitNodes;
    options.limitMoveDuration = pSearchOptions.limitMoveDuration;
    options.limitThreads = pSearchOptions.limitThreads;
    
    
    Search::GetBestMove(SearchBoard, options, bestMove, statistics);
    
    // Copy the values to result
    SearchResult *result = [[SearchResult alloc]init];
    result.moveFromIndex = bestMove.moveFromIndex;
    result.moveToIndex = bestMove.moveToIndex;
    result.promotionPieceType = bestMove.promotionPieceType;
    result.cancelled = bestMove.cancelled;
    result.error = bestMove.error;
    result.errorMessage = [NSString stringWithUTF8String:helper::SearchErrorMessage.at(bestMove.error).c_str()];
    return result;
}



// does a forward bitscan on the given number
- (int) bitscanForward:(const uint64_t)pNum {
    return helper::BitScanForward(pNum);
}

@end

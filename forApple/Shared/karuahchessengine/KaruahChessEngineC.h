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

#pragma once
#import <Foundation/Foundation.h>

#ifdef __cplusplus
#include "cpp/bitboard.h"
using namespace KaruahChess;
#endif

@interface KaruahChessEngineC : NSObject
#ifdef __cplusplus
{
    @private
    BitBoard MainBoard;
    BitBoard SearchBoard;
}
#endif

- (NSString * _Nonnull) getBoard;
- (NSString * _Nonnull) getState;
- (void) setBoard:(const NSString * _Nonnull) pBoardFENString;
- (void) setState:(const NSString * _Nonnull) pBoardStateString;
- (void) getBoardArray:(uint64_t * _Nonnull) pBoardArray;
- (void) getStateArray:(int32_t * _Nonnull) pStateArray;
- (void) setBoardArray:(const uint64_t * _Nonnull) pBoardArray;
- (void) setStateArray:(const int32_t * _Nonnull) pStateArray;
- (void) setStateWhiteClockOffset:(const int32_t) pOffset;
- (void) setStateBlackClockOffset:(const int32_t) pOffset;
- (int32_t) getStateWhiteClockOffset;
- (int32_t) getStateBlackClockOffset;
- (void) reset;
- (void) cancelSearch;
- (int32_t) getSpin:(const int32_t) pIndex;
- (int32_t) getStateActiveColour;
- (void) setStateActiveColour:(const int32_t) pColour;
- (int32_t) getStateGameStatus;
- (void) setStateGameStatus:(const int32_t) pStatus;
- (int32_t) getStateFullMoveCount;
- (int32_t) getStateCastlingAvailability;
- (bool) setStateCastlingAvailability:(const int32_t) pCastlingAvailability :(const int32_t) pColour;
- (int32_t) getKingIndex:(const int32_t) pColour;
- (bool) isKingCheck:(const int32_t) pColour;
- (uint64_t) getPotentialMove:(const int32_t) pSqIndex;
- (uint64_t) getOccupiedBySpin:(const int32_t) pSpin;
- (uint64_t) getOccupiedByColour:(const int32_t) pColour;
- (NSObject * _Nonnull) move:(const int32_t)pFromIndex :(const int32_t)pToIndex :(const int32_t)pPawnPromotionPiece :(const bool)pValidateEnabled :(const bool)pCommit;
- (NSObject * _Nonnull) arrange:(const int32_t)pFromIndex :(const int32_t)pToIndex;
- (NSObject * _Nonnull) arrangeUpdate:(const char)pFen :(const int32_t)pToIndex;
- (int32_t) findFromIndex:(const int32_t)pToIndex :(const int32_t)pSpin :(NSArray<NSNumber *> * _Nonnull)pValidFromIndexes NS_SWIFT_NAME(findFromIndex(pToIndex:pSpin:pValidFromIndexes:));
- (int32_t) getSpinFromPieceName:(const NSString * _Nonnull)pPieceName;
- (NSString * _Nonnull) getPieceNameFromChar:(const char)pFenChar NS_SWIFT_NAME(getPieceNameFromChar(pFenChar:));
- (char) getFENCharFromSpin:(const int32_t)pSpin NS_SWIFT_NAME(getFENCharFromSpin(pSpin:));
- (NSString * _Nonnull) getFullFEN;
- (bool) isPawnPromotion:(const int32_t)pFromIndex :(const int32_t)pToIndex;
- (NSObject * _Nonnull) searchStart:(NSObject * _Nonnull)pSearchOptions;
- (int) bitscanForward:(const uint64_t)pNum;

@end



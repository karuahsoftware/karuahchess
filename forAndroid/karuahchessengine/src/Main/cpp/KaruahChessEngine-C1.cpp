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

#include <jni.h>
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>
#include "helper.h"
#include "engine.h"
#include "bitboard.h"
#include "search.h"
#include "moverules.h"
#include "sf_evaluate.h"
#include <unordered_map>

using namespace KaruahChess;

std::unordered_map<int, BitBoard*> MainBoardMap;
std::unordered_map<int, BitBoard*> SearchBoardMap;


/// <summary>
/// Initialise helper function
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_initialise (
        JNIEnv* pEnv,
        jobject pThis,
        jobject pAssetMgr,
        jint pId)
{

    // Initialise with the NNUE file, if not already previously loaded
    if (!(Engine::nnueLoadedBig && Engine::nnueLoadedSmall))
    {
        const char* nnueFileNameBig = "nn-1111cefa1111.nnue";
        const char* nnueFileNameSmall = "nn-37f18f62d772.nnue";

        AAssetManager *mgr = AAssetManager_fromJava(pEnv, pAssetMgr);
        AAsset *nnueAssetBig = AAssetManager_open(mgr,nnueFileNameBig, AASSET_MODE_BUFFER);
        AAsset *nnueAssetSmall = AAssetManager_open(mgr,nnueFileNameSmall, AASSET_MODE_BUFFER);

        if (nnueAssetBig != NULL && nnueAssetSmall != NULL) {
            long nnueSizeBig = AAsset_getLength(nnueAssetBig);
            char *nnueBufferBig = (char *) malloc(nnueSizeBig);
            long nnueSizeSmall = AAsset_getLength(nnueAssetSmall);
            char *nnueBufferSmall = (char *) malloc(nnueSizeSmall);

            if (nnueBufferBig != NULL && nnueBufferSmall != NULL) {
                AAsset_read(nnueAssetBig, nnueBufferBig, nnueSizeBig);
                AAsset_read(nnueAssetSmall, nnueBufferSmall, nnueSizeSmall);
                Engine::init(nnueFileNameBig, nnueBufferBig, nnueSizeBig, nnueFileNameSmall, nnueBufferSmall, nnueSizeSmall);
                // Close the file asset
                AAsset_close(nnueAssetBig);
                AAsset_close(nnueAssetSmall);
            } else {
                Engine::engineErr.add(helper::NNUE_MEMORY_ALLOCATION_ERROR);
            }

        } else {
            Engine::engineErr.add(helper::NNUE_FILE_OPEN_ERROR);
        }

    }

    // Keep track of the engine object
    int id = pId;
    MainBoardMap.insert(std::pair<int, BitBoard*>(id, new BitBoard()));
    SearchBoardMap.insert(std::pair<int, BitBoard*>(id, new BitBoard()));

}

/// <summary>
/// Gets a board FEN string
/// </summary>
extern "C"
JNIEXPORT jstring JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getBoard (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return pEnv->NewStringUTF(boardItr->second->GetBoard().c_str());
    }
    else {
        return pEnv->NewStringUTF("");
    }

}

/// <summary>
/// Get positions occupied by the specified colour
/// </summary>
extern "C"
JNIEXPORT jlong JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getOccupiedByColour (
        JNIEnv* pEnv,
        jobject pThis,
        jint pColour,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->GetOccupied(pColour);
    }
    else {
        return 0;
    }
}

/// <summary>
/// Get board state string
/// </summary>
extern "C"
JNIEXPORT jstring JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getState (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return pEnv->NewStringUTF(boardItr->second->GetState().c_str());
    }
    else {
        return pEnv->NewStringUTF("");
    }



}

/// <summary>
/// Set board from FEN string
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setBoard (
        JNIEnv* pEnv,
        jobject pThis,
        jstring pBoardFENString,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        const char *fenChr = pEnv->GetStringUTFChars(pBoardFENString,0);
        std::string fenStr = fenChr;
        boardItr->second->SetBoard(fenStr);
        pEnv->ReleaseStringUTFChars(pBoardFENString,fenChr);
    }


}

/// <summary>
/// Set board state from string
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setState (
        JNIEnv* pEnv,
        jobject pThis,
        jstring pBoardStateString,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        const char *stateChr = pEnv->GetStringUTFChars(pBoardStateString, 0);
        std::string stateStr = stateChr;
        boardItr->second->SetState(stateStr);
        pEnv->ReleaseStringUTFChars(pBoardStateString, stateChr);
    }

}

/// <summary>
/// Get board array
/// </summary>
extern "C"
JNIEXPORT jlongArray JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getBoardArrayL (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        uint64_t boardArray[276];
        boardItr->second->GetBoardArray(boardArray);

        // Copy the array to native array
        jlong jboardArray[276] = {0};
        for (int i = 0; i < 276; ++i) jboardArray[i] = boardArray[i];
        jlongArray outArray = pEnv->NewLongArray(276);
        pEnv->SetLongArrayRegion(outArray, 0, 276, jboardArray);

        return outArray;
    } else{
        jlong jboardArray[276] = {0};
        jlongArray outArray = pEnv->NewLongArray(276);
        pEnv->SetLongArrayRegion(outArray, 0, 276, jboardArray);
        return outArray;
    }

}

/// <summary>
/// Get board array
/// </summary>
extern "C"
JNIEXPORT jintArray JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateArray (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        int32_t stateArray[8];
        boardItr->second->GetStateArray(stateArray);

        // Copy the array to native array
        jint jstateArray[8] = {0};
        for (int i = 0; i < 8; ++i) jstateArray[i] = stateArray[i];
        jintArray outArray = pEnv->NewIntArray(8);
        pEnv->SetIntArrayRegion(outArray,0,8,jstateArray);

        return outArray;
    } else {
        jint jstateArray[8] = {0};
        jintArray outArray = pEnv->NewIntArray(8);
        pEnv->SetIntArrayRegion(outArray,0,8,jstateArray);
        return outArray;
    }
}

/// <summary>
/// Set board array
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setBoardArrayL (
        JNIEnv* pEnv,
        jobject pThis,
        jlongArray pBoardArray,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {

        jlong jboardArray[276] = {0};
        pEnv->GetLongArrayRegion(pBoardArray, 0, 276, jboardArray);

        uint64_t boardArray[276];
        for (int i = 0; i < 276; ++i) boardArray[i] = (uint64_t) jboardArray[i];
        boardItr->second->SetBoardArray(boardArray);
    }
}

/// <summary>
/// Set state array
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setStateArray (
        JNIEnv* pEnv,
        jobject pThis,
        jintArray pStateArray,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        jint jstateArray[8] = {0};
        pEnv->GetIntArrayRegion(pStateArray, 0, 8, jstateArray);

        int32_t stateArray[8];
        for (int i = 0; i < 8; ++i) stateArray[i] = (int32_t) jstateArray[i];

        boardItr->second->SetStateArray(stateArray);
    }
}

/// <summary>
/// Set white clock offset
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setStateWhiteClockOffset (
        JNIEnv* pEnv,
        jobject pThis,
        jint pOffset,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        boardItr->second->StateWhiteClockOffset = pOffset;
    }
}

/// <summary>
/// Set black clock offset
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setStateBlackClockOffset (
        JNIEnv* pEnv,
        jobject pThis,
        jint pOffset,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        boardItr->second->StateBlackClockOffset = pOffset;
    }
}

/// <summary>
/// Get white clock offset
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateWhiteClockOffset (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->StateWhiteClockOffset;
    } else{
        return 0;
    }

}

/// <summary>
/// Get black clock offset
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateBlackClockOffset (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->StateBlackClockOffset;
    }
    else {
        return 0;
    }
}

/// <summary>
/// Resets the board
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_reset (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        boardItr->second->Reset();
    }

    auto searchItr = SearchBoardMap.find(pId);
    if(searchItr != SearchBoardMap.end()) {
       searchItr->second->Reset();
       Search::Cancel();
       Search::ClearCache();
    }


}

/// <summary>
/// Cancels the search
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_cancelSearch (
        JNIEnv* pEnv,
        jobject pThis)
{
    Search::Cancel();
}

/// <summary>
/// Get spin of an index
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getSpin (
        JNIEnv* pEnv,
        jobject pThis,
        jint pIndex,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->GetSpin(pIndex);
    }
    else {
        return 0;
    }
}

/// <summary>
///  Get state active colour
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateActiveColour (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->StateActiveColour;
    }
    else {
        return 0;
    }
}

/// <summary>
///  Set state active colour
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setStateActiveColour (
        JNIEnv* pEnv,
        jobject pThis,
        jint pColour,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        boardItr->second->StateActiveColour = pColour;
    }
}

/// <summary>
///  Get state game status
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateGameStatus (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->StateGameStatus;
    }
    else {
        return 0;
    }
}

/// <summary>
///  Set state game status
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setStateGameStatus (
        JNIEnv* pEnv,
        jobject pThis,
        jint pStatus,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        boardItr->second->StateGameStatus = pStatus;
    }
}

/// <summary>
///  Get full move count
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateFullMoveCount (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->StateFullMoveCount;
    }
    else {
        return 0;
    }
}

/// <summary>
///  Get castling availability
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getStateCastlingAvailability (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->StateCastlingAvailability;
    }
    else {
        return 0;
    }
}

/// <summary>
///  Get king index
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getKingIndex (
        JNIEnv* pEnv,
        jobject pThis,
        jint pColour,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        if (pColour == helper::WHITEPIECE) {
            return boardItr->second->KingIndex<helper::WHITEPIECE>();
        }
        else {
            return boardItr->second->KingIndex<helper::BLACKPIECE>();
        }
    }
    else {
        return 0;
    }
}

/// <summary>
///  Determine if king is in check
/// </summary>
extern "C"
JNIEXPORT jboolean JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_isKingCheck (
        JNIEnv* pEnv,
        jobject pThis,
        jint pColour,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        if (boardItr->second->IsKingCheck(pColour)) {
            return JNI_TRUE;
        } else {
            return JNI_FALSE;
        }
    }
    else {
        return JNI_FALSE;
    }
}

/// <summary>
///  Gets a potential move of a given square index
/// </summary>
extern "C"
JNIEXPORT jlong JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getPotentialMoveL (
        JNIEnv* pEnv,
        jobject pThis,
        jint pSqIndex,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        return boardItr->second->GetPotentialMove(pSqIndex);
    }
    else {
        return 0;
    }
}

/// <summary>
///  Moves a piece
/// </summary>
extern "C"
JNIEXPORT jobject JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_move (
        JNIEnv* pEnv,
        jobject pThis,
        jint pFromIndex,
        jint pToIndex,
        jint pPawnPromotionPiece,
        jboolean pValidateEnabled,
        jboolean pCommit,
        jint pId)
{

    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        helper::PawnPromotionEnum pawnPromotion = static_cast<helper::PawnPromotionEnum>(pPawnPromotionPiece);
        bool success = MoveRules::Move(pFromIndex, pToIndex, *boardItr->second, pawnPromotion,
                                       pValidateEnabled, pCommit);

        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/MoveResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        jfieldID successFieldID = pEnv->GetFieldID(mResultClass, "success", "Z");
        jfieldID returnMessageFieldID = pEnv->GetFieldID(mResultClass, "returnMessage",
                                                         "Ljava/lang/String;");
        jfieldID moveDataStrFieldID = pEnv->GetFieldID(mResultClass, "moveDataStr",
                                                       "Ljava/lang/String;");

        if (success) {
            pEnv->SetBooleanField(mResultObj, successFieldID, JNI_TRUE);
        } else {
            pEnv->SetBooleanField(mResultObj, successFieldID, JNI_FALSE);
        }
        jstring jRtnMessage = pEnv->NewStringUTF(boardItr->second->ReturnMessage.c_str());
        pEnv->SetObjectField(mResultObj, returnMessageFieldID, jRtnMessage);

        std::string moveDataStr = std::to_string(boardItr->second->MoveData[0]) + "|" +
                                  std::to_string(boardItr->second->MoveData[1]) + "|" +
                                  std::to_string(boardItr->second->MoveData[2]) + "|" +
                                  std::to_string(boardItr->second->MoveData[3]);
        jstring jMoveDataStr = pEnv->NewStringUTF(moveDataStr.c_str());
        pEnv->SetObjectField(mResultObj, moveDataStrFieldID, jMoveDataStr);

        return mResultObj;
    }
    else {
        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/MoveResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        return mResultObj;
    }
}


/// <summary>
///  Arrange a piece. Used for editing the board.
/// </summary>
extern "C"
JNIEXPORT jobject JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_arrange (
        JNIEnv* pEnv,
        jobject pThis,
        jint pFromIndex,
        jint pToIndex,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        bool success = MoveRules::Arrange(pFromIndex, pToIndex, *boardItr->second);

        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/MoveResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        jfieldID successFieldID = pEnv->GetFieldID(mResultClass, "success", "Z");
        jfieldID returnMessageFieldID = pEnv->GetFieldID(mResultClass, "returnMessage",
                                                         "Ljava/lang/String;");

        if (success) {
            pEnv->SetBooleanField(mResultObj, successFieldID, JNI_TRUE);

            // Clear EnPassant
            boardItr->second->StateEnpassantIndex = -1;
        } else {
            pEnv->SetBooleanField(mResultObj, successFieldID, JNI_FALSE);
        }
        jstring jRtnMessage = pEnv->NewStringUTF(boardItr->second->ReturnMessage.c_str());
        pEnv->SetObjectField(mResultObj, returnMessageFieldID, jRtnMessage);

        return mResultObj;
    }
    else {
        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/MoveResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        return mResultObj;
    }
}

/// <summary>
///  Arrange a piece. Used for editing the board.
/// </summary>
extern "C"
JNIEXPORT jobject JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_arrangeUpdate (
        JNIEnv* pEnv,
        jobject pThis,
        jchar pFen,
        jint pToIndex,
        jint pId
        )
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        bool success = MoveRules::ArrangeUpdate(pFen, pToIndex, *boardItr->second);

        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/MoveResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        jfieldID successFieldID = pEnv->GetFieldID(mResultClass, "success", "Z");
        jfieldID returnMessageFieldID = pEnv->GetFieldID(mResultClass, "returnMessage",
                                                         "Ljava/lang/String;");

        if (success) {
            pEnv->SetBooleanField(mResultObj, successFieldID, JNI_TRUE);

            // Clear EnPassant
            boardItr->second->StateEnpassantIndex = -1;
        } else {
            pEnv->SetBooleanField(mResultObj, successFieldID, JNI_FALSE);
        }
        jstring jRtnMessage = pEnv->NewStringUTF(boardItr->second->ReturnMessage.c_str());
        pEnv->SetObjectField(mResultObj, returnMessageFieldID, jRtnMessage);

        return mResultObj;
    }
    else {
        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/MoveResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        return mResultObj;
    }
}

/// <summary>
/// Find valid from index from a given to index
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_findFromIndex (
        JNIEnv* pEnv,
        jobject pThis,
        jint pToIndex,
        jint pSpin,
        jintArray pValidFromIndexes,
        jint pId)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        std::vector<int> validFromIndexesVector;

        if (pValidFromIndexes != NULL) {
            jsize arraySize = pEnv->GetArrayLength(pValidFromIndexes);

            if (arraySize > 0) {
                jint jvalidFromIndexesArray[arraySize];
                pEnv->GetIntArrayRegion(pValidFromIndexes, 0, arraySize, jvalidFromIndexesArray);
                for (const int32_t validFromIndex : jvalidFromIndexesArray) {
                    validFromIndexesVector.push_back(validFromIndex);
                }
            }
        }

        int fromIndex = MoveRules::FindFromIndex(*boardItr->second, pToIndex, pSpin,
                                                 validFromIndexesVector);

        return fromIndex;
    }
    else {
        return 0;
    }
}

/// <summary>
///  Checks if the move is a pawn promoting move
/// </summary>
extern "C"
JNIEXPORT jboolean JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_isPawnPromotion (
        JNIEnv* pEnv,
        jobject pThis,
        jint pFromIndex,
        jint pToIndex,
        jint pId
)
{
    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        bool success = MoveRules::IsPawnPromotion(pFromIndex, pToIndex, *boardItr->second);
        if (success) return  JNI_TRUE;
        else return JNI_FALSE;
    }
    else {
        return JNI_FALSE;
    }
}


/// <summary>
///  Gets spin value from piece name
/// </summary>
extern "C"
JNIEXPORT jint JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getSpinFromPieceName (
        JNIEnv* pEnv,
        jobject pThis,
        jstring pPieceName)
{
    const char *piecenameChr = pEnv->GetStringUTFChars(pPieceName,0);
    std::string piecenameStr = piecenameChr;

    return helper::GetSpinFromPieceName(piecenameStr);
}

/// <summary>
///  Get piece name from a FEN char
/// </summary>
extern "C"
JNIEXPORT jstring JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getPieceNameFromChar (
        JNIEnv* pEnv,
        jobject pThis,
        jchar pFENChar)
{

    std::string piecename = helper::GetPieceNameFromChar((char)pFENChar);
    return pEnv->NewStringUTF(piecename.c_str());
}

/// <summary>
///  Get FEN char from spin
/// </summary>
extern "C"
JNIEXPORT jchar JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_getFENCharFromSpin (
        JNIEnv* pEnv,
        jobject pThis,
        jint pSpin)
{
    return (char16_t)helper::GetFENCharFromSpin(pSpin);

}

/// <summary>
///  Searches for the best move
/// </summary>
extern "C"
JNIEXPORT jobject JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_searchStart (
        JNIEnv* pEnv,
        jobject pThis,
        jobject pSearchOptions,
        jint pId) {
    auto boardItr = MainBoardMap.find(pId);
    auto searchItr = SearchBoardMap.find(pId);
    if (boardItr != MainBoardMap.end() && searchItr != SearchBoardMap.end()) {

        uint64_t boardArray[276];
        int32_t stateArray[8];


        boardItr->second->GetBoardArray(boardArray);
        boardItr->second->GetStateArray(stateArray);
        searchItr->second->SetBoardArray(boardArray);
        searchItr->second->SetStateArray(stateArray);


        Search::SearchTreeNode bestMove;
        Search::SearchStatistics statistics;
        Search::SearchOptions options;

        jclass mSearchOptions = pEnv->GetObjectClass(pSearchOptions);

        jfieldID randomiseFirstMoveFieldID = pEnv->GetFieldID(mSearchOptions, "randomiseFirstMove","Z");
        options.randomiseFirstMove = pEnv->GetBooleanField(pSearchOptions, randomiseFirstMoveFieldID);

        jfieldID limitSkillLevelFieldID = pEnv->GetFieldID(mSearchOptions, "limitSkillLevel","I");
        options.limitSkillLevel = pEnv->GetIntField(pSearchOptions, limitSkillLevelFieldID);

        jfieldID limitDepthFieldID = pEnv->GetFieldID(mSearchOptions, "limitDepth","I");
        options.limitDepth = pEnv->GetIntField(pSearchOptions, limitDepthFieldID);

        jfieldID limitNodesFieldID = pEnv->GetFieldID(mSearchOptions, "limitNodes","I");
        options.limitNodes = pEnv->GetIntField(pSearchOptions, limitNodesFieldID);

        jfieldID limitMoveDurationFieldID = pEnv->GetFieldID(mSearchOptions, "limitMoveDuration","I");
        options.limitMoveDuration = pEnv->GetIntField(pSearchOptions, limitMoveDurationFieldID);

        jfieldID limitThreadsFieldID = pEnv->GetFieldID(mSearchOptions, "limitThreads","I");
        options.limitThreads = pEnv->GetIntField(pSearchOptions, limitThreadsFieldID);

        jfieldID alternateMoveFieldID = pEnv->GetFieldID(mSearchOptions, "alternateMove","Z");
        options.alternateMove = pEnv->GetBooleanField(pSearchOptions, alternateMoveFieldID);

        // Search for a move
        Search::GetBestMove(*searchItr->second, options, bestMove, statistics);

        // Copy the values to result
        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/SearchResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        jfieldID moveFromIndexFieldID = pEnv->GetFieldID(mResultClass, "moveFromIndex", "I");
        jfieldID moveToIndexFieldID = pEnv->GetFieldID(mResultClass, "moveToIndex", "I");
        jfieldID promotionPieceTypeFieldID = pEnv->GetFieldID(mResultClass, "promotionPieceType", "I");
        jfieldID cancelledFieldID = pEnv->GetFieldID(mResultClass, "cancelled", "Z");
        jfieldID errorFieldID = pEnv->GetFieldID(mResultClass, "error", "I");
        jfieldID errorMessageFieldID = pEnv->GetFieldID(mResultClass, "errorMessage", "Ljava/lang/String;");

        pEnv->SetIntField(mResultObj, moveFromIndexFieldID, bestMove.moveFromIndex);
        pEnv->SetIntField(mResultObj, moveToIndexFieldID, bestMove.moveToIndex);
        pEnv->SetIntField(mResultObj, promotionPieceTypeFieldID, bestMove.promotionPieceType);



        if (bestMove.cancelled) {
            pEnv->SetBooleanField(mResultObj, cancelledFieldID, JNI_TRUE);
        } else {
            pEnv->SetBooleanField(mResultObj, cancelledFieldID, JNI_FALSE);
        }

        pEnv->SetIntField(mResultObj, errorFieldID, bestMove.error);
        pEnv->SetObjectField(mResultObj, errorMessageFieldID, pEnv->NewStringUTF(helper::SearchErrorMessage.at(bestMove.error).c_str()));

        return mResultObj;
    } else {
        jclass mResultClass = pEnv->FindClass("purpletreesoftware/karuahchess/engine/SearchResult");
        jobject mResultObj = pEnv->AllocObject(mResultClass);
        return mResultObj;
    }

}



/// <summary>
///  Sets the castling availability if it is valid
/// </summary>
extern "C"
JNIEXPORT jboolean JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_setStateCastlingAvailability (
        JNIEnv* pEnv,
        jobject pThis,
        jint pCastlingAvailability,
        jint pColour,
        jint pId
)
{

    auto boardItr = MainBoardMap.find(pId);
    if(boardItr != MainBoardMap.end()) {
        bool success = false;
        if (pColour == helper::WHITEPIECE) {
            success = boardItr->second->setStateCastlingAvailability<helper::WHITEPIECE>(pCastlingAvailability);
        }
        else {
            success = boardItr->second->setStateCastlingAvailability<helper::BLACKPIECE>(pCastlingAvailability);
        }

        if (success) return  JNI_TRUE;
        else return JNI_FALSE;

    }
    else {
        return JNI_FALSE;
    }

}

/// <summary>
///  Cleans up objects created. Called by finalize from kotlin side
/// </summary>
extern "C"
JNIEXPORT void JNICALL
Java_purpletreesoftware_karuahchess_engine_KaruahChessEngineC1_cleanup (
        JNIEnv* pEnv,
        jobject pThis,
        jint pId) {

    auto boardItr = MainBoardMap.find(pId);
    if (boardItr != MainBoardMap.end()) {
        delete boardItr->second;
        MainBoardMap.erase(boardItr);
    }

    auto searchItr = SearchBoardMap.find(pId);
    if (searchItr != SearchBoardMap.end()) {
        delete searchItr->second;
        SearchBoardMap.erase(searchItr);
    }

}
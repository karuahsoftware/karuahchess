﻿/*
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

using System;
using KaruahChess.Pieces;
using Windows.UI.Xaml.Media.Imaging;
using KaruahChess.Model;
using KaruahChessEngine;
using KaruahChess.Common;
using System.Collections.Generic;

namespace KaruahChess.Rules
{
    public class BoardAnimation
    {

        KaruahChessEngineClass _tempBoardBefore;
        KaruahChessEngineClass _tempBoardAfter;

        /// <summary>
        /// Constructor
        /// </summary>
        public BoardAnimation()
        {
            _tempBoardBefore = new KaruahChessEngineClass();
            _tempBoardAfter = new KaruahChessEngineClass();
        }

        /// <summary>
        /// Creates a move animation sequence
        /// </summary>
        /// <param name="pBoardA"></param>
        /// <param name="pBoardB"></param>
        /// <param name="pBoardSquareDS"></param>
        /// <returns></returns>
        public List<PieceAnimationInstruction> CreateAnimationList(GameRecordArray pBoardRecA, GameRecordArray pBoardRecB, BoardSquareDataService pBoardSquareDS)
        {
            var animationList = new List<PieceAnimationInstruction>(4);
            var moveList = GetAnimationMoveList(pBoardRecA, pBoardRecB);                        
            int animationNumber = 0;

            
            foreach (var move in moveList) {
                int spin = move[0];
                int fromIndex = move[1];
                int toIndex = move[2];

                if (fromIndex > -1 && toIndex > -1) {
                    // Piece move animation                                          
                    var pieceImgData = Piece.GetImage(BoardSquareDataService.GetPieceTypeFromSpin(spin), BoardSquareDataService.GetPieceColourFromSpin(spin), pBoardSquareDS.SquareSize, pBoardSquareDS.SquareSize);
                    var instruction = GetAnimationInstruction(fromIndex, toIndex, pieceImgData, pBoardSquareDS);
                    instruction.AnimationType = PieceAnimationInstruction.AnimationTypeEnum.Move;
                    animationList.Add(instruction);                    
                }
                else if (fromIndex > -1 && toIndex == -1)
                {
                    // Piece take animation
                    var pieceImgData = Piece.GetImage(BoardSquareDataService.GetPieceTypeFromSpin(spin), BoardSquareDataService.GetPieceColourFromSpin(spin), pBoardSquareDS.SquareSize, pBoardSquareDS.SquareSize);
                    var instruction = GetAnimationInstruction(fromIndex, fromIndex, pieceImgData, pBoardSquareDS);
                    instruction.AnimationType = PieceAnimationInstruction.AnimationTypeEnum.Take;
                    animationList.Add(instruction);
                }
                else if (fromIndex == -1 && toIndex > -1)
                {
                    // Piece return animation                    
                    var pieceImgData = Piece.GetImage(BoardSquareDataService.GetPieceTypeFromSpin(spin), BoardSquareDataService.GetPieceColourFromSpin(spin), pBoardSquareDS.SquareSize, pBoardSquareDS.SquareSize);
                    var instruction = GetAnimationInstruction(toIndex, toIndex, pieceImgData, pBoardSquareDS);
                    instruction.AnimationType = PieceAnimationInstruction.AnimationTypeEnum.Put;
                    animationList.Add(instruction);
                }
                animationNumber++;
            }
            
            return animationList;
        }
               

        /// <summary>
        /// Detects changes between two bitboards and returns list of moves. Only works up to two different piece types. If
        /// more than two pieces changed, then the list returned is empty.
        /// </summary>
        /// <param name="pBoardFrom"></param>
        /// <param name="pBoardTo"></param>
        /// <returns></returns>
        private List<int[]> GetAnimationMoveList(GameRecordArray pBoardRecBefore, GameRecordArray pBoardRecAfter)
        {
            _tempBoardBefore.SetBoardArray(pBoardRecBefore.BoardArray);
            _tempBoardBefore.SetStateArray(pBoardRecBefore.StateArray);
            _tempBoardAfter.SetBoardArray(pBoardRecAfter.BoardArray);
            _tempBoardAfter.SetStateArray(pBoardRecAfter.StateArray);

            UInt64 allBeforeWhitePos = _tempBoardBefore.GetOccupiedByWhite();
            UInt64 allBeforeBlackPos = _tempBoardBefore.GetOccupiedByBlack();
            UInt64 allAfterWhitePos = _tempBoardAfter.GetOccupiedByWhite();
            UInt64 allAfterBlackPos = _tempBoardAfter.GetOccupiedByBlack();

            UInt64 allChangeWhitePos = allBeforeWhitePos ^ allAfterWhitePos;
            UInt64 allChangeBlackPos = allBeforeBlackPos ^ allAfterBlackPos;

           
            int[,] spinChange = new int[13, 2];
            CreateChangeArray(allChangeWhitePos, spinChange);           
            CreateChangeArray(allChangeBlackPos, spinChange);
            List<int[]> spinChangeList = ConvertSpinChangeArrayToList(spinChange);
                        
            // Too many changes to animate so just clear the list
            if (spinChangeList.Count > 2)
            {
                spinChangeList.Clear();
            }

            return spinChangeList;
        }

        /// <summary>
        /// Converts a spin change array to a list of changes
        /// </summary>
        /// <param name="pSpinChange"></param>
        /// <returns></returns>
        private List<int[]> ConvertSpinChangeArrayToList(int[,] pSpinChange)
        {
            const int spinOffset = 6;
            const int sqIndexOffset = 1;

            // Build the list of changes
            List<int[]> pieceChangeList = new List<int[]>(2);
            for (int i = 0; i < 13; i++)
            {
                if (pSpinChange[i, 0] > 0 || pSpinChange[i, 1] > 0)
                {
                    int[] change = new int[] { i - spinOffset, -1, -1 };
                    if (pSpinChange[i, 0] > 0) change[1] = pSpinChange[i, 0] - sqIndexOffset;
                    if (pSpinChange[i, 1] > 0) change[2] = pSpinChange[i, 1] - sqIndexOffset;
                    pieceChangeList.Add(change);
                }
            }

            return pieceChangeList;
        }

        /// <summary>
        /// Creates an array of spin changes
        /// </summary>
        /// <param name="pChangedPositions"></param>
        /// <returns></returns>        
        private void CreateChangeArray(UInt64 pChangedPositions, int[,] pSpinChange)
        {
            // [Spin offset, {From Index, To Index}]
            
            const int spinOffset = 6;
            const int sqIndexOffset = 1;

            // Changes            
            while (pChangedPositions > 0)
            {  

                int pos = helper.BitScanForward(pChangedPositions);
                UInt64 sqMask = 1uL << pos;
                pChangedPositions ^= sqMask;
                int sqIndex = 63 - pos;

                int beforeSpin = _tempBoardBefore.GetSpin(sqIndex);
                int afterSpin = _tempBoardAfter.GetSpin(sqIndex);

                if (beforeSpin != 0) pSpinChange[beforeSpin + spinOffset, 0] = sqIndex + sqIndexOffset;
                if (afterSpin != 0) pSpinChange[afterSpin + spinOffset, 1] = sqIndex + sqIndexOffset;
                                
            }
                        
        }


        /// <summary>
        /// Creates an animation instruction
        /// </summary>
        /// <param name="pFromId"></param>
        /// <param name="pToId"></param>
        /// <param name="pImage"></param>
        /// <param name="pImageRotationZ"></param>
        /// <returns></returns>
        private PieceAnimationInstruction GetAnimationInstruction(int pFromIndex, int pToIndex, BitmapImage pImage, BoardSquareDataService pBoardSquareDS)
        {
            var fromTile = pBoardSquareDS.BoardTiles[pFromIndex];
            var toTile = pBoardSquareDS.BoardTiles[pToIndex];
            var fromTilePoint = fromTile.Coordinates();
            var toTilePoint = toTile.Coordinates();

            var animIntruct = new PieceAnimationInstruction();
            animIntruct.ImageData = pImage;
            animIntruct.MoveFrom = fromTilePoint;
            animIntruct.MoveTo = toTilePoint;
            animIntruct.HiddenSquareIndexes.Add(pFromIndex);
            animIntruct.HiddenSquareIndexes.Add(pToIndex);

            return animIntruct;
        }
    }
}

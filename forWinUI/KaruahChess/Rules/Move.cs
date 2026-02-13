/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2026 Karuah Software

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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;
using KaruahChess.Model;
using Microsoft.UI;
using System.Collections.Generic;
using KaruahChessEngine;
using KaruahChess.Common;

namespace KaruahChess.Rules
{
    public class Move
    {

        // variables
        ResourceDictionary _rd;
        

        public enum HighlightEnum { None, MovePath, Select};
        /// <summary>
        /// Square to move from
        /// </summary>
        public int FromIndex { get; private set; }

        /// <summary>
        ///  Square to move to
        /// </summary>
        public int ToIndex { get; private set; }


        


        /// <summary>
        /// Constructor
        /// </summary>
        public Move(BoardSquareDataService pDsBoardSquare)
        {

            FromIndex = -1;
            ToIndex = -1;
            _rd = Common.helper.GetStyles();
            
        }


        /// <summary>
        /// Add a square to the move
        /// </summary>
        /// <param name="pBoardSquareIndex"></param>
        public bool Add(int pBoardSquareIndex, KaruahChessEngineClass pBoard, HighlightEnum pHighlight)
        {
            bool complete = false;

            if (FromIndex == pBoardSquareIndex)
            {
                Clear();             
            }
            else if (FromIndex ==-1 && ToIndex == -1)
            {
                FromIndex = pBoardSquareIndex;

                // Highlight squares
                if (pHighlight == HighlightEnum.MovePath)
                {                      
                    UInt64 sqMark = pBoard.GetPotentialMove(pBoardSquareIndex);                    
                    if (!((sqMark & (Constants.BITMASK >> FromIndex)) > 0)) sqMark = sqMark | (Constants.BITMASK >> FromIndex);

                    HashSet<int> sqMarkSet = new HashSet<int>();
                    for(int i=0; i < 64; i++) {
                        if (((Constants.BITMASK >> i) & sqMark) > 0) sqMarkSet.Add(i);
                    }

                    SolidColorBrush colour = new SolidColorBrush(Colors.DarkGreen);
                    BoardSquare.EllipseShow(sqMarkSet, colour, true);
                }
                else if(pHighlight == HighlightEnum.Select)
                {
                    var sqMark = new HashSet<int>() { FromIndex };
                    SolidColorBrush colour = new SolidColorBrush(Colors.DarkGreen);
                    BoardSquare.EllipseShow(sqMark, colour, true);
                }
            }
            else if (FromIndex > -1 && ToIndex == -1)
            {
                ToIndex = pBoardSquareIndex;
                complete = true;
                BoardSquare.EllipseClearAll();
            } 
            else
            {
                Clear();
              
            }

            return complete;

        }

        /// <summary>
        /// Clear the move
        /// </summary>
        public void Clear()
        {
            FromIndex = -1;
            ToIndex = -1;
            BoardSquare.EllipseClearAll();
        }
            

    }
}

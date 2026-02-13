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
using KaruahChess.Pieces;
using System.Collections.Generic;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;
namespace KaruahChess.Model
{
       
    public class BoardSquare
    {       
        
        private static HashSet<WeakReference<BoardSquare>> _objectInstances = new HashSet<WeakReference<BoardSquare>>();
        
        
        public int Index { get; set; }
                       
        public Piece.TypeEnum PieceType { get; set; }
                
        public Piece.ColourEnum PieceColour { get; set; }
                
        // Enum            
        public enum ColourEnum { Black, White }

        
        public ColourEnum Colour
        {
            get
            {
                if ((Index + 1) % 2 == 0)
                {
                    if ((Index >=0 && Index <= 7) || (Index >= 16 && Index <= 23) || (Index >= 32 && Index <= 39) || (Index >= 48 && Index <= 55))
                    {
                        return ColourEnum.Black;
                    }
                    else
                    {
                        return ColourEnum.White;
                    }
                }
                else
                {
                    if ((Index >= 0 && Index <= 7) || (Index >= 16 && Index <= 23) || (Index >= 32 && Index <= 39) || (Index >= 48 && Index <= 55))
                    {
                        return ColourEnum.White;
                    }
                    else
                    {
                        return ColourEnum.Black;
                    }
                }
            }
        }

                
        public Piece Piece { get; private set; }

        /// <summary>
        /// Constructor
        /// </summary>       
            
        public BoardSquare()
        {            
            _objectInstances.Add(new WeakReference<BoardSquare>(this));
        }

        /// <summary>
        /// Returns the FEN representation of this Square
        /// </summary>
        /// <returns></returns>
        public Char GetFEN()
        {
            Char FEN;

            switch (PieceType)
            {
                case Piece.TypeEnum.Pawn:
                    FEN = 'p';
                    break;
                case Piece.TypeEnum.Rook:
                    FEN = 'r';
                    break;
                case Piece.TypeEnum.Knight:
                    FEN = 'n';
                    break;
                case Piece.TypeEnum.Bishop:
                    FEN = 'b';
                    break;
                case Piece.TypeEnum.Queen:
                    FEN = 'q';
                    break;
                case Piece.TypeEnum.King:
                    FEN = 'k';
                    break;
                default:
                    FEN = '0';  // Represents a blank square
                    break;

            }

            if (PieceColour == Piece.ColourEnum.White)
            {
                FEN = Char.ToUpper(FEN);
            }

            return FEN;
        }

        /// <summary>
        /// Sets boardquare based on FEN character
        /// </summary>
        /// <param name="pFENChar"></param>
        public void SetFEN(Char pFENChar, double pSquareSize, bool pLargePawn)
        {
            var FENLowerChar = Char.ToLower(pFENChar);

            // Set type
            switch (FENLowerChar)
            {
                case 'p':
                    PieceType = Piece.TypeEnum.Pawn;
                    break;
                case 'r':
                    PieceType = Piece.TypeEnum.Rook;
                    break;
                case 'n':
                    PieceType = Piece.TypeEnum.Knight;
                    break;
                case 'b':
                    PieceType = Piece.TypeEnum.Bishop;
                    break;
                case 'q':
                    PieceType = Piece.TypeEnum.Queen;
                    break;
                case 'k':
                    PieceType = Piece.TypeEnum.King;
                    break;
                default:
                    PieceType = Piece.TypeEnum.Empty;
                    break;
            }

            // Set Colour
            if (PieceType == Piece.TypeEnum.Empty) PieceColour = Piece.ColourEnum.None;
            else if (FENLowerChar == pFENChar) PieceColour = Piece.ColourEnum.Black;
            else PieceColour = Piece.ColourEnum.White;

            // Set piece and square size
            SetPiece(pSquareSize, pLargePawn);

        }

        /// <summary>
        /// Sets the piece on the square
        /// </summary>       
        private void SetPiece(double pSquareSize, bool pLargePawn)
        {
            if (Piece == null)
            {
                Piece = new Piece();
                Piece.StyleTemplate = new PieceStyleTemplate();
            }
            Piece.SetType(PieceType, PieceColour, pSquareSize, pSquareSize, pLargePawn);
        }

        /// <summary>
        /// Function to get the default value of a board square
        /// </summary>
        /// <param name="pIndex">The index of the square 1 to 64</param>             
        public static PieceAttribute GetDefaultAttribute(int pIndex)
        {
            PieceAttribute returnValue;
            returnValue = new PieceAttribute();

            switch (pIndex)
            {
                case 1:
                    returnValue.Type = Piece.TypeEnum.Rook;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 2:
                    returnValue.Type = Piece.TypeEnum.Knight;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 3:
                    returnValue.Type = Piece.TypeEnum.Bishop;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 4:
                    returnValue.Type = Piece.TypeEnum.Queen;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 5:
                    returnValue.Type = Piece.TypeEnum.King;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 6:
                    returnValue.Type = Piece.TypeEnum.Bishop;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 7:
                    returnValue.Type = Piece.TypeEnum.Knight;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 8:
                    returnValue.Type = Piece.TypeEnum.Rook;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 9:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 10:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 11:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 12:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 13:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 14:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 15:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 16:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.Black;
                    returnValue.FirstMove = true;
                    break;
                case 49:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 50:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 51:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 52:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 53:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 54:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 55:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 56:
                    returnValue.Type = Piece.TypeEnum.Pawn;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 57:
                    returnValue.Type = Piece.TypeEnum.Rook;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 58:
                    returnValue.Type = Piece.TypeEnum.Knight;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 59:
                    returnValue.Type = Piece.TypeEnum.Bishop;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 60:
                    returnValue.Type = Piece.TypeEnum.Queen;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 61:
                    returnValue.Type = Piece.TypeEnum.King;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 62:
                    returnValue.Type = Piece.TypeEnum.Bishop;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 63:
                    returnValue.Type = Piece.TypeEnum.Knight;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                case 64:
                    returnValue.Type = Piece.TypeEnum.Rook;
                    returnValue.Colour = Piece.ColourEnum.White;
                    returnValue.FirstMove = true;
                    break;
                default:
                    returnValue.Type = Piece.TypeEnum.Empty;
                    returnValue.Colour = Piece.ColourEnum.None;
                    returnValue.FirstMove = false;
                    break;
            }

            return returnValue;
        }


        /// <summary>
        /// Function to show ellipses
        /// </summary>
        /// <param name="pBoardSquareSet"></param>            
        public static void EllipseShow(HashSet<int> pBoardSquareSet, SolidColorBrush pBrushColor, bool pApplyFill)
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (pBoardSquareSet.Contains(obj.Index))
                    {
                        if (obj.Piece != null) { 
                            obj.Piece.SetEllipseVisibility(Visibility.Visible, pBrushColor, pApplyFill);
                        }
                    }
                    else
                    {
                        if (obj.Piece != null)
                        {
                            obj.Piece.SetEllipseVisibility(Visibility.Collapsed, null, false);
                        }
                    }
                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }

        

        /// <summary>
        /// Function to clear all ellipses
        /// </summary>          
        public static void EllipseClearAll()
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (obj.Piece != null)
                    {
                        obj.Piece.SetEllipseVisibility(Visibility.Collapsed, null, false);
                    }

                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }



        /// <summary>
        /// Function to show rectangles
        /// </summary>
        /// <param name="pBoardSquareSet"></param>        
        public static void RectangleShow(HashSet<int> pBoardSquareSet, SolidColorBrush pBrushColor)
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (pBoardSquareSet.Contains(obj.Index))
                    {
                        if (obj.Piece != null)
                        {
                            obj.Piece.SetRectangleVisibility(Visibility.Visible, pBrushColor);
                        }
                    }
                    else
                    {
                        if (obj.Piece != null)
                        {
                            obj.Piece.SetRectangleVisibility(Visibility.Collapsed, null);
                        }
                    }
                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }



        /// <summary>
        /// Function to clear all rectangles
        /// </summary>              
        public static void RectangleClearAll()
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (obj.Piece != null)
                    {
                        obj.Piece.SetRectangleVisibility(Visibility.Collapsed, null);
                    }

                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }


        /// <summary>
        /// Function to show piece edit select
        /// </summary>
        /// <param name="pBoardSquareSet"></param>        
        public static void PieceEditSelectShow(HashSet<int> pBoardSquareSet, SolidColorBrush pBrushColor)
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (pBoardSquareSet.Contains(obj.Index))
                    {
                        if (obj.Piece != null)
                        {
                           obj.Piece.SetPieceEditSelectVisibility(Visibility.Visible, pBrushColor);
                        }
                    }
                    else
                    {
                        if (obj.Piece != null)
                        {
                           obj.Piece.SetPieceEditSelectVisibility(Visibility.Collapsed, null);
                        }
                    }
                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }



        /// <summary>
        /// Function to clear all piece edit selects
        /// </summary>              
        public static void PieceEditSelectClearAll()
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (obj.Piece != null)
                    {
                        obj.Piece.SetPieceEditSelectVisibility(Visibility.Collapsed, null);
                    }

                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }


        /// <summary>
        /// Function to show Ellipse UnderAttacks
        /// </summary>
        /// <param name="pBoardSquareSet"></param>
        public static void EllipseUnderAttackShow(HashSet<int> pBoardSquareSet, SolidColorBrush pBrushColor)
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (pBoardSquareSet.Contains(obj.Index))
                    {
                        if (obj.Piece != null)
                        {
                            obj.Piece.SetEllipseUnderAttackVisibility(Visibility.Visible, pBrushColor);
                        }
                    }
                    else
                    {
                        if (obj.Piece != null)
                        {
                            obj.Piece.SetEllipseUnderAttackVisibility(Visibility.Collapsed, null);
                        }
                    }
                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }

        /// <summary>
        /// Function to clear all Ellipse UnderAttacks
        /// </summary>        
        public static void EllipseUnderAttackClearAll()
        {
            BoardSquare obj;
            var _objectInstancesValid = new HashSet<WeakReference<BoardSquare>>();

            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null)
                {
                    if (obj.Piece != null)
                    {
                        obj.Piece.SetEllipseUnderAttackVisibility(Visibility.Collapsed, null);
                    }

                    _objectInstancesValid.Add(weakRef);
                }
            }

            _objectInstances = _objectInstancesValid;
        }


        /// <summary>
        /// Shake all pieces animation
        /// </summary>
        /// <param name="pEnabled"></param>
        public static void Shake(bool pEnabled)
        {
            BoardSquare obj;
            
            foreach (var weakRef in _objectInstances)
            {
                if (weakRef.TryGetTarget(out obj) && obj != null && obj.Piece != null)  {                 
                    obj.Piece.Shake(pEnabled);
                }
            }

           
        }

    }
}

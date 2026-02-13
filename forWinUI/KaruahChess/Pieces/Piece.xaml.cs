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
using Microsoft.UI;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Media.Animation;
using Microsoft.UI.Xaml.Media.Imaging;



namespace KaruahChess.Pieces
{
    public sealed partial class Piece : UserControl
    {
        //Properties
        private double _pieceWidth;
        private double _pieceHeight;
        private bool _largePawn;
        public enum TypeEnum { Empty = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5, King = 6}
        
        public TypeEnum Type { get; private set; }

        public enum ColourEnum {None = 0, Black = -1, White = 1 }

        public ColourEnum Colour { get; private set; }

        public PieceStyleTemplate StyleTemplate { get; set; }

        public BitmapImage ImageData { get; private set; }


        /// <summary>
        /// Constructor
        /// </summary>
        public Piece()
        {
            this.InitializeComponent();

            // Set default value
            SetType(TypeEnum.Empty, ColourEnum.None, 0, 0, false);
            SetEllipseVisibility(Visibility.Collapsed, null, false);

            
        }

        /// <summary>
        /// Sets the ellipse visibility
        /// </summary>
        /// <param name="pVisibility"></param>
        public void SetEllipseVisibility(Visibility pVisibility, SolidColorBrush pBrushColor, bool pApplyFill)
        {
            PieceEllipse.Visibility = pVisibility;

            if (pBrushColor != null) {
                PieceEllipse.StrokeThickness = 2;
                PieceEllipse.Stroke = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 255, 255, 255));
                if (pApplyFill) PieceEllipse.Fill = pBrushColor;
                else { PieceEllipse.Fill = null; }
                
            }

            StartPieceEllipseStoryBoard();
            
        }


        /// <summary>
        /// Sets the rectangle visibility
        /// </summary>
        /// <param name="pVisibility"></param>
        public void SetRectangleVisibility(Visibility pVisibility, SolidColorBrush pBrushColor)
        {
            PieceRectangle.Visibility = pVisibility;

            if (pBrushColor != null)
            {               
                PieceRectangle.Stroke = pBrushColor;
                PieceRectangle.Fill = pBrushColor;
            }

            StartPieceRectangleStoryBoard();

        }

        /// <summary>
        /// Sets the piece edit select visibility
        /// </summary>
        /// <param name="pVisibility"></param>
        public void SetPieceEditSelectVisibility(Visibility pVisibility, SolidColorBrush pBrushColor)
        {
            PieceEditSelect.Visibility = pVisibility;

            if (pBrushColor != null)
            {
                PieceEditSelect.Stroke = pBrushColor;
                PieceEditSelect.Fill = pBrushColor;
            }

        }

        /// <summary>
        /// Sets the ellipse UnderAttack visibility
        /// </summary>
        /// <param name="pVisibility"></param>
        public void SetEllipseUnderAttackVisibility(Visibility pVisibility, SolidColorBrush pBrushColor)
        {
            PieceEllipseUnderAttack.Visibility = pVisibility;

            if (pBrushColor != null)
            {
                PieceEllipseUnderAttack.StrokeThickness = 2;
                PieceEllipseUnderAttack.Stroke = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 255, 255, 255));
                PieceEllipseUnderAttack.Fill = pBrushColor;
            }

            StartPieceEllipseUnderAttackStoryBoard();

        }
                
        /// <summary>
        /// Sets the image for the piece type
        /// </summary>
        /// <param name="pType">Piece type</param>
        /// <param name="pColour">Piece colour</param>
        public void SetType(TypeEnum pType, ColourEnum pColour)
        {
            SetType(pType, pColour, _pieceWidth, _pieceHeight, _largePawn);
        }


        /// <summary>
        ///  Sets the image for the piece type
        /// </summary>
        /// <param name="pType">Enum type</param>
        /// <param name="pColour">Piece colour</param>
        /// <param name="pWidth">Width of image</param>
        /// <param name="pHeight">Height of image</param>        
        public void SetType(TypeEnum pType, ColourEnum pColour, double pWidth, double pHeight, bool pLargePawn)
        {
            _pieceWidth = pWidth;
            _pieceHeight = pHeight;
            _largePawn = pLargePawn;

            Type = pType;
            Colour = pColour;
            PieceCanvas.Width = pWidth;
            PieceCanvas.Height = pHeight;
           

            // Set Ellipse dimensions
            if (pType != TypeEnum.Empty) { 
                PieceEllipse.Width = _pieceWidth * 0.9;
                PieceEllipse.Height = _pieceHeight * 0.9;
            }
            else
            {
                PieceEllipse.Width = _pieceWidth * 0.3;
                PieceEllipse.Height = _pieceHeight * 0.3;
            }

            Canvas.SetLeft(PieceEllipse, pWidth / 2 - PieceEllipse.Width / 2);
            Canvas.SetTop(PieceEllipse, pHeight / 2 - PieceEllipse.Height / 2);

            // Set Rectangle dimensions
            PieceRectangle.Width = _pieceWidth > 0 ? _pieceWidth - 1 : _pieceWidth;
            PieceRectangle.Height = _pieceHeight > 0 ? _pieceHeight - 1 : _pieceHeight;
            Canvas.SetLeft(PieceRectangle, pWidth / 2 - PieceRectangle.Width / 2);
            Canvas.SetTop(PieceRectangle, pHeight / 2 - PieceRectangle.Height / 2);


            // Set Ellipse UnderAttack dimensions
            PieceEllipseUnderAttack.Width = _pieceWidth * 0.9;
            PieceEllipseUnderAttack.Height = _pieceHeight * 0.9;
            Canvas.SetLeft(PieceEllipseUnderAttack, pWidth / 2 - PieceEllipseUnderAttack.Width / 2);
            Canvas.SetTop(PieceEllipseUnderAttack, pHeight / 2 - PieceEllipseUnderAttack.Height / 2);

            // Set Rectangle dimensions
            PieceEditSelect.Width = _pieceWidth > 0 ? _pieceWidth - 1 : _pieceWidth;
            PieceEditSelect.Height = _pieceHeight > 0 ? _pieceHeight - 1 : _pieceHeight;
            Canvas.SetLeft(PieceEditSelect, pWidth / 2 - PieceEditSelect.Width / 2);
            Canvas.SetTop(PieceEditSelect, pHeight / 2 - PieceEditSelect.Height / 2);

            if (pType == TypeEnum.Empty)
            {
                PieceImage.Visibility = Visibility.Collapsed;
                PieceImage.Source = null;
                ImageData = null;
            }
            else
            {
                var img = GetImage(pType, pColour, pWidth, pHeight, pLargePawn);
                PieceImage.Source = img;
                PieceImage.Width = pHeight;
                PieceImage.Height = pHeight;
                PieceImage.Visibility = Visibility.Visible;
                ImageData = img;
            }

            
            // Run soft animation
            if (PieceImage.Visibility == Visibility.Visible)
            {
                var storyboard = PieceImage.Resources["PieceSoftAppearStoryboard"] as Storyboard;
                storyboard.Begin();
            }

        }

        /// <summary>
        /// Refresh the piece image
        /// </summary>
        public void RefreshPiece(bool pLargePawn)
        {
            _largePawn = pLargePawn;
            var img = GetImage(Type, Colour, _pieceWidth, _pieceHeight, pLargePawn);
            PieceImage.Source = img;
            PieceImage.Width = _pieceWidth;
            PieceImage.Height = _pieceHeight;
            PieceImage.Visibility = Visibility.Visible;
            ImageData = img;
        }

        /// <summary>
        /// Start the ellipse storyboard if not empty
        /// </summary>
        public void StartPieceEllipseStoryBoard()
        {
            if (PieceEllipse.Visibility == Visibility.Visible)
            {             
                var storyboard = PieceEllipse.Resources["PieceEllipseStoryboard"] as Storyboard;
                storyboard.AutoReverse = false;
                storyboard.Begin();
            }
        }


        /// <summary>
        /// Start the rectangle storyboard
        /// </summary>
        public void StartPieceRectangleStoryBoard()
        {
            if (PieceRectangle.Visibility == Visibility.Visible)
            {
                var storyboard = PieceRectangle.Resources["PieceRectangleStoryboard"] as Storyboard;
                storyboard.AutoReverse = false;
                storyboard.Begin();
            }
        }

        
        /// <summary>
        /// Start the ellipse storyboard if not empty
        /// </summary>
        public void StartPieceEllipseUnderAttackStoryBoard()
        {
            if (PieceEllipseUnderAttack.Visibility == Visibility.Visible)
            {
                var storyboard = PieceEllipseUnderAttack.Resources["PieceEllipseUnderAttackStoryboard"] as Storyboard;
                storyboard.AutoReverse = false;
                storyboard.Begin();
            }
        }

        /// <summary>
        /// Set piece shake animation
        /// </summary>
        /// <param name="pEnabled"></param>
        public void Shake(bool pEnabled)
        {
            if (pEnabled)
            {
                var storyboard = PieceImage.Resources["PieceShakeStoryboard"] as Storyboard;                
                storyboard.Begin();
            }
            else {
                var storyboard = PieceImage.Resources["PieceShakeStoryboard"] as Storyboard;               
                storyboard.Stop();
            }
        }

        /// <summary>
        /// Get image data from fen char
        /// </summary>
        /// <param name="pFen"></param>
        /// <param name="pWidth"></param>
        /// <param name="pHeight"></param>
        /// <returns></returns>
        public static BitmapImage GetImage(char pFen, double pWidth, double pHeight, bool pLargePawn)
        {
            var FENLowerChar = Char.ToLower(pFen);
            TypeEnum pieceType;

            // Set type
            switch (FENLowerChar)
            {
                case 'p':
                    pieceType = Piece.TypeEnum.Pawn;
                    break;
                case 'r':
                    pieceType = Piece.TypeEnum.Rook;
                    break;
                case 'n':
                    pieceType = Piece.TypeEnum.Knight;
                    break;
                case 'b':
                    pieceType = Piece.TypeEnum.Bishop;
                    break;
                case 'q':
                    pieceType = Piece.TypeEnum.Queen;
                    break;
                case 'k':
                    pieceType = Piece.TypeEnum.King;
                    break;
                default:
                    pieceType = Piece.TypeEnum.Empty;
                    break;
            }

            // Set Colour
            ColourEnum pieceColour;
            if (pieceType == Piece.TypeEnum.Empty) pieceColour = Piece.ColourEnum.None;
            else if (FENLowerChar == pFen) pieceColour = Piece.ColourEnum.Black;
            else pieceColour = Piece.ColourEnum.White;

            return GetImage(pieceType, pieceColour, pWidth, pHeight, pLargePawn);
        }
        

        /// <summary>
        /// Get image data
        /// </summary>
        /// <param name="pType"></param>
        /// <param name="pColour"></param>
        /// <returns></returns>
        public static BitmapImage GetImage(TypeEnum pType, ColourEnum pColour, double pWidth, double pHeight, bool pLargePawn)
        {
            BitmapImage imgData;

            if (pType == TypeEnum.Empty)
            {                
                imgData = null;
            }
            else if (pType == TypeEnum.King && pColour == ColourEnum.Black)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackKing0.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;
                imgData = img;
            }
            else if (pType == TypeEnum.Queen && pColour == ColourEnum.Black)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackQueen.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Rook && pColour == ColourEnum.Black)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackRook.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Knight && pColour == ColourEnum.Black)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackKnight.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Bishop && pColour == ColourEnum.Black)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackBishop.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Pawn && pColour == ColourEnum.Black)
            {
                var img = pLargePawn ? new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackPawnLarge.png")) : new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackPawn.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.King && pColour == ColourEnum.White)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhiteKing0.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;
                imgData = img;
            }
            else if (pType == TypeEnum.Queen && pColour == ColourEnum.White)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhiteQueen.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Rook && pColour == ColourEnum.White)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhiteRook.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Knight && pColour == ColourEnum.White)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhiteKnight.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else if (pType == TypeEnum.Bishop && pColour == ColourEnum.White)
            {
                var img = new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhiteBishop.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;
                imgData = img;
            }
            else if (pType == TypeEnum.Pawn && pColour == ColourEnum.White)
            {
                var img = pLargePawn ? new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhitePawnLarge.png")) : new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhitePawn.png"));
                img.DecodePixelWidth = (int)pWidth;
                img.DecodePixelHeight = (int)pHeight;                
                imgData = img;
            }
            else
            {                
                imgData = null;
            }


            return imgData;

        }


    }
}

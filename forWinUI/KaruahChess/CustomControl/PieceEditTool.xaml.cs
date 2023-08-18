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


using System;
using System.Collections.Generic;
using Windows.Foundation;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using KaruahChess.Common;
using KaruahChess.Model;
using KaruahChessEngine;
using Microsoft.UI;
using Windows.UI;

namespace KaruahChess.CustomControl
{
    public sealed partial class PieceEditTool : UserControl
    {
                       
        double _buttonSize = 60;
        double _fontSize = 12;
        

        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }

        
        private ViewModel.BoardViewModel _boardVM;

        public int SqIndex { get; private set; } = -1;

        public KaruahChessEngineClass BufferBoard { get; private set; }
                
        public int EditPieceColour { get; set; } = Common.Constants.WHITEPIECE;


        /// <summary>
        /// Constructor
        /// </summary>
        public PieceEditTool()
        {

            

            this.InitializeComponent();

            // Loads a default style template if one has not been set
            if (StyleTemplate == null)
            {
                StyleTemplate = (CustomStyleTemplate)CustomStyleDefaultResourceDictionary["CustomStyleTemplateDefaultObject"];
            }

            BufferBoard = new KaruahChessEngineClass();
            PagePopup.IsOpen = false;

                        
        }

        /// <summary>
        /// Sets the board view model
        /// </summary>
        /// <param name="pBoardVM"></param>
        public void SetBoardVM(ViewModel.BoardViewModel pBoardVM)
        {
            _boardVM = pBoardVM;
        }

         /// <summary>
        /// Indicates if the control is open
        /// </summary>
        public bool IsOpen()
        {
            return PagePopup.IsOpen;
        }

        /// <summary>
        /// Show the popup
        /// </summary>
        /// <returns></returns>
        public void Show(int pSqIndex, Point pTileCoordinates, double pTileSize)        
        {
            if (PagePopup.IsOpen) { 
                PagePopup.IsOpen = false;                
            }

            _buttonSize = calcButtonSize(pTileSize);

            if (pTileSize > 60)
            {
                _fontSize = 28;
            }
            else if (pTileSize > 40 && pTileSize <= 60)
            {
                _fontSize = 24;
            }
            else         
            {
                _fontSize = 18;
            }

            SqIndex = pSqIndex;
            PagePopup.IsOpen = true;


            CreateButtonsEditTool();

            // Calculate position
            Double tilePanelBorderThickness = 4;
            Double xMargin = _boardVM.BoardCoordinateMargin.Left;            
            Double xPos;
            Double yPos;
            int buttonCount = mainStackA.Children.Count + mainStackB.Children.Count;

            double maxX = pTileSize * 8 - buttonCount * _buttonSize + xMargin + tilePanelBorderThickness;
            if (maxX < 0) maxX = 0;

            double midX = (_buttonSize * buttonCount) / 2 - (pTileSize / 2);

            if (pTileCoordinates.X - midX > 0) xPos = pTileCoordinates.X - midX + xMargin + tilePanelBorderThickness;
            else xPos = 0 + xMargin + tilePanelBorderThickness;

            if (xPos > maxX) xPos = maxX;

            if (pTileCoordinates.Y - _buttonSize > 0) yPos = pTileCoordinates.Y - _buttonSize;
            else yPos = pTileCoordinates.Y + pTileSize;


            PagePopup.HorizontalOffset = xPos;
            PagePopup.VerticalOffset = yPos;

            
        }

        /// <summary>
        /// Close the popup
        /// </summary>
        public void Close()
        {
            if (PagePopup.IsOpen)
            {
                PagePopup.IsOpen = false;
                BoardSquare.EllipseClearAll();
            }
        }

        

        /// <summary>
        /// Creates the piece edit list
        /// </summary>
        /// <returns></returns>
        private List<char> GetPieceEditList()
        {
            List<char> pieceEditList;
            if (EditPieceColour == Constants.BLACKPIECE)
            {
                pieceEditList = new List<char>() { 'p', 'r', 'n', 'b', 'q' };
            }
            else
            {
                pieceEditList = new List<char>() { 'P', 'R', 'N', 'B', 'Q' };
            }

            return pieceEditList;
        }

        
        /// <summary>
        /// Create edit buttons
        /// </summary>        
        private void CreateButtonsEditTool()
        {
            List<char> pieceEditList = GetPieceEditList();
            
            CreatePieceButtons(pieceEditList);

            // **********************
            // Button A
            // **********************
            mainStackA.Children.Clear();

            Button eraseButton = new Button
            {
                Width = _buttonSize,
                Height = _buttonSize,
                Padding = new Thickness(0),
                Content = "\uE75C",
                FontFamily = new FontFamily("Segoe MDL2 Assets"),
                FontSize = _fontSize,               
                Background = new SolidColorBrush(Color.FromArgb(50, 251, 251, 251)),
                Tag = ' ' 
            };

            eraseButton.Click += EditPieceSelect_Click;
            mainStackA.Children.Add(eraseButton);

            Button sqArrowButton = new Button
            {
                Width = _buttonSize,
                Height = _buttonSize,
                Padding = new Thickness(0),
                Content = "\uE76C",
                FontFamily = new FontFamily("Segoe MDL2 Assets"),
                FontSize = _fontSize,
                Background = new SolidColorBrush(Color.FromArgb(50, 251, 251, 251)),
                Tag = ' '
            };
            sqArrowButton.Click += EditPieceArrow_Click;
            mainStackA.Children.Add(sqArrowButton);
                               
        }

        /// <summary>
        /// Create piece buttons
        /// </summary>
        /// <param name="pColour"></param>
        private void CreatePieceButtons(List<char> pPieceList)
        {
            // **********************
            // Button B
            // **********************                        
            mainStackB.Children.Clear();            
                        

            foreach (char fen in pPieceList)
            {
                Button pieceButton = new Button
                {
                    Width = _buttonSize,
                    Height = _buttonSize,
                    Padding = new Thickness(0),
                    Content = new Image
                    {
                        Source = Pieces.Piece.GetImage(fen, _buttonSize, _buttonSize),
                        VerticalAlignment = VerticalAlignment.Center
                    },
                    Background = new SolidColorBrush(Color.FromArgb(50, 251, 251, 251))

                };
                pieceButton.Tag = fen;

                pieceButton.Click += EditPieceSelect_Click;

                mainStackB.Children.Add(pieceButton);
            }

        }

        /// <summary>
        /// Update piece on board square
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void EditPieceSelect_Click(object sender, RoutedEventArgs e)
        {
            char fen = (char)((Button)sender).Tag;
            _boardVM.ArrangeUpdate(fen, SqIndex);
            PagePopup.IsOpen = false;
        }

        /// <summary>
        /// Update piece on board square
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void EditPieceArrow_Click(object sender, RoutedEventArgs e)
        {
            EditPieceColour *= -1;

            List<char> editPieceList = GetPieceEditList();
            CreatePieceButtons(editPieceList);
        }

        /// <summary>
        /// Calculates the size of the button based on the tile size
        /// </summary>
        private double calcButtonSize(Double pTileSize)
        {
            if (pTileSize > 90)
            {
                return pTileSize * 0.85;
            }
            else
            {
                return pTileSize * 0.95;
            }
        }

       

        

    }
}

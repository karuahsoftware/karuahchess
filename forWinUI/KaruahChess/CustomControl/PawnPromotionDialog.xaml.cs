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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using KaruahChess.Common;

namespace KaruahChess.CustomControl
{
    

    public sealed partial class PawnPromotionDialog : ContentDialog
    {
        double _buttonSize = 80;
       
        public int Result { get; set; }

        /// <summary>
        /// Constructor
        /// </summary>
        public PawnPromotionDialog()
        {
            this.InitializeComponent();

            // Default result
            this.Result = 5; 
        }


        /// <summary>
        /// Show the popup
        /// </summary>
        /// <returns></returns>
        public void CreateContent(int pColour)
        {            
            var pieceList = GetPieceList(pColour);
            CreatePieceButtons(pieceList);
            
        }

        
        /// <summary>
        /// Creates the piece list
        /// </summary>
        /// <returns></returns>
        private List<char> GetPieceList(int pColour)
        {
            List<char> pieceList;
            if (pColour == Constants.BLACKPIECE)
            {
                pieceList = new List<char>() { 'r', 'n', 'b', 'q' };
            }
            else
            {
                pieceList = new List<char>() { 'R', 'N', 'B', 'Q' };
            }

            return pieceList;
        }


        /// <summary>
        /// Create piece buttons
        /// </summary>
        /// <param name="pColour"></param>
        private void CreatePieceButtons(List<char> pPieceList)
        {

            mainStackA.Children.Clear();

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
                    }
                };
                pieceButton.Tag = fen;

                pieceButton.Click += PawnPromotePieceSelect_Click;

                mainStackA.Children.Add(pieceButton);
            }

        }

        /// <summary>
        /// Update piece on board square
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void PawnPromotePieceSelect_Click(object sender, RoutedEventArgs e)
        {
            char fen = (char)((Button)sender).Tag;

            fen = char.ToUpper(fen);

            if (fen == 'R') Result = 4;
            else if (fen == 'B') Result = 3;
            else if (fen == 'N') Result = 2;
            else Result = 5;

            this.Hide();
        }


    }
}

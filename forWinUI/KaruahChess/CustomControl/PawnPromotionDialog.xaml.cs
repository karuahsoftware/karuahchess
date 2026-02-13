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
using Microsoft.UI.Xaml.Controls;
using KaruahChess.Common;
using static KaruahChess.Pieces.Piece;

namespace KaruahChess.CustomControl
{
    

    public sealed partial class PawnPromotionDialog : Page
    {        
        ContentDialog _dialog;

        public int Result { get; set; }

        /// <summary>
        /// Constructor
        /// </summary>
        public PawnPromotionDialog(int pColour)
        {
            this.InitializeComponent();

            // Default result
            this.Result = 5;

            if (pColour == (int)ColourEnum.White)
            {
                whitePiecesStack.Visibility = Visibility.Visible;
                blackPiecesStack.Visibility = Visibility.Collapsed;
            }
            else
            {
                whitePiecesStack.Visibility = Visibility.Collapsed;
                blackPiecesStack.Visibility = Visibility.Visible;
            }
        }

        /// <summary>
        /// Create the dialog
        /// </summary>
         public ContentDialog CreateDialog()
        {
            _dialog = new ContentDialog
            {
                Title = "Pawn promotion -> Select a piece",
                Content = this
            };
           
            return _dialog;
        }
        

        /// <summary>
        /// Update piece on board square with the selected promotion piece
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void pieceBtn_Click(object sender, RoutedEventArgs e)
        {            
            Button btn = (Button)sender;
            string tag = (string)btn.Tag;
            Char fen = tag[0];

            fen = char.ToUpper(fen);
            if (fen == 'R') Result = 4;
            else if (fen == 'B') Result = 3;
            else if (fen == 'N') Result = 2;
            else Result = 5;

            _dialog.Hide();
        }


    }
}

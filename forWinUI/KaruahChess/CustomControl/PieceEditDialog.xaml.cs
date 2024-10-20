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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using KaruahChess.Common;
using KaruahChess.ViewModel;

namespace KaruahChess.CustomControl
{
    public sealed partial class PieceEditDialog : Page
    {
        ContentDialog dialog;
        BoardViewModel boardVM;

        public PieceEditDialog()
        {
            this.InitializeComponent();            

        }

        /// <summary>
        /// Create the dialog
        /// </summary>
        public ContentDialog CreateDialog(BoardViewModel pBoardVM)
        {
            boardVM = pBoardVM;

            dialog = new ContentDialog
            {
                Title = "Add piece to selected squares",
                Content = this,
                PrimaryButtonText = "Close"
            };

            return dialog;
        }

        /// <summary>
        /// Click handler for black pawn
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void pieceBtn_Click(object sender, RoutedEventArgs e)
        {
            Button btn = (Button)sender;
            string tag = (string)btn.Tag;
            Char fen = tag[0];
            boardVM.editToolUpdateSelectedTiles(fen);
            dialog.Hide();
        }
    }
}

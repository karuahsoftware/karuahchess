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
using Microsoft.UI.Xaml.Controls;
using KaruahChess.Pieces;
using KaruahChess.Model;
using KaruahChessEngine;
using KaruahChess.Common;

namespace KaruahChess.CustomControl
{
    public sealed partial class CastlingRightsDialog : Page
    {
        private GameRecordArray record;
        private KaruahChessEngineClass board = new KaruahChessEngineClass();
        private int kingSpin;
        private ViewModel.BoardViewModel boardVM;

        public CastlingRightsDialog(Piece.TypeEnum pPieceType, Piece.ColourEnum pColour, GameRecordArray pRecord, ViewModel.BoardViewModel pBoardVM)
        {
            this.InitializeComponent();

            boardVM = pBoardVM;

            var imageSize = 70;
            KingImage.Source = Piece.GetImage(pPieceType, pColour, imageSize, imageSize, boardVM.LargePawnEnabled);
            KingImage.Width = imageSize;
            KingImage.Height = imageSize;

            record = pRecord;
            kingSpin = (int)pPieceType * (int)pColour;

            setControlState();
        }

        public ContentDialog CreateDialog()
        {
            var dialog = new ContentDialog
            {
                Title = "Castling Rights",
                Content = this,
                PrimaryButtonText = "Close"
            };

            dialog.PrimaryButtonClick += Close_Click;

            return dialog;
        }

        /// <summary>
        /// Enables and disables controls depending on options set
        /// </summary>        
        private void setControlState()
        {
            board.SetBoardArray(record.BoardArray);
            board.SetStateArray(record.StateArray);

            int stateCastlingAvailability = board.GetStateCastlingAvailability();
            if (kingSpin == Constants.WHITE_KING_SPIN)
            {
                KingSideCastleCheckBox.IsChecked = (stateCastlingAvailability & 0b000010) > 0;
                QueenSideCastleCheckBox.IsChecked = (stateCastlingAvailability & 0b000001) > 0;
         }
            else
            {
                KingSideCastleCheckBox.IsChecked = (stateCastlingAvailability & 0b001000) > 0;
                QueenSideCastleCheckBox.IsChecked = (stateCastlingAvailability & 0b000100) > 0;
             }
        }

        /// <summary>
        /// Saves form values
        /// </summary>  
        private void Save()
        {

            bool success = false;

            // Check that castling selection is valid
            if (kingSpin == Constants.WHITE_KING_SPIN)
            {
                int stateCastlingAvailability = 0;
                if (KingSideCastleCheckBox.IsChecked ?? false) stateCastlingAvailability = stateCastlingAvailability | 0b000010;
                if (QueenSideCastleCheckBox.IsChecked ?? false) stateCastlingAvailability = stateCastlingAvailability | 0b000001;
                success = board.SetStateCastlingAvailability(stateCastlingAvailability, Constants.WHITEPIECE);
            }
            else if (kingSpin == Constants.BLACK_KING_SPIN)
            {
                int stateCastlingAvailability = 0;
                if (KingSideCastleCheckBox.IsChecked ?? false) stateCastlingAvailability = stateCastlingAvailability | 0b001000;
                if (QueenSideCastleCheckBox.IsChecked ?? false) stateCastlingAvailability = stateCastlingAvailability | 0b000100;
                success = board.SetStateCastlingAvailability(stateCastlingAvailability, Constants.BLACKPIECE);
            }

            // Save the values
            if (success)
            {
                GameRecordArray updatedRecord = new GameRecordArray();
                updatedRecord.Id = record.Id;
                board.GetBoardArray(updatedRecord.BoardArray);
                board.GetStateArray(updatedRecord.StateArray);

                GameRecordDataService.instance.UpdateGameState(updatedRecord);
                boardVM.UpdateBoardIndicators(updatedRecord);
            }
            else
            {
                boardVM.ShowBoardMessage("","Error, cannot set castling rights as Rook or King position is not valid for castling.", TextMessage.TypeEnum.Error, TextMessage.AnimationEnum.FadeOut);
            }

        }


        private void Close_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            Save();
            SenderDialog.PrimaryButtonClick -= Close_Click;
        }

        
    }
}

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
using KaruahChessEngine;
using KaruahChess.Model;
using KaruahChess.Common;

namespace KaruahChess.CustomControl
{
    public sealed partial class Export : UserControl
    {
        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }
               
        
        private KaruahChessEngineClass _board;

        public Export()
        {

            this.InitializeComponent();

            _board = new KaruahChessEngineClass();

            // Loads a default style template if one has not been set
            if (StyleTemplate == null) {
                StyleTemplate = (CustomStyleTemplate)CustomStyleDefaultResourceDictionary["CustomStyleTemplateDefaultObject"];
            }
            PagePopup.IsOpen = false;
        }

        /// <summary>
        /// Sets up the board 
        /// </summary>
        /// <param name="pBoardVM"></param>
        public void SetBoard(GameRecordArray pGameRecordArray)
        {
            _board.SetBoardArray(pGameRecordArray.BoardArray);
            _board.SetStateArray(pGameRecordArray.StateArray);
        }


        /// <summary>
        /// Show the popup
        /// </summary>
        /// <returns></returns>
        public void Show() {
            PagePopup.IsOpen = true;

        }

        /// <summary>
        /// Close the popup
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Close_Click(object sender, RoutedEventArgs e)
        {
            ExportErrorText.Text = "";
            ExportTextBox.Text = "";
            PagePopup.IsOpen = false;
        }

        /// <summary>
        /// Sets the position of the control
        /// </summary>
        /// <param name="pMaxWidth"></param>
        public void SetPosition(double pMaxWidth)
        {
            if (pMaxWidth <= 400)
            {
                this.SetValue(Canvas.LeftProperty, 5);
                this.SetValue(Canvas.TopProperty, 5);
                this.StyleTemplate.Width = pMaxWidth - 15;
                this.StyleTemplate.Height = pMaxWidth - 15;
            }
            if (pMaxWidth > 400)
            {
                double popupSize = pMaxWidth * 0.8;
                double popupOffset = (pMaxWidth - popupSize) / 2 - 5;
                this.SetValue(Canvas.LeftProperty, popupOffset);
                this.SetValue(Canvas.TopProperty, popupOffset);
                this.StyleTemplate.Width = popupSize;
                this.StyleTemplate.Height = popupSize;
            }
            
        }


        /// <summary>
        /// Gets the full fen string of the board
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void FEN_Click(object sender, RoutedEventArgs e)
        {

            ExportErrorText.Text = "";
            ExportTextBox.Text = "Current board FEN String:\n"
                                 + _board.GetFullFEN() + "\n\n"
                                 + "State string:\n"
                                 + _board.GetState();



        }


        /// <summary>
        /// Clear the export box
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Clear_Click(object sender, RoutedEventArgs e)
        {
            ExportErrorText.Text = "";
            ExportTextBox.Text = "";
        }

        
    }
}

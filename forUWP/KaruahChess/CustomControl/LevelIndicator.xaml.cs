/*
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
using Windows.UI.Xaml.Controls;
using KaruahChess.Common;

namespace KaruahChess.CustomControl
{
    public sealed partial class LevelIndicator : UserControl
    {
        
                
        private ViewModel.BoardViewModel _boardVM;

        /// <summary>
        /// Constructor
        /// </summary>
        public LevelIndicator()
        {
            this.InitializeComponent();                     
                        
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
        /// Level indicator button click event
        /// </summary>
        private void LevelIndicatorButton_Click(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            _boardVM.showEngineSettingsDialog();
        }

        /// <summary>
        /// Gets the level text
        /// </summary>
        private string getLevelText(int pElo)
        {
            return (Constants.eloList.IndexOf(pElo) + 1).ToString();   
        }
    }
}

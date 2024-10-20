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

using Microsoft.UI.Xaml;
using KaruahChess.ViewModel;


namespace KaruahChess
{    
    public sealed partial class MainWindow : Window
    {
        
        public MainWindow()
        {

            

            // Initialise the Board view model
            BoardVM = new BoardViewModel(this);
                       

            this.InitializeComponent();



            // Set Control reference                                 
            BoardVM.SetBoardTextMessageControl(BoardTextMessage);
            BoardVM.SetBoardTilePanelControl(BoardTilePanel);
            BoardVM.SetPieceAnimationControl(PieceAnimationControl);
            BoardVM.SetCoordinatesControl(CoordinatesControl);
            
            ChessClockControl.SetBoardVM(BoardVM);
            BoardVM.SetChessClockControl(ChessClockControl);
            
            MoveNavigatorControl.SetBoardVM(BoardVM);
            BoardVM.SetMoveNavigatorControl(MoveNavigatorControl);

            LevelIndicatorControl.SetBoardVM(BoardVM);
            BoardVM.SetLevelIndicatorControl(LevelIndicatorControl);

            BoardVM.PostInit();

        }


        //Propeties          
        public BoardViewModel BoardVM { get; set; }


    }
}

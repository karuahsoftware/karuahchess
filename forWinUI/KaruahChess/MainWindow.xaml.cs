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


using KaruahChess.ViewModel;
using Microsoft.UI.Xaml;
using System;

namespace KaruahChess
{    
    public sealed partial class MainWindow : Window
    {
        
        public MainWindow()
        {

            // Initialise the Board view model
            BoardVM = new BoardViewModel(this);
                       

            this.InitializeComponent();

            // Subscribe to events
            this.Activated += MainWindow_Activated;
            this.VisibilityChanged += MainWindow_VisibilityChanged;
            this.Closed += MainWindow_Closed;


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
            
            ActionButtonsControl.SetBoardVM(BoardVM);

            BoardVM.PostInit();
        
        }

        

        //Propeties          
        public BoardViewModel BoardVM { get; set; }

        /// <summary>
        /// Main window activated event
        /// </summary>        
        private void MainWindow_Activated(object sender, WindowActivatedEventArgs args)
        {
            if (args.WindowActivationState == WindowActivationState.Deactivated)
            {
                // Stop voice recogntion if running
                BoardVM.StopVoiceListen();
            }            
        }

        /// <summary>
        /// Main window visibility event
        /// </summary>        
        private void MainWindow_VisibilityChanged(object sender, WindowVisibilityChangedEventArgs args)
        {
            // Stop voice recogntion if running
            BoardVM.StopVoiceListen();
        }

        /// <summary>
        /// Main window closed event
        /// </summary>        
        private void MainWindow_Closed(object sender, WindowEventArgs args)
        {
            // Clean up when the window is closed
            // Stop voice recogntion if running
            BoardVM.StopVoiceListen();

        }


    }
}

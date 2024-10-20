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

namespace KaruahChess.CustomControl
{
    public sealed partial class ClockPanel : UserControl
    {
        DispatcherTimer _timer;
        ViewModel.BoardViewModel boardVM;
        TimeSpan whiteHistorical = new TimeSpan(0, 0, 0);
        TimeSpan blackHistorical = new TimeSpan(0, 0, 0);
        bool displayHistorical = false;
        bool clockIsTicking = false;
        bool clockEnabled = false;

        public ClockPanel()
        {
            this.InitializeComponent();                        
        }

        /// <summary>
        /// White clock
        /// </summary>
        public Clock WhiteClock { get; private set; } = new Clock();
        
        /// <summary>
        /// Black clock
        /// </summary>
        public Clock BlackClock { get; private set; } = new Clock();

                
        /// <summary>
        /// Sets the board view model
        /// </summary>        
        public void SetBoardVM(ViewModel.BoardViewModel pBoardVM)
        {
            boardVM = pBoardVM;
        }



        public Orientation ClockOrientation
        {
            get { return (Orientation)GetValue(ClockOrientationProperty); }
            set { SetValue(ClockOrientationProperty, value); }
        }

        // Using a DependencyProperty as the backing store for ClockOrientation.  
        public static readonly DependencyProperty ClockOrientationProperty =
            DependencyProperty.Register("ClockOrientation", typeof(Orientation), typeof(ClockPanel), new PropertyMetadata(Orientation.Horizontal));

        

        

        /// <summary>
        /// Switch to white or black clock
        /// </summary>        
        public void Start(int pPieceColour)
        {
            if (!clockEnabled) return;

            if (pPieceColour == Constants. WHITEPIECE)
            {
                BlackClock.Pause();
                WhiteClock.Start();
            }
            else
            {
                WhiteClock.Pause();
                BlackClock.Start();
            }

            if (!clockIsTicking)
            {
                RefreshClockDisplay();
                RunClockTick();
            }

            SetEditButton();


        }

        /// <summary>
        /// Pause the clock
        /// </summary>
        public void PauseClock()
        {
            if (!clockEnabled) return;

            WhiteClock.Pause();
            BlackClock.Pause();

            StopClockTick();
            RefreshClockDisplay();
            SetEditButton();
        }

        /// <summary>
        /// Determines if both clocks are paused
        /// </summary>
        public bool IsPaused()
        {
            return WhiteClock.IsPaused() && BlackClock.IsPaused();
        }

        /// <summary>
        /// Clock tick
        /// </summary>
        private void RunClockTick()
        {
            clockIsTicking = true;

            // Create a new timer to update the timer display
            _timer = new DispatcherTimer();
            _timer.Tick += ClockTick;
            _timer.Interval = TimeSpan.FromMilliseconds(1000);
            _timer.Start();
           
        }


        /// <summary>
        /// Updates the timer display
        /// </summary>        
        private void ClockTick(object sender, Object e)
        {
            RefreshClockDisplay();
            CheckTimeExpired();
        }

        /// <summary>
        /// Stop the clock ticking. The clock timer may still be running, this just stops
        /// the clock display from being updated.
        /// </summary>   
        private void StopClockTick()
        {
            if (_timer != null)
            {
                _timer.Stop();
                _timer = null;
            }
            clockIsTicking = false;
        }


        /// <summary>
        /// Refresh the clock display
        /// </summary>
        public void RefreshClockDisplay()
        {

            if (!displayHistorical)
            {
                WhiteTimerText.Text = GetHHMMSS(WhiteClock.RemainingTime()) + " W";
                BlackTimerText.Text = GetHHMMSS(BlackClock.RemainingTime()) + " B";
            }
            else
            {
                WhiteTimerText.Text = GetHHMMSS(whiteHistorical) + " W";
                BlackTimerText.Text = GetHHMMSS(blackHistorical) + " B";
            }


        }

        /// <summary>
        /// Convert a time in seconds to a hh:mm:ss formatted string
        /// </summary>
        private string GetHHMMSS(TimeSpan pTimeSpan)
        {


            if (pTimeSpan.TotalSeconds >= 0 && pTimeSpan.TotalSeconds < 3600)
            {
                return pTimeSpan.Minutes.ToString("D2") + ":" + pTimeSpan.Seconds.ToString("D2");
            }
            else
            {
                return pTimeSpan.Hours.ToString("D2") + ":" + pTimeSpan.Minutes.ToString("D2") + ":" + pTimeSpan.Seconds.ToString("D2");

            }
        }


        /**
        * Show the control
        */
        public void Show()
        {
            this.Visibility = Visibility.Visible;
                               
            SetEditButton();
            clockEnabled = true;
        }


        /// <summary>
        /// Initialise the clocks and display
        /// </summary>
        public void SetClock(int pWhiteClockSeconds, int pBlackClockSeconds)
        {
            if (!clockEnabled) return;

            WhiteClock.SetNewLimit(new TimeSpan(0, 0, pWhiteClockSeconds));
            BlackClock.SetNewLimit(new TimeSpan(0, 0, pBlackClockSeconds));


            ShowCurrentTime();
            RefreshClockDisplay();
            SetEditButton();
        }


        /// <summary>
        /// Shows the requested time.
        /// </summary>        
        public void ShowHistoricalTime(int pWhiteSeconds, int pBlackSeconds)
        {
            if (!clockEnabled) return;

            whiteHistorical = new TimeSpan(0, 0, pWhiteSeconds);
            blackHistorical = new TimeSpan(0, 0, pBlackSeconds);
            displayHistorical = true;
            RefreshClockDisplay();

        }

        /// <summary>
        /// Display the current clock
        /// </summary>  
        public void ShowCurrentTime()
        {
            if (!clockEnabled) return;

            displayHistorical = false;
            RefreshClockDisplay();
        }



        /// <summary>
        /// Hide the control
        /// </summary>  
        public void Hide()
        {
            PauseClock();
            SetClock(0, 0);
            this.Visibility = Visibility.Collapsed;
            clockEnabled = false;
        }


        /// <summary>
        /// Displays the edit button if the clock is paused
        /// </summary> 
        private void SetEditButton()
        {
            if (IsPaused())
            {
                ClockEditButton.Visibility = Visibility.Visible;
                ClockStartPauseButton.Content = "\xF5B0";
            }
            else
            {
                ClockEditButton.Visibility = Visibility.Collapsed;
                ClockStartPauseButton.Content = "\xE916";
            }
        }

        /// <summary>
        /// Check if time has expired
        /// </summary>
        private void CheckTimeExpired()
        {
            if (WhiteClock.RemainingTime().TotalSeconds == 0 && BlackClock.RemainingTime().TotalSeconds > 0)
            {
                boardVM.TimeExpired(Pieces.Piece.ColourEnum.Black);
                PauseClock();
            }
            else if (WhiteClock.RemainingTime().TotalSeconds > 0 && BlackClock.RemainingTime().TotalSeconds == 0)
            {
                boardVM.TimeExpired(Pieces.Piece.ColourEnum.White);
                PauseClock();
            }
        }
        

        /// <summary>
        /// Clock start pause toggle button click event
        /// </summary>        
        private void ClockStartPauseButton_Click(object sender, RoutedEventArgs e)
        {
            if (IsPaused())
            {
                boardVM.CheckChessClock();
            }
            else
            {
                PauseClock();
            }
            
        }

        /// <summary>
        /// Clock edit button click event
        /// </summary>        
        private async void ClockEditButton_Click(object sender, RoutedEventArgs e)
        {
            await boardVM.ShowClockSettingsDialog();
            
        }

        


        


       

        



    }
}

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
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using System.Diagnostics;


namespace KaruahChess.CustomControl
{
    public sealed partial class ChessClock : UserControl
    {
        DispatcherTimer _timer;
        Stopwatch _stopwatchWhite;
        Stopwatch _stopwatchBlack;
        bool _showCurrentTime;
        bool _isInitialised;

        public ChessClock()
        {
            this.InitializeComponent();

            
            
        }

        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public ChessClockStyleTemplate StyleTemplate { get; set; }


        /// <summary>
        /// Routine to initialise, or unintialise the clock
        /// </summary>
        private void Init(bool pInitialise)
        {
            if (pInitialise) {

                if (!_isInitialised)
                {                    
                    _showCurrentTime = true;

                    // Loads a default style template if one has not been set
                    if (StyleTemplate == null)
                    {
                        StyleTemplate = (ChessClockStyleTemplate)ChessClockStyleDefaultResourceDictionary["ChessClockStyleTemplateDefaultObject"];
                    }

                    // Create new stopwatch to measure play time.
                    _stopwatchWhite = new Stopwatch();
                    _stopwatchBlack = new Stopwatch();

                    // Create a new timer to update the timer display
                    _timer = new DispatcherTimer();
                    _timer.Tick += Timer_Click;
                    _timer.Interval = TimeSpan.FromMilliseconds(1000);
                    _timer.Start();

                    // Show the clock
                    this.Visibility = Visibility.Visible;

                    _isInitialised = true;
                }
            }
            else
            {
                // Unitialises the clock
                _showCurrentTime = false;

                // Create new stopwatch to measure play time.
                _stopwatchWhite = null;
                _stopwatchBlack = null;

                // Create a new timer to update the timer display
                _timer = null;
              
                // Hide the clock
                this.Visibility = Visibility.Collapsed;

                _isInitialised = false;
            }
        }
               
        public int WhiteOffset
        {
            get { return (int)GetValue(WhiteOffsetProperty); }
            set { SetValue(WhiteOffsetProperty, value); }
        }

        // Using a DependencyProperty as the backing store for WhiteOffset.
        public static readonly DependencyProperty WhiteOffsetProperty =
            DependencyProperty.Register("WhiteOffset", typeof(int), typeof(ChessClock), new PropertyMetadata(0));


        public int BlackOffset
        {
            get { return (int)GetValue(BlackOffsetProperty); }
            set { SetValue(BlackOffsetProperty, value); }
        }

        // Using a DependencyProperty as the backing store for BlackOffset.
        public static readonly DependencyProperty BlackOffsetProperty =
            DependencyProperty.Register("BlackOffset", typeof(int), typeof(ChessClock), new PropertyMetadata(0));



        public double WhiteTotalTime
        {
            get { return (double)GetValue(WhiteTotalTimeProperty); }
            set { SetValue(WhiteTotalTimeProperty, value); }
        }

        // Using a DependencyProperty as the backing store for WhiteTotalTime.
        public static readonly DependencyProperty WhiteTotalTimeProperty =
            DependencyProperty.Register("WhiteTotalTime", typeof(double), typeof(ChessClock), new PropertyMetadata(0.0));

        public double BlackTotalTime
        {
            get { return (double)GetValue(BlackTotalTimeProperty); }
            set { SetValue(BlackTotalTimeProperty, value); }
        }

        // Using a DependencyProperty as the backing store for BlackTotalTime.
        public static readonly DependencyProperty BlackTotalTimeProperty =
            DependencyProperty.Register("BlackTotalTime", typeof(double), typeof(ChessClock), new PropertyMetadata(0.0));



        public Orientation ClockOrientation
        {
            get { return (Orientation)GetValue(ClockOrientationProperty); }
            set { SetValue(ClockOrientationProperty, value); }
        }

        // Using a DependencyProperty as the backing store for ClockOrientation.  
        public static readonly DependencyProperty ClockOrientationProperty =
            DependencyProperty.Register("ClockOrientation", typeof(Orientation), typeof(ChessClock), new PropertyMetadata(Orientation.Horizontal));

        

        public bool ClockEnabled
        {
            get { return (bool)GetValue(ClockEnabledProperty); }
            set {
                var currentValue = ClockEnabled;
                if (currentValue != value)
                {
                    Init(value);
                    SetValue(ClockEnabledProperty, value);

                }
            }
        }

        // Using a DependencyProperty as the backing store for ClockEnabled.
        public static readonly DependencyProperty ClockEnabledProperty =
            DependencyProperty.Register("ClockEnabled", typeof(bool), typeof(ChessClock), new PropertyMetadata(false));




        /// <summary>
        /// Updates the timer display
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Timer_Click(object sender, Object e)
        {            
            UpdateCurrentTime();
        }

        /// <summary>
        /// Shows the requested time.
        /// </summary>
        /// <param name="pWhiteSeconds"></param>
        /// <param name="pBlackSeconds"></param>
        public void ShowHistoricalTime(int pWhiteSeconds, int pBlackSeconds)
        {
            if (!_isInitialised) return;

            _showCurrentTime = false;

            var whiteTimeSpan = new TimeSpan(0, 0, pWhiteSeconds);
            WhiteTimerText.Text = "W." + whiteTimeSpan.ToString(@"h\:mm\:ss");

            var blackTimeSPan = new TimeSpan(0, 0, pBlackSeconds);
            BlackTimerText.Text = "B." + blackTimeSPan.ToString(@"h\:mm\:ss");

        }

        public void ShowCurrentTime()
        {
            if (!_isInitialised) return;

            _showCurrentTime = true;
        }

        /// <summary>
        /// Switch to white or black clock
        /// </summary>
        /// <param name="pReset"></param>
        public void Start(int pColour)
        {
            if (!_isInitialised) return;

            if (pColour == 1) {
                // White
                 if(_stopwatchBlack.IsRunning) _stopwatchBlack.Stop();
                if(!_stopwatchWhite.IsRunning) _stopwatchWhite.Start();                
            }
            else if (pColour == -1)
            {
                // Black
                if (_stopwatchWhite.IsRunning) _stopwatchWhite.Stop();
                if (!_stopwatchBlack.IsRunning) _stopwatchBlack.Start();                
            }
            
                        
        }


        /// <summary>
        /// Stop both white and black clocks
        /// </summary>
        public void StopAll()
        {
            if (!_isInitialised) return;

            _stopwatchBlack.Stop();
            _stopwatchWhite.Stop();            
        }

        /// <summary>
        /// Reset both white and black clocks
        /// </summary>
        public void ResetAll()
        {
            if (!_isInitialised) return;

            _stopwatchBlack.Reset();
            _stopwatchWhite.Reset();            

            WhiteTotalTime = 0;
            BlackTotalTime = 0;
            
            WhiteTimerText.Text = "W." + (_stopwatchWhite.Elapsed + new TimeSpan(0, 0, WhiteOffset)).ToString(@"h\:mm\:ss");
            BlackTimerText.Text = "B." + (_stopwatchBlack.Elapsed + new TimeSpan(0, 0, BlackOffset)).ToString(@"h\:mm\:ss");

            _showCurrentTime = true;            
        }


        /// <summary>
        /// Updates the current time display
        /// </summary>
        public void UpdateCurrentTime()
        {
            if (!_isInitialised) return;

            if (_stopwatchWhite != null)
            {
                var initialOffset = new TimeSpan(0, 0, WhiteOffset);
                var totalTime = _stopwatchWhite.Elapsed + initialOffset;
                WhiteTotalTime = totalTime.TotalSeconds;
                if (_showCurrentTime) WhiteTimerText.Text = "W." + totalTime.ToString(@"h\:mm\:ss");
            }

            if (_stopwatchBlack != null)
            {
                var initialOffset = new TimeSpan(0, 0, BlackOffset);
                var totalTime = _stopwatchBlack.Elapsed + initialOffset;
                BlackTotalTime = totalTime.TotalSeconds;
                if (_showCurrentTime) BlackTimerText.Text = "B." + totalTime.ToString(@"h\:mm\:ss");
            }
        }

    }
}

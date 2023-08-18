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

using KaruahChess.Common;
using KaruahChess.Model;
using KaruahChess.Model.ParameterObjects;
using System;
using System.Collections.Generic;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace KaruahChess.CustomControl
{
    public sealed partial class ClockSettings : UserControl
    {
        private ViewModel.BoardViewModel _boardVM;
        private readonly List<string> clockHourLabel = Constants.clockHour;
        private readonly List<string> clockMinSecLabel = Constants.clockMinSec;
        private readonly List<string> clockResetLabel = Constants.clockResetLabel;

        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }

        
        /// <summary>
        /// Constructor
        /// </summary>
        public ClockSettings()
        {

            this.InitializeComponent();

            // Loads a default style template if one has not been set
            if (StyleTemplate == null)
            {
                StyleTemplate = (CustomStyleTemplate)CustomStyleDefaultResourceDictionary["CustomStyleTemplateDefaultObject"];
            }
            PagePopup.IsOpen = false;
                        
            
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
        /// Show the popup
        /// </summary>
        /// <returns></returns>
        public void Show()
        {
            
            
            // White hours combo
            int whiteHours = _boardVM.chessClockControl.WhiteClock.RemainingTime().Hours;
            if(whiteHours >= 0 && whiteHours <= 10)
            {
                WhiteTimeHourCombo.SelectedIndex = whiteHours;
            }
            else
            {
                WhiteTimeHourCombo.SelectedIndex = 0;
            }

            // White minutes combo
            int whiteMinutes = _boardVM.chessClockControl.WhiteClock.RemainingTime().Minutes;
            if (whiteMinutes >= 0 && whiteMinutes <= 59)
            {
                WhiteTimeMinuteCombo.SelectedIndex = whiteMinutes;
            }
            else
            {
                WhiteTimeMinuteCombo.SelectedIndex = 0;
            }

            // White seconds combo
            int whiteSeconds = _boardVM.chessClockControl.WhiteClock.RemainingTime().Seconds;
            if (whiteSeconds >= 0 && whiteSeconds <= 59)
            {
                WhiteTimeSecondCombo.SelectedIndex = whiteSeconds;
            }
            else
            {
                WhiteTimeSecondCombo.SelectedIndex = 0;
            }

            // Black hours combo
            int BlackHours = _boardVM.chessClockControl.BlackClock.RemainingTime().Hours;
            if (BlackHours >= 0 && BlackHours <= 10)
            {
                BlackTimeHourCombo.SelectedIndex = BlackHours;
            }
            else
            {
                BlackTimeHourCombo.SelectedIndex = 0;
            }

            // Black minutes combo
            int BlackMinutes = _boardVM.chessClockControl.BlackClock.RemainingTime().Minutes;
            if (BlackMinutes >= 0 && BlackMinutes <= 59)
            {
                BlackTimeMinuteCombo.SelectedIndex = BlackMinutes;
            }
            else
            {
                BlackTimeMinuteCombo.SelectedIndex = 0;
            }

            // Black seconds combo
            int BlackSeconds = _boardVM.chessClockControl.BlackClock.RemainingTime().Seconds;
            if (BlackSeconds >= 0 && BlackSeconds <= 59)
            {
                BlackTimeSecondCombo.SelectedIndex = BlackSeconds;
            }
            else
            {
                BlackTimeSecondCombo.SelectedIndex = 0;
            }


            int defaultClockIndex = ParameterDataService.instance.Get<ParamClockDefault>().Index;
            if (defaultClockIndex < Constants.clockResetSeconds.Count) 
            {
                ResetCombo.SelectedIndex = defaultClockIndex; 
            }
            else 
            {
                ResetCombo.SelectedIndex = 0; 
            }

            PagePopup.IsOpen = true;

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
        /// Close the popup
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Close_Click(object sender, RoutedEventArgs e)
        {
            Save();
            PagePopup.IsOpen = false;
                        
        }

        

        

        /// <summary>
        /// Saves form values
        /// </summary>
        private void Save()
        {

            int whiteSecondsRemaining = GetWhiteSecondsFromCombo();
            int blackSecondsRemaining = GetBlackSecondsFromCombo();

            // Set the clock
            _boardVM.chessClockControl.SetClock(whiteSecondsRemaining, blackSecondsRemaining);

            // Save the default
            ParamClockDefault clockDefaultParam = ParameterDataService.instance.Get<ParamClockDefault>();            
            if (clockDefaultParam.Index != ResetCombo.SelectedIndex) {
                clockDefaultParam.Index = ResetCombo.SelectedIndex;
                ParameterDataService.instance.Set<ParamClockDefault>(clockDefaultParam);
            }

            // Update the clock if first move
            _boardVM.InitialiseClockFirstMove(whiteSecondsRemaining, blackSecondsRemaining);


            

        }
      
        /// <summary>
        /// Get white seconds from combo
        /// </summary>  
        private int GetWhiteSecondsFromCombo() {
            return (WhiteTimeHourCombo.SelectedIndex * 3600 +
                     WhiteTimeMinuteCombo.SelectedIndex * 60 +
                     WhiteTimeSecondCombo.SelectedIndex);
        }


        /// <summary>
        /// Get white seconds from combo
        /// </summary>  
        private int GetBlackSecondsFromCombo()
        {
            return (BlackTimeHourCombo.SelectedIndex * 3600 +
                     BlackTimeMinuteCombo.SelectedIndex * 60 +
                     BlackTimeSecondCombo.SelectedIndex);
        }

        /// <summary>
        /// Reset clock button
        /// </summary>        
        private void BtnReset_Click(object sender, RoutedEventArgs e)
            {            
                TimeSpan resetTimeSpan = new TimeSpan(0, 0, Constants.clockResetSeconds[ResetCombo.SelectedIndex]);

                WhiteTimeHourCombo.SelectedIndex = resetTimeSpan.Hours;
                BlackTimeHourCombo.SelectedIndex = resetTimeSpan.Hours;

                WhiteTimeMinuteCombo.SelectedIndex = resetTimeSpan.Minutes;
                BlackTimeMinuteCombo.SelectedIndex = resetTimeSpan.Minutes;

                WhiteTimeSecondCombo.SelectedIndex = resetTimeSpan.Seconds;
                BlackTimeSecondCombo.SelectedIndex = resetTimeSpan.Seconds;

                
            }
        }
}

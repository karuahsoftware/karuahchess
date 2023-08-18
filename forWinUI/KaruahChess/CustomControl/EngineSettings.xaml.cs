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
using System.Collections.Generic;
using KaruahChess.Common;
using KaruahChess.Model.ParameterObjects;

namespace KaruahChess.CustomControl
{
    public sealed partial class EngineSettings : UserControl
    {
        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }

        private ViewModel.BoardViewModel _boardVM;
        private readonly List<Strength> strengthList = Constants.strengthList;
                
        private readonly string moveDurationLimitToolTip = "Valid values are 1 to " + 600000.ToString("N0") + ". Leave blank for no limit.";


        /// <summary>
        /// Constructor
        /// </summary>
        public EngineSettings()
        {

            this.InitializeComponent();

            // Loads a default style template if one has not been set
            if (StyleTemplate == null)
            {
                StyleTemplate = (CustomStyleTemplate)CustomStyleDefaultResourceDictionary["CustomStyleTemplateDefaultObject"];
            }
            PagePopup.IsOpen = false;

            // Set maximum threads selectable to be equal to the logical processors on the machine
            // Default is to set to one less than the maximum so some processing power is left over for the application
            ThreadsSlider.Maximum = Environment.ProcessorCount > 1 ? Environment.ProcessorCount : 1;
            
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
            ComputerPlayerCheckBox.IsChecked = _boardVM.ComputerPlayerEnabled;
            ComputerMoveFirstCheckBox.IsChecked = _boardVM.ComputerMoveFirstEnabled;
            RandomiseFirstMoveCheckBox.IsChecked = _boardVM.RandomiseFirstMoveEnabled;
            ComputerSkillLevelCombo.SelectedIndex = Math.Clamp(_boardVM.LimitSkillLevel, 0, Constants.strengthList.Count - 1);
            LevelAutoCheckBox.IsChecked = _boardVM.LevelAutoEnabled;
            ComputerAdvancedSettingsCheckBox.IsChecked = _boardVM.LimitAdvancedEnabled;
            DepthLimitSlider.Value = Math.Clamp(_boardVM.limitDepth, DepthLimitSlider.Minimum, DepthLimitSlider.Maximum);            
            MoveDurationLimitTextBox.Text = _boardVM.limitMoveDuration == 0 ? "" : _boardVM.limitMoveDuration.ToString();
            ThreadsSlider.Value = Math.Clamp(_boardVM.limitThreads, ThreadsSlider.Minimum, ThreadsSlider.Maximum);

            SetControlState();

            // Clear validation errors
            MoveDurationLimitErrorText.Text = "";
            MoveDurationLimitErrorText.Visibility = Visibility.Collapsed;

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

                MoveDurationLimitText.MaxWidth = 100;
            }
            if (pMaxWidth > 400)
            {
                double popupSize = pMaxWidth * 0.8;
                double popupOffset = (pMaxWidth - popupSize) / 2 - 5;
                this.SetValue(Canvas.LeftProperty, popupOffset);
                this.SetValue(Canvas.TopProperty, popupOffset);
                this.StyleTemplate.Width = popupSize;
                this.StyleTemplate.Height = popupSize;

                MoveDurationLimitText.MaxWidth = 200;
            }

        }

        /// <summary>
        /// Close the popup
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Close_Click(object sender, RoutedEventArgs e)
        {
            save();
            PagePopup.IsOpen = false;
                        
        }

        /// <summary>
        /// Reset to default
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ResetToDefault_Click(object sender, RoutedEventArgs e)
        {
            ComputerPlayerCheckBox.IsChecked = new ParamComputerPlayer().Enabled;
            ComputerMoveFirstCheckBox.IsChecked = new ParamComputerMoveFirst().Enabled;
            RandomiseFirstMoveCheckBox.IsChecked = new ParamRandomiseFirstMove().Enabled;
            ComputerSkillLevelCombo.SelectedIndex = Math.Clamp(new ParamLimitSkillLevel().level, 0, Constants.strengthList.Count - 1);
            LevelAutoCheckBox.IsChecked = new ParamLevelAuto().Enabled;
            ComputerAdvancedSettingsCheckBox.IsChecked = new ParamLimitAdvanced().Enabled;
            DepthLimitSlider.Value = new ParamLimitDepth().depth;            

            var defaultMoveDuration = new ParamLimitMoveDuration().moveDurationMS;
            MoveDurationLimitTextBox.Text = defaultMoveDuration == 0 ? "" : defaultMoveDuration.ToString();
            
            ThreadsSlider.Value = new ParamLimitThreads().threads;

            SetControlState();
        }

        /// <summary>
        /// Reset to default
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void StopSearch_Click(object sender, RoutedEventArgs e)
        {
            _boardVM.StopSearchJob();
        }

        /// <summary>
        /// Computer player checkbox button
        /// </summary>
        private void ComputerPlayerCheckBox_Click(object sender, RoutedEventArgs e)
        {         
            SetControlState();
        }

        /// <summary>
        /// Computer player advanced settings checkbox
        /// </summary>
        private void ComputerAdvancedSettingsCheckBox_Click(object sender, RoutedEventArgs e)
        {
            SetControlState();

            
        }

        /// <summary>
        /// Saves form values
        /// </summary>
        private void save()
        {

            _boardVM.ComputerPlayerEnabled = ComputerPlayerCheckBox.IsChecked == true;
            _boardVM.ComputerMoveFirstEnabled = ComputerMoveFirstCheckBox.IsChecked == true;
            _boardVM.RandomiseFirstMoveEnabled = RandomiseFirstMoveCheckBox.IsChecked == true;
            _boardVM.LevelAutoEnabled = LevelAutoCheckBox.IsChecked == true;
            _boardVM.LimitSkillLevel = ComputerSkillLevelCombo.SelectedIndex;
            
            _boardVM.LimitAdvancedEnabled = ComputerAdvancedSettingsCheckBox.IsChecked == true;
            _boardVM.limitDepth = (int)DepthLimitSlider.Value;
            
            Int32.TryParse(MoveDurationLimitTextBox.Text, out int limitMoveDuration);
            if (limitMoveDuration < 0 || limitMoveDuration > 600000) limitMoveDuration = 0;
            _boardVM.limitMoveDuration = limitMoveDuration;

            _boardVM.limitThreads = (int)ThreadsSlider.Value;

            

        }


        /// <summary>
        /// Enables and disables controls depending on options set
        /// </summary>
        private void SetControlState()
        {
            bool advanced = false;
            double advancedOpacity = 1;

            var ComputerPlayer = ComputerPlayerCheckBox.IsChecked == true;
            if (ComputerPlayer) {
                ComputerMoveFirstCheckBox.IsEnabled = true;
                RandomiseFirstMoveCheckBox.IsEnabled = true;
                LevelAutoCheckBox.IsEnabled = true;
                ComputerSkillLevelCombo.IsEnabled = true;
                ComputerSkillLevelTitleText.Opacity = 1.0;
                ComputerAdvancedSettingsCheckBox.IsEnabled = true;
                advanced = ComputerAdvancedSettingsCheckBox.IsEnabled && (ComputerAdvancedSettingsCheckBox.IsChecked == true);
                advancedOpacity = advanced ? 1 : 0.5;

            }
            else
            {
                ComputerMoveFirstCheckBox.IsEnabled = false;
                RandomiseFirstMoveCheckBox.IsEnabled = false;
                LevelAutoCheckBox.IsEnabled = false;
                ComputerSkillLevelCombo.IsEnabled = false;
                ComputerSkillLevelTitleText.Opacity = 0.5;
                ComputerAdvancedSettingsCheckBox.IsEnabled = false;
                advanced = false;
                advancedOpacity = 0.5;
            }

            // Advanced Settings
            DepthLimitSlider.IsEnabled = advanced;
            DepthLimitText.Opacity = advancedOpacity;
            DepthLimitValueText.Opacity = advancedOpacity;            
            MoveDurationLimitTextBox.IsEnabled = advanced;
            MoveDurationLimitText.Opacity = advancedOpacity;
            MoveDurationLimitErrorText.Opacity = advancedOpacity;
            ThreadsSlider.IsEnabled = advanced;
            ThreadsText.Opacity = advancedOpacity;
            ThreadsValueText.Opacity = advancedOpacity;
        }

        
        // Gets the value as string, or return off if zero
        private string getValueZeroOff(double pValue)
        {
            return pValue > 0 ? pValue.ToString("N0") : "off";
        }
               

        // Before changing event, filter out invalid characters
        private void MoveDurationLimitTextBox_OnBeforeTextChanging(TextBox sender, TextBoxBeforeTextChangingEventArgs args)
        {
            args.Cancel = System.Text.RegularExpressions.Regex.IsMatch(args.NewText, "[^0-9]");

        }

        // Display validation error
        private void MoveDurationLimitTextBox_TextChanged(object sender, TextChangedEventArgs e)
        {
            var tBox = sender as TextBox;
            Int32.TryParse(tBox.Text, out int MoveDurationLimit);

            if (MoveDurationLimit < 0 || MoveDurationLimit > 600000)
            {
                MoveDurationLimitErrorText.Text = "Valid values are 0 to " + 600000.ToString("N0") + ".";
                MoveDurationLimitErrorText.Visibility = Visibility.Visible;
            }
            else
            {
                MoveDurationLimitErrorText.Text = "";
                MoveDurationLimitErrorText.Visibility = Visibility.Collapsed;
            }
        }

    }
}

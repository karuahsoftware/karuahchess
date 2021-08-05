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
        private List<String> strengthLabelList = Constants.strengthLabelList;


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

            // Set maximum threads selectable to be less than the logical processors on the machine
            // Set to one less than the maximum so some processing power is left over for the application
            ThreadsSlider.Maximum = Environment.ProcessorCount > 1 ? Environment.ProcessorCount - 1 : 1;
            
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
            ComputerStrengthCombo.SelectedIndex = Constants.eloList.IndexOf(_boardVM.limitEngineStrengthELO);
            LevelAutoCheckBox.IsChecked = _boardVM.LevelAutoEnabled;
            ComputerAdvancedSettingsCheckBox.IsChecked = _boardVM.LimitAdvancedEnabled;
            DepthLimitSlider.Value = _boardVM.limitDepth;
            NodeLimitSlider.Value = _boardVM.limitNodes;
            MoveDurationLimitSlider.Value = _boardVM.limitMoveDuration;
            ThreadsSlider.Value = _boardVM.limitThreads;

            SetControlState();

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
                this.SetValue(Canvas.LeftProperty, 10);
                this.SetValue(Canvas.TopProperty, 10);
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
            ComputerStrengthCombo.SelectedIndex = Constants.eloList.IndexOf(new ParamLimitEngineStrengthELO().eloRating);
            LevelAutoCheckBox.IsChecked = new ParamLevelAuto().Enabled;
            ComputerAdvancedSettingsCheckBox.IsChecked = new ParamLimitAdvanced().Enabled;
            DepthLimitSlider.Value = new ParamLimitDepth().depth;
            NodeLimitSlider.Value = new ParamLimitNodes().nodes;
            MoveDurationLimitSlider.Value = new ParamLimitMoveDuration().moveDurationMS;
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
            _boardVM.stopMoveJob();
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
            _boardVM.LevelAutoEnabled = LevelAutoCheckBox.IsChecked == true;
            int eloRating = Constants.eloList[ComputerStrengthCombo.SelectedIndex];
            _boardVM.limitEngineStrengthELO = eloRating;
            
            _boardVM.LimitAdvancedEnabled = ComputerAdvancedSettingsCheckBox.IsChecked == true;
            _boardVM.limitDepth = (int)DepthLimitSlider.Value;
            _boardVM.limitNodes = (int)NodeLimitSlider.Value;
            _boardVM.limitMoveDuration = (int)MoveDurationLimitSlider.Value;
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
                LevelAutoCheckBox.IsEnabled = true;
                ComputerStrengthCombo.IsEnabled = true;
                ComputerStrengthTitleText.Opacity = 1.0;
                ComputerAdvancedSettingsCheckBox.IsEnabled = true;
                advanced = ComputerAdvancedSettingsCheckBox.IsEnabled && (ComputerAdvancedSettingsCheckBox.IsChecked == true);
                advancedOpacity = advanced ? 1 : 0.5;

            }
            else
            {
                ComputerMoveFirstCheckBox.IsEnabled = false;
                LevelAutoCheckBox.IsEnabled = false;
                ComputerStrengthCombo.IsEnabled = false;
                ComputerStrengthTitleText.Opacity = 0.5;
                ComputerAdvancedSettingsCheckBox.IsEnabled = false;
                advanced = false;
                advancedOpacity = 0.5;
            }

            // Advanced Settings
            DepthLimitSlider.IsEnabled = advanced;
            DepthLimitText.Opacity = advancedOpacity;
            DepthLimitValueText.Opacity = advancedOpacity;
            NodeLimitSlider.IsEnabled = advanced;
            NodeLimitText.Opacity = advancedOpacity;
            NodeLimitValueText.Opacity = advancedOpacity;
            MoveDurationLimitSlider.IsEnabled = advanced;
            MoveDurationLimitText.Opacity = advancedOpacity;
            MoveDurationLimitValueText.Opacity = advancedOpacity;
            ThreadsSlider.IsEnabled = advanced;
            ThreadsText.Opacity = advancedOpacity;
            ThreadsValueText.Opacity = advancedOpacity;
        }

        // Gets the duration as a string with units
        private string getDurationStr(double pDurationMS)
        {
            return pDurationMS > 0 ? pDurationMS.ToString("N0") + " ms" : "off";
        }


        // Gets the value as string, or return off if zero
        private string getValueZeroOff(double pValue)
        {
            return pValue > 0 ? pValue.ToString("N0") : "off";
        }
    }
}

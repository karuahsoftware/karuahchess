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
using System.Collections.Generic;
using System.Threading;
using KaruahChess.Common;
using KaruahChess.Model.ParameterObjects;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media.Imaging;


namespace KaruahChess.CustomControl
{
    public sealed partial class EngineSettingsDialog : Page
    {
        
        ViewModel.BoardViewModel _boardVM;
        readonly List<Strength> strengthList = Constants.strengthList;
        readonly string moveDurationLimitToolTip = "Valid values are 1 to " + 600000.ToString("N0") + ". Leave blank for no limit.";
        bool dataSaved = false;
        bool computerMoveFirst;

        /// <summary>
        /// Constructor
        /// </summary>
        public EngineSettingsDialog(ViewModel.BoardViewModel pBoardVM)
        {
            this.InitializeComponent();
            _boardVM = pBoardVM;

            
            // Set maximum threads selectable to be equal to the logical processors on the machine
            // Default is to set to one less than the maximum so some processing power is left over for the application
            ThreadsSlider.Maximum = Environment.ProcessorCount > 1 ? Environment.ProcessorCount : 1;

            // Set initial values
            ComputerPlayerCheckBox.IsChecked = _boardVM.ComputerPlayerEnabled;
            SetComputerColour(_boardVM.ComputerMoveFirstEnabled);            
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
        }


        public ContentDialog CreateDialog()
        {
            var dialog = new ContentDialog
            {
                Content = this,
                PrimaryButtonText = "Close",
                SecondaryButtonText = "Stop search",
                CloseButtonText = "Set to default"

            };

            dialog.PrimaryButtonClick += Close_Click;
            dialog.SecondaryButtonClick += StopSearch_Click;
            dialog.CloseButtonClick += ResetToDefault_Click;
            dialog.Closing += Dialog_Closing;
            return dialog;
        }
        
        /// <summary>
        /// Saves form values
        /// </summary>
        private void Save()
        {
            _boardVM.ComputerPlayerEnabled = ComputerPlayerCheckBox.IsChecked == true;
            _boardVM.ComputerMoveFirstEnabled = computerMoveFirst;
            _boardVM.RandomiseFirstMoveEnabled = RandomiseFirstMoveCheckBox.IsChecked == true;
            _boardVM.LevelAutoEnabled = LevelAutoCheckBox.IsChecked == true;
            _boardVM.LimitSkillLevel = ComputerSkillLevelCombo.SelectedIndex;

            _boardVM.LimitAdvancedEnabled = ComputerAdvancedSettingsCheckBox.IsChecked == true;
            _boardVM.limitDepth = (int)DepthLimitSlider.Value;

            Int32.TryParse(MoveDurationLimitTextBox.Text, out int limitMoveDuration);
            if (limitMoveDuration < 0 || limitMoveDuration > 600000) limitMoveDuration = 0;
            _boardVM.limitMoveDuration = limitMoveDuration;

            _boardVM.limitThreads = (int)ThreadsSlider.Value;

            dataSaved = true;
        }
        /// <summary>
        /// Dialog closing event
        /// </summary>
        private void Dialog_Closing(ContentDialog SenderDialog, ContentDialogClosingEventArgs args)
        {
            // Cancels the closing of the dialog if the stop search
            // or reset to default buttons are pressed.
            // Only the close button will save and close the dialog
            if (!dataSaved)
            {
                args.Cancel = true;
            }
        }

        /// <summary>
        /// Save on close
        /// </summary>
        private void Close_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            Save();

            SenderDialog.PrimaryButtonClick -= Close_Click;
            SenderDialog.SecondaryButtonClick -= StopSearch_Click;
            SenderDialog.CloseButtonClick -= ResetToDefault_Click;
            SenderDialog.Closing -= Dialog_Closing;
        }

        /// <summary>
        /// Reset to default
        /// </summary>
        private void ResetToDefault_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            ComputerPlayerCheckBox.IsChecked = new ParamComputerPlayer().Enabled;            
            SetComputerColour(new ParamComputerMoveFirst().Enabled);
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
        private void StopSearch_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
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
        /// Enables and disables controls depending on options set
        /// </summary>
        private void SetControlState()
        {
            bool advanced = false;
            double advancedOpacity = 1;

            var ComputerPlayer = ComputerPlayerCheckBox.IsChecked == true;
            if (ComputerPlayer)
            {
                btnComputerColour.IsEnabled = true;
                ComputerColourImage.Opacity = 1.0;
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
                btnComputerColour.IsEnabled = false;
                ComputerColourImage.Opacity = 0.5;
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

        // Computer colour click handler
        private void btnComputerColour_Click(object sender, RoutedEventArgs e)
        {
            SetComputerColour(!computerMoveFirst);
        }

        // Set computer colour
        private void SetComputerColour(bool pComputerMoveFirst)
        {
            computerMoveFirst = pComputerMoveFirst;
            if (pComputerMoveFirst)
            {
                ComputerColourImage.Source = new BitmapImage(new Uri("ms-appx:///Pieces/Images/WhitePawnLarge.png"));
                ComputerColourImage.Width = 60;
                ComputerColourImage.Height = 60;
            }
            else
            {
                ComputerColourImage.Source = new BitmapImage(new Uri("ms-appx:///Pieces/Images/BlackPawnLarge.png"));
                ComputerColourImage.Width = 60;
                ComputerColourImage.Height = 60;
            }
        }

    }
}

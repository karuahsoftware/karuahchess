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
using KaruahChess.Common;
using KaruahChess.Model.ParameterObjects;
using Windows.Foundation;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;


namespace KaruahChess.CustomControl
{
    public sealed partial class BoardSettingsDialog : Page
    {
        
        ViewModel.BoardViewModel _boardVM;

        readonly List<ColourARGB> darkSquareColourList = Constants.darkSquareColourList;

        RotateTransform orientationImageTransform = new RotateTransform();

        /// <summary>
        /// Constructor
        /// </summary>
        public BoardSettingsDialog(ViewModel.BoardViewModel pBoardVM)
        {
            this.InitializeComponent();
            _boardVM = pBoardVM;

            // Set initial values
            DarkSquareCombo.SelectedIndex = Constants.darkSquareColourList.IndexOf(_boardVM.ColourDarkSquaresARGB);
            orientationBorder.Background = new SolidColorBrush(_boardVM.ColourDarkSquaresARGB.GetColour());

            orientationImage.RenderTransformOrigin = new Point(0.5, 0.5);
            orientationImage.RenderTransform = orientationImageTransform;
            orientationImageTransform.Angle = -_boardVM.RotateBoardValue;

        }


        public ContentDialog CreateDialog()
        {
            var dialog = new ContentDialog
            {
                Content = this,
                PrimaryButtonText = "Close"
            };

            dialog.PrimaryButtonClick += Close_Click;

            return dialog;
        }
        
        /// <summary>
        /// Saves form values
        /// </summary>
        private void Save()
        {
            ColourARGB darkSquareColour = DarkSquareCombo.SelectedIndex > -1 ? Constants.darkSquareColourList[DarkSquareCombo.SelectedIndex] : new ParamColourDarkSquares().ARGB();
            _boardVM.ColourDarkSquaresARGB = darkSquareColour;

            // Refresh colour
            _boardVM.ApplyBoardColour();

            int newRotate = -(int)orientationImageTransform.Angle;
            _boardVM.RotateBoardValue = newRotate;

            if (_boardVM.coordinatesControl != null)
            {
                _boardVM.coordinatesControl.SetCoordLabels(newRotate);
            }
        }

        /// <summary>
        /// Save on close
        /// </summary>
        private void Close_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            Save();

            SenderDialog.PrimaryButtonClick -= Close_Click;
        }

        /// <summary>
        /// Rotation click event
        /// </summary>        
        private void btnRotate_Click(object sender, RoutedEventArgs e)
        {
            double currentRotate = orientationImageTransform.Angle;
            double newRotate = (currentRotate + 90);

            if (newRotate > 270 || newRotate < -270)
            {
                newRotate = 0;
            }

            orientationImageTransform.Angle = newRotate;
        }

        /// <summary>
        /// Adjust the background colour of preview
        /// </summary> 
        private void DarkSquareCombo_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            // Adjust the background colour of the board orientation preview when the colour selection changes
            ColourARGB darkSquareColour = DarkSquareCombo.SelectedIndex > -1 ? Constants.darkSquareColourList[DarkSquareCombo.SelectedIndex] : new ParamColourDarkSquares().ARGB();
            orientationBorder.Background = new SolidColorBrush(darkSquareColour.GetColour());
        }

    }
}

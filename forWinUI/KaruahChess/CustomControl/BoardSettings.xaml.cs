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
using KaruahChess.Common;
using System.Collections.Generic;
using KaruahChess.Model.ParameterObjects;
using Windows.Foundation;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace KaruahChess.CustomControl
{
    public sealed partial class BoardSettings : UserControl
    {
        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }

        ViewModel.BoardViewModel _boardVM;

        readonly List<ColourARGB> darkSquareColourList = Constants.darkSquareColourList;

        RotateTransform orientationImageTransform = new RotateTransform();

        /// <summary>
        /// Constructor
        /// </summary>
        public BoardSettings()
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
            DarkSquareCombo.SelectedIndex = Constants.darkSquareColourList.IndexOf(_boardVM.ColourDarkSquaresARGB);
            orientationBorder.Background = new SolidColorBrush(_boardVM.ColourDarkSquaresARGB.GetColour());
            
            orientationImage.RenderTransformOrigin = new Point(0.5, 0.5);
            orientationImage.RenderTransform = orientationImageTransform;
            orientationImageTransform.Angle = -_boardVM.RotateBoardValue;

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
            save();
            PagePopup.IsOpen = false;
                        
        }


        /// <summary>
        /// Saves form values
        /// </summary>
        private void save()
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

        private void DarkSquareCombo_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            // Adjust the background colour of the board orientation preview when the colour selection changes
            ColourARGB darkSquareColour = DarkSquareCombo.SelectedIndex > -1 ? Constants.darkSquareColourList[DarkSquareCombo.SelectedIndex] : new ParamColourDarkSquares().ARGB();
            orientationBorder.Background = new SolidColorBrush(darkSquareColour.GetColour());
        }
    }
}

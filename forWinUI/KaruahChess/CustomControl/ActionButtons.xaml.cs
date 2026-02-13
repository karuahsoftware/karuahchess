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


using System;
using System.Collections.Generic;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace KaruahChess.CustomControl
{

    public sealed partial class ActionButtons : UserControl
    {
        // variables
        ViewModel.BoardViewModel boardVM;
        
              
        int CurrentPage = 0;
        int nextPageOffset = 0;
        const int MainPageIndexA = 0;
        const int MainPageIndexB = 1;
        const int MainPageIndexC = 2;        
        const int VoicePageIndex = 3;
        const int EditPageIndex = 4;
        List<int> PossiblePages = [];



        public ActionButtons()
        {
            this.InitializeComponent();

            setCurrentPage();
            setButtonVisibility();

        }


        /// <summary>
        /// Sets the board view model
        /// </summary>        
        public void SetBoardVM(ViewModel.BoardViewModel pBoardVM)
        {
            boardVM = pBoardVM;
        }

        // Button orientation
        public Orientation ActionButtonsOrientation
        {
            get { return (Orientation)GetValue(ActionButtonsOrientationProperty); }
            set { SetValue(ActionButtonsOrientationProperty, value); }
        }

        // Using a DependencyProperty as the backing store for the property.  
        public static readonly DependencyProperty ActionButtonsOrientationProperty =
            DependencyProperty.Register("ActionButtonsOrientation", typeof(Orientation), typeof(ActionButtons), new PropertyMetadata(Orientation.Horizontal));


        // Edit buttons enabled
        public bool EditEnabled
        {
            get { return (bool)GetValue(EditEnabledProperty); }
            set { 
                SetValue(EditEnabledProperty, value);

                if (value)
                {
                    CurrentPage = EditPageIndex;
                }
                setCurrentPage();
                setButtonVisibility();
            }
        }

        // Using a DependencyProperty as the backing store for the property.  
        public static readonly DependencyProperty EditEnabledProperty =
            DependencyProperty.Register("EditEnabled", typeof(bool), typeof(ActionButtons), new PropertyMetadata(false));


        // Voice buttons enabled
        public bool VoiceEnabled
        {
            get { return (bool)GetValue(VoiceEnabledProperty); }
            set { 
                SetValue(VoiceEnabledProperty, value);
                setCurrentPage();
                setButtonVisibility();
            }
        }

        // Using a DependencyProperty as the backing store for the property.  
        public static readonly DependencyProperty VoiceEnabledProperty =
            DependencyProperty.Register("VoiceEnabled", typeof(bool), typeof(ActionButtons), new PropertyMetadata(false));


        // Voice buttons enabled
        public bool HintEnabled
        {
            get { return (bool)GetValue(HintEnabledProperty); }
            set
            {
                SetValue(HintEnabledProperty, value);
                setCurrentPage();
                setButtonVisibility();
            }
        }
        
        // Using a DependencyProperty as the backing store for the property.  
        public static readonly DependencyProperty HintEnabledProperty =
            DependencyProperty.Register("HintEnabled", typeof(bool), typeof(ActionButtons), new PropertyMetadata(false));



        // New game button
        private void btnNewGame_Click(object sender, RoutedEventArgs e)
        {
            boardVM.btnNewGame_Click(sender, e);
        }

        // Last move button
        private void btnLastMove_Click(object sender, RoutedEventArgs e)
        {
            boardVM.btnLastMove_Click(sender, e);
        }

        // Hint button
        private void btnHint_Click(object sender, RoutedEventArgs e)
        {
            boardVM.btnHint_Click(sender, e);
        }

        // Add piece button
        private void btnAddPiecesDialog_Click(object sender, RoutedEventArgs e)
        {
            boardVM.btnAddPiecesDialog_Click(sender, e);
        }

        // Erase selection button
        private void btnEditEraseSelection_Click(object sender, RoutedEventArgs e)
        {
            boardVM.btnEditEraseSelection_Click(sender, e);
        }


        // Erase selection button
        private void btnNext_Click(object sender, RoutedEventArgs e)
        {
            nextPageOffset++;
            
            setCurrentPage();
            setButtonVisibility();
        }

        // Erase selection button
        private void btnStartVoiceListen_Click(object sender, RoutedEventArgs e)
        {
            boardVM.btnStartVoiceListen_Click(sender, e);
        }

       

        // Sets the button visibility
        private void setButtonVisibility()
        {
            NewBtn.Visibility = Visibility.Collapsed;
            LastBtn.Visibility = Visibility.Collapsed;
            HintBtn.Visibility = Visibility.Collapsed;
            editAddPiecesBtn.Visibility = Visibility.Collapsed;
            editEraseBtn.Visibility = Visibility.Collapsed;
            startListenBtn.Visibility = Visibility.Collapsed;
            nextBtn.Visibility = Visibility.Collapsed;

            
            if (CurrentPage == MainPageIndexA)
            {
                NewBtn.Visibility = Visibility.Visible;
                LastBtn.Visibility = Visibility.Visible;
                HintBtn.Visibility = Visibility.Visible;
            }
            if (CurrentPage == MainPageIndexB)
            {
                NewBtn.Visibility = Visibility.Visible;
                LastBtn.Visibility = Visibility.Visible;                
            }
            if (CurrentPage == MainPageIndexC)
            {
                HintBtn.Visibility = HintEnabled ? Visibility.Visible : Visibility.Collapsed;
            }
            else if (CurrentPage == EditPageIndex)
            {
                editAddPiecesBtn.Visibility = Visibility.Visible;
                editEraseBtn.Visibility = Visibility.Visible;
            }
            else if (CurrentPage == VoicePageIndex)
            {
                startListenBtn.Visibility = Visibility.Visible;

            }

            // Set next button
            if ((CurrentPage == MainPageIndexB || CurrentPage == MainPageIndexC || CurrentPage == VoicePageIndex) && VoiceEnabled)
            {
                nextBtn.Visibility = Visibility.Visible;
            }


        }


        // Sets the current page
        private void setCurrentPage()
        {
            PossiblePages.Clear();

            if (EditEnabled)
            {
                PossiblePages.Add(EditPageIndex);                
            }
            else if (VoiceEnabled)
            {
                if (HintEnabled)
                {
                    PossiblePages.Add(VoicePageIndex);
                    PossiblePages.Add(MainPageIndexB);
                    PossiblePages.Add(MainPageIndexC);
                }
                else
                {
                    PossiblePages.Add(VoicePageIndex);
                    PossiblePages.Add(MainPageIndexB);
                }
            }            
            else
            {
                if (HintEnabled)
                {
                    PossiblePages.Add(MainPageIndexA);
                }
                else
                {
                    PossiblePages.Add(MainPageIndexB);
                }
                
            }

            if (nextPageOffset >= PossiblePages.Count) {
                nextPageOffset = 0;
            }

            if (PossiblePages.Count > 0)
            {
                CurrentPage = PossiblePages[0 + nextPageOffset];
            }
            
        }

    }
}

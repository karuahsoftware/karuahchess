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
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;

namespace KaruahChess.CustomControl
{
    public sealed partial class MoveNavigator : UserControl
    {
                
        /// <summary>
        /// A class containing styling info for the control
        /// </summary>

        private ViewModel.BoardViewModel _boardVM;
        

        /// <summary>
        /// Constructor
        /// </summary>
        public MoveNavigator()
        {

            this.InitializeComponent();

            
            

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
        /// Load the navigation items
        /// </summary>
        /// <returns></returns>
        public void Load(List<int> pNavList, int pSelectedId)
        {
            if (this.Visibility == Visibility.Collapsed) return;

            navigatorStack.Children.Clear();
           
            for (int index = 0;  index < pNavList.Count; index++) {
                var navButton = new NavToggleButton {
                    Width = 32,
                    Height = 32,
                    Padding = new Thickness(0),
                    Margin = new Thickness(0, 0, 1, 0),
                    Content = pNavList[index].ToString(),
                    Tag = pNavList[index],
                    BorderThickness = new Thickness(0)                    
                    
                };
                
                // Add button to stackpanel
                navButton.Tapped += NavigatorStack_ItemClick;
                navigatorStack.Children.Add(navButton);
                
            }

            SetSelected(pSelectedId);

            
            

        }

        /// <summary>
        /// Sets the selected button
        /// </summary>
        /// <param name="pSelectedId"></param>
        public void SetSelected(int pSelectedId)
        {
            if (this.Visibility == Visibility.Collapsed) return;

            int buttonSelectedId = -1;
            int buttonId = -1;
            for (int index = 0; index < navigatorStack.Children.Count; index++)
            {
                var navButton = (NavToggleButton)navigatorStack.Children[index];

                // Highlight selected id
                buttonId = (int)navButton.Tag;
                if (pSelectedId == buttonId)
                {
                    buttonSelectedId = buttonId;
                    navButton.IsChecked = true;
                    navButton.Opacity = 1;
                }
                else
                {
                    navButton.IsChecked = false;
                    navButton.Opacity = 0.51;
                }
            }

            // Scroll to end of scrollviewer if last button is selected
            if (buttonId == buttonSelectedId && buttonId > -1)
            {
                var period = TimeSpan.FromMilliseconds(100);
                Windows.System.Threading.ThreadPoolTimer.CreateTimer(async (source) =>
                {
                    await Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
                    {
                        var Success = navigatorScoll.ChangeView(navigatorScoll.ScrollableWidth, null, null, true);
                    });
                }, period);
            }

        }
        
        /// <summary>
        /// Show the control
        /// </summary>
        public void Show()
        {
            this.Visibility = Visibility.Visible;
        }

        /// <summary>
        /// Hide the control
        /// </summary>
        public void Hide()
        {            
            this.Visibility = Visibility.Collapsed;
        }

        

        /// <summary>
        /// Navigate to selected record
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void NavigatorStack_ItemClick(object sender, RoutedEventArgs e)
        {            
            ToggleButton navButton = (ToggleButton)sender;
            int moveId = (int)(navButton).Tag;
            _boardVM.NavigateGameRecord(moveId, false);            
        }

       
        /// <summary>
        /// Navigate scroll left
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void LeftBtn_Click(object sender, RoutedEventArgs e)
        {
            double currentOffset = navigatorScoll.HorizontalOffset;
            double newOffset = currentOffset - 66;
            if (newOffset < 0) newOffset = 0;
            navigatorScoll.ChangeView(newOffset, null, null);
        }

        /// <summary>
        /// Navigate scroll right
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void RightBtn_Click(object sender, RoutedEventArgs e)
        {
            double currentOffset = navigatorScoll.HorizontalOffset;
            double newOffset = currentOffset + 66;
            navigatorScoll.ChangeView(newOffset, null, null);
        }

        private void NavGrid_PointerEntered(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            if (navigatorScoll.ScrollableWidth > 0)
            {
                LeftBtn.Opacity = 0.51;
                RightBtn.Opacity = 0.51;
            }
        }

        private void NavGrid_PointerExited(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            LeftBtn.Opacity = 0.31;
            RightBtn.Opacity = 0.31;
        }
    }
}

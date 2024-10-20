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
using Windows.Foundation;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Dispatching;

namespace KaruahChess.CustomControl
{
    public sealed partial class MoveNavigator : UserControl
    {
                
        /// <summary>
        /// A class containing styling info for the control
        /// </summary>

        private ViewModel.BoardViewModel _boardVM;
        public NavToggleButton _selectedButton;
        private DispatcherQueue mainDispatcherQueue = DispatcherQueue.GetForCurrentThread();
        
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
            if (this.Visibility == Visibility.Visible)
            {

                navigatorStack.Children.Clear();
                _selectedButton = null;

                for (int index = 0; index < pNavList.Count; index++)
                {
                    var navButton = new NavToggleButton
                    {                        
                        Content = pNavList[index].ToString(),
                        Tag = pNavList[index],
                        VerticalAlignment = VerticalAlignment.Top,
                        Style= (Style)Application.Current.Resources["ButtonNavStyle"]
                    };

                    // Add button to stackpanel
                    navButton.Click += NavigatorStack_ItemClick;
                    navigatorStack.Children.Add(navButton);

                }

                SetSelected(pSelectedId);
            }
            
            

        }

        /// <summary>
        /// Sets the selected button
        /// </summary>
        /// <param name="pSelectedId"></param>
        public void SetSelected(int pSelectedId)
        {
            if (this.Visibility == Visibility.Visible)
            {

                for (int index = 0; index < navigatorStack.Children.Count; index++)
                {
                    var navButton = (NavToggleButton)navigatorStack.Children[index];

                    // Highlight selected id
                    int buttonId = (int)navButton.Tag;
                    if (pSelectedId == buttonId)
                    {
                        navButton.IsChecked = true;
                        navButton.Opacity = 1;
                        _selectedButton = navButton;
                    }
                    else
                    {
                        navButton.IsChecked = false;
                        navButton.Opacity = 0.51;
                    }
                }
            }         

        }
        
        /// <summary>
        /// Scroll to the selected button
        /// </summary>
        public void ScrollToSelected()
        {
            // Scroll to position of button selected
            if (this.Visibility == Visibility.Visible && _selectedButton != null)
            {
                TimeSpan period = TimeSpan.FromMilliseconds(100);
                Windows.System.Threading.ThreadPoolTimer.CreateTimer(async (source) =>
                {
                    mainDispatcherQueue.TryEnqueue (() =>
                    { 
                        var transform = _selectedButton.TransformToVisual(navigatorStack);
                        var position = transform.TransformPoint(new Point(0, 0));
                        var Success = navigatorScoll.ChangeView(position.X, null, null, true);
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
            int moveId = (int)navButton.Tag;
            
            // Set animation to true if only navigating to the nearest move. If skipping moves then don't animate.            
            int navDistance = GetNavDistance();            
            if (navDistance == 1) _boardVM.NavigateGameRecord(moveId, true, false, false);
            else _boardVM.NavigateGameRecord(moveId, false, false, false);


        }

       
        /// <summary>
        /// Navigate scroll left
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void LeftScrollBtn_Click(object sender, RoutedEventArgs e)
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
        private void RightScrollBtn_Click(object sender, RoutedEventArgs e)
        {
            double currentOffset = navigatorScoll.HorizontalOffset;
            double newOffset = currentOffset + 66;
            navigatorScoll.ChangeView(newOffset, null, null);
        }

        private void NavGrid_PointerEntered(object sender, Microsoft.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            if (navigatorScoll.ScrollableWidth > 0)
            {
                LeftScrollBtn.Opacity = 0.51;
                RightScrollBtn.Opacity = 0.51;
            }
        }

        private void NavGrid_PointerExited(object sender, Microsoft.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            LeftScrollBtn.Opacity = 0.31;
            RightScrollBtn.Opacity = 0.31;
        }


        /// <summary>
        /// Back button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void BackBtn_Click(object sender, RoutedEventArgs e)
        {
            if (this.Visibility == Visibility.Collapsed) return;

            int selectedIndex = GetFirstSelectedIndex();
            if (selectedIndex > -1)
            {                
                int previousIndex = selectedIndex - 1;
                if (previousIndex > -1)
                {
                    var navButton = (NavToggleButton)navigatorStack.Children[previousIndex];
                    int buttonId = (int)navButton.Tag;
                    _boardVM.NavigateGameRecord(buttonId, true, false, true);
                  
                    
                }
            }
        }

        /// <summary>
        /// Foward button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ForwardBtn_Click(object sender, RoutedEventArgs e)
        {
            if (this.Visibility == Visibility.Collapsed) return;

            int selectedIndex = GetFirstSelectedIndex();
            if (selectedIndex > -1)
            {
                int childrenCount = navigatorStack.Children.Count;
                int nextIndex = selectedIndex + 1;
                if (nextIndex < childrenCount)
                {
                    var navButton = (NavToggleButton)navigatorStack.Children[nextIndex];
                    int buttonId = (int)navButton.Tag;
                    _boardVM.NavigateGameRecord(buttonId, true, false, true);                    
                }
            }

        }


        /// <summary>
        /// Gets the currently selected navigation index
        /// </summary>
        /// <returns></returns>
        private int GetFirstSelectedIndex()
        {
            int selectedIndex = -1;

            int childrenCount = navigatorStack.Children.Count;
            for (int index = 0; index < childrenCount; index++)
            {
                var navButton = (NavToggleButton)navigatorStack.Children[index];
                if (navButton.IsChecked == true)
                {
                    selectedIndex = index;
                    break;
                }
            }

            return selectedIndex;
        }


        /// <summary>
        /// Determine the navigation distance.
        /// </summary>
        /// <returns></returns>
        private int GetNavDistance()
        {
            int childrenCount = navigatorStack.Children.Count;
            int firstSelected = -1;
            int lastSelected = -1;

            for (int index = 0; index<childrenCount; index++)
            {
                var navButton = (NavToggleButton)navigatorStack.Children[index];
                if (navButton.IsChecked == true)
                {
                    if (firstSelected == -1) firstSelected = index;
                    else lastSelected = index;
                }
           }


            if (firstSelected > -1 && lastSelected > -1) return lastSelected - firstSelected;
            else return 0;
        }

    }
}

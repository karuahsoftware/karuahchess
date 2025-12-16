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
using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Input;
using System;
using System.Collections.Generic;
using Windows.Foundation;
using static KaruahChess.ViewModel.BoardViewModel;


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

            // Ensure the control can receive keyboard focus and capture arrow keys from children
            this.IsTabStop = true;
            this.AddHandler(UIElement.KeyDownEvent, new KeyEventHandler(MoveNavigator_KeyDown), handledEventsToo: true);
                       
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
        /// Synchronise the navigation buttons with the current records.
        /// Layout rules:
        /// - For a white move of move N: insert a TextBlock "N." BEFORE the white move button.
        /// - If there is NO white move for move N but there is a black move, insert "N..." BEFORE that black move button.
        /// - Handles edge cases where multiple consecutive moves of the same colour appear due to missing records.
        /// Start position remains first (Id == 1).
        /// </summary>
        public void SyncNavButtons()
        {
            if (this.Visibility != Visibility.Visible) return;

            // Record currently selected record id
            int? selectedRecId = null;
            for (int i = 0; i < navigatorStack.Children.Count; i++)
            {
                if (navigatorStack.Children[i] is NavToggleButton btn && btn.IsChecked == true)
                {
                    selectedRecId = (int)btn.Tag;
                    break;
                }
            }

            List<int> navList = GameRecordDataService.instance.GetAllRecordIDList();
            if (navList.Count == 0)
            {
                navigatorStack.Children.Clear();
                return;
            }

            navigatorStack.Children.Clear();

            // Always first: Start position (Id == 1)
            if (navList[0] == 1)
            {
                var startBtn = new NavToggleButton
                {
                    Tag = 1,
                    Content = GetNavLabel(1),
                    VerticalAlignment = VerticalAlignment.Top,
                    Style = (Style)Application.Current.Resources["ButtonNavStyle"]
                };
                startBtn.Click += NavigatorStack_ItemClick;
                navigatorStack.Children.Add(startBtn);
            }

            
            // Iterate remaining records (plys). We interpret:
            // recId -> ply = recId - 2 (0-based half-move index)
            // isWhite = ply % 2 == 0 (unless the record is missing which causes "skips")
            // Missing white for move N => black ply exists (ply odd) but white ply ((N-1)*2) absent.
            for (int listIndex = 1; listIndex < navList.Count; listIndex++)
            {
                int recId = navList[listIndex];
                if (recId <= 1) continue; // safety

                int ply = recId - 2;
                if (ply < 0) continue;

                bool isResignedOrTimeExpired = GameRecordDataService.instance.GetStateGameStatus(recId) == (int)BoardStatusEnum.Resigned ||
                                               GameRecordDataService.instance.GetStateGameStatus(recId) == (int)BoardStatusEnum.TimeExpired;
                bool isWhitePly = GameRecordDataService.instance.GetActiveMoveColour(recId) == -1;
                int moveNumber = (ply / 2) + 1;

                if (isResignedOrTimeExpired)
                {                    
                    navigatorStack.Children.Add(CreateMoveButton(recId));
                }
                else if (isWhitePly)
                {
                    // Insert move number "N."
                    var moveNumberBlock = new TextBlock
                    {
                        Text = $"{moveNumber}.",
                        VerticalAlignment = VerticalAlignment.Center,
                        Margin = new Thickness(8, 0, 2, 0),
                        Opacity = 0.5
                    };
                    navigatorStack.Children.Add(moveNumberBlock);

                    // White move button
                    navigatorStack.Children.Add(CreateMoveButton(recId));

                    // Attempt to pair with an immediately following black move (expected ply+1)
                    if (listIndex + 1 < navList.Count)
                    {
                        int nextRecId = navList[listIndex + 1];
                        bool blackReplyExists = GameRecordDataService.instance.GetActiveMoveColour(nextRecId) == 1;                        
                        
                        if (blackReplyExists) // black reply exists
                        {
                            navigatorStack.Children.Add(CreateMoveButton(nextRecId));
                            listIndex++; // consume black
                        }
                    }
                }
                else
                {                   

                    // Insert "N..." before this black move button
                    var moveNumberBlock = new TextBlock
                    {
                        Text = $"{moveNumber}...",
                        VerticalAlignment = VerticalAlignment.Center,
                        Margin = new Thickness(8, 0, 2, 0),
                        Opacity = 0.5
                    };
                    navigatorStack.Children.Add(moveNumberBlock);
                 
                    // Add black move button
                    navigatorStack.Children.Add(CreateMoveButton(recId));
                }
            }

            // Restore selection if possible
            if (selectedRecId.HasValue)
            {
                SetSelected(selectedRecId.Value);
            }
                        
        }

        /// <summary>
        /// Returns the content for a navigation button (SAN only, or Start).
        /// Move numbers are now displayed as separate TextBlocks.
        /// </summary>
        private string GetNavLabel(int recId)
        {
            if (recId == 1) return "Start";

            var rec = GameRecordDataService.instance.Get(recId);
            string san = rec?.MoveSAN;
            return string.IsNullOrWhiteSpace(san) ? "" : san;
        }

        /// <summary>
        /// Helper to create a move button for a record id.
        /// </summary>
        private NavToggleButton CreateMoveButton(int recId)
        {            
            var btn = new NavToggleButton
            {
                Tag = recId,
                Content = GetNavLabel(recId),
                VerticalAlignment = VerticalAlignment.Top,
                Style = (Style)Application.Current.Resources["ButtonNavStyle"],
                Opacity = 0.51
            };
            btn.Click += NavigatorStack_ItemClick;
                        
            return btn;
        }

        /// <summary>
        /// Sets the selected button (updated to skip TextBlocks).
        /// </summary>
        public void SetSelected(int pSelectedId)
        {
            if (this.Visibility != Visibility.Visible) return;

            for (int index = 0; index < navigatorStack.Children.Count; index++)
            {
                if (navigatorStack.Children[index] is NavToggleButton navButton)
                {
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
                    mainDispatcherQueue.TryEnqueue(() =>
                    {
                        if (_selectedButton == null || navigatorScoll == null || navigatorStack == null) return;

                        // Position of the button relative to the stack inside the ScrollViewer
                        var transform = _selectedButton.TransformToVisual(navigatorStack);
                        var position = transform.TransformPoint(new Point(0, 0));

                        double itemLeft = position.X;
                        double itemWidth = _selectedButton.ActualWidth;
                        double itemRight = itemLeft + itemWidth;

                        double viewportLeft = navigatorScoll.HorizontalOffset;
                        double viewportWidth = navigatorScoll.ViewportWidth;
                        double viewportRight = viewportLeft + viewportWidth;

                        // Only scroll if not currently visible (fully)
                        bool isFullyVisible = itemLeft >= viewportLeft && itemRight <= viewportRight;

                        if (!isFullyVisible && viewportWidth > 0)
                        {
                            // Target offset to center the item in the viewport
                            double targetOffset = itemLeft + (itemWidth / 2.0) - (viewportWidth / 2.0);

                            // Clamp to valid scroll range
                            double minOffset = 0.0;
                            double maxOffset = Math.Max(0.0, navigatorScoll.ScrollableWidth);
                            if (double.IsNaN(targetOffset) || double.IsInfinity(targetOffset))
                            {
                                targetOffset = viewportLeft; // fail-safe
                            }
                            targetOffset = Math.Min(Math.Max(targetOffset, minOffset), maxOffset);

                            var success = navigatorScoll.ChangeView(targetOffset, navigatorScoll.VerticalOffset, null, true);
                        }

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
        /// Back button (skips non-button elements).
        /// </summary>
        private async void BackBtn_Click(object sender, RoutedEventArgs e)
        {
            if (this.Visibility == Visibility.Collapsed) return;

            int currentChildIndex = GetFirstSelectedChildIndex();
            if (currentChildIndex > -1)
            {
                for (int i = currentChildIndex - 1; i >= 0; i--)
                {
                    if (navigatorStack.Children[i] is NavToggleButton btn)
                    {
                        await _boardVM.NavigateGameRecord((int)btn.Tag, true, false, false);
                        break;
                    }
                }

                ScrollToSelected();
            }
        }

        /// <summary>
        /// Forward button (skips non-button elements).
        /// </summary>
        private async void ForwardBtn_Click(object sender, RoutedEventArgs e)
        {
            if (this.Visibility == Visibility.Collapsed) return;

            int currentChildIndex = GetFirstSelectedChildIndex();
            if (currentChildIndex > -1)
            {
                for (int i = currentChildIndex + 1; i < navigatorStack.Children.Count; i++)
                {
                    if (navigatorStack.Children[i] is NavToggleButton btn)
                    {
                        await _boardVM.NavigateGameRecord((int)btn.Tag, true, false, false);
                        break;
                    }
                }

                ScrollToSelected();
            }
        }

        /// <summary>
        /// View PGN button
        /// </summary>
        private async void ViewPGNBtn_Click(object sender, RoutedEventArgs e)
        {
            string pgn = BuildPGNFromNavigator();
            await _boardVM.ShowMoveNavigatorPGNDialog(pgn);
        }

        /// <summary>
        /// Gets the child index (in the panel) of the selected button (skips TextBlocks).
        /// </summary>
        private int GetFirstSelectedChildIndex()
        {
            for (int index = 0; index < navigatorStack.Children.Count; index++)
            {
                if (navigatorStack.Children[index] is NavToggleButton navButton && navButton.IsChecked == true)
                {
                    return index;
                }
            }
            return -1;
        }


        /// <summary>
        /// Navigation distance considering only buttons.
        /// </summary>
        private int GetNavDistance()
        {
            int first = -1;
            int last = -1;
            int buttonIndex = 0;
            for (int i = 0; i < navigatorStack.Children.Count; i++)
            {
                if (navigatorStack.Children[i] is NavToggleButton navButton)
                {
                    if (navButton.IsChecked == true)
                    {
                        if (first == -1) first = buttonIndex;
                        else last = buttonIndex;
                    }
                    buttonIndex++;
                }
            }
            if (first > -1 && last > -1) return last - first;
            return 0;
        }

        /// <summary>
        /// Move navigator key down handler (left/right arrows).
        /// </summary>        
        private void MoveNavigator_KeyDown(object sender, KeyRoutedEventArgs e)
        {
            switch (e.Key)
            {
                case Windows.System.VirtualKey.Left:
                    BackBtn_Click(this, new RoutedEventArgs());
                    e.Handled = true;
                    break;

                case Windows.System.VirtualKey.Right:
                    ForwardBtn_Click(this, new RoutedEventArgs());
                    e.Handled = true;
                    break;
            }
        }

        /// <summary>
        /// Navigator ScrollViewer key down handler (left/right arrows).
        /// </summary>                
        private void NavigatorScroll_PreviewKeyDown(object sender, KeyRoutedEventArgs e)
        {
            if (e.Key == Windows.System.VirtualKey.Left || e.Key == Windows.System.VirtualKey.Right)
            {
                // Prevent the ScrollViewer from processing arrow keys (no scroll)
                e.Handled = true;
            }
        }


        /// <summary>
        /// Builds a PGN string from the current contents of navigatorStack.
        /// Uses TextBlocks for move numbers ("N." / "N...") and NavToggleButtons' SAN labels.
        /// Skips the "Start" button.
        /// </summary>
        private string BuildPGNFromNavigator()
        {
            if (navigatorStack == null || navigatorStack.Children == null || navigatorStack.Children.Count == 0)
                return string.Empty;

            var parts = new List<string>(navigatorStack.Children.Count);

            for (int i = 0; i < navigatorStack.Children.Count; i++)
            {
                var child = navigatorStack.Children[i];

                if (child is TextBlock tb)
                {
                    var text = tb.Text?.Trim();
                    if (!string.IsNullOrEmpty(text))
                    {
                        parts.Add(text);
                    }
                }
                else if (child is NavToggleButton btn)
                {
                    // Skip the Start position entry
                    if (btn.Tag is int id && id == 1)
                        continue;

                    var san = $"{(btn.Content as string)?.Trim()} ";
                    if (!string.IsNullOrWhiteSpace(san))
                    {
                        parts.Add(san);
                    }
                }
            }

            return string.Concat(parts);
        }

    }
}

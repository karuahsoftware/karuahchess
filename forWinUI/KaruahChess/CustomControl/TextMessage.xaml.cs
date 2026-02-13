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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Media.Animation;

namespace KaruahChess.CustomControl
{
    public sealed partial class TextMessage : UserControl
    {

        private bool _fadeOutInProgress;
        private TextBlock _defaultTextBlock = new TextBlock();

        public TextMessage()
        {
            this.InitializeComponent();

            this.Visibility = Visibility.Collapsed;

            // Loads a default style template if one has not been set
            if (StyleTemplate == null)
            {
                StyleTemplate = (TextMessageStyleTemplate)TextMessageStyleDefaultResourceDictionary["TextMessageStyleTemplateDefaultObject"];
            }

        }

        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public TextMessageStyleTemplate StyleTemplate { get; set; }


        public enum TypeEnum { Info, Error, Award }
        public enum AnimationEnum { Fixed, Flash, FadeOut }



        /// <summary>
        /// Displays message and starts fade in animation
        /// </summary>        
        public void Show(String pTitle, String pMessage, TypeEnum pType, AnimationEnum pAnimation)
        {
            // Just exit immediately if no changes
            if (pTitle == TextTitleTextBlock.Text && pMessage == TextMessageTextBlock.Text) return;

            this.Visibility = Visibility.Visible;

            var storyboardFlash = TextMessageGrid.Resources["TextMessageGridFlashStoryboard"] as Storyboard;
            var storyboardFixed = TextMessageGrid.Resources["TextMessageGridFixedStoryboard"] as Storyboard;

            TextTitleTextBlock.Text = pTitle;
            if (pTitle != String.Empty)
            {
                TextTitleTextBlock.Visibility = Visibility.Visible;
            }
            else
            {
                TextTitleTextBlock.Visibility = Visibility.Collapsed;
            }

            TextMessageTextBlock.Text = pMessage;
            if (pMessage != String.Empty)
            {
                TextMessageTextBlock.Visibility = Visibility.Visible;
            }
            else
            {
                TextMessageTextBlock.Visibility = Visibility.Collapsed;
            }


            if (pType == TypeEnum.Error)
            {
                TextMessageIcon.Text = "\xE7BA";
                TextMessageIcon.Foreground = _defaultTextBlock.Foreground;
            }
            else if (pType == TypeEnum.Award)
            {
                TextMessageIcon.Text = "\xE735";
                TextMessageIcon.Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 241, 169, 22));
            }
            else
            {
                TextMessageIcon.Text = "\xE946";
                TextMessageIcon.Foreground = _defaultTextBlock.Foreground;
            }

            if (pAnimation == AnimationEnum.FadeOut)
            {
                var duration = pType == TypeEnum.Error ? new TimeSpan(0, 0, 8) : new TimeSpan(0, 0, 3);
                FadeOut(duration);
            }
            else if (pAnimation == AnimationEnum.Flash)
            {
                storyboardFlash.AutoReverse = true;
                storyboardFlash.Begin();
            }
            else
            {
                storyboardFixed.Begin();
            }


        }


        /// <summary>
        /// Do fade out
        /// </summary>
        private void FadeOut(TimeSpan pDuration)
        {
            if (!_fadeOutInProgress)
            {
                _fadeOutInProgress = true;
                var storyboardFadeOut = TextMessageGrid.Resources["TextMessageGridFadeOutStoryboard"] as Storyboard;
                FadeOutAnimation.Duration = pDuration;

                EventHandler<object> onComplete = null;
                onComplete = (s, e) => {
                    storyboardFadeOut.Completed -= onComplete;
                    Clear();
                    _fadeOutInProgress = false;
                };
                storyboardFadeOut.Completed += onComplete;
                storyboardFadeOut.Begin();
            }
        }


        /// <summary>
        /// Sets the position of the control
        /// </summary>
        /// <param name="pMaxWidth"></param>
        public void SetPosition(double pMaxWidth)
        {

            if (pMaxWidth <= 400)
            {
                this.SetValue(Canvas.LeftProperty, pMaxWidth * 0.1);
                this.SetValue(Canvas.TopProperty, pMaxWidth * 0.05);
                this.StyleTemplate.Width = pMaxWidth * 0.8;
                this.StyleTemplate.MaxHeight = pMaxWidth * 0.6;
            }
            else
            {
                this.SetValue(Canvas.LeftProperty, pMaxWidth * 0.2);
                this.SetValue(Canvas.TopProperty, pMaxWidth * 0.2);
                this.StyleTemplate.Width = pMaxWidth * 0.6;
                this.StyleTemplate.MaxHeight = pMaxWidth * 0.4;

            }

        }



        /// <summary>
        /// Clears message
        /// </summary>
        public void Clear()
        {
            TextMessageIcon.Text = String.Empty;
            TextMessageTextBlock.Text = String.Empty;
            TextTitleTextBlock.Text = String.Empty;

            this.Visibility = Visibility.Collapsed;
        }

        /// <summary>
        /// Event when user control is tapped
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void UserControl_Tapped(object sender, Microsoft.UI.Xaml.Input.TappedRoutedEventArgs e)
        {
            var duration = new TimeSpan(0, 0, 0, 0, 800);
            FadeOut(duration);
        }
    }
}

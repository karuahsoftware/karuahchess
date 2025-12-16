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
using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media.Animation;
using Windows.Media.SpeechRecognition;

namespace KaruahChess.CustomControl
{
    public sealed partial class VoiceListenIndicator : UserControl
    {
        private DispatcherQueue mainDispatcherQueue = DispatcherQueue.GetForCurrentThread();
        private bool listening = false;
        private string speechText = "";
                        
        /// <summary>
        /// Constructor
        /// </summary>
        public VoiceListenIndicator()
        {
            this.InitializeComponent();
            SetVoiceIndicatorDisplay();
        }

        // Show indicator        
        public SpeechRecognizerState IndicatorState
        {
            get { return (SpeechRecognizerState)GetValue(IndicatorStateProperty); }
            set
            {
                SetValue(IndicatorStateProperty, value);
                if (value == SpeechRecognizerState.Capturing || value == SpeechRecognizerState.SoundEnded || value == SpeechRecognizerState.SoundStarted || value == SpeechRecognizerState.SpeechDetected)
                {
                    if (!listening)
                    {
                        listening = true;
                        SetVoiceIndicatorDisplay();
                    }
                }
                else
                {
                    if (listening)
                    {
                        listening = false;
                        SetVoiceIndicatorDisplay();
                    }
                }
                
            }
        }

        // Using a DependencyProperty as the backing store for the property.  
        public static readonly DependencyProperty IndicatorStateProperty =
            DependencyProperty.Register("IndicatorState", typeof(SpeechRecognizerState), typeof(VoiceListenIndicator), new PropertyMetadata(SpeechRecognizerState.Idle));


        // Show spoken text       
        public string IndicatorSpokenText
        {
            get { return (string)GetValue(IndicatorSpokenTextProperty); }
            set
            {
                SetValue(IndicatorSpokenTextProperty, value);
                if (speechText != value)
                {
                    speechText = value;
                    SetVoiceIndicatorDisplay();
                }

            }
        }

        // Using a DependencyProperty as the backing store for the property.  
        public static readonly DependencyProperty IndicatorSpokenTextProperty =
            DependencyProperty.Register("IndicatorSpokenText", typeof(string), typeof(VoiceListenIndicator), new PropertyMetadata(""));

        /// <summary>
        /// Sets the voice indicator display
        /// </summary>        
        private void SetVoiceIndicatorDisplay()
        {
            if (!string.IsNullOrEmpty(speechText))
            {
                VoiceIndicatorText.Text = speechText;
                var storyboard = VoiceIndicatorText.Resources["VoiceIndicatorFlashStoryboard"] as Storyboard;
                storyboard.Stop();
                VoiceIndicatorText.Visibility = Visibility.Visible;
            }
            else if (listening)
            {
                VoiceIndicatorText.Text = "Listening";
                var storyboard = VoiceIndicatorText.Resources["VoiceIndicatorFlashStoryboard"] as Storyboard;
                storyboard.Begin();
                VoiceIndicatorText.Visibility = Visibility.Visible;
            }
            else
            {
                VoiceIndicatorText.Text = "";
                var storyboard = VoiceIndicatorText.Resources["VoiceIndicatorFlashStoryboard"] as Storyboard;
                storyboard.Stop();
                VoiceIndicatorText.Visibility = Visibility.Collapsed;
            }

        }

        

    }
}

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
using KaruahChessEngine;
using Microsoft.UI.Dispatching;
using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Windows.Globalization;
using Windows.Media.Capture;
using Windows.Media.SpeechRecognition;
using Windows.Storage;


namespace KaruahChess.Voice
{
    public class VoiceRecognition
    {
        public SpeechRecognizer SpeechRecogniserObj{get; private set;}                
        private bool Listening { get; set; }
        private Func<List<int>, String, Task> MoveFunctionA { get; set; }
        private Func<List<string>, String, Task> MoveFunctionB { get; set; }
        private Action<SpeechRecognizerState> VoiceIndicatorStateFunction { get; set; }
        private Action<string> VoiceIndicatorSpokenTextFunction { get; set; }
        private Func<Task> StartMoveVoiceAction { get; set; }
        public static HashSet<String> SupportedLanguages { get; private set; } = new HashSet<string> { "en-AU", "en-CA", "en-GB", "en-IN", "en-NZ", "en-US" };
        public int Ignore { get; set; }
               
        
        private DispatcherQueue mainDispatcherQueue = DispatcherQueue.GetForCurrentThread();
        
        private List<int> confirmMoveset = null;        
        private List<string> confirmMoveCommandB = null;
        private string confirmText = "";
        

        /// <summary>
        /// Check Microphone permissions
        /// </summary>
        /// <returns></returns>
        public static async Task<bool> CheckMicrophonePermission()
        {
           
            try { 
                MediaCaptureInitializationSettings settings = new MediaCaptureInitializationSettings();
                settings.StreamingCaptureMode = StreamingCaptureMode.Audio;
                settings.MediaCategory = MediaCategory.Speech;
                MediaCapture capture = new MediaCapture();

                await capture.InitializeAsync(settings);
                                
            }
            catch (UnauthorizedAccessException)
            {
                return false;
            }

            return true;
        }

        /// <summary>
        /// Initialise recogniser
        /// </summary>
        /// <param name="pLanguage"></param>
        /// <returns></returns>
        public async Task Initialise(Language pLanguage, Func<List<int>, String, Task> pMoveFunctionA, Func<List<string>, String, Task> pMoveFunctionB, Action<SpeechRecognizerState> pVoiceIndicatorStateFunction, Action<string> pVoiceIndicatorSpokenTextFunction, Func<Task> pStartMoveVoiceAction)
        {
            if (Listening)
            {
                Stop();
            }
            else
            {
                // Set the action functions
                MoveFunctionA = pMoveFunctionA;
                MoveFunctionB = pMoveFunctionB;
                VoiceIndicatorStateFunction = pVoiceIndicatorStateFunction;
                VoiceIndicatorSpokenTextFunction = pVoiceIndicatorSpokenTextFunction;
                StartMoveVoiceAction = pStartMoveVoiceAction;
                                               
                // Create an instance of SpeechRecognizer.
                SpeechRecogniserObj = new SpeechRecognizer(pLanguage);
                string languageTag = pLanguage.LanguageTag;

                // Add a grammar file constraint to the recognizer.
                var storageFile = await StorageFile.GetFileFromApplicationUriAsync(new Uri("ms-appx:///Voice/Grammar-" + languageTag + ".xml"));
                var grammarFileConstraintMoveRule = new SpeechRecognitionGrammarFileConstraint(storageFile);
                SpeechRecogniserObj.Constraints.Add(grammarFileConstraintMoveRule);
                await SpeechRecogniserObj.CompileConstraintsAsync();


                // Start recognition.
                SpeechRecogniserObj.StateChanged += SpeechRecognizerObj_StateChanged;
                SpeechRecogniserObj.ContinuousRecognitionSession.Completed += ContinuousRecognitionSession_Completed;
                SpeechRecogniserObj.ContinuousRecognitionSession.ResultGenerated += ContinuousRecognitionSession_ResultGenerated;
                

                // Not awaited so that function return is not delayed
                await SpeechRecogniserObj.ContinuousRecognitionSession.StartAsync();
                Listening = true;
            }
            
        }
                    
        /// <summary>
        /// Stop recogniser
        /// </summary>
        public void Stop()
        {
            if (SpeechRecogniserObj != null)
            {
                VoiceIndicatorStateFunction?.Invoke(SpeechRecognizerState.Idle);

                // cleanup prior to re-initializing.
                SpeechRecogniserObj.StateChanged -= SpeechRecognizerObj_StateChanged;
                SpeechRecogniserObj.ContinuousRecognitionSession.Completed -= ContinuousRecognitionSession_Completed;
                SpeechRecogniserObj.ContinuousRecognitionSession.ResultGenerated -= ContinuousRecognitionSession_ResultGenerated;

                this.SpeechRecogniserObj.Dispose();
                this.SpeechRecogniserObj = null;               
            }

            confirmMoveset = null;
            confirmMoveCommandB = null;
            confirmText = "";
            VoiceIndicatorSpokenTextFunction("");

            Listening = false;

        }


        /// <summary>
        /// Run when result received
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void ContinuousRecognitionSession_ResultGenerated(SpeechContinuousRecognitionSession sender, SpeechContinuousRecognitionResultGeneratedEventArgs args)
        {
            if ((Ignore == 0) && (args.Result.Confidence == SpeechRecognitionConfidence.Low || args.Result.Confidence == SpeechRecognitionConfidence.Medium || args.Result.Confidence == SpeechRecognitionConfidence.High))
            {
                mainDispatcherQueue.TryEnqueue(async () =>
                {
                    var text = args.Result.Text;

                    if (confirmMoveset == null && confirmMoveCommandB == null)
                    {                        

                        var moveset = GetMoveSetFromText(text);
                        if (moveset.Count > 0)
                        {
                            VoiceIndicatorSpokenTextFunction(text + ", yes to proceed?");
                            confirmMoveset = moveset;
                            confirmText = text;
                                                        
                            return;
                        }

                        var moveCommandB = GetMoveCommandBFromText(text);
                        if (moveCommandB.Count == 3)
                        {
                            VoiceIndicatorSpokenTextFunction(text + ", yes to proceed?");
                            confirmMoveCommandB = moveCommandB;
                            confirmText = text;

                            return;
                        }

                        if (text == "Start")
                        {
                            await StartMoveVoiceAction();
                            return;
                        }
                    }
                    else
                    {
                        if (text == "Confirm" || text == "Yes")
                        {
                            if (confirmMoveset != null)
                            {
                                VoiceIndicatorSpokenTextFunction("");
                                await MoveFunctionA(confirmMoveset, confirmText);                                
                                confirmMoveset = null;

                            }
                            else if (confirmMoveCommandB != null)
                            {
                                VoiceIndicatorSpokenTextFunction("");
                                await MoveFunctionB(confirmMoveCommandB, confirmText);                                
                                confirmMoveCommandB = null;
                            }
                            
                            return;
                        }
                        else if (text == "Cancel" || text == "No")
                        {
                            VoiceIndicatorSpokenTextFunction("");
                            confirmText = "";
                            confirmMoveset = null;
                            confirmMoveCommandB = null;
                        }
                    }
                });

            }
        }


        /// <summary>
        /// Run when session completed
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void ContinuousRecognitionSession_Completed(SpeechContinuousRecognitionSession sender, SpeechContinuousRecognitionCompletedEventArgs args)
        {
            Listening = false;
        }

        /// <summary>
        /// Run when state changed
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void SpeechRecognizerObj_StateChanged(SpeechRecognizer sender, SpeechRecognizerStateChangedEventArgs args)
        {

            mainDispatcherQueue.TryEnqueue(() =>
            {
                VoiceIndicatorStateFunction?.Invoke(args.State);
            });
        }

        /// <summary>
        /// Gets move set from text
        /// </summary>
        /// <param name="pSpeechText"></param>
        /// <returns></returns>
        private List<int> GetMoveSetFromText(string pSpeechText)
        {
            // Replace phonetic coordinates
            pSpeechText = pSpeechText.Replace("alfa","a").Replace("bravo","b").Replace("charlie","c")
                .Replace("delta","d").Replace("echo","e").Replace("foxtrot","f").Replace("golf","g").Replace("hotel","h");
            

            List<int> returnList = new List<int>(2); 
            Regex regex = new Regex("[a-h] [1-8]");
            MatchCollection matches = regex.Matches(pSpeechText);

            if (matches.Count == 2)
            {                
                string matchA = matches[0].Value.Replace(" ", string.Empty);
                if (helper.BoardCoordinateReverseDict.ContainsKey(matchA)) {
                    returnList.Add(helper.BoardCoordinateReverseDict[matchA]);
                }

                
                string matchB = matches[1].Value.Replace(" ", string.Empty);
                if (helper.BoardCoordinateReverseDict.ContainsKey(matchB)) {
                    returnList.Add(helper.BoardCoordinateReverseDict[matchB]);
                }
                
            }            

            return returnList;
        }


        /// <summary>
        /// Gets move set from text
        /// </summary>
        /// <param name="pSpeechText"></param>
        /// <returns></returns>
        private List<string> GetMoveCommandBFromText(string pSpeechText)
        {
            // Replace phonetic coordinates
            pSpeechText = pSpeechText.Replace("alfa", "a").Replace("bravo", "b").Replace("charlie", "c")
                .Replace("delta", "d").Replace("echo", "e").Replace("foxtrot", "f").Replace("golf", "g").Replace("hotel", "h");


            List<string> returnCommand = new List<string>(3);
            string pieceStr = string.Empty;

            List<Regex> regexList = new List<Regex>(3);
            regexList.Add(new Regex("(White|Black)", RegexOptions.IgnoreCase));
            regexList.Add(new Regex("(King|Queen|Rook|Bishop|Knight|Pawn)", RegexOptions.IgnoreCase));
            regexList.Add(new Regex("[a-h] [1-8]", RegexOptions.IgnoreCase));

            // Create move command
            int index = 0b001;
            int validation = 0;
            foreach (var regex in regexList)
            {                
                MatchCollection matches = regex.Matches(pSpeechText);
                if (matches.Count == 1)
                {
                    returnCommand.Add(matches[0].Value.Replace(" ", string.Empty));
                    validation = validation | index;
                }
                else
                {
                    returnCommand.Add(String.Empty);
                }

                index <<= 1;
            }

           // Check that command is valid (white or black is optional, but other parameters mandatory)
           if (!((validation & 0b110) == 0b110))
           {
                //if invalid command then just send and empty command back
                returnCommand.Clear();
           }

            return returnCommand;
        }

        
    }
}

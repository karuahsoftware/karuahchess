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
using System.Threading.Tasks;
using Windows.Globalization;
using Windows.Media.Capture;
using Windows.Media.SpeechRecognition;
using Windows.Storage;
using System.Text.RegularExpressions;
using Windows.UI.Core;
using KaruahChess.Common;
using KaruahChessEngine;
using Microsoft.UI.Dispatching;

namespace KaruahChess.Voice
{
    public class VoiceRecognition
    {
        public SpeechRecognizer SpeechRecogniserObj{get; private set;}
        public bool Listening { get; private set; }

        public bool Complete { get; private set; }

        public SpeechRecognizerState State { get; private set; }

        private Action<List<int>, String> MoveFunctionA { get; set; }
        private Action<bool> HelpFunction { get; set; }

        private Action<List<string>, String> MoveFunctionB { get; set; }

        private Action<List<char>, String> PieceFindFunction { get; set; }

        public static HashSet<String> SupportedLanguages { get; private set; } = new HashSet<string> { "en-AU", "en-CA", "en-GB", "en-IN", "en-NZ", "en-US" };

        public int Ignore { get; set; }


        private KaruahChessEngineClass _tempBoard = new KaruahChessEngineClass();

        private DispatcherQueue mainDispatcherQueue = DispatcherQueue.GetForCurrentThread();

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
        public async Task Initialise(Language pLanguage, Action<List<int>, String> pMoveFunctionA, Action<List<string>, String> pMoveFunctionB, Action<List<char>, String> pPieceFunction, Action<bool> pHelpFunction)
        {
            // Stop first incase object already exists
            Stop();

            // Set the action functions
            MoveFunctionA = pMoveFunctionA;
            MoveFunctionB = pMoveFunctionB;
            PieceFindFunction = pPieceFunction;
            HelpFunction = pHelpFunction;

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
            
            // Start listening
            await SpeechRecogniserObj.ContinuousRecognitionSession.StartAsync();
            Listening = true;
        }

        /// <summary>
        /// Stop recogniser
        /// </summary>
        public void Stop()
        {
            if (SpeechRecogniserObj != null)
            {
                // cleanup prior to re-initializing.
                SpeechRecogniserObj.StateChanged -= SpeechRecognizerObj_StateChanged;
                SpeechRecogniserObj.ContinuousRecognitionSession.Completed -= ContinuousRecognitionSession_Completed;
                SpeechRecogniserObj.ContinuousRecognitionSession.ResultGenerated -= ContinuousRecognitionSession_ResultGenerated;

                this.SpeechRecogniserObj.Dispose();
                this.SpeechRecogniserObj = null;

               
            }

        }

         


        /// <summary>
        /// Run when result received
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void ContinuousRecognitionSession_ResultGenerated(SpeechContinuousRecognitionSession sender, SpeechContinuousRecognitionResultGeneratedEventArgs args)
        {
            if ((Ignore == 0) && (args.Result.Confidence == SpeechRecognitionConfidence.Medium || args.Result.Confidence == SpeechRecognitionConfidence.High))
            {                
                var text = args.Result.Text;
                                
                var moveset = GetMoveSetFromText(text);
                if (moveset.Count > 0)
                {
                    mainDispatcherQueue.TryEnqueue(() =>
                    {
                        MoveFunctionA(moveset, text);
                    });
                    return;
                }

                var fenList = GetFENFromText(text);
                if (fenList.Count > 0)
                {
                    mainDispatcherQueue.TryEnqueue(() =>
                    {
                        PieceFindFunction(fenList, text);
                    });
                    return;
                }
                
                var moveCommandB = GetMoveCommandBFromText(text);
                if (moveCommandB.Count == 3)
                {
                    mainDispatcherQueue.TryEnqueue(() =>
                    {
                        MoveFunctionB(moveCommandB, text);
                    });
                    return;
                }
                
                if (text == "Help")
                {
                    mainDispatcherQueue.TryEnqueue(() =>
                    {
                        HelpFunction(true);
                    });
                    return;
                }

               
            }
        }


        /// <summary>
        /// Run when session completed
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void ContinuousRecognitionSession_Completed(SpeechContinuousRecognitionSession sender, SpeechContinuousRecognitionCompletedEventArgs args)
        {
            if (args.Status != SpeechRecognitionResultStatus.Success)
            {
                Complete = true;
                Listening = false;
            }
        }

        /// <summary>
        /// Run when state changed
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void SpeechRecognizerObj_StateChanged(SpeechRecognizer sender, SpeechRecognizerStateChangedEventArgs args)
        {
            State = args.State;
            
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

        /// <summary>
        /// Gets piece spin from text
        /// </summary>
        /// <param name="pSpeechText"></param>
        /// <returns></returns>
        private List<char> GetFENFromText(string pSpeechText)
        {
            List<char> returnList = new List<char>(2);

            
            if (Regex.IsMatch(pSpeechText, "King$", RegexOptions.IgnoreCase))
            {
                if (Regex.IsMatch(pSpeechText, "^White", RegexOptions.IgnoreCase))
                {                    
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_KING_SPIN));
                }
                else if (Regex.IsMatch(pSpeechText, "^Black", RegexOptions.IgnoreCase))
                {                   
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_KING_SPIN));
                }
                else
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_KING_SPIN));
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_KING_SPIN));
                }
            }
            else if (Regex.IsMatch(pSpeechText, "Queen$", RegexOptions.IgnoreCase))
            {
                if (Regex.IsMatch(pSpeechText, "^White", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_QUEEN_SPIN));
                }
                else if (Regex.IsMatch(pSpeechText, "^Black", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_QUEEN_SPIN));
                }   
                else
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_QUEEN_SPIN));
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_QUEEN_SPIN));
                }

            }
            else if (Regex.IsMatch(pSpeechText, "Rook$", RegexOptions.IgnoreCase))
            {
                if (Regex.IsMatch(pSpeechText, "^White", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_ROOK_SPIN));
                }
                else if (Regex.IsMatch(pSpeechText, "^Black", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_ROOK_SPIN));
                }
                else
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_ROOK_SPIN));
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_ROOK_SPIN));
                }
            }
            else if (Regex.IsMatch(pSpeechText, "Bishop$", RegexOptions.IgnoreCase))
            {
                if (Regex.IsMatch(pSpeechText, "^White", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_BISHOP_SPIN));
                }
                else if (Regex.IsMatch(pSpeechText, "^Black", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_BISHOP_SPIN));
                } 
                else
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_BISHOP_SPIN));
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_BISHOP_SPIN));
                }

            }
            else if (Regex.IsMatch(pSpeechText, "Knight$", RegexOptions.IgnoreCase))
            {
                if (Regex.IsMatch(pSpeechText, "^White", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_KNIGHT_SPIN));
                }
                else if (Regex.IsMatch(pSpeechText, "^Black", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_KNIGHT_SPIN));
                }
                else
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_KNIGHT_SPIN));
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_KNIGHT_SPIN));
                }
               
            }
            else if (Regex.IsMatch(pSpeechText, "Pawn$", RegexOptions.IgnoreCase))
            {
                if (Regex.IsMatch(pSpeechText, "^White", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_PAWN_SPIN));
                }
                else if (Regex.IsMatch(pSpeechText, "^Black", RegexOptions.IgnoreCase))
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_PAWN_SPIN));
                }
                else
                {
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.WHITE_PAWN_SPIN));
                    returnList.Add(_tempBoard.GetFENCharFromSpin(Constants.BLACK_PAWN_SPIN));
                }
                
            }

            return returnList;
        }




        }
}

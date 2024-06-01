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
using System.Text.RegularExpressions;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using KaruahChess.Model;
using Microsoft.Data.Sqlite;
using KaruahChess.Database;
using KaruahChessEngine;
using KaruahChess.Common;

namespace KaruahChess.CustomControl
{
    public sealed partial class ImportPGN : UserControl
    {
        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }

        private ViewModel.BoardViewModel _boardVM;
                
        private KaruahChessEngineClass _board;

        public ImportPGN()
        {

            this.InitializeComponent();
            
            _board = new KaruahChessEngineClass();

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
            PagePopup.IsOpen = true;

        }

        /// <summary>
        /// Close the popup
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Close_Click(object sender, RoutedEventArgs e)
        {
            PagePopup.IsOpen = false;
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
        /// Imports a pgn game a closes the popup
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Import_Click(object sender, RoutedEventArgs e)
        {

            ImportErrorText.Text = "";
            String pgnGameFilterA = ImportPGNTextBox.Text;

            // Remove header comments
            pgnGameFilterA = Regex.Replace(pgnGameFilterA, @"(?s)\[.*\]", "");

            // Remove any curly bracket comments in game string
            pgnGameFilterA = Regex.Replace(pgnGameFilterA, "(?s){.*?}", " ");

            
            // Remove line breaks
            string[] lines = pgnGameFilterA.Split(new[] { "\r\n", "\r", "\n" }, StringSplitOptions.None);
            string pgnGameFilterB = string.Empty;
            foreach (string line in lines)
            {
                // Remove semicolon comments
                pgnGameFilterB = pgnGameFilterB + Regex.Replace(line, @";(.*)?$", "") + " ";
            }

            var pgnGame = pgnGameFilterB;
            var success = ProcessGame(pgnGame);
            if (success) { 
                PagePopup.IsOpen = false;
                if(_boardVM != null) {
                    _boardVM.NavigateMaxRecord();
                }
            }
                        
        }

        /// <summary>
        /// Processes a PGN game string
        /// </summary>
        /// <param name="pPGNGameStr"></param>
        /// <returns></returns>
        public bool ProcessGame(string pPGNGameStr)
        {
            pPGNGameStr = pPGNGameStr.Trim();

            // Remove the score from the end of the PGN string           
            Regex regex = new Regex("[10][-][10]$");            
            if (regex.Match(pPGNGameStr).Success) {                
                pPGNGameStr = regex.Replace(pPGNGameStr, "");
            }

            regex = new Regex("[1][/][2][-][1][/][2]$");            
            if (regex.Match(pPGNGameStr).Success) {                
                pPGNGameStr = regex.Replace(pPGNGameStr, "");
            }

            // Replace multi spaces            
            regex = new Regex("[ ]{2,}");
            pPGNGameStr = regex.Replace(pPGNGameStr, " ");
            pPGNGameStr = pPGNGameStr.Trim();

            var gameParseA = Regex.Replace(pPGNGameStr, @"[0-9]?[0-9]?[0-9]\.", "|");
            var gameParseB = gameParseA.Split('|');
            
            _board.Reset();
                        
            string rtnMsg = string.Empty;

            // Loop through game moves
            int id = 1;
            int moveNumber = 1;

            // Add start record
            List<GameRecordArray> gameRecList = new List<GameRecordArray>();
            GameRecordArray startRecord = new GameRecordArray();
            startRecord.Id = id;
            _board.GetBoardArray(startRecord.BoardArray);
            _board.GetStateArray(startRecord.StateArray);  
            gameRecList.Add(startRecord);
            id++;

            // Add all moves in the PGN string
            try {                 
                foreach (var move in gameParseB)
                {
                    if (move.Length > 0)
                    {
                        string[] halfMoveArray = move.Trim().Split(' ');
                        foreach (var halfMove in halfMoveArray)
                        {
                            if (halfMove.Trim() == "") continue;                        
                            bool success = MovePGN(halfMove, _board, true);
                            if (!success)
                            {
                                ImportErrorText.Text += "Import failed. Error occurred at move " + moveNumber + ": " + move + Environment.NewLine;
                                ImportErrorText.Text += rtnMsg;
                                return false;
                            }

                            GameRecordArray gamerec = new GameRecordArray();
                            gamerec.Id = id;
                            _board.GetBoardArray(gamerec.BoardArray);
                            _board.GetStateArray(gamerec.StateArray);
                            gameRecList.Add(gamerec);
                            
                            id++;
                        }
                        moveNumber++;
                        
                    }
                }

                if (gameRecList.Count <= 1)
                {
                    ImportErrorText.Text = "Import failed - nothing to import.";
                    return false;
                }

                // Loads the game in to the database
                LoadGameIntoDatabase(gameRecList);
             
            }
            catch(Exception ex)
            {                
                ImportErrorText.Text += ex.Message;
                return false;
            }

            // If made this far then return true
            return true;
        }


        /// <summary>
        /// Moves a piece according to the PGN value
        /// </summary>
        /// <param name="pPGNValue"></param>
        /// <param name="pBoard"></param>
        /// <param name="pCommit"></param>
        /// <param name="pMoveData"></param>
        /// <param name="pReturnMessage"></param>
        /// <returns></returns>
        private static bool MovePGN(string pPGNValue, KaruahChessEngineClass pBoard, bool pCommit)
        {

            if (Regex.IsMatch(pPGNValue, @"^[a-h][1-8]([=][QRBN])?[\+]?[#]?$"))
            {
                // Pawn move

                ViewModel.BoardViewModel.PawnPromotionEnum Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Queen;
                if (Regex.IsMatch(pPGNValue, @"^[a-h][1-8][=][QRBN][\+]?[#]?$"))
                {
                    String PromotionStr = pPGNValue.Substring(3, 1);
                    if (PromotionStr == "Q") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Queen;
                    else if (PromotionStr == "R") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Rook;
                    else if (PromotionStr == "B") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Bishop;
                    else if (PromotionStr == "N") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Knight;                    
                }

                int[] validFromIndexes = helper.FileDict[pPGNValue.Substring(0, 1)];
                var toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(0, 2)];                
                var fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour(), validFromIndexes);
                MoveResult mResult = pBoard.Move(fromIndex, toIndex, (int)Promotion, true, pCommit);
                return mResult.success;

            }
            else if (Regex.IsMatch(pPGNValue, @"^[a-h][x][a-h][1-8]([=][QRBN])?[\+]?[#]?$"))
            {
                // Pawn take move
                ViewModel.BoardViewModel.PawnPromotionEnum Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Queen;
                if (Regex.IsMatch(pPGNValue, @"^[a-h][x][a-h][1-8][=][QRBN][\+]?[#]?$"))
                {
                    String PromotionStr = pPGNValue.Substring(5, 1);
                    if (PromotionStr == "Q") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Queen;
                    else if (PromotionStr == "R") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Rook;
                    else if (PromotionStr == "B") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Bishop;
                    else if (PromotionStr == "N") Promotion = ViewModel.BoardViewModel.PawnPromotionEnum.Knight;
                }

                int[] validFromIndexes = helper.FileDict[pPGNValue.Substring(0, 1)];
                var toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(2, 2)];                
                var fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour(), validFromIndexes);
                MoveResult mResult = pBoard.Move(fromIndex, toIndex, (int)Promotion, true, pCommit);
                return mResult.success;
            }
            else if (Regex.IsMatch(pPGNValue, @"^[KQRBN][a-h]?[1-8]?[x]?[a-h][1-8][\+]?[#]?$"))
            {
                // King, Queen, Rook, Bishop, Knight move with from file
                int spin = 0;
                string piece = pPGNValue.Substring(0, 1);
                if (piece == "K") spin = Constants.WHITE_KING_SPIN;
                else if (piece == "Q") spin = Constants.WHITE_QUEEN_SPIN;
                else if (piece == "R") spin = Constants.WHITE_ROOK_SPIN;
                else if (piece == "B") spin = Constants.WHITE_BISHOP_SPIN;
                else if (piece == "N") spin = Constants.WHITE_KNIGHT_SPIN;

                int toIndex = -1;
                int fromIndex = -1;

                if (Regex.IsMatch(pPGNValue, @"^[KQRBN][a-h][1-8][\+]?[#]?$"))
                {
                    // Move
                    toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(1, 2)];                    
                    fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour() * spin, null);
                }
                else if (Regex.IsMatch(pPGNValue, @"^[KQRBN][a-h][a-h][1-8][\+]?[#]?$"))
                {
                    // Move with file
                    int[] validFromIndexes = helper.FileDict[pPGNValue.Substring(1, 1)];
                    toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(2, 2)];                    
                    fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour() * spin, validFromIndexes);
                }
                else if (Regex.IsMatch(pPGNValue, @"^[KQRBN][1-8][a-h][1-8][\+]?[#]?$"))
                {
                    // Move with rank
                    int[] validFromIndexes = helper.RankDict[pPGNValue.Substring(1, 1)];
                    toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(2, 2)];                    
                    fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour() * spin, validFromIndexes);
                }
                else if (Regex.IsMatch(pPGNValue, @"^[KQRBN][x][a-h][1-8][\+]?[#]?$"))
                {
                    // Move
                    toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(2, 2)];                    
                    fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour() * spin, null);
                }
                else if (Regex.IsMatch(pPGNValue, @"^[KQRBN][a-h][x][a-h][1-8][\+]?[#]?$"))
                {
                    // Move with file
                    int[] validFromIndexes = helper.FileDict[pPGNValue.Substring(1, 1)];
                    toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(3, 2)];                    
                    fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour() * spin, validFromIndexes);
                }
                else if (Regex.IsMatch(pPGNValue, @"^[KQRBN][1-8][x][a-h][1-8][\+]?[#]?$"))
                {
                    // Move with rank
                    int[] validFromIndexes = helper.RankDict[pPGNValue.Substring(1, 1)];
                    toIndex = helper.BoardCoordinateReverseDict[pPGNValue.Substring(3, 2)];                    
                    fromIndex = pBoard.FindFromIndex(toIndex, pBoard.GetStateActiveColour() * spin, validFromIndexes);
                }

                MoveResult mResult = pBoard.Move(fromIndex, toIndex, (int)ViewModel.BoardViewModel.PawnPromotionEnum.Queen, true, pCommit);
                return mResult.success;
            }
            else if (Regex.IsMatch(pPGNValue, @"^[O][-][O][\+]?[#]?$"))
            {
                // King side castle                
                bool castlingAvailableKingSide = false;
                if (pBoard.GetStateActiveColour() == Constants.WHITEPIECE) castlingAvailableKingSide = (pBoard.GetStateCastlingAvailability() & 0b000010) > 0;
                else if (pBoard.GetStateActiveColour() == Constants.BLACKPIECE) castlingAvailableKingSide = (pBoard.GetStateCastlingAvailability() & 0b001000) > 0;

                if (!castlingAvailableKingSide) throw new Exception("Castling not available, invalid move.");


                int fromIndex = pBoard.GetKingIndex(pBoard.GetStateActiveColour());
                int toIndex = pBoard.GetStateActiveColour() == Constants.WHITEPIECE ? 62 : 6;


                MoveResult mResult = pBoard.Move(fromIndex, toIndex, (int)ViewModel.BoardViewModel.PawnPromotionEnum.Queen, true, pCommit);
                return mResult.success;
            }
            else if (Regex.IsMatch(pPGNValue, @"^[O][-][O][-][O][\+]?[#]?$"))
            {
                // King side castle                
                bool castlingAvailableQueenSide = false;
                if (pBoard.GetStateActiveColour() == Constants.WHITEPIECE) castlingAvailableQueenSide = (pBoard.GetStateCastlingAvailability() & 0b000001) > 0;
                else if (pBoard.GetStateActiveColour() == Constants.BLACKPIECE) castlingAvailableQueenSide = (pBoard.GetStateCastlingAvailability() & 0b000100) > 0;

                if (!castlingAvailableQueenSide) throw new Exception("Castling not available, invalid move.");


                int fromIndex = pBoard.GetKingIndex(pBoard.GetStateActiveColour());
                int toIndex = pBoard.GetStateActiveColour() == Constants.WHITEPIECE ? 58 : 2;


                MoveResult mResult = pBoard.Move(fromIndex, toIndex, (int)ViewModel.BoardViewModel.PawnPromotionEnum.Queen, true, pCommit);
                return mResult.success;
            }

            return false;

        }
                

        /// <summary>
        /// Loads the game in to the databse
        /// </summary>
        /// <param name="pGameRecordList"></param>
        private void LoadGameIntoDatabase(List<GameRecordArray> pGameRecordList)
        {            
            KaruahChessEngineClass board = new KaruahChessEngineClass();

            // Do the import
            using (var connection = KaruahChessDB.GetDBConnection())
            {
                using (var tran = connection.BeginTransaction())
                {
                    try
                    {
                        // multiple operations involving cn and tran here
                        using (var command = connection.CreateCommand())
                        {
                            command.CommandText = $"Delete from {KaruahChessDB.GameRecordTableName};";
                            command.ExecuteNonQuery();
                        }

                        foreach (var gr in pGameRecordList)
                        {
                            board.SetBoardArray(gr.BoardArray);
                            board.SetStateArray(gr.StateArray);                            
                            String boardSquareStr = board.GetBoard();
                            String gameStateStr = board.GetState();

                            using (var command = connection.CreateCommand())
                            {
                                command.CommandText = $"INSERT INTO {KaruahChessDB.GameRecordTableName} (Id, BoardSquareStr, GameStateStr) Values (@Id, @BoardSquareStr, @GameStateStr);";
                                command.Parameters.Add(new SqliteParameter("@Id", gr.Id));
                                command.Parameters.Add(new SqliteParameter("@BoardSquareStr", boardSquareStr));
                                command.Parameters.Add(new SqliteParameter("@GameStateStr", gameStateStr));
                                command.ExecuteNonQuery();
                            }

                        }


                        tran.Commit();
                    }
                    catch (Exception ex)
                    {
                        tran.Rollback();
                        throw ex;
                    }
                }

                GameRecordDataService.instance.Load();
            }
        }
    }
}

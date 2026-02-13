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
using KaruahChess.Common;
using PurpleTreeSoftware.Panel;
using KaruahChess.Pieces;
using KaruahChessEngine;

namespace KaruahChess.Model
{
    public class BoardSquareDataService : IBoardSquareDataService
    {
       
        private HashSet<int> _hiddenSquares = new HashSet<int>();

        public double SquareSize { get; set; }
        public bool LargePawn { get; set; }

        KaruahChessEngineClass _tempboard;

        /// <summary>
        /// List of tile pieces
        /// </summary>
        ObservableCollectionCustom<Tile> _boardTiles;
        public ObservableCollectionCustom<Tile> BoardTiles
        {
            get { return _boardTiles; }
            private set { _boardTiles = value; }
        }
        

        // Constructor
        private BoardSquareDataService()
        {            
            BoardTiles = new ObservableCollectionCustom<Tile>();
            _tempboard = new KaruahChessEngineClass();
        }

        /// <summary>
        /// Singleton accessor
        /// </summary>
        public static BoardSquareDataService instance { get; private set; } = new BoardSquareDataService();


        /// <summary>
        /// Loads squares in to the observable collection
        /// </summary>
        /// <param name="pSquareSize">The pixel width of the Tile. Height is set to the same value.</param>        
        public void Load(double pSquareSize, bool pLargePawn, GameRecordArray pRec)
        {
            SquareSize = pSquareSize;
            LargePawn = pLargePawn;
            _tempboard.SetBoardArray(pRec.BoardArray);
            _tempboard.SetStateArray(pRec.StateArray);

            List<BoardSquare> boardSquareList = new List<BoardSquare>(64);
            for (int i = 0; i < 64; i++)
            {
                var bsquare = new BoardSquare();
                bsquare.Index = i;
                char fen = _tempboard.GetFENCharFromSpin(_tempboard.GetSpin(i));
                bsquare.SetFEN(fen, pSquareSize, pLargePawn);
                boardSquareList.Add(bsquare);
            }

            // Create the tiles            
            boardSquareList.GetTiles<BoardSquare>(ref _boardTiles, SquareSize);

        }

               
        /// <summary>
        /// Updates a board square
        /// </summary>
        /// <param name="pBoardSquare">The board square item to update</param>       
        /// <param name="pReload">Reload all from database after update</param>
        /// <returns></returns>
        public int Update(GameRecordArray pRecord, bool pChangeVisible)
        {
            _tempboard.SetBoardArray(pRecord.BoardArray);
            _tempboard.SetStateArray(pRecord.StateArray);

            int updateCount = 0;

            if (BoardTiles.Count == 64)
            {
                for(int sqIndex = 0; sqIndex < 64; sqIndex++)
                {
                    var tile = BoardTiles[sqIndex];
                    var currentSq = (BoardSquare)tile.Entity;
                    char fenSq = _tempboard.GetFENCharFromSpin(_tempboard.GetSpin(sqIndex));
                    if (currentSq.GetFEN() != fenSq) {                         
                        if (pChangeVisible == false) {
                            // Change is not immediately visible to the user                        
                            if (!_hiddenSquares.Contains(sqIndex))
                            {
                                 tile.SetVisibility(Microsoft.UI.Xaml.Visibility.Collapsed);
                                _hiddenSquares.Add(sqIndex);
                            }
                        }
                        currentSq.SetFEN(fenSq, SquareSize, LargePawn);
                        updateCount++;
                    }
                }

               
            }
            
            return updateCount;
        }


        /// <summary>
        /// Gets a board square ID
        /// </summary>
        /// <param name="pBoardSquareID"></param>
        /// <returns></returns>
        public BoardSquare Get(int pBoardSquareIndex)
        {
            if (pBoardSquareIndex >= 0 && pBoardSquareIndex <= 63)
            {
                return (BoardSquare)BoardTiles[pBoardSquareIndex].Entity;
            }
            else
            {
                return null;
            }
        }


        /// <summary>
        /// Hides a square
        /// </summary>
        /// <param name="pBoardSquareId"></param>
        public void Hide(int pBoardSquareIndex)
        {
            if (pBoardSquareIndex >= 0 && BoardTiles.Count == 64)
            {
                var tile = BoardTiles[pBoardSquareIndex];

                // Hide square
                if (!_hiddenSquares.Contains(pBoardSquareIndex))
                {
                    tile.SetVisibility(Microsoft.UI.Xaml.Visibility.Collapsed);
                    _hiddenSquares.Add(pBoardSquareIndex);
                }

            }
        }

        /// <summary>
        /// Makes all the hidden squares visible
        /// </summary>
        public void ShowAllHidden()
        {
            foreach (var sqIndex in _hiddenSquares)
            {
                var tile = BoardTiles[sqIndex];
                tile.SetVisibility(Microsoft.UI.Xaml.Visibility.Visible);
            }

            _hiddenSquares.Clear();
        }

        /// <summary>
        /// Get board squre id as a string description
        /// </summary>
        /// <param name="pBoardSquareID"></param>
        /// <returns></returns>
        public String GetDescription(int pBoardSquareIndex)
        {
            if (pBoardSquareIndex >= 0 && pBoardSquareIndex <= 63)
            {
                var sq = (BoardSquare)BoardTiles[pBoardSquareIndex].Entity;
                string description = Enum.GetName(typeof(Pieces.Piece.ColourEnum), sq.PieceColour);
                description = string.Join(" ", description, Enum.GetName(typeof(Pieces.Piece.TypeEnum), sq.PieceType));

                return description;
            }
            else
            {
                return String.Empty;
            }
        }


        


        /// <summary>
        /// Gets piece colour from spin
        /// </summary>
        /// <param name="pSpin"></param>
        public static Piece.ColourEnum GetPieceColourFromSpin(int pSpin)
        {
            if (pSpin > 0)
            {
                return Piece.ColourEnum.White;
            }
            else if (pSpin < 0)
            {
                return Piece.ColourEnum.Black;
            }
            else
            {
                return Piece.ColourEnum.None;
            }
        }


        /// <summary>
        /// Gets piece type from spin
        /// </summary>
        /// <param name="pSpin"></param>
        public static Piece.TypeEnum GetPieceTypeFromSpin(int pSpin)
        {
            if (pSpin == Constants.WHITE_PAWN_SPIN || pSpin == Constants.BLACK_PAWN_SPIN)
            {
                return Piece.TypeEnum.Pawn;
            }
            else if (pSpin == Constants.WHITE_KNIGHT_SPIN || pSpin == Constants.BLACK_KNIGHT_SPIN)
            {
                return Piece.TypeEnum.Knight;
            }
            else if (pSpin == Constants.WHITE_BISHOP_SPIN || pSpin == Constants.BLACK_BISHOP_SPIN)
            {
                return Piece.TypeEnum.Bishop;
            }
            else if (pSpin == Constants.WHITE_ROOK_SPIN || pSpin == Constants.BLACK_ROOK_SPIN)
            {
                return Piece.TypeEnum.Rook;
            }
            else if (pSpin == Constants.WHITE_QUEEN_SPIN || pSpin == Constants.BLACK_QUEEN_SPIN)
            {
                return Piece.TypeEnum.Queen;
            }
            else if (pSpin == Constants.WHITE_KING_SPIN || pSpin == Constants.BLACK_KING_SPIN)
            {
                return Piece.TypeEnum.King;
            }
            else
            {
                return Piece.TypeEnum.Empty;
            }
        }

        /// <summary>
        /// Locate pieces from spin value
        /// </summary>
        /// <param name="pSpin"></param>
        /// <returns>Coordiantes of pieces found</returns>
        public List<string> LocatePiece(char pFenChar)
        {
            var coordList = new List<string>(16);

            var tileCount = _boardTiles.Count;

            // Constructs the list ordered by file
            var fileList = new List<int[]>(8);
            fileList.Add(helper.FileDict["a"]);
            fileList.Add(helper.FileDict["b"]);
            fileList.Add(helper.FileDict["c"]);
            fileList.Add(helper.FileDict["d"]);
            fileList.Add(helper.FileDict["e"]);
            fileList.Add(helper.FileDict["f"]);
            fileList.Add(helper.FileDict["g"]);
            fileList.Add(helper.FileDict["h"]);

            if (tileCount == 64)
            {
               foreach(var file in fileList)
                {
                    for(int i = 0; i < 8; i++)
                    {
                        var tileIndex = file[i];
                        var sq = (BoardSquare)_boardTiles[tileIndex].Entity;
                        if (pFenChar.Equals(sq.GetFEN())) coordList.Add(helper.BoardCoordinateDict[sq.Index]);
                    }
                }
               
            }

            return coordList;
        }

    }
}

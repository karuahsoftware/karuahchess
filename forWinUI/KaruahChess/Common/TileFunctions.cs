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
using KaruahChess.Model;
using PurpleTreeSoftware.Panel;


namespace KaruahChess.Common
{
    public static class TileFunctions
    {
      

        /// <summary>
        /// Adds a list of tiles to an observable collection
        /// </summary>
        /// <typeparam name="T">The type of object in the list</typeparam>
        /// <param name="pList">A list of objects to add to tile collection</param>
        /// <param name="pTileCollection">The collection of tiles</param>
        public static void GetTiles<T>(this IEnumerable<T> pList, ref ObservableCollectionCustom<Tile> pTileCollection, Double pSquareSize)
        {
            var rd = helper.GetStyles();         

            pTileCollection.SupressNotification = true;
            pTileCollection.Clear();

            // Load the data in to the collection
            foreach (T obj in pList)
            {
                if (typeof(T) == typeof(BoardSquare))
                {   
                    // Create a new tile
                    var sq = obj as BoardSquare;
                                                           
                   
                    // Create the tile
                    var tile = new Tile(sq, sq.Piece);
                    
                    // Set the colour
                    if (sq.Colour == BoardSquare.ColourEnum.Black)
                    {
                        tile.StyleTemplate = (TileStyleTemplate)rd["BlackTileStyleTemplateObject"];
                        tile.StyleTemplate.Width = pSquareSize;
                        tile.StyleTemplate.Height = pSquareSize;
                    }
                    else
                    {
                        tile.StyleTemplate = (TileStyleTemplate)rd["WhiteTileStyleTemplateObject"];
                        tile.StyleTemplate.Width = pSquareSize;
                        tile.StyleTemplate.Height = pSquareSize;
                    }

                    // Add tile to the collection
                    pTileCollection.Add(tile);
                }
            }
            pTileCollection.SupressNotification = false;

        }

        /// <summary>
        /// Gets the entities from a tile collection
        /// </summary>
        /// <typeparam name="T">The object type expected in the tile entity property</typeparam>
        /// <param name="pTileCollection">The collection of tiles to iterate over</param>
        /// <returns>A list containing the entity objects</returns>
        public static List<T> GetEntities<T>(this ObservableCollectionCustom<Tile> pTileCollection)
        {
            var entityList = new List<T>(pTileCollection.Count);
            foreach (var tile in pTileCollection)
            {
                if (tile.Entity != null)
                {
                    entityList.Add((T)tile.Entity);
                }
            }

            return entityList;
        }


        

    }
}

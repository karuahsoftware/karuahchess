﻿/*
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
using Windows.Foundation;
using Windows.UI.Xaml.Media.Imaging;


namespace KaruahChess.Pieces
{
    public class PieceAnimationInstruction
    {

        public enum AnimationTypeEnum { Move = 0, Take = 1, Put = 2, Fall = 3 }

        /// <summary>
        /// Constructor
        /// </summary>
        public PieceAnimationInstruction()
        {
            HiddenSquareIndexes = new List<int>();
        } 

        public AnimationTypeEnum AnimationType { get; set; }

        /// <summary>
        /// Image bitmap
        /// </summary>
        public BitmapImage ImageData { get; set; }

      
        /// <summary>
        /// Move from point
        /// </summary>
        public Point MoveFrom { get; set; }

        /// <summary>
        /// Move to point
        /// </summary>
        public Point MoveTo { get; set; }


        /// <summary>
        /// List of square indexes that should be hidden for the animation
        /// </summary>
        public List<int> HiddenSquareIndexes { get; private set; }

    }
}

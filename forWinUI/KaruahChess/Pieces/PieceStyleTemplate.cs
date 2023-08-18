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
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;

namespace KaruahChess.Pieces
{
    public class PieceStyleTemplate : DependencyObject
    {
        
        /// <summary>
        /// Constructor
        /// </summary>
        public PieceStyleTemplate()
            {
            
            }

            /// <summary>
            /// Background colour of Piece
            /// </summary>
            public Brush Background
            {
                get { return (Brush)GetValue(BackgroundProperty); }
                set { SetValue(BackgroundProperty, value); }
            }

            // Using a DependencyProperty as the backing store for Background. 
            public static readonly DependencyProperty BackgroundProperty =
                DependencyProperty.Register("Background", typeof(Brush), typeof(PieceStyleTemplate), new PropertyMetadata(null));

    

    }
    }

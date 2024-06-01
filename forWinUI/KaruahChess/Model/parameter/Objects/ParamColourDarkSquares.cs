﻿/*
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
using System.Runtime.Serialization;
using KaruahChess.Common;

namespace KaruahChess.Model.ParameterObjects
{
    [DataContract]
    public class ParamColourDarkSquares
    {
        
        [DataMember]
        public byte A { get; set; }

        [DataMember]
        public byte R { get; set; }

        [DataMember]
        public byte G { get; set; }

        [DataMember]
        public byte B { get; set; }

        
        public ParamColourDarkSquares()
        {
            // Set default value (green)
            A = 255;
            R = 100;
            G = 153;
            B = 100;
        }

        public ColourARGB ARGB()
        {
            return new ColourARGB(A, R, G, B, "");
        }
    }
}
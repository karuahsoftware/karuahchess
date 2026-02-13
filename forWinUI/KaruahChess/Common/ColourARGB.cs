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


namespace KaruahChess.Common
{
    public struct ColourARGB : IEquatable<ColourARGB>
    {
        public byte A;
        public byte R;
        public byte G;
        public byte B;
        public string Text;

        internal ColourARGB(byte pA, byte pR, byte pG, byte pB, string pText)
        {
            A = pA;
            R = pR;
            G = pG;
            B = pB;
            Text = pText;
        }



        public static bool operator ==(ColourARGB pLeft, ColourARGB pRight)
        {
            return Equals(pLeft, pRight);
        }

        public static bool operator !=(ColourARGB pLeft, ColourARGB pRight)
        {
            return !Equals(pLeft, pRight);
        }

        public override bool Equals(object pObj)
        {
            return (pObj is ColourARGB pColour) && Equals(pColour);
        }

        public bool Equals(ColourARGB pColour)
        {
            return A == pColour.A && R == pColour.R && G == pColour.G && B == pColour.B;
        }

        public override int GetHashCode()
        {
            return (A, R, G, B).GetHashCode();
        }

        public Windows.UI.Color GetColour()
        {
            return new Windows.UI.Color() { A = A, R = R, G = G, B = B };
        }
    }
}

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
using System.Xml.Serialization;
using System.IO;
using System.Collections.Generic;
using Microsoft.UI.Xaml;

namespace KaruahChess.Common
{
    public static class helper
    {

        /// <summary>
        /// Serialises a class to xml
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="dataToSerialize"></param>
        /// <returns></returns>
        public static byte[] Serialize<T>(this T dataToSerialize)
        {
            MemoryStream returnStream = new MemoryStream();
            var serializer = new XmlSerializer(typeof(T));
            serializer.Serialize(returnStream, dataToSerialize);

            return returnStream.ToArray();
        }

        /// <summary>
        /// Deseerialises an xml string
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="xmlText"></param>
        /// <returns></returns>
        public static T Deserialize<T>(this byte[] xmlByte)
        {
            if (xmlByte != null)
            {
                var xmlStream = new MemoryStream(xmlByte);
                var serializer = new XmlSerializer(typeof(T));
                
                return (T)serializer.Deserialize(xmlStream);
            }
            else
            {
                return default(T);
            }

        }


        /// <summary>
        /// Gets the styles resouce dictionary
        /// </summary>
        /// <returns></returns>
        public static ResourceDictionary GetStyles()
        {
            var rd = new ResourceDictionary();
            rd.Source = new Uri("ms-appx:///Common/Styles.xaml");

            return rd;
        }

        

        /// <summary>
        /// Returns the not of a bool
        /// </summary>
        /// <param name="pBool"></param>
        public static bool NotBool(bool pBool)
        {
            return !pBool;
        }
                

        public static Dictionary<int, String> BoardCoordinateDict = new Dictionary<int, String>()
        {
            {0, "a8"}, {1, "b8"}, {2, "c8"},{3, "d8"},{4, "e8"},{5, "f8"},{6, "g8"},{7, "h8"},
            {8, "a7"}, {9, "b7"}, {10, "c7"},{11, "d7"},{12, "e7"},{13, "f7"},{14, "g7"},{15, "h7"},
            {16, "a6"}, {17, "b6"}, {18, "c6"},{19, "d6"},{20, "e6"},{21, "f6"},{22, "g6"},{23, "h6"},
            {24, "a5"}, {25, "b5"}, {26, "c5"},{27, "d5"},{28, "e5"},{29, "f5"},{30, "g5"},{31, "h5"},
            {32, "a4"}, {33, "b4"}, {34, "c4"},{35, "d4"},{36, "e4"},{37, "f4"},{38, "g4"},{39, "h4"},
            {40, "a3"}, {41, "b3"}, {42, "c3"},{43, "d3"},{44, "e3"},{45, "f3"},{46, "g3"},{47, "h3"},
            {48, "a2"}, {49, "b2"}, {50, "c2"},{51, "d2"},{52, "e2"},{53, "f2"},{54, "g2"},{55, "h2"},
            {56, "a1"}, {57, "b1"}, {58, "c1"},{59, "d1"},{60, "e1"},{61, "f1"},{62, "g1"},{63, "h1"}
        };


        public static Dictionary<String, int> BoardCoordinateReverseDict = new Dictionary<String, int>()
        {
            {"a8", 0}, {"b8", 1}, {"c8", 2},{"d8", 3},{"e8", 4 },{"f8", 5 },{"g8", 6 },{"h8", 7},
            {"a7", 8 }, {"b7", 9}, {"c7", 10},{"d7", 11},{"e7", 12},{"f7", 13},{"g7", 14},{"h7", 15},
            {"a6", 16}, {"b6", 17}, {"c6", 18},{"d6", 19},{"e6", 20},{"f6", 21},{"g6", 22},{"h6", 23},
            {"a5", 24}, {"b5", 25}, {"c5", 26},{"d5", 27},{"e5", 28},{"f5", 29},{"g5", 30},{"h5", 31},
            {"a4", 32}, {"b4", 33}, {"c4", 34},{"d4", 35},{"e4", 36},{"f4", 37},{"g4", 38},{"h4", 39},
            {"a3", 40}, {"b3", 41}, {"c3", 42},{"d3", 43},{"e3", 44},{"f3", 45},{"g3", 46},{"h3", 47},
            {"a2", 48}, {"b2", 49}, {"c2", 50},{"d2", 51},{"e2", 52},{"f2", 53},{"g2", 54},{"h2", 55},
            {"a1", 56}, {"b1", 57}, {"c1", 58},{"d1", 59},{"e1", 60},{"f1", 61},{"g1", 62},{"h1", 63}
        };


        public static Dictionary<String, int[]> FileDict = new Dictionary<String, int[]>()
        {
            {"a", new int[8] { 0, 8, 16, 24, 32, 40, 48, 56 } },
            {"b", new int[8] { 1, 9, 17, 25, 33, 41, 49, 57 } },
            {"c", new int[8] { 2, 10, 18, 26, 34, 42, 50, 58 } },
            {"d", new int[8] { 3, 11, 19, 27, 35, 43, 51, 59 } },
            {"e", new int[8] { 4, 12, 20, 28, 36, 44, 52, 60 } },
            {"f", new int[8] { 5, 13, 21, 29, 37, 45, 53, 61 } },
            {"g", new int[8] { 6, 14, 22, 30, 38, 46, 54, 62 } },
            {"h", new int[8] { 7, 15, 23, 31, 39, 47, 55, 63 } }
        };


        public static Dictionary<String, int[]> RankDict = new Dictionary<String, int[]>()
        {
            {"8", new int[8] { 0, 1, 2, 3, 4, 5, 6, 7 } },
            {"7", new int[8] { 8, 9, 10, 11, 12, 13, 14, 15} },
            {"6", new int[8] { 16, 17, 18, 19, 20, 21, 22, 23} },
            {"5", new int[8] { 24, 25, 26, 27, 28, 29, 30, 31} },
            {"4", new int[8] { 32, 33, 34, 35, 36, 37, 38, 39} },
            {"3", new int[8] { 40, 41, 42, 43, 44, 45, 46, 47} },
            {"2", new int[8] { 48, 49, 50, 51, 52, 53, 54, 55} },
            {"1", new int[8] { 56, 57, 58, 59, 60, 61, 62, 63} }
        };


        /// <summary>
        /// Gets a binary string from a UInt, for debugging
        /// </summary>
        /// <param name="pInt"></param>
        /// <returns></returns>
        public static String GetBinaryStr(this UInt64 pInt)
        {
            UInt64 mask = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000u;
            string binary = string.Empty;

            for (int i = 0; i < 64; i++)
            {
                if ((mask & pInt) > 0)
                {
                    binary = binary + "1";
                }
                else
                {
                    binary = binary + "0";
                }

                if ((i + 1) % 8 == 0 && i < 63) binary = binary + "_";
                mask >>= 1;

            }


            return binary;

        }


        private const UInt64 BitScanMagic = 0x37E84A99DAE458F;
        private static readonly int[] BitScanMagicTable =
        {
        0, 1, 17, 2, 18, 50, 3, 57,
        47, 19, 22, 51, 29, 4, 33, 58,
        15, 48, 20, 27, 25, 23, 52, 41,
        54, 30, 38, 5, 43, 34, 59, 8,
        63, 16, 49, 56, 46, 21, 28, 32,
        14, 26, 24, 40, 53, 37, 42, 7,
        62, 55, 45, 31, 13, 39, 36, 6,
        61, 44, 12, 35, 60, 11, 10, 9,
        };

        /// <summary>
        /// Returns the index of the first bit set from the least significant bit
        /// </summary>        
        public static int BitScanForward(UInt64 pNum)
        {
            return BitScanMagicTable[((ulong)((long)pNum & -(long)pNum) * BitScanMagic) >> 58];
        }


        /// <summary>
        /// Logs exception messages
        /// </summary>
        public static void LogException(AggregateException ex)
        {
            
        }


        public static void LogError(int errorID)
        {
            
        }
    }
}

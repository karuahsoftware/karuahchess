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
using Microsoft.UI.Xaml.Controls;


namespace KaruahChess.CustomControl
{
    public sealed partial class Coordinates : UserControl
    {
        public enum AxisEnum { X = 0, Y = 1}
        

        private List<Border> rankFileList = new List<Border>(16);
        private String[] fileArray = new String[] { "a", "b", "c", "d", "e", "f", "g", "h" };
        private String[] fileArrayRev = new String[] { "h", "g", "f", "e", "d", "c", "b", "a" };
        private String[] rankArray = new String[] { "1", "2", "3", "4", "5", "6", "7", "8" };
        private String[] rankArrayRev = new String[] { "8", "7", "6", "5", "4", "3", "2", "1" };

        /// <summary>
        /// Constructor
        /// </summary>
        public Coordinates()
        {
            this.InitializeComponent();


            rankFileList.Add(Coordinate0);
            rankFileList.Add(Coordinate1);
            rankFileList.Add(Coordinate2);
            rankFileList.Add(Coordinate3);
            rankFileList.Add(Coordinate4);
            rankFileList.Add(Coordinate5);
            rankFileList.Add(Coordinate6);
            rankFileList.Add(Coordinate7);
            rankFileList.Add(Coordinate8);
            rankFileList.Add(Coordinate9);
            rankFileList.Add(Coordinate10);
            rankFileList.Add(Coordinate11);
            rankFileList.Add(Coordinate12);
            rankFileList.Add(Coordinate13);
            rankFileList.Add(Coordinate14);
            rankFileList.Add(Coordinate15);

        }


        /// <summary>
        /// Show or hide the control
        /// </summary>
        /// <param name="pEnabled"></param>
        public void Show(bool pEnabled)
        {
            if (pEnabled)
            {
                this.Visibility = Microsoft.UI.Xaml.Visibility.Visible;
            }
            else
            {
                this.Visibility = Microsoft.UI.Xaml.Visibility.Collapsed;
            }
        }

        /// <summary>
        /// Draw layout.
        /// </summary>
        /// <param name="pPanelWidth"></param>
        /// <param name="pPanelHeight"></param>
        /// <param name="pBoardMargin"></param>
        /// <param name="pRotation"></param>
        public void Draw(double pTileSize, double pWidth, double pHeight, int pRotation)
        {

            // Only run this function if board is visible
            if (this.Visibility != Microsoft.UI.Xaml.Visibility.Visible) return;

            // Set layout width and height
            this.Width = pWidth;
            this.Height = pHeight;

            // Connect up the layout
            SetCoordLabels(pRotation);
            var coordListX = GetCoordList(AxisEnum.X);
            var coordListY = GetCoordList(AxisEnum.Y);
    
            // Set the coordinate text width and height to match tile size
            for(int index = 0; index <= 7; index++)
            {
                var coordY = coordListY[index];
                var coordX = coordListX[index];
               
                coordY.Height = pTileSize;
                coordX.Width = pTileSize;
                
            }
        

    }

    /// <summary>
    /// Get coordinate list
    /// </summary>
    /// <param name="pAxis"></param>
    /// <returns></returns>
    private List<Border> GetCoordList(AxisEnum pAxis) {

            List<Border> coordList = new List<Border>(8);

            // Rank List
            if (pAxis == AxisEnum.Y)  {
                for (int index = 0; index <= 7; index++) {
                    coordList.Add(rankFileList[index]);
                }
            }
            else  {
                for (int index = 8; index <= 15; index++) {
                    coordList.Add(rankFileList[index]);
                }
            }


            return coordList;

        }

        /// <summary>
        /// Set coordinate labels
        /// </summary>
        /// <param name="pRotation"></param>
        public void SetCoordLabels(int pRotation)
        {
            // Only run this function if board is visible
            if (this.Visibility != Microsoft.UI.Xaml.Visibility.Visible) return;


            for (int index = 0; index <= 7; index ++)
            {
                if (pRotation == 0)
                {
                    ((TextBlock)rankFileList[index].Child).Text = rankArrayRev[index];
                    ((TextBlock)rankFileList[index + 8].Child).Text = fileArray[index];
                }
                else if (pRotation == -90)
                {
                    ((TextBlock)rankFileList[index].Child).Text = fileArray[index];
                    ((TextBlock)rankFileList[index + 8].Child).Text = rankArray[index];
                }
                else if (pRotation == -180)
                {
                    ((TextBlock)rankFileList[index].Child).Text = rankArray[index];
                    ((TextBlock)rankFileList[index + 8].Child).Text = fileArrayRev[index];
                }
                else if (pRotation == -270)
                {
                    ((TextBlock)rankFileList[index].Child).Text = fileArrayRev[index];
                    ((TextBlock)rankFileList[index + 8].Child).Text = rankArrayRev[index];
                }
            }
        }


    }
}

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

using KaruahChess.Common;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace KaruahChess.CustomControl
{
    public sealed partial class AboutPage : UserControl
    {
        

        /// <summary>
        /// A class containing styling info for the control
        /// </summary>
        public CustomStyleTemplate StyleTemplate { get; set; }


        public AboutPage()
        {

            this.InitializeComponent();

            // Loads a default style template if one has not been set
            if (StyleTemplate == null)
            {
                StyleTemplate = (CustomStyleTemplate)CustomStyleDefaultResourceDictionary["CustomStyleTemplateDefaultObject"];
            }
            PagePopup.IsOpen = false;
        }

        /// <summary>
        /// Show the popup
        /// </summary>
        /// <returns></returns>
        public void Show()
        {

            // Set version text
            VersionText.Text = GetVersion();

            PagePopup.IsOpen = true;

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
        /// Close the popup
        /// </summary>        
        private void btnCloseButton_Click(object sender, RoutedEventArgs e)
        {
            PagePopup.IsOpen = false;
        }

        
        /// <summary>
        /// Get product version
        /// </summary>
        private string GetVersion()
        {
            var version = string.Format("Version: {0}.{1}.{2}.{3}",
                   Windows.ApplicationModel.Package.Current.Id.Version.Major,
                   Windows.ApplicationModel.Package.Current.Id.Version.Minor,
                   Windows.ApplicationModel.Package.Current.Id.Version.Build,
                   Windows.ApplicationModel.Package.Current.Id.Version.Revision);

            return version;
        }

        
        
    }
}

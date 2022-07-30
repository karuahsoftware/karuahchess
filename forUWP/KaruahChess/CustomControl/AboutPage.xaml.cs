using System;
using Windows.ApplicationModel;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;


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
                this.SetValue(Canvas.LeftProperty, 10);
                this.SetValue(Canvas.TopProperty, 10);
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
                    Package.Current.Id.Version.Major,
                    Package.Current.Id.Version.Minor,
                    Package.Current.Id.Version.Build,
                    Package.Current.Id.Version.Revision);

            return version;
        }

        
    }
}

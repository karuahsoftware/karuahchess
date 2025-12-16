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

using Microsoft.UI.Xaml.Controls;
using System;
using System.Threading.Tasks;
using Windows.ApplicationModel.DataTransfer;


namespace KaruahChess.CustomControl
{
    public sealed partial class MoveNavigatorPGNDialog : Page
    {

        bool closeDialog = false;

        /// <summary>
        /// Constructor
        /// </summary>
        public MoveNavigatorPGNDialog(string pPGNText)
        {
            this.InitializeComponent();
            ExportPGNTextBox.Text = pPGNText;
        }


        public ContentDialog CreateDialog()
        {
            var dialog = new ContentDialog
            {
                Content = this,
                PrimaryButtonText = "Close",
                SecondaryButtonText = "Copy to clipboard",
            };
            
            dialog.PrimaryButtonClick += Close_Click;
            dialog.SecondaryButtonClick += CopyToClipboard_Click;
            dialog.Closing += Dialog_Closing;

            return dialog;
        }

        /// <summary>
        /// Copy PGN to clipboard
        /// </summary>
        private async void CopyToClipboard_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            try
            {
                var text = ExportPGNTextBox?.Text ?? string.Empty;

                var data = new DataPackage
                {
                    RequestedOperation = DataPackageOperation.Copy
                };
                data.SetText(text);

                Clipboard.SetContent(data);
                Clipboard.Flush(); // keep content available after dialog/app closes

                // Show success message (bottom row of this dialog)
                var infoBar = new InfoBar
                {
                    Severity = InfoBarSeverity.Success,
                    IsOpen = true,
                    IsClosable = false,
                    Message = "Copied to clipboard."
                };
                Grid.SetRow(infoBar, 2);
                PageGrid.Children.Add(infoBar);

                await Task.Delay(2000);
                infoBar.IsOpen = false;
                PageGrid.Children.Remove(infoBar);
            }
            catch
            {
                // Swallow exceptions to avoid breaking the dialog flow.                
            }
        }

        /// <summary>
        /// Save on close
        /// </summary>
        private void Close_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            closeDialog = true;

            SenderDialog.PrimaryButtonClick -= Close_Click;
            SenderDialog.SecondaryButtonClick -= CopyToClipboard_Click;
            SenderDialog.Closing -= Dialog_Closing;
        }

        /// <summary>
        /// Dialog closing event
        /// </summary>
        private void Dialog_Closing(ContentDialog SenderDialog, ContentDialogClosingEventArgs args)
        {
            // Cancels the closing of the dialog unless the close button was pressed
            if (!closeDialog)
            {
                args.Cancel = true;
            }
        }

    }
}

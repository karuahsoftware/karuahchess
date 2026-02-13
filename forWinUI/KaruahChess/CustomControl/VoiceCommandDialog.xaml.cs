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

using Microsoft.UI.Xaml.Controls;
using System;



namespace KaruahChess.CustomControl
{
    public sealed partial class VoiceCommandDialog : Page
    {

        ViewModel.BoardViewModel _boardVM;

        /// <summary>
        /// Constructor
        /// </summary>
        public VoiceCommandDialog(ViewModel.BoardViewModel pBoardVM)
        {            
            this.InitializeComponent();
            _boardVM = pBoardVM;

            // Set initial values
            VoiceCommandEnabledCheckBox.IsChecked = _boardVM.VoiceCommandEnabled;
            
        }


        public ContentDialog CreateDialog()
        {
            var dialog = new ContentDialog
            {
                Content = this,
                PrimaryButtonText = "Close"
            };

            dialog.PrimaryButtonClick += Close_Click;

            return dialog;
        }

        

        /// <summary>
        /// Saves form values
        /// </summary>
        private void Save()
        {
            _boardVM.VoiceCommandEnabled = VoiceCommandEnabledCheckBox.IsChecked == true;
     
        }

        /// <summary>
        /// Save on close
        /// </summary>
        private void Close_Click(ContentDialog SenderDialog, ContentDialogButtonClickEventArgs DialogEventArgs)
        {
            Save();

            SenderDialog.PrimaryButtonClick -= Close_Click;
        }

        

    }
}

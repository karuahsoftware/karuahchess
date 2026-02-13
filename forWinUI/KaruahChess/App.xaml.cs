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

using Microsoft.UI.Xaml;
using KaruahChess.Database;
using KaruahChess.Common;
using System.IO.MemoryMappedFiles;
using Microsoft.UI.Xaml.Navigation;

namespace KaruahChess
{
    /// <summary>
    /// Provides application-specific behavior to supplement the default Application class.
    /// </summary>
    public partial class App : Application
    {
        private Window mainWindowRef;
        private int dbStatus;
        MemoryMappedFile instancemmf;

        /// <summary>
        /// Initializes the singleton application object.  This is the first line of authored code
        /// executed, and as such is the logical equivalent of main() or WinMain().
        /// </summary>
        public App()
        {
            this.InitializeComponent();
                       

            // Track instances created in a memory mapped file
            instancemmf = MemoryMappedFile.CreateOrOpen("karuahchessinstance",4);           
            MemoryMappedViewAccessor instanceAccessor = instancemmf.CreateViewAccessor();
            int instanceID = instanceAccessor.ReadInt32(0);
            int nextID = instanceID + 1;
            instanceAccessor.Write(0, nextID);

            
            // Create database if it does not exist and check it is operational            
            dbStatus = KaruahChessDB.Init(instanceID);
                                    

        }

        /// <summary>
        /// Invoked when the application is launched normally by the end user.  Other entry points
        /// will be used such as when the application is launched to open a specific file.
        /// </summary>
        /// <param name="args">Details about the launch request and process.</param>
        protected override void OnLaunched(Microsoft.UI.Xaml.LaunchActivatedEventArgs args)
        {
            if (dbStatus == KaruahChessDB.DB_OK)
            {
                mainWindowRef = new MainWindow();
            }
            else
            {
                helper.LogError(dbStatus);
                mainWindowRef = new ErrorWindow();
            }

            if (KaruahChessDB.instanceID > 0)
            {
                mainWindowRef.Title = $"{Application.Current.Resources["ApplicationTitle"]} - {KaruahChessDB.instanceID}";
            }
            else
            {
                mainWindowRef.Title = $"{Application.Current.Resources["ApplicationTitle"]}";
            }

            mainWindowRef.Activate();

        }

        

    }
}

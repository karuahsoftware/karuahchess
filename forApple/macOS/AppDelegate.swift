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

import SwiftUI

@MainActor class AppDelegate: NSObject, NSApplicationDelegate {
    let mainWindowController = MainWindowController()
    let menu = Menu()
    
    func applicationDidFinishLaunching(_ aNotification: Notification) {
        
        if let window = NSApplication.shared.windows.first {
            
            // Window settings
            window.center()
            window.isReleasedWhenClosed = false
            window.delegate = mainWindowController
        
            // Set up the title bar accessories
            let titleBarAccessoryView = TitlebarAccessoryView()
            let accessoryHostingView = NSHostingView(rootView: titleBarAccessoryView)
            accessoryHostingView .frame.size = accessoryHostingView.fittingSize
            
            let titlebarAccessory = NSTitlebarAccessoryViewController()
            titlebarAccessory.view = accessoryHostingView
            
            window.addTitlebarAccessoryViewController(titlebarAccessory)
            
            // Set the tile size
            Device.instance.tileSize = MainWindowController.getTileSize(pSize: window.contentLayoutRect.size)
        }
        
        NSApplication.shared.mainMenu = menu.make()
        
        
    }
    
    

    func applicationWillTerminate(_ aNotification: Notification) {
        // Insert code here to tear down your application
    }
    
    
    func applicationShouldTerminateAfterLastWindowClosed(_ sender: NSApplication) -> Bool {
        true
    }
    
    

}


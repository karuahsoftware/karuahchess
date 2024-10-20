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

class MainWindowController: NSWindowController, NSWindowDelegate {
    
    func windowDidResize(_ notification: Notification) {
        MainWindowController.refreshTileSize()
    }
    
    /// Refreshes the tile size
    static func refreshTileSize() {
        if let window = NSApplication.shared.windows.first {
            Device.instance.tileSize = MainWindowController.getTileSize(pSize: window.contentLayoutRect.size)
        }
    }
    
    ///  Gets the size of a tile based on the screen dimensions
    /// - Parameters:
    ///   - pSize: Screen dimensions
    ///   - pStatusBarHeight: The height of the status bar
    ///   - pNavBarHeight: The height of the navigation bar
    /// - Returns: The size the tile should be
    static func getTileSize(pSize: CGSize) -> CGFloat {
        let padding: CGFloat = 5
        let availableHeight = pSize.height - Device.instance.navigationHeight - Device.instance.boardCoordPadding
        let availableWidth = pSize.width - Device.instance.boardCoordPadding
        
        if availableHeight < availableWidth {
            return (availableHeight - padding) / 8
        }
        else {
            return (availableWidth - padding) / 8
        }
        
    }
}

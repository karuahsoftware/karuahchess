/*
Karuah Chess is a chess playing program
Copyright (C) 2020 Karuah Software

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
        if let window = NSApplication.shared.windows.first {
            Device.shared.tileSize = MainWindowController.getTileSize(pSize: window.contentLayoutRect.size)
            BoardViewModel.shared.pieceEditTool.Close() // Close the edit tool if it is open as it is not positioned correctly when window resizes
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
        if pSize.height < pSize.width {
            return (pSize.height - padding) / 8
        }
        else {
            return (pSize.width - padding) / 8
        }
    }
}

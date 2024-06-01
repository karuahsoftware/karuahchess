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

class Menu : NSObject {
    @ObservedObject var menuSettingsVM : MenuSettingsViewModel = MenuSettingsViewModel()
    
    @objc func newGame() {
        let action : ()->Void = {
            Task(priority: .userInitiated) {
                await BoardViewModel.instance.newGame()
            }
        }
        BoardViewModel.instance.boardMessageAlertVM.show("Start a new game?", "", BoardMessageAlertViewModel.alertTypeEnum.YesNo, action)
    }
    
    @objc func resign() {
        let action : ()->Void = {
            Task(priority: .userInitiated) {
                await BoardViewModel.instance.resignGame()
            }
        }
        BoardViewModel.instance.boardMessageAlertVM.show("Resign from current game?", "", BoardMessageAlertViewModel.alertTypeEnum.YesNo, action)
    }
    
    @objc func engineSettings() {
        MenuSheet.shared.active = .engineSettings
    }
    
    @objc func coordinates(_ sender: NSMenuItem) {
        menuSettingsVM.coordinatesEnabled.toggle()
        sender.state = menuSettingsVM.coordinatesEnabled ? .on : .off
    }
    
    @objc func highlightMoves(_ sender: NSMenuItem) {
        menuSettingsVM.moveHighlightEnabled.toggle()
        sender.state = menuSettingsVM.moveHighlightEnabled ? .on : .off
    }
    
    
    @objc func hint(_ sender: NSMenuItem) {
        menuSettingsVM.hintEnabled.toggle()
        sender.state = menuSettingsVM.hintEnabled ? .on : .off
    }
     
    
    @objc func navigator(_ sender: NSMenuItem) {
        menuSettingsVM.navigatorEnabled.toggle()
        sender.state = menuSettingsVM.navigatorEnabled ? .on : .off
    }
    
    @objc func boardSettings() {
        MenuSheet.shared.active = .boardSettings
    }
    
    @objc func pieceSettings() {
        MenuSheet.shared.active = .pieceSettings
    }
    
    @objc func soundSettings() {
        MenuSheet.shared.active = .soundSettings
    }
    
    @objc func edit(_ sender: NSMenuItem) {
        menuSettingsVM.arrangeBoardEnabled.toggle()
        sender.state = menuSettingsVM.arrangeBoardEnabled ? .on : .off
    }
    
    @objc func undo() {
        Task(priority: .userInitiated) {
         await BoardViewModel.instance.undoMove()
        }
    }
    
    @objc func switchDirection() {
        Task(priority: .userInitiated) {
        await BoardViewModel.instance.switchDirection()
        }
    }
    
    @objc func loadGame() {
        BoardViewModel.instance.showFileImporter = true
    }
    
    @objc func saveGame() {
        Task(priority: .userInitiated) {
            await BoardViewModel.instance.saveGame()
        }
    }
    
    @objc func about() {
        MenuSheet.shared.active = .about
    }
    
    @objc func showWindow() {
        if let window = NSApplication.shared.windows.first {
            window.makeKeyAndOrderFront(nil)
        }
    }
    
    
    @objc func quit() {
        NSApplication.shared.terminate(self)
    }
    
    func make() -> NSMenu {
        let mainMenu = NSMenu()
        let mainAppMenu = NSMenuItem(title: "Karuah Chess", action: nil, keyEquivalent: "")
        mainMenu.addItem(mainAppMenu)
        
        let appMenu = NSMenu()
        mainAppMenu.submenu = appMenu
        
        // New Game
        do {
            let menuItem = NSMenuItem(title: "New Game", action: #selector(self.newGame), keyEquivalent: "n")
            menuItem.image = NSImage(systemSymbolName: "target", accessibilityDescription: "New Game")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Resign
        do {
            let menuItem = NSMenuItem(title: "Resign", action: #selector(self.resign), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "flag", accessibilityDescription: "Resigm")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Engine Settings
        do {
            let menuItem = NSMenuItem(title: "Engine Settings", action: #selector(self.engineSettings), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "gear", accessibilityDescription: "Engine Settings")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        appMenu.addItem(NSMenuItem.separator())
        
        // Coordinates
        do {
            let menuItem = NSMenuItem(title: "Coordinates", action: #selector(self.coordinates), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "globe", accessibilityDescription: "Coordinates")
            menuItem.target = self
            menuItem.state = menuSettingsVM.coordinatesEnabled ? .on : .off
            appMenu.addItem(menuItem)
            
        }
        
        // Highlight Moves
        do {
            let menuItem = NSMenuItem(title: "Highlight Moves", action: #selector(self.highlightMoves), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "circle.hexagonpath", accessibilityDescription: "Highlight Moves")
            menuItem.target = self
            menuItem.state = menuSettingsVM.moveHighlightEnabled ? .on : .off
            appMenu.addItem(menuItem)
            
        }
        
        // Navigator
        do {
            let menuItem = NSMenuItem(title: "Navigator", action: #selector(self.navigator), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "play", accessibilityDescription: "Navigator")
            menuItem.target = self
            menuItem.state = menuSettingsVM.navigatorEnabled ? .on : .off
            appMenu.addItem(menuItem)
            
        }
        
        // Hint
        do {
            let menuItem = NSMenuItem(title: "Hint Button", action: #selector(self.hint), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "lightbulb", accessibilityDescription: "Hint")
            menuItem.target = self
            menuItem.state = menuSettingsVM.hintEnabled ? .on : .off
            appMenu.addItem(menuItem)
            
        }
        
        appMenu.addItem(NSMenuItem.separator())
        
        // Board settings
        do {
            let menuItem = NSMenuItem(title: "Board", action: #selector(self.boardSettings), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "square.grid.3x3.square", accessibilityDescription: "Board")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Piece settings
        do {
            let menuItem = NSMenuItem(title: "Piece", action: #selector(self.pieceSettings), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "person", accessibilityDescription: "Piece")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Sound settings
        do {
            let menuItem = NSMenuItem(title: "Sound", action: #selector(self.soundSettings), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "music.note", accessibilityDescription: "Sound")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        appMenu.addItem(NSMenuItem.separator())
        
        // Edit board
        do {
            let menuItem = NSMenuItem(title: "Edit", action: #selector(self.edit), keyEquivalent: "e")
            menuItem.image = NSImage(systemSymbolName: "pencil", accessibilityDescription: "Edit")
            menuItem.target = self
            menuItem.state = menuSettingsVM.arrangeBoardEnabled ? .on : .off
            appMenu.addItem(menuItem)
        }
        
        // Undo
        do {
            let menuItem = NSMenuItem(title: "Undo", action: #selector(self.undo), keyEquivalent: "z")
            menuItem.image = NSImage(systemSymbolName: "arrow.uturn.left", accessibilityDescription: "Undo")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Switch direction
        do {
            let menuItem = NSMenuItem(title: "Switch Direction", action: #selector(self.switchDirection), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "arrow.up.arrow.down", accessibilityDescription: "Switch Direction")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Load game
        do {
            let menuItem = NSMenuItem(title: "Load Game", action: #selector(self.loadGame), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "doc", accessibilityDescription: "Load Game")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Save game
        do {
            let menuItem = NSMenuItem(title: "Save Game", action: #selector(self.saveGame), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "opticaldiscdrive", accessibilityDescription: "Save Game")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        appMenu.addItem(NSMenuItem.separator())
        
        // About
        do {
            let menuItem = NSMenuItem(title: "About", action: #selector(self.about), keyEquivalent: "")
            menuItem.image = NSImage(systemSymbolName: "questionmark", accessibilityDescription: "About")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        appMenu.addItem(NSMenuItem.separator())
        
        // Show main window
        do {
            let menuItem = NSMenuItem(title: "Show Main Window", action: #selector(self.showWindow), keyEquivalent: "w")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        // Quit application
        do {
            let menuItem = NSMenuItem(title: "Quit", action: #selector(self.quit), keyEquivalent: "q")
            menuItem.target = self
            appMenu.addItem(menuItem)
        }
        
        return mainMenu
    }
}

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

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?
    var navController: UINavigationController?
    
    
    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
        // Create the SwiftUI view that provides the window contents.
        let contentView = ContentView()
        
        
        // Use a UIHostingController as window root view controller.
        if let windowScene = scene as? UIWindowScene {
            let window = UIWindow(windowScene: windowScene)
            
            let rootViewController = UIHostingController(rootView: contentView)
            navController = UINavigationController(rootViewController: rootViewController)
            window.rootViewController = navController
                
            self.window = window
            window.makeKeyAndVisible()
            
            // Set the navigation bar height variable
            let navBarHeight: CGFloat = navController?.navigationBar.frame.height ?? 0
            
            // Set initial tile size
            let size = windowScene.screen.bounds.size
            let statusBarHeight: CGFloat = windowScene.statusBarManager?.statusBarFrame.height ?? 0
            let keyWindow = UIApplication.shared.connectedScenes
                .filter({$0.activationState == .foregroundActive})
                .compactMap({$0 as? UIWindowScene})
                .first?.windows
                .filter({$0.isKeyWindow}).first
            
            let safearea: CGFloat = (keyWindow?.safeAreaInsets.top ?? 0) + (keyWindow?.safeAreaInsets.bottom ?? 0)
            Device.instance.tileSize = SceneDelegate.getTileSize(pSize: size, pStatusBarHeight: statusBarHeight, pNavBarHeight: navBarHeight, pSafeArea: safearea)
            Device.instance.isLandScape = size.width > size.height
            
        }
        

    }
    
    func windowScene(_ windowScene: UIWindowScene,
                     didUpdate previousCoordinateSpace: UICoordinateSpace,
                     interfaceOrientation previousInterfaceOrientation: UIInterfaceOrientation,
                     traitCollection previousTraitCollection: UITraitCollection) {
        
        // Update size on screen rotate
        if let navBarHeight: CGFloat = navController?.navigationBar.frame.height
        {
            let size = windowScene.screen.bounds.size
            let statusBarHeight: CGFloat = windowScene.statusBarManager?.statusBarFrame.height ?? 0
            
            let keyWindow = UIApplication.shared.connectedScenes
                .filter({$0.activationState == .foregroundActive})
                .compactMap({$0 as? UIWindowScene})
                .first?.windows
                .filter({$0.isKeyWindow}).first
            
            let safearea: CGFloat = (keyWindow?.safeAreaInsets.top ?? 0) + (keyWindow?.safeAreaInsets.bottom ?? 0)
            Device.instance.tileSize = SceneDelegate.getTileSize(pSize: size, pStatusBarHeight: statusBarHeight, pNavBarHeight: navBarHeight, pSafeArea: safearea)
            Device.instance.isLandScape = size.width > size.height
        }
    }

    
    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not neccessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }
    
    /// Refreshes the tile size
    static func refreshTileSize() {
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
            let size = windowScene.screen.bounds.size
            let statusBarHeight: CGFloat = windowScene.statusBarManager?.statusBarFrame.height ?? 0
            
            
            let keyWindow = UIApplication.shared.connectedScenes
                .filter({$0.activationState == .foregroundActive})
                .compactMap({$0 as? UIWindowScene})
                .first?.windows
                .filter({$0.isKeyWindow}).first
            
            let safearea = (keyWindow?.safeAreaInsets.top ?? 0) + (keyWindow?.safeAreaInsets.bottom ?? 0)
            
            if let navController = keyWindow?.rootViewController as? UINavigationController {
                let navBarHeight = navController.navigationBar.frame.height
                Device.instance.tileSize = SceneDelegate.getTileSize(pSize: size, pStatusBarHeight: statusBarHeight, pNavBarHeight: navBarHeight, pSafeArea: safearea)
                Device.instance.isLandScape = size.width > size.height
            }
        }
    }
    
    ///  Gets the size of a tile based on the screen dimensions
    /// - Parameters:
    ///   - pSize: Screen dimensions
    ///   - pStatusBarHeight: The height of the status bar
    ///   - pNavBarHeight: The height of the navigation bar
    /// - Returns: The size the tile should be
    static func getTileSize(pSize: CGSize, pStatusBarHeight: CGFloat, pNavBarHeight: CGFloat, pSafeArea: CGFloat) -> CGFloat {
        let availableHeight = pSize.height - Device.instance.navigationHeight - Device.instance.boardCoordPadding - (pStatusBarHeight + pNavBarHeight + pSafeArea)
        let availableWidth = pSize.width - Device.instance.boardCoordPadding
        
        
        if availableHeight < availableWidth {
            return availableHeight / 8
        }
        else {
            return availableWidth / 8
        }
    }
    
    
}


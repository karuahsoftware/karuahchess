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


struct TileView: View {
    let index : Int
    
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var tileVM : TileViewModel = TileViewModel()
    @State private var highlightOpacity: Double = 0.0
    @State private var checkIndicatorOpacity: Double = 0.0
    @State private var shakeComplete: CGFloat = 0
    @State private var highLightFullFadeOutComplete: Double = 0
    
    var body: some View {
            let fadeOutModifier = TileFadeOutModifier(complete: highLightFullFadeOutComplete)
        
            ZStack {
                
                // Edit highlight
                if tileVM.highlightEdit {
                   Rectangle()
                    .fill(Color("Magenta"))
                    .frame(width: device.tileSize - 1, height: device.tileSize - 1)
                }
                
                // Full highlight magenta
                if tileVM.highlightFull {
                   Rectangle()
                    .fill(tileVM.hightlightFullColor)
                    .frame(width: device.tileSize - 1, height: device.tileSize - 1)
                }
                
                
                // Full highlight magenta with fade out
                if tileVM.highlightFullFadeOut && fadeOutModifier.animatableData < 1 {
                   Rectangle()
                    .fill(tileVM.hightlightFullColor)
                    .frame(width: device.tileSize - 1, height: device.tileSize - 1)
                    .modifier(fadeOutModifier)
                    .onAppear() {
                        withAnimation(.easeIn(duration: 5)) {
                            highLightFullFadeOutComplete = 1
                        }
                    }
                    .onDisappear() {
                        tileVM.highlightFullFadeOut = false
                        highLightFullFadeOutComplete = 0
                    }
                }
                
                // Highlight path
                if tileVM.highlight {
                    
                    if tileVM.spin == 0 {
                        Circle()
                            .fill(Color("DarkGreen"))
                            .frame(width: device.tileSize * 0.3, height: device.tileSize * 0.3)
                            .opacity(highlightOpacity)
                            .onAppear() {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    highlightOpacity = 1.0
                                }
                            }
                            .onDisappear() {
                                highlightOpacity = 0.0
                            }
                    }
                    else {
                        Circle()
                            .fill(Color("DarkGreen"))
                            .frame(width: device.tileSize * 0.95, height: device.tileSize * 0.95)
                            .opacity(highlightOpacity)
                            .onAppear() {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    highlightOpacity = 1.0
                                }
                            }
                            .onDisappear() {
                                highlightOpacity = 0.0
                            }
                    }
                }
                
                // Check indicator
                if tileVM.checkIndicator {
                    Circle()
                    .fill(Color("Red"))
                    .frame(width: device.tileSize * 0.95, height: device.tileSize * 0.95)
                    .opacity(checkIndicatorOpacity)
                    .onAppear() {
                        withAnimation(.easeInOut(duration: 0.4)) {
                            checkIndicatorOpacity = 0.4
                        }
                    }
                    .onDisappear() {
                        checkIndicatorOpacity = 0.0
                    }
                }
                
                
                
                // Piece
                GeometryReader {geo in
                   if !tileVM.pieceName.isEmpty && tileVM.visible {
                       
                        Image(tileVM.pieceName)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: device.tileSize, height: device.tileSize)
                        .clipped()
                        .preference(
                           key: TileFrameBoardPreferenceKey.self,
                           value: [TileFrameBoardPreference(tileIndex: index, tileFrame: geo.frame(in: .named("BoardCoordinateSpace")))]
                        )
                        .preference(
                               key: TileFrameMainPreferenceKey.self,
                               value: [TileFrameMainPreference(tileIndex: index, tileFrame: geo.frame(in: .named("MainCoordinateSpace")))]
                        )
                        .onDrag{
                            return NSItemProvider(object: String(index) as NSString)
                        }
                        .modifier(TileShakeEffect(complete: shakeComplete))
                        .onAppear() {
                            withAnimation(tileVM.editMode ? Animation.easeIn(duration: 0.1).repeatForever(autoreverses: true) : Animation.linear(duration: 0)) {
                                shakeComplete = tileVM.editMode ? 1.0 : 0.0
                            }
                        }
                        .onChange(of: tileVM.editMode) { _, newValue in
                            withAnimation(newValue ? Animation.easeIn(duration: 0.1).repeatForever(autoreverses: true) : Animation.linear(duration: 0)) {
                               shakeComplete = newValue ? 1.0 : 0.0
                            }
                        }
                            
 
                    }
                    else {
                        Spacer()
                        .frame(width: device.tileSize, height: device.tileSize)
                        .preference(
                           key: TileFrameBoardPreferenceKey.self,
                           value: [TileFrameBoardPreference(tileIndex: index, tileFrame: geo.frame(in: .named("BoardCoordinateSpace")))]
                        )
                        .preference(
                            key: TileFrameMainPreferenceKey.self,
                            value: [TileFrameMainPreference(tileIndex: index, tileFrame: geo.frame(in: .named("MainCoordinateSpace")))]
                        )
                    }
                    
                }.frame(width: device.tileSize, height: device.tileSize)
                
                
            }.background(getTileBackgroundColour(pIndex: index))
            .onTapGesture {
                BoardViewModel.instance.onTileClick(pTile: self)
            }
            .onDrop(of: ["public.utf8-plain-text"], delegate: PieceMoveDelegate(toIndex: index, highLightColour: Color("Magenta")))
            .rotationEffect(Angle(degrees: -BoardViewModel.instance.boardRotation))
            
            
     }
    
    
    /// Gets the board square index colour
    /// - Parameter pIndex: Index of square
    /// - Returns: Tile background colour
    func getTileBackgroundColour(pIndex: Int) -> Color {
        
        if((pIndex + 1) % 2 == 0)
        {
            if((pIndex >= 0 && pIndex <= 7) || (pIndex >= 16 && pIndex <= 23) || (pIndex >= 32 && pIndex <= 39) || (pIndex >= 48 && pIndex <= 55) ){
                return device.tileDarkSquareColour
            }
            else {
                return Color("TileWhite")
            }
        }
        else {
            if((pIndex >= 0 && pIndex <= 7) || (pIndex >= 16 && pIndex <= 23) || (pIndex >= 32 && pIndex <= 39) || (pIndex >= 48 && pIndex <= 55) ){
                return Color("TileWhite")
            }
            else {
                return device.tileDarkSquareColour
            }
        }
        
    }
    
    
    /// Drop delegate for drag and drop functionality
   struct PieceMoveDelegate: DropDelegate {
        var toIndex: Int
        var highLightColour: Color
       
        func validateDrop(info: DropInfo) -> Bool {
            return info.hasItemsConforming(to: ["public.utf8-plain-text"])
        }
        
        func dropEntered(info: DropInfo) {
          BoardViewModel.instance.tilePanelVM.setHighLightFull(pBits: Constants.BITMASK >> toIndex, pColour: highLightColour)
        }
        
        func dropExited(info: DropInfo) {
           BoardViewModel.instance.tilePanelVM.setHighLightFull(pBits: 0, pColour: highLightColour)
        }
        
        func dropUpdated(info: DropInfo) -> DropProposal? {
            return DropProposal(operation: .move)
        }
        
       
        func performDrop(info: DropInfo) -> Bool {
            if let itemProvider = info.itemProviders(for: ["public.utf8-plain-text"]).first {
                itemProvider.loadItem(forTypeIdentifier: "public.utf8-plain-text", options: nil) { item, error in
                    if let data = item as? Data {
                        if let fromIndex = Int(String(decoding: data, as: UTF8.self)) {
                            Task(priority: .userInitiated) {
                                let fromTile = await BoardViewModel.instance.tilePanelVM.getTile(pIndex: fromIndex)
                                let toTile = await BoardViewModel.instance.tilePanelVM.getTile(pIndex: toIndex)
                                await BoardViewModel.instance.onTileMoveAction(pFromTile: fromTile, pToTile: toTile)
                            }
                        }
                    }
                }
            }
            
            BoardViewModel.instance.tilePanelVM.setHighLightFull(pBits: 0, pColour: highLightColour)
        
                
            return true
        }
        
    }

    
}



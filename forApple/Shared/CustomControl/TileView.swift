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


struct TileView: View {
    let index : Int
    
    @StateObject public var device : Device = Device.shared
    @ObservedObject var tileVM : TileViewModel = TileViewModel()
    @State private var highlightOpacity: Double = 0.0
    @State private var checkIndicatorOpacity: Double = 0.0
    @State private var shakeComplete: CGFloat = 0
    @State private var highLightFullFadeOutComplete: Double = 0
    
    var body: some View {
            let fadeOutModifier = TileFadeOutModifier(complete: highLightFullFadeOutComplete)
        
            ZStack {
                
                // Full highlight magenta
                if self.tileVM.highlightFull {
                   Rectangle()
                    .fill(Color("Magenta"))
                    .frame(width: self.device.tileSize, height: self.device.tileSize)
                }
                
                // Full highlight magenta with fade out
                if tileVM.highlightFullFadeOut && fadeOutModifier.animatableData < 1 {
                   Rectangle()
                    .fill(Color("Magenta"))
                    .frame(width: self.device.tileSize - 1, height: self.device.tileSize - 1)
                    .modifier(fadeOutModifier)
                    .onAppear() {
                        withAnimation(.easeIn(duration: 3)) {
                            highLightFullFadeOutComplete = 1
                        }
                    }
                    .onDisappear() {
                        tileVM.highlightFullFadeOut = false
                        highLightFullFadeOutComplete = 0
                    }
                }
                
                 
                
                // Highlight path
                if self.tileVM.highlight {
                    
                    if self.tileVM.spin == 0 {
                        Circle()
                            .fill(Color("DarkGreen"))
                            .frame(width: self.device.tileSize * 0.3, height: self.device.tileSize * 0.3)
                            .opacity(self.highlightOpacity)
                            .onAppear() {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    self.highlightOpacity = 1.0
                                }
                            }
                            .onDisappear() {
                                self.highlightOpacity = 0.0
                            }
                    }
                    else {
                        Circle()
                            .fill(Color("DarkGreen"))
                            .frame(width: self.device.tileSize * 0.95, height: self.device.tileSize * 0.95)
                            .opacity(self.highlightOpacity)
                            .onAppear() {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    self.highlightOpacity = 1.0
                                }
                            }
                            .onDisappear() {
                                self.highlightOpacity = 0.0
                            }
                    }
                }
                
                // Check indicator
                if self.tileVM.checkIndicator {
                    Circle()
                    .fill(Color("Red"))
                    .frame(width: self.device.tileSize * 0.95, height: self.device.tileSize * 0.95)
                    .opacity(self.checkIndicatorOpacity)
                    .onAppear() {
                        withAnimation(.easeInOut(duration: 0.4)) {
                            self.checkIndicatorOpacity = 0.4
                        }
                    }
                    .onDisappear() {
                        self.checkIndicatorOpacity = 0.0
                    }
                }
                
                
                
                // Piece
                GeometryReader {geo in
                   if !self.tileVM.pieceName.isEmpty && self.tileVM.visible {
                    
                        Image(self.tileVM.pieceName)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: self.device.tileSize, height: self.device.tileSize)
                        .clipped()
                        .preference(
                           key: TileFramePreferenceKey.self,
                           value: [TileFramePreference(tileIndex: self.index, tileFrame: geo.frame(in: .named("BoardCoordinateSpace")))]
                        )
                        .onDrag{
                            return NSItemProvider(object: String(self.index) as NSString)
                        }
                        .modifier(TileShakeEffect(complete: shakeComplete))
                        .onAppear() {
                            withAnimation(tileVM.editMode ? Animation.easeIn(duration: 0.1).repeatForever(autoreverses: true) : Animation.linear(duration: 0)) {
                                shakeComplete = tileVM.editMode ? 1.0 : 0.0
                            }
                        }
                        .onChange(of: tileVM.editMode) {newValue in
                            withAnimation(newValue ? Animation.easeIn(duration: 0.1).repeatForever(autoreverses: true) : Animation.linear(duration: 0)) {
                               shakeComplete = newValue ? 1.0 : 0.0
                            }
                        }
                            
 
                    }
                    else {
                        Spacer()
                        .frame(width: self.device.tileSize, height: self.device.tileSize)
                        .preference(
                           key: TileFramePreferenceKey.self,
                           value: [TileFramePreference(tileIndex: self.index, tileFrame: geo.frame(in: .named("BoardCoordinateSpace")))]
                        )                                                              
                    }
                    
                }.frame(width: self.device.tileSize, height: self.device.tileSize)
                
                
            }.background(self.getTileBackgroundColour(pIndex: self.index))
            .onTapGesture {
                BoardViewModel.shared.onTileClick(pTile: self)
            }
            .onDrop(of: ["public.utf8-plain-text"], delegate: PieceMoveDelegate(toIndex: index))
            .rotationEffect(Angle(degrees: -BoardViewModel.shared.boardRotation))
            
            
     }
    
    
    /// Gets the board square index colour
    /// - Parameter pIndex: Index of square
    /// - Returns: Tile background colour
    func getTileBackgroundColour(pIndex: Int) -> Color {
        
        if((pIndex + 1) % 2 == 0)
        {
            if((pIndex >= 0 && pIndex <= 7) || (pIndex >= 16 && pIndex <= 23) || (pIndex >= 32 && pIndex <= 39) || (pIndex >= 48 && pIndex <= 55) ){
                return Color("TileBlack")
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
                return Color("TileBlack")
            }
        }
        
    }
    
    
    /// Drop delegate for drag and drop functionality
    struct PieceMoveDelegate: DropDelegate {
        var toIndex: Int
        
        
        func validateDrop(info: DropInfo) -> Bool {
            return info.hasItemsConforming(to: ["public.utf8-plain-text"])
        }
        
        func dropEntered(info: DropInfo) {
            BoardViewModel.shared.boardLayout.setHighLightFull(pBits: Constants.BITMASK >> toIndex)
        }
        
        func dropExited(info: DropInfo) {
            BoardViewModel.shared.boardLayout.setHighLightFull(pBits: 0)
        }
        
        func dropUpdated(info: DropInfo) -> DropProposal? {
            return DropProposal(operation: .move)
        }
        
        func performDrop(info: DropInfo) -> Bool {
            if let itemProvider = info.itemProviders(for: ["public.utf8-plain-text"]).first {
                itemProvider.loadItem(forTypeIdentifier: "public.utf8-plain-text", options: nil) { item, error in
                    if let data = item as? Data {
                        if let fromIndex = Int(String(decoding: data, as: UTF8.self)) {
                            let fromTile = BoardViewModel.shared.boardLayout.getTile(pIndex: fromIndex)
                            let toTile = BoardViewModel.shared.boardLayout.getTile(pIndex: toIndex)
                            BoardViewModel.shared.onTileMoveAction(pFromTile: fromTile, pToTile: toTile)
                        }
                    }
                }
            }
            
            BoardViewModel.shared.boardLayout.setHighLightFull(pBits: 0)
          
            return true
        }
        
    }

    
}



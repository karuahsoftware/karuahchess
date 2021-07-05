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

struct TileAnimationView: View {
    
    @StateObject private var device : Device = Device.shared
    @ObservedObject var tileAnimationVM : TileAnimationViewModel = TileAnimationViewModel()
    @State private var complete: [CGFloat] = [CGFloat](repeating: 0, count: 10)
    
    
    
    var body: some View {
        ZStack { [self] in
            
            if tileAnimationVM.visible {
                
                ForEach(tileAnimationVM.animationInstructionArray.indices) {index in
                        let animInstruct = tileAnimationVM.animationInstructionArray[index]
                        if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Take {
                           // Take animation
                            Group {
                                animInstruct.imageData
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .modifier(TileAnimationTakeModifier(complete: complete[index]))
                                .rotationEffect(Angle(degrees: -BoardViewModel.shared.boardRotation))
                                .onAppear {
                                    complete[index] = 0
                                    withAnimation(.easeInOut(duration: tileAnimationVM.duration)) {
                                        complete[index] = 1
                                    }
                                }
                            }
                            .frame(width: device.tileSize, height: device.tileSize)
                            .position(animInstruct.moveFrom)
                                
                        }
                        else if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Fall {
                           // Fall animation
                            Group {
                                animInstruct.imageData
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .modifier(TileFallEffect(complete: complete[index]))
                                .rotationEffect(Angle(degrees: -BoardViewModel.shared.boardRotation))
                                .onAppear {
                                    complete[index] = 0
                                    withAnimation(.easeIn(duration: tileAnimationVM.duration)) {
                                        complete[index] = 1
                                     }
                                    
                                 }
 
                            }
                            .frame(width: device.tileSize, height: device.tileSize)
                            .position(animInstruct.moveFrom)
                            
                        }
                        else if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Put {
                            // Put animation
                            Group {
                                animInstruct.imageData
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .modifier(TileAnimationPutModifier(complete: complete[index]))
                                .rotationEffect(Angle(degrees: -BoardViewModel.shared.boardRotation))
                                .onAppear {
                                    complete[index] = 0
                                    withAnimation(.easeInOut(duration: tileAnimationVM.duration)) {
                                        complete[index] = 1
                                    }
                                }
                            }
                            .frame(width: device.tileSize, height: device.tileSize)
                            .position(animInstruct.moveFrom)
                        }
                        else if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Move {
                            // Move animation
                            Group {
                                animInstruct.imageData
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .rotationEffect(Angle(degrees: -BoardViewModel.shared.boardRotation))
                                .modifier(TileAnimationMoveModifier(complete: complete[index], moveFrom: animInstruct.moveFrom, moveTo: animInstruct.moveTo))
                                .onAppear {
                                    complete[index] = 0
                                    withAnimation(.easeInOut(duration: tileAnimationVM.duration)) {
                                        complete[index] = 1
                                    }
                                }
                            }
                            .frame(width: device.tileSize, height: device.tileSize)
                            .position(animInstruct.moveFrom)
                        }
                    
                }
                
            }
            Spacer()
            
        }.frame(width: device.tileSize * 8, height: device.tileSize * 8)
            
    }
    
    
   
    
}

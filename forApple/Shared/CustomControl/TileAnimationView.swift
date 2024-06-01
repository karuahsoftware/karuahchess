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

struct TileAnimationView: View {
    
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var tileAnimationVM : TileAnimationViewModel
    @State private var complete: [CGFloat] = [CGFloat](repeating: 0, count: 10)
    
    var body: some View {
        
        ZStack {

            if tileAnimationVM.visible {
                
                ForEach(tileAnimationVM.animationInstructionArray.indices, id: \.self) {index in
                    
                        let animInstruct = tileAnimationVM.animationInstructionArray[index]
                
                        // Animation
                        animInstruct.imageData
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .modify {
                            if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Fall {
                                $0.modifier(TileFallEffect(complete: complete[index]))
                            }
                            else {
                                $0
                            }
                        }
                        .rotationEffect(Angle(degrees: -BoardViewModel.instance.boardRotation))
                        .modify {
                            if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Move {
                                $0.modifier(TileAnimationMoveModifier(complete: complete[index], moveFrom: animInstruct.moveFrom, moveTo: animInstruct.moveTo))
                            }
                            else if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Take {
                                $0.modifier(TileAnimationTakeModifier(complete: complete[index]))
                            }
                            else if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.Put {
                                $0.modifier(TileAnimationPutModifier(complete: complete[index]))
                            }
                            else if animInstruct.animationType == TileAnimationInstruction.AnimationTypeEnum.MoveFade {
                                $0.modifier(TileAnimationMoveFadeModifier(complete: complete[index], moveFrom: animInstruct.moveFrom, moveTo: animInstruct.moveTo))
                            }
                            else {
                                $0
                            }
                        }
                        .frame(width: device.tileSize, height: device.tileSize)
                        .position(animInstruct.moveFrom)
                        .onReceive(tileAnimationVM.$visible) { visible in
                            if visible {
                                complete[index] = 0
                                withAnimation(.easeInOut(duration: tileAnimationVM.duration)) {
                                    complete[index] = 1
                                }
                            }
                            else {
                                complete[index] = 0
                            }
                        }
                        .onAnimationCompleted(for: complete[index]) {
                            tileAnimationVM.waiter?.resume()
                            tileAnimationVM.waitCount = 0
                            tileAnimationVM.waiter = nil
                        }
                }
            }
 
            Spacer()
            
        }.frame(width: device.tileSize * 8, height: device.tileSize * 8)
    
    }
    
}




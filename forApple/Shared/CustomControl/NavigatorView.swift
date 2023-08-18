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

struct NavigatorView: View {
    @ObservedObject private var device : Device = Device.instance
    @ObservedObject var navigatorVM : NavigatorViewModel
    @ObservedObject var boardVM : BoardViewModel
    @ObservedObject private var boardSquareDS = BoardSquareDataService.instance
    
    
    
    var body: some View {
        
        #if os(iOS)
        let buttonSize: CGFloat = 45
        let chevronWidth: CGFloat = 32
        #elseif os(macOS)
        let buttonSize: CGFloat = 30
        let chevronWidth: CGFloat = 21
        #endif
        
        ScrollViewReader { scrollView in
            if boardSquareDS.gameRecordCurrentValue > 0 {
                HStack(alignment: .top, spacing: 1) {
                    
                    // Left Scroll
                    Button(action: {
                        Task(priority: .userInitiated) {
                            await boardVM.navigateGameRecord(pRecId: boardSquareDS.gameRecordCurrentValue - 1, pAnimate: true)
                            withAnimation {
                                scrollView.scrollTo(boardSquareDS.gameRecordCurrentValue)
                            }
                            
                        }
                    }, label: {
                        Image(systemName: "chevron.left")
                            .foregroundColor(Color("NavigationText").opacity(0.6))
                            .frame(width: chevronWidth, height: buttonSize)
                            .background(Color("NavigationScroll"))
                    }).buttonStyle(PlainButtonStyle())
                    
                    // Record buttons
                    ScrollView(.horizontal, showsIndicators: false) {
                        LazyHStack(alignment: .top, spacing: 1) {
                            ForEach(navigatorVM.recordIdList, id: \.self) {recordId in
                                Button(action: {
                                    Task(priority: .userInitiated) {
                                        let currentRecordID = BoardSquareDataService.instance.gameRecordCurrentValue
                                        let animate = abs(currentRecordID - recordId) == 1 ? true : false
                                        await boardVM.navigateGameRecord(pRecId: recordId, pAnimate: animate)
                                    }
                                }, label: {
                                    Text("\(recordId)")
                                        .frame(width: buttonSize, height: buttonSize)
                                        .modify {
                                            if recordId == boardSquareDS.gameRecordCurrentValue {
                                                $0.background(Color("NavigationHighlight"))
                                            }
                                            else {
                                                $0.background(Color("NavigationScroll").opacity(0.9))
                                                  .foregroundColor(Color("NavigationText")
                                                  .opacity(0.6))
                                            }
                                        }
                                        .id(recordId)
                                })
                                .buttonStyle(PlainButtonStyle())
                                
                            }
                        }
                       
                    }.frame(minHeight: 40)
                    
                    // Scroll right button
                    Button(action: {
                        Task(priority: .userInitiated) {
                            await boardVM.navigateGameRecord(pRecId: boardSquareDS.gameRecordCurrentValue + 1, pAnimate: true)
                            withAnimation {
                                scrollView.scrollTo(boardSquareDS.gameRecordCurrentValue)
                            }
                        }
                        
                    }, label: {
                        Image(systemName: "chevron.right")
                            .foregroundColor(Color("NavigationText").opacity(0.6))
                            .frame(width: chevronWidth, height: buttonSize)
                            .background(Color("NavigationScroll"))
                    }).buttonStyle(PlainButtonStyle())
                }
                .onChange(of: boardSquareDS.gameRecordCurrentValue)  {newValue in
                    Task(priority: .userInitiated) {
                        try? await Task.sleep(nanoseconds: UInt64(0.1) * 1000000)
                        withAnimation {
                            scrollView.scrollTo(newValue)
                        }
                    }
                }
                .onAppear() {
                    Task(priority: .userInitiated) {
                        try? await Task.sleep(nanoseconds: UInt64(0.1) * 1000000)
                        withAnimation {
                            scrollView.scrollTo(boardSquareDS.gameRecordCurrentValue)
                        }
                    }
                }
 
                
      }
    }
  }
}


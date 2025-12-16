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
    
    @State private var showPGNSheet: Bool = false
    @State private var pgnText: String = ""
    @State private var navItems: [NavItem] = []
    @State private var copyConfirmed: Bool = false
    
    private enum NavItemKind {
        case label(String)
        case move(Int)
    }

    private struct NavItem: Identifiable {
        let id: String
        let kind: NavItemKind
    }
    
    var body: some View {
        
#if os(iOS)
        let buttonSize: CGFloat = 45
        let scrollIndicatorAllowance: CGFloat = 4
#elseif os(macOS)
        let buttonSize: CGFloat = 30
        let scrollIndicatorAllowance: CGFloat = 18
#endif
        
        VStack(alignment: .leading, spacing: 0) {
            ScrollViewReader { scrollView in
                if boardSquareDS.gameRecordCurrentValue > 0 {
                    HStack(alignment: .top, spacing: 1) {
                        
                        // Record buttons
                        ScrollView(.horizontal, showsIndicators: true) {
                            LazyHStack(alignment: .top, spacing: 1) {
                                ForEach(navItems) { item in
                                    switch item.kind {
                                    case .label(let text):
                                        Text(text)
                                            .frame(height: buttonSize)
                                            .padding(.leading, 8)
                                            .padding(.trailing, 2)
                                            .opacity(0.5)
                                        
                                    case .move(let recordId):
                                        let isSelected = recordId == boardSquareDS.gameRecordCurrentValue
                                        Button(action: {
                                            Task(priority: .userInitiated) {
                                                let currentRecordID = BoardSquareDataService.instance.gameRecordCurrentValue
                                                let animate = abs(currentRecordID - recordId) == 1
                                                await boardVM.navigateGameRecord(pRecId: recordId, pAnimate: animate)
                                            }
                                        }, label: {
                                            Text(getNavLabel(recordId) )
                                                .lineLimit(1)
                                                .fixedSize(horizontal: true, vertical: false)
                                                .padding(.horizontal, 10)
                                                .frame(minWidth: buttonSize,
                                                           maxWidth: .infinity,
                                                           minHeight: buttonSize,
                                                           maxHeight: buttonSize,
                                                           alignment: .center)
                                                .opacity(isSelected ? 1.0 : 0.6)
                                                .background(isSelected ? Color("NavigationHighlight") : Color("NavigationScroll"))
                                                .cornerRadius(6)
                                                .contentShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                                                
                                        })
                                        .id(moveViewID(recordId))
                                        .buttonStyle(PlainButtonStyle())
                                        
                                    }
                                }
                            }
                        }.padding(.trailing,2)
                        .frame(height: buttonSize + scrollIndicatorAllowance)
                        
                        // Navigate left
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await boardVM.navigateGameRecord(pRecId: boardSquareDS.gameRecordCurrentValue - 1, pAnimate: true)
                                withAnimation {
                                    scrollView.scrollTo(moveViewID(boardSquareDS.gameRecordCurrentValue))
                                }
                                
                            }
                        }, label: {
                            Image(systemName: "chevron.left")
                                .frame(width: buttonSize, height: buttonSize)
                                .background(Color("NavigationScroll"))
                                
                        }).buttonStyle(PlainButtonStyle())
                        
                        // Navigate right
                        Button(action: {
                            Task(priority: .userInitiated) {
                                await boardVM.navigateGameRecord(pRecId: boardSquareDS.gameRecordCurrentValue + 1, pAnimate: true)
                                withAnimation {
                                    scrollView.scrollTo(moveViewID(boardSquareDS.gameRecordCurrentValue))
                                }
                            }
                            
                        }, label: {
                            Image(systemName: "chevron.right")
                                .frame(width: buttonSize, height: buttonSize)
                                .background(Color("NavigationScroll"))
                        }).buttonStyle(PlainButtonStyle())
                        
                        // PGN button
                        Button(action: {
                            showPGNSheet = true
                        }, label: {
                            Image(systemName: "list.clipboard")
                                .frame(width: buttonSize, height: buttonSize)
                                .background(Color("NavigationScroll"))
                        })
                        .buttonStyle(PlainButtonStyle())
                        
                    }
                    .onChange(of: boardSquareDS.gameRecordCurrentValue)  {_, newValue in
                        Task(priority: .userInitiated) {
                            try? await Task.sleep(nanoseconds: 300_000_000) // 300 ms
                            withAnimation {
                                scrollView.scrollTo(moveViewID(newValue))
                            }
                        }
                    }
                    .onAppear() {
                        Task(priority: .userInitiated) {
                            try? await Task.sleep(nanoseconds: 300_000_000) // 300 ms
                            withAnimation {
                                scrollView.scrollTo(moveViewID(boardSquareDS.gameRecordCurrentValue))
                            }
                        }
                    }
                                        
                }
            }
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        // Keep navItems and pgnText in sync with the source data
        .onAppear { refreshNavItems() }
        .onChange(of: navigatorVM.recordIdList) { refreshNavItems() }
        .sheet(isPresented: $showPGNSheet) {
            ZStack(alignment: Alignment(horizontal: .center, vertical: .center)) {
                            
                VStack(alignment: .leading) {
                    HStack {
                        Image(systemName: "list.clipboard").imageScale(.large)
                        Text("Portable Game Notation").font(.headline)
                    }
                    
                    ScrollView(.vertical) {
                        Text(pgnText)
                            .font(.system(.body, design: .monospaced))
                            .textSelection(.enabled)
                            .frame(maxWidth: .infinity, alignment: .topLeading)
                            .padding(.vertical, 6)
                            .padding(.horizontal, 4)
                    }
                    .frame(minHeight: 80)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray.opacity(0.2))
                    )
                        
                    Divider()
                                                            
                    HStack(alignment: .center) {
                        Button("Close") { showPGNSheet = false }.buttonStyle(.bordered)
                        Button("Copy to clipboard") { copyPGNToClipboard() }.buttonStyle(.bordered)
                    }
                    
                    if copyConfirmed {
                        Label("Copied to clipboard.", systemImage: "checkmark.circle.fill")
                            .foregroundColor(.green)
                            .transition(.opacity)
                    }
                }
                .padding(10)
                .background(RoundedRectangle(cornerRadius: 10).fill(Color("FormBackground")))
                .clipped()
                .shadow(radius: 6)
                .shadow(radius: 10)
                
            }.frame(maxWidth: device.tileSize * 8, maxHeight: device.tileSize * 8)
                        

        }
    }
    
    // Helper id used for both the button and scrollTo
    private func moveViewID(_ recId: Int) -> String { "move-\(recId)" }
    
    // Build navigation items from record id list
    private func buildNavItems(from navList: [Int]) -> [NavItem] {
        var items: [NavItem] = []
        guard !navList.isEmpty else { return items }

        // Always first: Start position (Id == 1)
        if navList.first == 1 {
            items.append(NavItem(id: "move-1", kind: .move(1)))
        }

        var idx = 1
        while idx < navList.count {
            let recId = navList[idx]
            if recId <= 1 { idx += 1; continue }

            let ply = recId - 2
            if ply < 0 { idx += 1; continue }

            let status = GameRecordDataService.instance.getStateGameStatus(pId: recId)
            let isGameOver: Bool = {
                if let s = BoardViewModel.BoardStatusEnum(rawValue: status) {
                    return s == .Resigned || s == .TimeExpired
                }
                return false
            }()

            let isWhitePly = GameRecordDataService.instance.getActiveMoveColour(pId: recId) == -1
            let moveNumber = (ply / 2) + 1

            if isGameOver {
                items.append(NavItem(id: "move-\(recId)", kind: .move(recId)))
            } else if isWhitePly {
                // "N." label then white move
                items.append(NavItem(id: "lbl-\(moveNumber)-w", kind: .label("\(moveNumber).")))
                items.append(NavItem(id: "move-\(recId)", kind: .move(recId)))

                // Try to pair black reply (next rec id)
                if idx + 1 < navList.count {
                    let nextRecId = navList[idx + 1]
                    let blackReply = GameRecordDataService.instance.getActiveMoveColour(pId: nextRecId) == 1
                    if blackReply {
                        items.append(NavItem(id: "move-\(nextRecId)", kind: .move(nextRecId)))
                        idx += 1 // consume black
                    }
                }
            } else {
                // "N..." label then black move
                items.append(NavItem(id: "lbl-\(moveNumber)-b", kind: .label("\(moveNumber)...")))
                items.append(NavItem(id: "move-\(recId)", kind: .move(recId)))
            }

            idx += 1
        }

        return items
    }

    // Label for a move button
    private func getNavLabel(_ recId: Int) -> String {
        if recId == 1 { return "Start" }
        if let rec = GameRecordDataService.instance.get(pId: recId) {
                let san = rec.moveSAN
                return san.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "" : san
            }
            return ""
    }
    
    // Keep navItems and pgnText synchronized
    private func refreshNavItems() {
        let items = buildNavItems(from: navigatorVM.recordIdList)
        self.navItems = items
                
        var parts: [String] = []
        for item in items {
                switch item.kind {
                case .label(let text):
                    let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !trimmed.isEmpty {
                        parts.append(trimmed)
                    }
                case .move(let recordId):
                    // Skip the Start position entry
                    if recordId == 1 { continue }
                    let san = getNavLabel(recordId).trimmingCharacters(in: .whitespacesAndNewlines)
                    if !san.isEmpty {
                        parts.append(san + " ")
                    }
                }
            }
            self.pgnText = parts.joined()
    }
    
    // Copy PGN to clipboard
    private func copyPGNToClipboard() {
#if os(iOS)
        UIPasteboard.general.string = pgnText
#elseif os(macOS)
        let pb = NSPasteboard.general
        pb.clearContents()
        pb.setString(pgnText, forType: .string)
#endif
        withAnimation { copyConfirmed = true }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            withAnimation { copyConfirmed = false }
        }
    }
    
}


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

struct AboutView: View {
    @Binding var showMenu: Bool
    private let infoText = "Karuah Chess is a chess playing program.\nCopyright © 2020 Karuah Software\n\nFind Karuah Chess on;"
    private let otherInfoA = "Karuah Chess uses StockFish 17 with neural network version nn-1111cefa1111.nnue (big) and nn-37f18f62d772.nnue (small). You can find out more about StockFish at;"
    private let otherInfoLinkA = Link("https://stockfishchess.org", destination: URL(string: "https://stockfishchess.org")!)
    private let otherInfoB = "The full source code for Karuah Chess is available at;"
    private let otherInfoLinkB = Link("https://github.com/karuahsoftware/karuahchess", destination: URL(string: "https://github.com/karuahsoftware/karuahchess")!)
    private let licenseTitle = "Karuah Chess is distributed under the GNU General Public License Version 3 (GPLv3)."
    private let licenseInfoA = "Karuah Chess is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."
    private let licenseInfoB = "Karuah Chess is distributed in the hope that it will be useful,but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for full details at; "
    private let licenseInfoLinkB = Link("https://www.gnu.org/licenses/gpl-3.0.txt", destination: URL(string: "https://www.gnu.org/licenses/gpl-3.0.txt")!)
    private let version: String = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? ""
    
    var body: some View {
        Form {
            VStack (alignment: .leading, spacing: 0) {
                Group {
                    Text("Version: " + version)
                    Text("")
                    Text(infoText)
                    HStack(spacing:15) {
                        Link(destination: URL(string: "https://www.facebook.com/karuahchess")!) {
                            Image("SocialLogoF").resizable().frame(width: 30, height: 30, alignment: Alignment.center)
                        }.buttonStyle(BorderlessButtonStyle())
                        Link(destination: URL(string: "https://x.com/karuahsoftware")!) {
                            Image("SocialLogoT").resizable().frame(width: 30, height: 30, alignment: Alignment.center)
                        }.buttonStyle(BorderlessButtonStyle())
                    }.padding(10)
                    Text("")
                }
                
                Group {
                    Text(otherInfoA)
                    otherInfoLinkA
                    Text("")
                }
                
                Group {
                    Text(otherInfoB)
                    otherInfoLinkB
                    Text("")
                }
                
                Group {
                    Text(licenseTitle).fontWeight(.bold)
                    Text(licenseInfoA + licenseInfoB)
                    licenseInfoLinkB
                }
                
            }
            
        }.padding(0)
        .modify {
            #if os(iOS)
                $0.navigationBarTitle(Text("About Karuah Chess"), displayMode: .inline)
                .navigationBarItems(trailing: Button("Close") {
                   self.showMenu = false
                })
            #endif
        }
        
    }
    
    
}


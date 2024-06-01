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
    
    private let infoText = "Karuah Chess is a chess playing program.\nCopyright © 2020 Karuah Software\n\nFind Karuah Chess on;"
    private let otherInfoA = "Karuah Chess uses StockFish 16.1 with neural network version nn-b1a57edbea57.nnue (big) and nn-baff1ede1f90.nnue (small). You can find out more about StockFish at;"
    private let otherInfoLinkA = Link("https://stockfishchess.org", destination: URL(string: "https://stockfishchess.org")!)
    private let otherInfoB = "The full source code for Karuah Chess is available at;"
    private let otherInfoLinkB = Link("https://github.com/karuahsoftware/karuahchess", destination: URL(string: "https://github.com/karuahsoftware/karuahchess")!)
    private let licenseTitle = "Karuah Chess is distributed under the GNU General Public License Version 3 (GPLv3)."
    private let licenseInfoA = "Karuah Chess is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."
    private let licenseInfoB = "Karuah Chess is distributed in the hope that it will be useful,but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for full details at; "
    private let licenseInfoLinkB = Link("https://www.gnu.org/licenses/gpl-3.0.txt", destination: URL(string: "https://www.gnu.org/licenses/gpl-3.0.txt")!)
    private let version: String = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? ""
    private let menuSheet : MenuSheet
    
    // Initialisation
    init(pMenuSheet: MenuSheet) {
        menuSheet = pMenuSheet
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            ScrollView([.vertical]) {
                VStack(alignment: .leading) {
                    HStack {
                        Image(systemName: "questionmark")
                        .imageScale(.large)
                        Text("About Karuah Chess").font(.headline)
                    }.padding(.bottom, 10)
                    
                    Group {
                        Text("Version: " + version)
                        Text("")
                        Text(infoText)
                        
                        HStack(spacing:15) {
                            Link(destination: URL(string: "https://www.facebook.com/karuahchess")!) {
                                Image("SocialLogoF").resizable().frame(width: 30, height: 30, alignment: Alignment.center)
                            }.buttonStyle(BorderlessButtonStyle())
                            Link(destination: URL(string: "https://twitter.com/karuahsoftware")!) {
                                Image("SocialLogoT").resizable().frame(width: 30, height: 30, alignment: Alignment.center)
                            }.buttonStyle(BorderlessButtonStyle())
                        }.padding(10)
                        
                        Text("")
                    }.fixedSize(horizontal: false, vertical: true)
                    
                    Group {
                        Text(otherInfoA)
                        otherInfoLinkA.buttonStyle(BorderlessButtonStyle())
                        Text("")
                    }.fixedSize(horizontal: false, vertical: true)
                    
                    Group {
                        Text(otherInfoB)
                        otherInfoLinkB.buttonStyle(BorderlessButtonStyle())
                        Text("")
                    }.fixedSize(horizontal: false, vertical: true)
                    
                    Group {
                        Text(licenseTitle).fontWeight(.bold)
                        Text(licenseInfoA + licenseInfoB)
                        licenseInfoLinkB.buttonStyle(BorderlessButtonStyle())
                    }.fixedSize(horizontal: false, vertical: true)
                        
                    Spacer()
                        .frame(maxWidth: .infinity)
                            
                        
                }
            }.frame(minWidth: 50, maxWidth: .infinity, minHeight: 50, maxHeight: .infinity)
            
        } // Scrollview
            
        Divider()
        
        HStack {
            Button(action: {
                menuSheet.active = nil
            }){
                Text("Close")
            }
        }
    
        
    }
    
    
}


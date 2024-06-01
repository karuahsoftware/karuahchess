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

struct ToastView: View {
    @ObservedObject private var device : Device = Device.instance
    
    var body: some View {
        HStack {
            if device.toastMessage != "" {
                Text(device.toastMessage)
                .padding(15)
                .font(.headline)
                    
                    
                .modify {
                        #if os(iOS)
                            $0.background(Rectangle().fill(Color(.systemGray4)).cornerRadius(20))
                        #elseif os(macOS)
                            $0.background(Rectangle().fill(Color(.textBackgroundColor)).cornerRadius(20))
                        #endif
                    }
                    
                .offset(y: -50)
                    .modify {
                        #if os(iOS)
                            $0.foregroundColor(Color(.label))
                        #elseif os(macOS)
                            $0
                        #endif
                    }
                     
                .transition(AnyTransition.opacity.animation(.easeInOut(duration:0.5)))
            }
        }
       
    }
    
}


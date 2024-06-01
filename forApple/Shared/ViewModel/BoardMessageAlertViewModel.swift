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

@MainActor class BoardMessageAlertViewModel: ObservableObject {
    @Published var title = ""
    @Published var message = ""
    @Published var showAlert = false
    @Published var alertType = alertTypeEnum.Ok
    
    var action: Action? = {}
    
    typealias Action = () -> ()
    enum alertTypeEnum : Int { case Ok = 0, YesNo = 1}
    
    /// Show the message
    func show(_ pTitle: String, _ pMessage: String, _ pAlertType: alertTypeEnum) {
        title = pTitle
        message = pMessage
        showAlert = true
        alertType = pAlertType
    }
    
    
    /// Show the message
    func show(_ pTitle: String, _ pMessage: String, _ pAlertType: alertTypeEnum, _ pAction: Action?) {
        title = pTitle
        message = pMessage
        showAlert = true
        alertType = pAlertType
        action = pAction
    }
    
    /// Clear the message
    func clear(_ pAlertType: alertTypeEnum) {
        title = ""
        message = ""
        showAlert = false
    }
    
    
    
    
    
}

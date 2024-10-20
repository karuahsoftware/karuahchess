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

@MainActor class HintSettingsViewModel: ObservableObject {
    static let instance = HintSettingsViewModel()
    
    @Published var hintEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamHint.self).enabled {
        didSet {
            let parameter = ParamHint()
            parameter.enabled = hintEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    @Published var hintMoveEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamHintMove.self).enabled {
        didSet {
            let parameter = ParamHintMove()
            parameter.enabled = hintMoveEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
}



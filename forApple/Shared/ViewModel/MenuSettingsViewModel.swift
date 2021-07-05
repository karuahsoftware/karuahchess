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

class MenuSettingsViewModel: ObservableObject {
    @Published var value = MenuSettingsValue()
    
}

struct MenuSettingsValue {
    var arrangeBoardEnabled : Bool = BoardViewModel.shared.parameterDS.get(pParameterClass: ParamArrangeBoard.self).enabled {
        didSet {
            BoardViewModel.shared.boardLayout.editMode(pEnable: arrangeBoardEnabled)
            let parameter = ParamArrangeBoard()
            parameter.enabled = arrangeBoardEnabled
            _ = BoardViewModel.shared.parameterDS.set(pObj: parameter)
            if !parameter.enabled {
                BoardViewModel.shared.pieceEditTool.Close()
            }
        }
    }
    
    var moveHighlightEnabled : Bool = BoardViewModel.shared.parameterDS.get(pParameterClass: ParamMoveHighlight.self).enabled {
        didSet {
            let parameter = ParamMoveHighlight()
            parameter.enabled = moveHighlightEnabled
            _ = BoardViewModel.shared.parameterDS.set(pObj: parameter)
            
        }
    }
    
    var soundEnabled: Bool = BoardViewModel.shared.parameterDS.get(pParameterClass: ParamSound.self).enabled {
        didSet {
            let parameter = ParamSound()
            parameter.enabled = soundEnabled
            _ = BoardViewModel.shared.parameterDS.set(pObj: parameter)
            
        }
    }
}

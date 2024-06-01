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

@MainActor class MenuSettingsViewModel: ObservableObject {
    static let instance = MenuSettingsViewModel()
    
    @Published var isShowingEngineSettings = false
    
    @Published var arrangeBoardEnabled : Bool = ParameterDataService.instance.get(pParameterClass: ParamArrangeBoard.self).enabled {
        didSet {
            BoardViewModel.instance.tilePanelVM.editMode(pEnable: arrangeBoardEnabled)
            let parameter = ParamArrangeBoard()
            parameter.enabled = arrangeBoardEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
            if !parameter.enabled {
                BoardViewModel.instance.pieceEditToolVM.close()
                BoardViewModel.instance.castlingRightsVM.close()
            }
            Task(priority: .userInitiated) {
                BoardViewModel.instance.stopSearchJob()
            }
            
        }
    }
    
    @Published var coordinatesEnabled : Bool = ParameterDataService.instance.get(pParameterClass: ParamBoardCoord.self).enabled {
        didSet {
            let parameter = ParamBoardCoord()
            parameter.enabled = coordinatesEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
            BoardViewModel.instance.coordPanelEnabled = parameter.enabled
            
        }
    }
    
    @Published var moveHighlightEnabled : Bool = ParameterDataService.instance.get(pParameterClass: ParamMoveHighlight.self).enabled {
        didSet {
            let parameter = ParamMoveHighlight()
            parameter.enabled = moveHighlightEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
            
        }
    }
    
    @Published var navigatorEnabled : Bool = ParameterDataService.instance.get(pParameterClass: ParamNavigator.self).enabled {
        didSet {
            let parameter = ParamNavigator()
            parameter.enabled = navigatorEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
            BoardViewModel.instance.navigatorVM.enabled = parameter.enabled
            BoardViewModel.instance.loadNavigator(pEnabled: parameter.enabled)
        }
    }
    
    @Published var hintEnabled : Bool = ParameterDataService.instance.get(pParameterClass: ParamHint.self).enabled {
        didSet {
            let parameter = ParamHint()
            parameter.enabled = hintEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
            HintViewModel.instance.enabled = hintEnabled

        }
    }
    
    @Published var soundReadEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamSoundRead.self).enabled {
        didSet {
            let parameter = ParamSoundRead()
            parameter.enabled = soundReadEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
            
        }
    }
    
    
}



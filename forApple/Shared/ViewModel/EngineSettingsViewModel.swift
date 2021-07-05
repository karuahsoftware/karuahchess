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

class EngineSettingsViewModel: ObservableObject {
    static let shared = EngineSettingsViewModel()
    @Published var value = EngineSettingsValue()
    
    
}

struct EngineSettingsValue {
    var computerPlayerEnabled: Bool = BoardViewModel.shared.parameterDS.get(pParameterClass: ParamComputerPlayer.self).enabled {
        didSet {
            let parameter = ParamComputerPlayer()
            parameter.enabled = computerPlayerEnabled
            _ = BoardViewModel.shared.parameterDS.set(pObj: parameter)
        }
    }
    var computerMovesFirst: Bool = BoardViewModel.shared.parameterDS.get(pParameterClass: ParamComputerMoveFirst.self).enabled {
        didSet {
        let parameter = ParamComputerMoveFirst()
        parameter.enabled = computerMovesFirst
        _ = BoardViewModel.shared.parameterDS.set(pObj: parameter)
        }
    }
    
    private var _limitEngineStrengthELOIndex: Int = 0
    var limitEngineStrengthELOIndex: Int  {
        get {
            return eloIndex(pEloRating: BoardViewModel.shared.parameterDS.get(pParameterClass: ParamLimitEngineStrengthELO.self).eloRating)
        }
        set {
            _limitEngineStrengthELOIndex = newValue
            let parameter = ParamLimitEngineStrengthELO()
            parameter.eloRating = Constants.eloarray[_limitEngineStrengthELOIndex]
            _ = BoardViewModel.shared.parameterDS.set(pObj: parameter)
        }
    }
    
    
    // Looks up the elo index
    private func eloIndex(pEloRating: Int) -> Int {
        if let eloIndex = Constants.eloarray.firstIndex(of: pEloRating) {
            return eloIndex
        }
        else {
            return 0
        }
        
    }
}

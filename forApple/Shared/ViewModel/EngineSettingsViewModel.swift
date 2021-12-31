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

@MainActor class EngineSettingsViewModel: ObservableObject {
    static let instance = EngineSettingsViewModel()
    @Published var value = EngineSettingsValue()
    
    
    /// Resets the values to their default
    func resetToDefault() {
        value.computerPlayerEnabled = ParamComputerPlayer().enabled
        value.computerMovesFirst = ParamComputerMoveFirst().enabled
        value.randomiseFirstMove = ParamRandomiseFirstMove().enabled
        
        if let eloIndex = Constants.eloarray.firstIndex(of: ParamLimitEngineStrengthELO().eloRating) {
            value.limitEngineStrengthELOIndex = eloIndex
        }
        else {
            value.limitEngineStrengthELOIndex = 0
        }
        
        value.levelAuto = ParamLevelAuto().enabled
        value.limitAdvanced = ParamLimitAdvanced().enabled
        value.limitDepth = Double(ParamLimitDepth().depth)
        value.limitNodes = ParamLimitNodes().nodes
        value.limitMoveDuration = ParamLimitMoveDuration().moveDurationMS
        value.limitThreads = Double(ParamLimitThreads().threads)
        
    }
}

struct EngineSettingsValue {
    var computerPlayerEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamComputerPlayer.self).enabled {
        didSet {
            let parameter = ParamComputerPlayer()
            parameter.enabled = computerPlayerEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
   
    var computerMovesFirst: Bool = ParameterDataService.instance.get(pParameterClass: ParamComputerMoveFirst.self).enabled {
        didSet {
        let parameter = ParamComputerMoveFirst()
        parameter.enabled = computerMovesFirst
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    var randomiseFirstMove: Bool = ParameterDataService.instance.get(pParameterClass: ParamRandomiseFirstMove.self).enabled {
        didSet {
        let parameter = ParamRandomiseFirstMove()
        parameter.enabled = randomiseFirstMove
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    var levelAuto: Bool = ParameterDataService.instance.get(pParameterClass: ParamLevelAuto.self).enabled {
        didSet {
        let parameter = ParamLevelAuto()
        parameter.enabled = levelAuto
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    private var _limitEngineStrengthELOIndex: Int = 0
    var limitEngineStrengthELOIndex: Int  {
        get {
            return eloIndex(pEloRating: ParameterDataService.instance.get(pParameterClass: ParamLimitEngineStrengthELO.self).eloRating)
        }
        set {
            _limitEngineStrengthELOIndex = newValue
            let parameter = ParamLimitEngineStrengthELO()
            parameter.eloRating = Constants.eloarray[_limitEngineStrengthELOIndex]
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    
    // Looks up the elo index
    func eloIndex(pEloRating: Int) -> Int {
        if let eloIndex = Constants.eloarray.firstIndex(of: pEloRating) {
            return eloIndex
        }
        else {
            return 0
        }
        
    }
    
    var limitAdvanced: Bool = ParameterDataService.instance.get(pParameterClass: ParamLimitAdvanced.self).enabled {
        didSet {
        let parameter = ParamLimitAdvanced()
        parameter.enabled = limitAdvanced
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    var limitDepth: Double = Double(ParameterDataService.instance.get(pParameterClass: ParamLimitDepth.self).depth) {
        didSet {
        let parameter = ParamLimitDepth()
        parameter.depth = Int(limitDepth)
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    var limitNodesIsValid: Bool = true
    
    var limitNodes: Int = ParameterDataService.instance.get(pParameterClass: ParamLimitNodes.self).nodes {
        didSet {
            if limitNodes >= 10 && limitNodes <= 2000000000 {
                let parameter = ParamLimitNodes()
                parameter.nodes = limitNodes
                _ = ParameterDataService.instance.set(pObj: parameter)
                limitNodesIsValid = true
            }
            else {
                limitNodesIsValid = false
            }
        }
    }
    
    var limitMoveDurationIsValid: Bool = true
    
    var limitMoveDuration: Int = ParameterDataService.instance.get(pParameterClass: ParamLimitMoveDuration.self).moveDurationMS {
        didSet {
            if limitMoveDuration >= 0 && limitMoveDuration <= 600000 {
                let parameter = ParamLimitMoveDuration()
                parameter.moveDurationMS = limitMoveDuration
                _ = ParameterDataService.instance.set(pObj: parameter)
                limitMoveDurationIsValid = true
            }
            else {
                limitMoveDurationIsValid = false
            }
        }
    }
    
    var limitThreads: Double = Double(ParameterDataService.instance.get(pParameterClass: ParamLimitThreads.self).threads) {
        didSet {
        let parameter = ParamLimitThreads()
        parameter.threads = Int(limitThreads)
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    
    
    
}

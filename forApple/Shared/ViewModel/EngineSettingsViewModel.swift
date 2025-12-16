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

@MainActor class EngineSettingsViewModel: ObservableObject {
    static let instance = EngineSettingsViewModel()
    
    @Published var computerPlayerEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamComputerPlayer.self).enabled {
        didSet {
            let parameter = ParamComputerPlayer()
            parameter.enabled = computerPlayerEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
   
    @Published var computerMovesFirst: Bool = ParameterDataService.instance.get(pParameterClass: ParamComputerMoveFirst.self).enabled {
        didSet {
        let parameter = ParamComputerMoveFirst()
        parameter.enabled = computerMovesFirst
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    @Published var randomiseFirstMove: Bool = ParameterDataService.instance.get(pParameterClass: ParamRandomiseFirstMove.self).enabled {
        didSet {
        let parameter = ParamRandomiseFirstMove()
        parameter.enabled = randomiseFirstMove
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    @Published var levelAuto: Bool = ParameterDataService.instance.get(pParameterClass: ParamLevelAuto.self).enabled {
        didSet {
        let parameter = ParamLevelAuto()
        parameter.enabled = levelAuto
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
   
    @Published var limitSkillLevel: Int = ParameterDataService.instance.get(pParameterClass: ParamLimitSkillLevel.self).level {
        didSet {
            let parameter = ParamLimitSkillLevel()
            parameter.level = limitSkillLevel
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    @Published var limitAdvanced: Bool = ParameterDataService.instance.get(pParameterClass: ParamLimitAdvanced.self).enabled {
        didSet {
        let parameter = ParamLimitAdvanced()
        parameter.enabled = limitAdvanced
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    @Published var limitDepth: Double = Double(ParameterDataService.instance.get(pParameterClass: ParamLimitDepth.self).depth) {
        didSet {
        let parameter = ParamLimitDepth()
        parameter.depth = Int(limitDepth)
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    
    @Published var limitMoveDurationIsValid: Bool = true
    
    @Published var limitMoveDuration: Int = ParameterDataService.instance.get(pParameterClass: ParamLimitMoveDuration.self).moveDurationMS {
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
    
    @Published var limitThreads: Double = Double(ParameterDataService.instance.get(pParameterClass: ParamLimitThreads.self).threads) {
        didSet {
        let parameter = ParamLimitThreads()
        parameter.threads = Int(limitThreads)
        _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    // Toggles the computer moves first value
    func computerMovesFirstToggle() {
        computerMovesFirst = !computerMovesFirst
    }
    
    /// Resets the values to their default
    func resetToDefault() {
        computerPlayerEnabled = ParamComputerPlayer().enabled
        computerMovesFirst = ParamComputerMoveFirst().enabled
        randomiseFirstMove = ParamRandomiseFirstMove().enabled
        limitSkillLevel = ParamLimitSkillLevel().level
        levelAuto = ParamLevelAuto().enabled
        limitAdvanced = ParamLimitAdvanced().enabled
        limitDepth = Double(ParamLimitDepth().depth)
        limitMoveDuration = ParamLimitMoveDuration().moveDurationMS
        limitThreads = Double(ParamLimitThreads().threads)
        
    }
    
    
}



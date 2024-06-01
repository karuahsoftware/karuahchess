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

@MainActor class SoundSettingsViewModel: ObservableObject {
    static let instance = SoundSettingsViewModel()
    
    @Published var soundReadEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamSoundRead.self).enabled {
        didSet {
            let parameter = ParamSoundRead()
            parameter.enabled = soundReadEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    @Published var soundEffectEnabled: Bool = ParameterDataService.instance.get(pParameterClass: ParamSoundEffect.self).enabled {
        didSet {
            let parameter = ParamSoundEffect()
            parameter.enabled = soundEffectEnabled
            _ = ParameterDataService.instance.set(pObj: parameter)
        }
    }
    
    
    @Published var darkSquareColour: ColourARGB = ParameterDataService.instance.get(pParameterClass: ParamColourDarkSquares.self).argb() {
        didSet {
            let parameter = ParamColourDarkSquares()
            parameter.a = darkSquareColour.a
            parameter.r = darkSquareColour.r
            parameter.g = darkSquareColour.g
            parameter.b = darkSquareColour.b
            _ = ParameterDataService.instance.set(pObj: parameter)
            Device.instance.tileDarkSquareColour = Color(red: Double(darkSquareColour.r) / 255, green: Double(darkSquareColour.g) / 255, blue: Double(darkSquareColour.b) / 255)
        }
    }
     
}



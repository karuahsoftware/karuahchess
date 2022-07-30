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

struct LevelIndicatorView: View {
    @ObservedObject private var engineSettingsVM : EngineSettingsViewModel = EngineSettingsViewModel.instance
    
    
    var body: some View {
        if engineSettingsVM.computerPlayerEnabled && 0 ..< Constants.skillLevelList.endIndex ~= engineSettingsVM.limitSkillLevel {
            Text("\(Constants.skillLevelList[engineSettingsVM.limitSkillLevel])")
        }
        else {
            EmptyView()
        }
    }
}

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

package purpletreesoftware.karuahchess.customcontrol

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

@ExperimentalUnsignedTypes
class ActivityFragmentFactory(private val pActivityID: Int): FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        if (className == BoardSettings::class.java.name) {
            return BoardSettings(pActivityID)
        }
        else if (className == PieceSettings::class.java.name) {
            return PieceSettings(pActivityID)
        }
        else if (className == CastlingRights::class.java.name) {
            return CastlingRights(pActivityID)
        }
        else if (className == ClockSettings::class.java.name) {
            return ClockSettings(pActivityID)
        }
        else if (className == EngineSettings::class.java.name) {
            return EngineSettings(pActivityID)
        }
        else if (className == ImportPGN::class.java.name) {
            return ImportPGN(pActivityID)
        }
        else if (className == SoundSettings::class.java.name) {
            return SoundSettings(pActivityID)
        }
        else if (className == HintSettings::class.java.name) {
            return HintSettings(pActivityID)
        }
        return super.instantiate(classLoader, className)
    }

}
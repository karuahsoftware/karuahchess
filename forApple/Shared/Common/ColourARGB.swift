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

struct ColourARGB: Equatable, CustomStringConvertible, Hashable {
    
    var a: Int
    var r: Int
    var g: Int
    var b: Int
    var text: String
    
    static func == (lhs: ColourARGB, rhs: ColourARGB) -> Bool {
        return lhs.a == rhs.a && lhs.r == rhs.r && lhs.g == rhs.g && lhs.b == rhs.b
    }
    
    var description: String {
        return text
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(a)
        hasher.combine(r)
        hasher.combine(g)
        hasher.combine(b)
    }
}

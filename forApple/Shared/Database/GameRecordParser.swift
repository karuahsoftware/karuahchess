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

class GameRecordParser: NSObject, XMLParserDelegate {
    private var gameRecordItems : [GameRecord] = []
    private var currentElement : String = ""
    private var inRecord : Bool = false
    private var inField : Bool = false
    private var newRecord = GameRecord()
    var errorOccurred = false
    
    func parser(_ parser: XMLParser, didStartElement elementName: String, namespaceURI: String?, qualifiedName qName: String?, attributes attributeDict: [String : String]) {
        currentElement = elementName
        if elementName == "GameRecord" {
            inRecord = true
            newRecord = GameRecord()
        }
        else if inRecord && (elementName == "Id" ||  elementName == "BoardSquareStr" || elementName == "GameStateStr" || elementName == "MoveSANStr") {
            inField = true
        }
    }
       
    
    func parser(_ parser: XMLParser, didEndElement elementName: String, namespaceURI: String?, qualifiedName qName: String?) {
        
        if elementName == "GameRecord" {
            inRecord = false
            inField = false
            gameRecordItems.append(newRecord)
        }
        else if inRecord && (elementName == "Id" || elementName == "BoardSquareStr" || elementName == "GameStateStr" || elementName == "MoveSANStr") {
            inField = false
        }
        
    }
       
    
    func parser(_ parser: XMLParser, foundCharacters string: String) {
        if inRecord && inField {
        let trimmedString = string.trimmingCharacters(in: .whitespacesAndNewlines)
            switch currentElement {
            case "Id" : newRecord.id = Int(trimmedString) ?? 0
            case "BoardSquareStr" : newRecord.boardSquareStr = trimmedString
            case "GameStateStr" : newRecord.gameStateStr = trimmedString
            case "MoveSANStr" : newRecord.moveSANStr = trimmedString
            default: break
            }
        }
    }

   func parser(_ parser: XMLParser, parseErrorOccurred parseError: Error) {
        errorOccurred = true
   }

    func getResult() -> [GameRecord] {
        return gameRecordItems
    }
}

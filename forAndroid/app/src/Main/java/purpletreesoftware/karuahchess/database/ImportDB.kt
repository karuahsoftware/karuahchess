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

package purpletreesoftware.karuahchess.database

import android.content.ContentValues
import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import purpletreesoftware.karuahchess.model.gamerecord.GameRecord
import purpletreesoftware.karuahchess.model.gamerecord.GameRecordDataService
import java.io.InputStream
import java.io.PushbackInputStream
import java.util.zip.GZIPInputStream

@ExperimentalUnsignedTypes
class ImportDB {

    enum class ImportTypeEnum { GameXML }

    fun import(pFile: InputStream, pImportType: ImportTypeEnum, pContext: Context, pActivityID: Int): Long {

        var result = 0L

        if (pImportType == ImportTypeEnum.GameXML) {

            // Get the file header bytes
            val pFilePB = PushbackInputStream(pFile, 2)
            val headerBytes = ByteArray(2)
            pFilePB.read(headerBytes, 0, 2)
            pFilePB.unread(headerBytes)

            // If the file is compressed then uncompress, otherwise assume it is xml
            val fileBytes = if(isGZipped(headerBytes)) {
                GZIPInputStream(pFilePB, 2048).use {
                        it.readBytes()
                    }
            }
            else { pFilePB.readBytes() }

            // Create parser
            val xmlParser = Xml.newPullParser()
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            xmlParser.setInput(fileBytes.inputStream(), "utf-8")

            // Read xml in to list
            val gameRecList = ArrayList<GameRecord>()
            var gameRec: GameRecord? = null

            var eventType = xmlParser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val element = xmlParser.name
                        if (element == "GameRecord") {
                            gameRec = GameRecord()
                            gameRecList.add(gameRec)
                        } else if (gameRec != null) {
                            if (element == "Id") {
                                gameRec.id = xmlParser.nextText().toInt()
                            } else if (element == "BoardSquareStr") {
                                gameRec.boardSquareStr = xmlParser.nextText()
                            } else if (element == "GameStateStr") {
                                gameRec.gameStateStr = xmlParser.nextText()
                            }
                        }
                    }
                }

                eventType = xmlParser.next()
            }

            // Insert records in to database
            if (gameRecList.count() > 0) {
                val db = DatabaseHelper.getInstance(pContext).writableDatabase
                val table = TableName(pActivityID)

                db.delete("${table.GameRecord}", null, null)

                for(gameRecord in gameRecList) {
                    val contentValues = ContentValues()
                    contentValues.put("Id", gameRecord.id)
                    contentValues.put("BoardSquareStr", gameRecord.boardSquareStr)
                    contentValues.put("GameStateStr", gameRecord.gameStateStr)

                    result += db.insert("${table.GameRecord}", null, contentValues)

                }

                GameRecordDataService.getInstance(pActivityID).load()
            }

        }
        else {
            throw Exception("Invalid import type specified.")
        }


        return result

    }

    /**
     * Checks if data is compressed
     */
    private fun isGZipped(pHeaderBytes: ByteArray): Boolean {
        return if (pHeaderBytes.size < 2) {
            false
        }
        else {
            pHeaderBytes[0] == 0x1f.toByte() && pHeaderBytes[1] == 0x8b.toByte()
        }
    }

}
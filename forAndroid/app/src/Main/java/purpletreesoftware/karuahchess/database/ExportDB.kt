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

import android.content.Context
import android.util.Xml
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

class ExportDB {

    enum class ExportTypeEnum { GameXML }

    /**
     * Exports game to file
     */
    fun export(pExportType: ExportTypeEnum, pContext: Context, pActivityID: Int): File {

        if (pExportType == ExportTypeEnum.GameXML) {
            val table = TableName(pActivityID)

            //Start Document
            val outputBytes = ByteArrayOutputStream()
            val xmlDoc = Xml.newSerializer()
            xmlDoc.setOutput(outputBytes, "utf-8")

            xmlDoc.startDocument("utf-8", true)
            xmlDoc.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            xmlDoc.startTag(null, "Records")

            // Data
            val db = DatabaseHelper.getInstance(pContext).readableDatabase
            db.rawQuery("select * from ${table.GameRecord} order by id", null).use { dbCursor ->
                while (dbCursor.moveToNext()) {

                    xmlDoc.startTag(null, "GameRecord")

                    // Id
                    xmlDoc.startTag(null, "Id")
                    xmlDoc.text(dbCursor.getInt(dbCursor.getColumnIndex("Id")).toString())
                    xmlDoc.endTag(null, "Id")

                    // BoardSquareStr
                    xmlDoc.startTag(null, "BoardSquareStr")
                    xmlDoc.text(dbCursor.getString(dbCursor.getColumnIndex("BoardSquareStr")))
                    xmlDoc.endTag(null, "BoardSquareStr")

                    // GameStateStr
                    xmlDoc.startTag(null, "GameStateStr")
                    xmlDoc.text(dbCursor.getString(dbCursor.getColumnIndex("GameStateStr")))
                    xmlDoc.endTag(null, "GameStateStr")

                    // MoveSANStr
                    xmlDoc.startTag(null, "MoveSANStr")
                    xmlDoc.text(dbCursor.getString(dbCursor.getColumnIndex("MoveSANStr")))
                    xmlDoc.endTag(null, "MoveSANStr")

                    xmlDoc.endTag(null, "GameRecord")

                }
            }

            // Footer
            xmlDoc.endTag(null, "Records")
            xmlDoc.endDocument()
            xmlDoc.flush()

            db.close()


            return writeFile(outputBytes.toByteArray(), pContext)
        }
        else{
            throw Exception("Invalid export type specified.")
        }

    }


    /**
     * Writes a file to internal storage and returns file path as string
     */
    private fun writeFile(pByteArray: ByteArray, pContext: Context): File {

        // Filename for internal storage - this is overwritten for each request
        val filenameZip = "KaruahChess-Game.gz"

        // Create a directory for the data if it doesn't exist
        val dirname = "KaruahChessData"
        val folder = File(pContext.filesDir, dirname)
        if(!folder.exists()){
            folder.mkdir()
        }

        // Compress byte array and write to file
        val file = File(folder, filenameZip)
        val fos = FileOutputStream(file, false)
        val gzip = GZIPOutputStream(fos)
        gzip.write(pByteArray)
        gzip.close()
        fos.close()

        return file

    }
}
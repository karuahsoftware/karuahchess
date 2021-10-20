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

package purpletreesoftware.karuahchess.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(pDatabase: SQLiteDatabase) {
        createTables(pDatabase)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }


    /**
     * Creates all the database tables
     */
    private fun createTables(pDatabase: SQLiteDatabase) {

        pDatabase.execSQL("CREATE TABLE Parameter(Name TEXT PRIMARY KEY NOT NULL, Value BLOB NOT NULL);")
        pDatabase.execSQL("CREATE TABLE GameRecord (Id INTEGER PRIMARY KEY NOT NULL, BoardSquareStr TEXT NOT NULL, GameStateStr TEXT NOT NULL);")

    }



    companion object {

        private const val DATABASE_NAME : String = "KaruahChessV6.sqlite"
        private const val DATABASE_VERSION : Int = 1
        private var Instance: DatabaseHelper? = null

        fun getInstance(context: Context?): DatabaseHelper {
            if (Instance == null && context != null) {
                Instance = DatabaseHelper(context.applicationContext)
            }

            return Instance!!
        }
    }

}
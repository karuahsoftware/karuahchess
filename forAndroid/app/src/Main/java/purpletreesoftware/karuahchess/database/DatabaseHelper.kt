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
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.style.TabStopSpan

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbContext: Context

    init {
        dbContext = context
    }


    fun initDB(pActivityID: Int): Int {

        val cachedDBStatus = DBStatusCache.get(pActivityID)

        if (cachedDBStatus == null) {
            val db: SQLiteDatabase = this.writableDatabase

            var dbStatus = CheckDB(db, pActivityID)

            if (dbStatus != DB_OK) {
                if (dbStatus == ERROR_DB_TABLENOTEXISTS) {
                    createTablesIfNotExists(db, pActivityID)
                }

                // Recheck status
                dbStatus = CheckDB(db, pActivityID)
            }
            DBStatusCache.put(pActivityID, dbStatus)
            return dbStatus
        }
        else {
            return cachedDBStatus
        }
    }


    override fun onCreate(pDatabase: SQLiteDatabase) {
        createTablesIfNotExists(pDatabase, 0)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }


    /**
     * Creates all the database tables
     */
    private fun createTablesIfNotExists(pDatabase: SQLiteDatabase, pActivityID: Int) {

        val table = TableName(pActivityID)
        pDatabase.execSQL("CREATE TABLE IF NOT EXISTS ${table.Parameter} (Name TEXT PRIMARY KEY NOT NULL, Value BLOB NOT NULL)")
        pDatabase.execSQL("CREATE TABLE IF NOT EXISTS ${table.GameRecord} (Id INTEGER PRIMARY KEY NOT NULL, BoardSquareStr TEXT NOT NULL, GameStateStr TEXT NOT NULL, MoveSANStr TEXT NULL)")

    }


    /**
    * Check the DB is correct and operational
    */
    private fun CheckDB(pDatabase: SQLiteDatabase, pActivityID: Int): Int
    {

        val table = TableName(pActivityID)
        // Check if all tables exist
        val tableCount: Int = DatabaseUtils.longForQuery(pDatabase, "select count(name) from sqlite_master where type='table' and (name='${table.GameRecord}' or name = '${table.Parameter}');",null).toInt()

        if (tableCount != 2)
        {
            return ERROR_DB_TABLENOTEXISTS
        }

        // Read and write test
        try
        {
            // Read test
            pDatabase.rawQuery("select Id from ${table.GameRecord} limit 1",null).use { dbCursor ->
                while (dbCursor.moveToNext()) {
                    dbCursor.getInt(0)
                }
            }

            pDatabase.rawQuery("select Name from ${table.Parameter} limit 1", null).use { dbCursor ->
                while (dbCursor.moveToNext()) {
                    dbCursor.getInt(0)
                }
            }


            // Write test
            pDatabase.execSQL("pragma user_version = 0")
        }
        catch (ex: Exception)
        {
            return ERROR_DB_READWRITE;

        }

        return DB_OK;


    }


    companion object {

        private const val DATABASE_NAME : String = "KaruahChessV13.sqlite"
        private const val DATABASE_VERSION : Int = 1
        private var instance: DatabaseHelper? = null
        val DB_OK: Int = 0
        val ERROR_DB_FILEMISSING: Int = 200
        val ERROR_DB_READWRITE: Int = 201
        val ERROR_DB_TABLENOTEXISTS: Int = 202
        private val DBStatusCache = mutableMapOf<Int, Int>()


        fun getInstance(context: Context?): DatabaseHelper {
            if (instance == null && context != null) {
                instance = DatabaseHelper(context.applicationContext)
            }

            return instance!!
        }



    }

}
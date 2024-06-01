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

package purpletreesoftware.karuahchess.model.parameter

import android.content.ContentValues
import purpletreesoftware.karuahchess.common.App
import purpletreesoftware.karuahchess.common.HelperJava
import purpletreesoftware.karuahchess.database.DatabaseHelper
import purpletreesoftware.karuahchess.database.TableName
import java.io.InvalidClassException

class ParameterDataService(pActivityID: Int) : IParameterDataService  {

    private val parameters: MutableMap<String, Any>
    private val activityID: Int
    private val table: TableName

    init {
        activityID = pActivityID
        table = TableName(activityID)
        parameters = mutableMapOf()
        load()
    }

    /**
     * Loads parameters
     */
    override fun load() {
        parameters.clear()
        val db = DatabaseHelper.getInstance(App.appContext).readableDatabase
        db.rawQuery("select * from ${table.Parameter}", null).use { dbCursor ->
            while (dbCursor.moveToNext()) {
                val name = dbCursor.getString(dbCursor.getColumnIndex("Name"))
                val value = dbCursor.getBlob(dbCursor.getColumnIndex("Value"))

                try {
                    parameters[name] = HelperJava.Deserialize(value)
                }
                catch (ex: ClassNotFoundException){
                    // Do nothing
                }
                catch (ex: InvalidClassException){
                    // Do nothing
                }
            }

        }

    }


    /**
     * Gets a parameter
     */
    override fun <T: Any> get(pParameterClass: Class<T>): T {
        var param = pParameterClass.newInstance()

        if (parameters.containsKey(param.javaClass.simpleName)) {
            param = HelperJava.Cast(parameters[param.javaClass.simpleName])
        } else {
            // Sets the new parameter and puts it in the database
            set(param)
        }

        return param
    }


    /**
     * Sets a parameter
     */
    override fun <T: Any> set(pObj: T): Long {
        val param = Parameter(pObj.javaClass.simpleName,
            HelperJava.Serialize(pObj))

        return updateOrAdd(param, pObj, false)
    }



    /**
     * Updates the record if it exists. Otherwise adds a new record.
     * @param p_parameter The serialized parameter to write to the database
     * @param pObj The object being updated
     * @param pReload Reloads all instances from the DB
     * @return
     */
    private fun updateOrAdd(p_parameter: Parameter?, pObj: Any, pReload: Boolean): Long {
        var result: Long = 0

        if (p_parameter != null) {
            // Attempt to update record
            val db = DatabaseHelper.getInstance(App.appContext).writableDatabase

            var contentValues = ContentValues()
            contentValues.put("Value", p_parameter.value)
            val whereClause = "Name=?"
            val whereArgs = arrayOf<String?>(p_parameter.name)
            result = db.update("${table.Parameter}", contentValues, whereClause, whereArgs).toLong()

            //If there was no update then insert a new record
            if (result == 0L) {
                contentValues = ContentValues()
                contentValues.put("Name", p_parameter.name)
                contentValues.put("Value", p_parameter.value)
                result = db.insert("${table.Parameter}", null, contentValues)
            }



            if (result > 0) {
                if (pReload) {
                    // Reload all from db
                    load()
                } else {
                    // Just update the affected list item
                    parameters[p_parameter.name] = pObj
                }

            }
        }

        return result
    }

    companion object  {
        private val instanceMap = mutableMapOf<Int, ParameterDataService>()

        fun getInstance(pActivityID: Int): ParameterDataService {
            val instance: ParameterDataService = instanceMap.get(pActivityID) ?: run {
                val newInstance = ParameterDataService(pActivityID)
                instanceMap.put(pActivityID, newInstance)
                newInstance
            }

            return instance

        }


    }

}

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

package purpletreesoftware.karuahchess.model.parameter

import android.content.ContentValues
import purpletreesoftware.karuahchess.common.App
import java.lang.ref.WeakReference
import java.util.HashSet
import purpletreesoftware.karuahchess.common.Helper
import purpletreesoftware.karuahchess.database.DatabaseHelper
import java.io.InvalidClassException

class ParameterDataService : IParameterDataService {

    private val parameters: MutableMap<String, Any>

    init {
        objectInstances.add(WeakReference(this))
        parameters = mutableMapOf()
        load()
    }

    /**
     * Loads parameters
     */
    override fun load() {
        parameters.clear()
        val db = DatabaseHelper.getInstance(App.appContext).readableDatabase
        db.rawQuery("select * from Parameter", null).use { dbCursor ->
            while (dbCursor.moveToNext()) {
                val name = dbCursor.getString(dbCursor.getColumnIndex("Name"))
                val value = dbCursor.getBlob(dbCursor.getColumnIndex("Value"))

                try {
                    parameters[name] = Helper.Deserialize(value)
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
            param = Helper.Cast(parameters[param.javaClass.simpleName])
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
        val param = Parameter(pObj.javaClass.simpleName,Helper.Serialize(pObj))

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
            result = db.update("Parameter", contentValues, whereClause, whereArgs).toLong()

            //If there was no update then insert a new record
            if (result == 0L) {
                contentValues = ContentValues()
                contentValues.put("Name", p_parameter.name)
                contentValues.put("Value", p_parameter.value)
                result = db.insert("Parameter", null, contentValues)
            }



            if (result > 0) {
                if (pReload) {
                    // Reload all from db
                    reloadAllInstances()
                } else {
                    // Just update the affected list item
                    parameters[p_parameter.name] = pObj

                }

            }
        }

        return result
    }


    companion object {

        private var objectInstances = HashSet<WeakReference<ParameterDataService>>()

        /**
         * Refreshes all instances
         */
        fun reloadAllInstances() {
            var obj: ParameterDataService?
            val objectInstancesValid = HashSet<WeakReference<ParameterDataService>>()

            for (weakRef in objectInstances) {
                obj = weakRef.get()
                if (obj != null) {
                    obj.load()
                    objectInstancesValid.add(weakRef)
                }
            }

            objectInstances = objectInstancesValid
        }
    }

}

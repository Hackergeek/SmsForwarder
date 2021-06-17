package com.idormy.sms.forwarder.utils

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.model.RuleModel
import com.idormy.sms.forwarder.model.RuleTable
import java.util.*

object RuleUtil {
    var TAG = "RuleUtil"
    var dbHelper: DbHelper? = null
    var db: SQLiteDatabase? = null

    init {
        dbHelper = DbHelper(MyApplication.globalContext)
        // Gets the data repository in write mode
        db = dbHelper!!.readableDatabase
    }

    fun addRule(ruleModel: RuleModel): Long {

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(RuleTable.RuleEntry.COLUMN_NAME_FILED, ruleModel.filed)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_CHECK, ruleModel.check)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_VALUE, ruleModel.value)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID, ruleModel.ruleSenderId)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT, ruleModel.simSlot)

        // Insert the new row, returning the primary key value of the new row
        return db!!.insert(RuleTable.RuleEntry.TABLE_NAME, null, values)
    }

    fun updateRule(ruleModel: RuleModel?): Long {
        if (ruleModel == null) return 0

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(RuleTable.RuleEntry.COLUMN_NAME_FILED, ruleModel.filed)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_CHECK, ruleModel.check)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_VALUE, ruleModel.value)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID, ruleModel.ruleSenderId)
        values.put(RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT, ruleModel.simSlot)
        val selection = RuleTable.RuleEntry._ID + " = ? "
        val whereArgs = arrayOf(ruleModel.id.toString())
        return db!!.update(RuleTable.RuleEntry.TABLE_NAME, values, selection, whereArgs).toLong()
    }

    fun delRule(id: Long?): Int {
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + RuleTable.RuleEntry._ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        val selectionArgs = selectionArgList.toTypedArray()
        // Issue SQL statement.
        return db!!.delete(RuleTable.RuleEntry.TABLE_NAME, selection, selectionArgs)
    }

    @JvmStatic
    fun getRule(id: Long?, key: String?): List<RuleModel> {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            RuleTable.RuleEntry.COLUMN_NAME_FILED,
            RuleTable.RuleEntry.COLUMN_NAME_CHECK,
            RuleTable.RuleEntry.COLUMN_NAME_VALUE,
            RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID,
            RuleTable.RuleEntry.COLUMN_NAME_TIME,
            RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT
        )
        // Define 'where' part of query.
        var selection = " 1 = 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + RuleTable.RuleEntry._ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        if (key != null) {
            // Define 'where' part of query.
            selection += if (key == "SIM1" || key == "SIM2") {
                " and " + RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT + " IN ( 'ALL', ? ) "
            } else {
                " and " + RuleTable.RuleEntry.COLUMN_NAME_VALUE + " LIKE ? "
            }
            // Specify arguments in placeholder order.
            selectionArgList.add(key)
        }
        val selectionArgs = selectionArgList.toTypedArray()

        // How you want the results sorted in the resulting Cursor
        val sortOrder = RuleTable.RuleEntry._ID + " DESC"
        val cursor = db!!.query(
            RuleTable.RuleEntry.TABLE_NAME,  // The table to query
            projection,  // The array of columns to return (pass null to get all)
            selection,  // The columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,  // don't group the rows
            null,  // don't filter by row groups
            sortOrder // The sort order
        )
        val tRules: MutableList<RuleModel> = ArrayList()
        while (cursor.moveToNext()) {
            val itemId = cursor.getLong(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry._ID)
            )
            val itemFiled = cursor.getString(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_FILED)
            )
            val itemCheck = cursor.getString(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_CHECK)
            )
            val itemValue = cursor.getString(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_VALUE)
            )
            val itemSenderId = cursor.getLong(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID)
            )
            val itemTime = cursor.getLong(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_TIME)
            )
            val itemSimSlot = cursor.getString(
                cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT)
            )
            Log.d(TAG, "getRule: itemId$itemId")
            val ruleModel = RuleModel()
            ruleModel.id = itemId
            ruleModel.filed = itemFiled
            ruleModel.check = itemCheck
            ruleModel.value = itemValue
            ruleModel.ruleSenderId = itemSenderId
            ruleModel.time = itemTime
            ruleModel.simSlot = itemSimSlot
            tRules.add(ruleModel)
        }
        cursor.close()
        return tRules
    }
}
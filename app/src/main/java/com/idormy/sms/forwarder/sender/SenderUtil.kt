package com.idormy.sms.forwarder.sender

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.model.SenderModel
import com.idormy.sms.forwarder.model.SenderTable
import com.idormy.sms.forwarder.utils.DbHelper
import java.util.*

object SenderUtil {
    var TAG = "SenderUtil"
    var hasInit = false
    var dbHelper: DbHelper? = null
    var db: SQLiteDatabase? = null
    fun init() {
        synchronized(hasInit) {
            if (hasInit) return
            hasInit = true
            dbHelper = DbHelper(MyApplication.globalContext)
            // Gets the data repository in write mode
            db = dbHelper!!.readableDatabase
        }
    }

    fun addSender(senderModel: SenderModel): Long {

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(SenderTable.SenderEntry.COLUMN_NAME_NAME, senderModel.name)
        values.put(SenderTable.SenderEntry.COLUMN_NAME_TYPE, senderModel.type)
        values.put(SenderTable.SenderEntry.COLUMN_NAME_STATUS, senderModel.getStatus())
        values.put(SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING, senderModel.jsonSetting)

        // Insert the new row, returning the primary key value of the new row
        return db!!.insert(SenderTable.SenderEntry.TABLE_NAME, null, values)
    }

    fun updateSender(senderModel: SenderModel?): Long {
        if (senderModel == null) return 0

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(SenderTable.SenderEntry.COLUMN_NAME_NAME, senderModel.name)
        values.put(SenderTable.SenderEntry.COLUMN_NAME_TYPE, senderModel.type)
        values.put(SenderTable.SenderEntry.COLUMN_NAME_STATUS, senderModel.getStatus())
        values.put(SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING, senderModel.jsonSetting)
        val selection = SenderTable.SenderEntry.ID + " = ? "
        val whereArgs = arrayOf(senderModel.id.toString())
        return db!!.update(SenderTable.SenderEntry.TABLE_NAME, values, selection, whereArgs)
            .toLong()
    }

    fun delSender(id: Long?): Int {
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + SenderTable.SenderEntry.ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        val selectionArgs = selectionArgList.toTypedArray()
        // Issue SQL statement.
        return db!!.delete(SenderTable.SenderEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun getSender(id: Long?, key: String?): List<SenderModel> {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            SenderTable.SenderEntry.COLUMN_NAME_NAME,
            SenderTable.SenderEntry.COLUMN_NAME_TYPE,
            SenderTable.SenderEntry.COLUMN_NAME_STATUS,
            SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING,
            SenderTable.SenderEntry.COLUMN_NAME_TIME
        )
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + SenderTable.SenderEntry.ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        if (key != null) {
            // Define 'where' part of query.
            selection =
                " and (" + SenderTable.SenderEntry.COLUMN_NAME_NAME + " LIKE ? or " + SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING + " LIKE ? ) "
            // Specify arguments in placeholder order.
            selectionArgList.add(key)
            selectionArgList.add(key)
        }
        val selectionArgs = selectionArgList.toTypedArray()

        // How you want the results sorted in the resulting Cursor
        val sortOrder = SenderTable.SenderEntry.ID + " DESC"
        val cursor = db!!.query(
            SenderTable.SenderEntry.TABLE_NAME,  // The table to query
            projection,  // The array of columns to return (pass null to get all)
            selection,  // The columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,  // don't group the rows
            null,  // don't filter by row groups
            sortOrder // The sort order
        )
        val tSenders: MutableList<SenderModel> = ArrayList()
        while (cursor.moveToNext()) {
            val itemId = cursor.getLong(
                cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.ID)
            )
            val itemName = cursor.getString(
                cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_NAME)
            )
            val itemStatus = cursor.getInt(
                cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_STATUS)
            )
            val itemType = cursor.getInt(
                cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_TYPE)
            )
            val itemJsonSetting = cursor.getString(
                cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING)
            )
            val itemTime = cursor.getLong(
                cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_TIME)
            )
            Log.d(TAG, "getSender: itemId$itemId")
            val senderModel = SenderModel()
            senderModel.id = itemId
            senderModel.name = itemName
            senderModel.setStatus(itemStatus)
            senderModel.type = itemType
            senderModel.jsonSetting = itemJsonSetting
            senderModel.time = itemTime
            tSenders.add(senderModel)
        }
        cursor.close()
        return tSenders
    }

    fun countSender(key: String?): Int {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf<String>()
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (key != null) {
            // Define 'where' part of query.
            selection =
                " and (" + SenderTable.SenderEntry.COLUMN_NAME_NAME + " LIKE ? or " + SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING + " LIKE ? ) "
            // Specify arguments in placeholder order.
            selectionArgList.add(key)
            selectionArgList.add(key)
        }
        val selectionArgs = selectionArgList.toTypedArray()

        // How you want the results sorted in the resulting Cursor
        val cursor = db!!.query(
            SenderTable.SenderEntry.TABLE_NAME,  // The table to query
            projection,  // The array of columns to return (pass null to get all)
            selection,  // The columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,  // don't group the rows
            null,  // don't filter by row groups
            null // The sort order
        )
        val count = cursor.count
        cursor.close()
        return count
    }
}
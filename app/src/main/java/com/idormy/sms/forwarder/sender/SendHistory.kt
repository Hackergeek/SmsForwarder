package com.idormy.sms.forwarder.sender

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.model.LogModel
import com.idormy.sms.forwarder.model.LogTable
import com.idormy.sms.forwarder.utils.DbHelper
import com.idormy.sms.forwarder.utils.Define
import com.idormy.sms.forwarder.utils.SettingUtil.saveMsgHistory
import java.util.*

object SendHistory {
    var TAG = "SendHistory"
    var hasInit = false
    var dbHelper: DbHelper? = null
    var db: SQLiteDatabase? = null
    fun init() {
        synchronized(hasInit) {
            if (hasInit) return
            hasInit = true
            dbHelper = DbHelper(MyApplication.globalContext)
            db = dbHelper!!.readableDatabase
        }
    }

    fun addHistory(msg: String) {
        //不保存转发消息
        if (!saveMsgHistory()) return
        //保存
        val sp =
            MyApplication.globalContext.getSharedPreferences(Define.SP_MSG, Context.MODE_PRIVATE)
        val msgSetDefault: Set<String> = HashSet()
        val msgSet: MutableSet<String>? = sp.getStringSet(Define.SP_MSG_SET_KEY, msgSetDefault)
        Log.d(TAG, "msg_set：" + msgSet.toString())
        Log.d(TAG, "msg_set：" + msgSet!!.size.toString())
        msgSet.add(msg)
        sp.edit().putStringSet(Define.SP_MSG_SET_KEY, msgSet).apply()
    }

    val history: String
        get() {
            val sp = MyApplication.globalContext.getSharedPreferences(
                Define.SP_MSG,
                Context.MODE_PRIVATE
            )
            var msgSet: Set<String>? = HashSet()
            msgSet = sp.getStringSet(Define.SP_MSG_SET_KEY, msgSet)
            Log.d(TAG, "msg_set.toString()" + msgSet.toString())
            var getMsg = ""
            for (str in msgSet!!) {
                getMsg += """
                $str
                
                """.trimIndent()
            }
            return getMsg
        }

    fun addHistoryDb(logModel: LogModel): Long {
        //不保存转发消息
        if (!saveMsgHistory()) return 0

        // Gets the data repository in write mode
        val db = dbHelper!!.writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(LogTable.LogEntry.COLUMN_NAME_FROM, logModel.from)
        values.put(LogTable.LogEntry.COLUMN_NAME_CONTENT, logModel.content)
        values.put(LogTable.LogEntry.COLUMN_NAME_TIME, logModel.time)

        // Insert the new row, returning the primary key value of the new row
        return db.insert(LogTable.LogEntry.TABLE_NAME, null, values)
    }

    fun delHistoryDb(id: Long?, key: String?): Int {
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + LogTable.LogEntry.ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        if (key != null) {
            // Define 'where' part of query.
            selection =
                " and (" + LogTable.LogEntry.COLUMN_NAME_FROM + " LIKE ? or " + LogTable.LogEntry.COLUMN_NAME_CONTENT + " LIKE ? ) "
            // Specify arguments in placeholder order.
            selectionArgList.add(key)
            selectionArgList.add(key)
        }
        val selectionArgs = selectionArgList.toTypedArray()
        // Issue SQL statement.
        return db!!.delete(LogTable.LogEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun getHistoryDb(id: Long?, key: String?): String {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            LogTable.LogEntry.COLUMN_NAME_FROM,
            LogTable.LogEntry.COLUMN_NAME_CONTENT,
            LogTable.LogEntry.COLUMN_NAME_TIME
        )
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + LogTable.LogEntry.ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        if (key != null) {
            // Define 'where' part of query.
            selection =
                " and (" + LogTable.LogEntry.COLUMN_NAME_FROM + " LIKE ? or " + LogTable.LogEntry.COLUMN_NAME_CONTENT + " LIKE ? ) "
            // Specify arguments in placeholder order.
            selectionArgList.add(key)
            selectionArgList.add(key)
        }
        val selectionArgs = selectionArgList.toTypedArray()

        // How you want the results sorted in the resulting Cursor
        val sortOrder = LogTable.LogEntry.ID + " DESC"
        val cursor = db!!.query(
            LogTable.LogEntry.TABLE_NAME,  // The table to query
            projection,  // The array of columns to return (pass null to get all)
            selection,  // The columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,  // don't group the rows
            null,  // don't filter by row groups
            sortOrder // The sort order
        )
        val tLogs: MutableList<Long> = ArrayList()
        while (cursor.moveToNext()) {
            val itemId = cursor.getLong(
                cursor.getColumnIndexOrThrow(LogTable.LogEntry.ID)
            )
            tLogs.add(itemId)
        }
        cursor.close()
        val sp =
            MyApplication.globalContext.getSharedPreferences(Define.SP_MSG, Context.MODE_PRIVATE)
        var msgSet: Set<String>? = HashSet()
        msgSet = sp.getStringSet(Define.SP_MSG_SET_KEY, msgSet)
        Log.d(TAG, "msg_set.toString()" + msgSet.toString())
        var getMsg = ""
        for (str in msgSet!!) {
            getMsg += """
                $str
                
                """.trimIndent()
        }
        return getMsg
    }
}
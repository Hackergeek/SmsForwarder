package com.idormy.sms.forwarder.utils

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.model.*
import com.idormy.sms.forwarder.model.vo.LogVo
import java.util.*

object LogUtil {
    var TAG = "LogUtil"
    var dbHelper: DbHelper? = null
    var db: SQLiteDatabase? = null

    init {
        dbHelper = DbHelper(MyApplication.globalContext)
        // Gets the data repository in write mode
        db = dbHelper!!.readableDatabase
    }

    @JvmStatic
    fun addLog(logModel: LogModel?): Long {
        Log.i(TAG, "addLog logModel: $logModel")
        //不保存转发消息
        if (logModel == null) return 0

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(LogTable.LogEntry.COLUMN_NAME_FROM, logModel.from)
        values.put(LogTable.LogEntry.COLUMN_NAME_CONTENT, logModel.content)
        values.put(LogTable.LogEntry.COLUMN_NAME_SIM_INFO, logModel.simInfo)
        values.put(LogTable.LogEntry.COLUMN_NAME_RULE_ID, logModel.ruleId)

        // Insert the new row, returning the primary key value of the new row
        return db!!.insert(LogTable.LogEntry.TABLE_NAME, null, values)
    }

    fun delLog(id: Long?, key: String?): Int {
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + LogTable.LogEntry._ID + " = ? "
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

    @JvmStatic
    fun updateLog(id: Long?, forward_status: Int, forward_response: String?): Int {
        if (id == null || id <= 0) return 0
        val selection = LogTable.LogEntry._ID + " = ? "
        val selectionArgList: MutableList<String> = ArrayList()
        selectionArgList.add(id.toString())
        val values = ContentValues()
        values.put(LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS, forward_status)
        values.put(LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE, forward_response)
        val selectionArgs = selectionArgList.toTypedArray()
        return db!!.update(LogTable.LogEntry.TABLE_NAME, values, selection, selectionArgs)
    }

    fun getLog(id: Long?, key: String?): List<LogVo> {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            LogTable.LogEntry.TABLE_NAME + "." + BaseColumns._ID + " AS " + BaseColumns._ID,
            LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_FROM + " AS " + LogTable.LogEntry.COLUMN_NAME_FROM,
            LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_TIME + " AS " + LogTable.LogEntry.COLUMN_NAME_TIME,
            LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_CONTENT + " AS " + LogTable.LogEntry.COLUMN_NAME_CONTENT,
            LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_SIM_INFO + " AS " + LogTable.LogEntry.COLUMN_NAME_SIM_INFO,
            LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " AS " + LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS,
            LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE + " AS " + LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE,
            RuleTable.RuleEntry.TABLE_NAME + "." + RuleTable.RuleEntry.COLUMN_NAME_FILED + " AS " + RuleTable.RuleEntry.COLUMN_NAME_FILED,
            RuleTable.RuleEntry.TABLE_NAME + "." + RuleTable.RuleEntry.COLUMN_NAME_CHECK + " AS " + RuleTable.RuleEntry.COLUMN_NAME_CHECK,
            RuleTable.RuleEntry.TABLE_NAME + "." + RuleTable.RuleEntry.COLUMN_NAME_VALUE + " AS " + RuleTable.RuleEntry.COLUMN_NAME_VALUE,
            RuleTable.RuleEntry.TABLE_NAME + "." + RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT + " AS " + RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT,
            SenderTable.SenderEntry.TABLE_NAME + "." + SenderTable.SenderEntry.COLUMN_NAME_NAME + " AS " + SenderTable.SenderEntry.COLUMN_NAME_NAME,
            SenderTable.SenderEntry.TABLE_NAME + "." + SenderTable.SenderEntry.COLUMN_NAME_TYPE + " AS " + SenderTable.SenderEntry.COLUMN_NAME_TYPE
        )
        // Define 'where' part of query.
        var selection = " 1 "
        // Specify arguments in placeholder order.
        val selectionArgList: MutableList<String> = ArrayList()
        if (id != null) {
            // Define 'where' part of query.
            selection += " and " + LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry._ID + " = ? "
            // Specify arguments in placeholder order.
            selectionArgList.add(id.toString())
        }
        if (key != null) {
            // Define 'where' part of query.
            selection =
                " and (" + LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_FROM + " LIKE ? or " + LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_CONTENT + " LIKE ? ) "
            // Specify arguments in placeholder order.
            selectionArgList.add(key)
            selectionArgList.add(key)
        }
        val selectionArgs = selectionArgList.toTypedArray()

        // How you want the results sorted in the resulting Cursor
        val sortOrder = LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry._ID + " DESC"
        val cursor = db!!.query( // The table to query
            LogTable.LogEntry.TABLE_NAME
                    + " LEFT JOIN " + RuleTable.RuleEntry.TABLE_NAME + " ON " + LogTable.LogEntry.TABLE_NAME + "." + LogTable.LogEntry.COLUMN_NAME_RULE_ID + "=" + RuleTable.RuleEntry.TABLE_NAME + "." + RuleTable.RuleEntry._ID
                    + " LEFT JOIN " + SenderTable.SenderEntry.TABLE_NAME + " ON " + SenderTable.SenderEntry.TABLE_NAME + "." + SenderTable.SenderEntry._ID + "=" + RuleTable.RuleEntry.TABLE_NAME + "." + RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID,
            projection,  // The array of columns to return (pass null to get all)
            selection,  // The columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,  // don't group the rows
            null,  // don't filter by row groups
            sortOrder // The sort order
        )
        Log.d(TAG, "getLog: " + db!!.path)
        val LogVos: MutableList<LogVo> = ArrayList()
        Log.d(TAG, "getLog: itemId cursor" + Arrays.toString(cursor.columnNames))
        while (cursor.moveToNext()) {
            try {
                val itemid = cursor.getLong(
                    cursor.getColumnIndexOrThrow(BaseColumns._ID)
                )
                val itemfrom = cursor.getString(
                    cursor.getColumnIndexOrThrow(LogTable.LogEntry.COLUMN_NAME_FROM)
                )
                val content = cursor.getString(
                    cursor.getColumnIndexOrThrow(LogTable.LogEntry.COLUMN_NAME_CONTENT)
                )
                val simInfo = cursor.getString(
                    cursor.getColumnIndexOrThrow(LogTable.LogEntry.COLUMN_NAME_SIM_INFO)
                )
                val time = cursor.getString(
                    cursor.getColumnIndexOrThrow(LogTable.LogEntry.COLUMN_NAME_TIME)
                )
                val forwardStatus = cursor.getInt(
                    cursor.getColumnIndexOrThrow(LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS)
                )
                val forwardResponse = cursor.getString(
                    cursor.getColumnIndexOrThrow(LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE)
                )
                val ruleFiled = cursor.getString(
                    cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_FILED)
                )
                val ruleCheck = cursor.getString(
                    cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_CHECK)
                )
                val ruleValue = cursor.getString(
                    cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_VALUE)
                )
                val ruleSimSlot = cursor.getString(
                    cursor.getColumnIndexOrThrow(RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT)
                )
                val senderName = cursor.getString(
                    cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_NAME)
                )
                val senderType = cursor.getInt(
                    cursor.getColumnIndexOrThrow(SenderTable.SenderEntry.COLUMN_NAME_TYPE)
                )
                var rule = RuleModel.getRuleMatch(ruleFiled, ruleCheck, ruleValue, ruleSimSlot)
                if (senderName != null) rule += senderName.trim { it <= ' ' }
                val senderImageId = SenderModel.getImageId(senderType)
                val logVo = LogVo(
                    itemid,
                    itemfrom,
                    content,
                    simInfo,
                    time,
                    rule,
                    senderImageId,
                    forwardStatus,
                    forwardResponse
                )
                LogVos.add(logVo)
            } catch (e: Exception) {
                Log.e(TAG, "getLog e:" + e.message)
            }
        }
        cursor.close()
        return LogVos
    }
}
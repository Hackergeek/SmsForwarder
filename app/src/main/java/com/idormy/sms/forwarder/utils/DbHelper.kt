package com.idormy.sms.forwarder.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.idormy.sms.forwarder.model.LogTable
import com.idormy.sms.forwarder.model.RuleTable
import com.idormy.sms.forwarder.model.SenderTable

class DbHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        for (createEntries in SQL_CREATE_ENTRIES) {
            Log.d(TAG, "onCreate:createEntries $createEntries")
            db.execSQL(createEntries)
        }
    }

    fun delCreateTable(db: SQLiteDatabase) {
        for (delCreateEntries in SQL_DELETE_ENTRIES) {
            Log.d(TAG, "delCreateTable:delCreateEntries $delCreateEntries")
            db.execSQL(delCreateEntries)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) { //当数据库版本小于版本2时
            val sql =
                "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + LogTable.LogEntry.COLUMN_NAME_SIM_INFO + " TEXT "
            db.execSQL(sql)
        }
        if (oldVersion < 3) { //当数据库版本小于版本3时
            val sql =
                "Alter table " + RuleTable.RuleEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT + " TEXT NOT NULL DEFAULT 'ALL' "
            db.execSQL(sql)
        }
        if (oldVersion < 4) { //添加转发状态与返回信息
            var sql =
                "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " INTEGER NOT NULL DEFAULT 1 "
            db.execSQL(sql)
            sql =
                "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE + " TEXT NOT NULL DEFAULT 'ok' "
            db.execSQL(sql)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val TAG = "DbHelper"
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "sms_forwarder.db"
        private val SQL_CREATE_ENTRIES = listOf(
            "CREATE TABLE " + LogTable.LogEntry.TABLE_NAME + " (" +
                    LogTable.LogEntry.ID + " INTEGER PRIMARY KEY," +
                    LogTable.LogEntry.COLUMN_NAME_FROM + " TEXT," +
                    LogTable.LogEntry.COLUMN_NAME_CONTENT + " TEXT," +
                    LogTable.LogEntry.COLUMN_NAME_SIM_INFO + " TEXT," +
                    LogTable.LogEntry.COLUMN_NAME_RULE_ID + " INTEGER," +
                    LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " INTEGER NOT NULL DEFAULT 1," +
                    LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE + " TEXT NOT NULL DEFAULT 'ok'," +
                    LogTable.LogEntry.COLUMN_NAME_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE " + RuleTable.RuleEntry.TABLE_NAME + " (" +
                    RuleTable.RuleEntry.ID + " INTEGER PRIMARY KEY," +
                    RuleTable.RuleEntry.COLUMN_NAME_FILED + " TEXT," +
                    RuleTable.RuleEntry.COLUMN_NAME_CHECK + " TEXT," +
                    RuleTable.RuleEntry.COLUMN_NAME_VALUE + " TEXT," +
                    RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID + " INTEGER," +
                    RuleTable.RuleEntry.COLUMN_NAME_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT + " TEXT NOT NULL DEFAULT 'ALL')",
            "CREATE TABLE " + SenderTable.SenderEntry.TABLE_NAME + " (" +
                    SenderTable.SenderEntry.ID + " INTEGER PRIMARY KEY," +
                    SenderTable.SenderEntry.COLUMN_NAME_NAME + " TEXT," +
                    SenderTable.SenderEntry.COLUMN_NAME_STATUS + " INTEGER," +
                    SenderTable.SenderEntry.COLUMN_NAME_TYPE + " INTEGER," +
                    SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING + " TEXT," +
                    SenderTable.SenderEntry.COLUMN_NAME_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"
        )
        private val SQL_DELETE_ENTRIES = listOf(
            "DROP TABLE IF EXISTS " + LogTable.LogEntry.TABLE_NAME + " ; ",
            "DROP TABLE IF EXISTS " + RuleTable.RuleEntry.TABLE_NAME + " ; ",
            "DROP TABLE IF EXISTS " + SenderTable.SenderEntry.TABLE_NAME + " ; "
        )
    }
}
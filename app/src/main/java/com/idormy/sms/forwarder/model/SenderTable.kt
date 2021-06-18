package com.idormy.sms.forwarder.model

import android.provider.BaseColumns

class SenderTable  // To prevent someone from accidentally instantiating the contract class,
// make the constructor private.
private constructor() {
    /* Inner class that defines the table contents */
    object SenderEntry : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLE_NAME = "sender"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_STATUS = "status"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_JSON_SETTING = "json_setting"
        const val COLUMN_NAME_TIME = "time"
    }
}
package com.idormy.sms.forwarder.model

import android.provider.BaseColumns

class LogTable  // To prevent someone from accidentally instantiating the contract class,
// make the constructor private.
private constructor() {
    /* Inner class that defines the table contents */
    object LogEntry : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLE_NAME = "log"
        const val COLUMN_NAME_FROM = "l_from"
        const val COLUMN_NAME_CONTENT = "content"
        const val COLUMN_NAME_RULE_ID = "rule_id"
        const val COLUMN_NAME_TIME = "time"
        const val COLUMN_NAME_SIM_INFO = "sim_info"
        const val COLUMN_NAME_FORWARD_STATUS = "forward_status"
        const val COLUMN_NAME_FORWARD_RESPONSE = "forward_response"
    }
}
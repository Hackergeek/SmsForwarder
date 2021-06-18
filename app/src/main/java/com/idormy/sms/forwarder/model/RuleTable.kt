package com.idormy.sms.forwarder.model

import android.provider.BaseColumns

class RuleTable  // To prevent someone from accidentally instantiating the contract class,
// make the constructor private.
private constructor() {
    /* Inner class that defines the table contents */
    object RuleEntry : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLE_NAME = "rule"
        const val COLUMN_NAME_FILED = "filed"
        const val COLUMN_NAME_CHECK = "tcheck"
        const val COLUMN_NAME_VALUE = "value"
        const val COLUMN_NAME_SENDER_ID = "sender_id"
        const val COLUMN_NAME_TIME = "time"
        const val COLUMN_NAME_SIM_SLOT = "sim_slot"
    }
}
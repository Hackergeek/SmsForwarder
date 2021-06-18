package com.idormy.sms.forwarder.model.vo

import com.idormy.sms.forwarder.R
import java.io.Serializable

class SmsSettingVo : Serializable {
    var simSlot = 0
    var mobiles: String? = null
    var onlyNoNetwork: Boolean? = null

    constructor() {}
    constructor(simSlot: Int, mobiles: String?, onlyNoNetwork: Boolean?) {
        this.simSlot = simSlot
        this.mobiles = mobiles
        this.onlyNoNetwork = onlyNoNetwork
    }

    val smsSimSlotCheckId: Int
        get() = when (simSlot) {
            1 -> {
                R.id.btnSmsSimSlot1
            }
            2 -> {
                R.id.btnSmsSimSlot2
            }
            else -> {
                R.id.btnSmsSimSlotOrg
            }
        }
}
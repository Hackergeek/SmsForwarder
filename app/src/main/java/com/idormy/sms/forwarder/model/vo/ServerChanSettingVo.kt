package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

class ServerChanSettingVo : Serializable {
    var sendKey: String? = null

    constructor() {}
    constructor(sendKey: String?) {
        this.sendKey = sendKey
    }
}
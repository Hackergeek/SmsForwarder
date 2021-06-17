package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

class BarkSettingVo : Serializable {
    var server: String? = null

    constructor() {}
    constructor(server: String?) {
        this.server = server
    }
}
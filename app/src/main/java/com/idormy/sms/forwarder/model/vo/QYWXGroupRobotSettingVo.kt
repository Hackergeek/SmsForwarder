package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

class QYWXGroupRobotSettingVo : Serializable {
    var webHook: String? = null

    constructor() {}
    constructor(webHook: String?) {
        this.webHook = webHook
    }
}
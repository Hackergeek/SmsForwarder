package com.idormy.sms.forwarder.model.vo

import com.idormy.sms.forwarder.R
import java.io.Serializable

class WebNotifySettingVo : Serializable {
    var webServer: String? = null
    var secret: String? = null
    var method: String? = null

    constructor() {}
    constructor(webServer: String?, secret: String?, method: String?) {
        this.webServer = webServer
        this.secret = secret
        this.method = method
    }

    val webNotifyMethodCheckId: Int
        get() = if (method == null || method == "POST") {
            R.id.radioWebNotifyMethodPost
        } else {
            R.id.radioWebNotifyMethodGet
        }
}
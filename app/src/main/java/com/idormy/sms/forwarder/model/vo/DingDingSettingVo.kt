package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

class DingDingSettingVo : Serializable {
    var token: String? = null
    var secret: String? = null
    var atMobils: String? = null
    var atAll: Boolean? = null

    constructor() {}
    constructor(token: String?, secret: String?, atMobils: String?, atAll: Boolean?) {
        this.token = token
        this.secret = secret
        this.atMobils = atMobils
        this.atAll = atAll
    }
}
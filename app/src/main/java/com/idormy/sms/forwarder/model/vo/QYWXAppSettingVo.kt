package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

class QYWXAppSettingVo : Serializable {
    var corpID: String? = null
    var agentID: String? = null
    var secret: String? = null
    var toUser: String? = null
    var atAll: Boolean? = null

    constructor() {}
    constructor(
        corpID: String?,
        agentID: String?,
        secret: String?,
        toUser: String?,
        atAll: Boolean?
    ) {
        this.corpID = corpID
        this.agentID = agentID
        this.secret = secret
        this.toUser = toUser
        this.atAll = atAll
    }
}
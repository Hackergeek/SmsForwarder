package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

data class QYWXAppSettingVo(
    var corpID: String? = null,
    var agentID: String? = null,
    var secret: String? = null,
    var toUser: String? = null,
    var atAll: Boolean? = null
) : Serializable
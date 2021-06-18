package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

data class DingDingSettingVo(
    var token: String? = null,
    var secret: String? = null,
    var atPhoneNumber: String? = null,
    var atAll: Boolean? = null
) : Serializable
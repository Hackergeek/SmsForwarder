package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

data class EmailSettingVo(
    var host: String? = "",
    var port: String? = "",
    var ssl: Boolean = true,
    var fromEmail: String? = "",
    var nickname: String? = "",
    var pwd: String? = "",
    var toEmail: String? = ""
) : Serializable
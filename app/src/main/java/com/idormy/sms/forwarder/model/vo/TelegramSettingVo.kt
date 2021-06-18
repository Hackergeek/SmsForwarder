package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

data class TelegramSettingVo(
    var apiToken: String? = null,
    var chatId: String? = null
) : Serializable
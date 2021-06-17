package com.idormy.sms.forwarder.model.vo

import java.io.Serializable

class TelegramSettingVo : Serializable {
    var apiToken: String? = null
    var chatId: String? = null

    constructor() {}
    constructor(apiToken: String?, chatId: String?) {
        this.apiToken = apiToken
        this.chatId = chatId
    }
}
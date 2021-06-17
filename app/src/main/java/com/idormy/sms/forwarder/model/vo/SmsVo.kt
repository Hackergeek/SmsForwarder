package com.idormy.sms.forwarder.model.vo

import com.idormy.sms.forwarder.utils.SettingUtil.addExtraDeviceMark
import com.idormy.sms.forwarder.utils.SettingUtil.smsTemplate
import com.idormy.sms.forwarder.utils.SettingUtil.switchAddExtra
import com.idormy.sms.forwarder.utils.SettingUtil.switchSmsTemplate
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class SmsVo : Serializable {
    var mobile: String? = null
    var content: String? = null
    var date: Date? = null
    var simInfo = "SIM1_unknown_unknown"

    constructor() {}
    constructor(mobile: String?, content: String?, date: Date?, simInfo: String) {
        this.mobile = mobile
        this.content = content
        this.date = date
        this.simInfo = simInfo
    }

    val smsVoForSend: String
        get() {
            val switchAddExtra = switchAddExtra
            val switchSmsTemplate = switchSmsTemplate
            var smsTemplate = smsTemplate!!.trim { it <= ' ' }
            val deviceMark = addExtraDeviceMark!!.trim { it <= ' ' }
            if (!switchAddExtra) {
                smsTemplate = smsTemplate.replace("{{卡槽信息}}\n", "").replace("{{卡槽信息}}", "")
            }
            if (!switchSmsTemplate) {
                smsTemplate = "{{来源号码}}\n{{短信内容}}\n{{卡槽信息}}\n{{接收时间}}\n{{设备名称}}"
            }
            return smsTemplate.replace("{{来源号码}}", mobile!!)
                .replace("{{短信内容}}", content!!)
                .replace("{{卡槽信息}}", simInfo)
                .replace("{{接收时间}}", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date))
                .replace("{{设备名称}}", deviceMark)
                .trim { it <= ' ' }
        }

    override fun toString(): String {
        return "SmsVo{" +
                "mobile='" + mobile + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", simInfo=" + simInfo +
                '}'
    }
}
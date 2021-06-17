package com.idormy.sms.forwarder.sender

import android.os.Handler
import android.util.Log
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import com.idormy.sms.forwarder.utils.SimUtil.getSubscriptionIdBySimId
import com.idormy.sms.forwarder.utils.SmsUtil.sendSms

object SenderSmsMsg {
    var TAG = "SenderSmsMsg"
    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        simSlot: Int,
        mobiles: String?,
        onlyNoNetwork: Boolean?,
        from: String?,
        text: String
    ) {
        Log.i(
            TAG,
            "sendMsg simSlot:$simSlot mobiles:$mobiles onlyNoNetwork:$onlyNoNetwork from:$from text:$text"
        )

        //TODO：simSlot转subId
        val subId = getSubscriptionIdBySimId(simSlot)
        val res = sendSms(subId, mobiles!!, text)

        //TODO:粗略解析是否发送成功
        if (res == null) {
            updateLog(logId, 1, "发送成功")
        } else {
            updateLog(logId, 0, res)
        }
    }
}
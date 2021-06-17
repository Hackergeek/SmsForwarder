package com.idormy.sms.forwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.idormy.sms.forwarder.model.vo.SmsVo
import com.idormy.sms.forwarder.sender.SendUtil
import com.idormy.sms.forwarder.utils.SettingUtil.addExtraSim1
import com.idormy.sms.forwarder.utils.SettingUtil.addExtraSim2
import com.idormy.sms.forwarder.utils.SimUtil.getSimIdBySubscriptionId
import com.idormy.sms.forwarder.utils.SimUtil.getSimInfo
import java.util.*

class SmsForwarderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receiveAction = intent.action
        Log.d(TAG, "onReceive intent $receiveAction")
        if ("android.provider.Telephony.SMS_RECEIVED" == receiveAction) {
            try {
                val extras = intent.extras
                val `object` = Objects.requireNonNull(extras)?.get("pdus") as Array<*>?
                if (`object` != null) {

                    //接收手机卡信息
                    var simInfo = ""
                    //卡槽ID，默认卡槽为1
                    var simId = 1
                    try {
                        if (extras!!.containsKey("simId")) {
                            simId = extras.getInt("simId")
                        } else if (extras.containsKey("subscription")) {
                            simId = getSimIdBySubscriptionId(extras.getInt("subscription"))
                        }

                        //自定义备注优先
                        simInfo = if (simId == 2) addExtraSim2 else addExtraSim1
                        simInfo = if (simInfo.isNotEmpty()) {
                            "SIM" + simId + "_" + simInfo
                        } else {
                            getSimInfo(simId)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "获取接收手机号失败：" + e.message)
                    }
                    val smsVoList: MutableList<SmsVo> = ArrayList()
                    val format = intent.getStringExtra("format")
                    val mobileToContent: MutableMap<String, String> = HashMap()
                    var date: Date? = Date()
                    for (pdus in `object`) {
                        val pdusMsg = pdus as ByteArray
                        val sms = SmsMessage.createFromPdu(pdusMsg, format)
                        val mobile = sms.originatingAddress ?: continue //发送短信的手机号
                        //下面是获取短信的发送时间
                        date = Date(sms.timestampMillis)
                        var content = mobileToContent[mobile]
                        if (content == null) content = ""
                        content += sms.messageBody //短信内容
                        mobileToContent[mobile] = content
                    }
                    for (mobile in mobileToContent.keys) {
                        smsVoList.add(SmsVo(mobile, mobileToContent[mobile], date, simInfo))
                    }
                    Log.d(TAG, "短信：$smsVoList")
                    SendUtil.sendMsgList(smsVoList, simId)
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "解析短信失败：" + throwable.message)
            }
        }
    }

    companion object {
        const val TAG = "SmsForwarderBroadcast"
    }
}
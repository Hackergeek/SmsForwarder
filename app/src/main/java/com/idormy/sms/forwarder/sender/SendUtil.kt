package com.idormy.sms.forwarder.sender

import android.os.Handler
import android.util.Log
import com.alibaba.fastjson.JSON
import com.idormy.sms.forwarder.model.LogModel
import com.idormy.sms.forwarder.model.RuleModel
import com.idormy.sms.forwarder.model.SenderModel
import com.idormy.sms.forwarder.model.vo.*
import com.idormy.sms.forwarder.sender.SenderBarkMsg.sendMsg
import com.idormy.sms.forwarder.sender.SenderDingDingMsg.sendMsg
import com.idormy.sms.forwarder.sender.SenderMailMsg.sendEmail
import com.idormy.sms.forwarder.utils.LogUtil.addLog
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import com.idormy.sms.forwarder.utils.NetUtil.netWorkStatus
import com.idormy.sms.forwarder.utils.RuleUtil.getRule

object SendUtil {
    private const val TAG = "SendUtil"
    fun sendMsgList(smsVoList: List<SmsVo>, simId: Int) {
        Log.i(TAG, "send_msg_list size: " + smsVoList.size)
        for (smsVo in smsVoList) {
            sendMsg(smsVo, simId)
        }
    }

    fun sendMsg(smsVo: SmsVo, simId: Int) {
        Log.i(TAG, "send_msg smsVo:$smsVo")
        val key = "SIM$simId"
        val ruleList = getRule(null, key)
        if (ruleList.isNotEmpty()) {
            SenderUtil.init()
            for (ruleModel in ruleList) {
                //规则匹配发现需要发送
                try {
                    if (ruleModel.checkMsg(smsVo)) {
                        val senderModels = SenderUtil.getSender(ruleModel.ruleSenderId, null)
                        for (senderModel in senderModels) {
                            val logId = addLog(
                                LogModel(
                                    smsVo.mobile!!,
                                    smsVo.content!!,
                                    smsVo.simInfo,
                                    ruleModel.id!!
                                )
                            )
                            senderSendMsgNoHandError(smsVo, senderModel, logId)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "send_msg: fail checkMsg:", e)
                }
            }
        }
    }

    @Throws(Exception::class)
    fun sendMsgByRuleModelSenderId(
        handError: Handler?,
        ruleModel: RuleModel,
        smsVo: SmsVo,
        senderId: Long?
    ) {
        if (senderId == null) {
            throw Exception("先新建并选择发送方")
        }
        val testSim = smsVo.simInfo.substring(0, 4)
        val ruleSim = ruleModel.simSlot
        if (ruleSim != "ALL" && ruleSim != testSim) {
            throw Exception("接收卡槽未匹配中规则")
        }
        if (!ruleModel.checkMsg(smsVo)) {
            throw Exception("短信未匹配中规则")
        }
        val senderModels = SenderUtil.getSender(senderId, null)
        if (senderModels.isEmpty()) {
            throw Exception("未找到发送方")
        }
        for (senderModel in senderModels) {
            senderSendMsg(handError, smsVo, senderModel, 0)
        }
    }

    fun senderSendMsgNoHandError(smsVo: SmsVo, senderModel: SenderModel?, logId: Long) {
        senderSendMsg(null, smsVo, senderModel, logId)
    }

    fun senderSendMsg(handError: Handler?, smsVo: SmsVo, senderModel: SenderModel?, logId: Long) {
        Log.i(TAG, "senderSendMsg smsVo:" + smsVo + "senderModel:" + senderModel)
        when (senderModel!!.type) {
            SenderModel.TYPE_DINGDING ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val dingDingSettingVo = JSON.parseObject(
                        senderModel.jsonSetting, DingDingSettingVo::class.java
                    )
                    if (dingDingSettingVo != null) {
                        try {
                            sendMsg(
                                logId,
                                handError,
                                dingDingSettingVo.token,
                                dingDingSettingVo.secret,
                                dingDingSettingVo.atPhoneNumber,
                                dingDingSettingVo.atAll,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: dingding error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_EMAIL ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val emailSettingVo = JSON.parseObject(
                        senderModel.jsonSetting, EmailSettingVo::class.java
                    )
                    if (emailSettingVo != null) {
                        try {
                            sendEmail(
                                logId,
                                handError,
                                emailSettingVo.host!!,
                                emailSettingVo.port!!,
                                emailSettingVo.ssl,
                                emailSettingVo.fromEmail!!,
                                emailSettingVo.nickname!!,
                                emailSettingVo.pwd!!,
                                emailSettingVo.toEmail!!,
                                smsVo.mobile,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderMailMsg error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_BARK ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val barkSettingVo =
                        JSON.parseObject(senderModel.jsonSetting, BarkSettingVo::class.java)
                    if (barkSettingVo != null) {
                        try {
                            sendMsg(
                                logId,
                                handError,
                                barkSettingVo.server,
                                smsVo.mobile!!,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderBarkMsg error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_WEB_NOTIFY ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val webNotifySettingVo = JSON.parseObject(
                        senderModel.jsonSetting, WebNotifySettingVo::class.java
                    )
                    if (webNotifySettingVo != null) {
                        try {
                            SenderWebNotifyMsg.sendMsg(
                                logId,
                                handError,
                                webNotifySettingVo.webServer,
                                webNotifySettingVo.secret,
                                webNotifySettingVo.method,
                                smsVo.mobile,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderWebNotifyMsg error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_QYWX_GROUP_ROBOT ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val qywxGroupRobotSettingVo = JSON.parseObject(
                        senderModel.jsonSetting, QYWXGroupRobotSettingVo::class.java
                    )
                    if (qywxGroupRobotSettingVo != null) {
                        try {
                            SenderQyWxGroupRobotMsg.sendMsg(
                                logId,
                                handError,
                                qywxGroupRobotSettingVo.webHook,
                                smsVo.mobile,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderQyWxGroupRobotMsg error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_QYWX_APP ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val qYWXAppSettingVo = JSON.parseObject(
                        senderModel.jsonSetting, QYWXAppSettingVo::class.java
                    )
                    if (qYWXAppSettingVo != null) {
                        try {
                            SenderQyWxAppMsg.sendMsg(
                                logId,
                                handError,
                                qYWXAppSettingVo.corpID,
                                qYWXAppSettingVo.agentID,
                                qYWXAppSettingVo.secret,
                                qYWXAppSettingVo.toUser,
                                smsVo.smsVoForSend,
                                false
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: qywx_app error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_SERVER_CHAN ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val serverChanSettingVo = JSON.parseObject(
                        senderModel.jsonSetting, ServerChanSettingVo::class.java
                    )
                    if (serverChanSettingVo != null) {
                        try {
                            SenderServerChanMsg.sendMsg(
                                logId,
                                handError,
                                serverChanSettingVo.sendKey,
                                smsVo.mobile,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderServerChanMsg error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_TELEGRAM ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val telegramSettingVo = JSON.parseObject(
                        senderModel.jsonSetting, TelegramSettingVo::class.java
                    )
                    if (telegramSettingVo != null) {
                        try {
                            SenderTelegramMsg.sendMsg(
                                logId,
                                handError,
                                telegramSettingVo.apiToken,
                                telegramSettingVo.chatId,
                                smsVo.mobile,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderTelegramMsg error " + e.message)
                        }
                    }
                }
            SenderModel.TYPE_SMS ->                 //try phrase json setting
                if (senderModel.jsonSetting != null) {
                    val smsSettingVo =
                        JSON.parseObject(senderModel.jsonSetting, SmsSettingVo::class.java)
                    if (smsSettingVo != null) {
                        //仅当无网络时启用
                        if (true == smsSettingVo.onlyNoNetwork && 0 != netWorkStatus) {
                            val msg = "仅当无网络时启用，当前网络状态：$netWorkStatus"
                            updateLog(logId, 0, msg)
                            Log.d(TAG, msg)
                            return
                        }
                        try {
                            var simSlot = smsSettingVo.simSlot - 1
                            if (simSlot < 0) { //原进原出
                                simSlot = smsVo.simInfo.substring(3, 4).toInt() - 1
                                Log.d(TAG, "simSlot = $simSlot")
                            }
                            SenderSmsMsg.sendMsg(
                                logId,
                                handError,
                                simSlot,
                                smsSettingVo.mobiles,
                                smsSettingVo.onlyNoNetwork,
                                smsVo.mobile,
                                smsVo.smsVoForSend
                            )
                        } catch (e: Exception) {
                            updateLog(logId, 0, e.message)
                            Log.e(TAG, "senderSendMsg: SenderSmsMsg error " + e.message)
                        }
                    }
                }
            else -> {
            }
        }
    }
}
package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.alibaba.fastjson.JSON
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object SenderDingDingMsg {
    var TAG = "SenderDingdingMsg"

    @JvmStatic
    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        token: String?,
        secret: String?,
        atMobiles: String?,
        atAll: Boolean?,
        msg: String
    ) {
        var token = token
        Log.i(TAG, "sendMsg token:$token secret:$secret atMobiles:$atMobiles atAll:$atAll msg:$msg")
        if (token == null || token.isEmpty()) {
            return
        }
        token = "https://oapi.dingtalk.com/robot/send?access_token=$token"
        if (secret != null && !secret.isEmpty()) {
            val timestamp = System.currentTimeMillis()
            val stringToSign = """
                $timestamp
                $secret
                """.trimIndent()
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(secret.toByteArray(charset("UTF-8")), "HmacSHA256"))
            val signData = mac.doFinal(stringToSign.toByteArray(charset("UTF-8")))
            val sign = URLEncoder.encode(String(Base64.encode(signData, Base64.NO_WRAP)), "UTF-8")
            token += "&timestamp=$timestamp&sign=$sign"
            Log.i(TAG, "webhook_token:$token")
        }
        val textMsgMap: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        textMsgMap["msgtype"] = "text"
        val textText: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        textText["content"] = msg
        textMsgMap["text"] = textText
        if (atMobiles != null || atAll != null) {
            val atMap: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
            if (atMobiles != null) {
                val atMobilesArray = atMobiles.split(",").toTypedArray()
                val atMobilesList: MutableList<String> = ArrayList()
                for (atMobile in atMobilesArray) {
                    if (TextUtils.isDigitsOnly(atMobile)) {
                        atMobilesList.add(atMobile)
                    }
                }
                if (atMobilesList.isNotEmpty()) {
                    atMap["atMobiles"] = atMobilesList
                }
            }
            atMap["isAtAll"] = false
            if (atAll != null) {
                atMap["isAtAll"] = atAll
            }
            textMsgMap["at"] = atMap
        }
        val textMsg = JSON.toJSONString(textMsgMap)
        Log.i(TAG, "textMsg:$textMsg")
        val client = OkHttpClient()
        val requestBody: RequestBody = RequestBody.create(
            "application/json;charset=utf-8".toMediaTypeOrNull(),
            textMsg
        )
        val request: Request = Request.Builder()
            .url(token)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(requestBody)
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                updateLog(logId, 0, e.message)
                Log.d(TAG, "onFailure：" + e.message)
                if (handError != null) {
                    val msg = Message()
                    msg.what = SenderActivity.NOTIFY
                    val bundle = Bundle()
                    bundle.putString("DATA", "发送失败：" + e.message)
                    msg.data = bundle
                    handError.sendMessage(msg)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body!!.string()
                Log.d(TAG, "Code：" + response.code.toString() + responseStr)

                //TODO:粗略解析是否发送成功
                if (responseStr.contains("\"errcode\":0")) {
                    updateLog(logId, 1, responseStr)
                } else {
                    updateLog(logId, 0, responseStr)
                }
                if (handError != null) {
                    val msg = Message()
                    msg.what = SenderActivity.NOTIFY
                    val bundle = Bundle()
                    bundle.putString("DATA", "发送状态：$responseStr")
                    msg.data = bundle
                    handError.sendMessage(msg)
                    Log.d(TAG, "Coxxyyde：" + response.code.toString() + responseStr)
                }
            }
        })
    }
}
package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.alibaba.fastjson.JSON
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.util.*

object SenderQyWxAppMsg {
    var TAG = "SenderQyWxAppMsg"

    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        corpID: String?,
        agentID: String?,
        secret: String?,
        toUser: String?,
        content: String,
        forceRefresh: Boolean
    ) {
        Log.i(
            TAG,
            "sendMsg corpID:$corpID agentID:$agentID secret:$secret toUser:$toUser content:$content forceRefresh:$forceRefresh"
        )
        if (corpID == null || corpID.isEmpty() || agentID == null || agentID.isEmpty() || secret == null || secret.isEmpty()) {
            return
        }

        //TODO:判断access_token是否失效
        if (forceRefresh || MyApplication.QyWxAccessToken == null || MyApplication.QyWxAccessToken!!.isEmpty() || System.currentTimeMillis() > MyApplication.QyWxAccessTokenExpiresIn
        ) {
            var gettokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?"
            gettokenUrl += "corpid=$corpID"
            gettokenUrl += "&corpsecret=$secret"
            Log.d(TAG, "gettokenUrl：$gettokenUrl")
            val client = OkHttpClient()
            val request: Request = Request.Builder().url(gettokenUrl).get().build()
            val call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    updateLog(logId, 0, e.message)
                    Log.d(TAG, "onFailure：" + e.message)
                    if (handError != null) {
                        val msg = Message()
                        msg.what = SenderActivity.NOTIFY
                        val bundle = Bundle()
                        bundle.putString("DATA", "获取access_token失败：" + e.message)
                        msg.data = bundle
                        handError.sendMessage(msg)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body!!.string()
                    Log.d(TAG, "Code：" + response.code + " Response: " + json)
                    val jsonObject = JSON.parseObject(json)
                    val errcode = jsonObject.getInteger("errcode")
                    if (errcode == 0) {
                        MyApplication.QyWxAccessToken = jsonObject.getString("access_token")
                        MyApplication.QyWxAccessTokenExpiresIn =
                            System.currentTimeMillis() + (jsonObject.getInteger("expires_in") - 120) * 1000 //提前2分钟过期
                        Log.d(TAG, "access_token：" + MyApplication.QyWxAccessToken)
                        Log.d(TAG, "expires_in：" + MyApplication.QyWxAccessTokenExpiresIn)
                        sendTextMsg(logId, handError, agentID, toUser, content)
                    } else {
                        val errmsg = jsonObject.getString("errmsg")
                        updateLog(logId, 0, errmsg)
                        Log.d(TAG, "onFailure：$errmsg")
                        if (handError != null) {
                            val msg = Message()
                            msg.what = SenderActivity.NOTIFY
                            val bundle = Bundle()
                            bundle.putString("DATA", "获取access_token失败：$errmsg")
                            msg.data = bundle
                            handError.sendMessage(msg)
                        }
                    }
                }
            })
        } else {
            sendTextMsg(logId, handError, agentID, toUser, content)
        }
    }

    //发送文本消息
    fun sendTextMsg(
        logId: Long,
        handError: Handler?,
        agentID: String?,
        toUser: String?,
        content: String?
    ) {
        val sendUrl =
            "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + MyApplication.QyWxAccessToken
        Log.d(TAG, "sendUrl：$sendUrl")
        val textMsgMap: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        textMsgMap["touser"] = toUser
        textMsgMap["msgtype"] = "text"
        textMsgMap["agentid"] = agentID
        val textText: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        textText["content"] = content
        textMsgMap["text"] = textText
        val textMsg = JSON.toJSONString(textMsgMap)
        Log.d(TAG, "textMsg：$textMsg")
        val client = OkHttpClient()
        val requestBody =
            RequestBody.create("application/json;charset=utf-8".toMediaTypeOrNull(), textMsg)
        val request: Request = Request.Builder()
            .url(sendUrl)
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
                Log.d(TAG, "Code：" + response.code.toString() + " Response: " + responseStr)

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
                    Log.d(TAG, "Code：" + response.code.toString() + " Response: " + responseStr)
                }
            }
        })
    }
}
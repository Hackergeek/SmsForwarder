package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.alibaba.fastjson.JSON
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.util.*

object SenderQyWxGroupRobotMsg {
    var TAG = "SenderQyWxGroupRobotMsg"

    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        webHook: String?,
        from: String?,
        content: String
    ) {
        Log.i(TAG, "sendMsg webHook:$webHook from:$from content:$content")
        if (webHook == null || webHook.isEmpty()) {
            return
        }

        //String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"" + from + " : " + content + "\"}}";
        val textMsgMap: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        textMsgMap["msgtype"] = "text"
        val textText: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        textText["content"] = content
        textMsgMap["text"] = textText
        val textMsg = JSON.toJSONString(textMsgMap)
        Log.i(TAG, "textMsg:$textMsg")
        val client = OkHttpClient()
        val requestBody =
            RequestBody.create("application/json;charset=utf-8".toMediaTypeOrNull(), textMsg)
        val request: Request = Request.Builder()
            .url(webHook)
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
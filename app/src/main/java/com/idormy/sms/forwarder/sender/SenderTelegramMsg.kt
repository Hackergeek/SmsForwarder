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

object SenderTelegramMsg {
    var TAG = "SenderTelegramMsg"

    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        apiToken: String?,
        chatId: String?,
        from: String?,
        text: String
    ) {
        var text = text
        Log.i(TAG, "sendMsg apiToken:$apiToken chatId:$chatId text:$text")
        if (apiToken == null || apiToken.isEmpty()) {
            return
        }

        //特殊处理避免标题重复
        text = text.replaceFirst("^" + from + "(.*)".toRegex(), "").replace("#".toRegex(), "井")
            .trim { it <= ' ' }
        val sendUrl = "https://api.telegram.org/bot$apiToken/sendMessage"
        Log.d(TAG, "sendUrl：$sendUrl")
        val bodyMap: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()
        bodyMap["chat_id"] = chatId
        bodyMap["text"] = text
        bodyMap["parse_mode"] = "HTML"
        val bodyMsg = JSON.toJSONString(bodyMap)
        Log.d(TAG, "body：$bodyMsg")
        val client = OkHttpClient()
        val requestBody =
            RequestBody.create("application/json;charset=utf-8".toMediaTypeOrNull(), bodyMsg)
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
                Log.d(TAG, "Code：" + response.code + responseStr)

                //TODO:粗略解析是否发送成功
                if (responseStr.contains("\"ok\":true")) {
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
                    Log.d(TAG, "Response：" + response.code + responseStr)
                }
            }
        })
    }
}
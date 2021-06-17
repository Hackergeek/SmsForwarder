package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import okhttp3.*
import java.io.IOException

object SenderServerChanMsg {
    var TAG = "SenderServerChanMsg"

    @Throws(Exception::class)
    fun sendMsg(logId: Long, handError: Handler?, sendKey: String?, title: String?, desp: String) {
        var desp = desp
        Log.i(TAG, "sendMsg sendKey:$sendKey title:$title desp:$desp")
        if (sendKey == null || sendKey.isEmpty()) {
            return
        }

        //特殊处理避免标题重复
        desp = desp.replaceFirst("^" + title + "(.*)".toRegex(), "").trim { it <= ' ' }
        val sendUrl = "https://sctapi.ftqq.com/$sendKey.send"
        val client = OkHttpClient().newBuilder().build()
        val builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("title", title!!)
            .addFormDataPart("desp", desp)
        val body: RequestBody = builder.build()
        val request: Request = Request.Builder().url(sendUrl).method("POST", body).build()
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
                if (responseStr.contains("\"code\":0")) {
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
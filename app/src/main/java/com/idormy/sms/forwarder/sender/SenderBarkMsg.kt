package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import java.util.regex.Pattern

object SenderBarkMsg {
    var TAG = "SenderBarkMsg"

    @JvmStatic
    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        barkServer: String?,
        from: String,
        content: String
    ) {
        var barkServer = barkServer
        var content = content
        Log.i(TAG, "sendMsg barkServer:$barkServer from:$from content:$content")
        if (barkServer == null || barkServer.isEmpty()) {
            return
        }

        //特殊处理避免标题重复
        content = content.replaceFirst("^" + from + "(.*)".toRegex(), "").trim { it <= ' ' }
        barkServer += URLEncoder.encode(from, "UTF-8")
        barkServer += "/" + URLEncoder.encode(content, "UTF-8")
        barkServer += "?isArchive=1" //自动保存
        val isCode = content.indexOf("验证码")
        val isPassword = content.indexOf("动态密码")
        if (isCode != -1 || isPassword != -1) {
            val p = Pattern.compile("(\\d{4,6})")
            val m = p.matcher(content)
            if (m.find()) {
                println(m.group())
                barkServer += "&automaticallyCopy=1&copy=" + m.group()
            }
        }
        val client = OkHttpClient()
        val request: Request = Request.Builder().url(barkServer).get().build()
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
                if (responseStr.contains("\"message\":\"success\"")) {
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
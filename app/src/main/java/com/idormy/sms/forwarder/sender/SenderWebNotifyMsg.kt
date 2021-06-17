package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.util.Log
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.CertUtils.hostnameVerifier
import com.idormy.sms.forwarder.utils.CertUtils.sSLSocketFactory
import com.idormy.sms.forwarder.utils.CertUtils.x509TrustManager
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SenderWebNotifyMsg {
    var TAG = "SenderWebNotifyMsg"

    @Throws(Exception::class)
    fun sendMsg(
        logId: Long,
        handError: Handler?,
        webServer: String?,
        secret: String?,
        method: String?,
        from: String?,
        content: String
    ) {
        var webServer = webServer
        Log.i(TAG, "sendMsg webServer:$webServer from:$from content:$content")
        if (webServer == null || webServer.isEmpty()) {
            return
        }
        val timestamp = System.currentTimeMillis()
        var sign = ""
        if (secret != null && !secret.isEmpty()) {
            val stringToSign = """
                $timestamp
                $secret
                """.trimIndent()
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(secret.toByteArray(charset("UTF-8")), "HmacSHA256"))
            val signData = mac.doFinal(stringToSign.toByteArray(charset("UTF-8")))
            sign = URLEncoder.encode(String(Base64.encode(signData, Base64.NO_WRAP)), "UTF-8")
            Log.i(TAG, "sign:$sign")
        }
        val request: Request
        if (method == "GET") {
            webServer += (if (webServer.contains("?")) "&" else "?") + "from=" + URLEncoder.encode(
                from,
                "UTF-8"
            )
            webServer += "&content=" + URLEncoder.encode(content, "UTF-8")
            if (secret != null && !secret.isEmpty()) {
                webServer += "&timestamp=$timestamp"
                webServer += "&sign=$sign"
            }
            Log.d(TAG, "method = GET, Url = $webServer")
            request = Request.Builder().url(webServer).get().build()
        } else {
            val builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("from", from!!)
                .addFormDataPart("content", content)
            if (secret != null && secret.isNotEmpty()) {
                builder.addFormDataPart("timestamp", timestamp.toString())
                builder.addFormDataPart("sign", sign)
            }
            val body: RequestBody = builder.build()
            Log.d(TAG, "method = POST, Body = $body")
            request = Request.Builder().url(webServer).method("POST", body).build()
        }
        val client = OkHttpClient().newBuilder() //忽略https证书
            .sslSocketFactory(sSLSocketFactory, x509TrustManager!!)
            .hostnameVerifier(hostnameVerifier)
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
                Log.d(TAG, "Code：" + response.code + " Response：" + responseStr)

                //返回http状态200即为成功
                if (200 == response.code) {
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
                }
            }
        })
    }
}
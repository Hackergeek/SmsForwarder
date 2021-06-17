package com.idormy.sms.forwarder.sender

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.idormy.sms.forwarder.SenderActivity
import com.idormy.sms.forwarder.utils.LogUtil.updateLog
import com.smailnet.emailkit.Draft
import com.smailnet.emailkit.EmailKit
import com.smailnet.emailkit.EmailKit.GetSendCallback

object SenderMailMsg {
    private const val TAG = "SenderMailMsg"

    @JvmStatic
    fun sendEmail(
        logId: Long,
        handError: Handler?,
        host: String,
        port: String,
        ssl: Boolean,
        fromemail: String,
        nickname: String,
        pwd: String,
        toAdd: String,
        title: String?,
        content: String?
    ) {
        Log.d(
            TAG,
            "sendEmail: host:$host port:$port ssl:$ssl fromemail:$fromemail nickname:$nickname pwd:$pwd toAdd:$toAdd"
        )
        try {
            //初始化框架
            //EmailKit.initialize(this);

            //配置发件人邮件服务器参数
            val config = EmailKit.Config()
                .setSMTP(host, port.toInt(), ssl) //设置SMTP服务器主机地址、端口和是否开启ssl
                .setAccount(fromemail) //发件人邮箱
                .setPassword(pwd) //密码或授权码

            //设置一封草稿邮件
            val draft = Draft()
                .setNickname(nickname) //发件人昵称
                .setTo(toAdd) //收件人邮箱
                .setSubject(title) //邮件主题
                .setText(content) //邮件正文

            //使用SMTP服务发送邮件
            EmailKit.useSMTPService(config)
                .send(draft, object : GetSendCallback {
                    override fun onSuccess() {
                        updateLog(logId, 1, "发送成功")
                        Log.i(TAG, "发送成功")
                        if (handError != null) {
                            val msg = Message()
                            msg.what = SenderActivity.NOTIFY
                            val bundle = Bundle()
                            bundle.putString("DATA", "发送成功")
                            msg.data = bundle
                            handError.sendMessage(msg)
                        }
                    }

                    override fun onFailure(errMsg: String) {
                        updateLog(logId, 0, errMsg)
                        Log.i(TAG, "发送失败，错误：$errMsg")
                        if (handError != null) {
                            val msg = Message()
                            msg.what = SenderActivity.NOTIFY
                            val bundle = Bundle()
                            bundle.putString("DATA", "发送失败，错误：$errMsg")
                            msg.data = bundle
                            handError.sendMessage(msg)
                        }
                    }
                })

            //销毁框架
            EmailKit.destroy()
        } catch (e: Exception) {
            updateLog(logId, 0, e.message)
            Log.e(TAG, e.message, e)
            if (handError != null) {
                val msg = Message()
                msg.what = SenderActivity.NOTIFY
                val bundle = Bundle()
                bundle.putString("DATA", e.message)
                msg.data = bundle
                handError.sendMessage(msg)
            }
        }
    }
}
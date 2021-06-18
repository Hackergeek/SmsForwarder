package com.idormy.sms.forwarder

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.alibaba.fastjson.JSON
import com.idormy.sms.forwarder.adapter.SenderAdapter
import com.idormy.sms.forwarder.model.SenderModel
import com.idormy.sms.forwarder.model.vo.*
import com.idormy.sms.forwarder.sender.*
import com.umeng.analytics.MobclickAgent
import java.text.SimpleDateFormat
import java.util.*

class SenderActivity : AppCompatActivity() {
    // 用于存储数据
    private var senderModels: MutableList<SenderModel> = mutableListOf()
    private var adapter: SenderAdapter? = null

    //消息处理者,创建一个Handler的子类对象,目的是重写Handler的处理消息的方法(handleMessage())
    private val handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NOTIFY -> Toast.makeText(
                    this@SenderActivity,
                    msg.data.getString("DATA"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender)
        SenderUtil.init()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        //是否关闭页面提示
        val helpTip = findViewById<TextView>(R.id.help_tip)
        helpTip.visibility = if (MyApplication.showHelpTip) View.VISIBLE else View.GONE

        // 先拿到数据并放在适配器上
        initSenders() //初始化数据
        adapter = SenderAdapter(this@SenderActivity, R.layout.item_sender, senderModels)

        // 将适配器上的数据传递给listView
        val listView = findViewById<ListView>(R.id.list_view_sender)
        listView.adapter = adapter

        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val senderModel = senderModels[position]
            Log.d(TAG, "onItemClick: $senderModel")
            when (senderModel.type) {
                SenderModel.TYPE_DINGDING -> setDingDing(senderModel)
                SenderModel.TYPE_EMAIL -> setEmail(senderModel)
                SenderModel.TYPE_BARK -> setBark(senderModel)
                SenderModel.TYPE_WEB_NOTIFY -> setWebNotify(senderModel)
                SenderModel.TYPE_QYWX_GROUP_ROBOT -> setQYWXGroupRobot(senderModel)
                SenderModel.TYPE_QYWX_APP -> setQYWXApp(senderModel)
                SenderModel.TYPE_SERVER_CHAN -> setServerChan(senderModel)
                SenderModel.TYPE_TELEGRAM -> setTelegram(senderModel)
                SenderModel.TYPE_SMS -> setSms(senderModel)
                else -> {
                    Toast.makeText(this@SenderActivity, "异常的发送方类型，自动删除！", Toast.LENGTH_LONG).show()
                    if (senderModel != null) {
                        SenderUtil.delSender(senderModel.id)
                        initSenders()
                        adapter!!.update(senderModels)
                    }
                }
            }
        }
        listView.onItemLongClickListener =
            OnItemLongClickListener { _, _, position, _ -> //定义AlertDialog.Builder对象，当长按列表项的时候弹出确认删除对话框
                val builder = AlertDialog.Builder(this@SenderActivity)
                builder.setMessage("确定删除?")
                builder.setTitle("提示")

                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定") { _, _ ->
                    SenderUtil.delSender(senderModels[position].id)
                    initSenders()
                    adapter!!.update(senderModels)
                    Toast.makeText(baseContext, "删除列表项", Toast.LENGTH_SHORT).show()
                }

                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton("取消") { _, _ -> }
                builder.create().show()
                true
            }
    }

    // 初始化数据
    private fun initSenders() {
        senderModels = SenderUtil.getSender(null, null) as MutableList<SenderModel>
    }

    fun addSender(view: View?) {
        val builder = AlertDialog.Builder(this@SenderActivity)
        builder.setTitle("选择发送方类型")
        builder.setItems(R.array.add_sender_menu) { _, which ->

            //添加列表
            when (which) {
                SenderModel.TYPE_DINGDING -> setDingDing(null)
                SenderModel.TYPE_EMAIL -> setEmail(null)
                SenderModel.TYPE_BARK -> setBark(null)
                SenderModel.TYPE_WEB_NOTIFY -> setWebNotify(null)
                SenderModel.TYPE_QYWX_GROUP_ROBOT -> setQYWXGroupRobot(null)
                SenderModel.TYPE_QYWX_APP -> setQYWXApp(null)
                SenderModel.TYPE_SERVER_CHAN -> setServerChan(null)
                SenderModel.TYPE_TELEGRAM -> setTelegram(null)
                SenderModel.TYPE_SMS -> setSms(null)
                else -> Toast.makeText(this@SenderActivity, "暂不支持这种转发！", Toast.LENGTH_LONG).show()
            }
        }
        builder.show()
        Log.d(TAG, "setDingDing show" + senderModels.size)
    }

    private fun setDingDing(senderModel: SenderModel?) {
        var dingDingSettingVo: DingDingSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                dingDingSettingVo = JSON.parseObject(jsonSettingStr, DingDingSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_dingding, null)
        val editTextDingdingName = view1.findViewById<EditText>(R.id.editTextDingdingName)
        if (senderModel != null) editTextDingdingName.setText(senderModel.name)
        val editTextDingdingToken = view1.findViewById<EditText>(R.id.editTextDingdingToken)
        if (dingDingSettingVo != null) editTextDingdingToken.setText(dingDingSettingVo.token)
        val editTextDingdingSecret = view1.findViewById<EditText>(R.id.editTextDingdingSecret)
        if (dingDingSettingVo != null) editTextDingdingSecret.setText(dingDingSettingVo.secret)
        val editTextDingdingAtMobiles = view1.findViewById<EditText>(R.id.editTextDingdingAtMobiles)
        if (dingDingSettingVo?.atPhoneNumber != null) editTextDingdingAtMobiles.setText(
            dingDingSettingVo.atPhoneNumber
        )
        val switchDingDingAtAll = view1.findViewById<SwitchCompat>(R.id.switchDingDingAtAll)
        if (dingDingSettingVo?.atAll != null) switchDingDingAtAll.isChecked =
            dingDingSettingVo.atAll!!
        val buttonDingDingOk = view1.findViewById<Button>(R.id.buttondingdingok)
        val buttonDingDingDel = view1.findViewById<Button>(R.id.buttondingdingdel)
        val buttonDingDingTest = view1.findViewById<Button>(R.id.buttondingdingtest)
        alertDialog71
            .setTitle(R.string.setdingdingtitle)
            .setIcon(R.mipmap.dingding)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonDingDingOk.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextDingdingName.text.toString()
                newSenderModel.type = SenderModel.TYPE_DINGDING
                newSenderModel.setStatus(SenderModel.STATUS_ON)
                val dingDingSettingVonew = DingDingSettingVo(
                    editTextDingdingToken.text.toString(),
                    editTextDingdingSecret.text.toString(),
                    editTextDingdingAtMobiles.text.toString(),
                    switchDingDingAtAll.isChecked
                )
                newSenderModel.jsonSetting = JSON.toJSONString(dingDingSettingVonew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
                //                    adapter.add(newSenderModel);
            } else {
                senderModel.name = editTextDingdingName.text.toString()
                senderModel.type = SenderModel.TYPE_DINGDING
                senderModel.setStatus(SenderModel.STATUS_ON)
                val dingDingSettingVonew = DingDingSettingVo(
                    editTextDingdingToken.text.toString(),
                    editTextDingdingSecret.text.toString(),
                    editTextDingdingAtMobiles.text.toString(),
                    switchDingDingAtAll.isChecked
                )
                senderModel.jsonSetting = JSON.toJSONString(dingDingSettingVonew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
                //                    adapter.update(senderModel,position);
            }
            show.dismiss()
        }
        buttonDingDingDel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
                //                    adapter.del(position);
            }
            show.dismiss()
        }
        buttonDingDingTest.setOnClickListener {
            val token = editTextDingdingToken.text.toString()
            val secret = editTextDingdingSecret.text.toString()
            val atMobiles = editTextDingdingAtMobiles.text.toString()
            val atAll = switchDingDingAtAll.isChecked
            if (token != null && token.isNotEmpty()) {
                try {
                    SenderDingDingMsg.sendMsg(
                        0,
                        handler,
                        token,
                        secret,
                        atMobiles,
                        atAll,
                        "测试内容(content)@" + SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(
                            Date()
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "token 不能为空", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setEmail(senderModel: SenderModel?) {
        var emailSettingVo: EmailSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                emailSettingVo = JSON.parseObject(jsonSettingStr, EmailSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_email, null)
        val editTextEmailName = view1.findViewById<EditText>(R.id.editTextEmailName)
        if (senderModel != null) editTextEmailName.setText(senderModel.name)
        val editTextEmailHost = view1.findViewById<EditText>(R.id.editTextEmailHost)
        if (emailSettingVo != null) editTextEmailHost.setText(emailSettingVo.host)
        val editTextEmailPort = view1.findViewById<EditText>(R.id.editTextEmailPort)
        if (emailSettingVo != null) editTextEmailPort.setText(emailSettingVo.port)
        val switchEmailSSl = view1.findViewById<SwitchCompat>(R.id.switchEmailSSl)
        if (emailSettingVo != null) switchEmailSSl.isChecked = emailSettingVo.ssl
        val editTextEmailFromAdd = view1.findViewById<EditText>(R.id.editTextEmailFromAdd)
        if (emailSettingVo != null) editTextEmailFromAdd.setText(emailSettingVo.fromEmail)
        val editTextEmailNickname = view1.findViewById<EditText>(R.id.editTextEmailNickname)
        if (emailSettingVo != null) editTextEmailNickname.setText(emailSettingVo.nickname)
        val editTextEmailPsw = view1.findViewById<EditText>(R.id.editTextEmailPsw)
        if (emailSettingVo != null) editTextEmailPsw.setText(emailSettingVo.pwd)
        val editTextEmailToAdd = view1.findViewById<EditText>(R.id.editTextEmailToAdd)
        if (emailSettingVo != null) editTextEmailToAdd.setText(emailSettingVo.toEmail)
        val buttonemailok = view1.findViewById<Button>(R.id.buttonemailok)
        val buttonemaildel = view1.findViewById<Button>(R.id.buttonemaildel)
        val buttonemailtest = view1.findViewById<Button>(R.id.buttonemailtest)
        alertDialog71
            .setTitle(R.string.setemailtitle)
            .setIcon(R.mipmap.email)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonemailok.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextEmailName.text.toString()
                newSenderModel.type = SenderModel.TYPE_EMAIL
                newSenderModel.setStatus(SenderModel.STATUS_ON)
                val emailSettingVonew = EmailSettingVo(
                    editTextEmailHost.text.toString(),
                    editTextEmailPort.text.toString(),
                    switchEmailSSl.isChecked,
                    editTextEmailFromAdd.text.toString(),
                    editTextEmailNickname.text.toString(),
                    editTextEmailPsw.text.toString(),
                    editTextEmailToAdd.text.toString()
                )
                newSenderModel.jsonSetting = JSON.toJSONString(emailSettingVonew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextEmailName.text.toString()
                senderModel.type = SenderModel.TYPE_EMAIL
                senderModel.setStatus(SenderModel.STATUS_ON)
                val emailSettingVonew = EmailSettingVo(
                    editTextEmailHost.text.toString(),
                    editTextEmailPort.text.toString(),
                    switchEmailSSl.isChecked,
                    editTextEmailFromAdd.text.toString(),
                    editTextEmailNickname.text.toString(),
                    editTextEmailPsw.text.toString(),
                    editTextEmailToAdd.text.toString()
                )
                senderModel.jsonSetting = JSON.toJSONString(emailSettingVonew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonemaildel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonemailtest.setOnClickListener {
            val host = editTextEmailHost.text.toString()
            val port = editTextEmailPort.text.toString()
            val ssl = switchEmailSSl.isChecked
            val fromemail = editTextEmailFromAdd.text.toString()
            val pwd = editTextEmailPsw.text.toString()
            val toemail = editTextEmailToAdd.text.toString()
            var nickname = editTextEmailNickname.text.toString()
            if (nickname == null || nickname == "") {
                nickname = "SmsForwarder"
            }
            if (host.isNotEmpty() && port.isNotEmpty() && fromemail.isNotEmpty() && pwd.isNotEmpty() && toemail.isNotEmpty()) {
                try {
                    SenderMailMsg.sendEmail(
                        0,
                        handler,
                        host,
                        port,
                        ssl,
                        fromemail,
                        nickname,
                        pwd,
                        toemail,
                        "SmsForwarder Title",
                        "测试内容(content)@" + SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(
                            Date()
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "邮箱参数不完整", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setBark(senderModel: SenderModel?) {
        var barkSettingVo: BarkSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                barkSettingVo = JSON.parseObject(jsonSettingStr, BarkSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_bark, null)
        val editTextBarkName = view1.findViewById<EditText>(R.id.editTextBarkName)
        if (senderModel != null) editTextBarkName.setText(senderModel.name)
        val editTextBarkServer = view1.findViewById<EditText>(R.id.editTextBarkServer)
        if (barkSettingVo != null) editTextBarkServer.setText(barkSettingVo.server)
        val buttonBarkOk = view1.findViewById<Button>(R.id.buttonBarkOk)
        val buttonBarkDel = view1.findViewById<Button>(R.id.buttonBarkDel)
        val buttonBarkTest = view1.findViewById<Button>(R.id.buttonBarkTest)
        alertDialog71
            .setTitle(R.string.setbarktitle)
            .setIcon(R.mipmap.bark)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonBarkOk.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextBarkName.text.toString()
                newSenderModel.type = SenderModel.TYPE_BARK
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val barkSettingVoNew = BarkSettingVo(
                    editTextBarkServer.text.toString()
                )
                newSenderModel.jsonSetting = JSON.toJSONString(barkSettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextBarkName.text.toString()
                senderModel.type = SenderModel.TYPE_BARK
                senderModel.setStatus(SenderModel.STATUS_ON)
                val barkSettingVoNew = BarkSettingVo(
                    editTextBarkServer.text.toString()
                )
                senderModel.jsonSetting = JSON.toJSONString(barkSettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonBarkDel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonBarkTest.setOnClickListener {
            val barkServer = editTextBarkServer.text.toString()
            if (barkServer.isNotEmpty()) {
                try {
                    SenderBarkMsg.sendMsg(
                        0,
                        handler,
                        barkServer,
                        "19999999999",
                        "【京东】验证码为387481（切勿将验证码告知他人），请在页面中输入完成验证，如有问题请点击 ihelp.jd.com 联系京东客服"
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "bark-server 不能为空", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setServerChan(senderModel: SenderModel?) {
        var serverchanSettingVo: ServerChanSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                serverchanSettingVo =
                    JSON.parseObject(jsonSettingStr, ServerChanSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 =
            View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_serverchan, null)
        val editTextServerChanName = view1.findViewById<EditText>(R.id.editTextServerChanName)
        if (senderModel != null) editTextServerChanName.setText(senderModel.name)
        val editTextServerChanSendKey = view1.findViewById<EditText>(R.id.editTextServerChanSendKey)
        if (serverchanSettingVo != null) editTextServerChanSendKey.setText(serverchanSettingVo.sendKey)
        val buttonServerChanOk = view1.findViewById<Button>(R.id.buttonServerChanOk)
        val buttonServerChanDel = view1.findViewById<Button>(R.id.buttonServerChanDel)
        val buttonServerChanTest = view1.findViewById<Button>(R.id.buttonServerChanTest)
        alertDialog71
            .setTitle(R.string.setserverchantitle)
            .setIcon(R.mipmap.serverchan)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonServerChanOk.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextServerChanName.text.toString()
                newSenderModel.type = SenderModel.TYPE_SERVER_CHAN
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val serverchanSettingVoNew = ServerChanSettingVo(
                    editTextServerChanSendKey.text.toString()
                )
                newSenderModel.jsonSetting = JSON.toJSONString(serverchanSettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextServerChanName.text.toString()
                senderModel.type = SenderModel.TYPE_SERVER_CHAN
                senderModel.setStatus(SenderModel.STATUS_ON)
                val serverchanSettingVoNew = ServerChanSettingVo(
                    editTextServerChanSendKey.text.toString()
                )
                senderModel.jsonSetting = JSON.toJSONString(serverchanSettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonServerChanDel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonServerChanTest.setOnClickListener {
            val serverchanServer = editTextServerChanSendKey.text.toString()
            if (serverchanServer.isNotEmpty()) {
                try {
                    SenderServerChanMsg.sendMsg(
                        0,
                        handler,
                        serverchanServer,
                        "19999999999",
                        "【京东】验证码为387481（切勿将验证码告知他人），请在页面中输入完成验证，如有问题请点击 ihelp.jd.com 联系京东客服"
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(
                    this@SenderActivity,
                    "Server酱·Turbo版的 SendKey 不能为空",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setWebNotify(senderModel: SenderModel?) {
        var webNotifySettingVo: WebNotifySettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                webNotifySettingVo =
                    JSON.parseObject(jsonSettingStr, WebNotifySettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_webnotify, null)
        val editTextWebNotifyName = view1.findViewById<EditText>(R.id.editTextWebNotifyName)
        if (senderModel != null) editTextWebNotifyName.setText(senderModel.name)
        val editTextWebNotifyWebServer =
            view1.findViewById<EditText>(R.id.editTextWebNotifyWebServer)
        if (webNotifySettingVo != null) editTextWebNotifyWebServer.setText(webNotifySettingVo.webServer)
        val editTextWebNotifySecret = view1.findViewById<EditText>(R.id.editTextWebNotifySecret)
        if (webNotifySettingVo != null) editTextWebNotifySecret.setText(webNotifySettingVo.secret)
        val radioGroupWebNotifyMethod =
            view1.findViewById<View>(R.id.radioGroupWebNotifyMethod) as RadioGroup
        if (webNotifySettingVo != null) radioGroupWebNotifyMethod.check(webNotifySettingVo.webNotifyMethodCheckId)
        val buttonbebnotifyok = view1.findViewById<Button>(R.id.buttonbebnotifyok)
        val buttonbebnotifydel = view1.findViewById<Button>(R.id.buttonbebnotifydel)
        val buttonbebnotifytest = view1.findViewById<Button>(R.id.buttonbebnotifytest)
        alertDialog71
            .setTitle(R.string.setwebnotifytitle)
            .setIcon(R.mipmap.webhook)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonbebnotifyok.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextWebNotifyName.text.toString()
                newSenderModel.type = SenderModel.TYPE_WEB_NOTIFY
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val webNotifySettingVoNew = WebNotifySettingVo(
                    editTextWebNotifyWebServer.text.toString(),
                    editTextWebNotifySecret.text.toString(),
                    if (radioGroupWebNotifyMethod.checkedRadioButtonId == R.id.radioWebNotifyMethodGet) "GET" else "POST"
                )
                newSenderModel.jsonSetting = JSON.toJSONString(webNotifySettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextWebNotifyName.text.toString()
                senderModel.type = SenderModel.TYPE_WEB_NOTIFY
                senderModel.setStatus(SenderModel.STATUS_ON)
                val webNotifySettingVoNew = WebNotifySettingVo(
                    editTextWebNotifyWebServer.text.toString(),
                    editTextWebNotifySecret.text.toString(),
                    if (radioGroupWebNotifyMethod.checkedRadioButtonId == R.id.radioWebNotifyMethodGet) "GET" else "POST"
                )
                senderModel.jsonSetting = JSON.toJSONString(webNotifySettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonbebnotifydel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonbebnotifytest.setOnClickListener {
            val webServer = editTextWebNotifyWebServer.text.toString()
            val secret = editTextWebNotifySecret.text.toString()
            val method =
                if (radioGroupWebNotifyMethod.checkedRadioButtonId == R.id.radioWebNotifyMethodGet) "GET" else "POST"
            if (webServer.isNotEmpty()) {
                try {
                    SenderWebNotifyMsg.sendMsg(
                        0,
                        handler,
                        webServer,
                        secret,
                        method,
                        "SmsForwarder Title",
                        "测试内容(content)@" + SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(
                            Date()
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "WebServer 不能为空", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setQYWXGroupRobot(senderModel: SenderModel?) {
        var qywxGroupRobotSettingVo: QYWXGroupRobotSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                qywxGroupRobotSettingVo =
                    JSON.parseObject(jsonSettingStr, QYWXGroupRobotSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 =
            View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_qywxgrouprobot, null)
        val editTextQYWXGroupRobotName =
            view1.findViewById<EditText>(R.id.editTextQYWXGroupRobotName)
        if (senderModel != null) editTextQYWXGroupRobotName.setText(senderModel.name)
        val editTextQYWXGroupRobotWebHook =
            view1.findViewById<EditText>(R.id.editTextQYWXGroupRobotWebHook)
        if (qywxGroupRobotSettingVo != null) editTextQYWXGroupRobotWebHook.setText(
            qywxGroupRobotSettingVo.webHook
        )
        val buttonQyWxGroupRobotOk = view1.findViewById<Button>(R.id.buttonQyWxGroupRobotOk)
        val buttonQyWxGroupRobotDel = view1.findViewById<Button>(R.id.buttonQyWxGroupRobotDel)
        val buttonQyWxGroupRobotTest = view1.findViewById<Button>(R.id.buttonQyWxGroupRobotTest)
        alertDialog71
            .setTitle(R.string.setqywxgrouprobottitle)
            .setIcon(R.mipmap.qywx)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonQyWxGroupRobotOk.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextQYWXGroupRobotName.text.toString()
                newSenderModel.type = SenderModel.TYPE_QYWX_GROUP_ROBOT
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val qywxGroupRobotSettingVoNew = QYWXGroupRobotSettingVo(
                    editTextQYWXGroupRobotWebHook.text.toString()
                )
                newSenderModel.jsonSetting = JSON.toJSONString(qywxGroupRobotSettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextQYWXGroupRobotName.text.toString()
                senderModel.type = SenderModel.TYPE_QYWX_GROUP_ROBOT
                senderModel.setStatus(SenderModel.STATUS_ON)
                val qywxGroupRobotSettingVoNew = QYWXGroupRobotSettingVo(
                    editTextQYWXGroupRobotWebHook.text.toString()
                )
                senderModel.jsonSetting = JSON.toJSONString(qywxGroupRobotSettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonQyWxGroupRobotDel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonQyWxGroupRobotTest.setOnClickListener {
            val webHook = editTextQYWXGroupRobotWebHook.text.toString()
            if (webHook.isNotEmpty()) {
                try {
                    SenderQyWxGroupRobotMsg.sendMsg(
                        0,
                        handler,
                        webHook,
                        "SmsForwarder Title",
                        "测试内容(content)@" + SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(
                            Date()
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "webHook 不能为空", Toast.LENGTH_LONG).show()
            }
        }
    }

    //企业微信应用
    private fun setQYWXApp(senderModel: SenderModel?) {
        var qywxAppSettingVo: QYWXAppSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                qywxAppSettingVo =
                    JSON.parseObject<QYWXAppSettingVo>(jsonSettingStr, QYWXAppSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_qywxapp, null)
        val editTextQYWXAppName = view1.findViewById<EditText>(R.id.editTextQYWXAppName)
        if (senderModel != null) editTextQYWXAppName.setText(senderModel.name)
        val editTextQYWXAppCorpID = view1.findViewById<EditText>(R.id.editTextQYWXAppCorpID)
        val editTextQYWXAppAgentID = view1.findViewById<EditText>(R.id.editTextQYWXAppAgentID)
        val editTextQYWXAppSecret = view1.findViewById<EditText>(R.id.editTextQYWXAppSecret)
        val linearLayoutQYWXAppToUser =
            view1.findViewById<LinearLayout>(R.id.linearLayoutQYWXAppToUser)
        val editTextQYWXAppToUser = view1.findViewById<EditText>(R.id.editTextQYWXAppToUser)
        val switchQYWXAppAtAll = view1.findViewById<SwitchCompat>(R.id.switchQYWXAppAtAll)
        if (qywxAppSettingVo != null) {
            editTextQYWXAppCorpID.setText(qywxAppSettingVo.corpID)
            editTextQYWXAppAgentID.setText(qywxAppSettingVo.agentID)
            editTextQYWXAppSecret.setText(qywxAppSettingVo.secret)
            editTextQYWXAppToUser.setText(qywxAppSettingVo.toUser)
            switchQYWXAppAtAll.isChecked = qywxAppSettingVo.atAll!!
            linearLayoutQYWXAppToUser.visibility =
                if (qywxAppSettingVo.atAll as Boolean) View.GONE else View.VISIBLE
        }
        switchQYWXAppAtAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                linearLayoutQYWXAppToUser.visibility = View.GONE
                editTextQYWXAppToUser.setText("@all")
            } else {
                linearLayoutQYWXAppToUser.visibility = View.VISIBLE
                editTextQYWXAppToUser.setText("")
            }
            Log.d(TAG, "onCheckedChanged:$isChecked")
        }
        val buttonQYWXAppok = view1.findViewById<Button>(R.id.buttonQYWXAppOk)
        val buttonQYWXAppdel = view1.findViewById<Button>(R.id.buttonQYWXAppDel)
        val buttonQYWXApptest = view1.findViewById<Button>(R.id.buttonQYWXAppTest)
        alertDialog71
            .setTitle(R.string.setqywxapptitle)
            .setIcon(R.mipmap.qywxapp)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonQYWXAppok.setOnClickListener(View.OnClickListener {
            val toUser = editTextQYWXAppToUser.text.toString()
            if (toUser == null || toUser.isEmpty()) {
                Toast.makeText(this@SenderActivity, "指定成员 不能为空 或者 选择@all", Toast.LENGTH_LONG).show()
                editTextQYWXAppToUser.isFocusable = true
                editTextQYWXAppToUser.requestFocus()
                return@OnClickListener
            }
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextQYWXAppName.text.toString()
                newSenderModel.type = SenderModel.TYPE_QYWX_APP
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val qywxAppSettingVoNew = QYWXAppSettingVo(
                    editTextQYWXAppCorpID.text.toString(),
                    editTextQYWXAppAgentID.text.toString(),
                    editTextQYWXAppSecret.text.toString(),
                    editTextQYWXAppToUser.text.toString(),
                    switchQYWXAppAtAll.isChecked
                )
                newSenderModel.jsonSetting = JSON.toJSONString(qywxAppSettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextQYWXAppName.text.toString()
                senderModel.type = SenderModel.TYPE_QYWX_APP
                senderModel.setStatus(SenderModel.STATUS_ON)
                val qywxAppSettingVoNew = QYWXAppSettingVo(
                    editTextQYWXAppCorpID.text.toString(),
                    editTextQYWXAppAgentID.text.toString(),
                    editTextQYWXAppSecret.text.toString(),
                    editTextQYWXAppToUser.text.toString(),
                    switchQYWXAppAtAll.isChecked
                )
                senderModel.jsonSetting = JSON.toJSONString(qywxAppSettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        })
        buttonQYWXAppdel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonQYWXApptest.setOnClickListener {
            val cropID = editTextQYWXAppCorpID.text.toString()
            val agentID = editTextQYWXAppAgentID.text.toString()
            val secret = editTextQYWXAppSecret.text.toString()
            val toUser = editTextQYWXAppToUser.text.toString()
            //Boolean atAll = switchQYWXAppAtAll.isChecked();
            if (toUser != null && toUser.isNotEmpty()) {
                try {
                    SenderQyWxAppMsg.sendMsg(
                        0,
                        handler,
                        cropID,
                        agentID,
                        secret,
                        toUser,
                        "测试内容(content)@" + SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(
                            Date()
                        ),
                        true
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "指定成员 不能为空 或者 选择@all", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Telegram机器人
    private fun setTelegram(senderModel: SenderModel?) {
        var telegramSettingVo: TelegramSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            if (jsonSettingStr != null) {
                telegramSettingVo = JSON.parseObject(jsonSettingStr, TelegramSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_telegram, null)
        val editTextTelegramName = view1.findViewById<EditText>(R.id.editTextTelegramName)
        if (senderModel != null) editTextTelegramName.setText(senderModel.name)
        val editTextTelegramApiToken = view1.findViewById<EditText>(R.id.editTextTelegramApiToken)
        if (telegramSettingVo != null) editTextTelegramApiToken.setText(telegramSettingVo.apiToken)
        val editTextTelegramChatId = view1.findViewById<EditText>(R.id.editTextTelegramChatId)
        if (telegramSettingVo != null) editTextTelegramChatId.setText(telegramSettingVo.chatId)
        val buttonTelegramOk = view1.findViewById<Button>(R.id.buttonTelegramOk)
        val buttonTelegramDel = view1.findViewById<Button>(R.id.buttonTelegramDel)
        val buttonTelegramTest = view1.findViewById<Button>(R.id.buttonTelegramTest)
        alertDialog71
            .setTitle(R.string.settelegramtitle)
            .setIcon(R.mipmap.telegram)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonTelegramOk.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextTelegramName.text.toString()
                newSenderModel.type = SenderModel.TYPE_TELEGRAM
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val telegramSettingVoNew = TelegramSettingVo(
                    editTextTelegramApiToken.text.toString(),
                    editTextTelegramChatId.text.toString()
                )
                newSenderModel.jsonSetting = JSON.toJSONString(telegramSettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextTelegramName.text.toString()
                senderModel.type = SenderModel.TYPE_TELEGRAM
                senderModel.setStatus(SenderModel.STATUS_ON)
                val telegramSettingVoNew = TelegramSettingVo(
                    editTextTelegramApiToken.text.toString(),
                    editTextTelegramChatId.text.toString()
                )
                senderModel.jsonSetting = JSON.toJSONString(telegramSettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonTelegramDel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonTelegramTest.setOnClickListener {
            val apiToken = editTextTelegramApiToken.text.toString()
            val chatId = editTextTelegramChatId.text.toString()
            if (apiToken.isNotEmpty() && chatId.isNotEmpty()) {
                try {
                    SenderTelegramMsg.sendMsg(
                        0,
                        handler,
                        apiToken,
                        chatId,
                        "19999999999",
                        "【京东】验证码为387481（切勿将验证码告知他人），请在页面中输入完成验证，如有问题请点击 ihelp.jd.com 联系京东客服"
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(
                    this@SenderActivity,
                    "机器人的ApiToken 和 被通知人的ChatId 都不能为空",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    //Sms
    private fun setSms(senderModel: SenderModel?) {
        var smsSettingVo: SmsSettingVo? = null
        //try phrase json setting
        if (senderModel != null) {
            val jsonSettingStr = senderModel.jsonSetting
            Log.d(TAG, "jsonSettingStr = $jsonSettingStr")
            if (jsonSettingStr != null) {
                smsSettingVo = JSON.parseObject(jsonSettingStr, SmsSettingVo::class.java)
            }
        }
        val alertDialog71 = AlertDialog.Builder(this@SenderActivity)
        val view1 = View.inflate(this@SenderActivity, R.layout.alert_dialog_setview_sms, null)
        val editTextSmsName = view1.findViewById<EditText>(R.id.editTextSmsName)
        if (senderModel != null) editTextSmsName.setText(senderModel.name)
        val radioGroupSmsSimSlot = view1.findViewById<View>(R.id.radioGroupSmsSimSlot) as RadioGroup
        if (smsSettingVo != null) radioGroupSmsSimSlot.check(smsSettingVo.smsSimSlotCheckId)
        val editTextSmsMobiles = view1.findViewById<EditText>(R.id.editTextSmsMobiles)
        if (smsSettingVo != null) editTextSmsMobiles.setText(smsSettingVo.mobiles)
        val switchSmsOnlyNoNetwork = view1.findViewById<SwitchCompat>(R.id.switchSmsOnlyNoNetwork)
        if (smsSettingVo != null) switchSmsOnlyNoNetwork.isChecked = smsSettingVo.onlyNoNetwork!!
        val buttonSmsOk = view1.findViewById<Button>(R.id.buttonSmsOk)
        val buttonSmsDel = view1.findViewById<Button>(R.id.buttonSmsDel)
        val buttonSmsTest = view1.findViewById<Button>(R.id.buttonSmsTest)
        alertDialog71
            .setTitle(R.string.setsmstitle)
            .setIcon(R.mipmap.sms)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonSmsOk.setOnClickListener {
            if (senderModel == null) {
                val newSenderModel = SenderModel()
                newSenderModel.name = editTextSmsName.text.toString()
                newSenderModel.type = SenderModel.TYPE_SMS
                newSenderModel.setStatus(SenderModel.STATUS_ON)

                val smsSettingVoNew = SmsSettingVo(
                    newSenderModel.getSmsSimSlotId(radioGroupSmsSimSlot.checkedRadioButtonId),
                    editTextSmsMobiles.text.toString(),
                    switchSmsOnlyNoNetwork.isChecked
                )
                newSenderModel.jsonSetting = JSON.toJSONString(smsSettingVoNew)
                SenderUtil.addSender(newSenderModel)
                initSenders()
                adapter!!.update(senderModels)
            } else {
                senderModel.name = editTextSmsName.text.toString()
                senderModel.type = SenderModel.TYPE_SMS
                senderModel.setStatus(SenderModel.STATUS_ON)
                val smsSettingVoNew = SmsSettingVo(
                    senderModel.getSmsSimSlotId(radioGroupSmsSimSlot.checkedRadioButtonId),
                    editTextSmsMobiles.text.toString(),
                    switchSmsOnlyNoNetwork.isChecked
                )
                senderModel.jsonSetting = JSON.toJSONString(smsSettingVoNew)
                SenderUtil.updateSender(senderModel)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonSmsDel.setOnClickListener {
            if (senderModel != null) {
                SenderUtil.delSender(senderModel.id)
                initSenders()
                adapter!!.update(senderModels)
            }
            show.dismiss()
        }
        buttonSmsTest.setOnClickListener {
            var simSlot = 0
            if (R.id.btnSmsSimSlot2 == radioGroupSmsSimSlot.checkedRadioButtonId) {
                simSlot = 1
            }
            val mobiles = editTextSmsMobiles.text.toString()
            val onlyNoNetwork = switchSmsOnlyNoNetwork.isChecked
            if (mobiles.isNotEmpty() && mobiles.isNotEmpty()) {
                try {
                    SenderSmsMsg.sendMsg(
                        0,
                        handler,
                        simSlot,
                        mobiles,
                        onlyNoNetwork,
                        "19999999999",
                        "【京东】验证码为387481（切勿将验证码告知他人），请在页面中输入完成验证，如有问题请点击 ihelp.jd.com 联系京东客服"
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@SenderActivity, "发送失败：" + e.message, Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@SenderActivity, "接收手机号不能为空", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    companion object {
        const val NOTIFY = 0x9731993
        private const val TAG = "SenderActivity"
    }
}
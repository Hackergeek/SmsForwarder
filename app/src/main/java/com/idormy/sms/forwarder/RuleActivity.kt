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
import com.idormy.sms.forwarder.adapter.RuleAdapter
import com.idormy.sms.forwarder.model.RuleModel
import com.idormy.sms.forwarder.model.vo.SmsVo
import com.idormy.sms.forwarder.sender.SendUtil
import com.idormy.sms.forwarder.sender.SenderUtil
import com.idormy.sms.forwarder.utils.RuleUtil
import com.idormy.sms.forwarder.utils.SettingUtil
import com.umeng.analytics.MobclickAgent
import java.util.*

class RuleActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "RuleActivity"
    }

    // 用于存储数据
    private var ruleModels: List<RuleModel> = ArrayList()
    private var adapter: RuleAdapter? = null

    //消息处理者,创建一个Handler的子类对象,目的是重写Handler的处理消息的方法(handleMessage())
    private val handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SenderActivity.NOTIFY -> Toast.makeText(
                    this@RuleActivity,
                    msg.data.getString("DATA"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule)
        SenderUtil.init()
    }

    override fun onStart() {
        super.onStart()

        //是否关闭页面提示
        val helpTip = findViewById<TextView>(R.id.help_tip)
        helpTip.visibility = if (MyApplication.showHelpTip) View.VISIBLE else View.GONE

        // 先拿到数据并放在适配器上
        initRules() //初始化数据
        adapter = RuleAdapter(this@RuleActivity, R.layout.item_rule, ruleModels)

        // 将适配器上的数据传递给listView
        val listView = findViewById<ListView>(R.id.list_view_rule)
        listView.adapter = adapter

        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val ruleModel = ruleModels[position]
            Log.d(TAG, "onItemClick: $ruleModel")
            setRule(ruleModel)
        }
        listView.onItemLongClickListener =
            OnItemLongClickListener { _, _, position, _ -> //定义AlertDialog.Builder对象，当长按列表项的时候弹出确认删除对话框
                val builder = AlertDialog.Builder(this@RuleActivity)
                builder.setMessage("确定删除?")
                builder.setTitle("提示")

                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定") { _, _ ->
                    RuleUtil.delRule(ruleModels[position].id)
                    initRules()
                    adapter!!.del(ruleModels)
                    Toast.makeText(baseContext, "删除列表项", Toast.LENGTH_SHORT).show()
                }

                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton("取消") { _, _ -> }
                builder.create().show()
                true
            }
    }

    // 初始化数据
    private fun initRules() {
        ruleModels = RuleUtil.getRule(null, null)
    }

    fun addRule(view: View?) {
        setRule(null)
    }

    private fun setRule(ruleModel: RuleModel?) {
        val alertDialog71 = AlertDialog.Builder(this@RuleActivity)
        val view1 = View.inflate(this@RuleActivity, R.layout.alert_dialog_setview_rule, null)
        val radioGroupRuleFiled = view1.findViewById<View>(R.id.radioGroupRuleFiled) as RadioGroup
        if (ruleModel != null) radioGroupRuleFiled.check(ruleModel.ruleFiledCheckId)
        val radioGroupRuleCheck = view1.findViewById<View>(R.id.radioGroupRuleCheck) as RadioGroup
        val radioGroupRuleCheck2 = view1.findViewById<View>(R.id.radioGroupRuleCheck2) as RadioGroup
        if (ruleModel != null) {
            val ruleCheckCheckId = ruleModel.ruleCheckCheckId
            if (ruleCheckCheckId == R.id.btnIs || ruleCheckCheckId == R.id.btnNotIs || ruleCheckCheckId == R.id.btnContain) {
                radioGroupRuleCheck.check(ruleCheckCheckId)
            } else {
                radioGroupRuleCheck2.check(ruleCheckCheckId)
            }
        } else {
            radioGroupRuleCheck.check(R.id.btnIs)
        }
        val radioGroupSimSlot = view1.findViewById<RadioGroup>(R.id.radioGroupSimSlot)
        if (ruleModel != null) radioGroupSimSlot.check(ruleModel.ruleSimSlotCheckId)
        val tvMuRuleTips = view1.findViewById<TextView>(R.id.tv_mu_rule_tips)
        val ruleSenderTv = view1.findViewById<TextView>(R.id.ruleSenderTv)
        if (ruleModel?.ruleSenderId != null) {
            val getSenders = SenderUtil.getSender(ruleModel.ruleSenderId, null)
            if (getSenders.isNotEmpty()) {
                ruleSenderTv.text = getSenders[0].name
                ruleSenderTv.tag = getSenders[0].id
            }
        }
        val btSetRuleSender = view1.findViewById<View>(R.id.btSetRuleSender) as Button
        btSetRuleSender.setOnClickListener { //Toast.makeText(RuleActivity.this, "selectSender", Toast.LENGTH_LONG).show();
            selectSender(ruleSenderTv)
        }
        val editTextRuleValue = view1.findViewById<EditText>(R.id.editTextRuleValue)
        if (ruleModel != null) editTextRuleValue.setText(ruleModel.value)

        //当更新选择的字段的时候，更新之下各个选项的状态
        val matchTypeLayout = view1.findViewById<View>(R.id.matchTypeLayout) as LinearLayout
        val matchValueLayout = view1.findViewById<View>(R.id.matchValueLayout) as LinearLayout
        refreshSelectRadioGroupRuleFiled(
            radioGroupRuleFiled,
            radioGroupRuleCheck,
            radioGroupRuleCheck2,
            editTextRuleValue,
            tvMuRuleTips,
            matchTypeLayout,
            matchValueLayout
        )
        val buttonruleok = view1.findViewById<Button>(R.id.buttonruleok)
        val buttonruledel = view1.findViewById<Button>(R.id.buttonruledel)
        val buttonruletest = view1.findViewById<Button>(R.id.buttonruletest)
        alertDialog71
            .setTitle(R.string.setrule) //.setIcon(R.drawable.ic_sms_forwarder)
            .setView(view1)
            .create()
        val show = alertDialog71.show()
        buttonruleok.setOnClickListener {
            val ruleSenderId = ruleSenderTv.tag
            val radioGroupRuleCheckId =
                radioGroupRuleCheck.checkedRadioButtonId.coerceAtLeast(radioGroupRuleCheck2.checkedRadioButtonId)
            Log.d(
                TAG,
                "XXXX " + radioGroupRuleCheck.checkedRadioButtonId + "  " + radioGroupRuleCheck2.checkedRadioButtonId + " " + radioGroupRuleCheckId
            )
            if (ruleModel == null) {
                val newRuleModel = RuleModel()
                newRuleModel.filed =
                    RuleModel.getRuleFiledFromCheckId(radioGroupRuleFiled.checkedRadioButtonId)
                newRuleModel.check = RuleModel.getRuleCheckFromCheckId(radioGroupRuleCheckId)
                newRuleModel.simSlot =
                    RuleModel.getRuleSimSlotFromCheckId(radioGroupSimSlot.checkedRadioButtonId)
                newRuleModel.value = editTextRuleValue.text.toString()
                if (ruleSenderId != null) {
                    newRuleModel.ruleSenderId = java.lang.Long.valueOf(ruleSenderId.toString())
                }
                RuleUtil.addRule(newRuleModel)
                initRules()
                adapter!!.add(ruleModels)
            } else {
                ruleModel.filed =
                    RuleModel.getRuleFiledFromCheckId(radioGroupRuleFiled.checkedRadioButtonId)
                ruleModel.check = RuleModel.getRuleCheckFromCheckId(radioGroupRuleCheckId)
                ruleModel.simSlot =
                    RuleModel.getRuleSimSlotFromCheckId(radioGroupSimSlot.checkedRadioButtonId)
                ruleModel.value = editTextRuleValue.text.toString()
                if (ruleSenderId != null) {
                    ruleModel.ruleSenderId = java.lang.Long.valueOf(ruleSenderId.toString())
                }
                RuleUtil.updateRule(ruleModel)
                initRules()
                adapter!!.update(ruleModels)
            }
            show.dismiss()
        }
        buttonruledel.setOnClickListener {
            if (ruleModel != null) {
                RuleUtil.delRule(ruleModel.id)
                initRules()
                adapter!!.del(ruleModels)
            }
            show.dismiss()
        }
        buttonruletest.setOnClickListener {
            val ruleSenderId = ruleSenderTv.tag
            if (ruleSenderId == null) {
                Toast.makeText(this@RuleActivity, "请先创建选择发送方", Toast.LENGTH_LONG).show()
            } else {
                val radioGroupRuleCheckId =
                    radioGroupRuleCheck.checkedRadioButtonId.coerceAtLeast(radioGroupRuleCheck2.checkedRadioButtonId)
                if (ruleModel == null) {
                    val newRuleModel = RuleModel()
                    newRuleModel.filed =
                        RuleModel.getRuleFiledFromCheckId(radioGroupRuleFiled.checkedRadioButtonId)
                    newRuleModel.check = RuleModel.getRuleCheckFromCheckId(radioGroupRuleCheckId)
                    newRuleModel.simSlot =
                        RuleModel.getRuleSimSlotFromCheckId(radioGroupSimSlot.checkedRadioButtonId)
                    newRuleModel.value = editTextRuleValue.text.toString()
                    newRuleModel.ruleSenderId = java.lang.Long.valueOf(ruleSenderId.toString())
                    testRule(newRuleModel, java.lang.Long.valueOf(ruleSenderId.toString()))
                } else {
                    ruleModel.filed =
                        RuleModel.getRuleFiledFromCheckId(radioGroupRuleFiled.checkedRadioButtonId)
                    ruleModel.check = RuleModel.getRuleCheckFromCheckId(radioGroupRuleCheckId)
                    ruleModel.simSlot =
                        RuleModel.getRuleSimSlotFromCheckId(radioGroupSimSlot.checkedRadioButtonId)
                    ruleModel.value = editTextRuleValue.text.toString()
                    ruleModel.ruleSenderId = java.lang.Long.valueOf(ruleSenderId.toString())
                    testRule(ruleModel, java.lang.Long.valueOf(ruleSenderId.toString()))
                }
            }
        }
    }

    //当更新选择的字段的时候，更新之下各个选项的状态
    // 如果设置了转发全部，禁用选择模式和匹配值输入
    // 如果设置了多重规则，选择模式置为是
    private fun refreshSelectRadioGroupRuleFiled(
        radioGroupRuleFiled: RadioGroup,
        radioGroupRuleCheck: RadioGroup,
        radioGroupRuleCheck2: RadioGroup,
        editTextRuleValue: EditText,
        tv_mu_rule_tips: TextView,
        matchTypeLayout: LinearLayout,
        matchValueLayout: LinearLayout
    ) {
        refreshSelectRadioGroupRuleFiledAction(
            radioGroupRuleFiled.checkedRadioButtonId,
            radioGroupRuleCheck,
            radioGroupRuleCheck2,
            editTextRuleValue,
            tv_mu_rule_tips,
            matchTypeLayout,
            matchValueLayout
        )
        radioGroupRuleCheck.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, group.toString())
            Log.d(TAG, checkedId.toString())
            if (group != null && checkedId > 0) {
                if (group === radioGroupRuleCheck) {
                    radioGroupRuleCheck2.clearCheck()
                } else if (group === radioGroupRuleCheck2) {
                    radioGroupRuleCheck.clearCheck()
                }
                group.check(checkedId)
            }
        }
        radioGroupRuleCheck2.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, group.toString())
            Log.d(TAG, checkedId.toString())
            if (group != null && checkedId > 0) {
                if (group === radioGroupRuleCheck) {
                    radioGroupRuleCheck2.clearCheck()
                } else if (group === radioGroupRuleCheck2) {
                    radioGroupRuleCheck.clearCheck()
                }
                group.check(checkedId)
            }
        }
        radioGroupRuleFiled.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, group.toString())
            Log.d(TAG, checkedId.toString())
            if (group === radioGroupRuleCheck) {
                radioGroupRuleCheck2.clearCheck()
            } else if (group === radioGroupRuleCheck2) {
                radioGroupRuleCheck.clearCheck()
            }
            refreshSelectRadioGroupRuleFiledAction(
                checkedId,
                radioGroupRuleCheck,
                radioGroupRuleCheck2,
                editTextRuleValue,
                tv_mu_rule_tips,
                matchTypeLayout,
                matchValueLayout
            )
        }
    }

    private fun refreshSelectRadioGroupRuleFiledAction(
        checkedRuleFiledId: Int,
        radioGroupRuleCheck: RadioGroup,
        radioGroupRuleCheck2: RadioGroup,
        editTextRuleValue: EditText,
        tv_mu_rule_tips: TextView,
        matchTypeLayout: LinearLayout,
        matchValueLayout: LinearLayout
    ) {
        tv_mu_rule_tips.visibility = View.GONE
        matchTypeLayout.visibility = View.VISIBLE
        matchValueLayout.visibility = View.VISIBLE
        when (checkedRuleFiledId) {
            R.id.btnTranspondAll -> {
                run {
                    var i = 0
                    while (i < radioGroupRuleCheck.childCount) {
                        (radioGroupRuleCheck.getChildAt(i) as RadioButton).isEnabled = false
                        i++
                    }
                }
                var i = 0
                while (i < radioGroupRuleCheck2.childCount) {
                    (radioGroupRuleCheck2.getChildAt(i) as RadioButton).isEnabled = false
                    i++
                }
                editTextRuleValue.isEnabled = false
                matchTypeLayout.visibility = View.GONE
                matchValueLayout.visibility = View.GONE
            }
            R.id.btnMultiMatch -> {
                run {
                    var i = 0
                    while (i < radioGroupRuleCheck.childCount) {
                        (radioGroupRuleCheck.getChildAt(i) as RadioButton).isEnabled = false
                        i++
                    }
                }
                var i = 0
                while (i < radioGroupRuleCheck2.childCount) {
                    (radioGroupRuleCheck2.getChildAt(i) as RadioButton).isEnabled = false
                    i++
                }
                editTextRuleValue.isEnabled = true
                matchTypeLayout.visibility = View.GONE
                tv_mu_rule_tips.visibility =
                    if (MyApplication.showHelpTip) View.VISIBLE else View.GONE
            }
            else -> {
                run {
                    var i = 0
                    while (i < radioGroupRuleCheck.childCount) {
                        (radioGroupRuleCheck.getChildAt(i) as RadioButton).isEnabled = true
                        i++
                    }
                }
                var i = 0
                while (i < radioGroupRuleCheck2.childCount) {
                    (radioGroupRuleCheck2.getChildAt(i) as RadioButton).isEnabled = true
                    i++
                }
                editTextRuleValue.isEnabled = true
            }
        }
    }

    fun selectSender(showTv: TextView) {
        val senderModels = SenderUtil.getSender(null, null)
        if (senderModels.isEmpty()) {
            Toast.makeText(this@RuleActivity, "请先去设置发送方页面添加", Toast.LENGTH_SHORT).show()
            return
        }
        val senderNames = arrayOfNulls<CharSequence>(senderModels.size)
        for (i in senderModels.indices) {
            senderNames[i] = senderModels[i].name
        }
        val builder = AlertDialog.Builder(this@RuleActivity)
        builder.setTitle("选择发送方")
        builder.setItems(senderNames) { _, which ->

            //添加列表
            Toast.makeText(this@RuleActivity, senderNames[which], Toast.LENGTH_LONG).show()
            showTv.text = senderNames[which]
            showTv.tag = senderModels[which].id
        }
        builder.show()
    }

    private fun testRule(ruleModel: RuleModel?, ruleSenderId: Long?) {
        val view = View.inflate(this@RuleActivity, R.layout.alert_dialog_setview_rule_test, null)
        val radioGroupTestSimSlot =
            view.findViewById<View>(R.id.radioGroupTestSimSlot) as RadioGroup
        val editTextTestPhone = view.findViewById<View>(R.id.editTextTestPhone) as EditText
        val editTextTestMsgContent =
            view.findViewById<View>(R.id.editTextTestMsgContent) as EditText
        val buttonRuleTest = view.findViewById<Button>(R.id.buttonruletest)
        val ad1 = AlertDialog.Builder(this@RuleActivity)
        ad1.setTitle("测试规则")
        ad1.setIcon(android.R.drawable.ic_dialog_email)
        ad1.setView(view)
        buttonRuleTest.setOnClickListener {
            Log.i("editTextTestPhone", editTextTestPhone.text.toString())
            Log.i("editTextTestMsgContent", editTextTestMsgContent.text.toString())
            try {
                val simSlot =
                    RuleModel.getRuleSimSlotFromCheckId(radioGroupTestSimSlot.checkedRadioButtonId)
                val simInfo = if (simSlot == "SIM2") {
                    simSlot + "_" + SettingUtil.addExtraSim2
                } else {
                    simSlot + "_" + SettingUtil.addExtraSim1
                }
                val testSmsVo = SmsVo(
                    editTextTestPhone.text.toString(),
                    editTextTestMsgContent.text.toString(),
                    Date(),
                    simInfo
                )
                SendUtil.sendMsgByRuleModelSenderId(handler, ruleModel!!, testSmsVo, ruleSenderId)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@RuleActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
        ad1.show() // 显示对话框
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
}
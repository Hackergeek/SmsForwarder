package com.idormy.sms.forwarder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.idormy.sms.forwarder.utils.SettingUtil

class SettingActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SettingActivity"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val switchAddExtra = findViewById<SwitchCompat>(R.id.switch_add_extra)
        switchAddExtra(switchAddExtra)
        val etAddExtraDeviceMark = findViewById<EditText>(R.id.et_add_extra_device_mark)
        editAddExtraDeviceMark(etAddExtraDeviceMark)
        val etAddExtraSim1 = findViewById<EditText>(R.id.et_add_extra_sim1)
        editAddExtraSim1(etAddExtraSim1)
        val etAddExtraSim2 = findViewById<EditText>(R.id.et_add_extra_sim2)
        editAddExtraSim2(etAddExtraSim2)
        val switchSmsTemplate = findViewById<SwitchCompat>(R.id.switch_sms_template)
        switchSmsTemplate(switchSmsTemplate)
        val textSmsTemplate = findViewById<EditText>(R.id.text_sms_template)
        editSmsTemplate(textSmsTemplate)
    }

    //设置转发附加信息
    private fun switchAddExtra(switchAddExtra: SwitchCompat) {
        switchAddExtra.isChecked = SettingUtil.switchAddExtra
        switchAddExtra.setOnCheckedChangeListener { _, isChecked ->
            SettingUtil.switchAddExtra(isChecked)
            Log.d(TAG, "onCheckedChanged:$isChecked")
        }
    }

    //设置转发附加信息device mark
    private fun editAddExtraDeviceMark(et_add_extra_device_mark: EditText) {
        et_add_extra_device_mark.setText(SettingUtil.addExtraDeviceMark)
        et_add_extra_device_mark.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                SettingUtil.setAddExtraDeviceMark(et_add_extra_device_mark.text.toString())
            }
        })
    }

    //设置转发附加信息device mark
    private fun editAddExtraSim1(et_add_extra_sim1: EditText) {
        et_add_extra_sim1.setText(SettingUtil.addExtraSim1)
        et_add_extra_sim1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                SettingUtil.addExtraSim1 = et_add_extra_sim1.text.toString()
            }
        })
    }

    //设置转发附加信息device mark
    private fun editAddExtraSim2(et_add_extra_sim2: EditText) {
        et_add_extra_sim2.setText(SettingUtil.addExtraSim2)
        et_add_extra_sim2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                SettingUtil.addExtraSim2 = et_add_extra_sim2.text.toString()
            }
        })
    }

    //设置转发时启用自定义模版
    private fun switchSmsTemplate(switchSmsTemplate: SwitchCompat) {
        val isOn = SettingUtil.switchSmsTemplate
        switchSmsTemplate.isChecked = isOn
        val layoutSmsTemplate = findViewById<LinearLayout>(R.id.layout_sms_template)
        layoutSmsTemplate.visibility = if (isOn) View.VISIBLE else View.GONE
        val textSmsTemplate = findViewById<EditText>(R.id.text_sms_template)
        switchSmsTemplate.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "onCheckedChanged:$isChecked")
            layoutSmsTemplate.visibility = if (isChecked) View.VISIBLE else View.GONE
            SettingUtil.switchSmsTemplate(isChecked)
            if (!isChecked) {
                textSmsTemplate.setText("{{来源号码}}\n{{短信内容}}\n{{卡槽信息}}\n{{接收时间}}\n{{设备名称}}")
            }
        }
    }

    //设置转发附加信息device mark
    private fun editSmsTemplate(textSmsTemplate: EditText) {
        textSmsTemplate.setText(SettingUtil.smsTemplate)
        textSmsTemplate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                SettingUtil.setSmsTemplate(textSmsTemplate.text.toString())
            }
        })
    }

    //插入标签
    fun toInsertLabel(v: View) {
        val textSmsTemplate = findViewById<EditText>(R.id.text_sms_template)
        textSmsTemplate.isFocusable = true
        textSmsTemplate.requestFocus()
        when (v.id) {
            R.id.bt_insert_sender -> {
                textSmsTemplate.append("{{来源号码}}")
                return
            }
            R.id.bt_insert_content -> {
                textSmsTemplate.append("{{短信内容}}")
                return
            }
            R.id.bt_insert_extra -> {
                textSmsTemplate.append("{{卡槽信息}}")
                return
            }
            R.id.bt_insert_time -> {
                textSmsTemplate.append("{{接收时间}}")
                return
            }
            R.id.bt_insert_device_name -> {
                textSmsTemplate.append("{{设备名称}}")
                return
            }
            else -> return
        }
    }

    //恢复初始化配置
    fun resetSettings(v: View?) {
        val switchAddExtra = findViewById<SwitchCompat>(R.id.switch_add_extra)
        switchAddExtra.isChecked = false
        switchAddExtra(switchAddExtra)
        val etAddExtraDeviceMark = findViewById<EditText>(R.id.et_add_extra_device_mark)
        etAddExtraDeviceMark.setText("")
        editAddExtraDeviceMark(etAddExtraDeviceMark)
        val etAddExtraSim1 = findViewById<EditText>(R.id.et_add_extra_sim1)
        etAddExtraSim1.setText("")
        editAddExtraSim1(etAddExtraSim1)
        val etAddExtraSim2 = findViewById<EditText>(R.id.et_add_extra_sim2)
        etAddExtraSim2.setText("")
        editAddExtraSim2(etAddExtraSim2)
        val switchSmsTemplate = findViewById<SwitchCompat>(R.id.switch_sms_template)
        switchSmsTemplate.isChecked = false
        switchSmsTemplate(switchSmsTemplate)
        val textSmsTemplate = findViewById<EditText>(R.id.text_sms_template)
        textSmsTemplate.setText("{{来源号码}}\n{{短信内容}}\n{{卡槽信息}}\n{{接收时间}}\n{{设备名称}}")
        editSmsTemplate(textSmsTemplate)
    }
}
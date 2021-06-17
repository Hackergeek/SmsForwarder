package com.idormy.sms.forwarder.utils

import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.utils.SimUtil.getSimInfo

object SettingUtil {
    var hasInit = false
    private const val TAG = "SettingUtil"
    private lateinit var sp_setting: SharedPreferences

    @JvmStatic
    fun init() {
        synchronized(hasInit) {
            if (hasInit) return
            hasInit = true
            Log.d(TAG, "init ")
            sp_setting = PreferenceManager.getDefaultSharedPreferences(MyApplication.globalContext)
        }
    }

    fun switchAddExtra(switchAddExtra: Boolean) {
        Log.d(TAG, "switchAddExtra :$switchAddExtra")
        sp_setting.edit()
            .putBoolean(Define.SP_MSG_KEY_SWITCH_ADD_EXTRA, switchAddExtra)
            .apply()
    }

    /**
     * 转发时是否附加卡槽信息
     */
    @JvmStatic
    val switchAddExtra: Boolean
        get() = sp_setting.getBoolean(Define.SP_MSG_KEY_SWITCH_ADD_EXTRA, false)

    fun switchSmsTemplate(switchSmsTemplate: Boolean) {
        Log.d(TAG, "switchSmsTemplate :$switchSmsTemplate")
        sp_setting.edit()
            .putBoolean(Define.SP_MSG_KEY_SWITCH_SMS_TEMPLATE, switchSmsTemplate)
            .apply()
    }

    /**
     * 转发时是否启动自定义模版
     */
    @JvmStatic
    val switchSmsTemplate: Boolean
        get() = sp_setting.getBoolean(Define.SP_MSG_KEY_SWITCH_SMS_TEMPLATE, false)

    /**
     * 设备名称
     */
    @JvmStatic
    val addExtraDeviceMark: String?
        get() {
            var res = sp_setting.getString(Define.SP_MSG_KEY_STRING_ADD_EXTRA_DEVICE_MARK, "")
            if (res == null || res == "") {
                res = Build.MODEL
            }
            return res
        }

    fun setAddExtraDeviceMark(addExtraDeviceMark: String) {
        Log.d(TAG, "addExtraDeviceMark :$addExtraDeviceMark")
        sp_setting.edit()
            .putString(Define.SP_MSG_KEY_STRING_ADD_EXTRA_DEVICE_MARK, addExtraDeviceMark)
            .apply()
    }

    /**
     * 转发信息模版
     */
    @JvmStatic
    val smsTemplate: String?
        get() = sp_setting.getString(
            Define.SP_MSG_KEY_STRING_SMS_TEMPLATE,
            "{{来源号码}}\n{{短信内容}}\n{{卡槽信息}}\n{{接收时间}}\n{{设备名称}}"
        )

    fun setSmsTemplate(textSmsTemplate: String) {
        Log.d(TAG, "textSmsTemplate :$textSmsTemplate")
        sp_setting.edit()
            .putString(Define.SP_MSG_KEY_STRING_SMS_TEMPLATE, textSmsTemplate)
            .apply()
    }

    @JvmStatic
    var addExtraSim1: String
        get() {
            var res = sp_setting.getString(Define.SP_MSG_KEY_STRING_ADD_EXTRA_SIM1, "")
            if (res == null || res == "") {
                res = getSimInfo(1)
            }
            return res
        }
        set(sim1) {
            Log.d(TAG, "sim1 :$sim1")
            sp_setting.edit()
                .putString(Define.SP_MSG_KEY_STRING_ADD_EXTRA_SIM1, sim1)
                .apply()
        }

    @JvmStatic
    var addExtraSim2: String
        get() {
            var res = sp_setting.getString(Define.SP_MSG_KEY_STRING_ADD_EXTRA_SIM2, "")
            if (res == null || res == "") {
                res = getSimInfo(2)
            }
            return res
        }
        set(sim2) {
            Log.d(TAG, "sim2 :$sim2")
            sp_setting.edit()
                .putString(Define.SP_MSG_KEY_STRING_ADD_EXTRA_SIM2, sim2)
                .apply()
        }

    @JvmStatic
    fun saveMsgHistory(): Boolean {
        return sp_setting.getBoolean("option_save_history_on", false)
    }
}
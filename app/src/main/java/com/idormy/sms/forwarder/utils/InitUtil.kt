package com.idormy.sms.forwarder.utils

import android.util.Log

object InitUtil {
    var hasInit = false
    private const val TAG = "InitUtil"
    @JvmStatic
    fun init() {
        Log.d(TAG, "TMSG init")
        synchronized(hasInit) {
            if (hasInit) return
            hasInit = true
            Log.d(TAG, "init context")
            SettingUtil.init()
        }
    }
}
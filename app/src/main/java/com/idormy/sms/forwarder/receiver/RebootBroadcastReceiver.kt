package com.idormy.sms.forwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.idormy.sms.forwarder.ForegroundService
import com.idormy.sms.forwarder.utils.InitUtil.init

class RebootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receiveAction = intent.action
        Log.d(TAG, "onReceive intent $receiveAction")
        if (receiveAction == "android.intent.action.BOOT_COMPLETED") {
            Log.d(TAG, "BOOT_COMPLETED")
            init()
            val frontServiceIntent = Intent(context, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(frontServiceIntent)
            } else {
                context.startService(frontServiceIntent)
            }
        }
    }

    companion object {
        const val TAG = "RebootBroadcastReceiver"
    }
}
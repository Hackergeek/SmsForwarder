package com.idormy.sms.forwarder.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.idormy.sms.forwarder.MyApplication
import java.util.*

object SmsUtil {
    var TAG = "SmsUtil"

    @JvmStatic
    fun sendSms(subId: Int, mobiles: String, message: String): String? {
        var mobiles = mobiles
        mobiles = mobiles.replace("；", ";")
        Log.d(TAG, "subId = $subId, mobiles = $mobiles, message = $message")
        return try {
            val smsManager = SmsManager.getSmsManagerForSubscriptionId(subId)
            val sendPI = PendingIntent.getBroadcast(
                MyApplication.globalContext,
                0,
                Intent(Context.TELEPHONY_SUBSCRIPTION_SERVICE),
                PendingIntent.FLAG_ONE_SHOT
            )
            val deliverPI =
                PendingIntent.getBroadcast(
                    MyApplication.globalContext,
                    0,
                    Intent("DELIVERED_SMS_ACTION"),
                    0
                )
            val sentPendingIntents = ArrayList<PendingIntent>()
            val deliveredPendingIntents = ArrayList<PendingIntent>()
            val divideContents = smsManager.divideMessage(message)
            for (i in divideContents.indices) {
                sentPendingIntents.add(i, sendPI)
                deliveredPendingIntents.add(i, deliverPI)
            }
            smsManager.sendMultipartTextMessage(
                mobiles,
                null,
                divideContents,
                sentPendingIntents,
                deliveredPendingIntents
            )
            null
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            e.message
        }
    }
}
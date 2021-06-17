package com.idormy.sms.forwarder.utils

import android.os.Bundle
import android.util.Log
import com.idormy.sms.forwarder.MyApplication

object SimUtil {
    private const val TAG = "SimUtil"

    //获取卡槽信息ID
    fun getSimId(bundle: Bundle?): Int {
        var whichSIM = -1
        if (bundle == null) {
            return whichSIM
        }
        if (bundle.containsKey("simId")) {
            whichSIM = bundle.getInt("simId")
            Log.d(TAG, "simId = $whichSIM")
        } else if (bundle.containsKey("com.android.phone.extra.slot")) {
            whichSIM = bundle.getInt("com.android.phone.extra.slot")
            Log.d(TAG, "com.android.phone.extra.slot = $whichSIM")
        } else {
            var keyName: String? = ""
            for (key in bundle.keySet()) {
                if (key.contains("sim")) keyName = key
            }
            if (bundle.containsKey(keyName)) {
                whichSIM = bundle.getInt(keyName)
            }
        }
        Log.d(TAG, "Slot Number $whichSIM")
        return whichSIM + 1
    }

    //通过SubscriptionId获取卡槽信息ID
    @JvmStatic
    fun getSimIdBySubscriptionId(subscriptionId: Int): Int {
        try {
            for (simInfo in MyApplication.SimInfoList) {
                if (simInfo.mSubscriptionId == subscriptionId) {
                    return simInfo.mSimSlotIndex + 1
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getSimExtra Fail: " + e.message)
        }
        return 0
    }

    //通过卡槽ID获取SubscriptionId
    @JvmStatic
    fun getSubscriptionIdBySimId(simId: Int): Int {
        try {
            for (simInfo in MyApplication.SimInfoList) {
                Log.d(TAG, "mSimSlotIndex = " + simInfo.mSimSlotIndex)
                if (simInfo.mSimSlotIndex != -1 && simInfo.mSimSlotIndex == simId) {
                    return simInfo.mSubscriptionId
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getSimExtra Fail: " + e.message)
        }
        return 0
    }

    //获取卡槽备注
    @JvmStatic
    fun getSimInfo(simId: Int): String {
        var res = ""
        try {
            for (simInfo in MyApplication.SimInfoList) {
                Log.d(TAG, simInfo.toString())
                if (simInfo.mSimSlotIndex != -1 && simInfo.mSimSlotIndex + 1 == simId) {
                    res = simInfo.mCarrierName.toString() + "_" + simInfo.mNumber
                    break
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getSimExtra Fail: " + e.message)
        }
        return res.replace("null", "unknown")
    }
}
package com.idormy.sms.forwarder.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.idormy.sms.forwarder.MyApplication
import java.io.File
import java.util.*

class PhoneUtils private constructor() {
    /**
     * SIM 卡信息
     */
    class SimInfo {
        /**
         * 运营商信息：中国移动 中国联通 中国电信
         */
        var mCarrierName: CharSequence? = null

        /**
         * 卡槽ID，SimSerialNumber
         */
        var mIccId: CharSequence? = null

        /**
         * 卡槽id， -1 - 没插入、 0 - 卡槽1 、1 - 卡槽2
         */
        var mSimSlotIndex = 0

        /**
         * 号码
         */
        var mNumber: CharSequence? = null

        /**
         * 城市
         */
        var mCountryIso: CharSequence? = null

        /**
         * 设备唯一识别码
         */
        var mImei: CharSequence? = iMEI

        /**
         * SIM的编号
         */
        var mImsi: CharSequence? = null

        /**
         * SIM的 Subscription Id (SIM插入顺序)
         */
        var mSubscriptionId = 0

        /**
         * 通过 IMEI 判断是否相等
         *
         * @param other
         * @return
         */
        override fun equals(other: Any?): Boolean {
            return other != null && other is SimInfo && (TextUtils.isEmpty(other.mImei) || other.mImei == mImei)
        }

        override fun toString(): String {
            return "SimInfo{" +
                    "mCarrierName=" + mCarrierName +
                    ", mIccId=" + mIccId +
                    ", mSimSlotIndex=" + mSimSlotIndex +
                    ", mNumber=" + mNumber +
                    ", mCountryIso=" + mCountryIso +
                    ", mImei=" + mImei +
                    ", mImsi=" + mImsi +
                    ", mSubscriptionId=" + mSubscriptionId +
                    '}'
        }
    }

    /**
     * 反射未找到方法
     */
    private class MethodNotFoundException(info: String?) : Exception(info) {
        companion object {
            const val serialVersionUID = -3241033488141442594L
        }
    }

    companion object {
        private var hasInit = false
        private const val TAG = "PhoneUtils"
        fun init() {
            synchronized(hasInit) {
                if (hasInit) return
                hasInit = true
            }
        }

        /**
         * 判断设备是否是手机
         *
         * @return `true`: 是<br></br>`false`: 否
         */
        val isPhone: Boolean
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return tm != null && tm.phoneType != TelephonyManager.PHONE_TYPE_NONE
            }

        /**
         * 判断设备是否root
         *
         * @return the boolean`true`: 是<br></br>`false`: 否
         */
        val isDeviceRoot: Boolean
            get() {
                val su = "su"
                val locations = arrayOf(
                    "/system/bin/",
                    "/system/xbin/",
                    "/sbin/",
                    "/system/sd/xbin/",
                    "/system/bin/failsafe/",
                    "/data/local/xbin/",
                    "/data/local/bin/",
                    "/data/local/"
                )
                for (location in locations) {
                    if (File(location + su).exists()) {
                        return true
                    }
                }
                return false
            }

        /**
         * 获取设备系统版本号
         *
         * @return 设备系统版本号
         */
        val sDKVersion: Int
            get() = Build.VERSION.SDK_INT

        /**
         * 获取设备AndroidID
         *
         * @return AndroidID
         */
        @get:SuppressLint("HardwareIds")
        val androidID: String
            get() = Settings.Secure.getString(
                MyApplication.globalContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )

        /**
         * 获取IMEI码
         *
         * 需添加权限 `<uses-permission android:name="android.permission.READ_PHONE_STATE"/>`
         *
         * @return IMEI码
         */
        @get:SuppressLint("HardwareIds")
        val iMEI: String?
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                try {
                    return tm.deviceId
                } catch (ignored: Exception) {
                }
                return uniquePsuedoID
            }//获取失败，使用AndroidId

        /**
         * 通过读取设备的ROM版本号、厂商名、CPU型号和其他硬件信息来组合出一串15位的号码
         * 其中“Build.SERIAL”这个属性来保证ID的独一无二，当API < 9 无法读取时，使用AndroidId
         *
         * @return 伪唯一ID
         */
        val uniquePsuedoID: String
            get() {
                val mSzdevidshort =
                    "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
                var serial: String
                try {
                    serial = Build::class.java.getField("SERIAL")[null].toString()
                    return UUID(
                        mSzdevidshort.hashCode().toLong(), serial.hashCode().toLong()
                    ).toString()
                } catch (e: Exception) {
                    //获取失败，使用AndroidId
                    serial = androidID
                    if (TextUtils.isEmpty(serial)) {
                        serial = "serial"
                    }
                }
                return UUID(
                    mSzdevidshort.hashCode().toLong(), serial.hashCode().toLong()
                ).toString()
            }

        /**
         * 获取IMSI码
         *
         * 需添加权限 `<uses-permission android:name="android.permission.READ_PHONE_STATE"/>`
         *
         * @return IMSI码
         */
        @get:SuppressLint("HardwareIds")
        val iMSI: String?
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                try {
                    return tm.subscriberId
                } catch (ignored: Exception) {
                }
                return null
            }

        /**
         * 判断sim卡是否准备好
         *
         * @return `true`: 是<br></br>`false`: 否
         */
        val isSimCardReady: Boolean
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return tm != null && tm.simState == TelephonyManager.SIM_STATE_READY
            }

        /**
         * 获取Sim卡运营商名称
         *
         * 中国移动、如中国联通、中国电信
         *
         * @return sim卡运营商名称
         */
        val simOperatorName: String?
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return tm.simOperatorName
            }

        /**
         * 获取Sim卡运营商名称
         *
         * 中国移动、如中国联通、中国电信
         *
         * @return 移动网络运营商名称
         */
        val simOperatorByMnc: String?
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val operator = (if (tm != null) tm.simOperator else null) ?: return null
                return when (operator) {
                    "46000", "46002", "46007" -> "中国移动"
                    "46001" -> "中国联通"
                    "46003" -> "中国电信"
                    else -> operator
                }
            }

        /**
         * 获取Sim卡序列号
         *
         *
         * Requires Permission:
         * [READ_PHONE_STATE][android.Manifest.permission.READ_PHONE_STATE]
         *
         * @return 序列号
         */
        val simSerialNumber: String
            @SuppressLint("HardwareIds")
            get() {
                try {
                    val tm =
                        MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    val serialNumber = tm.simSerialNumber
                    return serialNumber ?: ""
                } catch (e: Exception) {
                }
                return ""
            }

        /**
         * 获取Sim卡的国家代码
         *
         * @return 国家代码
         */
        val simCountryIso: String?
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return tm.simCountryIso
            }

        /**
         * 读取电话号码
         *
         *
         * Requires Permission:
         * [READ_PHONE_STATE][android.Manifest.permission.READ_PHONE_STATE]
         * OR
         * [android.Manifest.permission.READ_SMS]
         *
         *
         *
         * @return 电话号码
         */
        @get:SuppressLint("MissingPermission", "HardwareIds")
        val phoneNumber: String?
            get() {
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                try {
                    return if (ActivityCompat.checkSelfPermission(
                            MyApplication.globalContext,
                            Manifest.permission.READ_SMS
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            MyApplication.globalContext, Manifest.permission.READ_PHONE_NUMBERS
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            MyApplication.globalContext, Manifest.permission.READ_PHONE_STATE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        null
                    } else tm.line1Number
                } catch (ignored: Exception) {
                }
                return null
            }

        /**
         * 获得卡槽数，默认为1
         *
         * @return 返回卡槽数
         */
        val simCount: Int
            get() {
                var count = 1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    try {
                        val mSubscriptionManager =
                            MyApplication.globalContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                        if (mSubscriptionManager != null) {
                            count = mSubscriptionManager.activeSubscriptionInfoCountMax
                            return count
                        }
                    } catch (ignored: Exception) {
                    }
                }
                try {
                    count = getReflexMethod(MyApplication.globalContext, "getPhoneCount")!!
                        .toInt()
                } catch (ignored: MethodNotFoundException) {
                }
                return count
            }

        /**
         * 获取Sim卡使用的数量
         *
         * @return 0, 1, 2
         */
        val simUsedCount: Int
            get() {
                var count = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    try {
                        val mSubscriptionManager =
                            MyApplication.globalContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                        if (ActivityCompat.checkSelfPermission(
                                MyApplication.globalContext,
                                Manifest.permission.READ_PHONE_STATE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return count
                        }
                        count = mSubscriptionManager.activeSubscriptionInfoCount
                        return count
                    } catch (ignored: Exception) {
                    }
                }
                val tm =
                    MyApplication.globalContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (tm.simState == TelephonyManager.SIM_STATE_READY) {
                    count = 1
                }
                try {
                    if (getReflexMethodWithId(MyApplication.globalContext, "getSimState", "1")!!
                            .toInt() == TelephonyManager.SIM_STATE_READY
                    ) {
                        count = 2
                    }
                } catch (ignored: MethodNotFoundException) {
                }
                return count
            }//访问raw_contacts表

        /*Log.d(TAG, "3.通过反射读取卡槽信息，最后通过IMEI去重");
           //3.通过反射读取卡槽信息，最后通过IMEI去重
           for (int i = 0; i < getSimCount(); i++) {
               infos.add(getReflexSimInfo(MyApplication.globalContext, i));
           }
           List<SimInfo> simInfos = removeDuplicateWithOrder(infos);
           if (simInfos.size() < getSimCount()) {
               for (int i = simInfos.size(); i < getSimCount(); i++) {
                   simInfos.add(new SimInfo());
               }
           }
           return simInfos;*///2.版本低于5.1的系统，首先调用数据库，看能不能访问到//1.1.1 有使用的卡，就遍历所有卡//TODO//1.版本超过5.1，调用系统方法
        /**
         * 获取多卡信息
         *
         * @return 多Sim卡的具体信息
         */
        val simMultiInfo: List<SimInfo>
            get() {
                val infos: MutableList<SimInfo> = ArrayList()
                Log.d(TAG, "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT)
                Log.d(TAG, "Build.VERSION_CODES.LOLLIPOP_MR1 = " + Build.VERSION_CODES.LOLLIPOP_MR1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Log.d(TAG, "1.版本超过5.1，调用系统方法")
                    //1.版本超过5.1，调用系统方法
                    val mSubscriptionManager =
                        MyApplication.globalContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                    var activeSubscriptionInfoList: List<SubscriptionInfo>? = null
                    if (mSubscriptionManager != null) {
                        try {
                            if (ActivityCompat.checkSelfPermission(
                                    MyApplication.globalContext,
                                    Manifest.permission.READ_PHONE_STATE
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                //TODO
                            }
                            activeSubscriptionInfoList =
                                mSubscriptionManager.activeSubscriptionInfoList
                        } catch (ignored: Exception) {
                        }
                    }
                    if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                        //1.1.1 有使用的卡，就遍历所有卡
                        for (subscriptionInfo in activeSubscriptionInfoList) {
                            val simInfo = SimInfo()
                            simInfo.mCarrierName = subscriptionInfo.carrierName
                            simInfo.mIccId = subscriptionInfo.iccId
                            simInfo.mSimSlotIndex = subscriptionInfo.simSlotIndex
                            simInfo.mNumber = subscriptionInfo.number
                            simInfo.mCountryIso = subscriptionInfo.countryIso
                            simInfo.mSubscriptionId = subscriptionInfo.subscriptionId
                            try {
                                simInfo.mImei = getReflexMethodWithId(
                                    MyApplication.globalContext,
                                    "getDeviceId",
                                    simInfo.mSimSlotIndex.toString()
                                )
                                simInfo.mImsi = getReflexMethodWithId(
                                    MyApplication.globalContext,
                                    "getSubscriberId",
                                    subscriptionInfo.subscriptionId.toString()
                                )
                            } catch (ignored: MethodNotFoundException) {
                            }
                            Log.d(TAG, simInfo.toString())
                            infos.add(simInfo)
                        }
                    }
                } else {
                    Log.d(TAG, "2.版本低于5.1的系统，首先调用数据库，看能不能访问到")
                    //2.版本低于5.1的系统，首先调用数据库，看能不能访问到
                    val uri = Uri.parse("content://telephony/siminfo") //访问raw_contacts表
                    val resolver = MyApplication.globalContext.contentResolver
                    val cursor = resolver.query(
                        uri,
                        arrayOf(
                            "_id",
                            "icc_id",
                            "sim_id",
                            "display_name",
                            "carrier_name",
                            "name_source",
                            "color",
                            "number",
                            "display_number_format",
                            "data_roaming",
                            "mcc",
                            "mnc"
                        ),
                        null,
                        null,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            val simInfo = SimInfo()
                            simInfo.mCarrierName =
                                cursor.getString(cursor.getColumnIndex("carrier_name"))
                            simInfo.mIccId = cursor.getString(cursor.getColumnIndex("icc_id"))
                            simInfo.mSimSlotIndex = cursor.getInt(cursor.getColumnIndex("sim_id"))
                            simInfo.mNumber = cursor.getString(cursor.getColumnIndex("number"))
                            simInfo.mCountryIso = cursor.getString(cursor.getColumnIndex("mcc"))
                            val id = cursor.getString(cursor.getColumnIndex("_id"))
                            try {
                                simInfo.mImei = getReflexMethodWithId(
                                    MyApplication.globalContext,
                                    "getDeviceId",
                                    simInfo.mSimSlotIndex.toString()
                                )
                                simInfo.mImsi =
                                    getReflexMethodWithId(
                                        MyApplication.globalContext,
                                        "getSubscriberId",
                                        id.toString()
                                    )
                            } catch (ignored: MethodNotFoundException) {
                            }
                            Log.d(TAG, simInfo.toString())
                            infos.add(simInfo)
                        } while (cursor.moveToNext())
                        cursor.close()
                    }
                }

                /*Log.d(TAG, "3.通过反射读取卡槽信息，最后通过IMEI去重");
                   //3.通过反射读取卡槽信息，最后通过IMEI去重
                   for (int i = 0; i < getSimCount(); i++) {
                       infos.add(getReflexSimInfo(MyApplication.globalContext, i));
                   }
                   List<SimInfo> simInfos = removeDuplicateWithOrder(infos);
                   if (simInfos.size() < getSimCount()) {
                       for (int i = simInfos.size(); i < getSimCount(); i++) {
                           simInfos.add(new SimInfo());
                       }
                   }
                   return simInfos;*/return infos
            }
        val secondIMSI: String?
            get() {
                val maxCount = 20
                if (TextUtils.isEmpty(iMSI)) {
                    return null
                }
                for (i in 0 until maxCount) {
                    var imsi: String? = null
                    try {
                        imsi = getReflexMethodWithId(
                            MyApplication.globalContext,
                            "getSubscriberId",
                            i.toString()
                        )
                    } catch (ignored: MethodNotFoundException) {
                        Log.d(TAG, ignored.toString())
                    }
                    if (!TextUtils.isEmpty(imsi) && imsi != iMSI) {
                        return imsi
                    }
                }
                return null
            }

        /**
         * 通过反射获得SimInfo的信息
         * 当index为0时，读取默认信息
         *
         * @param index 位置,用来当subId和phoneId
         * @return [SimInfo] sim信息
         */
        private fun getReflexSimInfo(context: Context, index: Int): SimInfo {
            val simInfo = SimInfo()
            simInfo.mSimSlotIndex = index
            try {
                simInfo.mImei =
                    getReflexMethodWithId(context, "getDeviceId", simInfo.mSimSlotIndex.toString())
                //slotId,比较准确
                simInfo.mImsi = getReflexMethodWithId(
                    context,
                    "getSubscriberId",
                    simInfo.mSimSlotIndex.toString()
                )
                //subId,很不准确
                simInfo.mCarrierName = getReflexMethodWithId(
                    context,
                    "getSimOperatorNameForPhone",
                    simInfo.mSimSlotIndex.toString()
                )
                //PhoneId，基本准确
                simInfo.mCountryIso = getReflexMethodWithId(
                    context,
                    "getSimCountryIso",
                    simInfo.mSimSlotIndex.toString()
                )
                //subId，很不准确
                simInfo.mIccId = getReflexMethodWithId(
                    context,
                    "getSimSerialNumber",
                    simInfo.mSimSlotIndex.toString()
                )
                //subId，很不准确
                simInfo.mNumber = getReflexMethodWithId(
                    context,
                    "getLine1Number",
                    simInfo.mSimSlotIndex.toString()
                )
                //subId，很不准确
            } catch (ignored: MethodNotFoundException) {
            }
            return simInfo
        }

        /**
         * 通过反射调取@hide的方法
         *
         * @param predictedMethodName 方法名
         * @return 返回方法调用的结果
         * @throws MethodNotFoundException 方法没有找到
         */
        @Throws(MethodNotFoundException::class)
        private fun getReflexMethod(context: Context?, predictedMethodName: String): String? {
            var result: String? = null
            val telephony =
                context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val telephonyClass = Class.forName(telephony.javaClass.name)
                val getSimID = telephonyClass.getMethod(predictedMethodName)
                val obPhone = getSimID.invoke(telephony)
                if (obPhone != null) {
                    result = obPhone.toString()
                }
            } catch (e: Exception) {
                Log.d(TAG, e.fillInStackTrace().toString())
                throw MethodNotFoundException(predictedMethodName)
            }
            return result
        }

        /**
         * 通过反射调取@hide的方法
         *
         * @param predictedMethodName 方法名
         * @param id                  参数
         * @return 返回方法调用的结果
         * @throws MethodNotFoundException 方法没有找到
         */
        @Throws(MethodNotFoundException::class)
        private fun getReflexMethodWithId(
            context: Context?,
            predictedMethodName: String,
            id: String
        ): String? {
            var result: String? = null
            val telephony =
                context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val telephonyClass = Class.forName(telephony.javaClass.name)
                val parameter = arrayOfNulls<Class<*>?>(1)
                parameter[0] = Int::class.javaPrimitiveType
                val getSimID = telephonyClass.getMethod(predictedMethodName, *parameter)
                val parameterTypes = getSimID.parameterTypes
                val obParameter = arrayOfNulls<Any>(parameterTypes.size)
                if (parameterTypes[0].simpleName == "int") {
                    obParameter[0] = Integer.valueOf(id)
                } else if (parameterTypes[0].simpleName == "long") {
                    obParameter[0] = java.lang.Long.valueOf(id)
                } else {
                    obParameter[0] = id
                }
                val obPhone = getSimID.invoke(telephony, *obParameter)
                if (obPhone != null) {
                    result = obPhone.toString()
                }
            } catch (e: Exception) {
                Log.d(TAG, e.fillInStackTrace().toString())
                throw MethodNotFoundException(predictedMethodName)
            }
            return result
        }

        // 检查权限是否获取（android6.0及以上系统可能默认关闭权限，且没提示）
        fun checkPermission(pm: PackageManager, that: Context) {
            //PackageManager pm = getPackageManager();
            val permissionInternet = PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                "android.permission.INTERNET",
                that.packageName
            )
            val permissionReceiveBoot = PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                "android.permission.RECEIVE_BOOT_COMPLETED",
                that.packageName
            )
            val permissionForegroundService =
                PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                    "android.permission.FOREGROUND_SERVICE",
                    that.packageName
                )
            val permissionReadExternalStorage =
                PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                    "android.permission.READ_EXTERNAL_STORAGE",
                    that.packageName
                )
            val permissionWriteExternalStorage =
                PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    that.packageName
                )
            val permissionReceiveSms = PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                "android.permission.RECEIVE_SMS",
                that.packageName
            )
            val permissionReadSms = PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                "android.permission.READ_SMS",
                that.packageName
            )
            val permissionSendSms = PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                "android.permission.SEND_SMS",
                that.packageName
            )
            val permissionReadPhoneState =
                PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                    "android.permission.READ_PHONE_STATE",
                    that.packageName
                )
            val permissionReadPhoneNumbers =
                PackageManager.PERMISSION_GRANTED == pm.checkPermission(
                    "android.permission.READ_PHONE_NUMBERS",
                    that.packageName
                )
            if (!(permissionInternet && permissionReceiveBoot && permissionForegroundService &&
                        permissionReadExternalStorage && permissionWriteExternalStorage &&
                        permissionReceiveSms && permissionReadSms && permissionSendSms &&
                        permissionReadPhoneState && permissionReadPhoneNumbers)
            ) {
                ActivityCompat.requestPermissions(
                    (that as Activity), arrayOf(
                        Manifest.permission.INTERNET,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_PHONE_NUMBERS,
                        Manifest.permission.FOREGROUND_SERVICE
                    ), 0x01
                )
            }
        }
    }

    /**
     * 构造类
     */
    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
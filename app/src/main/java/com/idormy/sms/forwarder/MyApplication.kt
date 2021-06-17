package com.idormy.sms.forwarder

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.idormy.sms.forwarder.sender.SendHistory
import com.idormy.sms.forwarder.utils.Define
import com.idormy.sms.forwarder.utils.PhoneUtils.SimInfo
import com.idormy.sms.forwarder.utils.SettingUtil
import com.smailnet.emailkit.EmailKit
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import java.util.*

class MyApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        globalContext = this
        //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口。
        //建议在宿主App的Application.onCreate函数中调用基础组件库初始化函数。
        UMConfigure.init(
            this,
            "60254fc7425ec25f10f4293e",
            getChannelName(this),
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        // 选用LEGACY_AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_MANUAL)
        //pro close log
        UMConfigure.setLogEnabled(true)
        Log.i(TAG, "uminit")
        val intent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        SendHistory.init()
        SettingUtil.init()
        EmailKit.initialize(this)
        val sp = getSharedPreferences(Define.SP_CONFIG, MODE_PRIVATE)
        showHelpTip = sp.getBoolean(Define.SP_CONFIG_SWITCH_HELP_TIP, true)
    }

    companion object {
        private const val TAG = "MyApplication"
        lateinit var globalContext:Context
        //SIM卡信息
        @JvmField
        var SimInfoList: List<SimInfo> = ArrayList()

        //是否关闭页面提示
        @JvmField
        var showHelpTip = true

        //企业微信
        @JvmField
        var QyWxAccessToken: String? = null

        @JvmField
        var QyWxAccessTokenExpiresIn: Long = 0

        /**
         * <meta-data android:name="UMENG_CHANNEL" android:value="Umeng">
        </meta-data> *
         *
         * @param ctx
         * @return
         */
        // 获取渠道工具函数
        fun getChannelName(ctx: Context?): String? {
            if (ctx == null) {
                return null
            }
            var channelName: String? = null
            try {
                val packageManager = ctx.packageManager
                if (packageManager != null) {
                    //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是activity标签中，所以用ApplicationInfo
                    val applicationInfo = packageManager.getApplicationInfo(
                        ctx.packageName,
                        PackageManager.GET_META_DATA
                    )
                    if (applicationInfo != null) {
                        if (applicationInfo.metaData != null) {
                            channelName = applicationInfo.metaData["UMENG_CHANNEL"].toString() + ""
                        }
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            if (TextUtils.isEmpty(channelName)) {
                channelName = "Unknown"
            }
            Log.d(TAG, "getChannelName: $channelName")
            return channelName
        }
    }
}
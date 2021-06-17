package com.idormy.sms.forwarder

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.idormy.sms.forwarder.receiver.RebootBroadcastReceiver
import com.idormy.sms.forwarder.utils.CacheUtil
import com.idormy.sms.forwarder.utils.Define
import com.idormy.sms.forwarder.utils.Util
import com.xuexiang.xupdate.easy.EasyUpdate
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker

class AboutActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AboutActivity"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        Log.d(TAG, "onCreate: " + RebootBroadcastReceiver::class.java.name)
        val checkWithReboot = findViewById<SwitchCompat>(R.id.switch_with_reboot)
        checkWithReboot(checkWithReboot)
        val switchHelpTip = findViewById<SwitchCompat>(R.id.switch_help_tip)
        switchHelpTip(switchHelpTip)
        val versionNow = findViewById<TextView>(R.id.version_now)
        val checkVersionNow = findViewById<Button>(R.id.check_version_now)
        try {
            versionNow.text = Util.getVersionName(this@AboutActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        checkVersionNow.setOnClickListener {
            //checkNewVersion();
            try {
                var updateUrl: String? =
                    "https://xupdate.bms.ink/update/checkVersion?appKey=com.idormy.sms.forwarder&versionCode="
                updateUrl += Util.getVersionCode(this@AboutActivity)
                EasyUpdate.create(this@AboutActivity, updateUrl!!)
                    .updateChecker(object : DefaultUpdateChecker() {
                        override fun onBeforeCheck() {
                            super.onBeforeCheck()
                            Toast.makeText(this@AboutActivity, "查询中...", Toast.LENGTH_LONG).show()
                        }

                        override fun onAfterCheck() {
                            super.onAfterCheck()
                        }

                        override fun noNewVersion(throwable: Throwable) {
                            super.noNewVersion(throwable)
                            // 没有最新版本的处理
                            Toast.makeText(this@AboutActivity, "已是最新版本！", Toast.LENGTH_LONG).show()
                        }
                    })
                    .update()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val cacheSize = findViewById<TextView>(R.id.cache_size)
        try {
            cacheSize.text = CacheUtil.getTotalCacheSize(this@AboutActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val clearAllCache = findViewById<Button>(R.id.clear_all_cache)
        clearAllCache.setOnClickListener {
            CacheUtil.clearAllCache(this@AboutActivity)
            try {
                cacheSize.text = CacheUtil.getTotalCacheSize(this@AboutActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Toast.makeText(this@AboutActivity, "缓存清理完成", Toast.LENGTH_LONG).show()
        }
        val joinQqGroup = findViewById<Button>(R.id.join_qq_group)
        joinQqGroup.setOnClickListener {
            val key = "HvroJRfvK7GGfnQgaIQ4Rh1un9O83N7M"
            joinQQGroup(key)
        }
    }

    //检查重启广播接受器状态并设置
    private fun checkWithReboot(withRebootSwitch: SwitchCompat) {
        //获取组件
        val cm = ComponentName(this.packageName, RebootBroadcastReceiver::class.java.name)
        val pm = packageManager
        val state = pm.getComponentEnabledSetting(cm)
        withRebootSwitch.isChecked = (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                && state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
        withRebootSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newState =
                if (isChecked) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            pm.setComponentEnabledSetting(cm, newState, PackageManager.DONT_KILL_APP)
            Log.d(TAG, "onCheckedChanged:$isChecked")
        }
    }

    //页面帮助提示
    private fun switchHelpTip(switchHelpTip: SwitchCompat) {
        switchHelpTip.isChecked = MyApplication.showHelpTip
        switchHelpTip.setOnCheckedChangeListener { _, isChecked ->
            MyApplication.showHelpTip = isChecked
            val sp = getSharedPreferences(Define.SP_CONFIG, MODE_PRIVATE)
            sp.edit().putBoolean(Define.SP_CONFIG_SWITCH_HELP_TIP, isChecked).apply()
            Log.d(TAG, "onCheckedChanged:$isChecked")
        }
    }

    //发起添加群流程
    private fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            Toast.makeText(this@AboutActivity, "未安装手Q或安装的版本不支持！", Toast.LENGTH_LONG).show()
            false
        }
    }
}
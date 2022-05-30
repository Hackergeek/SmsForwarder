package com.idormy.sms.forwarder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.idormy.sms.forwarder.ReFreshListView.IRefreshListener
import com.idormy.sms.forwarder.adapter.LogAdapter
import com.idormy.sms.forwarder.model.vo.LogVo
import com.idormy.sms.forwarder.receiver.SmsForwarderBroadcastReceiver
import com.idormy.sms.forwarder.utils.LogUtil
import com.idormy.sms.forwarder.utils.NetUtil
import com.idormy.sms.forwarder.utils.PhoneUtils
import com.idormy.sms.forwarder.utils.Util
import com.umeng.analytics.MobclickAgent


class MainActivity : AppCompatActivity(),
    IRefreshListener {
    private val smsBroadcastReceiver: SmsForwarderBroadcastReceiver? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    /**
     * 忽略电池优化
     */
    @SuppressLint("BatteryLife")
    private fun ignoreBatteryOptimization(activity: Activity) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.packageName)
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + activity.packageName)
            startActivity(intent)
        }
    }

    // logVoList用于存储数据
    private var logVos: List<LogVo> = ArrayList()
    private var adapter: LogAdapter? = null
    private lateinit var listView: ReFreshListView
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //检查权限是否获取
        val pm = packageManager
        PhoneUtils.checkPermission(pm, this)

        ignoreBatteryOptimization(this)
        //获取SIM信息
        PhoneUtils.init()
        MyApplication.SimInfoList = PhoneUtils.simMultiInfo
        Log.d(TAG, "SimInfoList = " + MyApplication.SimInfoList)

        //短信&网络组件初始化
        NetUtil.init()
//        startWallpaperSetting()
    }

    private fun startWallpaperSetting() {
        val localIntent = Intent()
        localIntent.action =
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER //android.service.wallpaper.CHANGE_LIVE_WALLPAPER

        localIntent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(
                this.applicationContext.packageName,
                WallpaperSetting::class.java.canonicalName
            )
        )
        this.startActivity(localIntent)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        //是否关闭页面提示
        val helpTip = findViewById<TextView>(R.id.help_tip)
        helpTip.visibility = if (MyApplication.showHelpTip) View.VISIBLE else View.GONE

        // 先拿到数据并放在适配器上
        initTLogs() //初始化数据
        showList(logVos)

        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (position <= 0) return@OnItemClickListener
            val logVo = logVos[position - 1]
            logDetail(logVo)
        }
        listView.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
            if (position <= 0) return@OnItemLongClickListener false

            //定义AlertDialog.Builder对象，当长按列表项的时候弹出确认删除对话框
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage("确定删除?")
            builder.setTitle("提示")

            //添加AlertDialog.Builder对象的setPositiveButton()方法
            builder.setPositiveButton("确定") { _, _ ->
                val id = logVos[position - 1].id
                Log.d(TAG, "id = $id")
                LogUtil.delLog(id, null)
                initTLogs() //初始化数据
                showList(logVos)
                Toast.makeText(baseContext, "删除列表项", Toast.LENGTH_SHORT).show()
            }

            //添加AlertDialog.Builder对象的setNegativeButton()方法
            builder.setNegativeButton("取消") { _, _ -> }
            builder.create().show()
            true
        }
    }

    // 初始化数据
    private fun initTLogs() {
        logVos = LogUtil.getLog(null, null)
    }

    private fun showList(logVosN: List<LogVo>) {
        if (adapter == null) {
            // 将适配器上的数据传递给listView
            listView = findViewById<ReFreshListView>(R.id.list_view_log)
            listView.setInterface(this)
            adapter = LogAdapter(this@MainActivity, R.layout.item_log, logVosN)
            listView.adapter = adapter
        } else {
            adapter!!.update(logVosN)
        }
    }

    override fun onRefresh() {
        val handler = Handler()
        handler.postDelayed({
            //获取最新数据
            initTLogs()
            //通知界面显示
            showList(logVos)
            //通知listview 刷新数据完毕；
            listView.refreshComplete()
        }, 2000)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        //取消注册广播
        try {
            smsBroadcastReceiver?.let { unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "unregisterReceiver fail:" + e.message)
        }
    }

    fun logDetail(logVo: LogVo) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("详情")
        val simInfo = logVo.simInfo
        if (simInfo != null) {
            builder.setMessage(
                """
                    ${logVo.from}
                    
                    ${logVo.content}
                    
                    ${logVo.simInfo}
                    
                    ${logVo.rule}
                    
                    ${Util.utc2Local(logVo.time!!)}
                    
                    Response：${logVo.forwardResponse}
                    """.trimIndent()
            )
        } else {
            builder.setMessage(
                """
                    ${logVo.from}
                    
                    ${logVo.content}
                    
                    ${logVo.rule}
                    
                    ${Util.utc2Local(logVo.time!!)}
                    
                    Response：${logVo.forwardResponse}
                    """.trimIndent()
            )
        }
        builder.show()
    }

    fun toSetting() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    fun toAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    fun toRuleSetting(view: View?) {
        val intent = Intent(this, RuleActivity::class.java)
        startActivity(intent)
    }

    fun toSendSetting(view: View?) {
        val intent = Intent(this, SenderActivity::class.java)
        startActivity(intent)
    }

    fun cleanLog(view: View?) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("确定要清空转发记录吗？")
            .setPositiveButton("清空") { _, _ ->
                // 积极
                // TODO Auto-generated method stub
                LogUtil.delLog(null, null)
                initTLogs()
                adapter!!.update(logVos)
            }
        builder.show()
    }

    //按返回键不退出回到桌面
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.to_setting -> {
                toSetting()
                true
            }
            R.id.to_about -> {
                toAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
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
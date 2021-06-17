package com.idormy.sms.forwarder

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.idormy.sms.forwarder.utils.OSUtils
import com.idormy.sms.forwarder.utils.OSUtils.RomType

class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.drawable.ic_sms_forwarder)
        val romType = OSUtils.romType
        if (romType == RomType.MIUI_ROM) {
            builder.setContentTitle("短信转发器")
        }
        builder.setContentText("根据规则转发到钉钉/微信/邮箱/bark/Server酱/Telegram/webhook等")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            val notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME,
                NotificationManager.IMPORTANCE_MIN
            )
            notificationChannel.enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false) //是否显示角标
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
            builder.setChannelId(CHANNEL_ONE_ID)
        }
        val notification = builder.build()
        startForeground(1, notification)

        //检查权限是否获取
        //PackageManager pm = getPackageManager();
        //PhoneUtils.CheckPermission(pm, this);

        //Android8.1以下尝试启动主界面，以便动态获取权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "flags: $flags startId: $startId")
        return START_STICKY
    }

    companion object {
        private const val TAG = "ForegroundService"
        private const val CHANNEL_ONE_ID = "com.idormy.sms.forwarder"
        private const val CHANNEL_ONE_NAME = "com.idormy.sms.forwarderName"
    }
}
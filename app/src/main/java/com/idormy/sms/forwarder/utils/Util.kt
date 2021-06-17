package com.idormy.sms.forwarder.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Util {
    /**
     * 判断是否为MIUI系统，参考http://blog.csdn.net/xx326664162/article/details/52438706
     *
     * @return
     */
    val isMIUI: Boolean
        get() = try {
            val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
            val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
            val KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage"
            val prop = Properties()
            prop.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
            prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null || prop.getProperty(
                KEY_MIUI_VERSION_NAME,
                null
            ) != null || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null
        } catch (e: IOException) {
            false
        }

    @Throws(Exception::class)
    fun getVersionName(context: Context): String {
        // 获取packagemanager的实例
        val packageManager = context.packageManager
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        val packInfo =
            packageManager.getPackageInfo(context.packageName, 0)
        return packInfo.versionName
    }

    @Throws(Exception::class)
    fun getVersionCode(context: Context): Int {
        // 获取packagemanager的实例
        val packageManager = context.packageManager
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        val packInfo =
            packageManager.getPackageInfo(context.packageName, 0)
        return packInfo.versionCode
    }

    //友好时间显示
    @JvmStatic
    fun friendlyTime(utcTime: String): String {
        val utcFormater = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        utcFormater.timeZone = TimeZone.getTimeZone("UTC") //时区定义并进行时间获取
        val utcDate = try {
            utcFormater.parse(utcTime)
        } catch (e: ParseException) {
            e.printStackTrace()
            return utcTime
        }

        //获取utcDate距离当前的秒数
        val ct = ((System.currentTimeMillis() - utcDate.time) / 1000).toInt()
        if (ct == 0) {
            return "刚刚"
        }
        if (ct in 1..59) {
            return ct.toString() + "秒前"
        }
        if (ct in 60..3599) {
            return (ct / 60).coerceAtLeast(1).toString() + "分钟前"
        }
        if (ct in 3600..86399) {
            return (ct / 3600).toString() + "小时前"
        }
        if (ct in 86400..2591999) { //86400 * 30
            val day = ct / 86400
            return day.toString() + "天前"
        }
        return if (ct in 2592000..31103999) { //86400 * 30
            (ct / 2592000).toString() + "月前"
        } else (ct / 31104000).toString() + "年前"
    }

    /**
     * 函数功能描述:UTC时间转本地时间格式
     *
     * @param utcTime UTC时间
     * @return 本地时间格式的时间
     */
    fun utc2Local(utcTime: String): String {
        val utcTimePatten = "yyyy-MM-dd HH:mm:ss"
        val localTimePatten = "yyyy-MM-dd HH:mm:ss"
        val utcFormat = SimpleDateFormat(utcTimePatten, Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC") //时区定义并进行时间获取
        val utcDate = try {
            utcFormat.parse(utcTime)
        } catch (e: ParseException) {
            e.printStackTrace()
            return utcTime
        }
        val localFormat =
            SimpleDateFormat(localTimePatten, Locale.getDefault())
        localFormat.timeZone = TimeZone.getDefault()
        return localFormat.format(utcDate.time)
    }
}
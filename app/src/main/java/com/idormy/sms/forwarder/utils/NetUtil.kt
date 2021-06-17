package com.idormy.sms.forwarder.utils

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import com.idormy.sms.forwarder.MyApplication

object NetUtil {
    //没有网络
    private const val NETWORK_NONE = 0

    //移动网络
    private const val NETWORK_MOBILE = 1

    //无线网络
    private const val NETWORK_WIFI = 2
    var hasInit = false
    fun init() {
        synchronized(hasInit) {
            if (hasInit) return
            hasInit = true
        }
    }//没有网络

    //默认返回  没有网络
//返回移动网络//返回无线网络
    //判断是否移动网络
//判断是否是wifi//连接服务 CONNECTIVITY_SERVICE
    //网络信息 NetworkInfo
    //获取网络启动
    @JvmStatic
    val netWorkStatus: Int
        get() {
            //连接服务 CONNECTIVITY_SERVICE
            val connectivityManager =
                MyApplication.globalContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //网络信息 NetworkInfo
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                //判断是否是wifi
                if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    //返回无线网络
                    Toast.makeText(MyApplication.globalContext, "当前处于无线网络", Toast.LENGTH_SHORT)
                        .show()
                    return NETWORK_WIFI
                    //判断是否移动网络
                } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    Toast.makeText(MyApplication.globalContext, "当前处于移动网络", Toast.LENGTH_SHORT)
                        .show()
                    //返回移动网络
                    return NETWORK_MOBILE
                }
            } else {
                //没有网络
                Toast.makeText(MyApplication.globalContext, "当前没有网络", Toast.LENGTH_SHORT).show()
                return NETWORK_NONE
            }
            //默认返回  没有网络
            return NETWORK_NONE
        }
}
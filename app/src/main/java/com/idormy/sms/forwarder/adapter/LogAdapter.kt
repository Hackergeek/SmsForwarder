package com.idormy.sms.forwarder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.model.vo.LogVo
import com.idormy.sms.forwarder.utils.Util.friendlyTime

class LogAdapter     // 适配器的构造函数，把要适配的数据传入这里
    (context: Context?, private val resourceId: Int, private var list: List<LogVo>?) :
    ArrayAdapter<LogVo>(
        context!!, resourceId, list!!
    ) {
    override fun getCount(): Int {
        return list!!.size
    }

    override fun getItem(position: Int): LogVo {
        return list!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // convertView 参数用于将之前加载好的布局进行缓存
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val logVo = getItem(position) //获取当前项的TLog实例

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view = LayoutInflater.from(context).inflate(resourceId, parent, false)

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = ViewHolder()
            viewHolder.tLogFrom = view.findViewById(R.id.tlog_from)
            viewHolder.tLogContent = view.findViewById(R.id.tlog_content)
            viewHolder.tLogRule = view.findViewById(R.id.tlog_rule)
            viewHolder.tLogTime = view.findViewById(R.id.tlog_time)
            viewHolder.senderImage = view.findViewById(R.id.tlog_sender_image)
            viewHolder.statusImage = view.findViewById(R.id.tlog_status_image)
            viewHolder.simImage = view.findViewById(R.id.tlog_sim_image)

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // 获取控件实例，并调用set...方法使其显示出来
        if (logVo != null) {
            viewHolder.tLogFrom!!.text = logVo.from
            viewHolder.tLogContent!!.text = logVo.content
            viewHolder.tLogRule!!.text = logVo.rule
            viewHolder.tLogTime!!.text = friendlyTime(logVo.time!!)
            viewHolder.senderImage!!.setImageResource(logVo.senderImageId)
            viewHolder.simImage!!.setImageResource(logVo.simImageId)
            viewHolder.statusImage!!.setImageResource(logVo.statusImageId)
        }
        return view
    }

    fun add(logVos: List<LogVo>) {
        if (list != null) {
            list = logVos
            notifyDataSetChanged()
        }
    }

    fun del(logVos: List<LogVo>) {
        if (list != null) {
            list = logVos
            notifyDataSetChanged()
        }
    }

    fun update(logVos: List<LogVo>) {
        if (list != null) {
            list = logVos
            notifyDataSetChanged()
        }
    }

    fun onDateChange(logVos: List<LogVo>?) {
        list = logVos
        notifyDataSetChanged()
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    internal inner class ViewHolder {
        var tLogFrom: TextView? = null
        var tLogContent: TextView? = null
        var tLogRule: TextView? = null
        var tLogTime: TextView? = null
        var senderImage: ImageView? = null
        var simImage: ImageView? = null
        var statusImage: ImageView? = null
    }
}
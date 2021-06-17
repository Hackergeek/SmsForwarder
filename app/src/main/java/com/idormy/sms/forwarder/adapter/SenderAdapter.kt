package com.idormy.sms.forwarder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.model.SenderModel

class SenderAdapter     // 适配器的构造函数，把要适配的数据传入这里
    (context: Context?, private val resourceId: Int, private var list: MutableList<SenderModel>) :
    ArrayAdapter<SenderModel>(
        context!!, resourceId, list
    ) {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): SenderModel {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        val item = list[position]
        return item.id!!
    }

    // convertView 参数用于将之前加载好的布局进行缓存
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val senderModel = getItem(position) //获取当前项的TLog实例

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view = LayoutInflater.from(context).inflate(resourceId, parent, false)

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = ViewHolder()
            viewHolder.senderImage = view.findViewById(R.id.sender_image)
            viewHolder.senderName = view.findViewById(R.id.sender_name)

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // 获取控件实例，并调用set...方法使其显示出来
        if (senderModel != null) {
            viewHolder.senderImage!!.setImageResource(senderModel.imageId)
            viewHolder.senderName!!.text = senderModel.name
        }
        return view
    }

    fun add(senderModel: SenderModel) {
        if (list != null) {
            list!!.add(senderModel)
            notifyDataSetChanged()
        }
    }

    fun del(position: Int) {
        if (list != null) {
            list!!.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun update(senderModel: SenderModel, position: Int) {
        if (list != null) {
            list!![position] = senderModel
            notifyDataSetChanged()
        }
    }

    fun add(senderModels: MutableList<SenderModel>) {
        if (list != null) {
            list = senderModels
            notifyDataSetChanged()
        }
    }

    fun del(senderModels: MutableList<SenderModel>) {
        if (list != null) {
            list = senderModels
            notifyDataSetChanged()
        }
    }

    fun update(senderModels: MutableList<SenderModel>) {
        if (list != null) {
            list = senderModels
            notifyDataSetChanged()
        }
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    internal inner class ViewHolder {
        var senderImage: ImageView? = null
        var senderName: TextView? = null
    }
}
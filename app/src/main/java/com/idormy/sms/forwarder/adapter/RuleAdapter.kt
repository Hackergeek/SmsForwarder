package com.idormy.sms.forwarder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.model.RuleModel
import com.idormy.sms.forwarder.sender.SenderUtil

class RuleAdapter     // 适配器的构造函数，把要适配的数据传入这里
    (context: Context?, private val resourceId: Int, private var list: List<RuleModel>?) :
    ArrayAdapter<RuleModel>(
        context!!, resourceId, list!!
    ) {
    override fun getCount(): Int {
        return list!!.size
    }

    override fun getItem(position: Int): RuleModel {
        return list!![position]
    }

    override fun getItemId(position: Int): Long {
        val item = list!![position]
        return item.id!!
    }

    // convertView 参数用于将之前加载好的布局进行缓存
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val ruleModel = getItem(position) //获取当前项的TLog实例

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view = LayoutInflater.from(context).inflate(resourceId, parent, false)

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = ViewHolder()
            viewHolder.ruleMatch = view.findViewById(R.id.rule_match)
            viewHolder.ruleSender = view.findViewById(R.id.rule_sender)
            viewHolder.ruleSenderImage = view.findViewById(R.id.rule_sender_image)

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // 获取控件实例，并调用set...方法使其显示出来
        if (ruleModel != null) {
            val senderModel = SenderUtil.getSender(ruleModel.ruleSenderId, null)
            viewHolder.ruleMatch!!.text = ruleModel.ruleMatch
            if (senderModel.isNotEmpty()) {
                viewHolder.ruleSender!!.text = senderModel[0].name
                viewHolder.ruleSenderImage!!.setImageResource(senderModel[0].imageId)
            } else {
                viewHolder.ruleSender!!.text = ""
            }
        }
        return view
    }

    fun add(ruleModels: List<RuleModel>) {
        if (list != null) {
            list = ruleModels
            notifyDataSetChanged()
        }
    }

    fun del(ruleModels: List<RuleModel>) {
        if (list != null) {
            list = ruleModels
            notifyDataSetChanged()
        }
    }

    fun update(ruleModels: List<RuleModel>) {
        if (list != null) {
            list = ruleModels
            notifyDataSetChanged()
        }
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    internal inner class ViewHolder {
        var ruleMatch: TextView? = null
        var ruleSender: TextView? = null
        var ruleSenderImage: ImageView? = null
    }
}
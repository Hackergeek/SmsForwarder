package com.idormy.sms.forwarder.model

import com.idormy.sms.forwarder.R

class SenderModel {
    var id: Long? = null
    var name: String? = null
    private var status = 0
    var type = 0
    var jsonSetting: String? = null
    var time: Long = 0

    constructor() {}
    constructor(name: String?, status: Int, type: Int, jsonSetting: String?) {
        this.name = name
        this.status = if (status == STATUS_ON) STATUS_ON else STATUS_OFF
        this.type = type
        this.jsonSetting = jsonSetting
    }

    fun getStatus(): Int {
        return status
    }

    fun setStatus(status: Int) {
        this.status = if (status == STATUS_ON) STATUS_ON else STATUS_OFF
    }

    val imageId: Int
        get() = when (type) {
            TYPE_DINGDING -> R.mipmap.dingding
            TYPE_EMAIL -> R.mipmap.email
            TYPE_BARK -> R.mipmap.bark
            TYPE_WEB_NOTIFY -> R.mipmap.webhook
            TYPE_QYWX_GROUP_ROBOT -> R.mipmap.qywx
            TYPE_QYWX_APP -> R.mipmap.qywxapp
            TYPE_SERVER_CHAN -> R.mipmap.serverchan
            TYPE_TELEGRAM -> R.mipmap.telegram
            else -> R.mipmap.sms
        }

    fun getSmsSimSlotId(id: Int): Int {
        return if (id == R.id.btnSmsSimSlot1) {
            1
        } else if (id == R.id.btnSmsSimSlot2) {
            2
        } else {
            0
        }
    }

    override fun toString(): String {
        return "SenderModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", jsonSetting='" + jsonSetting + '\'' +
                ", time=" + time +
                '}'
    }

    companion object {
        const val STATUS_ON = 1
        const val STATUS_OFF = 0
        const val TYPE_DINGDING = 0
        const val TYPE_EMAIL = 1
        const val TYPE_BARK = 2
        const val TYPE_WEB_NOTIFY = 3
        const val TYPE_QYWX_GROUP_ROBOT = 4
        const val TYPE_QYWX_APP = 5
        const val TYPE_SERVER_CHAN = 6
        const val TYPE_TELEGRAM = 7
        const val TYPE_SMS = 8
        fun getImageId(type: Int): Int {
            return when (type) {
                TYPE_DINGDING -> R.mipmap.dingding
                TYPE_EMAIL -> R.mipmap.email
                TYPE_BARK -> R.mipmap.bark
                TYPE_WEB_NOTIFY -> R.mipmap.webhook
                TYPE_QYWX_GROUP_ROBOT -> R.mipmap.qywx
                TYPE_QYWX_APP -> R.mipmap.qywxapp
                TYPE_SERVER_CHAN -> R.mipmap.serverchan
                TYPE_TELEGRAM -> R.mipmap.telegram
                TYPE_SMS -> R.mipmap.sms
                else -> R.mipmap.sms
            }
        }
    }
}
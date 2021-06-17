package com.idormy.sms.forwarder.model

class LogModel(var from: String, var content: String, var simInfo: String, var ruleId: Long) {
    var time: Long? = null
    override fun toString(): String {
        return "LogModel{" +
                "from='" + from + '\'' +
                ", content='" + content + '\'' +
                ", simInfo=" + simInfo +
                ", ruleId=" + ruleId +
                ", time=" + time +
                '}'
    }
}
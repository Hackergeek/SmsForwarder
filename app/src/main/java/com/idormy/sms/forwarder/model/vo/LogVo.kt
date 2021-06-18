package com.idormy.sms.forwarder.model.vo

import com.idormy.sms.forwarder.R

class LogVo {
    var id: Long? = null
        private set
    var from: String? = null
    var content: String? = null
    var simInfo: String? = null
    var rule: String? = null
    var senderImageId = 0
    var time: String? = null
        private set
    private var forwardStatus = 0
    var forwardResponse: String? = null
        private set

    constructor(
        id: Long?,
        from: String?,
        content: String?,
        simInfo: String?,
        time: String?,
        rule: String?,
        senderImageId: Int,
        forwardStatus: Int,
        forwardResponse: String?
    ) {
        this.id = id
        this.from = from
        this.content = content
        this.simInfo = simInfo
        this.time = time
        this.rule = rule
        this.senderImageId = senderImageId
        this.forwardStatus = forwardStatus
        this.forwardResponse = forwardResponse
    }

    constructor() {}

    val simImageId: Int
        get() = if (simInfo != null && simInfo!!.isNotEmpty()
            && simInfo!!.replace("-", "").substring(0, 4) == "SIM2"
        ) {
            R.mipmap.sim2
        } else R.mipmap.sim1
    val statusImageId: Int
        get() = if (forwardStatus == 1) {
            R.drawable.ic_round_check
        } else R.drawable.ic_round_cancel
}
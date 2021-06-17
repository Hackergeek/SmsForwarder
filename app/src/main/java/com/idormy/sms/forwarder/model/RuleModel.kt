package com.idormy.sms.forwarder.model

import android.util.Log
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.model.vo.SmsVo
import com.idormy.sms.forwarder.utils.RuleLineUtils.checkRuleLines
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class RuleModel {
    companion object {
        const val FILED_TRANSPOND_ALL = "transpond_all"
        const val FILED_PHONE_NUM = "phone_num"
        const val FILED_MSG_CONTENT = "msg_content"
        const val FILED_MULTI_MATCH = "multi_match"
        val FILED_MAP: MutableMap<String, String> = HashMap()
        const val CHECK_IS = "is"
        const val CHECK_CONTAIN = "contain"
        const val CHECK_START_WITH = "startwith"
        const val CHECK_END_WITH = "endwith"
        const val CHECK_NOT_IS = "notis"
        const val CHECK_REGEX = "regex"
        val CHECK_MAP: MutableMap<String?, String> = HashMap()
        const val CHECK_SIM_SLOT_ALL = "ALL"
        const val CHECK_SIM_SLOT_1 = "SIM1"
        const val CHECK_SIM_SLOT_2 = "SIM2"
        val SIM_SLOT_MAP: MutableMap<String?, String> = HashMap()
        fun getRuleMatch(filed: String?, check: String?, value: String, simSlot: String?): String {
            val SimStr = SIM_SLOT_MAP[simSlot].toString() + "卡 "
            return if (filed == null || filed == FILED_TRANSPOND_ALL) {
                SimStr + "全部 转发到 "
            } else {
                SimStr + "当 " + FILED_MAP[filed] + " " + CHECK_MAP[check] + " " + value + " 转发到 "
            }
        }

        fun getRuleFiledFromCheckId(id: Int): String {
            return when (id) {
                R.id.btnContent -> FILED_MSG_CONTENT
                R.id.btnPhone -> FILED_PHONE_NUM
                R.id.btnMultiMatch -> FILED_MULTI_MATCH
                else -> FILED_TRANSPOND_ALL
            }
        }

        fun getRuleCheckFromCheckId(id: Int): String {
            return when (id) {
                R.id.btnContain -> CHECK_CONTAIN
                R.id.btnStartWith -> CHECK_START_WITH
                R.id.btnEndWith -> CHECK_END_WITH
                R.id.btnRegex -> CHECK_REGEX
                R.id.btnNotIs -> CHECK_NOT_IS
                else -> CHECK_IS
            }
        }

        fun getRuleSimSlotFromCheckId(id: Int): String {
            return when (id) {
                R.id.btnSimSlot1 -> CHECK_SIM_SLOT_1
                R.id.btnSimSlot2 -> CHECK_SIM_SLOT_2
                else -> CHECK_SIM_SLOT_ALL
            }
        }

        init {
            FILED_MAP["transpond_all"] = "全部转发"
            FILED_MAP["phone_num"] = "手机号"
            FILED_MAP["msg_content"] = "内容"
            FILED_MAP["multi_match"] = "多重匹配"
        }

        init {
            CHECK_MAP["is"] = "是"
            CHECK_MAP["contain"] = "包含"
            CHECK_MAP["startwith"] = "开头是"
            CHECK_MAP["endwith"] = "结尾是"
            CHECK_MAP["notis"] = "不是"
            CHECK_MAP["regex"] = "正则匹配"
        }

        init {
            SIM_SLOT_MAP["ALL"] = "全部"
            SIM_SLOT_MAP["SIM1"] = "SIM1"
            SIM_SLOT_MAP["SIM2"] = "SIM2"
        }
    }

    private val TAG = "RuleModel"
    var id: Long? = null
    var filed: String? = null
    var check: String? = null
    var value: String? = null
    var ruleSenderId: Long? = null
    var time: Long? = null
    var simSlot: String? = null

    //字段分支
    @Throws(Exception::class)
    fun checkMsg(msg: SmsVo?): Boolean {

        //检查这一行和上一行合并的结果是否命中
        var mixChecked = false
        if (msg != null) {
            //先检查规则是否命中
            when (filed) {
                FILED_TRANSPOND_ALL -> mixChecked = true
                FILED_PHONE_NUM -> mixChecked = checkValue(msg.mobile)
                FILED_MSG_CONTENT -> mixChecked = checkValue(msg.content)
                FILED_MULTI_MATCH -> mixChecked = checkRuleLines(msg, value)
                else -> {
                }
            }
        }
        Log.i(TAG, "rule:$this checkMsg:$msg checked:$mixChecked")
        return mixChecked
    }

    //内容分支
    fun checkValue(msgValue: String?): Boolean {
        var checked = false
        if (value != null) {
            when (check) {
                CHECK_IS -> checked = value == msgValue
                CHECK_CONTAIN -> if (msgValue != null) {
                    checked = msgValue.contains(value!!)
                }
                CHECK_START_WITH -> if (msgValue != null) {
                    checked = msgValue.startsWith(value!!)
                }
                CHECK_END_WITH -> if (msgValue != null) {
                    checked = msgValue.endsWith(value!!)
                }
                CHECK_REGEX -> if (msgValue != null) {
                    try {
                        checked = Pattern.matches(value, msgValue)
                    } catch (e: PatternSyntaxException) {
                        checked = false
                        Log.d(TAG, "PatternSyntaxException: ")
                        Log.d(TAG, "Description: " + e.description)
                        Log.d(TAG, "Index: " + e.index)
                        Log.d(TAG, "Message: " + e.message)
                        Log.d(TAG, "Pattern: " + e.pattern)
                    }
                }
                else -> {
                }
            }
        }
        Log.i(TAG, "checkValue " + msgValue + " " + check + " " + value + " checked:" + checked)
        return checked
    }

    val ruleMatch: String
        get() {
            val SimStr = SIM_SLOT_MAP[simSlot].toString() + "卡 "
            return if (filed == null || filed == FILED_TRANSPOND_ALL) {
                SimStr + "全部 转发到 "
            } else {
                SimStr + "当 " + FILED_MAP[filed] + " " + CHECK_MAP[check] + " " + value + " 转发到 "
            }
        }
    val ruleFiledCheckId: Int
        get() = when (filed) {
            FILED_MSG_CONTENT -> R.id.btnContent
            FILED_PHONE_NUM -> R.id.btnPhone
            FILED_MULTI_MATCH -> R.id.btnMultiMatch
            else -> R.id.btnTranspondAll
        }
    val ruleCheckCheckId: Int
        get() = when (check) {
            CHECK_CONTAIN -> R.id.btnContain
            CHECK_START_WITH -> R.id.btnStartWith
            CHECK_END_WITH -> R.id.btnEndWith
            CHECK_REGEX -> R.id.btnRegex
            CHECK_NOT_IS -> R.id.btnNotIs
            else -> R.id.btnIs
        }
    val ruleSimSlotCheckId: Int
        get() = when (simSlot) {
            CHECK_SIM_SLOT_1 -> R.id.btnSimSlot1
            CHECK_SIM_SLOT_2 -> R.id.btnSimSlot2
            else -> R.id.btnSimSlotAll
        }

    override fun toString(): String {
        return "RuleModel{" +
                "id=" + id +
                ", filed='" + filed + '\'' +
                ", check='" + check + '\'' +
                ", value='" + value + '\'' +
                ", senderId=" + ruleSenderId +
                ", time=" + time +
                '}'
    }
}
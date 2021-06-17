package com.idormy.sms.forwarder.utils

import android.util.Log
import com.idormy.sms.forwarder.model.vo.SmsVo
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class RuleLine(line: String, linenum: Int, beforeRuleLine: RuleLine?) {
    companion object {
        const val CONJUNCTION_AND = "并且"
        const val CONJUNCTION_OR = "或者"
        const val FILED_PHONE_NUM = "手机号"
        const val FILED_MSG_CONTENT = "短信内容"
        const val SURE_YES = "是"
        const val SURE_NOT = "不是"
        const val CHECK_EQUALS = "相等"
        const val CHECK_CONTAIN = "包含"
        const val CHECK_START_WITH = "开头"
        const val CHECK_END_WITH = "结尾"
        const val CHECK_REGEX = "正则"
        var CONJUNCTION_LIST: MutableList<String> = ArrayList()
        var FILED_LIST: MutableList<String> = ArrayList()
        var SURE_LIST: MutableList<String> = ArrayList()
        var CHECK_LIST: MutableList<String> = ArrayList()
        var TAG = "RuleLine"
        var STARTLOG = true
        fun startLog(startLog: Boolean) {
            STARTLOG = startLog
        }

        fun logg(msg: String?) {
            if (STARTLOG) {
                Log.i(TAG, msg!!)
            }
        }

        init {
            CONJUNCTION_LIST.add("and")
            CONJUNCTION_LIST.add("or")
            CONJUNCTION_LIST.add("并且")
            CONJUNCTION_LIST.add("或者")
        }

        init {
            FILED_LIST.add("手机号")
            FILED_LIST.add("短信内容")
        }

        init {
            SURE_LIST.add("是")
            SURE_LIST.add("不是")
        }

        init {
            CHECK_LIST.add("相等")
            CHECK_LIST.add("包含")
            CHECK_LIST.add("开头")
            CHECK_LIST.add("结尾")
        }
    }

    //开头有几个空格
    var headSpaceNum = 0
    var beforeRuleLine: RuleLine? = null
    var nextRuleLine: RuleLine? = null
    var parentRuleLine: RuleLine? = null
    var childRuleLine: RuleLine? = null

    //and or
    var conjunction: String

    //手机号 短信内容
    var field: String

    // 是否
    var sure: String
    var check: String
    var value: String

    //字段分支
    fun checkMsg(msg: SmsVo): Boolean {

        //检查这一行和上一行合并的结果是否命中
        var mixChecked = false
        when (field) {
            FILED_PHONE_NUM -> mixChecked = checkValue(msg.mobile)
            FILED_MSG_CONTENT -> mixChecked = checkValue(msg.content)
            else -> {
            }
        }
        mixChecked = when (sure) {
            SURE_YES -> mixChecked
            SURE_NOT -> !mixChecked
            else -> false
        }
        logg("rule:$this checkMsg:$msg checked:$mixChecked")
        return mixChecked
    }

    //内容分支
    fun checkValue(msgValue: String?): Boolean {
        var checked = false
        when (check) {
            CHECK_EQUALS -> checked = value == msgValue
            CHECK_CONTAIN -> if (msgValue != null) {
                checked = msgValue.contains(value)
            }
            CHECK_START_WITH -> if (msgValue != null) {
                checked = msgValue.startsWith(value)
            }
            CHECK_END_WITH -> if (msgValue != null) {
                checked = msgValue.endsWith(value)
            }
            CHECK_REGEX -> if (msgValue != null) {
                try {
                    checked = Pattern.matches(value, msgValue)
                } catch (e: PatternSyntaxException) {
                    checked = false
                    logg("PatternSyntaxException: ")
                    logg("Description: " + e.description)
                    logg("Index: " + e.index)
                    logg("Message: " + e.message)
                    logg("Pattern: " + e.pattern)
                }
            }
            else -> {
            }
        }
        logg("checkValue $msgValue $check $value checked:$checked")
        return checked
    }

    override fun toString(): String {
        return "RuleLine{" +
                "headSpaceNum='" + headSpaceNum + '\'' +
                "conjunction='" + conjunction + '\'' +
                ", field='" + field + '\'' +
                ", sure='" + sure + '\'' +
                ", check='" + check + '\'' +
                ", value='" + value + '\'' +
                '}'
    }

    init {
        logg("----------$linenum-----------------")
        logg(line)
        //规则检验：
        //并且 是 手机号 相等 10086
        //[并且, 是, 手机号, 相等, 10086]
        //  并且 是 内容 包含 asfas
        //[, , 并且, 是, 内容, 包含, asfas]

        //处理头空格数用来确认跟上一行节点的相对位置：是同级还是子级
        //处理4个字段，之后的全部当做value

        //标记3个阶段
        var isCountHeading = false
        var isDealMiddel = false
        var isDealValue = false

        //用于保存4个中间体： 并且, 是, 内容, 包含
        val middleList: MutableList<String> = ArrayList(4)
        //保存每个中间体字符串
        var buildMiddleWord = StringBuilder()
        val valueBuilder = StringBuilder()
        for (i in line.indices) {
            val w = line[i].toString()
            logg("walk over:$w")

            //控制阶段
            //开始处理头
            if (i == 0) {
                if (" " == w) {
                    logg("start to isCountHeading:")
                    isCountHeading = true
                } else {
                    //直接进入处理中间体阶段
                    isCountHeading = false
                    isDealMiddel = true
                    logg("start to isDealMiddel:")
                }
            }
            //正在数空格头，但是遇到非空格，阶段变更:由处理空头阶段  变为  处理 中间体阶段
            if (isCountHeading && " " != w) {
                logg("isCountHeading to isDealMiddel:")
                isCountHeading = false
                isDealMiddel = true
            }

            //正在处理中间体，中间体数量够了，阶段变更：由处理中间体  变为  处理 value
            if (isDealMiddel && middleList.size == 4) {
                logg("isDealMiddel done middleList:$middleList")
                logg("isDealMiddel to isDealValue:")
                isDealMiddel = false
                isDealValue = true
            }
            logg("isCountHeading:$isCountHeading")
            logg("isDealMiddel:$isDealMiddel")
            logg("isDealValue:$isDealValue")
            if (isCountHeading) {
                if (" " == w) {
                    logg("headSpaceNum++:$headSpaceNum")
                    headSpaceNum++
                }
            }
            if (isDealMiddel) {
                //遇到空格
                if (" " == w) {
                    buildMiddleWord = if (buildMiddleWord.length == 0) {
                        throw Exception(linenum.toString() + "行：语法错误不允许出现连续空格！")
                    } else {
                        //生成了一个中间体
                        middleList.add(buildMiddleWord.toString())
                        logg("get Middle++:$buildMiddleWord")
                        StringBuilder()
                    }
                } else {
                    //把w拼接到中间体上
                    buildMiddleWord.append(w)
                    logg("buildMiddleWord length:" + buildMiddleWord.length + "buildMiddleWord:" + buildMiddleWord.toString())
                }
            }
            if (isDealValue) {
                //把余下的所有字符都拼接给value
                valueBuilder.append(w)
            }
        }
        logg("isDealValue done valueBuilder:$valueBuilder")
        if (middleList.size != 4) {
            throw Exception(linenum.toString() + "行配置错误：每行必须有4段组成，例如： 并且 手机号 是 相等 ")
        }


        //规则对齐
        if (beforeRuleLine != null) {
            logg("beforeRuleLine :$beforeRuleLine")
            logg("thisRuleLine :$this")

            //同级别
            if (headSpaceNum == beforeRuleLine.headSpaceNum) {
                logg("同级别")
                this.beforeRuleLine = beforeRuleLine
                beforeRuleLine.nextRuleLine = this
            }
            //子级
            if (headSpaceNum - 1 == beforeRuleLine.headSpaceNum) {
                logg("子级")
                parentRuleLine = beforeRuleLine
                beforeRuleLine.childRuleLine = this
            }
            //查找父级别
            if (headSpaceNum < beforeRuleLine.headSpaceNum) {
                //匹配到最近一个同级
                var fBeforeRuleLine = beforeRuleLine.beforeRuleLine
                if (fBeforeRuleLine == null) {
                    fBeforeRuleLine = beforeRuleLine.parentRuleLine
                }
                while (fBeforeRuleLine != null) {
                    logg("fBeforeRuleLine$fBeforeRuleLine")

                    //查找到同级别
                    if (headSpaceNum == fBeforeRuleLine.headSpaceNum) {
                        logg("父级别")
                        this.beforeRuleLine = fBeforeRuleLine
                        fBeforeRuleLine.nextRuleLine = this
                        break
                    } else {
                        //向上查找
                        var testfBeforeRuleLine = fBeforeRuleLine.beforeRuleLine
                        if (testfBeforeRuleLine == null) {
                            testfBeforeRuleLine = fBeforeRuleLine.parentRuleLine
                        }
                        fBeforeRuleLine = testfBeforeRuleLine
                    }
                }
            }
        } else {
            logg("根级别")
        }
        conjunction = middleList[0]
        sure = middleList[1]
        field = middleList[2]
        check = middleList[3]
        value = valueBuilder.toString()
        if (!CONJUNCTION_LIST.contains(conjunction)) {
            throw Exception(linenum.toString() + "行配置错误：连接词只支持：" + CONJUNCTION_LIST + " 但提供了" + conjunction)
        }
        if (!FILED_LIST.contains(field)) {
            throw Exception(linenum.toString() + "行配置错误：字段只支持：" + FILED_LIST + " 但提供了" + field)
        }
        if (!SURE_LIST.contains(sure)) {
            throw Exception(linenum.toString() + "行配置错误 " + sure + " 确认词只支持：" + SURE_LIST + " 但提供了" + sure)
        }
        if (!CHECK_LIST.contains(check)) {
            throw Exception(linenum.toString() + "行配置错误：比较词只支持：" + CHECK_LIST + " 但提供了" + check)
        }
        logg("----------$linenum==$this")
    }
}
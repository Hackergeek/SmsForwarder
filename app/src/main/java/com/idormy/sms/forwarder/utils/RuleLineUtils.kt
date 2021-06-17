package com.idormy.sms.forwarder.utils

import android.util.Log
import com.idormy.sms.forwarder.model.vo.SmsVo
import java.util.*

object RuleLineUtils {
    var TAG = "RuleLineUtils"
    var STARTLOG = false

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val a = """并且 是 手机号 相等 10086
 或者 是 手机号 结尾 哈哈哈
  并且 是 短信内容 包含 asfas
 或者 是 手机号 结尾 aaaa
并且 是 手机号 相等 100861
并且 是 手机号 相等 100861"""
        val msg = SmsVo("10086", "哈哈哈", Date(), "15888888888")
        logg("check:" + checkRuleLines(msg, a))
    }

    fun startLog(startLog: Boolean) {
        STARTLOG = startLog
    }

    fun logg(msg: String?) {
        if (STARTLOG) {
            Log.i(TAG, msg!!)
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun checkRuleLines(msg: SmsVo, RuleLines: String?): Boolean {
        val scanner = Scanner(RuleLines)
        var linenum = 0
        var headRuleLine: RuleLine? = null
        var beforeRuleLine: RuleLine? = null
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            logg("$linenum : $line")
            //第一行
            if (linenum == 0) {
                //第一行不允许缩进
                if (line.startsWith(" ")) {
                    throw Exception("第一行不允许缩进")
                }
            }

            // process the line
            beforeRuleLine = generateRuleTree(line, linenum, beforeRuleLine)
            if (linenum == 0) {
                headRuleLine = beforeRuleLine
            }
            linenum++
        }
        return checkRuleTree(msg, headRuleLine)
    }

    /**
     * 使用规则树判断消息是否命中规则
     * Rule节点是否命中取决于：该节点是否命中、该节点子结点（如果有的话）是否命中、该节点下节点（如果有的话）是否命中
     * 递归检查
     */
    @Throws(Exception::class)
    fun checkRuleTree(msg: SmsVo, currentRuleLine: RuleLine?): Boolean {
        //该节点是否命中
        var currentAll = currentRuleLine!!.checkMsg(msg)
        logg("current:$currentRuleLine checked:$currentAll")

        //该节点子结点（如果有的话）是否命中
        if (currentRuleLine.childRuleLine != null) {
            logg(" child:" + currentRuleLine.childRuleLine)
            currentAll = when (currentRuleLine.childRuleLine!!.conjunction) {
                RuleLine.CONJUNCTION_AND -> currentAll && checkRuleTree(
                    msg,
                    currentRuleLine.childRuleLine
                )
                RuleLine.CONJUNCTION_OR -> currentAll || checkRuleTree(
                    msg,
                    currentRuleLine.childRuleLine
                )
                else -> throw Exception("child wrong conjunction")
            }
        }

        //该节点下节点（如果有的话）是否命中
        if (currentRuleLine.nextRuleLine != null) {
            logg("next:" + currentRuleLine.nextRuleLine)
            currentAll = when (currentRuleLine.nextRuleLine!!.conjunction) {
                RuleLine.CONJUNCTION_AND -> currentAll && checkRuleTree(
                    msg,
                    currentRuleLine.nextRuleLine
                )
                RuleLine.CONJUNCTION_OR -> currentAll || checkRuleTree(
                    msg,
                    currentRuleLine.nextRuleLine
                )
                else -> throw Exception("next wrong conjunction")
            }
        }
        return currentAll
    }

    /**
     * 生成规则树
     * 一行代表一个规则
     */
    @Throws(Exception::class)
    fun generateRuleTree(line: String, lineNum: Int, parentRuleLine: RuleLine?): RuleLine {
        val words = line.split(" ").toTypedArray()
        return RuleLine(line, lineNum, parentRuleLine)
    }
}
package com.adgvcxz.wechatextension.xposed

import android.content.ContentValues
import android.database.Cursor
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.*

/**
 * zhaowei
 * Created by zhaowei on 2017/2/28.
 */

abstract class WeChat {

    var TextMessageType = 1
    var VoiceMessageType = 34
    var RecallMessageType = 10000

    var SQLiteDatabaseClass = "com.tencent.mmdb.database.SQLiteProgram"
    var SQLiteSessionClass = "com.tencent.mmdb.database.SQLiteSession"
    var CancellationSignal = "com.tencent.mmdb.support.CancellationSignal"

    val Execute = arrayOf("execute", "executeSpecial", "executeForLastInsertedRowId", "executeForChangedRowCount", "executeForLong", "executeForString")

    open var HookRecallClass = ""
    open var HookRecallMethod = ""
    //
    open var HookDatabaseClass = ""
    open var HookDatabaseParams = ""

    var database: Any? = null

    @Suppress("UNCHECKED_CAST")
    fun hookRecall(loader: ClassLoader) {
//        XposedHelpers.findAndHookMethod(HookRecallClass, loader, HookRecallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
//            override fun afterHookedMethod(param: MethodHookParam?) {
//                super.afterHookedMethod(param)
//                val map = param!!.result as HashMap<String, String?>?
//                if (map == null) {
//                    XposedBridge.log("======null======")
//                } else {
//                    val iterator = map.iterator()
//                    XposedBridge.log("=====收到=====")
//                    while (iterator.hasNext()) {
//                        val m = iterator.next()
//                        XposedBridge.log("{${m.key} : ${m.value}}")
//                    }
//                    XposedBridge.log("=====结束=====")
//                }
//            }
//        })

        XposedHelpers.findAndHookConstructor(HookDatabaseClass, loader, HookDatabaseParams, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                database = param.args[0]
            }
        })

        XposedHelpers.findAndHookMethod(SQLiteDatabaseClass, loader, "getSql", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                val sql = param!!.result as String
                XposedBridge.log("格式化sql:  ${param.result}")
//                if (sql.toUpperCase().contains("MESSAGE") || sql.toUpperCase().contains("TIMELINE")) {
//                }
            }
        })

        val cancel = XposedHelpers.findClass(CancellationSignal, loader)

        Execute.forEach { XposedHelpers.findAndHookMethod(SQLiteSessionClass, loader, it, String::class.java, Array<Any?>::class.java, Int::class.java, cancel, callback) }

    }

    val callback = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
            getSql(param)
        }
    }

    private fun getSql(param: XC_MethodHook.MethodHookParam?) {
        var sql = param!!.args[0] as String
        if (sql.toUpperCase().contains("MESSAGE")) {
            if (param.args[1] != null) {
                val value = param.args[1] as Array<Any>
                value.forEach { sql = sql.replaceFirst("?", it.toString()) }
                XposedBridge.log("执行sql(${param.method?.name}):  $sql     ${isRevoke(sql)}")
                if (sql.toUpperCase().startsWith("UPDATE")) {
                    if (isRevoke(sql)) {
                        val message = getMessageById(value[1] as Long)
                        if (message != null) {
                            val content = value[0] as String
                            if (message.type == TextMessageType && !content.contains(" : ")) {
                                value[0] = "${value[0]} : ${message.content}"
                            } else {
                                sql = "UPDATE message SET,msgId=?,status=? WHERE msgId=?"
                                param.args[1] = arrayOf(value[0], 1)
                            }
                        }
                    }
                }
            } else {
                XposedBridge.log("===执行sql(${param.method?.name}===):  $sql     ${isRevoke(sql)}")
                if (sql.toUpperCase().startsWith("UPDATE")) {
                    if (isRevoke(sql)) {
                        param.args[0] = null
                    }
                }
            }
        }
    }

    private fun isRevoke(sql: String): Boolean {
        val upCase = sql.toUpperCase()
        return upCase.contains("TYPE=$RecallMessageType") && upCase.contains("UPDATE MESSAGE SET CONTENT=") && (!upCase.contains("你") && !upCase.contains("YOU"))
    }

    fun insert(param: XC_MethodHook.MethodHookParam) {
        XposedHelpers.callMethod(param.thisObject, Execute[2], "select * from message where talker= 'wxid_7zqk8bmgqnx721' order by createTime DESC limit 1", null, null)
    }


    fun getMessageById(id: Long): WeChatMessage? {

        val cursor = XposedHelpers.callMethod(database, "rawQuery", "select * from message where msgId=?", arrayOf("$id")) as Cursor
        var message: WeChatMessage? = null
        while (cursor.moveToNext()) {
            val type = cursor.getInt(cursor.getColumnIndex("type"))
            val content = cursor.getString(cursor.getColumnIndex("content"))
            val talker = cursor.getString(cursor.getColumnIndex("talker"))
            val talkerId = cursor.getLong(cursor.getColumnIndex("talkerId"))
            val createTime = cursor.getLong(cursor.getColumnIndex("createTime"))
            message = WeChatMessage(id, type, content, talker, talkerId, createTime)
        }
        cursor.close()
        return message
    }

//    fun getLastMessage(talker: String): WeChatMessage? {
//        val cursor = XposedHelpers.callMethod(database, "rawQuery", "select * from message where talker=?", arrayOf(talker)) as Cursor
//        var message: WeChatMessage? = null
//        while (cursor.moveToNext()) {
//            val id = cursor.getLong(cursor.getColumnIndex("id"))
//            val type = cursor.getInt(cursor.getColumnIndex("type"))
//            val talker = cursor.getString(cursor.getColumnIndex("talker"))
//            val createTime = cursor.getLong(cursor.getColumnIndex("createTime"))
//            message = WeChatMessage(id, type, talker, createTime)
//        }
//        cursor.close()
//        return message
//    }

    fun getNextMsgId(): Long {
        val cursor = XposedHelpers.callMethod(database, "rawQuery", "SELECT max(msgId) FROM message", null) as Cursor
        if (!cursor.moveToFirst()) {
            return -1
        }
        val id = cursor.getLong(0) + 1
        cursor.close()
        return id
    }

    fun insertRevokeMessage(message: WeChatMessage, content: String) {
        val msgSvrId = System.currentTimeMillis() + (Random().nextInt())
        val msgId = getNextMsgId()
        val v = ContentValues()
        v.put("msgid", msgId)
//        v.put("msgSvrId", msgSvrId)
        v.put("type", RecallMessageType)
        v.put("createTime", message.createTime - 100)
        v.put("talker", message.talker)
        v.put("content", "******** $content ********")
        v.put("talkerid", message.talkerId)
        insert("message", "", v)
    }

    fun insert(table: String?, selection: String?, values: ContentValues?) = XposedHelpers.callMethod(database, "insert", table, selection, values)
}
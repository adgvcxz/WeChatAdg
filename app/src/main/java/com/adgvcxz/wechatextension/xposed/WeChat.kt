package com.adgvcxz.wechatextension.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

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

//    open var HookRecallClass = ""
//    open var HookRecallMethod = ""
//
//    open var HookDatabaseClass = ""
//    open var HookDatabaseParams = ""

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

//        XposedHelpers.findAndHookConstructor(HookDatabaseClass, loader, HookDatabaseParams, object : XC_MethodHook() {
//            override fun afterHookedMethod(param: MethodHookParam) {
//                database = param.args[0]
//            }
//        })

        XposedHelpers.findAndHookMethod(SQLiteDatabaseClass, loader, "getSql", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                val sql = param!!.result as String
                if (sql.toUpperCase().contains("MESSAGE") || sql.toUpperCase().contains("TIMELINE")) {
                    XposedBridge.log("格式化sql:  ${param.result}")
                }
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
        if (sql.toUpperCase().contains("MESSAGE") || sql.toUpperCase().contains("TIMELINE")) {
            if (param.args[1] != null) {
                val value = param.args[1] as Array<*>
                value.forEach { sql = sql.replaceFirst("?", it.toString()) }
                XposedBridge.log("执行sql:  $sql")
            } else {
                XposedBridge.log("执行sql:  $sql")
            }
            if (isRevoke(sql)) {
                param.args[0] = null
            }
        }
    }

    private fun isRevoke(sql: String): Boolean {
        val upCase = sql.toUpperCase()
        return upCase.contains("TYPE=$RecallMessageType") && upCase.contains("UPDATE MESSAGE SET CONTENT=") && (!upCase.contains("CONTENT=你") && !upCase.contains("CONTENT=YOU"))
    }
}
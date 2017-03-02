package com.adgvcxz.wechatextension.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.*

/**
 * zhaowei
 * Created by zhaowei on 2017/2/28.
 */

abstract class WeChat {

    open var HookRecallClass = ""
    open var HookRecallMethod = ""

    open var HookDatabaseClass = ""
    open var HookDatabaseMethod = ""

    @Suppress("UNCHECKED_CAST")
    fun hookRecall(loader: ClassLoader) {
        XposedHelpers.findAndHookMethod(HookRecallClass, loader, HookRecallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                val map = param!!.result as HashMap<String, String?>?
                if (map == null) {
                    XposedBridge.log("======null======")
                } else {
                    val iterator = map.iterator()
                    XposedBridge.log("=====收到=====")
                    while (iterator.hasNext()) {
                        val m = iterator.next()
                        XposedBridge.log("{${m.key} : ${m.value}}")
                    }
                    XposedBridge.log("=====结束=====")
                }
            }
        })
    }

}
package com.adgvcxz.wechatextension.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.*

/**
 * zhaowei
 * Created by zhaowei on 2017/2/28.
 *
 * 版本为6.5.4的相关操作
 */

class WeChat654: IWeChat {

    val HookClass = "com.tencent.mm.sdk.platformtools.bg"
    val HookMethod = "q"

    @Suppress("UNCHECKED_CAST")
    override fun hook(loader: ClassLoader) {
        XposedHelpers.findAndHookMethod(HookClass, loader, HookMethod, String::class.java, String::class.java, object : XC_MethodHook() {
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
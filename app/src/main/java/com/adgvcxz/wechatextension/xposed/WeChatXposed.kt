package com.adgvcxz.wechatextension.xposed

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * zhaowei
 * Created by zhaowei on 2017/2/28.
 */

class WeChatXposed : IXposedHookLoadPackage {


    companion object {
        val ActivityThreadC = "android.app.ActivityThread"

        var wechat: IWeChat? = null
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (wechat == null) {
            XposedBridge.log(lpparam!!.packageName)
            if (lpparam!!.packageName != Constant.PackageName) {
                return
            }
            val activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass(ActivityThreadC, null), "currentActivityThread")
            val context = XposedHelpers.callMethod(activityThread, "getSystemContext") as Context
            val version = context.packageManager.getPackageInfo(lpparam.packageName, 0).versionName
            wechat = WeChatFactory.create(version)
            wechat?.hook(lpparam.classLoader)
        }
    }

}
package com.adgvcxz.wechatextension.xposed

/**
 * zhaowei
 * Created by zhaowei on 2017/2/28.
 *
 * 版本为6.5.4的相关操作
 */

class WeChat654 : WeChat() {

    override var HookRecallClass = "com.tencent.mm.sdk.platformtools.bg"
    override var HookRecallMethod = "q"

}
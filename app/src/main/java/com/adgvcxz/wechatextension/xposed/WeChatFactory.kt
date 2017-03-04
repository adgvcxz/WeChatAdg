package com.adgvcxz.wechatextension.xposed

/**
 * zhaowei
 * Created by zhaowei on 2017/2/28.
 */

class WeChatFactory {

    companion object {
        val V654 = "6.5.4"

        fun create(version: String): WeChat? {
//            when (version) {
//                V654 -> {
            return WeChat654()
//                }
//            }
//            return null
        }
    }


}

package com.adgvcxz.wechatextension.xposed

/**
 * zhaowei
 * Created by zhaowei on 2017/3/5.
 */

data class WeChatMessage(
    val msgId: Long,
    val type: Int,
    val content: String?,
    val talker: String,
    val talkerId: Long,
    val createTime: Long
)

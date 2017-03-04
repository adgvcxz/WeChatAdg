package com.adgvcxz.wechatextension

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CheckBox

/**
 * zhaowei
 * Created by zhaowei on 2017/3/4.
 */
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val revoke = findViewById(R.id.revoke_my_message_checkbox) as CheckBox
    }
}
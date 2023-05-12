package com.agora.entfulldemo.webview.constructor

/**
 * @author create by zhangwei03
 */
data class WebUsage constructor(
    val usage:WebUsageModel,
)

data class WebUsageModel constructor(
    private val user: UserModel,
    private val device_info: DeviceInfo,
)

data class UserModel constructor(
    private val avatar: String,
    private val name: String,
    private val phone: String,
)

data class DeviceInfo constructor(
    private val type: String,
    private val content: String,
    private val number: Int,
)

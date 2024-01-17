package io.agora.scene.pure1v1.callapi

data class CallReportInfo(
    var msgId: String,
    var category: String,
    var event: String,
    var label: String,
    var value: Int
    )
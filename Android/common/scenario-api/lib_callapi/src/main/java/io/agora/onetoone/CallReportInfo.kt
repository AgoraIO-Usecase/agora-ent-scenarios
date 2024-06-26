package io.agora.onetoone

data class CallReportInfo(
    var msgId: String,
    var category: String,
    var event: String,
    var label: String,
    var value: Int
    )
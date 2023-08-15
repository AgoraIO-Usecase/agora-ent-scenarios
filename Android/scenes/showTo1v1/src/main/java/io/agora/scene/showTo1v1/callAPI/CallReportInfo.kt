package io.agora.scene.showTo1v1.callAPI

data class CallReportInfo(
    var msgId: String,
    var category: String,
    var event: String,
    var label: String,
    var value: Int
    )
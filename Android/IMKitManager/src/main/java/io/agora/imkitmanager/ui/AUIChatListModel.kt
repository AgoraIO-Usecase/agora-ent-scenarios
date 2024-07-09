package io.agora.imkitmanager.ui

import io.agora.imkitmanager.model.AUICustomMsgType
import java.io.Serializable

enum class AUIChatListInterceptType constructor(val type: Int) {
    SUPER_INTERCEPT(0),
    INTERCEPT(1),
    NON_INTERCEPT(2)
}

enum class AUIChatInfoType {
    Common,
    Custom,
    Local
}

data class AUIChatInfo constructor(
    var type: AUIChatInfoType = AUIChatInfoType.Common,
    var userId: String,
    var userName: String,
    var content: String?,
    var customMsgType: AUICustomMsgType? = null
) : Serializable
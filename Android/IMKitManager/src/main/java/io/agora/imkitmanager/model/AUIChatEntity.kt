package io.agora.imkitmanager.model

import io.agora.imkitmanager.ui.AUIChatInfoType
import java.io.Serializable

data class AUIChatEntity constructor(
    var type: AUIChatInfoType = AUIChatInfoType.Common,
    var chatUser: AUIChatUserInfo? = null,
    var content: String = "",
    var customMsgType: AUICustomMsgType? = null
) : Serializable
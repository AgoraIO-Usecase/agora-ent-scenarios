package io.agora.imkitmanager.model

import java.io.Serializable

data class AUIChatEntity constructor(
    var chatUser: AUIChatUserInfo? = null,
    var content: String = "",
    var joined: Boolean = false
) : Serializable
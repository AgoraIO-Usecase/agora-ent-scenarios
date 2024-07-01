package io.agora.imkitmanager.ui

import java.io.Serializable

enum class AUIChatListInterceptType constructor(val type: Int){
    SUPER_INTERCEPT(0),
    INTERCEPT(1),
    NON_INTERCEPT(2)
}

data class AUIChatInfo constructor(
    var userId: String,
    var userName: String,
    var content: String?,
    var joined: Boolean = false,
    var localMsg: Boolean = false
) : Serializable
package io.agora.imkitmanager.model

import android.text.TextUtils

enum class AUICustomMsgType(value: String) {
    /**
     * 系统消息 成员加入
     */
    AUIChatRoomJoinedMember("AUIChatRoomJoinedMember");

    companion object {
        fun fromName(name: String?): AUICustomMsgType? {
            for (type in values()) {
                if (TextUtils.equals(type.name, name)) {
                    return type
                }
            }
            return null
        }
    }
}

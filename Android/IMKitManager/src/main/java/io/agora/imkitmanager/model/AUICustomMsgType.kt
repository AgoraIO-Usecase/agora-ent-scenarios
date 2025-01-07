package io.agora.imkitmanager.model

import android.text.TextUtils

enum class AUICustomMsgType(value: String) {
    /**
     * system message: user join chat room
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

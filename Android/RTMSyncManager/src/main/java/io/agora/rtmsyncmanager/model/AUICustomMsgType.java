package io.agora.rtmsyncmanager.model;

import android.text.TextUtils;

public enum AUICustomMsgType {
    /**
     * 系统消息 成员加入
     */
    AUIChatRoomJoinedMember("AUIChatRoomJoinedMember"),

    ;

    private String name;

    AUICustomMsgType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AUICustomMsgType fromName(String name) {
        for (AUICustomMsgType type : AUICustomMsgType.values()) {
            if(TextUtils.equals(type.getName(), name)) {
                return type;
            }
        }
        return null;
    }
}

package io.agora.scene.voice.imkit.custorm;

import android.text.TextUtils;

public enum CustomMsgType {
    /**
     * Gift message
     */
    CHATROOM_GIFT("chatroom_gift"),

    /**
     * Praise
     */
    CHATROOM_PRAISE("chatroom_praise"),

    /**
     * Apply message
     */
    CHATROOM_APPLY_SITE("chatroom_submitApplySiteNotify"),

    /**
     * Cancel apply message
     */
    CHATROOM_CANCEL_APPLY_SITE("chatroom_submitApplySiteNotifyCancel"),

    /**
     * Decline apply message (function not available yet)
     */
    CHATROOM_DECLINE_APPLY("chatroom_applyRefusedNotify"),

    /**
     * Invite message
     */
    CHATROOM_INVITE_SITE("chatroom_inviteSiteNotify"),

    /**
     * Decline invitation
     */
    CHATROOM_INVITE_REFUSED_SITE("chatroom_inviteRefusedNotify"),

    /**
     * System message: member joined
     */
    CHATROOM_SYSTEM("chatroom_join"),
    ;

    private String name;
    private CustomMsgType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CustomMsgType fromName(String name) {
        for (CustomMsgType type : CustomMsgType.values()) {
            if(TextUtils.equals(type.getName(), name)) {
                return type;
            }
        }
        return null;
    }
}

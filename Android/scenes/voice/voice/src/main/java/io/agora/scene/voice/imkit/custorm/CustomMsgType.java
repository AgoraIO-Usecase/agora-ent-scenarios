package io.agora.scene.voice.imkit.custorm;

import android.text.TextUtils;

public enum CustomMsgType {
    /**
     * 礼物消息
     */
    CHATROOM_GIFT("chatroom_gift"),

    /**
     * 点赞
     */
    CHATROOM_PRAISE("chatroom_praise"),

    /**
     * 申请消息
     */
    CHATROOM_APPLY_SITE("chatroom_submitApplySiteNotify"),

    /**
     * 取消申请消息
     */
    CHATROOM_CANCEL_APPLY_SITE("chatroom_submitApplySiteNotifyCancel"),

    /**
     * 拒绝申请消息 （暂无此功能）
     */
    CHATROOM_DECLINE_APPLY("chatroom_applyRefusedNotify"),

    /**
     * 邀请消息
     */
    CHATROOM_INVITE_SITE("chatroom_inviteSiteNotify"),

    /**
     * 拒绝邀请
     */
    CHATROOM_INVITE_REFUSED_SITE("chatroom_inviteRefusedNotify"),

    /**
     * 系统消息 成员加入
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

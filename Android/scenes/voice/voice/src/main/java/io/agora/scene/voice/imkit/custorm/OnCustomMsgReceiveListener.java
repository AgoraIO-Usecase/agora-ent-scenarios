package io.agora.scene.voice.imkit.custorm;

import io.agora.scene.voice.imkit.bean.ChatMessageData;

/**
 * 定义接收到的消息类型
 */
public interface OnCustomMsgReceiveListener {
    /**
     * 接收到礼物消息
     * @param message
     */
    void onReceiveGiftMsg(ChatMessageData message);

    /**
     * 接收到点赞消息
     * @param message
     */
    void onReceivePraiseMsg(ChatMessageData message);

    /**
     * 接收到普通消息
     * @param message
     */
    void onReceiveNormalMsg(ChatMessageData message);

    /**
     * 接收申请消息
     * @param message
     */
    void onReceiveApplySite(ChatMessageData message);

    /**
     * 接收取消申请消息
     * @param message
     */
    void onReceiveCancelApplySite(ChatMessageData message);

    /**
     * 接收邀请消息
     * @param message
     */
    void onReceiveInviteSite(ChatMessageData message);

    /**
     * 接收拒绝邀请消息
     * @param message
     */
    void onReceiveInviteRefusedSite(ChatMessageData message);

    /**
     * 接收拒绝申请消息
     * @param message
     */
    void onReceiveDeclineApply(ChatMessageData message);

    /**
     * 接收系统消息
     * @param message
     */
    void onReceiveSystem(ChatMessageData message);

}

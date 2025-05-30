package io.agora.scene.voice.imkit.custorm;

import io.agora.scene.voice.imkit.bean.ChatMessageData;

/**
 * Define types of received messages
 */
public interface OnCustomMsgReceiveListener {
    /**
     * Receive gift message
     * @param message
     */
    void onReceiveGiftMsg(ChatMessageData message);

    /**
     * Receive like message
     * @param message
     */
    void onReceivePraiseMsg(ChatMessageData message);

    /**
     * Receive normal message
     * @param message
     */
    void onReceiveNormalMsg(ChatMessageData message);

    /**
     * Receive application message
     * @param message
     */
    void onReceiveApplySite(ChatMessageData message);

    /**
     * Receive application cancellation message
     * @param message
     */
    void onReceiveCancelApplySite(ChatMessageData message);

    /**
     * Receive invitation message
     * @param message
     */
    void onReceiveInviteSite(ChatMessageData message);

    /**
     * Receive invitation rejection message
     * @param message
     */
    void onReceiveInviteRefusedSite(ChatMessageData message);

    /**
     * Receive application rejection message
     * @param message
     */
    void onReceiveDeclineApply(ChatMessageData message);

    /**
     * Receive system message
     * @param message
     */
    void onReceiveSystem(ChatMessageData message);

}

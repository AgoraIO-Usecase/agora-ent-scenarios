package io.agora.scene.voice.imkit.manager;

import java.util.List;
import java.util.Map;

public interface OnChatroomEventReceiveListener {
    /**
     * Chatroom destroyed
     * @param roomId
     */
    void onRoomDestroyed(String roomId);

    /**
     * Member joined
     * @param roomId
     * @param uid
     */
    void onMemberJoined(String roomId,String uid);

    /**
     * Member left
     * @param roomId
     * @param name
     * @param reason
     */
    void onMemberExited(String roomId,String name,String reason);

    /**
     * Member kicked from room
     * @param roomId
     * @param reason
     */
    void onKicked(String roomId,int reason);

    /**
     * Chatroom announcement updated
     * @param roomId
     * @param announcement
     */
    void onAnnouncementChanged(String roomId,String announcement);

    /**
     * Chatroom attributes updated
     * @param roomId
     * @param attributeMap
     * @param fromId
     */
    void onAttributesUpdate(String roomId, Map<String, String> attributeMap, String fromId);

    /**
     * Chatroom attributes removed
     * @param roomId
     * @param keyList
     * @param fromId
     */
    void onAttributesRemoved(String roomId, List<String> keyList, String fromId);

}

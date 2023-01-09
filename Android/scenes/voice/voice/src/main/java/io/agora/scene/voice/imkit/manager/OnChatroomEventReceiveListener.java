package io.agora.scene.voice.imkit.manager;

import java.util.List;
import java.util.Map;

public interface OnChatroomEventReceiveListener {
    /**
     * 聊天室销毁
     * @param roomId
     */
    void onRoomDestroyed(String roomId);

    /**
     * 成员加入
     * @param roomId
     * @param uid
     */
    void onMemberJoined(String roomId,String uid);

    /**
     * 成员离开
     * @param roomId
     * @param name
     * @param reason
     */
    void onMemberExited(String roomId,String name,String reason);

    /**
     * 成员被踢出房间
     * @param roomId
     * @param reason
     */
    void onKicked(String roomId,int reason);

    /**
     * 聊天室公告更新
     * @param roomId
     * @param announcement
     */
    void onAnnouncementChanged(String roomId,String announcement);

    /**
     * 聊天室属性更新
     * @param roomId
     * @param attributeMap
     * @param fromId
     */
    void onAttributesUpdate(String roomId, Map<String, String> attributeMap, String fromId);

    /**
     * 聊天室属性移除
     * @param roomId
     * @param keyList
     * @param fromId
     */
    void onAttributesRemoved(String roomId, List<String> keyList, String fromId);

}

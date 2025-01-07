package io.agora.scene.voice.spatial.service

import io.agora.scene.voice.spatial.model.RobotSpatialAudioModel
import io.agora.scene.voice.spatial.model.VoiceMemberModel

/**
 * @author create by zhangwei03
 *
 * im kv callback protocol
 */
interface VoiceRoomSubscribeDelegate {
    /**
     * Receive seat request message
     * @param message Message object
     */
    fun onReceiveSeatRequest() {}

    /**
     * Receive seat request rejection message
     * @param userId huanxin IM SDK user id
     */
    fun onReceiveSeatRequestRejected(userId: String) {}

    /**
     * Receive invitation message
     * @param message IM message object
     */
    fun onReceiveSeatInvitation() {}

    /**
     * Receive rejection invitation message
     *  @param userId
     */
    fun onReceiveSeatInvitationRejected(userId: String) {}

//    /**
//     * Receive seat request rejection message
//     * @param roomId Voice chat room ID
//     */
//    fun onReceiveSeatRequestRejected(roomId: String, message: ChatMessageData) {}

    /**
     * Chat room announcement update
     * @param roomId Chat room id
     * @param content Announcement change content
     */
    fun onAnnouncementChanged(roomId: String, content: String) {}

    /**
     * User joins chat room callback, with all user information
     * @param roomId Voice chat room ID
     * @param user User data
     */
    fun onUserJoinedRoom(roomId: String, user: VoiceMemberModel) {}

    /**
     * User leaves room
     * @param roomId Voice chat room ID
     * @param userId User id
     */
    fun onUserLeftRoom(roomId: String, userId: String) {}

    /**
     * Chat room member kicked out
     * @param roomId Voice chat room ID
     * @param reason Kicked out reason
     */
    fun onUserBeKicked(roomId: String, reason: VoiceRoomServiceKickedReason) {}

    /**
     * Room destroyed
     */
    fun onRoomDestroyed(roomId: String){}

    /**
     * Chat room custom seat attribute changes
     * @param roomId Voice chat room ID
     * @param attributeMap Changed attribute kv
     */
    fun onSeatUpdated(roomId: String, attributeMap: Map<String, String>) {}

    fun onRobotUpdate(roomId: String, robotInfo: RobotSpatialAudioModel) {}
}

enum class VoiceRoomServiceKickedReason{
    removed,
    destroyed,
    offLined,
}

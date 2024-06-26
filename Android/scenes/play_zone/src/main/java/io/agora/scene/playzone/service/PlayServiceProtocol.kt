package io.agora.scene.playzone.service

import io.agora.imkitmanager.service.IAUIIMManagerService
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.component.AgoraApplication

/**
 * Play zone service listener protocol
 *
 * @constructor Create empty Play zone service listener protocol
 */
interface PlayZoneServiceListenerProtocol {
    /**
     * 房间过期
     *
     */
    fun onRoomExpire() {}

    /**
     * 房间销毁
     *
     */
    fun onRoomDestroy() {}

    /**
     * 机器人列表
     *
     * @param robotMap
     */
    fun onRobotMapSnapshot(robotMap: Map<String, PlayRobotInfo>) {}

    /**
     * 房间人数更新
     *
     * @param userCount
     */
    fun onUserCountUpdate(userCount: Int) {}
}

/**
 * Play zone service protocol
 *
 * @constructor Create empty Play zone service protocol
 */
interface PlayZoneServiceProtocol {

    companion object {
        private var innnerProtocol: PlayZoneServiceProtocol? = null

        val serviceProtocol: PlayZoneServiceProtocol
            get() {
                if (innnerProtocol == null) {
                    innnerProtocol = PlaySyncManagerServiceImp(AgoraApplication.the())
                }
                return innnerProtocol!!
            }

        fun reset() {
            innnerProtocol = null
        }
    }

    /**
     * 获取房间剩余时间
     *
     * @param roomId
     * @return
     */
    fun getCurrentDuration(roomId: String): Long

    /**
     * 获取房间当前时间戳
     *
     * @param roomId
     * @return
     */
    fun getCurrentTs(roomId: String): Long

    /**
     * 获取房间房间列表
     *
     * @param completion
     * @receiver
     */
    fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit)

    /**
     * 创建房间
     *
     * @param inputModel
     * @param completion
     * @receiver
     */
    fun createRoom(inputModel: PlayCreateRoomModel, completion: (error: Exception?, result: AUIRoomInfo?) -> Unit)

    /**
     * 加入房间
     *
     * @param roomId
     * @param completion
     * @receiver
     */
    fun joinRoom(roomId: String, password: String?, completion: (error: Exception?) -> Unit)

    /**
     * 离开房间
     *
     * @param completion
     * @receiver
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * Subscribe listener
     *
     * @param listener
     */
    fun subscribeListener(listener: PlayZoneServiceListenerProtocol)

    /**
     * Unsubscribe listener
     *
     * @param listener
     */
    fun unsubscribeListener(listener: PlayZoneServiceListenerProtocol)
}
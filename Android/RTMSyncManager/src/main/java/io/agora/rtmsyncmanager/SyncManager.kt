package io.agora.rtmsyncmanager

import android.content.Context
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.AUILogger

class SyncManager constructor(
    private val context: Context,
    private val rtmClient: RtmClient? = null,
    private val commonConfig: AUICommonConfig
) {
    var rtmManager: AUIRtmManager
        private set

    private val tag = "SyncManager"
    private var sceneMap = mutableMapOf<String, Scene>()

    init {
        AUIRoomContext.shared().setCommonConfig(commonConfig)
        val rtm = rtmClient ?: createRtmClient()
        rtmManager = AUIRtmManager(context, rtm, rtm == rtmClient)
    }

    fun login(token: String, completion: (AUIRtmException?) -> Unit) {
        rtmManager.login(token, completion)
    }

    fun logout() {
        rtmManager.logout()
    }

    fun release() {
        rtmManager.deInit()
    }

    fun createScene(channelName: String, roomExpiration: RoomExpirationPolicy? = null): Scene {
        AUILogger.logger().d(tag, "createScene: $channelName")
        getScene(channelName)?.let {
            return it
        }

        val scene = Scene(channelName, rtmManager, roomExpiration = roomExpiration ?: RoomExpirationPolicy()) {
            sceneMap.remove(channelName)
        }
        sceneMap[channelName] = scene
        return scene
    }

    fun getScene(channelName: String): Scene? {
        val scene = sceneMap[channelName]
        if (scene != null) {
            return scene
        }
        return null
    }

    fun removeScene(channelName: String) {
        sceneMap.remove(channelName)
    }

    private fun createRtmClient(): RtmClient {
        val commonConfig = AUIRoomContext.shared().requireCommonConfig()
        val userInfo = AUIRoomContext.shared().currentUserInfo
        val rtmConfig = RtmConfig.Builder(commonConfig.appId, userInfo.userId).build()
        if (rtmConfig.appId.isEmpty()) {
            assert(false) { "userId is empty" }
        }
        if (rtmConfig.userId.isEmpty()) {
            assert(false) { "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'" }
        }
        return RtmClient.create(rtmConfig)
    }
}
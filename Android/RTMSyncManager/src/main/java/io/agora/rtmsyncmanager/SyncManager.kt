package io.agora.rtmsyncmanager

import android.content.Context
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.AUILogger.Companion.logger

class SyncManager constructor(
    context: Context,
    rtmClient: RtmClient? = null,
    commonConfig: AUICommonConfig
) {

    private val tag = "SyncManager"

    var rtmManager: AUIRtmManager
        private set

    private var sceneMap = mutableMapOf<String, Scene>()

    init {
        logger().d(tag, "init AUISyncManager")
        AUIRoomContext.shared().setCommonConfig(commonConfig)
        val rtm = rtmClient ?: createRtmClient()
        rtm.setParameters("{\"rtm.msg.tx_timeout\": 3000}")
        rtmManager = AUIRtmManager(context, rtm, rtm == rtmClient)
    }

    fun login(token: String, completion: (AUIRtmException?) -> Unit) {
        rtmManager.login(token, completion)
    }

    fun logout() {
        rtmManager.logout()
    }

    fun getScene(channelName: String): Scene {
        val scene = sceneMap[channelName]
        if (scene != null) {
            return scene
        }
        val newScene = Scene(channelName, rtmManager)
        sceneMap[channelName] = newScene
        return newScene
    }

    private fun createRtmClient(): RtmClient {
        val commonConfig = AUIRoomContext.shared().requireCommonConfig()
        val userInfo = AUIRoomContext.shared().currentUserInfo
        val rtmConfig = RtmConfig.Builder(commonConfig.appId, userInfo.userId).apply {
            presenceTimeout(60)
        }.build()
        if (rtmConfig.appId.isEmpty()) {
            assert(false) { "userId is empty" }
        }
        if (rtmConfig.userId.isEmpty()) {
            assert(false) { "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'" }
        }
        return RtmClient.create(rtmConfig)
    }
}
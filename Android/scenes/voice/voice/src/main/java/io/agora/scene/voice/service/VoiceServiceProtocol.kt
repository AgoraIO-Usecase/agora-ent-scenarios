package io.agora.scene.voice.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.voice.buddy.tool.LogTools.logE

interface VRValueCallBack<T> {
    fun onSuccess(var1: T)
    fun onError(var1: Int, var2: String?)
}

/**
 * @author create by zhangwei03
 */
interface VoiceServiceProtocol {

    enum class VoiceChatSubscribe {
        VoiceSubscribeCreated, //创建
        VoiceSubscribeDeleted, //删除
        VoiceSubscribeUpdated, //更新
    }

    companion object {

        const val ERR_OK = 0
        const val ERR_FAILED = 1
        private val instance by lazy {
            // VoiceChatServiceImp()
            VoiceSyncManagerServiceImp(AgoraApplication.the()) { error ->
                error?.message?.let {
                    "voice chat protocol error：$it".logE()
                }
            }
        }

        @JvmStatic
        fun getImplInstance(): VoiceServiceProtocol = instance
    }

    /**
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     * @param type 房间类型
     * @param completion callback
     */
    fun fetchRoomList(
        page: Int = 0,
        type: Int = 0,
        completion: (error: Int, result: List<VoiceRoomModel>) -> Unit
    )

    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     * @param completion callback
     */
    fun createRoom(
        inputModel: VoiceCreateRoomModel,
        completion: (error: Int, result: VoiceRoomModel) -> Unit
    )

    /**
     * 加入房间
     * @param roomId 房间id
     * @param completion callback
     */
    fun <T> joinRoom(roomId: String, completion: (error: Int, result: T) -> Unit)

    /**
     * 获取房间详情
     * @param roomId 房间id
     * @param completion callback
     */
    fun fetchRoomDetail(roomId: String, completion: (error: Int, result: VoiceRoomModel) -> Unit)

    /**
     * 离开房间
     * @param roomId 房间id
     * @param completion callback
     */
    fun <T> leaveRoom(roomId: String, isOwner: Boolean, completion: (error: Int, result: T) -> Unit)
}
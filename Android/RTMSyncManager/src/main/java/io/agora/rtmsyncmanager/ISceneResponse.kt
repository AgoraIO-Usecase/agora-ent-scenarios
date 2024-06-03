package io.agora.rtmsyncmanager

interface ISceneResponse {

    // 即将更新metadata
    fun onWillInitSceneMetadata(channelName: String): Map<String, Any>? { return null }

    // token即将过期
    fun onTokenPrivilegeWillExpire(channelName: String?) {}

    // 房间过期的回调
    fun onSceneExpire(channelName: String) {}

    // 房间被销毁的回调
    fun onSceneDestroy(channelName: String) {}

    // 房间用户被踢出房间
    fun onSceneUserBeKicked(channelName: String, userId: String) {}
}
package io.agora.rtmsyncmanager

interface ISceneResponse {

    fun onTokenPrivilegeWillExpire(channelName: String?) {}

    // 房间过期的回调
    fun onSceneExpire(channelName: String) {}

    // 房间被销毁的回调
    fun onSceneDestroy(channelName: String) {}

    // 房间用户被踢出房间
    fun onSceneUserBeKicked(channelName: String, userId: String) {}
}
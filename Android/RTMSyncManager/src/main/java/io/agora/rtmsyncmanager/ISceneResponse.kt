package io.agora.rtmsyncmanager

interface ISceneResponse {

    fun onTokenPrivilegeWillExpire(channelName: String?) {}

    /// 房间被销毁的回调
    /// - Parameter roomId: 房间id
    fun onSceneDestroy(roomId: String) {}

    /// Description 房间用户被踢出房间
    ///
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userId: 用户id
    fun onSceneUserBeKicked(roomId: String, userId: String) {}

}
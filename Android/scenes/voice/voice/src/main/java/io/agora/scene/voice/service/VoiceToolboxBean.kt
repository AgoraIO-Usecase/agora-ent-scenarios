package io.agora.scene.voice.service

/**
 * 置换token 与 im 配置
 * @author create by zhangwei03
 */
data class VoiceToolboxBean(
    var rtcToken: String = "",
    var chatRoomId: String = "", // 聊天室ID, 这里返回环信的聊天室ID
    var chatToken: String = "",  // 环信登录Token, 频道名使用聊天室ID
    var userName: String = "",   // 环信用户名
    var chatUUid: String = "",    //  todo 这里返回环信的用户uuid，端上用不到？
)

package io.agora.scene.voice.imkit.bean

import java.io.Serializable

data class ChatMicMemberBean constructor(
    /**
     * uid : string
     * chat_uid : string
     * name : string
     * portrait : string
     * rtc_uid : 0
     * mic_index : 0
     */
    var uid:String,
    var chat_uid:String,
    var name:String,
    var portrait:String,
    var rtc_uid:Int,
    var mic_index:Int
): Serializable

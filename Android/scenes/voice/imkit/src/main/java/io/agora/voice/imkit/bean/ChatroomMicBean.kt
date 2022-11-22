package io.agora.voice.imkit.bean

import java.io.Serializable

data class ChatroomMicBean(
    /**
     * mic_index : 0
     * status : 0
     * member : {"uid":"string","chat_uid":"string","name":"string","portrait":"string","rtc_uid":0,"mic_index":0}
     */
    var status:Int,
    var mic_index:Int,
    var member: ChatMicMemberBean?
): Serializable
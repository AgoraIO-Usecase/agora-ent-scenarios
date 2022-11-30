package io.agora.scene.voice.ui.widget.top

import io.agora.scene.voice.service.VoiceRankUserModel
import io.agora.scene.voice.service.VoiceRoomModel

interface IRoomLiveTopView {
    /**头部初始化*/
    fun onChatroomInfo(voiceRoomModel: VoiceRoomModel)

    fun onRankMember(topRankUsers: List<VoiceRankUserModel>)

    fun onUpdateMemberCount(count:Int){}

    fun onUpdateWatchCount(count: Int){}

    fun onUpdateGiftCount(count: Int){}
}
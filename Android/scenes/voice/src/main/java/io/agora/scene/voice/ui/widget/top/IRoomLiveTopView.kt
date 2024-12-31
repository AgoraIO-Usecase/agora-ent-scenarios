package io.agora.scene.voice.ui.widget.top

import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.scene.voice.model.VoiceRoomModel

interface IRoomLiveTopView {
    fun onChatroomInfo(voiceRoomModel: VoiceRoomModel)

    fun onRankMember(topRankUsers: List<VoiceRankUserModel>)

    fun onUpdateMemberCount(count:Int){}

    fun onUpdateWatchCount(count: Int){}

    fun onUpdateGiftCount(count: Int){}
}
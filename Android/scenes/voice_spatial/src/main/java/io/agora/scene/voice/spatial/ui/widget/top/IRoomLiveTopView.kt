package io.agora.scene.voice.spatial.ui.widget.top

import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.scene.voice.spatial.model.VoiceRoomModel

interface IRoomLiveTopView {
    /**Header initialization*/
    fun onChatroomInfo(voiceRoomModel: VoiceRoomModel)

    fun onRankMember(topRankUsers: List<VoiceRankUserModel>)

    fun onUpdateMemberCount(count:Int){}

    fun onUpdateWatchCount(count: Int){}

    fun onUpdateGiftCount(count: Int){}
}
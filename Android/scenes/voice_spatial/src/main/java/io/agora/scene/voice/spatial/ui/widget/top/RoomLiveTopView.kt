package io.agora.scene.voice.spatial.ui.widget.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoomLiveTopBinding
import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.utils.DeviceTools
import io.agora.voice.common.utils.DeviceTools.dp
import io.agora.voice.common.utils.DeviceTools.number2K
import io.agora.voice.common.utils.ImageTools

class RoomLiveTopView : ConstraintLayout, View.OnClickListener, IRoomLiveTopView {

    private lateinit var binding: VoiceSpatialViewRoomLiveTopBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private lateinit var roomDetailInfo: VoiceRoomModel

    private var onLiveTopClickListener: OnLiveTopClickListener? = null

    fun setOnLiveTopClickListener(onLiveTopClickListener: OnLiveTopClickListener) {
        this.onLiveTopClickListener = onLiveTopClickListener
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.voice_spatial_view_room_live_top, this)
        binding = VoiceSpatialViewRoomLiveTopBinding.bind(root)
        binding.ivChatroomBack.setOnClickListener(this)
        binding.llChatroomMemberRank.setOnClickListener(this)
//        binding.mtChatroomMembers.setOnClickListener(this)
        binding.mtChatroomNotice.setOnClickListener(this)
        binding.llChatroomAgoraSound.setOnClickListener(this)
        binding.ivChatroomMore.setOnClickListener(this)

    }

    fun setTitleMaxWidth() {
        val layoutParams: ViewGroup.LayoutParams = binding.llTitle.layoutParams
        layoutParams.width = DeviceTools.getDisplaySize().width - 220.dp.toInt()
        binding.llTitle.layoutParams = layoutParams
    }

    fun setRoomType(roomType: Int) {
        // 3D空间音频
        if (roomType == 1) {
            binding.ivChatroomOwner.visibility = VISIBLE // 头像
            binding.llTitle.visibility = VISIBLE // 标题栏（包含房间名与房主名）
            binding.iv3DLogo.visibility = INVISIBLE // xxx
            binding.tvChatroomName.visibility = INVISIBLE // xxx
            binding.mtChatroomGifts.visibility = GONE // 排行榜
        }
        // 其他房间类型
        else {
            binding.ivChatroomOwner.visibility = VISIBLE // 头像
            binding.llTitle.visibility = VISIBLE // 标题栏（包含房间名与房主名）
            binding.iv3DLogo.visibility = GONE // xxx
            binding.tvChatroomName.visibility = GONE // xxx
            binding.mtChatroomGifts.visibility = VISIBLE // 排行榜
        }
    }

    override fun onChatroomInfo(voiceRoomModel: VoiceRoomModel) {
        this.roomDetailInfo = voiceRoomModel
        binding.apply {
            mtChatroomOwnerName.text = roomDetailInfo.owner?.nickName
            mtChatroomName.text = roomDetailInfo.roomName
            tvChatroomName.text = roomDetailInfo.roomName
            val memberText = roomDetailInfo.memberCount.number2K()
            mtChatroomMembers.text = memberText
            val giftText = roomDetailInfo.giftAmount.number2K()
            mtChatroomGifts.text = giftText
            val watchText = roomDetailInfo.clickCount.number2K()
            mtChatroomWatch.text = watchText
            // 普通房间显示 最佳音效
            if (roomDetailInfo.roomType == ConfigConstants.RoomType.Common_Chatroom) {
                mtChatroomAgoraSound.isVisible = true
                llChatroomAgoraSound.isVisible = true
                ivIcon.isVisible = false
                mtChatroomAgoraSound.text = when (roomDetailInfo.soundEffect) {
                    ConfigConstants.SoundSelection.Karaoke -> root.context.getString(R.string.voice_chatroom_karaoke)
                    ConfigConstants.SoundSelection.Gaming_Buddy -> root.context.getString(R.string.voice_chatroom_gaming_buddy)
                    ConfigConstants.SoundSelection.Professional_Broadcaster -> root.context.getString(R.string.voice_chatroom_professional_broadcaster)
                    else -> root.context.getString(R.string.voice_chatroom_social_chat)
                }
            }
            // 空间音频
            else if (roomDetailInfo.roomType == ConfigConstants.RoomType.Spatial_Chatroom) {
                llChatroomAgoraSound.isVisible = true
                ivIcon.isVisible = true
                mtChatroomAgoraSound.text = root.context.getString(R.string.voice_chatroom_beginner_guide)
            } else {
                llChatroomAgoraSound.isVisible = false
                mtChatroomAgoraSound.isVisible = false
            }

            // 房主头像
            ImageTools.loadImage(binding.ivChatroomOwner, roomDetailInfo.owner?.portrait)
            val topGifts = roomDetailInfo.rankingList
            if (topGifts.isNullOrEmpty()) {
                llChatroomMemberRank.isVisible = false
            } else {
                llChatroomMemberRank.isVisible = true
                topGifts.forEachIndexed { index, audienceBean ->
                    when (index) {
                        0 -> {
                            ivChatroomMember1.isVisible = true
                            ImageTools.loadImage(ivChatroomMember1, audienceBean.portrait)
                        }
                        1 -> {
                            ivChatroomMember2.isVisible = true
                            ImageTools.loadImage(ivChatroomMember2, audienceBean.portrait)
                        }
                        2 -> {
                            ivChatroomMember3.isVisible = true
                            ImageTools.loadImage(ivChatroomMember3, audienceBean.portrait)
                        }
                        else -> {
                            return
                        }
                    }
                }
            }
        }
    }

    override fun onRankMember(topGifts: List<VoiceRankUserModel>) {
        binding.apply {
            if (topGifts.isEmpty()) {
                llChatroomMemberRank.isVisible = false
            } else {
                llChatroomMemberRank.isVisible = true
                topGifts.forEachIndexed { index, audienceBean ->
                    when (index) {
                        0 -> {
                            ivChatroomMember1.isVisible = true
                            ImageTools.loadImage(ivChatroomMember1, audienceBean.portrait)
                        }
                        1 -> {
                            ivChatroomMember2.isVisible = true
                            ImageTools.loadImage(ivChatroomMember2, audienceBean.portrait)
                        }
                        2 -> {
                            ivChatroomMember3.isVisible = true
                            ImageTools.loadImage(ivChatroomMember3, audienceBean.portrait)
                        }
                        else -> {
                            return
                        }
                    }
                }
            }
        }
    }

    override fun onUpdateMemberCount(count: Int) {
        super.onUpdateMemberCount(count)
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.memberCount = count
            val text = roomDetailInfo.memberCount.number2K()
            binding.mtChatroomMembers.text = text
        }
    }

    override fun onUpdateWatchCount(count: Int) {
        super.onUpdateWatchCount(count)
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.clickCount = count
            val text = roomDetailInfo.clickCount.number2K()
            binding.mtChatroomWatch.text = text
        }
    }

    override fun onUpdateGiftCount(count: Int) {
        super.onUpdateGiftCount(count)
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.giftAmount = count
            val text = roomDetailInfo.giftAmount.number2K()
            binding.mtChatroomGifts.text = text
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            // 返回
            binding.ivChatroomBack -> onLiveTopClickListener?.onClickBack(v)
            // 排行榜
            binding.llChatroomMemberRank,
            binding.mtChatroomMembers -> onLiveTopClickListener?.onClickRank(v)
            // 公告
            binding.mtChatroomNotice -> onLiveTopClickListener?.onClickNotice(v)
            //音效
            binding.llChatroomAgoraSound -> onLiveTopClickListener?.onClickSoundSocial(v)
            // 更多
            binding.ivChatroomMore -> onLiveTopClickListener?.onClickMore(v)
        }
    }
}
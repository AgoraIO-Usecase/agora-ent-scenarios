package io.agora.scene.voice.ui.widget.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.utils.DeviceTools.dp
import io.agora.voice.common.utils.DeviceTools.number2K
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceViewRoomLiveTopBinding
import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.voice.common.utils.DeviceTools
import io.agora.voice.common.utils.ImageTools

class RoomLiveTopView : ConstraintLayout, View.OnClickListener, IRoomLiveTopView {

    private lateinit var binding: VoiceViewRoomLiveTopBinding

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
        val root = View.inflate(context, R.layout.voice_view_room_live_top, this)
        binding = VoiceViewRoomLiveTopBinding.bind(root)
        binding.ivChatroomBack.setOnClickListener(this)
        binding.llChatroomMemberRank.setOnClickListener(this)
        binding.vRoomInfo.setOnClickListener(this)
        binding.tvRoomNotice.setOnClickListener(this)
        binding.tvRoomType.setOnClickListener(this)
        binding.tvClickCount.setOnClickListener(this)
        binding.ivChatroomMore.setOnClickListener(this)
        binding.tvBGM.setOnClickListener(this)
        binding.ivBGM.setOnClickListener(this)
    }

    override fun onChatroomInfo(voiceRoomModel: VoiceRoomModel) {
        this.roomDetailInfo = voiceRoomModel
        binding.apply {
            tvRoomName.text = roomDetailInfo.roomName
            tvOnLineCount.text = resources.getString(R.string.voice_room_online_count, roomDetailInfo.memberCount)
            mtChatroomGifts.text = roomDetailInfo.giftAmount.toString()
            tvClickCount.text = resources.getString(R.string.voice_room_click_count, roomDetailInfo.clickCount)
            // 普通房间显示 最佳音效
            if (roomDetailInfo.roomType == ConfigConstants.RoomType.Common_Chatroom) {
                tvRoomType.isVisible = true
                tvRoomType.text = when (roomDetailInfo.soundEffect) {
                    ConfigConstants.SoundSelection.Karaoke -> root.context.getString(R.string.voice_chatroom_karaoke)
                    ConfigConstants.SoundSelection.Gaming_Buddy -> root.context.getString(R.string.voice_chatroom_gaming_buddy)
                    ConfigConstants.SoundSelection.Professional_Broadcaster -> root.context.getString(R.string.voice_chatroom_professional_broadcaster)
                    else -> root.context.getString(R.string.voice_chatroom_social_chat)
                }
            } else {
                tvRoomType.isVisible = false
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
            binding.tvOnLineCount.text = resources.getString(R.string.voice_room_online_count, roomDetailInfo.memberCount)
        }
    }

    override fun onUpdateWatchCount(count: Int) {
        super.onUpdateWatchCount(count)
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.clickCount = count
            binding.tvClickCount.text = resources.getString(R.string.voice_room_click_count, roomDetailInfo.clickCount)
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

    override fun updateBGMContent(content: String?, isSingerOn: Boolean) {
        if (content != null) {
            binding.ivBGM.visibility = View.VISIBLE
            binding.tvBGM.visibility = View.VISIBLE
            binding.tvBGM.text = content
            if (isSingerOn) {
                binding.ivBGM.setImageResource(R.drawable.voice_icon_room_bgm_on)
            } else {
                binding.ivBGM.setImageResource(R.drawable.voice_icon_room_bgm_off)
            }
        } else {
            binding.ivBGM.visibility = View.INVISIBLE
            binding.tvBGM.visibility = View.INVISIBLE
            binding.tvBGM.text = ""
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 返回
            binding.ivChatroomBack.id -> onLiveTopClickListener?.onClickBack(v)
            // 公告
            binding.tvRoomNotice.id -> onLiveTopClickListener?.onClickNotice(v)
            //音效
            binding.tvRoomType.id -> onLiveTopClickListener?.onClickSoundSocial(v)
            // 排行榜
            binding.llChatroomMemberRank.id -> onLiveTopClickListener?.onClickRank(v, 0)
            // 成员列表
            binding.vRoomInfo.id -> onLiveTopClickListener?.onClickRank(v, 1)
            // 更多
            binding.ivChatroomMore.id -> onLiveTopClickListener?.onClickMore(v)
            // bgm
            binding.tvBGM.id -> onLiveTopClickListener?.onClickBGM(v)
            binding.ivBGM.id -> onLiveTopClickListener?.onClickBGMSinger(v)
        }
    }
}
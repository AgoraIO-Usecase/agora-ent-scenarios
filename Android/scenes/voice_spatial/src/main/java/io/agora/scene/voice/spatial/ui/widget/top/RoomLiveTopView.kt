package io.agora.scene.voice.spatial.ui.widget.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoomLiveTopBinding
import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.scene.voice.spatial.model.VoiceRoomModel

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
        binding.tvRoomNotice.setOnClickListener(this)
        binding.llChatroomAgoraSound.setOnClickListener(this)
        binding.ivChatroomMore.setOnClickListener(this)
    }

    override fun onChatroomInfo(voiceRoomModel: VoiceRoomModel) {
        this.roomDetailInfo = voiceRoomModel
        binding.apply {
            tvRoomName.text = roomDetailInfo.roomName
            tvOnLineCount.text =
                resources.getString(R.string.voice_spatial_room_online_count, roomDetailInfo.memberCount)
            tvClickCount.text = resources.getString(R.string.voice_spatial_room_click_count, roomDetailInfo.clickCount)
            // 空间音频
            llChatroomAgoraSound.isVisible = true
            ivIcon.isVisible = true
            mtChatroomAgoraSound.text = root.context.getString(R.string.voice_spatial_beginner_guide)

            // 房主头像
            loadImage(binding.ivChatroomOwner, roomDetailInfo.owner?.portrait)
            val topGifts = roomDetailInfo.rankingList
            if (topGifts.isNullOrEmpty()) {
                llChatroomMemberRank.isVisible = false
            } else {
                llChatroomMemberRank.isVisible = true
                topGifts.forEachIndexed { index, audienceBean ->
                    when (index) {
                        0 -> {
                            ivChatroomMember1.isVisible = true
                            loadImage(ivChatroomMember1, audienceBean.portrait)
                        }

                        1 -> {
                            ivChatroomMember2.isVisible = true
                            loadImage(ivChatroomMember2, audienceBean.portrait)
                        }

                        2 -> {
                            ivChatroomMember3.isVisible = true
                            loadImage(ivChatroomMember3, audienceBean.portrait)
                        }

                        else -> {
                            return
                        }
                    }
                }
            }
        }
    }

    private fun loadImage(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
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
                            loadImage(ivChatroomMember1, audienceBean.portrait)
                        }

                        1 -> {
                            ivChatroomMember2.isVisible = true
                            loadImage(ivChatroomMember2, audienceBean.portrait)
                        }

                        2 -> {
                            ivChatroomMember3.isVisible = true
                            loadImage(ivChatroomMember3, audienceBean.portrait)
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
            binding.tvOnLineCount.text =
                resources.getString(R.string.voice_spatial_room_online_count, roomDetailInfo.memberCount)
        }
    }

    override fun onUpdateWatchCount(count: Int) {
        super.onUpdateWatchCount(count)
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.clickCount = count
            binding.tvClickCount.text =
                resources.getString(R.string.voice_spatial_room_click_count, roomDetailInfo.clickCount)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            // 返回
            binding.ivChatroomBack -> onLiveTopClickListener?.onClickBack(v)
            // 排行榜
            binding.llChatroomMemberRank -> onLiveTopClickListener?.onClickRank(v)
            // 公告
            binding.tvRoomNotice -> onLiveTopClickListener?.onClickNotice(v)
            //音效
            binding.llChatroomAgoraSound -> onLiveTopClickListener?.onClickSoundSocial(v)
            // 更多
            binding.ivChatroomMore -> onLiveTopClickListener?.onClickMore(v)
        }
    }
}
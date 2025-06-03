package io.agora.scene.voice.ui.widget.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceViewRoomLiveTopBinding
import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.scene.voice.model.VoiceRoomModel
import java.math.RoundingMode
import java.text.DecimalFormat

fun Int.number2K(): String {
    if (this < 1000) return this.toString()
    val format = DecimalFormat("0.#")
    // Rounding mode for discarding decimals, RoundingMode.FLOOR means direct truncation
    format.roundingMode = RoundingMode.FLOOR
    return "${format.format(this / 1000f)}k"
}

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
    }

    override fun onChatroomInfo(voiceRoomModel: VoiceRoomModel) {
        this.roomDetailInfo = voiceRoomModel
        binding.apply {
            tvRoomName.text = roomDetailInfo.roomName
            tvOnLineCount.text = resources.getString(R.string.voice_room_online_count, roomDetailInfo.memberCount)
            mtChatroomGifts.text = roomDetailInfo.giftAmount.toString()
            tvClickCount.text = resources.getString(R.string.voice_room_click_count, roomDetailInfo.clickCount)
            // Show best sound effect for normal rooms
            tvRoomType.isVisible = true
            tvRoomType.text = when (roomDetailInfo.soundEffect) {
                ConfigConstants.SoundSelection.Karaoke -> root.context.getString(R.string.voice_chatroom_karaoke)
                ConfigConstants.SoundSelection.Gaming_Buddy -> root.context.getString(R.string.voice_chatroom_gaming_buddy)
                ConfigConstants.SoundSelection.Professional_Broadcaster -> root.context.getString(R.string.voice_chatroom_professional_broadcaster)
                else -> root.context.getString(R.string.voice_chatroom_social_chat)
            }

            // Owner avatar
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
        GlideApp.with(view)
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
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.memberCount = count
            binding.tvOnLineCount.text =
                resources.getString(R.string.voice_room_online_count, roomDetailInfo.memberCount)
        }
    }

    override fun onUpdateWatchCount(count: Int) {
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.clickCount = count
            binding.tvClickCount.text = resources.getString(R.string.voice_room_click_count, roomDetailInfo.clickCount)
        }
    }

    override fun onUpdateGiftCount(count: Int) {
        if (count < 0) return
        if (this::roomDetailInfo.isInitialized) {
            roomDetailInfo.giftAmount = count
            val text = roomDetailInfo.giftAmount.number2K()
            binding.mtChatroomGifts.text = text
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // Back
            binding.ivChatroomBack.id -> onLiveTopClickListener?.onClickBack(v)
            // Notice
            binding.tvRoomNotice.id -> onLiveTopClickListener?.onClickNotice(v)
            // Sound Effect
            binding.tvRoomType.id -> onLiveTopClickListener?.onClickSoundSocial(v)
            // Ranking
            binding.llChatroomMemberRank.id -> onLiveTopClickListener?.onClickRank(v, 0)
            // Member List
            binding.vRoomInfo.id -> onLiveTopClickListener?.onClickRank(v, 1)
            // More
            binding.ivChatroomMore.id -> onLiveTopClickListener?.onClickMore(v)
        }
    }
}
package io.agora.scene.voice.ui.mic.flat

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.voice.bean.BotMicInfoBean
import io.agora.scene.voice.bean.MicInfoBean
import io.agora.scene.voice.ui.mic.IRoomMicView
import io.agora.voice.baseui.adapter.OnItemChildClickListener
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.buddy.tool.MathTools.dp
import io.agora.voice.buddy.tool.ResourcesTools
import io.agora.scene.voice.R
import io.agora.scene.voice.ui.mic.RoomMicConstructor
import io.agora.scene.voice.databinding.VoiceViewRoom2dMicLayoutBinding

class Room2DMicLayout : ConstraintLayout, IRoomMicView {

    private lateinit var binding: VoiceViewRoom2dMicLayoutBinding

    private var room2DMicAdapter: Room2DMicAdapter? = null
    private var room2DMicBotAdapter: Room2DBotMicAdapter? = null

    private var onMicClickListener: OnItemClickListener<MicInfoBean>? = null
    private var onBotMicClickListener: OnItemClickListener<MicInfoBean>? = null

    fun onItemClickListener(
        onMicClickListener: OnItemClickListener<MicInfoBean>,
        onBotMicClickListener: OnItemClickListener<MicInfoBean>
    ) = apply {
        this.onMicClickListener = onMicClickListener
        this.onBotMicClickListener = onBotMicClickListener
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.voice_view_room_2d_mic_layout, this)
        binding = VoiceViewRoom2dMicLayoutBinding.bind(root)
    }

    fun setUpAdapter(isUseBot: Boolean) {
        room2DMicAdapter =
            Room2DMicAdapter(
                RoomMicConstructor.builderDefault2dMicList(),
                onMicClickListener,
                Room2DMicViewHolder::class.java
            )
        room2DMicBotAdapter =
            Room2DBotMicAdapter(
                RoomMicConstructor.builderDefault2dBotMicList(context, isUseBot),
                null,
                object : OnItemChildClickListener<BotMicInfoBean> {

                    // convert
                    override fun onItemChildClick(
                        data: BotMicInfoBean?, extData: Any?, view: View, position: Int, itemViewType: Long
                    ) {
                        if (extData is MicInfoBean) {
                            onBotMicClickListener?.onItemClick(extData, view, position, itemViewType)
                        }
                    }
                },
                Room2DBotMicViewHolder::class.java
            )

        val config = ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build()
        val concatAdapter = ConcatAdapter(config, room2DMicAdapter, room2DMicBotAdapter)
        val gridLayoutManager = GridLayoutManager(context, 4).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == (concatAdapter.itemCount) - 1) {
                        2
                    } else 1
                }
            }
        }
        binding.rvChatroomMicLayout.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL).apply {
                dividerThickness = 32.dp.toInt()

                dividerColor = ResourcesTools.getColor(context.resources, io.agora.voice.baseui.R.color.voice_transparent)
            })
            layoutManager = gridLayoutManager
            adapter = concatAdapter
        }
    }

    override fun onInitMic(micInfoList: List<MicInfoBean>, isBotActive: Boolean) {
        room2DMicAdapter?.submitListAndPurge(micInfoList)
        room2DMicBotAdapter?.activeBot(isBotActive)
    }

    override fun activeBot(active: Boolean) {
        room2DMicBotAdapter?.activeBot(active)
    }

    override fun updateVolume(index: Int, volume: Int) {
        room2DMicAdapter?.updateVolume(index, volume)
    }

    override fun updateBotVolume(speakerType: Int, volume: Int) {
        room2DMicBotAdapter?.updateVolume(speakerType, volume)
    }

    override fun findMicByUid(uid: String): Int {
        room2DMicAdapter?.dataList?.forEachIndexed { index, micInfoBean ->
            if (TextUtils.equals(micInfoBean.userInfo?.userId, uid)) {
                return index
            }
        }
        return -1
    }

    override fun receiverAttributeMap(newMicMap: Map<Int, MicInfoBean>) {
        room2DMicAdapter?.receiverAttributeMap(newMicMap)
        room2DMicBotAdapter?.receiverAttributeMap(newMicMap)
    }

    private var myRtcUid: Int = -1

    fun setMyRtcUid(rtcUid: Int) {
        this.myRtcUid = rtcUid
    }

    override fun myRtcUid(): Int {
        return myRtcUid
    }
}
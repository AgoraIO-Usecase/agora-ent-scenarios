package io.agora.scene.voice.ui.widget.mic

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
import io.agora.scene.voice.model.BotMicInfoBean
import io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.DeviceTools.dp
import io.agora.voice.common.utils.ResourcesTools
import io.agora.scene.voice.R
import io.agora.scene.voice.model.constructor.RoomMicConstructor
import io.agora.scene.voice.databinding.VoiceViewRoom2dMicLayoutBinding
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.ui.adapter.Room2DBotMicAdapter
import io.agora.scene.voice.ui.adapter.Room2DMicAdapter
import io.agora.scene.voice.ui.adapter.viewholder.Room2DMicViewHolder
import io.agora.scene.voice.ui.adapter.viewholder.Room2DBotMicViewHolder

class Room2DMicLayout : ConstraintLayout, IRoomMicView {

    private lateinit var binding: VoiceViewRoom2dMicLayoutBinding

    private var room2DMicAdapter: Room2DMicAdapter? = null
    private var room2DMicBotAdapter: Room2DBotMicAdapter? = null

    private var onMicClickListener: OnItemClickListener<VoiceMicInfoModel>? = null
    private var onBotMicClickListener: OnItemClickListener<VoiceMicInfoModel>? = null

    fun onItemClickListener(
        onMicClickListener: OnItemClickListener<VoiceMicInfoModel>,
        onBotMicClickListener: OnItemClickListener<VoiceMicInfoModel>
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

    fun setUpInitAdapter() {
        room2DMicAdapter =
            Room2DMicAdapter(
                RoomMicConstructor.builderDefault2dMicList(),
                onMicClickListener,
                Room2DMicViewHolder::class.java
            )
        room2DMicBotAdapter =
            Room2DBotMicAdapter(
                RoomMicConstructor.builderDefault2dBotMicList(context, false),
                null,
                object :
                    OnItemChildClickListener<BotMicInfoBean> {

                    // convert
                    override fun onItemChildClick(
                        data: BotMicInfoBean?, extData: Any?, view: View, position: Int, itemViewType: Long
                    ) {
                        if (extData is VoiceMicInfoModel) {
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

                dividerColor = ResourcesTools.getColor(context.resources, R.color.voice_transparent)
            })
            layoutManager = gridLayoutManager
            adapter = concatAdapter
        }
    }

    override fun onInitMic(micInfoList: List<VoiceMicInfoModel>, isBotActive: Boolean) {
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
            if (TextUtils.equals(micInfoBean.member?.userId, uid)) {
                return index
            }
        }
        return -1
    }

    override fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>) {
        room2DMicAdapter?.onSeatUpdated(newMicMap)
    }

    private var myRtcUid: Int = -1

    fun setMyRtcUid(rtcUid: Int) {
        this.myRtcUid = rtcUid
    }

    override fun myRtcUid(): Int {
        return myRtcUid
    }
}
package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.voice.model.MicManagerBean
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import io.agora.voice.common.utils.DeviceTools.dp
import io.agora.scene.voice.R
import io.agora.scene.voice.model.constructor.RoomMicConstructor
import io.agora.scene.voice.databinding.VoiceDialogMicManagerBinding
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.scene.voice.ui.adapter.RoomMicManagerAdapter
import io.agora.scene.voice.ui.adapter.viewholder.RoomMicManagerViewHolder
import io.agora.voice.common.utils.ImageTools

class RoomMicManagerSheetDialog constructor() : BaseSheetDialog<VoiceDialogMicManagerBinding>() {

    companion object {
        const val KEY_MIC_INFO = "mic_info"
        const val KEY_IS_OWNER = "owner_id"
        const val KEY_IS_MYSELF = "is_myself"
    }

    private var micManagerAdapter: RoomMicManagerAdapter? = null

    private val micManagerList = mutableListOf<MicManagerBean>()

    private val micInfo: VoiceMicInfoModel? by lazy {
        arguments?.get(KEY_MIC_INFO) as? VoiceMicInfoModel
    }
    private val isOwner: Boolean by lazy {
        arguments?.getBoolean(KEY_IS_OWNER, false) ?: false
    }
    private val isMyself: Boolean by lazy {
        arguments?.getBoolean(KEY_IS_MYSELF, false) ?: false
    }

    var onItemClickListener: OnItemClickListener<MicManagerBean>? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogMicManagerBinding {
        return VoiceDialogMicManagerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            micInfo?.let {
                bindingMicInfo(it)
                if (isOwner) {
                    micManagerList.addAll(
                        RoomMicConstructor.builderOwnerMicMangerList(
                            view.context,
                            it,
                            isMyself
                        )
                    )
                } else {
                    micManagerList.addAll(
                        RoomMicConstructor.builderGuestMicMangerList(
                            view.context,
                            it
                        )
                    )
                }
            }
        }
        micManagerAdapter = RoomMicManagerAdapter(micManagerList, object :
            OnItemClickListener<MicManagerBean> {
            override fun onItemClick(
                data: MicManagerBean,
                view: View,
                position: Int,
                viewType: Long
            ) {
                onItemClickListener?.onItemClick(data, view, position, viewType)
                dismiss()
            }
        }, RoomMicManagerViewHolder::class.java)
        binding?.apply {
            setOnApplyWindowInsets(root)
            val itemDecoration =
                MaterialDividerItemDecoration(
                    root.context,
                    MaterialDividerItemDecoration.HORIZONTAL
                ).apply {
                    dividerColor =
                        ResourcesCompat.getColor(
                            root.context.resources,
                            R.color.voice_divider_color_1f979797,
                            null
                        )
                    dividerThickness = 1.dp.toInt()
                }
            rvChatroomMicManager.addItemDecoration(itemDecoration)
            rvChatroomMicManager.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rvChatroomMicManager.adapter = micManagerAdapter
        }
    }

    private fun bindingMicInfo(micInfo: VoiceMicInfoModel) {
        binding?.apply {
            // 座位状态
            if (micInfo.member == null) { // 没人
                binding?.mtChatroomMicTag?.isVisible = false
                ivMicInnerIcon.isVisible = true
                mtMicUsername.text = resources.getString(R.string.voice_room_mic_number, micInfo.micIndex + 1)
                when (micInfo.micStatus) {
                    MicStatus.ForceMute -> {
                        ivMicTag.isVisible = true
                        ivMicInnerIcon.setImageResource(R.drawable.voice_ic_mic_empty)
                    }
                    MicStatus.Lock -> {
                        ivMicTag.isVisible = false
                        ivMicInnerIcon.setImageResource(R.drawable.voice_ic_mic_close)
                    }
                    MicStatus.LockForceMute -> {
                        ivMicInnerIcon.setImageResource(R.drawable.voice_ic_mic_close)
                        ivMicTag.isVisible = true
                    }
                    else -> {
                        ivMicTag.isVisible = false
                        ivMicInnerIcon.setImageResource(R.drawable.voice_ic_mic_empty)
                    }
                }
            } else { // 有人
                ivMicInnerIcon.isVisible = false
                ImageTools.loadImage(ivMicInfo, micInfo.member?.portrait)
                mtMicUsername.text = micInfo.member?.nickName ?: ""
                binding?.mtChatroomMicTag?.isVisible = (micInfo.micIndex == 0)
                mtChatroomMicTag.isVisible = (micInfo.micIndex == 0)
            }
        }
    }
}

package io.agora.scene.voice.ui.mic.flat

import io.agora.scene.voice.bean.MicInfoBean
import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.buddy.tool.MathTools
import io.agora.voice.buddy.tool.MathTools.dp
import io.agora.secnceui.annotation.MicClickAction
import io.agora.secnceui.annotation.MicStatus
import io.agora.scene.voice.databinding.VoiceItemRoom2dMicBinding

class Room2DMicAdapter constructor(
    dataList: List<MicInfoBean>?,
    listener: OnItemClickListener<MicInfoBean>?,
    viewHolderClass: Class<Room2DMicViewHolder>
) :
    BaseRecyclerViewAdapter<VoiceItemRoom2dMicBinding, MicInfoBean, Room2DMicViewHolder>(
        dataList, listener, viewHolderClass
    ) {

    override fun onBindViewHolder(holder: Room2DMicViewHolder, position: Int) {
        val layoutParams = holder.mBinding.root.layoutParams
        val size = ((MathTools.getDisplaySize().width - 28.dp) / 4).toInt()
        layoutParams.width = size
        holder.mBinding.root.layoutParams = layoutParams
        super.onBindViewHolder(holder, position)
    }

    fun updateMicStatusByAction(micIndex: Int, @MicClickAction action: Int) {
        if (micIndex >= dataList.size) return
        val micInfo = dataList[micIndex]
        when (action) {
            MicClickAction.ForceMute -> {
                // 禁言（房主操作）
                if (micInfo.micStatus == MicStatus.Lock) {
                    micInfo.micStatus = MicStatus.LockForceMute
                } else {
                    micInfo.micStatus = MicStatus.ForceMute
                }
            }
            // 取消禁言（房主操作）
            MicClickAction.ForceUnMute -> {
                if (micInfo.micStatus == MicStatus.LockForceMute) {
                    micInfo.micStatus = MicStatus.Lock
                } else {
                    if (micInfo.userInfo == null) {
                        micInfo.micStatus = MicStatus.Idle
                    } else {
                        micInfo.micStatus = MicStatus.Normal
                    }
                }
            }
            // 关麦（麦位用户操作包括房主操作自己）
            MicClickAction.Mute -> {
                micInfo.micStatus = MicStatus.Mute
            }
            // 关闭座位（房主操作）
            MicClickAction.UnMute -> {
                if (micInfo.userInfo == null) {
                    micInfo.micStatus = MicStatus.Idle
                } else {
                    micInfo.micStatus = MicStatus.Normal
                }
            }
            // 关闭座位（房主操作）
            MicClickAction.Lock -> {
                if (micInfo.micStatus == MicStatus.ForceMute) {
                    micInfo.micStatus = MicStatus.LockForceMute
                } else {
                    micInfo.micStatus = MicStatus.Lock
                }
            }
            // 打开座位（房主操作）
            MicClickAction.UnLock -> {
                if (micInfo.micStatus == MicStatus.LockForceMute) {
                    micInfo.micStatus = MicStatus.ForceMute
                } else {
                    if (micInfo.userInfo == null) {
                        micInfo.micStatus = MicStatus.Idle
                    } else {
                        micInfo.micStatus = MicStatus.Normal
                    }
                }
            }
            // 强制下麦（房主操作）
            MicClickAction.KickOff -> {
                if (micInfo.micStatus == MicStatus.ForceMute) {
                    micInfo.micStatus = MicStatus.ForceMute
                } else {
                    micInfo.micStatus = MicStatus.Idle
                }
            }
            // 下麦（嘉宾操作）
            MicClickAction.OffStage -> {
                if (micInfo.micStatus == MicStatus.ForceMute) {
                    micInfo.micStatus = MicStatus.ForceMute
                } else {
                    micInfo.micStatus = MicStatus.Idle
                }
            }
            MicClickAction.Invite -> {
                // TODO()
            }
        }
        notifyItemChanged(micIndex)
    }

    fun receiverAttributeMap(newMicMap: Map<Int, MicInfoBean>) {
        var needUpdate = false
        // 是否只更新一条
        var onlyOneUpdate = newMicMap.size == 1
        var onlyUpdateItemIndex = -1
        newMicMap.entries.forEach { entry ->
            val index = entry.key
            if (index >= 0 && index < dataList.size) {
                dataList.set(index, entry.value)
                needUpdate = true
                if (onlyOneUpdate) onlyUpdateItemIndex = index
            }
        }
        if (needUpdate) {
            if (onlyUpdateItemIndex >= 0) {
                notifyItemChanged(onlyUpdateItemIndex)
            } else {
                notifyItemRangeChanged(0,dataList.size,true)
            }
        }
    }

    fun updateVolume(index: Int, volume: Int) {
        if (index >= 0 && index < dataList.size) {
            dataList[index].audioVolumeType = volume
            notifyItemChanged(index)
        }
    }
}
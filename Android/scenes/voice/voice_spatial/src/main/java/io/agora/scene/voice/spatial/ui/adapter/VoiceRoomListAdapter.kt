package io.agora.scene.voice.spatial.ui.adapter

import android.content.Context
import android.view.View
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentRoomItemLayoutBinding
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.ImageTools

class VoiceRoomListAdapter constructor(
    dataList: List<VoiceRoomModel>?,
    listener: OnItemClickListener<VoiceRoomModel>?,
    viewHolderClass: Class<VoiceRoomListViewHolder>
) : BaseRecyclerViewAdapter<VoiceSpatialFragmentRoomItemLayoutBinding, VoiceRoomModel, VoiceRoomListAdapter.VoiceRoomListViewHolder>(
    dataList, listener, viewHolderClass
) {

    class VoiceRoomListViewHolder constructor(private val binding: VoiceSpatialFragmentRoomItemLayoutBinding) :
        BaseViewHolder<VoiceSpatialFragmentRoomItemLayoutBinding, VoiceRoomModel>(binding) {
        override fun binding(data: VoiceRoomModel?, selectedIndex: Int) {
            data?.let {
                setData(it,itemView.context)
            }
        }

        private fun setData(item: VoiceRoomModel, context: Context) {
            itemType(item.roomType)
            showPrivate(item.isPrivate)
            binding.roomName.text = item.roomName
            binding.ownerName.text = item.owner?.nickName ?: ""
            val countStr = if (item.memberCount > 0) item.memberCount.toString() else "0"
            binding.roomCount.text = context.getString(R.string.voice_room_list_count, countStr)

            ImageTools.loadImage(mBinding.ownerAvatar,item.owner?.portrait)
        }

        private fun itemType(type: Int) {
            when (type) {
                0 -> binding.roomItemLayout.setBackgroundResource(R.drawable.voice_bg_room_list_type_nomal)
                1 -> binding.roomItemLayout.setBackgroundResource(R.drawable.voice_bg_room_list_type_3d)
                2 -> binding.roomItemLayout.setBackgroundResource(R.drawable.voice_bg_room_list_type_official)
                else -> binding.roomItemLayout.setBackgroundResource(R.drawable.voice_bg_room_list_type_nomal)
            }
        }

        private fun showPrivate(isShow: Boolean) {
            if (isShow) {
                binding.roomTitleLayout.visibility = View.VISIBLE
                binding.privateTitle.text =
                    binding.privateTitle.context.getString(R.string.voice_room_list_title_private)
            } else {
                binding.roomTitleLayout.visibility = View.GONE
            }
        }
    }
}
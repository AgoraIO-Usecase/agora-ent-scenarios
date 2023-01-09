package io.agora.scene.voice.ui.adapter

import android.content.Context
import android.view.View
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentRoomItemLayoutBinding
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.ImageTools

class VoiceRoomListAdapter constructor(
    dataList: List<VoiceRoomModel>?,
    listener: OnItemClickListener<VoiceRoomModel>?,
    viewHolderClass: Class<VoiceRoomListViewHolder>
) : BaseRecyclerViewAdapter<VoiceFragmentRoomItemLayoutBinding, VoiceRoomModel, VoiceRoomListAdapter.VoiceRoomListViewHolder>(
    dataList, listener, viewHolderClass
) {

    class VoiceRoomListViewHolder constructor(private val binding: VoiceFragmentRoomItemLayoutBinding) :
        BaseViewHolder<VoiceFragmentRoomItemLayoutBinding, VoiceRoomModel>(binding) {
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
            binding.roomCount.text = context.getString(R.string.voice_room_list_count, item.memberCount.toString())

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
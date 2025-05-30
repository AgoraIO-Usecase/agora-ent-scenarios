package io.agora.scene.voice.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentRoomListLayoutBinding
import io.agora.scene.voice.databinding.VoiceItemRoomListBinding
import io.agora.scene.voice.model.isPrivate
import io.agora.scene.voice.model.memberCount
import io.agora.scene.voice.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.scene.voice.viewmodel.VoiceCreateViewModel
import io.agora.scene.widget.utils.UiUtils

class VoiceRoomListFragment : BaseViewBindingFragment<VoiceFragmentRoomListLayoutBinding>() {

    companion object {
        private const val TAG = "VoiceRoomListFragment"
    }

    private val voiceRoomViewModel: VoiceCreateViewModel by lazy {
        ViewModelProvider(this)[VoiceCreateViewModel::class.java]
    }
    private var mAdapter: RoomListAdapter? = null

    var itemCountListener: ((count: Int) -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentRoomListLayoutBinding {
        return VoiceFragmentRoomListLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context ?: return
        mAdapter = RoomListAdapter(null, context) { data, view ->
            if (UiUtils.isFastClick()) return@RoomListAdapter
            if (data.isPrivate()) {
                showInputDialog(data)
            } else {
                showLoadingView()
                voiceRoomViewModel.joinRoom(data.roomId)
            }
        }
        binding?.apply {
            rvRooms.layoutManager = GridLayoutManager(context, 2)
            rvRooms.adapter = mAdapter
            smartRefreshLayout.setEnableLoadMore(false)
            smartRefreshLayout.setOnRefreshListener {
                voiceRoomViewModel.getRoomList()
            }
            smartRefreshLayout.autoRefresh()
        }
        showLoadingView()
        voiceRoomViewModel.checkLoginIm(completion = {
            hideLoadingView()
        })
        voiceRoomObservable()
    }

    override fun onResume() {
        super.onResume()
        voiceRoomViewModel.getRoomList()
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.roomListObservable.observe(viewLifecycleOwner) { roomList: List<AUIRoomInfo>? ->
            binding?.apply {
                smartRefreshLayout.finishRefresh()
                if (roomList.isNullOrEmpty()) {
                    rvRooms.visibility = View.GONE
                    tvTips1.visibility = View.VISIBLE
                    ivBgMobile.visibility = View.VISIBLE
                } else {
                    mAdapter?.setDataList(roomList)
                    rvRooms.visibility = View.VISIBLE
                    tvTips1.visibility = View.GONE
                    ivBgMobile.visibility = View.GONE
                }
            }
        }
        voiceRoomViewModel.joinRoomObservable.observe(viewLifecycleOwner) { roomInfo: AUIRoomInfo? ->
            hideLoadingView()
            roomInfo ?: return@observe
            activity?.let {
                ChatroomLiveActivity.startActivity(it, roomInfo)
            }
        }
    }

    private fun showInputDialog(voiceRoomModel: AUIRoomInfo) {
        RoomEncryptionInputDialog()
            .leftText(requireActivity().getString(R.string.voice_room_cancel))
            .rightText(requireActivity().getString(R.string.voice_room_confirm))
            .setDialogCancelable(true)
            .setOnClickListener(object : RoomEncryptionInputDialog.OnClickBottomListener {
                override fun onCancelClick() {}
                override fun onConfirmClick(password: String) {
                    showLoadingView()
                    voiceRoomViewModel.joinRoom(voiceRoomModel.roomId, password)
                }
            })
            .show(childFragmentManager, "encryptionInputDialog")
    }

    private class RoomListAdapter constructor(
        private var mList: List<AUIRoomInfo>?,
        private val mContext: Context,
        private val mOnItemClick: ((AUIRoomInfo, View) -> Unit)? = null
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {

        fun setDataList(list: List<AUIRoomInfo>?) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(VoiceItemRoomListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val num = position % 5
            val resId: Int = mContext.resources.getIdentifier(
                "voice_img_room_item_bg_$num",
                "drawable",
                mContext.packageName
            )
            holder.binding.ivBackground.setImageResource(resId)
            val list = mList ?: return
            val data: AUIRoomInfo = list[position]
            GlideApp.with(holder.binding.ivAvatar.context)
                .load(data.roomOwner?.userAvatar)
                .into(holder.binding.ivAvatar)
            holder.binding.tvRoomName.text = data.roomName
            holder.binding.tvPersonNum.text = mContext.getString(R.string.voice_room_list_count, data.memberCount())
            holder.binding.tvUserName.text = data.roomOwner?.userName ?: ""
            holder.binding.ivLock.isVisible = data.isPrivate()
            holder.itemView.setOnClickListener { view ->
                mOnItemClick?.invoke(data, view)
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

        inner class ViewHolder constructor(val binding: VoiceItemRoomListBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
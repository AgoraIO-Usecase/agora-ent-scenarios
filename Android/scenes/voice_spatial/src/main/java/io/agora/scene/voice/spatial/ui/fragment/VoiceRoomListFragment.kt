package io.agora.scene.voice.spatial.ui.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentRoomListLayoutBinding
import io.agora.scene.voice.spatial.global.IParserSource
import io.agora.scene.voice.spatial.utils.ThreadManager
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.scene.voice.spatial.net.OnResourceParseCallback
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.spatial.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.scene.voice.spatial.viewmodel.VoiceCreateViewModel
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.UiUtils

class VoiceRoomListFragment : BaseViewBindingFragment<VoiceSpatialFragmentRoomListLayoutBinding>(), IParserSource {
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var mAdapter: RoomListAdapter? = null

    private var curVoiceRoomModel: VoiceRoomModel? = null

    var itemCountListener: ((count: Int) -> Unit)? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialFragmentRoomListLayoutBinding {
        return VoiceSpatialFragmentRoomListLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        mAdapter = RoomListAdapter(null, this.context!!) { data, view ->
            if (UiUtils.isFastClick()) return@RoomListAdapter
            onItemClick(data)
        }
        binding?.apply {
            rvRooms.layoutManager = GridLayoutManager(this@VoiceRoomListFragment.context, 2)
            rvRooms.adapter = mAdapter
            smartRefreshLayout.setEnableLoadMore(false)
            smartRefreshLayout.setOnRefreshListener {
                voiceRoomViewModel.getRoomList(0)
            }
        }
        voiceRoomObservable()
    }

    override fun onResume() {
        super.onResume()
        voiceRoomViewModel.getRoomList(0)
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.roomListObservable().observe(requireActivity()) { response: Resource<List<VoiceRoomModel>> ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceRoomModel>>() {
                override fun onSuccess(dataList: List<VoiceRoomModel>?) {
                    binding?.apply {
                        smartRefreshLayout.finishRefresh()
                        if (dataList == null || dataList.isEmpty()) {
                            rvRooms.visibility = View.GONE
                            tvTips1.visibility = View.VISIBLE
                            ivBgMobile.visibility = View.VISIBLE
                        } else {
                            mAdapter?.setDataList(dataList)
                            rvRooms.visibility = View.VISIBLE
                            tvTips1.visibility = View.GONE
                            ivBgMobile.visibility = View.GONE
                        }
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.smartRefreshLayout?.finishRefresh()
                }
            })
        }
        voiceRoomViewModel.checkPasswordObservable().observe(requireActivity()) { response ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(value: Boolean?) {
                    curVoiceRoomModel?.let {
                        // 房间列表进入需要置换 token 与获取 im 配置
                        gotoJoinRoom(it)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    binding?.smartRefreshLayout?.finishRefresh()
                    hideLoadingView()
                    CustomToast.show(getString(R.string.voice_spatial_room_check_password))
                }
            })
        }
        voiceRoomViewModel.joinRoomObservable().observe(requireActivity()) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel?>() {
                override fun onSuccess(reslut: VoiceRoomModel?) {
                    curVoiceRoomModel = reslut ?: return
                    goChatroomPage()
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    hideLoadingView()
                    if (code == VoiceServiceProtocol.ERR_ROOM_UNAVAILABLE) {
                        CustomToast.show(getString(R.string.voice_spatial_unavailable_tip))
                    } else {
                        CustomToast.show( message ?: "")
                    }
                }
            })
        }
    }

    private fun gotoJoinRoom(voiceRoomModel: VoiceRoomModel) {
        ThreadManager.getInstance().runOnMainThread {
            voiceRoomViewModel.joinRoom(voiceRoomModel.roomId)
        }
//        VoiceToolboxServerHttpManager.get().requestToolboxService(
//            channelId = voiceRoomModel.channelId,
//            chatroomId = voiceRoomModel.chatroomId,
//            chatroomName = voiceRoomModel.roomName,
//            chatOwner = voiceRoomModel.owner?.chatUid ?: "",
//            completion = { error, _ ->
//                if (error == VoiceServiceProtocol.ERR_OK) {
//                    ThreadManager.getInstance().runOnMainThread {
//                        voiceRoomViewModel.joinRoom(voiceRoomModel.roomId)
//                    }
//                }else{
//                    dismissLoading()
//                }
//            })
    }

    private fun onItemClick(voiceRoomModel: VoiceRoomModel) {
        curVoiceRoomModel = voiceRoomModel
        if (voiceRoomModel.isPrivate) {
            showInputDialog(voiceRoomModel)
        } else {
            // 房间列表进入需要置换 token 与获取 im 配置
            showLoadingView()
            gotoJoinRoom(voiceRoomModel)
        }
    }

    private fun goChatroomPage() {
        val parentActivity = activity
        curVoiceRoomModel?.let {
            if (parentActivity != null) ChatroomLiveActivity.startActivity(parentActivity, it)
        }

        hideLoadingView()
    }

    private fun showInputDialog(voiceRoomModel: VoiceRoomModel) {
        RoomEncryptionInputDialog()
            .leftText(requireActivity().getString(R.string.voice_spatial_room_cancel))
            .rightText(requireActivity().getString(R.string.voice_spatial_room_confirm))
            .setDialogCancelable(true)
            .setOnClickListener(object : RoomEncryptionInputDialog.OnClickBottomListener {
                override fun onCancelClick() {}
                override fun onConfirmClick(password: String) {
                    voiceRoomViewModel.checkPassword(voiceRoomModel.roomId, voiceRoomModel.roomPassword, password)
                    showLoadingView()
                }
            })
            .show(childFragmentManager, "encryptionInputDialog")
    }

    internal class BottomOffsetDecoration(private val mBottomOffset: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val dataSize = state.itemCount
            val position = parent.getChildAdapterPosition(view)
            if (dataSize > 0 && position == dataSize - 1) {
                outRect[0, 0, 0] = mBottomOffset
            } else {
                outRect[0, 0, 0] = 0
            }
        }
    }

    private class RoomListAdapter constructor(
        private var mList: List<VoiceRoomModel>?,
        private val mContext: Context,
        private val mOnItemClick: ((VoiceRoomModel, View) -> Unit)? = null
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {

        fun setDataList(list: List<VoiceRoomModel>?) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.voice_spatial_item_room_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val num = position % 5
            val resId: Int = mContext.resources.getIdentifier(
                "voice_img_room_item_bg_$num",
                "drawable",
                mContext.packageName
            )
            holder.ivBackground.setImageResource(resId)
            val list = mList ?: return
            val data: VoiceRoomModel = list[position]
            GlideApp.with(holder.ivAvatar.context).load(data.owner?.portrait)
                .into(holder.ivAvatar)
            holder.tvRoomName.text = data.roomName
            val peopleNum = if (data.memberCount > 0) data.memberCount else 0
            holder.tvPersonNum.text = mContext.getString(R.string.voice_spatial_room_list_count, peopleNum)
            holder.tvUserName.text = data.owner?.nickName ?: ""
            if (data.isPrivate) {
                holder.ivLock.visibility = View.VISIBLE
            } else {
                holder.ivLock.visibility = View.GONE
            }
            holder.itemView.setOnClickListener { view ->
                mOnItemClick?.invoke(data, view)
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivBackground: ImageView
            var ivAvatar: ImageView
            var ivLock: ImageView
            var tvRoomName: TextView
            var tvUserName: TextView
            var tvPersonNum: TextView

            init {
                ivBackground = itemView.findViewById(R.id.ivBackground)
                ivAvatar = itemView.findViewById(R.id.ivAvatar)
                ivLock = itemView.findViewById(R.id.ivLock)
                tvRoomName = itemView.findViewById(R.id.tvRoomName)
                tvUserName = itemView.findViewById(R.id.tvUserName)
                tvPersonNum = itemView.findViewById(R.id.tvPersonNum)
            }
        }
    }
}
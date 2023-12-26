package io.agora.scene.voice.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.CallBack
import io.agora.chat.adapter.EMAError
import io.agora.scene.base.GlideApp
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentRoomListLayoutBinding
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.netkit.VRCreateRoomResponse
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.scene.voice.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.net.callback.VRValueCallBack
import io.agora.voice.common.ui.BaseUiFragment
import io.agora.voice.common.utils.FastClickTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools

class VoiceRoomListFragment : BaseUiFragment<VoiceFragmentRoomListLayoutBinding>() {
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var mAdapter: RoomListAdapter? = null

    private var curVoiceRoomModel: VoiceRoomModel? = null

    var itemCountListener: ((count: Int) -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentRoomListLayoutBinding {
        return VoiceFragmentRoomListLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        mAdapter = RoomListAdapter(null, this.context!!) { data, view ->
            if (FastClickTools.isFastClick(view)) return@RoomListAdapter
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
        beforeLoginIm()
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
                    dismissLoading()
                    ToastTools.show(requireActivity(), getString(R.string.voice_room_check_password))
                }
            })
        }
        voiceRoomViewModel.joinRoomObservable().observe(requireActivity()) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel?>() {
                override fun onSuccess(reslut: VoiceRoomModel?) {
                    curVoiceRoomModel = reslut ?: return
                    "apex-wt :${reslut.memberCount}".logD()
                    val chatUsername = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName()
                    val chatToken = VoiceBuddyFactory.get().getVoiceBuddy().chatToken()
                    "Voice room list chat_username:$chatUsername".logD()
                    "Voice room list im_token:$chatToken".logD()
                    ChatroomIMManager.getInstance().login(chatUsername, chatToken, object : CallBack {
                        override fun onSuccess() {
                            goChatroomPage()
                        }

                        override fun onError(code: Int, desc: String) {
                            if (code == EMAError.USER_ALREADY_LOGIN) {
                                goChatroomPage()
                            } else {
                                dismissLoading()
                                activity?.let {
                                    ToastTools.show(it, it.getString(R.string.voice_room_login_exception))
                                }
                            }
                        }
                    })
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    dismissLoading()
                    if (code == VoiceServiceProtocol.ERR_ROOM_UNAVAILABLE) {
                        ToastTools.show(
                            requireActivity(),
                            getString(R.string.voice_room_unavailable_tip)
                        )
                    } else {
                        ToastTools.show(requireActivity(), message ?: "")
                    }
                }
            })
        }
    }

    private fun gotoJoinRoom(voiceRoomModel: VoiceRoomModel) {
        VoiceToolboxServerHttpManager.get().requestToolboxService(
            channelId = voiceRoomModel.channelId,
            chatroomId = voiceRoomModel.chatroomId,
            chatroomName = voiceRoomModel.roomName,
            chatOwner = voiceRoomModel.owner?.chatUid ?: "",
            completion = { error, _ ->
                if (error == VoiceServiceProtocol.ERR_OK) {
                    ThreadManager.getInstance().runOnMainThread {
                        voiceRoomViewModel.joinRoom(voiceRoomModel.roomId)
                    }
                }else{
                    dismissLoading()
                }
            })
    }

    private fun beforeLoginIm(){
        if (!ChatroomIMManager.getInstance().isLoggedIn){
            showLoading(false)
            VoiceToolboxServerHttpManager.get().createImRoom(
                roomName = "",
                roomOwner = "",
                chatroomId = "",
                type = 1,
                object : VRValueCallBack<VRCreateRoomResponse>{
                    override fun onSuccess(response: VRCreateRoomResponse?) {
                        response?.let {
                            VoiceBuddyFactory.get().getVoiceBuddy().setupChatToken(response.chatToken)
                            "beforeLoginIm userName:$it.userName,chatToken:$it.chatToken".logD()
                            ChatroomIMManager.getInstance().login(it.userName,it.chatToken, object : CallBack {
                                override fun onSuccess() {
                                    dismissLoading()
                                }

                                override fun onError(code: Int, desc: String) {
                                    dismissLoading()
                                    "beforeLoginIm sdk error code:$code,msg:$desc".logE()
                                }
                            })
                        }
                    }

                    override fun onError(code: Int, error: String?) {
                        dismissLoading()
                        "beforeLoginIm server error code:$code,msg:$error".logE()
                    }
                })
        }
    }

    private fun onItemClick(voiceRoomModel: VoiceRoomModel) {
        curVoiceRoomModel = voiceRoomModel
        if (voiceRoomModel.isPrivate) {
            showInputDialog(voiceRoomModel)
        } else {
            // 房间列表进入需要置换 token 与获取 im 配置
            showLoading(false)
            gotoJoinRoom(voiceRoomModel)
        }
    }

    private fun goChatroomPage() {
        val parentActivity = activity
        curVoiceRoomModel?.let {
            "apex-wt VoiceRoomListFragment ${it.memberCount}".logD()
            if (parentActivity != null) ChatroomLiveActivity.startActivity(parentActivity, it)
        }

        dismissLoading()
    }

    private fun showInputDialog(voiceRoomModel: VoiceRoomModel) {
        RoomEncryptionInputDialog()
            .leftText(requireActivity().getString(R.string.voice_room_cancel))
            .rightText(requireActivity().getString(R.string.voice_room_confirm))
            .setDialogCancelable(true)
            .setOnClickListener(object : RoomEncryptionInputDialog.OnClickBottomListener {
                override fun onCancelClick() {}
                override fun onConfirmClick(password: String) {
                    voiceRoomViewModel.checkPassword(voiceRoomModel.roomId, voiceRoomModel.roomPassword, password)
                    showLoading(false)
                }
            })
            .show(childFragmentManager, "encryptionInputDialog")
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
                LayoutInflater.from(mContext).inflate(R.layout.voice_item_room_list, parent, false)
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
            holder.tvPersonNum.text = mContext.getString(R.string.voice_room_list_count, peopleNum)
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
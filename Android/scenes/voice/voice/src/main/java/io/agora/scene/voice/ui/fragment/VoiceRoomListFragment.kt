package io.agora.scene.voice.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.agora.CallBack
import io.agora.chat.adapter.EMAError
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentRoomListLayoutBinding
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.netkit.VRCreateRoomResponse
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.ui.adapter.VoiceRoomListAdapter
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.scene.voice.ui.widget.recyclerview.EmptyRecyclerView
import io.agora.scene.voice.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.net.callback.VRValueCallBack
import io.agora.voice.common.ui.BaseUiFragment
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.FastClickTools
import io.agora.voice.common.utils.LogTools.i
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools

class VoiceRoomListFragment : BaseUiFragment<VoiceFragmentRoomListLayoutBinding>(),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var listAdapter: VoiceRoomListAdapter? = null

    private var curVoiceRoomModel: VoiceRoomModel? = null

    var itemCountListener: ((count: Int) -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentRoomListLayoutBinding {
        return VoiceFragmentRoomListLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        binding?.let {
            initAdapter(it.recycler)
            it.swipeLayout.setOnRefreshListener(this)
        }
        beforeLoginIm()
        voiceRoomObservable()
    }

    private fun initAdapter(recyclerView: EmptyRecyclerView) {
        val offsetPx = resources.getDimension(R.dimen.voice_space_84dp)
        recyclerView.addItemDecoration(BottomOffsetDecoration(offsetPx.toInt()))
        listAdapter = VoiceRoomListAdapter(null, object :
            OnItemClickListener<VoiceRoomModel> {
            override fun onItemClick(voiceRoomModel: VoiceRoomModel, view: View, position: Int, viewType: Long) {
                if (FastClickTools.isFastClick(view)) return
                onItemClick(voiceRoomModel)
            }
        }, VoiceRoomListAdapter.VoiceRoomListViewHolder::class.java)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listAdapter
        recyclerView.setEmptyView(binding?.voiceNoData?.root)
    }

    override fun onResume() {
        super.onResume()
        voiceRoomViewModel.getRoomList(0)
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.roomListObservable().observe(requireActivity()) { response: Resource<List<VoiceRoomModel>> ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceRoomModel>>() {
                override fun onSuccess(dataList: List<VoiceRoomModel>?) {
                    binding?.swipeLayout?.isRefreshing = false
                    "Voice room list total：${dataList?.size ?: 0}".logD()
                    listAdapter?.submitListAndPurge(dataList ?: mutableListOf())
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.swipeLayout?.isRefreshing = false
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
                    binding?.swipeLayout?.isRefreshing = false
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

    override fun onRefresh() {
        voiceRoomViewModel.getRoomList(0)
    }
}
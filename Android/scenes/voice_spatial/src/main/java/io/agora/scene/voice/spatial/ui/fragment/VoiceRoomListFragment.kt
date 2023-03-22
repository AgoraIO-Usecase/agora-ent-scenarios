package io.agora.scene.voice.spatial.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentRoomListLayoutBinding
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.spatial.ui.adapter.VoiceRoomListAdapter
import io.agora.scene.voice.spatial.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.scene.voice.spatial.ui.widget.recyclerview.EmptyRecyclerView
import io.agora.scene.voice.spatial.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.BaseUiFragment
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.FastClickTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools

class VoiceRoomListFragment : BaseUiFragment<VoiceSpatialFragmentRoomListLayoutBinding>(),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var listAdapter: VoiceRoomListAdapter? = null

    private var curVoiceRoomModel: VoiceRoomModel? = null

    var itemCountListener: ((count: Int) -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceSpatialFragmentRoomListLayoutBinding {
        return VoiceSpatialFragmentRoomListLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        binding?.let {
            initAdapter(it.recycler)
            it.swipeLayout.setOnRefreshListener(this)
        }
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
                    goChatroomPage()
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
            showLoading(false)
            gotoJoinRoom(voiceRoomModel)
        }
    }

    private fun goChatroomPage() {
        val parentActivity = activity
        curVoiceRoomModel?.let {
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
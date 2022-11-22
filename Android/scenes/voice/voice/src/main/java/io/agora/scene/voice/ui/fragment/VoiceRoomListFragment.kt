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
import com.alibaba.android.arouter.launcher.ARouter
import io.agora.CallBack
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentRoomListLayoutBinding
import io.agora.scene.voice.model.VoiceCreateViewModel
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.scene.voice.service.VoiceRoomModel
import io.agora.scene.voice.ui.adapter.VoiceRoomListAdapter
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog
import io.agora.voice.baseui.BaseUiFragment
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.config.RouterParams
import io.agora.voice.buddy.config.RouterPath
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.buddy.tool.ToastTools
import io.agora.voice.imkit.manager.ChatroomHelper

class VoiceRoomListFragment : BaseUiFragment<VoiceFragmentRoomListLayoutBinding>() , SwipeRefreshLayout.OnRefreshListener{
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var listAdapter: VoiceRoomListAdapter? = null

    private var curVoiceRoomModel: VoiceRoomModel? = null

    private var total = 0
    private var isEnd = false

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
        voiceRoomObservable()
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        val offsetPx = resources.getDimension(R.dimen.voice_space_84dp)
        recyclerView.addItemDecoration(BottomOffsetDecoration(offsetPx.toInt()))
        listAdapter = VoiceRoomListAdapter(null, object : OnItemClickListener<VoiceRoomModel> {
            override fun onItemClick(voiceRoomModel: VoiceRoomModel, view: View, position: Int, viewType: Long) {
                onItemClick(voiceRoomModel)
            }
        }, VoiceRoomListAdapter.VoiceRoomListViewHolder::class.java)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listAdapter

    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.getRoomList(0)
        voiceRoomViewModel.roomListObservable().observe(requireActivity()) { response: Resource<List<VoiceRoomModel>> ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceRoomModel>>() {
                override fun onSuccess(dataList: List<VoiceRoomModel>?) {
                    binding?.swipeLayout?.isRefreshing = false
                    total = dataList?.size ?: 0
                    "Voice room list total：${total}".logD()
                    if (dataList == null || dataList.isEmpty()) return
                    isEnd = true
                    listAdapter?.submitListAndPurge(dataList)
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
                    if (value == true) {
                        curVoiceRoomModel?.let {
                            // 房间列表进入需要置换 token 与获取 im 配置
                            voiceRoomViewModel.joinRoom(it.roomId, true)
                        }
                    } else {
                        dismissLoading()
                        ToastTools.show(requireActivity(), getString(R.string.voice_room_check_password))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.swipeLayout?.isRefreshing = false
                }
            })
        }
        voiceRoomViewModel.joinRoomObservable().observe(requireActivity()) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean?>() {
                override fun onSuccess(reslut: Boolean?) {
                    val chatUsername = VoiceBuddyFactory.get().getVoiceBuddy().chatUid()
                    val chatToken = VoiceBuddyFactory.get().getVoiceBuddy().chatToken()
                    "Voice room list chat_username:$chatUsername".logD()
                    "Voice room list im_token:$chatToken".logD()
                    if (!ChatroomHelper.getInstance().isLoggedIn) {
                        ChatroomHelper.getInstance().login(chatUsername, chatToken, object : CallBack {
                            override fun onSuccess() {
                                ThreadManager.getInstance().runOnMainThread {
                                    goChatroomPage()
                                }
                            }

                            override fun onError(code: Int, desc: String) {
                                ThreadManager.getInstance().runOnMainThread {
                                    dismissLoading()
                                }
                            }
                        })
                    } else {
                        ThreadManager.getInstance().runOnMainThread {
                            goChatroomPage()
                        }
                    }
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
            voiceRoomViewModel.joinRoom(voiceRoomModel.roomId, true)
        }
    }

    private fun goChatroomPage() {
        curVoiceRoomModel?.let {
            ARouter.getInstance()
                .build(RouterPath.ChatroomPath)
                .withSerializable(RouterParams.KEY_VOICE_ROOM_MODEL, it)
                .navigation()
            dismissLoading()
        }
    }

    private fun showInputDialog(voiceRoomModel: VoiceRoomModel) {
        RoomEncryptionInputDialog()
            .leftText(activity!!.getString(R.string.voice_room_cancel))
            .rightText(activity!!.getString(R.string.voice_room_confirm))
            .setOnClickListener(object : RoomEncryptionInputDialog.OnClickBottomListener {
                override fun onCancelClick() {}
                override fun onConfirmClick(password: String) {
                    voiceRoomViewModel.checkPassword(voiceRoomModel.roomId, voiceRoomModel.roomId, password)
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
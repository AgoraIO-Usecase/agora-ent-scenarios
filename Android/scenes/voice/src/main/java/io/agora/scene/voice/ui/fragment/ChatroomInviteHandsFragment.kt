package io.agora.scene.voice.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentHandsListLayoutBinding
import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.viewmodel.VoiceUserListViewModel
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.service.VoiceRoomSubscribeDelegate
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.adapter.ChatroomInviteAdapter
import io.agora.scene.voice.ui.dialog.ChatroomHandsDialog
import io.agora.voice.common.ui.BaseUiFragment
import io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools

class ChatroomInviteHandsFragment : BaseUiFragment<VoiceFragmentHandsListLayoutBinding>(),
    ChatroomInviteAdapter.onActionListener {
    private lateinit var userListViewModel: VoiceUserListViewModel
    private val dataList: MutableList<VoiceMemberModel> = ArrayList()
    private var baseAdapter: RoomBaseRecyclerViewAdapter<VoiceMemberModel>? = null
    private var adapter: ChatroomInviteAdapter? = null
    private var onFragmentListener: ChatroomHandsDialog.OnFragmentListener? = null
    private var roomId: String? = null
    private val map: MutableMap<String, Boolean> = HashMap()
    private var isRefreshing = false
    private var emptyView: View? = null
    private val voiceServiceProtocol = VoiceServiceProtocol.getImplInstance()

    // 房主准备邀请的麦位
    private var inviteMicIndex:Int = -1

    fun setInviteMicIndex(inviteMicIndex: Int){
        this.inviteMicIndex = inviteMicIndex
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        emptyView = layoutInflater.inflate(R.layout.voice_no_data_layout, container, false)
        val textView = emptyView?.findViewById<TextView>(R.id.content_item)
        textView?.text = getString(R.string.voice_empty_invite_hands)
        val params = LinearLayoutCompat.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        emptyView?.layoutParams = params
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentHandsListLayoutBinding {
        return VoiceFragmentHandsListLayoutBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getString("roomId")
        initView()
        initViewModel()
        initListener()
    }

    private fun initView() {
        baseAdapter = ChatroomInviteAdapter()
        adapter = baseAdapter as ChatroomInviteAdapter
        binding.let {
            it?.list?.layoutManager = LinearLayoutManager(
                activity
            )
            it?.list?.adapter = adapter
        }
        if (emptyView == null) {
            adapter?.setEmptyView(R.layout.voice_no_data_layout)
        } else {
            adapter?.setEmptyView(emptyView)
        }
    }

    override fun onResume() {
        super.onResume()
        reset()
    }

    private fun initViewModel() {
        userListViewModel = ViewModelProvider(this)[VoiceUserListViewModel::class.java]
        userListViewModel.inviteListObservable().observe(requireActivity()){ response: Resource<List<VoiceMemberModel>> ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
                override fun onSuccess(data: List<VoiceMemberModel>?) {
                    finishRefresh()
                    val total = data?.size ?: 0
                    adapter?.data = data ?: mutableListOf()
                    onFragmentListener?.getItemCount(total)
                    isRefreshing = false
                    adapter?.data?.let {
                        for (datum in it) {
                            if (map.containsKey(datum.chatUid)) {
                                adapter?.setInvited(map)
                            }
                        }
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    finishRefresh()
                }
            })
        }
        // 邀请上麦
        userListViewModel.startMicSeatInvitationObservable().observe(requireActivity()) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "invitation mic：$data".logD()
                    if (data != true) return
                    activity?.let {
                        ToastTools.show(it, getString(R.string.voice_room_invited))
                    }
                }
            })
        }

    }

    private fun initListener() {
        adapter?.setOnActionListener(this)
        binding?.swipeLayout?.setOnRefreshListener { reset() }
        voiceServiceProtocol.subscribeEvent(object : VoiceRoomSubscribeDelegate{
            override fun onReceiveSeatInvitationRejected(
                chatUid: String,
                message: ChatMessageData?
            ) {
                ThreadManager.getInstance().runOnMainThread {
                    adapter?.removeInvited(chatUid)
                }
            }
        })
    }

    private fun finishRefresh() {
        if (binding?.swipeLayout != null && binding?.swipeLayout?.isRefreshing == true) {
            binding?.swipeLayout?.isRefreshing = false
        }
    }

    fun reset() {
        isRefreshing = true
        userListViewModel.fetchInviteList()
    }

    override fun onItemActionClick(view: View, position: Int, chatUid: String) {
        map[chatUid] = true
        adapter?.setInvited(map)
        userListViewModel.startMicSeatInvitation(chatUid,inviteMicIndex)
    }

    fun setFragmentListener(listener: ChatroomHandsDialog.OnFragmentListener?) {
        this.onFragmentListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        map.clear()
    }

    fun micChanged(data: Map<Int, String>) {
        if (!adapter?.data.isNullOrEmpty()){
            adapter?.data?.let {
                dataList.addAll(it)
                for (key in data.keys) {
                    for (datum in it) {
                        if (data[key].toString() == datum.chatUid) {
                            reset()
                            return
                        }
                    }
                }
            }
        }
    }
}
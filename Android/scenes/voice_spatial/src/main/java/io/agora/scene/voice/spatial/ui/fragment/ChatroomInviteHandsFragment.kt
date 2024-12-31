package io.agora.scene.voice.spatial.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentHandsListLayoutBinding
import io.agora.scene.voice.spatial.global.IParserSource
import io.agora.scene.voice.spatial.model.VoiceMemberModel
import io.agora.scene.voice.spatial.net.OnResourceParseCallback
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.adapter.ChatroomInviteAdapter
import io.agora.scene.voice.spatial.ui.adapter.RoomBaseRecyclerViewAdapter
import io.agora.scene.voice.spatial.ui.dialog.ChatroomHandsDialog
import io.agora.scene.voice.spatial.viewmodel.VoiceUserListViewModel
import io.agora.scene.widget.toast.CustomToast

class ChatroomInviteHandsFragment : BaseViewBindingFragment<VoiceSpatialFragmentHandsListLayoutBinding>(),
    ChatroomInviteAdapter.onActionListener, IParserSource {
    private val TAG = ChatroomInviteHandsFragment::class.java.simpleName

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        emptyView = layoutInflater.inflate(R.layout.voice_spatial_no_data_layout, container, false)
        val textView = emptyView?.findViewById<TextView>(R.id.content_item)
        textView?.text = getString(R.string.voice_spatial_empty_invite_hands)
        val params = LinearLayoutCompat.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        emptyView?.layoutParams = params
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialFragmentHandsListLayoutBinding {
        return VoiceSpatialFragmentHandsListLayoutBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getString("roomId")
        initView()
        initViewModel()
        initListener()
    }

    override fun initView() {
        baseAdapter = ChatroomInviteAdapter()
        adapter = baseAdapter as ChatroomInviteAdapter
        binding.let {
            it?.list?.layoutManager = LinearLayoutManager(
                activity
            )
            it?.list?.adapter = adapter
        }
        if (emptyView == null) {
            adapter?.setEmptyView(R.layout.voice_spatial_no_data_layout)
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
        userListViewModel.inviteListObservable()
            .observe(requireActivity()) { response: Resource<List<VoiceMemberModel>> ->
                parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
                    override fun onSuccess(data: List<VoiceMemberModel>?) {
                        finishRefresh()
                        adapter?.data = data?.filter { it.micIndex == -1 }
                        onFragmentListener?.getItemCount(adapter?.data?.size ?: 0)
                        isRefreshing = false
                        adapter?.data?.let {
                            for (datum in it) {
                                if (map.containsKey(datum.userId)) {
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
        // Invite to mic
        userListViewModel.startMicSeatInvitationObservable().observe(requireActivity()) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceSpatialLogger.d(TAG, "invitation mic：$data")
                    if (data != true) return
                    CustomToast.show(getString(R.string.voice_spatial_room_invited))
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    CustomToast.show(getString(R.string.voice_spatial_room_invitation_fail))
                }
            })
        }

    }

    override fun initListener() {
        adapter?.setOnActionListener(this)
        binding?.swipeLayout?.setOnRefreshListener { reset() }
        voiceServiceProtocol.subscribeEvent(object : VoiceRoomSubscribeDelegate {
            override fun onReceiveSeatInvitationRejected(chatUid: String) {
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

    override fun onItemActionClick(view: View, position: Int, userId: String) {
        map[userId] = true
        adapter?.setInvited(map)
        userListViewModel.startMicSeatInvitation(userId, index)
    }

    private var index: Int = 0
    fun setIndex(index: Int) {
        this.index = index
    }

    fun setFragmentListener(listener: ChatroomHandsDialog.OnFragmentListener?) {
        this.onFragmentListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        map.clear()
    }

    fun micChanged(data: Map<Int, String>) {
        if (!adapter?.data.isNullOrEmpty()) {
            adapter?.data?.let {
                dataList.addAll(it)
                for (key in data.keys) {
                    for (datum in it) {
                        if (data[key].toString() == datum.userId) {
                            reset()
                            return
                        }
                    }
                }
            }
        }
    }
}
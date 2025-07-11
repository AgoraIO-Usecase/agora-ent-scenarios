package io.agora.scene.voice.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemChildClickListener
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.R
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.databinding.VoiceFragmentAudienceListBinding
import io.agora.scene.voice.databinding.VoiceItemRoomAudienceListBinding
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.scene.voice.service.VoiceServiceListenerProtocol
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.adapter.viewholder.RoomAudienceListViewHolder
import io.agora.scene.voice.viewmodel.VoiceUserListViewModel
import io.agora.scene.voice.netkit.OnResourceParseCallback
import io.agora.scene.voice.netkit.Resource
import io.agora.scene.voice.ui.IParserSource

class RoomAudienceListFragment : BaseViewBindingFragment<VoiceFragmentAudienceListBinding>(),
    SwipeRefreshLayout.OnRefreshListener, IParserSource {

    companion object {

        private val TAG = RoomAudienceListFragment::class.java.simpleName
        private const val KEY_ROOM_INFO = "voice_room_info"
        private val voiceServiceProtocol = VoiceServiceProtocol.serviceProtocol

        fun getInstance(voiceRoomModel: VoiceRoomModel): RoomAudienceListFragment {
            return RoomAudienceListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, voiceRoomModel)
                }
            }
        }
    }

    private var voiceRoomModel: VoiceRoomModel? = null

    private lateinit var userListViewModel: VoiceUserListViewModel

    private var total = 0
        set(value) {
            field = value
            checkEmpty()
        }
    private var isEnd = false
    private val members = mutableListOf<VoiceMemberModel>()

    private var audienceAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAudienceListBinding, VoiceMemberModel, RoomAudienceListViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentAudienceListBinding {
        return VoiceFragmentAudienceListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListViewModel = ViewModelProvider(this)[VoiceUserListViewModel::class.java]
        arguments?.apply {
            voiceRoomModel = getSerializable(KEY_ROOM_INFO) as VoiceRoomModel?
            voiceRoomModel?.let {
                userListViewModel.fetchMemberList()
                voiceServiceProtocol.subscribeListener(object : VoiceServiceListenerProtocol {
                    override fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {
                        VoiceLogger.d(TAG, "voiceServiceProtocol onUserJoinedRoom：")
                        ThreadManager.getInstance().runOnMainThread {
                            audienceAdapter?.addItem(voiceMember)
                            total += 1
                        }
                    }
                })
            }
        }
        binding?.apply {
            initAdapter(rvAudienceList)
            slAudienceList.setOnRefreshListener(this@RoomAudienceListFragment)
        }

        onObservable()
    }

    private fun checkEmpty() {
        binding?.apply {
            if (total == 0) {
                ivContributionEmpty.isVisible = true
                mtContributionEmpty.isVisible = true
            } else {
                ivContributionEmpty.isVisible = false
                mtContributionEmpty.isVisible = false
            }
        }
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        audienceAdapter = BaseRecyclerViewAdapter(
            members,
            null,
            object : OnItemChildClickListener<VoiceMemberModel> {
                override fun onItemChildClick(
                    data: VoiceMemberModel?,
                    extData: Any?,
                    view: View,
                    position: Int,
                    itemViewType: Long
                ) {
                    data?.chatUid?.let {
                        if (extData is Int) {
                            handleRequest(it, extData, position)
                        }
                    }
                }
            },
            RoomAudienceListViewHolder::class.java
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        context?.let {
            recyclerView.addItemDecoration(
                MaterialDividerItemDecoration(it, MaterialDividerItemDecoration.VERTICAL).apply {
                    dividerThickness = 1.dp.toInt()
                    dividerInsetStart = 15.dp.toInt()
                    dividerInsetEnd = 15.dp.toInt()
                    dividerColor = ResourcesCompat.getColor(it.resources, R.color.voice_divider_color_1f979797,null)
                }
            )
        }
        recyclerView.adapter = audienceAdapter
    }

    private fun onObservable() {
        userListViewModel.memberListObservable()
            .observe(requireActivity()) { response: Resource<List<VoiceMemberModel>> ->
                parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
                    override fun onSuccess(data: List<VoiceMemberModel>?) {
                        binding?.slAudienceList?.isRefreshing = false
                        VoiceLogger.d(TAG, "getMembers total：${data?.size}")
                        if (data == null) return
                        total = data.size
                        isEnd = true
                        if (data.isNotEmpty()) {
                            audienceAdapter?.addItems(data)
                        } else {
                            isEnd = true
                        }
                    }

                    override fun onError(code: Int, message: String?) {
                        super.onError(code, message)
                        binding?.slAudienceList?.isRefreshing = false
                    }
                })
            }
        userListViewModel.kickOffObservable().observe(requireActivity()) { response: Resource<Int> ->
            parseResource(response, object : OnResourceParseCallback<Int>() {
                override fun onSuccess(position: Int?) {
                    ThreadManager.getInstance().runOnMainThread {
                        position?.let {
                            audienceAdapter?.deleteItem(it)
                            if (total > 0) {
                                total -= 1
                            }
                        }
                    }
                }

                override fun onError(code: Int, message: String?) {
                    VoiceLogger.e(TAG, "kickOff member：$code $message")
                }
            })

        }


//        userListViewModel.startMicSeatInvitationObservable().observe(requireActivity()) {

//        }
    }

    override fun onRefresh() {
        audienceAdapter?.clear()
        userListViewModel.fetchMemberList()
    }

    private fun handleRequest(uid: String, @MicClickAction action: Int, position: Int) {
        if (action == MicClickAction.Invite) {
            userListViewModel.startMicSeatInvitation(uid, -1)
        } else if (action == MicClickAction.KickOff) {
            userListViewModel.kickMembersOutOfTheRoom(uid, position)
        }
    }
}



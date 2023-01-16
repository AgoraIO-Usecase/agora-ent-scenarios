//package io.agora.scene.voice.ui.fragment
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.view.isVisible
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
//import com.google.android.material.divider.MaterialDividerItemDecoration
//import io.agora.scene.voice.bean.RoomKitBean
//import io.agora.scene.voice.ui.adapter.viewholder.RoomAudienceListViewHolder
//import io.agora.voice.baseui.ui.BaseUiFragment
//import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
//import io.agora.voice.baseui.adapter.OnItemChildClickListener
//import io.agora.voice.baseui.ui.common.OnResourceParseCallback
//import io.agora.voice.baseui.general.net.Resource
//import io.agora.voice.buddy.tool.*
//import io.agora.voice.baseui.utils.LogTools.logE
//import io.agora.voice.baseui.utils.DeviceTools.dp
//import io.agora.scene.voice.R
//import io.agora.scene.voice.annotation.MicClickAction
//import io.agora.scene.voice.databinding.VoiceFragmentAudienceListBinding
//import io.agora.scene.voice.databinding.VoiceItemRoomAudienceListBinding
//import io.agora.scene.voice.model.VoiceUserListViewModel
//import io.agora.scene.voice.model.VoiceMemberModel
//
//class RoomAudienceListFragment : BaseUiFragment<VoiceFragmentAudienceListBinding>(),
//    SwipeRefreshLayout.OnRefreshListener {
//
//    companion object {
//
//        private const val KEY_ROOM_INFO = "room_info"
//
//        fun getInstance(roomKitBean: RoomKitBean): RoomAudienceListFragment {
//            return RoomAudienceListFragment().apply {
//                arguments = Bundle().apply {
//                    putSerializable(KEY_ROOM_INFO, roomKitBean)
//                }
//            }
//        }
//    }
//
//    private var roomKitBean: RoomKitBean? = null
//
//    private lateinit var userListViewModel: VoiceUserListViewModel
//
//    private var total = 0
//    private var isEnd = false
//    private val members = mutableListOf<VoiceMemberModel>()
//
//    private var audienceAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAudienceListBinding, VoiceMemberModel, RoomAudienceListViewHolder>? =
//        null
//
//    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentAudienceListBinding {
//        return VoiceFragmentAudienceListBinding.inflate(inflater, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        userListViewModel = ViewModelProvider(this)[VoiceUserListViewModel::class.java]
//        arguments?.apply {
//            roomKitBean = getSerializable(KEY_ROOM_INFO) as RoomKitBean?
//            roomKitBean?.let {
//                userListViewModel.fetchRoomMembers()
//            }
//        }
//        binding?.apply {
//            initAdapter(rvAudienceList)
//            slAudienceList.setOnRefreshListener(this@RoomAudienceListFragment)
//        }
//        onObservable()
//    }
//
//    private fun checkEmpty() {
//        binding?.apply {
//            if (total == 0) {
//                ivContributionEmpty.isVisible = true
//                mtContributionEmpty.isVisible = true
//            } else {
//                ivContributionEmpty.isVisible = false
//                mtContributionEmpty.isVisible = false
//            }
//        }
//    }
//
//    private fun initAdapter(recyclerView: RecyclerView) {
//        audienceAdapter =
//            BaseRecyclerViewAdapter(
//                members,
//                null,
//                object : OnItemChildClickListener<VoiceMemberModel> {
//                    override fun onItemChildClick(
//                        data: VoiceMemberModel?,
//                        extData: Any?,
//                        view: View,
//                        position: Int,
//                        itemViewType: Long
//                    ) {
//                        data?.chatUid?.let {
//                            if (extData is Int) {
//                                handleRequest(it, extData)
//                            }
//                        }
//                    }
//                },
//                RoomAudienceListViewHolder::class.java
//            )
//
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        context?.let {
//            recyclerView.addItemDecoration(
//                MaterialDividerItemDecoration(it, MaterialDividerItemDecoration.VERTICAL).apply {
//                    dividerThickness = 1.dp.toInt()
//                    dividerInsetStart = 15.dp.toInt()
//                    dividerInsetEnd = 15.dp.toInt()
//                    dividerColor = ResourcesTools.getColor(it.resources, R.color.voice_divider_color_1f979797)
//                }
//            )
//        }
//        recyclerView.adapter = audienceAdapter
//    }
//
//    private fun onObservable() {
//        userListViewModel.membersObservable().observe(requireActivity()) { response: Resource<List<VoiceMemberModel>> ->
//            parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
//                override fun onSuccess(data: List<VoiceMemberModel>?) {
//                    binding?.slAudienceList?.isRefreshing = false
//                    "getMembers total：${data?.size}".logE()
//                    if (data == null) return
//                    total = data.size
//                    checkEmpty()
//                    isEnd = true
//                    if (data.isNotEmpty()) {
//                        audienceAdapter?.addItems(data)
//                    } else {
//                        isEnd = true
//                    }
//                }
//
//                override fun onError(code: Int, message: String?) {
//                    super.onError(code, message)
//                    binding?.slAudienceList?.isRefreshing = false
//                }
//            })
//        }
//        userListViewModel.kickOffObservable().observe(requireActivity()) {
//        }
//        userListViewModel.startMicSeatInvitationObservable().observe(requireActivity()) {
//        }
//    }
//
//    override fun onRefresh() {
//        userListViewModel.fetchRoomMembers()
//    }
//
//    private fun handleRequest(uid: String, @MicClickAction action: Int) {
//        if (action == MicClickAction.Invite) {
//            userListViewModel.startMicSeatInvitation(uid, -1)
//        } else if (action == MicClickAction.KickOff) {
//            userListViewModel.kickOff(-1)
//        }
//    }
//}
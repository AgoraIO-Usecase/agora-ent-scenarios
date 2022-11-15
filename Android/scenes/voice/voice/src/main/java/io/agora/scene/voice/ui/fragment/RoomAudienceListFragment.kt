package io.agora.scene.voice.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.voice.bean.RoomKitBean
import io.agora.scene.voice.model.RoomRankViewModel
import io.agora.scene.voice.ui.adapter.RoomAudienceListViewHolder
import io.agora.voice.baseui.BaseUiFragment
import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.baseui.adapter.OnItemChildClickListener
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.tool.*
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.buddy.tool.DeviceTools.dp
import io.agora.scene.voice.R
import io.agora.secnceui.annotation.MicClickAction
import io.agora.scene.voice.databinding.VoiceFragmentAudienceListBinding
import io.agora.scene.voice.databinding.VoiceItemRoomAudienceListBinding
import io.agora.voice.network.tools.VRValueCallBack
import io.agora.voice.network.tools.bean.VMemberBean
import io.agora.voice.network.tools.bean.VRoomUserBean

class RoomAudienceListFragment : BaseUiFragment<VoiceFragmentAudienceListBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    companion object {

        private const val KEY_ROOM_INFO = "room_info"

        fun getInstance(roomKitBean: RoomKitBean): RoomAudienceListFragment {
            return RoomAudienceListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, roomKitBean)
                }
            }
        }
    }

    private var roomKitBean: RoomKitBean? = null

    private lateinit var roomRankViewModel: RoomRankViewModel

    private var pageSize = 10
    private var cursor = ""
    private var total = 0
    private var isEnd = false
    private val members = mutableListOf<VMemberBean>()

    private var audienceAdapter: BaseRecyclerViewAdapter<VoiceItemRoomAudienceListBinding, VMemberBean, RoomAudienceListViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentAudienceListBinding {
        return VoiceFragmentAudienceListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomRankViewModel =
            ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[RoomRankViewModel::class.java]
        arguments?.apply {
            roomKitBean = getSerializable(KEY_ROOM_INFO) as RoomKitBean?
            roomKitBean?.let {
                roomRankViewModel.getMembers(requireContext(), it.roomId, pageSize, cursor)
            }
        }
        binding?.apply {
            initAdapter(rvAudienceList)
            slAudienceList.setOnRefreshListener(this@RoomAudienceListFragment)
        }
        roomRankViewModel.membersObservable().observe(requireActivity()) { response: Resource<VRoomUserBean> ->
            parseResource(response, object : OnResourceParseCallback<VRoomUserBean>() {
                override fun onSuccess(data: VRoomUserBean?) {
                    binding?.slAudienceList?.isRefreshing = false
                    "getMembers cursor：${data?.cursor}，total：${data?.total}".logE()
                    if (data == null) return
                    cursor = data.cursor ?: ""
                    total = data.total
                    checkEmpty()
                    if (!data.members.isNullOrEmpty()) {
                        if (data.members.size < pageSize) {
                            isEnd = true
                        }
                        audienceAdapter?.addItems(data.members)
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
        audienceAdapter =
            BaseRecyclerViewAdapter(
                members,
                null,
                object : OnItemChildClickListener<VMemberBean> {
                    override fun onItemChildClick(
                        data: VMemberBean?,
                        extData: Any?,
                        view: View,
                        position: Int,
                        itemViewType: Long
                    ) {
                        if (extData is Int) {
                            handleRequest(roomKitBean?.roomId, data?.uid, extData)
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
                    dividerColor = ResourcesTools.getColor(it.resources, R.color.voice_divider_color_1f979797)
                }
            )
        }
        recyclerView.adapter = audienceAdapter
    }

    override fun onRefresh() {
        if (isEnd || cursor.isEmpty()) {
            ThreadManager.getInstance().runOnMainThreadDelay({
                binding?.slAudienceList?.isRefreshing = false
            }, 1500)
        } else {
            roomKitBean?.let {
                roomRankViewModel.getMembers(requireContext(), it.roomId, pageSize, cursor)
            }
        }
    }

    private fun handleRequest(roomId: String?, uid: String?, @MicClickAction action: Int) {
        if (roomId.isNullOrEmpty() || uid.isNullOrEmpty()) return
        context?.let { parentContext ->
            if (action == MicClickAction.Invite) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(parentContext).invitationMic(roomId, uid, object :
                    VRValueCallBack<Boolean> {
                    override fun onSuccess(var1: Boolean?) {
                        if (var1 != true) return
                        ThreadManager.getInstance().runOnMainThread(object :Runnable{
                            override fun run() {
                                activity?.let {
                                    ToastTools.show(it, it.getString(R.string.voice_chatroom_host_invitation_sent))
                                }
                            }

                        })
                    }

                    override fun onError(var1: Int, var2: String?) {

                    }
                })
            } else if (action == MicClickAction.KickOff) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(parentContext)
                    .kickMic(roomId, uid, -1, object :
                        VRValueCallBack<Boolean> {
                        override fun onSuccess(var1: Boolean?) {
                            if (var1 != true) return
                            activity?.let {
                                ToastTools.show(it, "kickMic success")
                            }
                        }

                        override fun onError(var1: Int, var2: String?) {
                            activity?.let {
                                ToastTools.show(it, "kickMic onError $var1 $var2")
                            }
                        }
                    })
            }
        }


    }
}
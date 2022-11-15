package io.agora.scene.voice.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.voice.bean.RoomKitBean
import io.agora.scene.voice.model.RoomRankViewModel
import io.agora.scene.voice.ui.adapter.RoomContributionRankingViewHolder
import io.agora.voice.baseui.BaseUiFragment
import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.DeviceTools.dp
import io.agora.voice.buddy.tool.ResourcesTools
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentContributionRankingBinding
import io.agora.scene.voice.databinding.VoiceItemContributionRankingBinding
import io.agora.voice.network.tools.bean.VRGiftBean
import io.agora.voice.network.tools.bean.VRankingMemberBean

class RoomContributionRankingFragment : BaseUiFragment<VoiceFragmentContributionRankingBinding>(),
    OnRefreshListener {

    companion object {
        private const val KEY_ROOM_INFO = "room_info"

        fun getInstance(roomKitBean: RoomKitBean): RoomContributionRankingFragment {
            return RoomContributionRankingFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, roomKitBean)
                }
            }
        }
    }

    private var roomKitBean: RoomKitBean? = null
    private var total = 0
    private var isEnd = false

    private lateinit var roomRankViewModel: RoomRankViewModel

    private var contributionAdapter: BaseRecyclerViewAdapter<VoiceItemContributionRankingBinding, VRankingMemberBean, RoomContributionRankingViewHolder>? =
        null


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceFragmentContributionRankingBinding {
        return VoiceFragmentContributionRankingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomRankViewModel = ViewModelProvider(
            this,
            io.agora.scene.voice.model.RoomRankViewModelFactory()
        )[RoomRankViewModel::class.java]

        arguments?.apply {
            roomKitBean = getSerializable(KEY_ROOM_INFO) as RoomKitBean?
            roomKitBean?.let {
                roomRankViewModel.getGifts(requireContext(), it.roomId)
            }
        }
        binding?.apply {
            initAdapter(rvContributionRanking)
            slContributionRanking.setOnRefreshListener(this@RoomContributionRankingFragment)
        }
        roomRankViewModel.giftsObservable().observe(requireActivity()) { response: Resource<VRGiftBean> ->
            parseResource(response, object : OnResourceParseCallback<VRGiftBean>() {
                override fun onSuccess(data: VRGiftBean?) {
                    binding?.slContributionRanking?.isRefreshing = false
                    total = data?.ranking_list?.size ?: 0
                    "getGifts total：${total}".logD()
                    if (data == null) return
                    isEnd = true
                    checkEmpty()
                    if (!data.ranking_list.isNullOrEmpty()) {
                        contributionAdapter?.submitListAndPurge(data.ranking_list)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.slContributionRanking?.isRefreshing = false
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
        contributionAdapter =
            BaseRecyclerViewAdapter(null, null, RoomContributionRankingViewHolder::class.java)

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
        recyclerView.adapter = contributionAdapter
    }

    override fun onRefresh() {
        if (isEnd) {
            ThreadManager.getInstance().runOnMainThreadDelay({
                binding?.slContributionRanking?.isRefreshing = false
            }, 1500)
        } else {
            roomKitBean?.let {
                roomRankViewModel.getGifts(requireContext(), it.roomId)
            }
        }
    }
}
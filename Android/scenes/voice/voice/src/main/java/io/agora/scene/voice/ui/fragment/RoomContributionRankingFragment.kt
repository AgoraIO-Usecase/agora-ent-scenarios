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
import io.agora.scene.voice.model.RoomKitBean
import io.agora.scene.voice.ui.adapter.viewholder.RoomContributionRankingViewHolder
import io.agora.voice.common.ui.BaseUiFragment
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.DeviceTools.dp
import io.agora.voice.common.utils.ResourcesTools
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentContributionRankingBinding
import io.agora.scene.voice.databinding.VoiceItemContributionRankingBinding
import io.agora.scene.voice.viewmodel.VoiceUserListViewModel
import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.voice.common.net.Resource

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

    private lateinit var roomRankViewModel: VoiceUserListViewModel

    private var contributionAdapter: BaseRecyclerViewAdapter<VoiceItemContributionRankingBinding, VoiceRankUserModel, RoomContributionRankingViewHolder>? =
        null


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceFragmentContributionRankingBinding {
        return VoiceFragmentContributionRankingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomRankViewModel = ViewModelProvider(this)[VoiceUserListViewModel::class.java]

        roomKitBean = arguments?.getSerializable(KEY_ROOM_INFO) as RoomKitBean?
        binding?.apply {
            initAdapter(rvContributionRanking)
            slContributionRanking.setOnRefreshListener(this@RoomContributionRankingFragment)
        }
        roomRankViewModel.fetchGiftContribute()
        roomRankViewModel.contributeListObservable()
            .observe(requireActivity()) { response: Resource<List<VoiceRankUserModel>> ->
                parseResource(response, object : OnResourceParseCallback<List<VoiceRankUserModel>>() {
                    override fun onSuccess(data: List<VoiceRankUserModel>?) {
                        binding?.slContributionRanking?.isRefreshing = false
                        val total = data?.size ?: 0
                        "getGifts total：$total".logD()
                        checkEmpty(total)
                        contributionAdapter?.submitListAndPurge(data ?: mutableListOf())
                    }

                    override fun onError(code: Int, message: String?) {
                        super.onError(code, message)
                        binding?.slContributionRanking?.isRefreshing = false
                    }
                })
            }
    }

    private fun checkEmpty(total: Int) {
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
            BaseRecyclerViewAdapter(
                null,
                null,
                RoomContributionRankingViewHolder::class.java
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
        recyclerView.adapter = contributionAdapter
    }

    override fun onRefresh() {
        roomKitBean?.let {
            roomRankViewModel.fetchGiftContribute()
        }
    }
}
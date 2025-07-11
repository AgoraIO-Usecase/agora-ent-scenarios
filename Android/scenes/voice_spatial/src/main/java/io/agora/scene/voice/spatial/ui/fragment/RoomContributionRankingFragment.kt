package io.agora.scene.voice.spatial.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentContributionRankingBinding
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemContributionRankingBinding
import io.agora.scene.voice.spatial.global.IParserSource
import io.agora.scene.voice.spatial.model.RoomKitBean
import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.scene.voice.spatial.net.OnResourceParseCallback
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.ui.adapter.viewholder.RoomContributionRankingViewHolder
import io.agora.scene.voice.spatial.viewmodel.VoiceUserListViewModel

class RoomContributionRankingFragment : BaseViewBindingFragment<VoiceSpatialFragmentContributionRankingBinding>(),
    OnRefreshListener, IParserSource {

    companion object {
        private const val TAG = "RoomContributionRankingFragment"

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

    private var contributionAdapter: BaseRecyclerViewAdapter<VoiceSpatialItemContributionRankingBinding, VoiceRankUserModel, RoomContributionRankingViewHolder>? =
        null


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialFragmentContributionRankingBinding {
        return VoiceSpatialFragmentContributionRankingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomRankViewModel = ViewModelProvider(this)[VoiceUserListViewModel::class.java]

        roomKitBean = arguments?.getSerializable(KEY_ROOM_INFO) as RoomKitBean?
        binding?.apply {
            initAdapter(rvContributionRanking)
            slContributionRanking.setOnRefreshListener(this@RoomContributionRankingFragment)
        }
        roomRankViewModel.contributeListObservable()
            .observe(requireActivity()) { response: Resource<List<VoiceRankUserModel>> ->
                parseResource(response, object : OnResourceParseCallback<List<VoiceRankUserModel>>() {
                    override fun onSuccess(data: List<VoiceRankUserModel>?) {
                        binding?.slContributionRanking?.isRefreshing = false
                        val total = data?.size ?: 0
                        VoiceSpatialLogger.d(TAG, "getGifts total：$total")
                        checkEmpty(total)
                        contributionAdapter?.replaceItems(data ?: mutableListOf())
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
                    dividerColor = ResourcesCompat.getColor(it.resources, io.agora.scene.widget.R.color.divider_color_1f979797, null)
                }
            )
        }
        recyclerView.adapter = contributionAdapter
    }

    override fun onRefresh() {

    }
}
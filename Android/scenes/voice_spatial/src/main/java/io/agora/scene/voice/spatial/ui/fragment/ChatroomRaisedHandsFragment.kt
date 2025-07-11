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
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentHandsListLayoutBinding
import io.agora.scene.voice.spatial.global.IParserSource
import io.agora.scene.voice.spatial.model.VoiceMemberModel
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.net.OnResourceParseCallback
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.ui.adapter.ChatroomRaisedAdapter
import io.agora.scene.voice.spatial.ui.adapter.RoomBaseRecyclerViewAdapter
import io.agora.scene.voice.spatial.ui.dialog.ChatroomHandsDialog
import io.agora.scene.voice.spatial.viewmodel.VoiceUserListViewModel
import io.agora.scene.widget.toast.CustomToast

class ChatroomRaisedHandsFragment : BaseViewBindingFragment<VoiceSpatialFragmentHandsListLayoutBinding>(),
    ChatroomRaisedAdapter.onActionListener, IParserSource {
    private lateinit var userListViewModel: VoiceUserListViewModel
    private var baseAdapter: RoomBaseRecyclerViewAdapter<VoiceMemberModel>? = null
    private var adapter: ChatroomRaisedAdapter? = null
    private var onFragmentListener: ChatroomHandsDialog.OnFragmentListener? = null
    private var roomId: String? = null
    private val map: MutableMap<String, Boolean> = HashMap()
    private var isRefreshing = false
    private var isLoadingNextPage = false
    private var emptyView: View? = null
    private var currentIndex: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        emptyView = layoutInflater.inflate(R.layout.voice_spatial_no_data_layout, container, false)
        val textView = emptyView?.findViewById<TextView>(R.id.content_item)
        textView?.text = getString(R.string.voice_spatial_empty_raised_hands)
        emptyView?.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialFragmentHandsListLayoutBinding? {
        return VoiceSpatialFragmentHandsListLayoutBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getString("roomId")
        initView()
        initListener()
        initViewModel()
    }

    override fun initView() {
        baseAdapter =
            ChatroomRaisedAdapter()
        adapter = baseAdapter as ChatroomRaisedAdapter
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
        userListViewModel.applicantsListObservable()
            .observe(requireActivity()) { response: Resource<List<VoiceMemberModel>> ->
                parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
                    override fun onSuccess(data: List<VoiceMemberModel>?) {
                        finishRefresh()
                        val total = data?.size ?: 0
                        adapter?.data = data ?: mutableListOf()
                        onFragmentListener?.getItemCount(total)
                        isRefreshing = false
                        adapter?.data?.let {
                            for (applyListBean in it) {
                                if (map.containsKey(applyListBean.userId)) {
                                    adapter?.setAccepted(
                                        applyListBean.userId,
                                        true == map[applyListBean.userId]
                                    )
                                }
                            }
                        }
                    }

                    override fun onError(code: Int, message: String) {
                        super.onError(code, message)
                        finishRefresh()
                    }
                })
            }
        // Accept mic seat application
        userListViewModel.acceptMicSeatApplyObservable()
            .observe(requireActivity()) { response: Resource<VoiceMicInfoModel> ->
                parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                    override fun onSuccess(data: VoiceMicInfoModel?) {
                        VoiceSpatialLogger.d(TAG, "accept mic seat apply：${data?.micIndex}")
                        data?.let {
                            it.member?.userId?.let {
                                adapter?.notifyItemRemoved(currentIndex)
                            }
                            onFragmentListener?.onAcceptMicSeatApply(it)
                        }
                    }

                    override fun onError(code: Int, message: String?) {
                        super.onError(code, message)
                        CustomToast.show(R.string.voice_spatial_room_agree_fail)
                    }
                })
            }
    }

    override fun initListener() {
        adapter?.setOnActionListener(this)
        binding?.swipeLayout?.setOnRefreshListener { reset() }
        binding?.list?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager?
                val lastVisibleItemPosition = lm?.findLastVisibleItemPosition()
                val totalCount = lm?.itemCount ?: 0
                if (lastVisibleItemPosition == totalCount - 1 && !isLoadingNextPage && !isRefreshing) {
                    // After addLoadItem, itemCount has changed
                    // Add a layer of judgment to ensure that the user is sliding to the place where it is being loaded, then load more
                    val findLastVisibleItemPosition = lm.findLastVisibleItemPosition()
                    if (findLastVisibleItemPosition == lm.itemCount - 1) {
                        ThreadManager.getInstance().runOnMainThread {
                            isLoadingNextPage = true
                        }
                    }
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
        activity?.let {
            userListViewModel.fetchApplicantsList()
        }
    }

    override fun onItemActionClick(view: View, index: Int, userId: String) {
        adapter?.setAccepted(userId, true)
        map[userId] = true
        currentIndex = index
        userListViewModel.acceptMicSeatApply(userId)
    }

    fun setFragmentListener(listener: ChatroomHandsDialog.OnFragmentListener?) {
        this.onFragmentListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        map.clear()
    }

    companion object {
        private const val TAG = "ChatroomRaisedHandsFragment"
    }
}
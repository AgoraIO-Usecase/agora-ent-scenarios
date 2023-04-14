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
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceFragmentHandsListLayoutBinding
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.viewmodel.VoiceUserListViewModel
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.ui.adapter.ChatroomRaisedAdapter
import io.agora.scene.voice.ui.dialog.ChatroomHandsDialog
import io.agora.voice.common.ui.BaseUiFragment
import io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ThreadManager

class ChatroomRaisedHandsFragment : BaseUiFragment<VoiceFragmentHandsListLayoutBinding>(),
    ChatroomRaisedAdapter.onActionListener {
    private lateinit var userListViewModel: VoiceUserListViewModel
    private var baseAdapter: RoomBaseRecyclerViewAdapter<VoiceMemberModel>? = null
    private var adapter: ChatroomRaisedAdapter? = null
    private var onFragmentListener: ChatroomHandsDialog.OnFragmentListener? = null
    private var roomId: String? = null
    private val map: MutableMap<String, Boolean> = HashMap()
    private var isRefreshing = false
    private var isLoadingNextPage = false
    private var emptyView: View? = null
    private var currentIndex:Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        emptyView = layoutInflater.inflate(R.layout.voice_no_data_layout, container, false)
        val textView = emptyView?.findViewById<TextView>(R.id.content_item)
        textView?.text = getString(R.string.voice_empty_raised_hands)
        emptyView?.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceFragmentHandsListLayoutBinding? {
        return VoiceFragmentHandsListLayoutBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getString("roomId")
        initView()
        initListener()
        initViewModel()
    }

    private fun initView() {
        baseAdapter = ChatroomRaisedAdapter()
        adapter = baseAdapter as ChatroomRaisedAdapter
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
        userListViewModel.applicantsListObservable()
            .observe(requireActivity()) { response: Resource<List<VoiceMemberModel>> ->
                parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
                    override fun onSuccess(data: List<VoiceMemberModel>?) {
                        finishRefresh()
                        val total = data?.size?:0
                        adapter?.data = data?: mutableListOf()
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
        // 同意上麦申请
        userListViewModel.acceptMicSeatApplyObservable()
            .observe(requireActivity()) { response: Resource<VoiceMicInfoModel> ->
                parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                    override fun onSuccess(data: VoiceMicInfoModel?) {
                        "accept mic seat apply：${data?.micIndex}".logD()
                        data?.let {
                            it.member?.chatUid?.let { chatUid ->
                                adapter?.notifyItemRemoved(currentIndex)
                                ChatroomIMManager.getInstance().removeSubmitMember(
                                    chatUid
                                )
                            }
                            onFragmentListener?.onAcceptMicSeatApply(it)
                        }
                    }
                })
            }
    }

    private fun initListener() {
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
                    // 在前面addLoadItem后，itemCount已经变化
                    // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
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

    override fun onItemActionClick(view: View, index: Int, uid: String) {
        adapter!!.setAccepted(uid, true)
        map[uid] = true
        currentIndex = index
        userListViewModel.acceptMicSeatApply(index, uid)
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
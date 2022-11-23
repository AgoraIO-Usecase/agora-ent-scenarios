package io.agora.scene.voice.ui.fragment

import io.agora.voice.baseui.BaseUiFragment
import io.agora.scene.voice.ui.adapter.ChatroomInviteAdapter
import io.agora.scene.voice.model.VoiceUserListViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import io.agora.scene.voice.R
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import android.widget.LinearLayout
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.buddy.tool.ThreadManager
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.databinding.VoiceFragmentHandsListLayoutBinding
import io.agora.scene.voice.service.VoiceMemberModel
import io.agora.voice.baseui.general.net.Resource
import java.util.ArrayList
import java.util.HashMap

class ChatroomInviteHandsFragment : BaseUiFragment<VoiceFragmentHandsListLayoutBinding>(),
    ChatroomInviteAdapter.onActionListener {
    private lateinit var userListViewModel: VoiceUserListViewModel
    private val dataList: MutableList<VoiceMemberModel> = ArrayList()
    private var adapter: ChatroomInviteAdapter? = null
    private val pageSize = 10
    private var listener: itemCountListener? = null
    private var roomId: String? = null
    private val map: MutableMap<String, Boolean> = HashMap()
    private var isRefreshing = false
    private var isLoadingNextPage = false
    private var emptyView: View? = null
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
        adapter = ChatroomInviteAdapter()
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
        userListViewModel = ViewModelProvider(this).get(VoiceUserListViewModel::class.java)
        userListViewModel.getInviteObservable().observe(requireActivity()){ response: Resource<List<VoiceMemberModel>> ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceMemberModel>>() {
                override fun onSuccess(data: List<VoiceMemberModel>?) {
                    var total = 0
                    if (data == null) return
                    dataList.clear()
                    dataList.addAll(data)
                    if (isRefreshing) {
                        adapter?.data = dataList
                    } else {
                        adapter?.addData(dataList)
                    }
                    listener?.getItemCount(total)
                    finishRefresh()
                    isRefreshing = false
                    adapter?.data?.let {
                        for (datum in it) {
                            if (map.containsKey(datum.uid)) {
                                adapter?.setInvited(map)
                            }
                        }
                    }
                }
            })
        }

        userListViewModel.startMicSeatInvitationObservable().observe(requireActivity()){
            // TODO:
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
                val totalCount = lm?.itemCount?:0
                if (lastVisibleItemPosition == totalCount - 1 && !isLoadingNextPage && !isRefreshing) {
                    // 在前面addLoadItem后，itemCount已经变化
                    // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
                    val findLastVisibleItemPosition = lm.findLastVisibleItemPosition()
                    if (findLastVisibleItemPosition == lm.itemCount - 1) {
                        ThreadManager.getInstance().runOnMainThread {
                            isLoadingNextPage = true
//                            if (!TextUtils.isEmpty(cursor)) {
//                                pullData()
//                            }
                        }
                    }
                }
            }
        })
    }

    private fun pullData() {
        ThreadManager.getInstance().runOnMainThread {
            userListViewModel.getInviteList()
            isLoadingNextPage = false
        }
    }

    private fun finishRefresh() {
        if (binding?.swipeLayout != null && binding?.swipeLayout?.isRefreshing == true) {
            binding?.swipeLayout?.isRefreshing = false
        }
    }

    fun reset() {
        isRefreshing = true
        userListViewModel.getInviteList()
    }

    override fun onItemActionClick(view: View, position: Int, uid: String) {
        // TODO:
        userListViewModel.startMicSeatInvitation(uid,-1)
    }

    interface itemCountListener {
        fun getItemCount(count: Int)
    }

    fun setItemCountChangeListener(listener: itemCountListener?) {
        this.listener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        map.clear()
    }

    fun micChanged(data: Map<String, String>) {
        if (!adapter?.data.isNullOrEmpty()){
            adapter?.data?.let {
                dataList.addAll(it)
                for (key in data.keys) {
                    for (datum in it) {
                        if (data[key].toString() == datum.uid) {
                            reset()
                            return
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "ChatroomInviteHandsFragment"
    }
}
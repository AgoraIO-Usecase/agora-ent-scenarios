package io.agora.scene.voice.ui.fragment

import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.baseui.BaseUiFragment
import io.agora.scene.voice.ui.adapter.ChatroomRaisedAdapter
import io.agora.scene.voice.model.VoiceUserListViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import io.agora.scene.voice.R
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import android.widget.LinearLayout
import io.agora.voice.network.tools.bean.VRMicListBean
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.scene.voice.general.net.ChatroomHttpManager
import io.agora.voice.network.tools.VRValueCallBack
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.databinding.VoiceFragmentHandsListLayoutBinding
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.tool.ToastTools
import java.util.HashMap

class ChatroomRaisedHandsFragment : BaseUiFragment<VoiceFragmentHandsListLayoutBinding>(),
    ChatroomRaisedAdapter.onActionListener {
    private lateinit var handsViewModel: VoiceUserListViewModel
    private var adapter: ChatroomRaisedAdapter? = null
    private val pageSize = 10
    private var cursor = ""
    private var listener: ItemCountListener? = null
    private var roomId: String? = null
    private val map: MutableMap<String, Boolean> = HashMap()
    private var isRefreshing = false
    private var isLoadingNextPage = false
    private var emptyView: View? = null
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
        adapter = ChatroomRaisedAdapter()
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
        handsViewModel = ViewModelProvider(this).get(VoiceUserListViewModel::class.java)
        handsViewModel.getRaisedObservable().observe(requireActivity()) { response: Resource<VRMicListBean> ->
            parseResource(response, object : OnResourceParseCallback<VRMicListBean?>() {
                override fun onSuccess(data: VRMicListBean?) {
                    if (data == null) return
                    val total = data.total
                    cursor = data.cursor
                    if (isRefreshing) {
                        adapter?.data = data.apply_list
                    } else {
                        adapter?.addData(data.apply_list)
                    }
                   listener?.getItemCount(total)
                    finishRefresh()
                    isRefreshing = false
                    adapter?.data?.let {
                        for (applyListBean in it) {
                            if (map.containsKey(applyListBean.member.uid)) {
                                adapter?.setAccepted(
                                    applyListBean.member.uid,
                                    true == map[applyListBean.member.uid]
                                )
                            }
                        }
                    }

                }

                override fun onError(code: Int, message: String) {
                    super.onError(code, message)
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
                val totalCount = lm?.itemCount?:0
                if (lastVisibleItemPosition == totalCount - 1 && !isLoadingNextPage && !isRefreshing) {
                    // 在前面addLoadItem后，itemCount已经变化
                    // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
                    val findLastVisibleItemPosition = lm.findLastVisibleItemPosition()
                    if (findLastVisibleItemPosition == lm.itemCount - 1) {
                        ThreadManager.getInstance().runOnMainThread {
                            isLoadingNextPage = true
                            if (!TextUtils.isEmpty(cursor)) {
                                pullData()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun pullData() {
        ThreadManager.getInstance().runOnMainThread {
            activity?.let {
                handsViewModel.getRaisedList(it, roomId, pageSize, cursor)
                isLoadingNextPage = false
            }

        }
    }

    protected fun finishRefresh() {
        if (binding?.swipeLayout != null && binding?.swipeLayout?.isRefreshing==true) {
            binding?.swipeLayout?.isRefreshing = false
        }
    }

    fun reset() {
        cursor = ""
        isRefreshing = true
        activity?.let {
            handsViewModel.getRaisedList(it, roomId, pageSize, cursor)
        }
    }

    override fun onItemActionClick(view: View, index: Int, uid: String) {
        ChatroomHttpManager.getInstance(activity)
            .applySubmitMic(roomId, uid, index, object : VRValueCallBack<Boolean> {
                override fun onSuccess(var1: Boolean) {
                    "onActionClick apply onSuccess $uid".logE(TAG)
                    ThreadManager.getInstance().runOnMainThread {
                        adapter?.setAccepted(uid, true)
                        map[uid] = true
                        activity?.let {
                            ToastTools.show(it,  getString(R.string.voice_room_agree_success))
                        }
                    }
                }

                override fun onError(code: Int, desc: String) {
                    "onActionClick apply onError $code $desc".logE(TAG)
                    activity?.let {
                        ToastTools.show(it,  getString(R.string.voice_room_agree_fail))
                    }
                }
            })
    }

    interface ItemCountListener {
        fun getItemCount(count: Int)
    }

    fun setItemCountChangeListener(listener: ItemCountListener?) {
        this.listener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        map.clear()
    }

    companion object {
        private const val TAG = "ChatroomRaisedHandsFragment"
    }
}
package io.agora.scene.show.widget.pk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLivePkRequestMessageListBinding
import io.agora.scene.show.service.ShowRoomDetailModel

class LivePKRequestMessageFragment : BaseFragment() {
    private var mBinding : ShowLivePkRequestMessageListBinding? = null
    private val binding get() = mBinding!!
    private lateinit var mListener : Listener
    private val linkPKViewAdapter : LivePKViewAdapter = LivePKViewAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkPKViewAdapter.setClickListener(object: LivePKViewAdapter.OnClickListener{
            override fun onClick(roomItem: ShowRoomDetailModel, position: Int) {
                mListener.onAcceptMicSeatItemChosen(roomItem)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ShowLivePkRequestMessageListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onlineBoardcasterList.adapter = linkPKViewAdapter
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            mListener.onRequestRefreshing()
        }
        binding.smartRefreshLayout.autoRefresh()
    }

    /**
     * 设置连麦申请列表
     */
    fun setOnlineBoardcasterList(roomList : List<ShowRoomDetailModel>) {
        if (mBinding == null) return
        if (roomList == null || roomList.isEmpty()) {
            binding.linkRequestListEmptyImg.setVisibility(View.VISIBLE)
            binding.linkRequestListEmpty.setVisibility(View.VISIBLE)
        } else {
            binding.linkRequestListEmptyImg.setVisibility(View.GONE)
            binding.linkRequestListEmpty.setVisibility(View.GONE)
        }
        linkPKViewAdapter.resetAll(roomList)
        binding.smartRefreshLayout.finishRefresh()
    }

    /**
     * pk-更新item选中状态
     */
    fun setPKInvitationItemStatus(roomItem: ShowRoomDetailModel, isInvited: Boolean) {
        if (mBinding == null) return
        binding.textPking.setText("与主播" + roomItem.roomName + "连麦中")
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAcceptMicSeatItemChosen(roomItem: ShowRoomDetailModel)
        fun onRequestRefreshing()
    }
}
package io.agora.scene.show.widget.pk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLivePkRequestMessageListBinding
import io.agora.scene.show.service.ShowInteractionInfo
import io.agora.scene.show.service.ShowInteractionStatus

class LivePKRequestMessageFragment : BaseFragment() {
    private var mBinding: ShowLivePkRequestMessageListBinding? = null
    private val binding get() = mBinding!!
    private lateinit var mListener: Listener
    private val linkPKViewAdapter: LivePKViewAdapter = LivePKViewAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkPKViewAdapter.setClickListener(object : LivePKViewAdapter.OnClickListener {
            override fun onClick(roomItem: LiveRoomConfig, position: Int) {
                mListener.onAcceptMicSeatItemChosen(roomItem)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = ShowLivePkRequestMessageListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onlineBoardcasterList.adapter = linkPKViewAdapter
        binding.smartRefreshLayout.setOnRefreshListener {
            mListener.onRequestRefreshing()
        }
        binding.iBtnStopPK.setOnClickListener {
            mListener.onStopPKingChosen()
        }
        binding.smartRefreshLayout.autoRefresh()
    }

    /**
     * 设置连麦申请列表
     */
    fun setOnlineBroadcasterList(interactionInfo: ShowInteractionInfo?, roomList: List<LiveRoomConfig>) {
        if (mBinding == null) return
        if (roomList.isEmpty()) {
            binding.linkRequestListEmptyImg.visibility = View.VISIBLE
            binding.linkRequestListEmpty.visibility = View.VISIBLE
        } else {
            binding.linkRequestListEmptyImg.visibility = View.GONE
            binding.linkRequestListEmpty.visibility = View.GONE
        }
        // TODO fix crash
//        if (interactionInfo == null) {
//            updateUI("", null)
//        } else {
//            updateUI(interactionInfo.userName, interactionInfo.interactStatus)
//        }
        linkPKViewAdapter.resetAll(roomList)
        binding.smartRefreshLayout.finishRefresh()
    }

    /**
     * pk-更新item选中状态
     */
    fun setPKInvitationItemStatus(userName: String, status: Int?) {
        if (mBinding == null) return
        // TODO fix crash
        //updateUI(userName, status)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAcceptMicSeatItemChosen(roomItem: LiveRoomConfig)
        fun onRequestRefreshing()
        fun onStopPKingChosen()
    }

    private fun updateUI(userName: String, status: Int?) {
        if (status == ShowInteractionStatus.pking.value) {
            binding.textPking.isVisible = true
            binding.iBtnStopPK.isVisible = true
            binding.textPking.text = getString(R.string.show_pk_to, userName)
        } else if (status == null) {
            binding.iBtnStopPK.isVisible = false
            binding.textPking.isVisible = false
        }
    }
}
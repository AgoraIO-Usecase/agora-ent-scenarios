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
            override fun onClick(view: View, roomItem: LiveRoomConfig, position: Int) {
                mListener.onAcceptMicSeatItemChosen(view, roomItem)
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
     * Set linking request list
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
        if (interactionInfo == null) {
            updateUI("", null)
        } else {
            updateUI(interactionInfo.userName, interactionInfo.interactStatus)
        }
        linkPKViewAdapter.resetAll(roomList)
        binding.smartRefreshLayout.finishRefresh()
    }

    /**
     * PK - Update item selection status
     */
    fun setPKInvitationItemStatus(userName: String, status: Int?) {
        if (mBinding == null) return
        updateUI(userName, status)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAcceptMicSeatItemChosen(view: View, roomItem: LiveRoomConfig)
        fun onRequestRefreshing()
        fun onStopPKingChosen()
    }

    private fun updateUI(userName: String, status: Int?) {
        if (status == ShowInteractionStatus.pking) {
            binding.textPking.isVisible = true
            binding.iBtnStopPK.isVisible = true
            if (isAdded) {
                binding.textPking.text = getString(R.string.show_pk_to, userName)
            }
            for (i in 0 until linkPKViewAdapter.itemCount) {
                val item = linkPKViewAdapter.getItem(i)
                if (item != null && item.getOwnerName() == userName) {
                    item.setInteractStatus(ShowInteractionStatus.pking)
                    linkPKViewAdapter.notifyItemChanged(i)
                    break
                }
            }

        } else if (status == null) {
            binding.iBtnStopPK.isVisible = false
            binding.textPking.isVisible = false
            for (i in 0 until linkPKViewAdapter.itemCount) {
                val item = linkPKViewAdapter.getItem(i)
                if (item != null && item.getInteractStatus() == ShowInteractionStatus.pking) {
                    item.setInteractStatus(ShowInteractionStatus.idle)
                    linkPKViewAdapter.notifyItemChanged(i)
                    break
                }
            }
        }
    }
}
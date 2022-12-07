package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageListBinding
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowRoomRequestStatus

class LiveLinkRequestFragment : BaseFragment() {
    private var mBinding : ShowLiveLinkRequestMessageListBinding? = null
    private val binding get() = mBinding!!
    private val linkRequestViewAdapter : LiveLinkRequestViewAdapter = LiveLinkRequestViewAdapter()
    private lateinit var mListener : Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkRequestViewAdapter.setClickListener(object : LiveLinkRequestViewAdapter.OnClickListener {
            override fun onClick(seatApply: ShowMicSeatApply, position: Int) {
                // 主播接受连麦
                mListener.onAcceptMicSeatItemChosen(seatApply, position)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ShowLiveLinkRequestMessageListBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.linkRequestList.adapter = linkRequestViewAdapter
        binding.iBtnStopLink.setOnClickListener {
            // 主播停止连麦
            mListener.onStopLinkingChosen()
        }
        binding.iBtnStopLink.isEnabled = false
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            mListener.onRequestRefreshing()
        }
        binding.smartRefreshLayout.autoRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    /**
     * 设置当前麦上状态
     */
    fun setOnSeatStatus(userName: String, status: ShowInteractionStatus) {
        if (mBinding == null) return
        if (status == ShowInteractionStatus.onSeat) {
            binding.iBtnStopLink.isEnabled = true
            binding.textLinking.setText("与观众 " + userName + " 连麦中")
        } else if (status == ShowInteractionStatus.idle) {
            binding.iBtnStopLink.isEnabled = false
            binding.textLinking.setText("未连麦")
        }
    }

    /**
     * 设置连麦申请列表
     */
    fun setSeatApplyList(list: List<ShowMicSeatApply>) {
        if (mBinding == null) return
        if (list == null || list.isEmpty()) {
            binding.linkRequestListEmptyImg.visibility = View.VISIBLE
            binding.linkRequestListEmpty.visibility = View.VISIBLE
        } else {
            binding.linkRequestListEmptyImg.visibility = View.GONE
            binding.linkRequestListEmpty.visibility = View.GONE
        }
        linkRequestViewAdapter.resetAll(list)
        binding.smartRefreshLayout.finishRefresh()
    }

    /**
     * 接受连麦-更新item选中状态
     */
    fun setSeatApplyItemStatus(seatApply: ShowMicSeatApply) {
        if (seatApply.status == ShowRoomRequestStatus.accepted) {
            val itemCount: Int = linkRequestViewAdapter.getItemCount()
            for (i in 0 until itemCount) {
                linkRequestViewAdapter.getItem(i)?.let {
                    if (it.userId == seatApply.userId) {
                        linkRequestViewAdapter.replace(i, ShowMicSeatApply(
                            it.userId,
                            it.userAvatar,
                            it.userName,
                            seatApply.status,
                            it.createAt
                        ))
                        linkRequestViewAdapter.notifyItemChanged(i)
                        return
                    }
                }
            }
        }
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAcceptMicSeatItemChosen(seatApply: ShowMicSeatApply, position: Int)
        fun onRequestRefreshing()
        fun onStopLinkingChosen()
    }
}
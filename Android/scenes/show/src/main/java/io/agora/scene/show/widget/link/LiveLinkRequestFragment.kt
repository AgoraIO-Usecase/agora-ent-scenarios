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
    private val mBinding by lazy {
        ShowLiveLinkRequestMessageListBinding.inflate(
            LayoutInflater.from(
                context
            )
        )}
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
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.linkRequestList.adapter = linkRequestViewAdapter
        mBinding.iBtnStopLink.setOnClickListener {
            // 主播停止连麦
            mListener.onStopLinkingChosen()
        }
        mBinding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            mListener.onRequestRefreshing()
        }
        mBinding.smartRefreshLayout.autoRefresh()
    }

    /**
     * 设置当前麦上状态
     */
    fun setOnSeatStatus(userName: String, status: ShowInteractionStatus) {
        if (status == ShowInteractionStatus.onSeat) {
            mBinding.iBtnStopLink.isEnabled = true
            mBinding.textLinking.setText("与观众" + userName + "连麦中")
        } else if (status == ShowInteractionStatus.idle) {
            mBinding.iBtnStopLink.isEnabled = false
            mBinding.textLinking.setText("未连麦")
        }
    }

    /**
     * 设置连麦申请列表
     */
    fun setSeatApplyList(list: List<ShowMicSeatApply>) {
        if (list == null || list.isEmpty()) {
            mBinding.linkRequestListEmpty.visibility = View.VISIBLE
        } else {
            mBinding.linkRequestListEmpty.visibility = View.GONE
        }
        linkRequestViewAdapter.resetAll(list)
        mBinding.smartRefreshLayout.finishRefresh()
    }

    /**
     * 接受连麦-更新item选中状态
     */
    fun setSeatApplyItemStatus(seatApply: ShowMicSeatApply) {
        if (seatApply.status == ShowRoomRequestStatus.accepted) {
            val itemCount: Int = linkRequestViewAdapter.getItemCount()
            for (i in 0 until itemCount) {
                var item: ShowMicSeatApply = linkRequestViewAdapter.getItem(i)!!
                if (item.userId == seatApply.userId) {
                    item = ShowMicSeatApply(
                        item.userId,
                        item.userAvatar,
                        item.userName,
                        seatApply.status,
                        item.createAt
                    )
                    linkRequestViewAdapter.notifyItemChanged(i)
                    break
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
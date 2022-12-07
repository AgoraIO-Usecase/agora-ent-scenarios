package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkAudienceBinding
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowRoomRequestStatus

class LiveLinkAudienceFragment : BaseFragment() {
    private val mBinding by lazy {
        ShowLiveLinkAudienceBinding.inflate(
            LayoutInflater.from(
                context
            )
        )}
    private val linkRequestViewAdapter : LiveLinkRequestViewAdapter = LiveLinkRequestViewAdapter()
    private lateinit var mListener : Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkRequestViewAdapter.setIsRoomOwner(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.linkRequestList.adapter = linkRequestViewAdapter
        mBinding.iBtnSeatApply.setOnClickListener {
            // 观众申请连麦
            mListener.onApplyOnSeat()
            mBinding.iBtnSeatApply.isVisible = false
            mBinding.iBtnStopLink.isVisible = false
            mBinding.iBtnCancelApply.isVisible = true
        }
        mBinding.iBtnStopLink.setOnClickListener {
            // 观众停止连麦
            mListener.onStopLinkingChosen()
        }
        mBinding.iBtnCancelApply.setOnClickListener {
            // 观众撤回申请
            mListener.onStopApplyingChosen()
            mBinding.iBtnSeatApply.isVisible = true
            mBinding.iBtnStopLink.isVisible = false
            mBinding.iBtnCancelApply.isVisible = false
        }
    }

    /**
     * 设置当前麦上状态
     */
    fun setOnSeatStatus(status: ShowInteractionStatus) {
        if (status == ShowInteractionStatus.onSeat) {
            mBinding.iBtnSeatApply.isVisible = false
            mBinding.iBtnStopLink.isVisible = true
            mBinding.iBtnSeatApply.setText("已上麦")
            mBinding.textLinking.setText("与主播连麦中")
        } else if (status == ShowInteractionStatus.idle) {
            mBinding.iBtnSeatApply.isVisible = true
            mBinding.iBtnStopLink.isVisible = false
            mBinding.iBtnSeatApply.setText("申请连麦")
            mBinding.textLinking.setText("可申请连麦")
        }
    }

    /**
     * 设置连麦申请列表
     */
    fun setSeatApplyList(list: List<ShowMicSeatApply>) {
        if (list == null || list.isEmpty()) {
            mBinding.iBtnSeatApply.visibility = View.VISIBLE
            mBinding.iBtnStopLink.visibility = View.GONE
            mBinding.iBtnCancelApply.visibility = View.GONE
            mBinding.linkRequestListEmpty.visibility = View.VISIBLE
        } else {
            mBinding.linkRequestListEmpty.visibility = View.GONE
        }
        linkRequestViewAdapter.resetAll(list)
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onApplyOnSeat()
        fun onStopLinkingChosen()
        fun onStopApplyingChosen()
    }
}
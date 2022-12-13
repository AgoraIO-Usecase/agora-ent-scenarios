package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.base.manager.UserManager
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkAudienceBinding
import io.agora.scene.show.service.ShowInteractionInfo
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowMicSeatApply

class LiveLinkAudienceFragment : BaseFragment() {
    private val mBinding by lazy {
        ShowLiveLinkAudienceBinding.inflate(
            LayoutInflater.from(
                context
            )
        )}
    private val linkRequestViewAdapter : LiveLinkRequestViewAdapter = LiveLinkRequestViewAdapter()
    private var mListener : Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkRequestViewAdapter.setIsRoomOwner(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.textLinking.setText(R.string.show_can_apply)
        mBinding.linkRequestList.adapter = linkRequestViewAdapter
        mBinding.iBtnStopLink.setOnClickListener {
            // 观众停止连麦
            mListener?.onStopLinkingChosen()
        }
        mBinding.iBtnCancelApply.setOnClickListener {
            // 观众撤回申请
            mListener?.onStopApplyingChosen()
            mBinding.iBtnStopLink.isVisible = false
            mBinding.iBtnCancelApply.isVisible = false
            mBinding.iBtnStopLinkText.isVisible = false
            mBinding.iBtnCancelApplyText.isVisible = false
        }
        mBinding.smartRefreshLayout.setOnRefreshListener {
            mListener?.onRequestRefreshing()
        }
        mBinding.smartRefreshLayout.autoRefresh()
    }

    /**
     * 设置当前麦上状态
     */
    fun setOnSeatStatus(userName: String, status: Int?) {
        if (status == ShowInteractionStatus.onSeat.value) {
            if (userName == UserManager.getInstance().user.name) {
                mBinding.iBtnCancelApply.isVisible = false
                mBinding.iBtnCancelApplyText.isVisible = false
                mBinding.iBtnStopLinkText.isVisible = true
                mBinding.iBtnStopLink.isVisible = true
                mBinding.textLinking.setText(R.string.show_linking)
            }
        } else if (status == null) {
            mBinding.iBtnStopLink.isVisible = false
            mBinding.iBtnStopLinkText.isVisible = false
            mBinding.iBtnCancelApplyText.isVisible = false
            mBinding.textLinking.setText(R.string.show_can_apply)
        }
    }

    fun setOnApplySuccess() {
        mBinding.iBtnCancelApply.isVisible = true
        mBinding.iBtnCancelApplyText.isVisible = true
    }

    /**
     * 设置连麦申请列表
     */
    fun setSeatApplyList(interactionInfo: ShowInteractionInfo?, list: List<ShowMicSeatApply>) {
        if (list.isEmpty()) {
            mBinding.linkRequestListEmptyImg.visibility = View.VISIBLE
            mBinding.linkRequestListEmpty.visibility = View.VISIBLE
        } else {
            mBinding.linkRequestListEmptyImg.visibility = View.GONE
            mBinding.linkRequestListEmpty.visibility = View.GONE
        }

        if (interactionInfo != null && interactionInfo.interactStatus == ShowInteractionStatus.onSeat.value &&
            interactionInfo.userId == UserManager.getInstance().user.id.toString()
        ) {
            mBinding.iBtnCancelApply.isVisible = false
            mBinding.iBtnCancelApplyText.isVisible = false
            mBinding.iBtnStopLinkText.isVisible = true
            mBinding.iBtnStopLink.isVisible = true
            mBinding.textLinking.setText(R.string.show_linking)
        }
        linkRequestViewAdapter.resetAll(list)
        mBinding.smartRefreshLayout.finishRefresh()
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onRequestRefreshing()
        fun onStopLinkingChosen()
        fun onStopApplyingChosen()
    }
}
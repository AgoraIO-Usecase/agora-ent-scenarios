package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkDialogBinding
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageListBinding
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowRoomRequestStatus
import io.agora.scene.show.widget.UserItem

class LiveLinkRequestFragment : BaseFragment() {
    private lateinit var mBinding : ShowLiveLinkRequestMessageListBinding
    private val linkRequestViewAdapter : LiveLinkRequestViewAdapter = LiveLinkRequestViewAdapter()
    private lateinit var mListener : Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkRequestViewAdapter.setClickListener(object : LiveLinkRequestViewAdapter.OnClickListener {
            override fun onClick(userItem: ShowMicSeatApply, position: Int) {
                // 主播接受连麦
                mListener.onAcceptMicSeatItemChosen(userItem, position)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ShowLiveLinkRequestMessageListBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.iBtnStopLink.setOnClickListener {
            // 主播停止连麦
            mListener.onStopLinkingChosen()
        }
    }

    /**
     * 设置当前麦上状态
     */
    fun setOnSeatStatus(userName: String) {
        mBinding.textLinking.setText("与观众" + userName + "连麦中")
    }

    /**
     * 设置连麦申请列表
     */
    fun setSeatApplyList(list: List<ShowMicSeatApply>) {
        if (list == null || list.isEmpty()) {
            mBinding.linkRequestListEmpty.setVisibility(View.VISIBLE)
        } else {
            mBinding.linkRequestListEmpty.setVisibility(View.GONE)
        }
        linkRequestViewAdapter.resetAll(list)
    }

    /**
     * 接受连麦-更新item选中状态
     */
    fun setSeatApplyItemStatus(seatApply: ShowMicSeatApply, isAccept: Boolean) {
        val itemCount: Int = linkRequestViewAdapter.getItemCount()
        for (i in 0 until itemCount) {
            var item: ShowMicSeatApply = linkRequestViewAdapter.getItem(i)!!
            if (item.userId == seatApply.userId) {
                item = ShowMicSeatApply(
                    item.userId,
                    item.userAvatar,
                    item.userName,
                    ShowRoomRequestStatus.accepted,
                    item.createAt
                )
                linkRequestViewAdapter.notifyItemChanged(i)
                break
            }
        }
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAcceptMicSeatItemChosen(userItem: ShowMicSeatApply, position: Int)
        fun onRequestRefreshing(tagIndex: Int)
        fun onStopLinkingChosen()
    }
}
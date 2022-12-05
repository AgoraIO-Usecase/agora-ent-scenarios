package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageListBinding
import io.agora.scene.show.widget.UserItem

class LiveLinkInvitationFragment : BaseFragment() {
    private lateinit var mBinding : ShowLiveLinkRequestMessageListBinding
    private val linkInvitationViewAdapter : LiveLinkInvitationViewAdapter = LiveLinkInvitationViewAdapter()
    private lateinit var mListener : LiveLinkInvitationFragment.Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkInvitationViewAdapter.setClickListener(object : LiveLinkInvitationViewAdapter.OnClickListener {
            override fun onClick(userItem: UserItem, position: Int) {
                // 主播发起邀请
                mListener.onInviteMicSeatItemChosen(userItem, position)
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
     * 连麦邀请列表-设置在线主播列表
     */
    fun setSeatInvitationList(list : List<UserItem>) {
        if (list == null || list.isEmpty()) {
            mBinding.linkRequestListEmpty.setVisibility(View.VISIBLE)
        } else {
            mBinding.linkRequestListEmpty.setVisibility(View.GONE)
        }
        linkInvitationViewAdapter.resetAll(list)
    }

    /**
     * 连麦邀请列表-接受连麦-更新item选中状态
     */
    fun setSeatInvitationItemStatus(applyItem: UserItem, isAccept: Boolean) {
        mBinding.textLinking.setText("与观众" + applyItem.userName + "连麦中")
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onInviteMicSeatItemChosen(userItem: UserItem, position: Int)
        fun onRequestRefreshing(tagIndex: Int)
        fun onStopLinkingChosen()
    }
}
package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkAudienceBinding
import io.agora.scene.show.databinding.ShowLiveLinkInvitationMessageBinding
import io.agora.scene.show.databinding.ShowLiveLinkInvitationMessageListBinding
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageListBinding
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowRoomRequestStatus
import io.agora.scene.show.service.ShowUser
import io.agora.scene.show.widget.UserItem

class LiveLinkInvitationFragment : BaseFragment() {
    private val mBinding by lazy {
        ShowLiveLinkInvitationMessageListBinding.inflate(
            LayoutInflater.from(
                context
            )
        )}
    private val linkInvitationViewAdapter : LiveLinkInvitationViewAdapter = LiveLinkInvitationViewAdapter()
    private lateinit var mListener : Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkInvitationViewAdapter.setClickListener(object : LiveLinkInvitationViewAdapter.OnClickListener {
            override fun onClick(userItem: ShowUser, position: Int) {
                // 主播发起邀请
                mListener.onInviteMicSeatItemChosen(userItem)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.linkInvitationList.adapter = linkInvitationViewAdapter
        mBinding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            mListener.onRequestRefreshing()
        }
        mBinding.smartRefreshLayout.autoRefresh()
    }

    /**
     * 连麦邀请列表-设置在线用户列表
     */
    fun setSeatInvitationList(list : List<ShowUser>) {
        if (list == null || list.isEmpty()) {
            mBinding.linkRequestListEmpty.setVisibility(View.VISIBLE)
        } else {
            mBinding.linkRequestListEmpty.setVisibility(View.GONE)
        }
        linkInvitationViewAdapter.resetAll(list)
        mBinding.smartRefreshLayout.finishRefresh()
    }

    /**
     * 连麦邀请列表-接受连麦-更新item选中状态
     */
    fun setSeatInvitationItemStatus(user: ShowUser) {
        val itemCount: Int = linkInvitationViewAdapter.getItemCount()
        for (i in 0 until itemCount) {
            var item: ShowUser = linkInvitationViewAdapter.getItem(i)!!
            if (item.userId == user.userId) {
                item = ShowUser(
                    item.userId,
                    item.avatar,
                    item.userName,
                    user.status
                )
                linkInvitationViewAdapter.notifyItemChanged(i)
                break
            }
        }
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onInviteMicSeatItemChosen(userItem: ShowUser)
        fun onRequestRefreshing()
        fun onStopLinkingChosen()
    }
}
package io.agora.scene.show.widget.pk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageListBinding
import io.agora.scene.show.databinding.ShowLivePkRequestMessageBinding
import io.agora.scene.show.databinding.ShowLivePkRequestMessageListBinding
import io.agora.scene.show.widget.UserItem
import io.agora.scene.show.widget.link.LiveLinkRequestViewAdapter

class LivePKRequestMessageFragment : BaseFragment() {
    private lateinit var mBinding : ShowLivePkRequestMessageListBinding
    private lateinit var mListener : Listener
    private val linkPKViewAdapter : LivePKViewAdapter = LivePKViewAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkPKViewAdapter.setClickListener(object: LivePKViewAdapter.OnClickListener{
            override fun onClick(userItem: UserItem, position: Int) {
                mListener.onAcceptMicSeatItemChosen(userItem)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ShowLivePkRequestMessageListBinding.inflate(layoutInflater)
        return mBinding.root
    }

    /**
     * 设置连麦申请列表
     */
    fun setOnlineBoardcasterList(userList : List<UserItem>) {
        if (userList == null || userList.isEmpty()) {
            mBinding.linkRequestListEmpty.setVisibility(View.VISIBLE)
        } else {
            mBinding.linkRequestListEmpty.setVisibility(View.GONE)
        }
        linkPKViewAdapter.resetAll(userList)
    }

    /**
     * pk-更新item选中状态
     */
    fun setPKInvitationItemStatus(userItem: UserItem, isInvited: Boolean) {
        mBinding.textPking.setText("与主播" + userItem + "连麦中")
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAcceptMicSeatItemChosen(userItem: UserItem)
        fun onRequestRefreshing(tagIndex: Int)
    }
}
package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.base.component.BaseFragment
import io.agora.scene.show.databinding.ShowLiveLinkInvitationMessageListBinding
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowUser

class LiveLinkInvitationFragment : BaseFragment() {
    private var mBinding : ShowLiveLinkInvitationMessageListBinding? = null
    private val binding get() = mBinding!!
    private val linkInvitationViewAdapter : LiveLinkInvitationViewAdapter = LiveLinkInvitationViewAdapter()
    private var mListener : Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkInvitationViewAdapter.setClickListener(object : LiveLinkInvitationViewAdapter.OnClickListener {
            override fun onClick(view : View, userItem: ShowUser, position: Int) {
                // Host initiates invitation
                mListener?.onInviteMicSeatItemChosen(view, userItem)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = ShowLiveLinkInvitationMessageListBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.linkInvitationList.adapter = linkInvitationViewAdapter
        binding.smartRefreshLayout.setOnRefreshListener {
            mListener?.onRequestRefreshing()
        }
        binding.smartRefreshLayout.autoRefresh()
    }

    /**
     * Set online user list in linking invitation list
     */
    fun setSeatInvitationList(list : List<ShowUser>) {
        if (mBinding == null) return
        if (list.isEmpty()) {
            binding.linkRequestListEmptyImg.visibility = View.VISIBLE
            binding.linkRequestListEmpty.visibility = View.VISIBLE
        } else {
            binding.linkRequestListEmptyImg.visibility = View.GONE
            binding.linkRequestListEmpty.visibility = View.GONE
        }
        linkInvitationViewAdapter.resetAll(list)
        binding.smartRefreshLayout.finishRefresh()
    }

    /**
     * Update item selection status after accepting linking in linking invitation list
     */
    fun setSeatInvitationItemStatus(user: ShowUser) {
        val itemCount: Int = linkInvitationViewAdapter.itemCount
        for (i in 0 until itemCount) {
            linkInvitationViewAdapter.getItem(i)?.let {
                if (it.userId == user.userId) {
                    linkInvitationViewAdapter.replace(i, it.copy(status = user.status))
                    linkInvitationViewAdapter.notifyItemChanged(i)
                    return
                }
            }
        }
    }

    fun setListener(listener : Listener) {
        mListener = listener
    }

    fun setOnSeatStatus(userName: String, status: Int?) {
        val itemCount: Int = linkInvitationViewAdapter.itemCount
        for (i in 0 until itemCount) {
            linkInvitationViewAdapter.getItem(i)?.let {
                if(status == null && it.status != ShowInteractionStatus.idle){
                    linkInvitationViewAdapter.replace(
                        i, it.copy(status = ShowInteractionStatus.idle)
                    )
                    linkInvitationViewAdapter.notifyItemChanged(i)
                    return
                }
                else if (it.userName == userName && status != null) {
                    linkInvitationViewAdapter.replace(
                        i, it.copy(status = status)
                    )
                    linkInvitationViewAdapter.notifyItemChanged(i)
                    return
                }
            }
        }
    }

    interface Listener {
        fun onInviteMicSeatItemChosen(view: View, userItem: ShowUser)
        fun onRequestRefreshing()
        fun onStopLinkingChosen(view: View)
    }
}
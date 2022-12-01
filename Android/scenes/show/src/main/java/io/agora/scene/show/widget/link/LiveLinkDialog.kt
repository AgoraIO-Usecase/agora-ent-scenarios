package io.agora.scene.show.widget.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.scene.show.databinding.ShowLiveDetailVideoLinkBinding
import io.agora.scene.show.widget.UserItem

class LiveLinkDialog : AppCompatDialogFragment() {
    private val mBinding by lazy { ShowLiveDetailVideoLinkBinding.inflate(LayoutInflater.from(context)) }

    private lateinit var linkDialogListener: OnLinkDialogActionListener;
    private val linkFragment: LiveLinkRequestFragment = LiveLinkRequestFragment()
    private val onlineUserFragment: LiveLinkInvitationFragment = LiveLinkInvitationFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(
            requireDialog().window!!.decorView
        ) { v: View?, insets: WindowInsetsCompat ->
            val inset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mBinding.pager.setPadding(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        mBinding.rBtnRequestMessage.setChecked(true)
        mBinding.pager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        linkFragment.setListener(object: LiveLinkRequestFragment.Listener {
            override fun onAcceptMicSeatItemChosen(userItem: UserItem, position: Int) {
                if (linkDialogListener != null) {
                    linkDialogListener.onAcceptMicSeatApplyChosen(this@LiveLinkDialog, userItem)
                }
            }

            override fun onRequestRefreshing(tagIndex: Int) {
                if (linkDialogListener != null) {
                    linkDialogListener.onRequestMessageRefreshing(this@LiveLinkDialog, tagIndex)
                }
            }

            override fun onStopLinkingChosen() {
                if (linkDialogListener != null) {
                    linkDialogListener.onStopLinkingChosen(this@LiveLinkDialog)
                }
            }
        })

        onlineUserFragment.setListener(object: LiveLinkInvitationFragment.Listener {
            override fun onInviteMicSeatItemChosen(userItem: UserItem, position: Int) {
                if (linkDialogListener != null) {
                    linkDialogListener.onOnlineAudienceRefreshing(this@LiveLinkDialog, position)
                }
            }

            override fun onRequestRefreshing(tagIndex: Int) {
                if (linkDialogListener != null) {
                    linkDialogListener.onOnlineAudienceRefreshing(this@LiveLinkDialog, tagIndex)
                }
            }

            override fun onStopLinkingChosen() {
                if (linkDialogListener != null) {
                    linkDialogListener.onStopLinkingChosen(this@LiveLinkDialog)
                }
            }
        })

        val fragments = arrayOf<Fragment>(linkFragment, onlineUserFragment)
        mBinding.pager.isSaveEnabled = false
        mBinding.pager.adapter =
            object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
                override fun getItemCount(): Int {
                    return fragments.size
                }

                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }
            }
        mBinding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    mBinding.rBtnRequestMessage.setChecked(true)
                } else {
                    mBinding.rBtnOnlineUser.setChecked(true)
                }
            }
        })
        setChosenItemCount(0)
    }

    override fun onStart() {
        super.onStart()
    }

    /**
     * 连麦申请列表-接受连麦
     */
    fun setLinkDialogActionListener(listener : OnLinkDialogActionListener) {
        linkDialogListener = listener
    }

    /**
     * 连麦申请列表-设置当前麦上状态
     */
    fun setOnSeatStatus(userName: String) {
        linkFragment.setOnSeatStatus(userName)
    }

    /**
     * 连麦申请列表-设置连麦申请列表
     */
    fun setSeatApplyList(list : List<UserItem>) {
        linkFragment.setSeatApplyList(list)
    }

    /**
     * 连麦申请列表-更新item选中状态
     */
    fun setSeatApplyItemStatus(applyItem: UserItem, isAccept: Boolean) {
        linkFragment.setSeatApplyItemStatus(applyItem, isAccept)
    }

    /**
     * 连麦邀请列表-设置在线主播列表
     */
    fun setSeatInvitationList(list : List<UserItem>) {
        onlineUserFragment.setSeatInvitationList(list)
    }

    /**
     * 连麦邀请列表-接受连麦-更新item选中状态
     */
    fun setSeatInvitationItemStatus(applyItem: UserItem, isAccept: Boolean) {
        onlineUserFragment.setSeatInvitationItemStatus(applyItem, isAccept)
    }

    private fun setChosenItemCount(count: Int) {
        var count = count
        if (mBinding == null) {
            return
        }
        if (count > 0) {
            mBinding.rBtnRequestMessage.setVisibility(View.VISIBLE)
            if (count > 99) {
                count = 99
            }
            mBinding.rBtnRequestMessage.setText(count.toString())
        } else {
            mBinding.rBtnRequestMessage.setVisibility(View.GONE)
        }
    }
}
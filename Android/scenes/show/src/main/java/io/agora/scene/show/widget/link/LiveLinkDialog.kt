package io.agora.scene.show.widget.link

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkDialogBinding
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.widget.UserItem

class LiveLinkDialog : BottomSheetDialogFragment() {
    private lateinit var mBinding: ShowLiveLinkDialogBinding
    private lateinit var linkDialogListener: OnLinkDialogActionListener;
    private val linkFragment: LiveLinkRequestFragment = LiveLinkRequestFragment()
    private val onlineUserFragment: LiveLinkInvitationFragment = LiveLinkInvitationFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ShowLiveLinkDialogBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置背景透明
        WindowCompat.setDecorFitsSystemWindows(requireDialog().window!!, false)
        requireDialog().setOnShowListener { dialog: DialogInterface? ->
            (view.parent as ViewGroup).setBackgroundColor(
                Color.TRANSPARENT
            )
        }
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
            override fun onAcceptMicSeatItemChosen(userItem: ShowMicSeatApply, position: Int) {
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
        mBinding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    mBinding.rBtnRequestMessage.setChecked(true)
                } else {
                    mBinding.rBtnOnlineUser.setChecked(true)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mBinding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i === R.id.rBtnRequestMessage) {
                mBinding.pager.currentItem = 0
            } else if (i === R.id.rBtnOnlineUser) {
                mBinding.pager.currentItem = 1
            }
        }
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
    fun setSeatApplyList(list : List<ShowMicSeatApply>) {
        linkFragment.setSeatApplyList(list)
    }

    /**
     * 连麦申请列表-更新item选中状态
     */
    fun setSeatApplyItemStatus(applyItem: ShowMicSeatApply, isAccept: Boolean) {
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
}
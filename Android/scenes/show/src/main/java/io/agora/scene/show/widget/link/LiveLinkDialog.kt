package io.agora.scene.show.widget.link

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkDialogBinding
import io.agora.scene.show.service.*

class LiveLinkDialog : BottomSheetDialogFragment() {
    private var mBinding : ShowLiveLinkDialogBinding? = null
    private val binding get() = mBinding!!
    private var linkDialogListener: OnLinkDialogActionListener? = null
    private val linkFragment: LiveLinkRequestFragment = LiveLinkRequestFragment()
    private val onlineUserFragment: LiveLinkInvitationFragment = LiveLinkInvitationFragment()
    private val audienceFragment: LiveLinkAudienceFragment = LiveLinkAudienceFragment()
    private var isRoomOwner: Boolean = true

    fun setIsRoomOwner(value: Boolean) {
        isRoomOwner = value
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = ShowLiveLinkDialogBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置背景透明
        WindowCompat.setDecorFitsSystemWindows(requireDialog().window!!, false)
        requireDialog().setOnShowListener {
            (view.parent as ViewGroup).setBackgroundColor(
                Color.TRANSPARENT
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(
            requireDialog().window!!.decorView
        ) { _: View?, insets: WindowInsetsCompat ->
            val inset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.pager.setPadding(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.rBtnRequestMessage.isChecked = true
        binding.pager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        if (isRoomOwner) {
            linkFragment.setListener(object: LiveLinkRequestFragment.Listener {
                override fun onAcceptMicSeatItemChosen(seatApply: ShowMicSeatApply, position: Int) {
                    linkDialogListener?.onAcceptMicSeatApplyChosen(this@LiveLinkDialog, seatApply)
                }

                override fun onRequestRefreshing() {
                    linkDialogListener?.onRequestMessageRefreshing(this@LiveLinkDialog)
                }

                override fun onStopLinkingChosen() {
                    linkDialogListener?.onStopLinkingChosen(this@LiveLinkDialog)
                }
            })

            onlineUserFragment.setListener(object: LiveLinkInvitationFragment.Listener {
                override fun onInviteMicSeatItemChosen(userItem: ShowUser) {
                    linkDialogListener?.onOnlineAudienceInvitation(this@LiveLinkDialog, userItem)
                }

                override fun onRequestRefreshing() {
                    linkDialogListener?.onOnlineAudienceRefreshing(this@LiveLinkDialog)
                }

                override fun onStopLinkingChosen() {
                    linkDialogListener?.onStopLinkingChosen(this@LiveLinkDialog)
                }
            })

            val fragments = arrayOf<Fragment>(linkFragment, onlineUserFragment)
            binding.pager.isSaveEnabled = false
            binding.pager.adapter =
                object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
                    override fun getItemCount(): Int {
                        return fragments.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return fragments[position]
                    }
                }
            binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) {
                        binding.rBtnRequestMessage.isChecked = true
                        binding.rBtnRequestMessage.setTypeface(null, Typeface.BOLD)
                        binding.rBtnOnlineUser.setTypeface(null, Typeface.NORMAL)
                    } else {
                        binding.rBtnOnlineUser.isChecked = true
                        binding.rBtnOnlineUser.setTypeface(null, Typeface.BOLD)
                        binding.rBtnRequestMessage.setTypeface(null, Typeface.NORMAL)
                    }
                }
            })
        } else {
            binding.radioGroup.visibility = View.INVISIBLE
            binding.rBtnRequestText.isVisible = true
            audienceFragment.setListener(object: LiveLinkAudienceFragment.Listener {
                override fun onRequestRefreshing() {
                    linkDialogListener?.onRequestMessageRefreshing(this@LiveLinkDialog)
                }

                override fun onStopLinkingChosen() {
                    linkDialogListener?.onStopLinkingChosen(this@LiveLinkDialog)
                }

                override fun onStopApplyingChosen() {
                    linkDialogListener?.onStopApplyingChosen(this@LiveLinkDialog)
                }
            })

            binding.pager.isSaveEnabled = false
            binding.pager.adapter =
                object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
                    override fun getItemCount(): Int {
                        return 1
                    }

                    override fun createFragment(position: Int): Fragment {
                        return audienceFragment
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.radioGroup.setOnCheckedChangeListener { _, i ->
            if (i === R.id.rBtnRequestMessage) {
                binding.pager.currentItem = 0
            } else if (i === R.id.rBtnOnlineUser) {
                binding.pager.currentItem = 1
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
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
    fun setOnSeatStatus(userName: String, status: Int?) {
        if (isRoomOwner) {
            linkFragment.setOnSeatStatus(userName, status)
        } else {
            audienceFragment.setOnSeatStatus(userName, status)
        }
    }

    /**
     * 连麦申请列表-设置连麦申请列表
     */
    fun setSeatApplyList(interactionInfo: ShowInteractionInfo?, list : List<ShowMicSeatApply>) {
        if (isRoomOwner) {
            linkFragment.setSeatApplyList(interactionInfo, list)
        } else {
            val waitList = list.filter { it.status == ShowRoomRequestStatus.waitting.value }
            audienceFragment.setSeatApplyList(interactionInfo, waitList)
        }
    }

    fun setOnApplySuccess() {
        audienceFragment.setOnApplySuccess()
    }

    /**
     * 连麦申请列表-更新item选中状态
     */
    fun setSeatApplyItemStatus(applyItem: ShowMicSeatApply) {
        linkFragment.setSeatApplyItemStatus(applyItem)
    }

    /**
     * 连麦邀请列表-设置在线主播列表
     */
    fun setSeatInvitationList(userList : List<ShowUser>) {
        onlineUserFragment.setSeatInvitationList(userList)
    }

    /**
     * 连麦邀请列表-接受连麦-更新item选中状态
     */
    fun setSeatInvitationItemStatus(user: ShowUser) {
        onlineUserFragment.setSeatInvitationItemStatus(user)
    }
}
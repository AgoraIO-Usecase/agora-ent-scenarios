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
import io.agora.scene.show.service.ShowInteractionInfo
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowUser

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

        // Set background transparent
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
                override fun onAcceptMicSeatItemChosen(view: View, seatApply: ShowMicSeatApply, position: Int) {
                    linkDialogListener?.onAcceptMicSeatApplyChosen(this@LiveLinkDialog, view, seatApply)
                }

                override fun onRequestRefreshing() {
                    linkDialogListener?.onRequestMessageRefreshing(this@LiveLinkDialog)
                }

                override fun onStopLinkingChosen(view: View) {
                    linkDialogListener?.onStopLinkingChosen(this@LiveLinkDialog, view)
                }
            })

            onlineUserFragment.setListener(object: LiveLinkInvitationFragment.Listener {
                override fun onInviteMicSeatItemChosen(view: View, userItem: ShowUser) {
                    linkDialogListener?.onOnlineAudienceInvitation(this@LiveLinkDialog, view, userItem)
                }

                override fun onRequestRefreshing() {
                    linkDialogListener?.onOnlineAudienceRefreshing(this@LiveLinkDialog)
                }

                override fun onStopLinkingChosen(view: View) {
                    linkDialogListener?.onStopLinkingChosen(this@LiveLinkDialog, view)
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

                override fun onStopLinkingChosen(view: View) {
                    linkDialogListener?.onStopLinkingChosen(this@LiveLinkDialog, view)
                }

                override fun onStopApplyingChosen(view: View) {
                    linkDialogListener?.onStopApplyingChosen(this@LiveLinkDialog, view, view.tag as? ShowMicSeatApply)
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
     * Accept linking request in linking request list
     */
    fun setLinkDialogActionListener(listener : OnLinkDialogActionListener) {
        linkDialogListener = listener
    }

    /**
     * Set current mic seat status in linking request list
     */
    fun setOnSeatStatus(userName: String, status: Int?) {
        if (isRoomOwner) {
            linkFragment.setOnSeatStatus(userName, status)
            onlineUserFragment.setOnSeatStatus(userName, status)
        } else {
            audienceFragment.setOnSeatStatus(userName, status)
        }
    }

    /**
     * Set linking request list in linking request list
     */
    fun setSeatApplyList(interactionInfo: ShowInteractionInfo?, list : List<ShowMicSeatApply>) {
        if (isRoomOwner) {
            linkFragment.setSeatApplyList(interactionInfo, list)
        } else {
            audienceFragment.setSeatApplyList(interactionInfo, list)
        }
    }

    fun setOnApplySuccess(apply: ShowMicSeatApply) {
        audienceFragment.setOnApplySuccess(apply)
    }

    /**
     * Update item selection status in linking request list
     */
    fun setSeatApplyItemStatus(applyItem: ShowMicSeatApply) {
        linkFragment.setSeatApplyItemStatus(applyItem)
    }

    /**
     * Set online host list in linking invitation list
     */
    fun setSeatInvitationList(userList : List<ShowUser>) {
        onlineUserFragment.setSeatInvitationList(userList)
    }

    /**
     * Update item selection status after accepting linking in linking invitation list
     */
    fun setSeatInvitationItemStatus(user: ShowUser) {
        onlineUserFragment.setSeatInvitationItemStatus(user)
    }
}
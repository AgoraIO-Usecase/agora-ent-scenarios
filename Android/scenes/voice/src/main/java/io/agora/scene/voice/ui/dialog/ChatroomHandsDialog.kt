package io.agora.scene.voice.ui.dialog

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceRoomHandLayoutBinding
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.ui.fragment.ChatroomInviteHandsFragment
import io.agora.scene.voice.ui.fragment.ChatroomRaisedHandsFragment
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import io.agora.voice.common.utils.DeviceTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ResourcesTools

class ChatroomHandsDialog : BaseSheetDialog<VoiceRoomHandLayoutBinding>() {
    private val titles = intArrayOf(R.string.voice_room_raised_hands_title, R.string.voice_room_invite_hands_title)
    private val fragments: MutableList<Fragment> = mutableListOf()
    private var title: MaterialTextView? = null
    private var index = 0
    private var mCount = 0
    private var roomId: String? = null
    private val bundle = Bundle()
    private var raisedHandsFragment: ChatroomRaisedHandsFragment? = null
    private var inviteHandsFragment: ChatroomInviteHandsFragment? = null
    private val TAG = ChatroomHandsDialog::class.java.simpleName

    private var onFragmentListener: OnFragmentListener? = null

    // 房主准备邀请的麦位
    private var inviteMicIndex:Int = -1

    fun setInviteMicIndex(inviteMicIndex: Int){
        this.inviteMicIndex = inviteMicIndex
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceRoomHandLayoutBinding {
        return VoiceRoomHandLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getString("roomId")
        binding?.root?.let {
            setOnApplyWindowInsets(it)
        }
        initView()
        initListener()
    }

    fun initView() {
        fragments.add(ChatroomRaisedHandsFragment())
        fragments.add(ChatroomInviteHandsFragment())
        setupWithViewPager()
    }

    fun initListener() {
        binding?.tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.customView?.let {
                    "onTabSelected：$mCount".logD(TAG)
                    index = tab.position
                    title = it.findViewById(R.id.mtTabText)
                    val tag_line = it.findViewById<ShapeableImageView>(R.id.tab_bg)
                    val layoutParams = title?.layoutParams.apply {

                    }
                    layoutParams?.height = DeviceTools.dp2px(requireContext(), 26f)
                    title?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        gravity = Gravity.CENTER
                        setTypeface(null, Typeface.BOLD)
                        val content =
                            getString(titles[index]) + getString(
                                R.string.voice_room_tab_layout_count,
                                mCount.toString()
                            )
                        text = content
                        setTextColor(ResourcesTools.getColor(resources, R.color.voice_dark_grey_color_040925))
                    }

                    tag_line.setBackgroundColor(ResourcesTools.getColor(resources, R.color.voice_color_156ef3))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.customView != null) {
                    "onTabUnselected：$mCount".logD(TAG)
                    val title = tab.customView?.findViewById<MaterialTextView>(R.id.mtTabText)
                    val tag_line = tab.customView?.findViewById<ShapeableImageView>(R.id.tab_bg)
                    title?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        setText(titles[tab.position])
                        setTypeface(null, Typeface.NORMAL)
                        setTextColor(ResourcesTools.getColor(resources, R.color.voice_color_979cbb))
                    }
                    tag_line?.setBackgroundColor(ResourcesTools.getColor(resources, R.color.voice_white))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                "onTabReselected：".logD(TAG)
                title = tab.customView?.findViewById(R.id.mtTabText)
                val tagLine = tab.customView?.findViewById<ShapeableImageView>(R.id.tab_bg)
                title?.apply {
                    setText(titles[tab.position])
                    setTextColor(ResourcesTools.getColor(resources, R.color.voice_dark_grey_color_040925))
                    setTypeface(null, Typeface.BOLD)
                }
                tagLine?.setBackgroundColor(ResourcesTools.getColor(resources,R.color.voice_color_156ef3))
            }
        })
        binding?.vpFragment?.currentItem = 0
        binding?.tabLayout?.selectTab(binding?.tabLayout?.getTabAt(0))
    }

    private fun setupWithViewPager() {
        binding?.vpFragment?.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        // set adapter
        binding?.vpFragment?.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                if (fragments[position] is ChatroomRaisedHandsFragment) {
                    raisedHandsFragment = fragments[position] as ChatroomRaisedHandsFragment?
                    raisedHandsFragment?.setFragmentListener(object : OnFragmentListener {
                        override fun getItemCount(count: Int) {
                            mCount = count
                            activity?.let {
                                val content = requireActivity().getString(titles[index]) + getString(
                                    R.string.voice_room_tab_layout_count,
                                    mCount.toString()
                                )
                                "getItemCount content1: $content".logD(TAG)
                                title?.text = content
                            }
                        }

                        override fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {
                            onFragmentListener?.onAcceptMicSeatApply(voiceMicInfoModel)
                            if (mCount > 0) {
                                mCount -= 1
                                activity?.let {
                                    val content = requireActivity().getString(titles[index]) + getString(
                                        R.string.voice_room_tab_layout_count,
                                        mCount.toString()
                                    )
                                    "getItemCount content1: $content".logD(TAG)
                                    title?.text = content
                                }
                            }
                        }

                    })
                } else if (fragments[position] is ChatroomInviteHandsFragment) {
                    inviteHandsFragment = fragments[position] as ChatroomInviteHandsFragment?
                    inviteHandsFragment?.setInviteMicIndex(inviteMicIndex)
                    inviteHandsFragment?.setFragmentListener(object : OnFragmentListener {
                        override fun getItemCount(count: Int) {
                            mCount = count
                            if (activity != null) {
                                val content = requireActivity().resources.getString(titles[index]) + getString(
                                    R.string.voice_room_tab_layout_count,
                                    mCount.toString()
                                )
                                "getItemCount content2: $content".logD(TAG)
                                title?.text = content
                            }
                        }
                    })
                }
                bundle.putString("roomId", roomId)
                fragments[position].arguments = bundle
                return fragments[position]
            }

            override fun getItemCount(): Int {
                return titles.size
            }
        }


        binding?.apply {
            // set TabLayoutMediator
            val mediator = TabLayoutMediator(tabLayout, vpFragment) { tab, position ->
                tab.setCustomView(R.layout.voice_room_hands_tab_item)
                title = tab.customView?.findViewById(R.id.mtTabText)
                title?.setText(titles[position])
            }
            // setup with viewpager2
            mediator.attach()
        }

    }

    fun update(index: Int) {
        when (index) {
            0 -> raisedHandsFragment?.reset()
            1 -> inviteHandsFragment?.reset()
        }
    }

    fun check(map: Map<Int, String>) {
        inviteHandsFragment?.micChanged(map)
    }

    fun setFragmentListener(listener: OnFragmentListener?) {
        this.onFragmentListener = listener
    }

    interface OnFragmentListener {
        fun getItemCount(count: Int) {}

        // 同意上麦
        fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {}
    }

    companion object {
        @JvmStatic
        val newInstance: ChatroomHandsDialog
            get() = ChatroomHandsDialog()
    }
}
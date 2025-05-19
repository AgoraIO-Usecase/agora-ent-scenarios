package io.agora.scene.voice.spatial.ui.dialog

import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.databinding.VoiceSpatialRoomHandLayoutBinding
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.ui.fragment.ChatroomInviteHandsFragment
import io.agora.scene.voice.spatial.ui.fragment.ChatroomRaisedHandsFragment

class ChatroomHandsDialog constructor() : BaseBottomSheetDialogFragment<VoiceSpatialRoomHandLayoutBinding>() {
    private val titles =
        intArrayOf(R.string.voice_spatial_room_raised_hands_title, R.string.voice_spatial_room_invite_hands_title)
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = activity ?: return
        if (activity is DialogInterface.OnDismissListener) {
            activity.onDismiss(dialog)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getString("roomId")
        initView()
        initListener()
    }

    fun initView() {
        fragments.add(ChatroomRaisedHandsFragment())
        fragments.add(ChatroomInviteHandsFragment())
        setupWithViewPager()
    }

    fun initListener() {
        mBinding?.tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.customView?.let {
                    VoiceSpatialLogger.d(TAG, "onTabSelected：$mCount")
                    index = tab.position
                    title = it.findViewById(R.id.mtTabText)
                    val tag_line = it.findViewById<ShapeableImageView>(R.id.tab_bg)
                    val layoutParams = title?.layoutParams.apply {

                    }
                    layoutParams?.height = 26.dp.toInt()
                    title?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        gravity = Gravity.CENTER
                        setTypeface(null, Typeface.BOLD)
                        val content =
                            getString(titles[index]) + getString(
                                R.string.voice_spatial_room_tab_layout_count,
                                mCount.toString()
                            )
                        text = content
                        setTextColor(ResourcesCompat.getColor(resources,io.agora.scene.widget.R.color.def_text_color_040, null))
                    }

                    tag_line.setBackgroundColor(ResourcesCompat.getColor(resources, io.agora.scene.widget.R.color.blue_15,
                        null))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.customView != null) {
                    VoiceSpatialLogger.d(TAG, "onTabUnselected：$mCount")
                    val title = tab.customView?.findViewById<MaterialTextView>(R.id.mtTabText)
                    val tag_line = tab.customView?.findViewById<ShapeableImageView>(R.id.tab_bg)
                    title?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        setText(titles[tab.position])
                        setTypeface(null, Typeface.NORMAL)
                        setTextColor(ResourcesCompat.getColor(resources, io.agora.scene.widget.R.color.def_text_grey_979, null))
                    }
                    tag_line?.setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                VoiceSpatialLogger.d(TAG, "onTabReselected：")
                title = tab.customView?.findViewById(R.id.mtTabText)
                val tagLine = tab.customView?.findViewById<ShapeableImageView>(R.id.tab_bg)
                title?.apply {
                    setText(titles[tab.position])
                    setTextColor(ResourcesCompat.getColor(resources, io.agora.scene.widget.R.color.def_text_color_040, null))
                    setTypeface(null, Typeface.BOLD)
                }
                tagLine?.setBackgroundColor(ResourcesCompat.getColor(resources, io.agora.scene.widget.R.color.blue_15, null))
            }
        })
        mBinding?.vpFragment?.currentItem = 0
        mBinding?.tabLayout?.selectTab(mBinding?.tabLayout?.getTabAt(0))
    }

    private fun setupWithViewPager() {
        mBinding?.vpFragment?.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        // set adapter
        mBinding?.vpFragment?.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                if (fragments[position] is ChatroomRaisedHandsFragment) {
                    raisedHandsFragment = fragments[position] as ChatroomRaisedHandsFragment?
                    raisedHandsFragment?.setFragmentListener(object : OnFragmentListener {
                        override fun getItemCount(count: Int) {
                            mCount = count
                            activity?.let {
                                val content = requireActivity().getString(titles[index]) + getString(
                                    R.string.voice_spatial_room_tab_layout_count,
                                    mCount.toString()
                                )
                                VoiceSpatialLogger.d(TAG, "getItemCount content1: $content")
                                title?.text = content
                            }
                        }

                        override fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {
                            onFragmentListener?.onAcceptMicSeatApply(voiceMicInfoModel)
                            if (mCount > 0) {
                                mCount -= 1
                                activity?.let {
                                    val content = requireActivity().getString(titles[index]) + getString(
                                        R.string.voice_spatial_room_tab_layout_count,
                                        mCount.toString()
                                    )
                                    VoiceSpatialLogger.d(TAG, "getItemCount content1: $content")
                                    title?.text = content
                                }
                            }
                        }

                    })
                } else if (fragments[position] is ChatroomInviteHandsFragment) {
                    inviteHandsFragment = fragments[position] as ChatroomInviteHandsFragment?
                    inviteHandsFragment?.setIndex(micIndex)
                    inviteHandsFragment?.setFragmentListener(object : OnFragmentListener {
                        override fun getItemCount(count: Int) {
                            mCount = count
                            if (activity != null) {
                                val content = requireActivity().resources.getString(titles[index]) + getString(
                                    R.string.voice_spatial_room_tab_layout_count,
                                    mCount.toString()
                                )
                                VoiceSpatialLogger.d(TAG, "getItemCount content2: $content")
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


        mBinding?.apply {
            // set TabLayoutMediator
            val mediator = TabLayoutMediator(tabLayout, vpFragment) { tab, position ->
                tab.setCustomView(R.layout.voice_spatial_room_hands_tab_item)
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

    private var micIndex = 0
    fun setMicIndex(index: Int) {
        micIndex = index
    }

    interface OnFragmentListener {
        fun getItemCount(count: Int) {}

        // Agree to join the microphone
        fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {}
    }
}
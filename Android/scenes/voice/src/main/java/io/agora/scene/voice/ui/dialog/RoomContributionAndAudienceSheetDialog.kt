package io.agora.scene.voice.ui.dialog

import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.voice.model.RoomKitBean
import io.agora.scene.voice.ui.fragment.RoomContributionRankingFragment
import io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog
import io.agora.voice.common.utils.ResourcesTools
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogContributionAndAudienceBinding
import io.agora.scene.voice.ui.fragment.RoomAudienceListFragment

class RoomContributionAndAudienceSheetDialog constructor() :
    BaseFixedHeightSheetDialog<VoiceDialogContributionAndAudienceBinding>() {

    companion object {
        const val KEY_ROOM_KIT_BEAN = "room_kit_bean"
        const val KEY_CURRENT_ITEM = "current_Item"
    }

    private val roomKitBean: RoomKitBean by lazy {
        arguments?.getSerializable(KEY_ROOM_KIT_BEAN) as RoomKitBean
    }

    private val currentItem: Int by lazy {
        arguments?.getInt(KEY_CURRENT_ITEM, 0) ?: 0
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogContributionAndAudienceBinding {
        return VoiceDialogContributionAndAudienceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragmentAdapter()
    }

    private fun initFragmentAdapter() {
        activity?.let { fragmentActivity->
            val adapter = RoomRankFragmentAdapter(fragmentActivity, roomKitBean)
            binding?.apply {
                setOnApplyWindowInsets(root)
                vpRankLayout.adapter = adapter
                val tabMediator = TabLayoutMediator(tabRankLayout, vpRankLayout) { tab, position ->
                    val customView =
                        LayoutInflater.from(root.context).inflate(R.layout.voice_view_room_rank_tab_item, tab.view, false)
                    val tabText = customView.findViewById<TextView>(R.id.mtTabText)
                    tab.customView = customView
                    if (position == RoomRankFragmentAdapter.PAGE_INDEX0) {
                        tabText.text = getString(R.string.voice_room_contribution_ranking)
                        onTabLayoutSelected(tab)
                    } else {
                        tabText.text = getString(R.string.voice_room_audience_list)
                        onTabLayoutUnselected(tab)
                    }

                }
                tabMediator.attach()
                tabRankLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        onTabLayoutSelected(tab)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        onTabLayoutUnselected(tab)
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }
                })
                vpRankLayout.setCurrentItem(currentItem, false)
            }
        }

    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.mtTabText)
            tabText.setTextColor(ResourcesTools.getColor(resources, R.color.voice_dark_grey_color_040925))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            val tabTip = it.findViewById<View>(R.id.vTabTip)
            tabTip.visibility = View.VISIBLE
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.mtTabText)
            tabText.setTextColor(ResourcesTools.getColor(resources, R.color.voice_dark_grey_color_6c7192))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            val tabTip = it.findViewById<View>(R.id.vTabTip)
            tabTip.visibility = View.GONE
        }
    }

    class RoomRankFragmentAdapter constructor(
        fragmentActivity: FragmentActivity,
        roomKitBean: RoomKitBean,
    ) : FragmentStateAdapter(fragmentActivity) {

        companion object {
            const val PAGE_INDEX0 = 0
            const val PAGE_INDEX1 = 1
        }

        private val fragments: SparseArray<Fragment> = SparseArray()

        init {
            with(fragments) {
                put(PAGE_INDEX0, RoomContributionRankingFragment.getInstance(roomKitBean))
                put(PAGE_INDEX1, RoomAudienceListFragment.getInstance(roomKitBean))
            }
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        override fun getItemCount(): Int {
            return fragments.size()
        }
    }
}
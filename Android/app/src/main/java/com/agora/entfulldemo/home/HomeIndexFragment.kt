package com.agora.entfulldemo.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentHomeIndexBinding
import com.agora.entfulldemo.home.constructor.HomeScenesType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.*
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingFragment

class HomeIndexFragment : BaseViewBindingFragment<AppFragmentHomeIndexBinding>() {

    private val mTabs by lazy {
        mutableListOf(
//            HomeScenesType.Full,
//            HomeScenesType.KTV,
//            HomeScenesType.Voice,
//            HomeScenesType.Live,
            HomeScenesType.Game
        )
    }

    private val mSingleScene: Boolean
        get() = mTabs.size == 1

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentHomeIndexBinding {
        return AppFragmentHomeIndexBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPaddingRelative(inset.left, inset.top, inset.right, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun initView() {
        val act = activity ?: return
        binding.tvDevEnv.isVisible = !ServerConfig.envRelease

        // 创建 Adapter
        val adapter = HomePagerAdapter(requireActivity(), mTabs)
        // 设置 Adapter 给 ViewPager2
        binding.viewPagerLayout.adapter = adapter
        // 将 TabLayout 与 ViewPager2 关联
        TabLayoutMediator(binding.tabLayout, binding.viewPagerLayout) { tab, position ->
            tab.setCustomView(R.layout.app_home_index_tab_item)
            val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return@TabLayoutMediator

            when (mTabs[position]) {
                HomeScenesType.KTV -> tvTabTitle.text = act.getString(R.string.app_home_scene_ktv)
                HomeScenesType.Voice -> tvTabTitle.text = act.getString(R.string.app_home_scene_voice)
                HomeScenesType.Live -> tvTabTitle.text = if (mSingleScene) act.getString(R.string.app_home_scene_live_merge) else act.getString(R.string.app_home_scene_live)
                HomeScenesType.Game -> tvTabTitle.text = act.getString(R.string.app_home_scene_game)
                else -> tvTabTitle.text = act.getString(R.string.app_home_full_scene)
            }
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d("zhangw","onTabSelected ${tab.text}")
                val customView = tab.customView
                if (customView == null) {
                    tab.setCustomView(R.layout.app_home_index_tab_item)
                }
                val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvTabTitle.setTextAppearance(R.style.app_TabLayoutTextSelected)
                } else {
                    tvTabTitle.setTextAppearance(act, R.style.app_TabLayoutTextSelected)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Log.d("zhangw","onTabUnselected ${tab.text}")
                val customView = tab.customView
                if (customView == null) {
                    tab.setCustomView(R.layout.app_home_index_tab_item)
                }
                val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvTabTitle.setTextAppearance(R.style.app_TabLayoutTextUnSelected)
                } else {
                    tvTabTitle.setTextAppearance(act, R.style.app_TabLayoutTextUnSelected)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Log.d("zhangw","onTabReselected ${tab.text}")

                val customView = tab.customView
                if (customView == null) {
                    tab.setCustomView(R.layout.app_home_index_tab_item)
                }
                val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvTabTitle.setTextAppearance(R.style.app_TabLayoutTextSelected)
                } else {
                    tvTabTitle.setTextAppearance(act, R.style.app_TabLayoutTextSelected)
                }
            }

        })
        binding.viewPagerLayout.currentItem = 0
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))

        val layoutParams = binding.tabLayout.layoutParams
        if (mSingleScene) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.tabLayout.tabGravity = GRAVITY_CENTER
        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.tabLayout.tabGravity = GRAVITY_START
        }
        binding.tabLayout.layoutParams = layoutParams
    }
}

class HomePagerAdapter constructor(activity: FragmentActivity, private val mTabs: List<HomeScenesType>) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return mTabs.size
    }

    override fun createFragment(position: Int): Fragment {
        val scenesType = mTabs[position]
        return HomeIndexSubFragment.newInstance(scenesType)
    }

}
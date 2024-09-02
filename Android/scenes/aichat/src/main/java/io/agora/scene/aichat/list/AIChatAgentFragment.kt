package io.agora.scene.aichat.list

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.aichat.R
import io.agora.scene.aichat.create.AIChatCreateAgentDialog
import io.agora.scene.aichat.databinding.AichatAgentFragmentBinding
import io.agora.scene.aichat.ext.addAgentTabSelectedListener
import io.agora.scene.base.component.BaseViewBindingFragment

/**
 * 智能体页面
 */
class AIChatAgentFragment : BaseViewBindingFragment<AichatAgentFragmentBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatAgentFragmentBinding {
        return AichatAgentFragmentBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding.viewPagerLayout.apply {
            isUserInputEnabled = false
            offscreenPageLimit = 2
            adapter = object : FragmentStateAdapter(this@AIChatAgentFragment) {
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> AIChatAgentListFragment.newInstance(true)
                        1 -> AIChatAgentListFragment.newInstance(false)
                        else -> AIChatAgentListFragment.newInstance(true)
                    }
                }

                override fun getItemCount() = 2
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPagerLayout) { tab, position ->
            tab.setCustomView(R.layout.aichat_agent_tabitem)
            val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return@TabLayoutMediator
            val viewIndicator: View = tab.customView?.findViewById(R.id.viewIndicator) ?: return@TabLayoutMediator

            Log.d("tab", "position: $position")
            when (position) {
                0 -> {
                    tvTabTitle.text = getString(R.string.aichat_public_agent)
                    TextViewCompat.setTextAppearance(tvTabTitle, R.style.aichat_TabLayoutTextSelected)
                    viewIndicator.setBackgroundResource(R.drawable.aichat_tablayout_indicator)
                }

                1 -> {
                    tvTabTitle.text = getString(R.string.aichat_private_agent)
                    TextViewCompat.setTextAppearance(tvTabTitle, R.style.aichat_TabLayoutTextUnSelected)
                    viewIndicator.setBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            android.R.color.transparent,
                            null
                        )
                    )
                }
            }
        }.attach()

        binding.tabLayout.addAgentTabSelectedListener()
    }

    override fun initListener() {
        super.initListener()
        binding.btnCreateAgent.setOnClickListener {
            AIChatCreateAgentDialog(2).show(parentFragmentManager, "AIChatCreateAgentDialog")
        }
    }
}
package io.agora.scene.aichat.list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.aichat.R
import io.agora.scene.aichat.create.AiChatAgentCreateActivity
import io.agora.scene.aichat.create.AiChatAgentCreateFragment
import io.agora.scene.aichat.databinding.AichatFragmentAgentBinding
import io.agora.scene.aichat.ext.addAgentTabSelectedListener
import io.agora.scene.aichat.list.logic.AIAgentViewModel
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast

/**
 * 智能体页面
 */
class AIChatAgentFragment : BaseViewBindingFragment<AichatFragmentAgentBinding>() {

    //viewModel
    private val mAIAgentViewModel: AIAgentViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentAgentBinding {
        return AichatFragmentAgentBinding.inflate(inflater)
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
            tab.setCustomView(R.layout.aichat_tabitem_agent)
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

        mAIAgentViewModel.privateAIAgentLiveData.observe(this) { aiAgentList ->
            if (isClickCreateAgent) {
                isClickCreateAgent = false
                if (aiAgentList.size >= AiChatAgentCreateFragment.maxCreateCount) {
                    CustomToast.show(
                        getString(R.string.aichat_create_agent_limit, AiChatAgentCreateFragment.maxCreateCount)
                    )
                    return@observe
                }
                activity?.let {
                    AiChatAgentCreateActivity.start(it)
                }
            }
        }

        mAIAgentViewModel.loadingChange.showDialog.observe(viewLifecycleOwner) {
            showLoadingView()
        }
        mAIAgentViewModel.loadingChange.dismissDialog.observe(viewLifecycleOwner) {
            hideLoadingView()
        }
    }

    private var isClickCreateAgent = false

    override fun initListener() {
        super.initListener()
        binding.cvAichatCreate.setOnClickListener {
            isClickCreateAgent = true
            mAIAgentViewModel.getUserAgent(true)
        }
    }
}
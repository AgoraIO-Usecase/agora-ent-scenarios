package io.agora.scene.aichat.list

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.aichat.R
import io.agora.scene.aichat.create.AiChatGroupCreateActivity
import io.agora.scene.aichat.databinding.AichatFragmentConversationBinding
import io.agora.scene.aichat.ext.addAgentTabSelectedListener
import io.agora.scene.base.component.BaseViewBindingFragment

/**
 * 会话页面
 */
class AIChatConversationFragment : BaseViewBindingFragment<AichatFragmentConversationBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentConversationBinding {
        return AichatFragmentConversationBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding.viewPagerLayout.apply {
            isUserInputEnabled = false
            offscreenPageLimit = 1
            adapter = object : FragmentStateAdapter(this@AIChatConversationFragment) {
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> AIChatConversationListFragment.newInstance()
                        else -> AIChatConversationListFragment.newInstance()                    }
                }

                override fun getItemCount() = 1
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPagerLayout) { tab, position ->
            tab.setCustomView(R.layout.aichat_tabitem_agent)
            val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return@TabLayoutMediator
            val viewIndicator: View = tab.customView?.findViewById(R.id.viewIndicator) ?: return@TabLayoutMediator

            Log.d("tag", "position: $position")
            when (position) {
                0 -> {
                    tvTabTitle.text = getString(R.string.aichat_conversation)
                    viewIndicator.setBackgroundResource(R.drawable.aichat_tablayout_indicator)
                }
            }
        }.attach()

        binding.tabLayout.addAgentTabSelectedListener()
    }

    override fun initListener() {
        super.initListener()
        binding.cvAichatCreate.setOnClickListener {
//            AIChatCreateGroupDialog().show(parentFragmentManager, "AIChatCreateGroupDialog")
            startActivity(Intent(requireContext(), AiChatGroupCreateActivity::class.java))
        }
    }
}
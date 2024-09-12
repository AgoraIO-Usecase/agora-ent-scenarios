package io.agora.scene.aichat.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.AIChatHelper
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatListActivityBinding
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.list.logic.AIUserViewModel
import io.agora.scene.base.component.BaseViewBindingActivity

/**
 * Ai chat list activity
 *
 * @constructor Create empty Ai chat list activity
 */
class AIChatListActivity : BaseViewBindingActivity<AichatListActivityBinding>() {

    //viewModel
    private val aiUserViewModel: AIUserViewModel by viewModels()

    private lateinit var mFragmentAdapter: FragmentStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v?.setPaddingRelative(inset.left, inset.top, inset.right, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): AichatListActivityBinding {
        return AichatListActivityBinding.inflate(inflater)
    }

    override fun init() {
        super.init()
        AIChatHelper.getInstance().init(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mFragmentAdapter = object : FragmentStateAdapter(this@AIChatListActivity) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> AIChatAgentFragment()
                    1 -> AIChatConversationFragment()
                    else -> AIChatAgentFragment()
                }
            }

            override fun getItemCount() = 2
        }
        binding.mainViewpager.apply {
            isUserInputEnabled = false
            offscreenPageLimit = 2
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> binding.mainBottom.setSelectedItemId(R.id.navigation_agent);
                        1 -> binding.mainBottom.setSelectedItemId(R.id.navigation_conversation);
                    }
                }
            })
        }
        binding.mainBottom.apply {
            itemIconTintList = null
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_agent -> {
                        binding.mainViewpager.setCurrentItem(0, false)
                    }

                    R.id.navigation_conversation -> {
                        binding.mainViewpager.setCurrentItem(1, false)
                    }
                }
                true
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.titleView.setLeftClick {
            finish()
        }

        // 所有会话
//        aiChatEventViewModel?.unreadMessageLiveData?.observe(this) { newMessage ->
//            val menuItem = binding.mainBottom.menu.findItem(R.id.navigation_conversation)
//            val isChecked = menuItem.isChecked
//            if (newMessage) {
//                menuItem.setIcon(R.drawable.aichat_icon_conversation_unread_selector)
//            } else {
//                menuItem.setIcon(R.drawable.aichat_icon_conversation_selector)
//            }
//        }
        aiUserViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        aiUserViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }
        aiUserViewModel.loginChatLiveData.observe(this) { success ->
            if (success) {
                AIChatHelper.getInstance().getDataModel().initDb()
                binding.mainViewpager.adapter = mFragmentAdapter
            } else {
                binding.mainViewpager.postDelayed({
                    finish()
                }, 500)
            }
        }
    }

    override fun requestData() {
        super.requestData()
        aiUserViewModel.checkLoginIM(AIChatCenter.mChatUsername)
    }

    override fun onDestroy() {
        EaseIM.logout(true)
        EaseIM.releaseGlobalListener()
        AIChatCenter.onLogoutScene()
        AIChatHelper.reset()
        super.onDestroy()
    }
}
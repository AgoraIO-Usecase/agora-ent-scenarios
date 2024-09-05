package io.agora.scene.aichat.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatListActivityBinding
import io.agora.scene.aichat.imkit.ChatOptions
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.list.event.AIChatEventViewModel
import io.agora.scene.aichat.list.logic.AIAgentViewModel
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.widget.toast.CustomToast


val aiChatEventViewModel: AIChatEventViewModel by lazy { AIChatListActivity.eventViewModelInstance }

/**
 * Ai chat list activity
 *
 * @constructor Create empty Ai chat list activity
 */
class AIChatListActivity : BaseViewBindingActivity<AichatListActivityBinding>() {

    companion object {
        lateinit var eventViewModelInstance: AIChatEventViewModel
    }

    //viewModel
    private val aiAgentViewModel: AIAgentViewModel by viewModels()

    private var mFactory: ViewModelProvider.Factory? = null

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

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        }
        return mFactory as ViewModelProvider.Factory
    }

    override fun init() {
        super.init()
        eventViewModelInstance = ViewModelProvider(this, getAppFactory())[AIChatEventViewModel::class.java]
        initIM()
    }

    private fun initIM(){
        val options = io.agora.chat.ChatOptions().apply {
            appKey = AIChatCenter.mChatAppKey
            autoLogin = false
        }
        EaseIM.init(application, options)
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

        // test
        binding.btnNewMessage.setOnClickListener {
            aiChatEventViewModel.unreadMessageLiveData.postValue(true)
        }
        binding.btnRemoveMessage.setOnClickListener {
            aiChatEventViewModel.unreadMessageLiveData.postValue(false)
        }

        // 单个会话
        aiChatEventViewModel.unreadConversationLiveData.observe(this) {

        }
        // 所有会话
        aiChatEventViewModel.unreadMessageLiveData.observe(this) { newMessage ->
            val menuItem = binding.mainBottom.menu.findItem(R.id.navigation_conversation)
            val isChecked = menuItem.isChecked
            if (newMessage) {
                menuItem.setIcon(R.drawable.aichat_icon_conversation_unread_selector)
            } else {
                menuItem.setIcon(R.drawable.aichat_icon_conversation_selector)
            }
        }
        aiAgentViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        aiAgentViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }
        aiAgentViewModel.loginChatLiveData.observe(this) { success ->
            if (success) {
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
        aiAgentViewModel.checkLoginIM(AIChatCenter.mChatUsername)
    }

    override fun onDestroy() {
        aiAgentViewModel.reset()
        super.onDestroy()
    }
}
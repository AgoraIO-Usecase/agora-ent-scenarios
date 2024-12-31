package io.agora.scene.voice.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASoundManager
import com.opensource.svgaplayer.utils.log.SVGALogger
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceAgoraRoomListLayoutBinding
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.voice.imkit.manager.ChatroomConfigManager
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.dialog.CreateRoomDialog
import io.agora.scene.voice.ui.fragment.VoiceRoomListFragment
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.StatusBarUtil
import io.agora.scene.widget.utils.UiUtils

@Route(path = PagePathConstant.pageVoiceChat)
class VoiceRoomListActivity : BaseViewBindingActivity<VoiceAgoraRoomListLayoutBinding>() {

    private var title: TextView? = null
    private var index = 0
    private val titles = intArrayOf(R.string.voice_tab_layout_all)

    override fun getViewBinding(inflater: LayoutInflater): VoiceAgoraRoomListLayoutBinding? {
        return VoiceAgoraRoomListLayoutBinding.inflate(inflater)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Library initialization
        ChatroomConfigManager.getInstance()
            .initRoomConfig(applicationContext, VoiceCenter.chatAppKey)
        VoiceToolboxServerHttpManager.generateAllToken { token, exception ->  }
        SVGAParser.shareParser().init(this)
        SVGALogger.setLogEnabled(true)
        SVGASoundManager.init()
    }

    override fun onDestroy() {
        ChatroomIMManager.getInstance().logout(true)
        VoiceServiceProtocol.destroy()
        VoiceCenter.destroy()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.hideStatusBar(window, true)
        VoiceServiceProtocol.ROOM_AVAILABLE_DURATION = SceneConfigManager.chatExpireTime * 1000L

        super.onCreate(savedInstanceState)
        if (VoiceCenter.chatAppKey.isEmpty()) {
            finish()
            CustomToast.show(R.string.voice_not_config_im)
            return
        }
        binding.titleBar.title.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        setupWithViewPager()
        initListener()
    }

    override fun initListener() {
        binding.titleBar.setOnBackPressListener {
            finish()
        }
        binding.btnCreateRoom.setOnClickListener {
            if (UiUtils.isFastClick()) return@setOnClickListener
            CreateRoomDialog(this).show(supportFragmentManager, "CreateRoomDialog")
        }
        binding.agoraTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                onTabLayoutSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                onTabLayoutUnselected(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        binding.avatarLayout.isVisible = false
        binding.avatarLayout.setOnClickListener {
        }
    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tab_item_title)
            tabText.setTextColor(ResourcesCompat.getColor(resources, R.color.voice_dark_grey_color_040925, null))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            index = tab.position
            title = tabText
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tab_item_title)
            tabText.setTextColor(ResourcesCompat.getColor(resources, R.color.voice_color_979cbb, null))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
    }

    private fun setupWithViewPager() {
        binding.vpFragment.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        // set adapter
        binding.vpFragment.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                val fragment = VoiceRoomListFragment()
                fragment.itemCountListener = { count ->
                    title?.let { textView ->
                        val layoutParams = textView.layoutParams
                        layoutParams.height = 26.dp.toInt()
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        textView.gravity = Gravity.CENTER
                        val content =
                            getString(titles[index]) + getString(
                                R.string.voice_room_tab_layout_count,
                                count.toString()
                            )
                        textView.setTextColor(
                            ResourcesCompat.getColor(
                                this@VoiceRoomListActivity.resources, R.color.voice_dark_grey_color_040925, null
                            )
                        )
                        textView.text = content
                    }

                }
                return fragment
            }

            override fun getItemCount(): Int {
                return titles.size
            }
        }
        // set TabLayoutMediator
        val mediator =
            TabLayoutMediator(binding.agoraTabLayout, binding.vpFragment) { tab: TabLayout.Tab, position: Int ->
                tab.setCustomView(R.layout.voice_tab_item_layout)
                tab.customView?.findViewById<TextView>(R.id.tab_item_title)?.setText(titles[position])
            }
        // setup with viewpager2
        mediator.attach()
    }

    override fun finish() {
        super.finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }
}
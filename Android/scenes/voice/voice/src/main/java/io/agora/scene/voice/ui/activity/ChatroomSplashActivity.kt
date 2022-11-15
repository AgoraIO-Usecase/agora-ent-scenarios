package io.agora.scene.voice.ui.activity

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.agora.scene.base.PagePathConstant
import io.agora.scene.voice.BuildConfig
import io.agora.scene.voice.VoiceConfigManager.initMain
import io.agora.scene.voice.databinding.VoiceActivitySplashBinding
import io.agora.voice.baseui.BaseUiActivity
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.buddy.config.RouterPath
import io.agora.voice.buddy.tool.ResourcesTools
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.network.tools.bean.VRUserBean

class ChatroomSplashActivity : BaseUiActivity<VoiceActivitySplashBinding>() {

    companion object {
        const val SPLASH_DELAYED = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.is_modular){
           // nothing
        }else{
            // library 初始化
            initMain()
        }
        initListeners()
        if (ResourcesTools.isZh(this)) {
            binding.mtChatroom.letterSpacing = 0.42f
        } else {
            binding.mtChatroom.letterSpacing = -0.05f
        }

        val startTime = SystemClock.elapsedRealtime()
        val loginViewModel: io.agora.scene.voice.model.LoginViewModel =
            ViewModelProvider(this)[io.agora.scene.voice.model.LoginViewModel::class.java]
        loginViewModel.loginObservable.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<VRUserBean?>(true) {
                override fun onSuccess(data: VRUserBean?) {
                    io.agora.scene.voice.general.repositories.ProfileManager.getInstance().profile = data
                    val interval = SystemClock.elapsedRealtime() - startTime
                    ThreadManager.getInstance().runOnMainThreadDelay({
                        initSplashPage()
                    }, (SPLASH_DELAYED - interval).toInt())
                }
            })
        }
        loginViewModel.loginFromServer(this)
    }

    private fun initSplashPage() {
        ARouter.getInstance().build(RouterPath.ChatroomListPath).navigation()
        finish()
    }

    override fun getViewBinding(inflater: LayoutInflater): VoiceActivitySplashBinding {
        return VoiceActivitySplashBinding.inflate(inflater)
    }

    private fun initListeners() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.clRoot.setPaddingRelative(0, inset.top, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
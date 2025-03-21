package com.agora.entfulldemo.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppFragmentHomeFindBinding
import io.agora.scene.base.URLStatics
import com.google.gson.reflect.TypeToken
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager

class HomeFindFragment : BaseViewBindingFragment<AppFragmentHomeFindBinding>() {


    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentHomeFindBinding {
        return AppFragmentHomeFindBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPaddingRelative(inset.left, 0, inset.right, 0)
            WindowInsetsCompat.CONSUMED
        }
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun initView() {
        loadBannerWeb()
    }

    private fun getWebTitle(moduleName: String, document: Boolean = false): String {
//        return if (document) {
//            getString(R.string.app_find_module_doc_title, moduleName)
//        } else {
//            getString(R.string.app_find_module_title, moduleName)
//        }
        return getString(R.string.app_name)
    }

    override fun initListener() {
        binding.cvKtvInner.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_ktv))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenarioKtvURL, title)
            }
        })
        binding.tvKtvDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_ktv), true)
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocKtvURL, title)
            }
        })
        binding.btnSearchMusic.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_ktv_music_library))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findKtvSearchURL, title)
            }
        })
        binding.cvChatroom.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_chatroom))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenarioChatroomURL, title)
            }
        })
        binding.tvChatroomDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_chatroom), true)
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocChatroomURL, title)
            }
        })
        binding.cvLive.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_live))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenarioLiveURL, title)
            }
        })
        binding.tvLiveDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_live), true)
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocLiveURL, title)
            }
        })
        binding.cv1v1.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_1v1))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenario1v1URL, title)
            }
        })
        binding.tv1v1Doc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_1v1), true)
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDoc1v1URL, title)
            }
        })
        binding.cvAiDenoise.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_ai_denoise))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findAIDenoiseURL, title)
            }
        })
        binding.cvAiSpatial.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_ai_spatial))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findAISpatialURL, title)
            }
        })
        binding.cvVirtualSound.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                val title = getWebTitle(getString(R.string.app_find_virtual_sound))
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocVirtualSoundDoc, title)
            }
        })
    }

    @SuppressLint("JavascriptInterface")
    @JavascriptInterface
    private fun loadBannerWeb() {
        val stringBuilder =
            StringBuilder(URLStatics.findBannerURL)
                .append("#/?token=").append(UserManager.getInstance().user.token)

        binding.webView.addJavascriptInterface(AndroidToJs { params ->
            mMainHandler.post {
                try {
                    val paramsMap: Map<String, Any> = GsonUtils.gson.fromJson(
                        params,
                        object : TypeToken<Map<String, Any>>() {}.type
                    )

                    val redirectUrl: String? = paramsMap["redirectUrl"]?.toString()
                    if (redirectUrl.isNullOrEmpty()) {
                        Log.e("zhangw", "redirectUrl is null or empty!")
                    } else {
                        PagePilotManager.pageWebViewWithBrowser(redirectUrl,"")
                    }
                } catch (e: Exception) {
                    Log.e("zhangw", "JsonParser h5 params error:${e.message}")
                }
            }
        }, "JSBridge")
        binding.webView.loadUrl(stringBuilder.toString())
    }
}

class AndroidToJs constructor(val callback: ValueCallback<String>) : Any() {
    @JavascriptInterface
    fun jumpToWebview(params: String) {
        Log.d("zhangw", "params:$params currentThread:${Thread.currentThread()}")
        callback.onReceiveValue(params)
    }
}
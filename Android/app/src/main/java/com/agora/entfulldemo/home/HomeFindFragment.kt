package com.agora.entfulldemo.home

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.agora.entfulldemo.databinding.AppFragmentHomeFindBinding
import com.agora.entfulldemo.home.constructor.URLStatics
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnFastClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.manager.UserManager

class HomeFindFragment : BaseViewBindingFragment<AppFragmentHomeFindBinding>() {
    private var fullscreenContainer: FrameLayout? = null
    private var customViewCallback: CustomViewCallback? = null
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AppFragmentHomeFindBinding {
        return AppFragmentHomeFindBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun initView() {
//        binding.webView.loadUrl("file:///android_asset/index.html")
        loadBannerWeb()
    }

    override fun initListener() {
//        binding.webView.webChromeClient = MyWebChromeClient()
        binding.cvKtvInner.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findScenarioKtvURL)
            }
        })
        binding.tvKtvDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findDocKtvURL)
            }
        })
        binding.btnSearchMusic.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findKtvSearchURL)
            }
        })
        binding.cvChatroom.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findScenarioChatroomURL)
            }
        })
        binding.tvChatroomDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findDocChatroomURL)
            }
        })
        binding.cvLive.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findScenarioLiveURL)
            }
        })
        binding.tvLiveDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findDocLiveURL)
            }
        })
        binding.cvAiDenoise.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findAIDenoiseURL)
            }
        })
        binding.cvAiSpatial.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebView(URLStatics.findAISpatialURL)
            }
        })
    }

    @SuppressLint("JavascriptInterface")
    @JavascriptInterface
    private fun loadBannerWeb() {
        val stringBuilder =
            StringBuilder(URLStatics.findBannerURL)
                .append("?token=").append(UserManager.getInstance().user.token)

        binding.webView.addJavascriptInterface(AndroidToJs(), "JSBridge")
        binding.webView.loadUrl(stringBuilder.toString())
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

            }
        }
    }



//    private inner class MyWebChromeClient : WebChromeClient() {
//
//        override fun onJsPrompt(
//            view: WebView?,
//            url: String?,
//            message: String?,
//            defaultValue: String?,
//            result: JsPromptResult?
//        ): Boolean {
//            Log.d("zhangw", "onJsPrompt:$url,$message,$defaultValue,$result")
//            return super.onJsPrompt(view, url, message, defaultValue, result)
//        }
//
//        override fun onHideCustomView() {
//            //退出全屏
//            hideCustomView()
//        }
//
//        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
//            //进入全屏
//            showCustomView(view, callback)
//        }
//    }

    /**
     * 显示自定义控件
     */
    private fun showCustomView(view: View, callback: CustomViewCallback) {
        if (fullscreenContainer != null) {
            callback.onCustomViewHidden()
            return
        }
        fullscreenContainer = FrameLayout(requireActivity())
        fullscreenContainer!!.setBackgroundColor(Color.WHITE)
        customViewCallback = callback
        fullscreenContainer!!.addView(view)
        val decorView = requireActivity().window.decorView as FrameLayout
        // 收到需求，WebView不允许横屏
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        decorView.addView(fullscreenContainer)
    }

    /**
     * 隐藏自定义控件
     */
    private fun hideCustomView() {
        if (fullscreenContainer == null) {
            return
        }
        val decorView = requireActivity().window.decorView as FrameLayout
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        fullscreenContainer!!.removeAllViews()
        decorView.removeView(fullscreenContainer)
        fullscreenContainer = null
        customViewCallback!!.onCustomViewHidden()
        customViewCallback = null
    }
}

class AndroidToJs : Any() {
    @JavascriptInterface
    fun jumpToWebview(params: String) {
        Log.d("zhangw", "params:$params")
    }
}
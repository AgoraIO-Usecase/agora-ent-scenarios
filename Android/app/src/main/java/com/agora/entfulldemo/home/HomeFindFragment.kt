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
import com.agora.entfulldemo.databinding.AppFragmentHomeFindBinding
import com.agora.entfulldemo.home.constructor.URLStatics
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

    override fun initListener() {
        binding.cvKtvInner.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenarioKtvURL)
            }
        })
        binding.tvKtvDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocKtvURL)
            }
        })
        binding.btnSearchMusic.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findKtvSearchURL)
            }
        })
        binding.cvChatroom.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenarioChatroomURL)
            }
        })
        binding.tvChatroomDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocChatroomURL)
            }
        })
        binding.cvLive.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenarioLiveURL)
            }
        })
        binding.tvLiveDoc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocLiveURL)
            }
        })
        binding.cv1v1.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findScenario1v1URL)
            }
        })
        binding.tv1v1Doc.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDoc1v1URL)
            }
        })
        binding.cvAiDenoise.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findAIDenoiseURL)
            }
        })
        binding.cvAiSpatial.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findAISpatialURL)
            }
        })
        binding.cvVirtualSound.setOnClickListener(object : OnFastClickListener() {
            override fun onClickJacking(view: View) {
                PagePilotManager.pageWebViewWithBrowser(URLStatics.findDocVirtualSoundDoc)
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
                        PagePilotManager.pageWebViewWithBrowser(redirectUrl)
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
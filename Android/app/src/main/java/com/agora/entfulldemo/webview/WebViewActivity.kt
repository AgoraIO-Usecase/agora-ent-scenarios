package com.agora.entfulldemo.webview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppActivityWebviewBinding
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.agora.scene.base.Constant
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.dp

@Route(path = PagePathConstant.pageWebView)
class WebViewActivity : BaseViewBindingActivity<AppActivityWebviewBinding>() {
    @JvmField
    @Autowired(name = Constant.URL)
    var url: String = "https://www.agora.io/cn/about-us/"

    @JvmField
    @Autowired(name = Constant.PARAMS_WITH_BROWSER)
    var withBrowser: Boolean = false

    @JvmField
    @Autowired(name = Constant.PARAMS_TITLE)
    var mTitle: String = ""

    override fun getViewBinding(layoutInflater: LayoutInflater): AppActivityWebviewBinding {
        return AppActivityWebviewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
        Log.d("WebViewActivity", "URL: $url")
        binding?.apply {
            setOnApplyWindowInsetsListener(superLayout)
            if (url.contains("privacy/service")) {
                titleView.setTitle(getString(R.string.app_user_agreement))
            } else if (url.contains("about-us")) {
                titleView.setTitle(getString(R.string.app_about_us))
            } else if (url.contains("privacy/privacy")) {
                titleView.setTitle(getString(R.string.app_privacy_agreement))
            } else if (url.contains("privacy/libraries")) {
                titleView.setTitle(getString(R.string.app_third_party_info_data_sharing))
            } else if (url.contains("ent-scenarios/pages/manifest")) {
                titleView.setTitle(getString(R.string.app_personal_info_collection_checklist))
            }
            webView.loadUrl(url)

            if (mTitle.isNotEmpty()){
                titleView.setTitle(mTitle)
            }


            if (withBrowser) {
                titleView.leftIcon.setImageResource(R.drawable.app_icon_back_black)
                val right = titleView.rightIcon
                right.visibility = View.VISIBLE
                val layoutParams = right.layoutParams
                layoutParams.width = 22.dp.toInt()
                layoutParams.height = 22.dp.toInt()
                right.layoutParams = layoutParams
                right.setImageResource(R.drawable.app_icon_find_earth)
                titleView.setRightIconClick { v: View? ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }

            titleView.setLeftClick { v: View? ->
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
            webView.webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView, title: String) {
                    super.onReceivedTitle(view, title)
                    if (mTitle.isNotEmpty()) return
                    if (!TextUtils.isEmpty(title) && !view.url!!.contains(title)) {
                        titleView.setTitle(title)
                    }
                }
            }
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (binding?.webView?.canGoBack() == true) {
            binding?.webView?.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

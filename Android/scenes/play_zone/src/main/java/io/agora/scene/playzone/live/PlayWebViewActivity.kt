package io.agora.scene.playzone.live

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.playzone.databinding.PlayZoneActivityWebviewBinding

class PlayWebViewActivity : BaseViewBindingActivity<PlayZoneActivityWebviewBinding>() {

    companion object {
        const val KEY_URL = "key_url"

        fun startActivity(context: Context, url: String?) {
            val intent = Intent(context, PlayWebViewActivity::class.java)
            intent.putExtra(KEY_URL, url)
            context.startActivity(intent)
        }
    }

    private val url: String get() = intent.getStringExtra(KEY_URL) ?: ""

    override fun getViewBinding(layoutInflater: LayoutInflater): PlayZoneActivityWebviewBinding {
        return PlayZoneActivityWebviewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setOnApplyWindowInsetsListener(binding.superLayout)
        binding.webView.loadUrl(url)

        binding.titleView.setLeftClick { v: View? ->
            Log.d("PlayWebViewActivity", "Left button clicked")
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                Log.d("PlayWebViewActivity", "Finishing activity")
                binding.webView.destroy()
                finish()
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                Log.d("zhangw", "WebChromeClient webView title：$title")
                val url = view.url ?: return
                if (title.isNotEmpty() && !url.contains(title)) {
                    binding.titleView.setTitle(title)
                }
            }
        }
        binding.webView.webViewClient = GameWebViewClient()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Listen to all clicked links, if we intercept what we need, jump to the corresponding page
    inner class GameWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            Log.d("zhangw", "shouldOverrideUrlLoading:$url")
            if (url.startsWith("game://")) {
                // Handle meta game exit page
                if (url == "game://close") {
                    finish()
                }
                return true // Indicates it's been handled, no need to load this URL in WebView
            }

            if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp") || url.startsWith("file:///android_asset")) {
                if (url.startsWith("file:///android_asset")) {
                    startActivity(this@PlayWebViewActivity, url)
                    return true
                }
                return false
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

    }
}

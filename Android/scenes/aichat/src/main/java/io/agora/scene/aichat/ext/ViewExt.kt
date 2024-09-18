package io.agora.scene.aichat.ext

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.agora.scene.aichat.R
import io.agora.scene.base.GlideApp


var lastClickTime = 0L

/**
 * 防止重复点击事件 默认1秒内不可重复点击
 * @param interval 时间间隔 默认1秒
 * @param action 执行方法
 */
fun View.clickNoRepeat(interval: Long = 1000, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (lastClickTime != 0L && (currentTime - lastClickTime < interval)) {
            return@setOnClickListener
        }
        lastClickTime = currentTime
        action(it)
    }
}

fun TabLayout.addAgentTabSelectedListener() {
    this.addOnTabSelectedListener(object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            Log.d("tab", "onTabSelected:${tab.id}")
            val customView = tab.customView
            if (customView == null) {
                tab.setCustomView(R.layout.aichat_agent_tabitem)
            }
            val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return
            TextViewCompat.setTextAppearance(tvTabTitle, R.style.aichat_TabLayoutTextSelected)
            val viewIndicator: View = tab.customView?.findViewById(R.id.viewIndicator) ?: return
            viewIndicator.setBackgroundResource(R.drawable.aichat_tablayout_indicator)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            Log.d("tab", "onTabUnselected:${tab.id}")
            val customView = tab.customView
            if (customView == null) {
                tab.setCustomView(R.layout.aichat_agent_tabitem)
            }
            val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return
            TextViewCompat.setTextAppearance(tvTabTitle, R.style.aichat_TabLayoutTextUnSelected)
            val viewIndicator: View = tab.customView?.findViewById(R.id.viewIndicator) ?: return
            viewIndicator.setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            Log.d("tab", "onTabReselected:${tab.id}")
            val customView = tab.customView
            if (customView == null) {
                tab.setCustomView(R.layout.aichat_agent_tabitem)
            }
            val tvTabTitle: TextView = tab.customView?.findViewById(R.id.tvTabTitle) ?: return
            TextViewCompat.setTextAppearance(tvTabTitle, R.style.aichat_TabLayoutTextSelected)
            val viewIndicator: View = tab.customView?.findViewById(R.id.viewIndicator) ?: return
            viewIndicator.setBackgroundResource(R.drawable.aichat_tablayout_indicator)
        }
    })
}

fun ImageView.loadImage(url: String) {
    GlideApp.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(this)
}

fun ImageView.loadCircleImage(url: String) {
    GlideApp.with(this)
        .load(url)
        .error(R.drawable.aichat_agent_avatar_2)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}

fun String.getIdentifier(context: Context, defType: String = "drawable"): Int {
    return context.resources.getIdentifier(this, defType, context.packageName)
}

/**
 * Show soft keyboard.
 */
fun View.showSoftKeyboard() {
    this.postDelayed({
        requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
            it.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }, 200)
}

/**
 * Hide soft keyboard.
 */
fun View.hideSoftKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
        it.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Int.getAgentItemBackground(): String {
    return when (this % 3) {
        0 -> return "aichat_agent_item_green_bg"
        1 -> return "aichat_agent_item_orange_bg"
        else -> return "aichat_agent_item_purple_bg"
    }
}

fun Int.getConversationItemBackground(): String {
    return when (this % 3) {
        0 -> return "aichat_conversation_item_green_bg"
        1 -> return "aichat_conversation_item_orange_bg"
        else -> return "aichat_conversation_item_purple_bg"
    }
}
package com.agora.entfulldemo.home.mine

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agora.entfulldemo.BuildConfig
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppAboutInfoItemBinding
import com.agora.entfulldemo.databinding.AppAboutSceneItemBinding
import com.agora.entfulldemo.databinding.AppActivityAboutUsBinding
import io.agora.rtc2.RtcEngine
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.dialog.CommonDialog

class AboutUsActivity : BaseViewBindingActivity<AppActivityAboutUsBinding>() {

    private val servicePhone = "400-632-6626"
    private val webSite = "https://www.agora.io/cn/about-us/"

    private val kChatRoomAppID = "io.agora.chatroom"
    private val kFullAppID = "com.agora.entfulldemo"

    private var counts = 0
    private val debugModeOpenTime: Long = 2000
    private var beginTime: Long = 0

    private val adapter = AboutUsAdapter()

    override fun getViewBinding(inflater: LayoutInflater): AppActivityAboutUsBinding {
        return AppActivityAboutUsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.rvAboutUs.adapter = adapter
        if (BuildConfig.APPLICATION_ID == kChatRoomAppID) {
            setupChatRoomAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kFullAppID) {
            setupFullAppInfo()
        }
        setupDebugMode()
        setupClickWebAction()
        setupClickPhoneAction()
    }
    // 设置语聊App的信息
    private fun setupChatRoomAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        adapter.appInfo = AppInfo(
            this.getString(R.string.app_about_name),
            "20230110-2.1.0-" + RtcEngine.getSdkVersion(),
            servicePhone,
            webSite
        )
    }
    // 设置综合App的信息
    private fun setupFullAppInfo() {
        val scenes = mutableListOf<SceneInfo>()
        if (io.agora.scene.base.BuildConfig.VERSION_SCENE_VOICE.isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_chat_room),
                    io.agora.scene.base.BuildConfig.VERSION_SCENE_VOICE
                )
            )
        }
        if (io.agora.scene.base.BuildConfig.VERSION_SCENE_KTV.isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_karaoke),
                    io.agora.scene.base.BuildConfig.VERSION_SCENE_KTV
                )
            )
        }
        if (io.agora.scene.base.BuildConfig.VERSION_SCENE_SPATIAL_VOICE.isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_chat_room_spatial),
                    io.agora.scene.base.BuildConfig.VERSION_SCENE_SPATIAL_VOICE
                )
            )
        }
        if (scenes.size == 1) {
            adapter.scenes = mutableListOf()
            val scene = scenes[0]
            adapter.appInfo = AppInfo(
                scene.name,
                scene.version,
                servicePhone,
                webSite
            )
        } else if (scenes.size > 1) {
            adapter.scenes = scenes
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_about_name),
                "20230110-2.1.0-" + RtcEngine.getSdkVersion(),
                servicePhone,
                webSite
            )
        }
    }

    private fun setupClickWebAction() {
        adapter.onClickWebSiteListener = {
            PagePilotManager.pageWebView(webSite)
        }
    }

    private fun setupClickPhoneAction() {
        adapter.onClickPhoneListener = {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE),1)
            } else {
                val dialog = CallPhoneDialog().apply {
                    arguments = Bundle().apply {
                        putString(CallPhoneDialog.KEY_PHONE, servicePhone)
                    }
                }
                dialog.onClickCallPhone = {
                    val intent = Intent(Intent.ACTION_CALL)
                    val uri = Uri.parse("tel:" + servicePhone)
                    intent.setData(uri)
                    startActivity(intent)
                }
                dialog.show(supportFragmentManager, "CallPhoneDialog")
            }
        }
    }

    private fun setupDebugMode() {
        binding.tvDebugMode.visibility = View.INVISIBLE
        adapter.onClickVersionListener = {
            if (counts == 0 || System.currentTimeMillis() - beginTime > debugModeOpenTime) {
                beginTime = System.currentTimeMillis();
                counts = 0
            }
            counts ++
            if (counts > 5) {
                counts = 0;
                binding.tvDebugMode.visibility = View.VISIBLE
                AgoraApplication.the().enableDebugMode(true)
                ToastUtils.showToast("Debug模式已打开");
            }
        }
        binding.tvDebugMode.setOnClickListener {
            showDebugModeCloseDialog()
        }
        if (AgoraApplication.the().isDebugModeOpen) {
            binding.tvDebugMode.visibility = View.VISIBLE
        }
    }

    private fun showDebugModeCloseDialog() {
        val dialog = CommonDialog(this)
        dialog.setDialogTitle("确定退出Debug模式么？")
        dialog.setDescText("退出debug模式后， 设置页面将恢复成正常的设置页面哦～")
        dialog.setDialogBtnText(getString(R.string.cancel),
            getString(R.string.app_exit))
        dialog.onButtonClickListener = object : OnButtonClickListener {
            override fun onLeftButtonClick() {}
            override fun onRightButtonClick() {
                counts = 0
                binding.tvDebugMode.visibility = View.GONE
                AgoraApplication.the().enableDebugMode(false)
                ToastUtils.showToast("Debug模式已关闭")
            }
        }
        dialog.show()
    }
}
private data class AppInfo(
    val name: String,
    val version: String,
    val servicePhone: String,
    val webSite: String
)

private data class SceneInfo(
    val name: String,
    val version: String
)

private class AboutUsAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_APP_INFO = 0

    private val VIEW_TYPE_SCENE_INFO = 1

    var appInfo: AppInfo? = null

    var scenes = mutableListOf<SceneInfo>()

    var onClickPhoneListener: (() -> Unit)? = null

    var onClickWebSiteListener: (() -> Unit)? = null

    var onClickVersionListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_APP_INFO) {
            val binding = AppAboutInfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AppInfoViewHolder(binding, binding.root)
        } else {
            val binding = AppAboutSceneItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SceneInfoViewHolder(binding, binding.root)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_APP_INFO) {
            val current = holder as AppInfoViewHolder
            appInfo?.let {
                current.binding.tvAppName.text = it.name
                current.binding.tvVersion.text = it.version
                current.binding.tvServiceNumber.text = it.servicePhone
                current.binding.tvHomeWebSite.text = it.webSite
            }
            current.binding.tvSceneSubTitle.visibility = if (scenes.size > 1)  View.VISIBLE else View.INVISIBLE
            current.binding.tvVersion.setOnClickListener {
                onClickVersionListener?.invoke()
            }
            current.binding.vServicePhone.setOnClickListener {
                onClickPhoneListener?.invoke()
            }
            current.binding.vHomeWebPage.setOnClickListener {
                onClickWebSiteListener?.invoke()
            }
        } else if (holder.itemViewType == VIEW_TYPE_SCENE_INFO) {
            val current = holder as SceneInfoViewHolder
            val index = position - 1
            val model = scenes[index]
            current.binding.tvTitle.text  = model.name
            current.binding.tvVersion.text = model.version
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_APP_INFO
        } else {
            VIEW_TYPE_SCENE_INFO
        }
    }

    override fun getItemCount(): Int {
        return scenes.size + 1
    }

    inner class AppInfoViewHolder(
        val binding: AppAboutInfoItemBinding,
        itemView: View
    ) : RecyclerView.ViewHolder(itemView)

    inner class SceneInfoViewHolder(
        val binding: AppAboutSceneItemBinding,
        itemView: View
    ) : RecyclerView.ViewHolder(itemView)
}
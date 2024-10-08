package com.agora.entfulldemo.home.mine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agora.entfulldemo.BuildConfig
import com.agora.entfulldemo.R
import com.agora.entfulldemo.databinding.AppAboutInfoItemBinding
import com.agora.entfulldemo.databinding.AppAboutSceneItemBinding
import com.agora.entfulldemo.databinding.AppActivityAboutUsBinding
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.rtc2.RtcEngine
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.manager.PagePilotManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.VersionUtils
import io.agora.scene.widget.dialog.CommonDialog

@Route(path = PagePathConstant.pageAboutUs)
class AboutUsActivity : BaseViewBindingActivity<AppActivityAboutUsBinding>() {

    private val servicePhone = "400-632-6626"
    private val webSite = "https://www.shengwang.cn/"

    private val kKtvRoomAppID = "io.agora.ktv"
    private val kChatRoomAppID = "io.agora.chatroom"
    private val kFullAppID = "io.agora.AgoraVoice"
    private val kSingRelayAppID = "io.agora.singrelay"
    private val kSingBattleRoomAppID = "io.agora.singbattle"
    private val kCantataAppID = "io.agora.cantata"
    private val kShowRoomAppID = "io.agora.test.entfull"
    private val kJoyRoomAppID = "io.agora.joy"

    private var counts = 0
    private val debugModeOpenTime: Long = 2000
    private var beginTime: Long = 0

    private val adapter = AboutUsAdapter(this)

    override fun getViewBinding(inflater: LayoutInflater): AppActivityAboutUsBinding {
        return AppActivityAboutUsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.rvAboutUs.adapter = adapter
        if (BuildConfig.APPLICATION_ID == kChatRoomAppID) {
            setupChatRoomAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kKtvRoomAppID) {
            setupKtvRoomAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kSingBattleRoomAppID) {
            setupSingBattleRoomAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kShowRoomAppID) {
            setupShowRoomAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kSingRelayAppID) {
            setupSingRelayAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kCantataAppID) {
            setupCantataAppInfo()
        } else if (BuildConfig.APPLICATION_ID == kJoyRoomAppID){
            setupJoyAppInfo()
        }else {
            setupFullAppInfo()
        }
        setupDebugMode()
        setupClickWebAction()
        setupClickPhoneAction()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    // 设置K歌房App的信息
    private fun setupKtvRoomAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        adapter.appInfo = AppInfo(
            this.getString(R.string.app_name),
            "20240511-" + VersionUtils.getVersion("io.agora.scene.ktv.BuildConfig") + "-" + RtcEngine.getSdkVersion(),
            servicePhone,
            webSite
        )
    }

    // 设置语聊App的信息
    private fun setupChatRoomAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        if (VersionUtils.getVersion("io.agora.scene.voice.BuildConfig").isNotEmpty()) {
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_name),
                "20230110-" + VersionUtils.getVersion("io.agora.scene.voice.BuildConfig") + "-" + RtcEngine.getSdkVersion(),
                servicePhone,
                webSite
            )
        }
    }

    // 设置接唱App的信息
    private fun setupSingRelayAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        if (VersionUtils.getVersion("io.agora.scene.ktv.singrelay.BuildConfig").isNotEmpty()) {
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_name),
                "20230830-" + VersionUtils.getVersion("io.agora.scene.ktv.singrelay.BuildConfig") + "-" + RtcEngine.getSdkVersion(),
                servicePhone,
                webSite
            )
        }
    }

    // 设置秀场直播App的信息
    private fun setupShowRoomAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        if (VersionUtils.getVersion("io.agora.scene.show.BuildConfig").isNotEmpty()) {
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_about_show),
                "20240511-" + VersionUtils.getVersion("io.agora.scene.show.BuildConfig") + "-" + RtcEngine.getSdkVersion(),
                servicePhone,
                webSite
            )
        }
    }

    // 设置抢唱App的信息
    private fun setupSingBattleRoomAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        adapter.appInfo = AppInfo(
            this.getString(R.string.app_name),
            "20230520-" + VersionUtils.getVersion("io.agora.scene.ktv.singbattle.BuildConfig") + RtcEngine.getSdkVersion(),
            servicePhone,
            webSite
        )
    }

    // 设置大合唱App的信息
    private fun setupCantataAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        if (VersionUtils.getVersion("io.agora.scene.cantata.BuildConfig").isNotEmpty()) {
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_name),
                "20231230-" + VersionUtils.getVersion("io.agora.scene.cantata.BuildConfig") + "-" + RtcEngine
                    .getSdkVersion(),
                servicePhone,
                webSite
            )
        }
    }

    // 设置小玩法的信息
    private fun setupJoyAppInfo() {
        adapter.scenes = mutableListOf<SceneInfo>()
        if (VersionUtils.getVersion("io.agora.scene.joy.BuildConfig").isNotEmpty()) {
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_about_joy),
                "20231230-" + VersionUtils.getVersion("io.agora.scene.joy.BuildConfig") + "-" + RtcEngine
                    .getSdkVersion(),
                servicePhone,
                webSite
            )
        }
    }

    // 设置综合App的信息
    private fun setupFullAppInfo() {
        val scenes = mutableListOf<SceneInfo>()
        if (VersionUtils.getVersion("io.agora.scene.voice.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_chat_room),
                    "YL-" + VersionUtils.getVersion("io.agora.scene.voice.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.voice.spatial.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_chat_room_spatial),
                    "YLKJ-" + VersionUtils.getVersion("io.agora.scene.voice.spatial.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.ktv.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_karaoke),
                    "KTV-" + VersionUtils.getVersion("io.agora.scene.ktv.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.ktv.singbattle.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_singbattle),
                    "QC-" + VersionUtils.getVersion("io.agora.scene.ktv.singbattle.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.ktv.singrelay.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_sing_relay),
                    "JC-" + VersionUtils.getVersion("io.agora.scene.ktv.singrelay.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.cantata.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_cantata),
                    "DHC-" + VersionUtils.getVersion("io.agora.scene.cantata.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.show.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_show),
                    "ZB-" + VersionUtils.getVersion("io.agora.scene.show.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.pure1v1.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_pure1v1),
                    "SMF-" + VersionUtils.getVersion("io.agora.scene.pure1v1.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.showTo1v1.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_showTo1v1),
                    "XCSMF-" + VersionUtils.getVersion("io.agora.scene.showTo1v1.BuildConfig")
                )
            )
        }
        if (VersionUtils.getVersion("io.agora.scene.joy.BuildConfig").isNotEmpty()) {
            scenes.add(
                SceneInfo(
                    this.getString(R.string.app_about_joy),
                    "XWF-" + VersionUtils.getVersion("io.agora.scene.joy.BuildConfig")
                )
            )
        }
        val versionTime = "20240904-"
        if (scenes.size == 1) {
            adapter.scenes = mutableListOf()
            val scene = scenes[0]
            adapter.appInfo = AppInfo(
                scene.name,
                versionTime + scene.version + "-" + RtcEngine.getSdkVersion(),
                servicePhone,
                webSite
            )
        } else if (scenes.size > 1) {
            adapter.scenes = scenes
            adapter.appInfo = AppInfo(
                this.getString(R.string.app_name),
                versionTime + io.agora.scene.base.BuildConfig.APP_VERSION_NAME + "-" + RtcEngine.getSdkVersion(),
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
            val dialog = CallPhoneDialog().apply {
                arguments = Bundle().apply {
                    putString(CallPhoneDialog.KEY_PHONE, servicePhone)
                }
            }
            dialog.onClickCallPhone = {
                val intent = Intent(Intent.ACTION_DIAL)
                val uri = Uri.parse("tel:$servicePhone")
                intent.setData(uri)
                startActivity(intent)
            }
            dialog.show(supportFragmentManager, "CallPhoneDialog")
        }
    }

    private val handler = Handler()
    private fun setupDebugMode() {
        binding.tvDebugMode.visibility = View.INVISIBLE
        adapter.onTouchVersionListener = { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.postDelayed({
                        binding.tvDebugMode.visibility = View.VISIBLE
                        AgoraApplication.the().enableDebugMode(true)
                        ToastUtils.showToast(R.string.app_debug_open)
                    }, 5000)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacksAndMessages(null)
                }
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
        dialog.setDialogTitle(getString(R.string.app_exit_debug))
        dialog.setDescText(getString(R.string.app_exit_debug_tip))
        dialog.setDialogBtnText(
            getString(R.string.cancel),
            getString(R.string.app_exit)
        )
        dialog.onButtonClickListener = object : OnButtonClickListener {
            override fun onLeftButtonClick() {}
            override fun onRightButtonClick() {
                binding.tvDebugMode.visibility = View.GONE
                AgoraApplication.the().enableDebugMode(false)
                ToastUtils.showToast(R.string.app_debug_off)
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

private class AboutUsAdapter(
    val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_APP_INFO = 0

    private val VIEW_TYPE_SCENE_INFO = 1

    var appInfo: AppInfo? = null

    var scenes = mutableListOf<SceneInfo>()

    var onClickPhoneListener: (() -> Unit)? = null

    var onClickWebSiteListener: (() -> Unit)? = null

    var onTouchVersionListener: ((MotionEvent) -> Unit)? = null

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
//                current.binding.tvAppName.text = it.name
                current.binding.tvVersion.text = context.getString(R.string.app_mine_current_version, it.version)
                current.binding.tvServiceNumber.text = it.servicePhone
                current.binding.tvHomeWebSite.text = it.webSite
            }
            current.binding.tvSceneSubTitle.visibility = if (scenes.size > 1) View.VISIBLE else View.INVISIBLE
            current.binding.tvVersion.setOnTouchListener { _, event ->
                onTouchVersionListener?.invoke(event)
                false
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
            current.binding.tvTitle.text = model.name
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
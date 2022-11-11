package io.agora.scene.voice

import android.app.Application
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASoundManager
import com.opensource.svgaplayer.utils.log.SVGALogger
import com.tencent.bugly.crashreport.CrashReport
import io.agora.scene.voice.general.interfaceOrImplement.UserActivityLifecycleCallbacks
import io.agora.voice.imkit.manager.ChatroomConfigManager
import io.agora.voice.network.http.VRRequestApi
import io.agora.voice.network.http.toolbox.VoiceToolboxRequestApi

/**
 * @author create by zhangwei03
 */
object VoiceConfigManager {
    private lateinit var instance: Application

    private val mLifecycleCallbacks = UserActivityLifecycleCallbacks()

    @JvmStatic
    fun initMain(app: Application){
        instance = app
        ChatroomConfigManager.getInstance().initRoomConfig(app, BuildConfig.im_app_key)
        VRRequestApi.get().setBaseUrl(BuildConfig.server_host)
        VoiceToolboxRequestApi.get().setBaseUrl(BuildConfig.toolbox_server_host)
        app.registerActivityLifecycleCallbacks(mLifecycleCallbacks)
        SVGAParser.shareParser().init(app)
        SVGALogger.setLogEnabled(true)
        SVGASoundManager.init()
        CrashReport.initCrashReport(app, "baed12f146", false)
    }

    @JvmStatic
    fun getLifecycleCallbacks(): UserActivityLifecycleCallbacks {
        return mLifecycleCallbacks
    }
}
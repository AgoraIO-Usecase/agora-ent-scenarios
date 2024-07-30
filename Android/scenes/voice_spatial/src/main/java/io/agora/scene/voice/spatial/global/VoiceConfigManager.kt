package io.agora.scene.voice.spatial.global

import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASoundManager
import com.opensource.svgaplayer.utils.log.SVGALogger

/**
 * @author create by zhangwei03
 */
object VoiceConfigManager {

    private val mLifecycleCallbacks =
        UserActivityLifecycleCallbacks()

    @JvmStatic
    fun initMain() {
        VoiceBuddyFactory.get().getVoiceBuddy().application().registerActivityLifecycleCallbacks(mLifecycleCallbacks)
        SVGAParser.shareParser().init( VoiceBuddyFactory.get().getVoiceBuddy().application())
        SVGALogger.setLogEnabled(true)
        SVGASoundManager.init()
//        CrashReport.initCrashReport(VoiceBuddyFactory.get().getVoiceBuddy().application(), "baed12f146", false)
    }

    fun unInitMain(){
        VoiceBuddyFactory.get().getVoiceBuddy().application().unregisterActivityLifecycleCallbacks(mLifecycleCallbacks)
    }

    @JvmStatic
    fun getLifecycleCallbacks(): UserActivityLifecycleCallbacks {
        return mLifecycleCallbacks
    }
}
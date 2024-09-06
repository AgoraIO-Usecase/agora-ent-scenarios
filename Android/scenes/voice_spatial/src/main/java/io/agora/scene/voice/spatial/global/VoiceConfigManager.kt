package io.agora.scene.voice.spatial.global

import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASoundManager
import com.opensource.svgaplayer.utils.log.SVGALogger

/**
 * @author create by zhangwei03
 */
object VoiceConfigManager {
    @JvmStatic
    fun initMain() {
        SVGAParser.shareParser().init( VoiceBuddyFactory.get().getVoiceBuddy().application())
        SVGALogger.setLogEnabled(true)
        SVGASoundManager.init()
//        CrashReport.initCrashReport(VoiceBuddyFactory.get().getVoiceBuddy().application(), "baed12f146", false)
    }

    fun unInitMain(){
    }
}
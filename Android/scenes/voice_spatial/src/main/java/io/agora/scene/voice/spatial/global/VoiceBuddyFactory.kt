package io.agora.scene.voice.spatial.global

import io.agora.scene.voice.spatial.rtckit.RtcChannelTemp

/**
 * @author create by zhangwei03
 */
class VoiceBuddyFactory {

    private var voiceUser: IVoiceBuddy? = null
    val rtcChannelTemp = RtcChannelTemp()

    companion object {

        private val instance: VoiceBuddyFactory by lazy {
            VoiceBuddyFactory()
        }

        @JvmStatic
        fun get(): VoiceBuddyFactory {
            return VoiceBuddyFactory.Companion.instance
        }
    }

    fun getVoiceBuddy(): IVoiceBuddy {
        if (voiceUser == null) {
            voiceUser = VoiceBuddyImp()
        }
        return voiceUser!!
    }
}
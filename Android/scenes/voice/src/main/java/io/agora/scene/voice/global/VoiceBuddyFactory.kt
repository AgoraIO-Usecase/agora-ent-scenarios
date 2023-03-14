package io.agora.scene.voice.global

import io.agora.scene.voice.rtckit.RtcChannelTemp

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
            return instance
        }
    }

    fun getVoiceBuddy(): IVoiceBuddy {
        if (voiceUser == null) {
            voiceUser = VoiceBuddyImp()
        }
        return voiceUser!!
    }
}
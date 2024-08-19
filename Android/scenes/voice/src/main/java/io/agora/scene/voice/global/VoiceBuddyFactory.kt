package io.agora.scene.voice.global

import io.agora.scene.voice.rtckit.RtcChannelTemp

/**
 * @author create by zhangwei03
 */
class VoiceBuddyFactory {

    private var voiceUser: IVoiceBuddy? = null
    val rtcChannelTemp = RtcChannelTemp()

    companion object {

        private var innerVoiceBuddy: VoiceBuddyFactory? = null

        @JvmStatic
        fun get(): VoiceBuddyFactory {
            if (innerVoiceBuddy == null) {
                innerVoiceBuddy = VoiceBuddyFactory()
            }
            return innerVoiceBuddy!!
        }

        @Synchronized
        fun destroy() {
            innerVoiceBuddy = null
        }
    }

    fun getVoiceBuddy(): IVoiceBuddy {
        if (voiceUser == null) {
            voiceUser = VoiceBuddyImp()
        }
        return voiceUser!!
    }
}
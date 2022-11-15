package io.agora.scene.voice.service

import io.agora.voice.buddy.IVoiceBuddy

/**
 * @author create by zhangwei03
 */
class VoiceBuddyFactory {

    private var voiceUser: IVoiceBuddy? = null

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
package io.agora.scene.voice.global

/**
 * @author create by zhangwei03
 */
object ConfigConstants {

    // Number of robots
    const val ROBOT_COUNT = 2

    // Clickable opacity
    const val ENABLE_ALPHA = 1.0f

    // Unclickable opacity
    const val DISABLE_ALPHA = 0.2f

    // Default volume
    const val RotDefaultVolume = 50

    //--------Room types start--------
    object RoomType {
        const val Common_Chatroom = 0
        const val Spatial_Chatroom = 1
    }
    //--------Room types end--------

    //--------Sound effect types start--------
    object SoundSelection {
        const val Social_Chat = 1
        const val Karaoke = 2
        const val Gaming_Buddy = 3
        const val Professional_Broadcaster = 4
    }
    //--------Sound effect types end--------

    object SoundSelectionText {
        const val Social_Chat = "Social Chat"
        const val Karaoke = "Karaoke"
        const val Gaming_Buddy = "Gaming Buddy"
        const val Professional_Broadcaster = "Professional podcaster"
    }

    //--------AI noise reduction modes start--------
    object AINSMode {
        const val AINS_High = 0 
        const val AINS_Medium = 1 
        const val AINS_Off = 2  // Off
        const val AINS_Unknown = -1

        const val AINS_Tradition_Strong = 5 // Traditional Strong
        const val AINS_Tradition_Weakness = 6 // Traditional Weak
        const val AINS_AI_Strong = 7 // AI Strong
        const val AINS_AI_Weakness = 8  // AI Weak
        const val AINS_Custom = 9  // Custom
    }
    //--------AI noise reduction modes end--------

    //--------AI robot playback types start--------
    object BotSpeaker {
        const val None = -1
        const val BotBlue = 0 // Robot Blue
        const val BotRed = 1 // Robot Red
        const val BotBoth = 2 // Both robots playing together
    }
    //--------AI robot playback types end--------

    //--------Volume levels start--------
    object VolumeType {
        const val Volume_None = 0
        const val Volume_Low = 1
        const val Volume_Medium = 2
        const val Volume_High = 3
        const val Volume_Max = 4
    }
    //--------Volume levels end--------

    //--------AI noise reduction - 14 types of noise samples start--------
    object AINSSoundType {
        const val AINS_TVSound = 1 // TV noise
        const val AINS_KitchenSound = 2 // Kitchen noise
        const val AINS_StreetSound = 3 // Street noise
        const val AINS_MachineSound = 4 // Machine noise
        const val AINS_OfficeSound = 5 // Office noise
        const val AINS_HomeSound = 6 // Home noise
        const val AINS_ConstructionSound = 7 // Construction noise
        const val AINS_AlertSound = 8 // Alert sounds/Music
        const val AINS_ApplauseSound = 9 // Applause
        const val AINS_WindSound = 10 // Wind noise
        const val AINS_MicPopFilterSound = 11 // Microphone pop
        const val AINS_AudioFeedback = 12 // Audio feedback
        const val AINS_MicrophoneFingerRub = 13 // Finger rubbing on mobile phone microphone
        const val AINS_MicrophoneScreenTap = 14 // Screen tapping noise on mobile phone
    }
    //--------AI noise reduction - 14 types of noise samples end--------

    object MicConstant {
        const val KeyMic0 = "mic_0"
        const val KeyMic1 = "mic_1"
        const val KeyMic2 = "mic_2"
        const val KeyMic3 = "mic_3"
        const val KeyMic4 = "mic_4"
        const val KeyMic5 = "mic_5"
        const val KeyMic6 = "mic_6"
        const val KeyMic7 = "mic_7"

        const val KeyIndex0 = 0
        const val KeyIndex1 = 1
        const val KeyIndex2 = 2
        const val KeyIndex3 = 3
        const val KeyIndex4 = 4
        const val KeyIndex5 = 5
        const val KeyIndex6 = 6
        const val KeyIndex7 = 7

        val micMap: MutableMap<String, Int> by lazy {
            mutableMapOf(
                KeyMic0 to KeyIndex0,
                KeyMic1 to KeyIndex1,
                KeyMic2 to KeyIndex2,
                KeyMic3 to KeyIndex3,
                KeyMic4 to KeyIndex4,
                KeyMic5 to KeyIndex5,
                KeyMic6 to KeyIndex6,
                KeyMic7 to KeyIndex7
            )
        }
    }
}
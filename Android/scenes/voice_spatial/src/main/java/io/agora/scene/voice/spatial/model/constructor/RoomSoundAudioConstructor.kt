package io.agora.scene.voice.spatial.model.constructor

import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.model.SoundAudioBean

/**
 * @author create by zhangwei03
 */
object RoomSoundAudioConstructor {

    private const val BASE_URL = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemochat/aisound"

    private const val CreateCommonRoom = "/01CreateRoomCommonChatroom%1\$s"
    private const val CreateSpatialRoom = "/02CeateRoomSpaticalChatroom%1\$s"

    private const val CreateCommonRoomSoundId = 100
    private const val CreateSpatialRoomSoundId = 200

    /**
     * 新房间创建欢迎语料
     */
    val createRoomSoundAudioMap: Map<Int, List<SoundAudioBean>> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        mutableMapOf(
            ConfigConstants.RoomType.Common_Chatroom to
                    mutableListOf(
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateCommonRoomSoundId + 1,
                            getCreateCommonRoomUrl("/CN/01-01-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateCommonRoomSoundId + 2,
                            getCreateCommonRoomUrl("/CN/01-02-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBoth,
                            CreateCommonRoomSoundId + 3,
                            getCreateCommonRoomUrl("/CN/01-03-B&R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateCommonRoomSoundId + 4,
                            getCreateCommonRoomUrl("/CN/01-04-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateCommonRoomSoundId + 5,
                            getCreateCommonRoomUrl("/CN/01-05-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateCommonRoomSoundId + 6,
                            getCreateCommonRoomUrl("/CN/01-06-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateCommonRoomSoundId + 7,
                            getCreateCommonRoomUrl("/CN/01-07-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateCommonRoomSoundId + 8,
                            getCreateCommonRoomUrl("/CN/01-08-B-CN.wav")
                        )
                    ),
            ConfigConstants.RoomType.Spatial_Chatroom to
                    mutableListOf(
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 1,
                            getCreateSpatialRoomUrl("/CN/02-01-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 2,
                            getCreateSpatialRoomUrl("/CN/02-02-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBoth,
                            CreateSpatialRoomSoundId + 3,
                            getCreateSpatialRoomUrl("/CN/02-03-B&R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 4,
                            getCreateSpatialRoomUrl("/CN/02-04-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 5,
                            getCreateSpatialRoomUrl("/CN/02-05-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 6,
                            getCreateSpatialRoomUrl("/CN/02-06-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 7,
                            getCreateSpatialRoomUrl("/CN/02-07-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 8,
                            getCreateSpatialRoomUrl("/CN/02-08-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 9,
                            getCreateSpatialRoomUrl("/CN/02-09-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 10,
                            getCreateSpatialRoomUrl("/CN/02-10-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 11,
                            getCreateSpatialRoomUrl("/CN/02-11-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 12,
                            getCreateSpatialRoomUrl("/CN/02-12-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 13,
                            getCreateSpatialRoomUrl("/CN/02-13-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 14,
                            getCreateSpatialRoomUrl("/CN/02-14-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 15,
                            getCreateSpatialRoomUrl("/CN/02-15-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 16,
                            getCreateSpatialRoomUrl("/CN/02-16-B-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotRed,
                            CreateSpatialRoomSoundId + 17,
                            getCreateSpatialRoomUrl("/CN/02-17-R-CN.wav")
                        ),
                        SoundAudioBean(
                            ConfigConstants.BotSpeaker.BotBlue,
                            CreateSpatialRoomSoundId + 18,
                            getCreateSpatialRoomUrl("/CN/02-18-B-CN.wav")
                        )
                    )
        )
    }

    private fun getCreateCommonRoomUrl(audioPath: String): String {
        return BASE_URL + String.format(CreateCommonRoom, audioPath)
    }

    private fun getCreateSpatialRoomUrl(audioPath: String): String {
        return BASE_URL + String.format(CreateSpatialRoom, audioPath)
    }

}
package io.agora.scene.voice.model.constructor

import android.content.Context
import io.agora.scene.voice.R
import io.agora.scene.voice.model.BotMicInfoBean
import io.agora.scene.voice.model.MicManagerBean
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.scene.voice.global.ConfigConstants

internal object RoomMicConstructor {

    fun builderDefault2dMicList(): MutableList<VoiceMicInfoModel> {
        return mutableListOf(
            VoiceMicInfoModel(micIndex = 0),
            VoiceMicInfoModel(micIndex = 1),
            VoiceMicInfoModel(micIndex = 2),
            VoiceMicInfoModel(micIndex = 3),
            VoiceMicInfoModel(micIndex = 4),
            VoiceMicInfoModel(micIndex = 5)
        )
    }

    fun builderDefault2dBotMicList(context: Context, isUserBot: Boolean = false): MutableList<BotMicInfoBean> {
        val blueBot = VoiceMicInfoModel(
            micIndex = 6,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolumeType = ConfigConstants.VolumeType.Volume_None,
            member = VoiceMemberModel().apply {
                nickName = context.getString(R.string.voice_chatroom_agora_blue)
                portrait = "voice_icon_room_blue_robot"
            }
        )
        val redBot = VoiceMicInfoModel(
            micIndex = 7,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolumeType = ConfigConstants.VolumeType.Volume_None,
            member = VoiceMemberModel().apply {
                nickName = context.getString(R.string.voice_chatroom_agora_red)
                portrait = "voice_icon_room_red_robot"
            }
        )
        return mutableListOf(BotMicInfoBean(blueBot, redBot))
    }

    fun builderDefault3dMicMap(context: Context, isUserBot: Boolean = false): Map<Int, VoiceMicInfoModel> {
        return mutableMapOf(
            ConfigConstants.MicConstant.KeyIndex0 to VoiceMicInfoModel(micIndex = 0),
            ConfigConstants.MicConstant.KeyIndex1 to VoiceMicInfoModel(micIndex = 1),
            ConfigConstants.MicConstant.KeyIndex2 to VoiceMicInfoModel(micIndex = 5),
            ConfigConstants.MicConstant.KeyIndex3 to VoiceMicInfoModel(micIndex = 6),
            // mic4 middle seat
            ConfigConstants.MicConstant.KeyIndex4 to VoiceMicInfoModel(micIndex = 4),
            ConfigConstants.MicConstant.KeyIndex5 to VoiceMicInfoModel(
                micIndex = 2,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolumeType = ConfigConstants.VolumeType.Volume_None,
                member = VoiceMemberModel().apply {
                    nickName = context.getString(R.string.voice_chatroom_agora_blue)
                    portrait = "voice_icon_room_blue_robot"
                }
            ),
            ConfigConstants.MicConstant.KeyIndex6 to VoiceMicInfoModel(
                micIndex = 3,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolumeType = ConfigConstants.VolumeType.Volume_None,
                member = VoiceMemberModel().apply {
                    nickName = context.getString(R.string.voice_chatroom_agora_red)
                    portrait = "voice_icon_room_red_robot"
                }
            ),
        )
    }

    /**
     * Owner clicks mic management
     */
    fun builderOwnerMicMangerList(
        context: Context, micInfo: VoiceMicInfoModel, isMyself: Boolean
    ): MutableList<MicManagerBean> {
        return when (micInfo.micStatus) {
            // Normal
            MicStatus.Normal -> {
                if (isMyself) {
                    if(micInfo.member?.micStatus == 0) {
                        mutableListOf(MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnMute))
                    } else{
                        mutableListOf(MicManagerBean(context.getString(R.string.voice_room_mute), true, MicClickAction.Mute))
                    }
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.voice_room_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.voice_room_mute), true, MicClickAction.ForbidMic),
                        MicManagerBean(context.getString(R.string.voice_room_block), true, MicClickAction.Lock)
                    )
                }
            }
            // Muted
            MicStatus.Mute -> {
                if (isMyself) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnMute)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.voice_room_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnForbidMic),
                        MicManagerBean(context.getString(R.string.voice_room_block), true, MicClickAction.Lock)
                    )
                }
            }
            // Forbidden: with/without user
            MicStatus.ForceMute -> {
                if (micInfo.member == null) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.voice_room_invite), true, MicClickAction.Invite),
                        MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnForbidMic),
                        MicManagerBean(context.getString(R.string.voice_room_block), true, MicClickAction.Lock)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.voice_room_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnForbidMic),
                        MicManagerBean(context.getString(R.string.voice_room_block), true, MicClickAction.Lock)
                    )
                }
            }
            // Locked
            MicStatus.Lock -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_room_invite), false, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.voice_room_mute), true, MicClickAction.ForbidMic),
                    MicManagerBean(context.getString(R.string.voice_room_unblock), true, MicClickAction.UnLock)
                )
            }
            // Locked and forbidden
            MicStatus.LockForceMute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_room_invite), false, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnForbidMic),
                    MicManagerBean(context.getString(R.string.voice_room_unblock), true, MicClickAction.UnLock)
                )
            }
            // Idle
            MicStatus.Idle -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_room_invite), true, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.voice_room_mute), true, MicClickAction.ForbidMic),
                    MicManagerBean(context.getString(R.string.voice_room_block), true, MicClickAction.Lock)
                )
            }
            else -> mutableListOf()
        }
    }

    /**
     * Guest clicks mic management
     */
    fun builderGuestMicMangerList(context: Context, micInfo: VoiceMicInfoModel): MutableList<MicManagerBean> {
        return when (micInfo.micStatus) {
            // Has user - normal
            MicStatus.Normal -> {
                mutableListOf(
                    if(micInfo.member?.micStatus == 0){
                        MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnMute)
                    }else{
                        MicManagerBean(context.getString(R.string.voice_room_mute), true, MicClickAction.Mute)
                    },
                    MicManagerBean(context.getString(R.string.voice_room_off_stage), true, MicClickAction.OffStage)
                )
            }
            // Has user - muted
            MicStatus.Mute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_room_unmute), true, MicClickAction.UnMute),
                    MicManagerBean(context.getString(R.string.voice_room_off_stage), true, MicClickAction.OffStage)
                )
            }
            // Has user - forbidden (forced mute by owner)
            MicStatus.ForceMute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_room_unmute), false, MicClickAction.UnForbidMic),
                    MicManagerBean(context.getString(R.string.voice_room_off_stage), true, MicClickAction.OffStage)
                )
            }
            // Other cases nothing
            else -> {
                mutableListOf()
            }

        }
    }
}
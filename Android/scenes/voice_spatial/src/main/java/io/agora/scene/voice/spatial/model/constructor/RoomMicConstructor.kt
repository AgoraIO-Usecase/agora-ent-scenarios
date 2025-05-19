package io.agora.scene.voice.spatial.model.constructor

import android.content.Context
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.model.BotMicInfoBean
import io.agora.scene.voice.spatial.model.MicManagerBean
import io.agora.scene.voice.spatial.model.VoiceMemberModel
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.annotation.MicClickAction
import io.agora.scene.voice.spatial.model.annotation.MicStatus

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
                nickName = context.getString(R.string.voice_spatial_agora_blue)
                portrait = "voice_icon_room_blue_robot"
            }
        )
        val redBot = VoiceMicInfoModel(
            micIndex = 7,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolumeType = ConfigConstants.VolumeType.Volume_None,
            member = VoiceMemberModel().apply {
                nickName = context.getString(R.string.voice_spatial_agora_red)
                portrait = "voice_icon_room_red_robot"
            }
        )
        return mutableListOf(BotMicInfoBean(blueBot, redBot))
    }

    fun builderDefault3dMicMap(context: Context, isUserBot: Boolean = false): Map<Int, VoiceMicInfoModel> {
        return mutableMapOf(
            // mic0 Middle seat
            ConfigConstants.MicConstant.KeyIndex0 to VoiceMicInfoModel(micIndex = 0),
            ConfigConstants.MicConstant.KeyIndex1 to VoiceMicInfoModel(micIndex = 1),
            ConfigConstants.MicConstant.KeyIndex2 to VoiceMicInfoModel(micIndex = 2),
            ConfigConstants.MicConstant.KeyIndex3 to VoiceMicInfoModel(
                micIndex = 3,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolumeType = ConfigConstants.VolumeType.Volume_None,
                member = VoiceMemberModel().apply {
                    nickName = context.getString(R.string.voice_spatial_agora_blue)
                    portrait = "voice_icon_room_blue_robot"
                }
            ),
            ConfigConstants.MicConstant.KeyIndex4 to VoiceMicInfoModel(micIndex = 4),
            ConfigConstants.MicConstant.KeyIndex5 to VoiceMicInfoModel(micIndex = 5),
            ConfigConstants.MicConstant.KeyIndex6 to VoiceMicInfoModel(
                micIndex = 6,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolumeType = ConfigConstants.VolumeType.Volume_None,
                member = VoiceMemberModel().apply {
                    nickName = context.getString(R.string.voice_spatial_agora_red)
                    portrait = "voice_icon_room_red_robot"
                }
            ),
        )
    }

    /**
     * Host click seat management
     */
    fun builderOwnerMicMangerList(
        context: Context, micInfo: VoiceMicInfoModel, isMyself: Boolean
    ): MutableList<MicManagerBean> {
        var temp = mutableListOf<MicManagerBean>()
        if (isMyself) { // Self as one action
            if (micInfo.member?.micStatus == MicStatus.Normal) {
                temp.add(
                    MicManagerBean(context.getString(R.string.voice_spatial_mute), true, MicClickAction.Mute)
                )
            } else {
                temp.add(
                    MicManagerBean(context.getString(R.string.voice_spatial_unmute), true, MicClickAction.UnMute)
                )
            }
            return temp
        }
        // Non-self as one action
        // First item: Invite, kick out
        if (micInfo.member == null) { // No one: Invite, cannot invite if locked
            when (micInfo.micStatus) {
                MicStatus.Lock,
                MicStatus.LockForceMute, -> {
                    temp.add(
                        MicManagerBean(context.getString(R.string.voice_spatial_room_invite), false, MicClickAction.Invite),
                    )
                }
                else -> {
                    temp.add(
                        MicManagerBean(context.getString(R.string.voice_spatial_room_invite), true, MicClickAction.Invite),
                    )
                }
            }
        } else {// Someone: kick out
            temp.add(
                MicManagerBean(context.getString(R.string.voice_spatial_kickoff), true, MicClickAction.KickOff)
            )
        }
        // Second item: whether to mute
        when (micInfo.micStatus) {
            MicStatus.Mute,
            MicStatus.ForceMute,
            MicStatus.LockForceMute, -> {
                temp.add(
                    MicManagerBean(context.getString(R.string.voice_spatial_unmute), true, MicClickAction.UnForbidMic)
                )
            }
            else -> {
                temp.add(
                    MicManagerBean(context.getString(R.string.voice_spatial_mute), true, MicClickAction.ForbidMic),
                )
            }
        }
        // Third item: whether to lock
        when (micInfo.micStatus) {
            MicStatus.Lock,
            MicStatus.LockForceMute, -> {
                temp.add(
                    MicManagerBean(context.getString(R.string.voice_spatial_unblock), true, MicClickAction.UnLock)
                )
            }
            else -> {
                temp.add(
                    MicManagerBean(context.getString(R.string.voice_spatial_block), true, MicClickAction.Lock)
                )
            }
        }
        return temp
    }

    /**
     * Guest click seat management
     */
    fun builderGuestMicMangerList(context: Context, micInfo: VoiceMicInfoModel): MutableList<MicManagerBean> {
        return when (micInfo.member?.micStatus) {
            MicStatus.Normal -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_spatial_off_stage), true, MicClickAction.OffStage),
                    MicManagerBean(context.getString(R.string.voice_spatial_mute), true, MicClickAction.Mute)
                )
            }
            MicStatus.Mute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_spatial_off_stage), true, MicClickAction.OffStage),
                    MicManagerBean(context.getString(R.string.voice_spatial_unmute), true, MicClickAction.UnMute)
                )
            }
            else -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.voice_spatial_off_stage), true, MicClickAction.OffStage),
                    MicManagerBean(context.getString(R.string.voice_spatial_mute), true, MicClickAction.Mute)
                )
            }
        }
    }
}
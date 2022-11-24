package io.agora.scene.voice.bean

import androidx.annotation.DrawableRes
import io.agora.voice.buddy.config.ConfigConstants

/**
 * 最佳音效介绍
 */
data class SoundSelectionBean constructor(
    val soundSelectionType: Int = ConfigConstants.SoundSelection.Social_Chat,
    val index: Int = 0,
    val soundName: String = "",
    val soundIntroduce: String = "",
    var isCurrentUsing: Boolean = false,
    val customer: List<CustomerUsageBean>? = null
) :BaseRoomBean

data class CustomerUsageBean constructor(
    val name: String? = "",
    @DrawableRes val avatar: Int = 0
) : BaseRoomBean
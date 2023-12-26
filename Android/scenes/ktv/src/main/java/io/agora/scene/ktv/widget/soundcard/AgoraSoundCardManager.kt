package io.agora.scene.ktv.widget.soundcard

import androidx.annotation.DrawableRes

data class PresetSoundModel constructor(
    val type: AgoraPresetSound,
    val name: String,
    val tips: String,
    @DrawableRes val resId: Int
) {
    override fun toString(): String {
        return name
    }
}

enum class AgoraPresetSound constructor(
    val presetValue: Int,
    val gainValue: Float,
    val gender: Int,
    val effect: Int,
) {
    Close(-1,-1f,-1,-1),
    Sound1001(4,1f,0,0),
    Sound1002(4,1f,0,1),
    Sound1003(4,1f,1,0),
    Sound1004(4,1f,1,1),
    Sound2001(4,1f,0,2),
    Sound2002(4,1f,1,2),
    Sound2003(4,1f,0,3),
    Sound2004(4,1f,1,3),
    Sound2005(4,1f,0,4),
    Sound2006(4,1f,1,4)
}
package io.agora.scene.cantata.ui.dialog

import androidx.annotation.DrawableRes

class EffectVoiceBean constructor(var id: Int, @field:DrawableRes var resId: Int, var title: String) {
    var isSelect = false
        private set

    fun setId(id: Int): EffectVoiceBean {
        this.id = id
        return this
    }

    fun setResId(resId: Int): EffectVoiceBean {
        this.resId = resId
        return this
    }

    fun setTitle(title: String): EffectVoiceBean {
        this.title = title
        return this
    }

    fun setSelect(select: Boolean): EffectVoiceBean {
        isSelect = select
        return this
    }
}
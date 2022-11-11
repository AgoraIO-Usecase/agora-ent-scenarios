package io.agora.voice.rtckit.internal.base

/**
 * @author create by zhangwei03
 *
 * AI 降噪管理引擎
 */
internal abstract class RtcBaseDeNoiseEngine<T> : RtcBaseEngine<T>() {

    /**
     * 关闭降噪
     */
    abstract fun closeDeNoise():Boolean

    /**
     * 中降噪
     */
    abstract fun openMediumDeNoise():Boolean

    /**
     * 高降噪
     */
    abstract fun openHeightDeNoise():Boolean
}
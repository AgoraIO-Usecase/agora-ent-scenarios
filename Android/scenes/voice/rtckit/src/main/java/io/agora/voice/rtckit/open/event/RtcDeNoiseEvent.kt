package io.agora.voice.rtckit.open.event

/**
 * @author create by zhangwei03
 *
 * AI 降噪操作事件
 */
sealed class RtcDeNoiseEvent {

    // 关闭降噪
    class CloseEvent constructor(): RtcDeNoiseEvent()
    // 中降噪
    class MediumEvent constructor(): RtcDeNoiseEvent()
    // 高降噪
    class HighEvent constructor(): RtcDeNoiseEvent()

}

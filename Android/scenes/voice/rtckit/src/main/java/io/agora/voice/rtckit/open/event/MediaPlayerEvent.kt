package io.agora.voice.rtckit.open.event

import io.agora.voice.buddy.config.ConfigConstants

/**
 * @author create by zhangwei03
 *
 * 音效操作事件
 */
sealed class MediaPlayerEvent {
    class OpenEvent constructor(
        val url: String,
        val startPos: Long = 0,
        val soundSpeaker: Int = ConfigConstants.BotSpeaker.BotBlue
    ) :
        MediaPlayerEvent()

    class PlayEvent constructor() : MediaPlayerEvent()

    class PauseEvent constructor() : MediaPlayerEvent()

    class StopEvent constructor() : MediaPlayerEvent()

    class ResumeEvent constructor() : MediaPlayerEvent()

    class ResetEvent constructor() : MediaPlayerEvent()

    class AdjustPlayoutVolumeEvent constructor(val volume: Int) : MediaPlayerEvent()
}

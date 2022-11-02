package io.agora.voice.rtckit.annotation

import androidx.annotation.IntDef

@IntDef(
    RtcNetWorkQuality.QualityUnknown, RtcNetWorkQuality.QualityExcellent, RtcNetWorkQuality.QualityGood,
    RtcNetWorkQuality.QualityPoor, RtcNetWorkQuality.QualityBad, RtcNetWorkQuality.QualityVBad,
    RtcNetWorkQuality.QualityDown
)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class RtcNetWorkQuality {

    companion object {

        /** The network quality is unknown. */
        const val QualityUnknown = 0

        /**  The network quality is excellent. */
        const val QualityExcellent = 1

        /** The network quality is quite good, but the bitrate may be slightly lower than excellent. */
        const val QualityGood = 2

        /** Users can feel the communication slightly impaired. */
        const val QualityPoor = 3

        /** Users can communicate only not very smoothly. */
        const val QualityBad = 4

        /** The network quality is so bad that users can hardly communicate. */
        const val QualityVBad = 5

        /** The network is disconnected and users cannot communicate at all. */
        const val QualityDown = 6
    }
}
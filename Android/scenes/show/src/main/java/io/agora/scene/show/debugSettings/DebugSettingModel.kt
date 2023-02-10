package io.agora.scene.show.debugSettings

import io.agora.rtc2.video.ColorEnhanceOptions

data class DebugSettingModel(
    var pvcEnabled: Boolean = true,
    var autoFocusFaceModeEnabled: Boolean = true,
    var exposurePositionX: Float? = null,
    var exposurePositionY: Float? = null,
    var cameraSelect: Int? = null,
    var videoFullrangeExt: Int? = null,
    var matrixCoefficientsExt: Int? = null,
    var enableHWEncoder: Boolean = true,
    var codecType: Int = 3,     // 2 -> h264, 3 -> h265
    var mirrorMode: Boolean = false,
    var fitMode: Int = 0,       // 0 -> hidden, 1 -> fix
    var colorEnhance: Boolean = false,
    var dark: Boolean = false,
    var noise: Boolean = false
)
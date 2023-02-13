package io.agora.scene.show.debugSettings

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
    var renderMode: Int = 0,       // 0 -> hidden, 1 -> fix
    var colorEnhance: Boolean = false,
    var dark: Boolean = false,
    var noise: Boolean = false,
    var srEnabled: Boolean = false,
    var srType: Double = 1.0         // 1 -> 6, 1.33 -> 7, 1.5 -> 8, 2 -> 3
)
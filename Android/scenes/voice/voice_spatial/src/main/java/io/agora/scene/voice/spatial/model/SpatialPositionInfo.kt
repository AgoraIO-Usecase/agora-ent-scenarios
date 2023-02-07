package io.agora.scene.voice.spatial.model

data class SeatPositionInfo(
    val pos: Array<Float>,
    val forward: Array<Float>,
    // 右手，默认forward顺时针偏转90°
    val right: Array<Float>,
    // 头顶，默认朝上，z轴方向
    val up: Array<Float>
)

data class DataStreamInfo(
    val code: Int, // SeatPositionInfo信息定做101
    val message: String, // json串，判断code是哪种数据再做相应的解析，例如是101则用SeatPositionInfo解析json
)
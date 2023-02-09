package io.agora.scene.voice.spatial.model

data class SeatPositionInfo(
    val uid: Int,
    val forward: Array<Float>,
    val x: Float,
    val y: Float,
    val angle: Float
)

data class DataStreamInfo(
    val code: Int, // SeatPositionInfo信息定做101
    val message: String, // json串，判断code是哪种数据再做相应的解析，例如是101则用SeatPositionInfo解析json
)
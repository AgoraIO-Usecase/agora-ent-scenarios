package io.agora.scene.voice.spatial.model

data class SeatPositionInfo(
    val uid: Int,
    val forward: FloatArray,
    val x: Float,
    val y: Float,
    val angle: Float
)

data class DataStreamInfo(
    val code: Int, // Code 101 represents SeatPositionInfo data
    val message: String // JSON string, parse according to code type. For example, if code is 101, parse as SeatPositionInfo JSON
)
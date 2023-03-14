package io.agora.scene.voice.spatial.model

interface OnItemMoveListener<T> {
    fun onItemMove(data: T, position: SeatPositionInfo, viewType: Long) {}
}
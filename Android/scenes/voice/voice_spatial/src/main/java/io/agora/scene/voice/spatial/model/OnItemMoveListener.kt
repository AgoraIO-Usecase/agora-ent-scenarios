package io.agora.scene.voice.spatial.model
import android.view.View

interface OnItemMoveListener<T> {
    fun onItemMove(data: T, position: SeatPositionInfo, viewType: Long) {}
}
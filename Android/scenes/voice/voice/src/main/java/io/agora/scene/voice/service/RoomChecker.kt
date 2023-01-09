package io.agora.scene.voice.service

import android.content.Context
import android.os.SystemClock
import androidx.collection.ArraySet
import androidx.collection.arraySetOf

class RoomChecker(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("VoiceRoom", Context.MODE_PRIVATE)
    private val keyRoomSet = "VoiceRoomSet"
    private val keyLastUpdateTime = "VoiceLastUpdateTime"
    private val expireDuration = 2 * 60 * 1000 // 2min

    fun joinRoom(roomId: String): Boolean {
        val lastUpdateTime =
            sharedPreferences.getLong(keyLastUpdateTime, SystemClock.elapsedRealtime())
        if ((SystemClock.elapsedRealtime() - lastUpdateTime) > expireDuration) {
            sharedPreferences.edit().putStringSet(keyRoomSet, arraySetOf()).commit()
        }
        val stringSet = sharedPreferences.getStringSet(keyRoomSet, arraySetOf())
        if (stringSet?.contains(roomId) == true) {
            return false
        }
        val newSet = ArraySet(stringSet)
        newSet.add(roomId)
        sharedPreferences.edit()
            .putStringSet(keyRoomSet, newSet)
            .putLong(keyLastUpdateTime, SystemClock.elapsedRealtime())
            .commit()
        return true
    }

    fun leaveRoom(roomId: String): Boolean{
        val stringSet = sharedPreferences.getStringSet(keyRoomSet, arraySetOf())
        if (stringSet?.contains(roomId) == true) {
            val newSet = ArraySet(stringSet)
            newSet.remove(roomId)
            sharedPreferences.edit()
                .putStringSet(keyRoomSet, newSet)
                .putLong(keyLastUpdateTime, SystemClock.elapsedRealtime())
                .commit()
            return true
        }
        return false
    }
}
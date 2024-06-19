package io.agora.scene.playzone.service

import android.content.Context

class PlayZoneSyncManagerServiceImp constructor(private val cxt: Context) : PlayZoneServiceProtocol {

    companion object {
        private const val TAG = "Play_Zone_Service_LOG"
        private const val kSceneId = "scene_play_zone_4.10.2"
    }
}
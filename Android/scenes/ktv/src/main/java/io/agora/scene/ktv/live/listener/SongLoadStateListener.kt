package io.agora.scene.ktv.live.listener

import io.agora.ktvapi.MusicLoadStatus

enum class SongLoadFailReason(val value: Int) {
    MUSIC_DOWNLOAD_FAIL(0),
    CANCELED(1),
    UNKNOW(2),
}

interface SongLoadStateListener {
    /**
     * song load success
     * @param songCode Song code, which is consistent with the songCode passed to loadMusic.
     * @param lyricUrl Lyrics url.
     */
    fun onMusicLoadSuccess(songCode: String,musicUri:String, lyricUrl: String)

    /**
     * song load fail
     * @param songCode Song code of failed load.
     * @param reason Reason for song load failure.
     */
    fun onMusicLoadFail(songCode: String, reason: SongLoadFailReason)

    /**
     * song load progress
     * @param songCode song code
     * @param percent song load progress
     * @param status status of song loading
     * @param lyricUrl lyric url
     */
    fun onMusicLoadProgress(songCode: String, percent: Int, status: MusicLoadStatus, lyricUrl: String?)
}
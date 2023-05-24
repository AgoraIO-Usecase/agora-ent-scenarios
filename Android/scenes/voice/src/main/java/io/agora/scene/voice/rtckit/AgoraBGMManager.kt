package io.agora.scene.voice.rtckit

import android.util.Log
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.musiccontentcenter.*
import io.agora.rtc2.RtcEngineEx

class AgoraBGMManager(
    private val mRtcEngine: RtcEngineEx,
    private val mAppId: String,
    private val mUid: Int,
    private val mRtmToken: String
) : IMediaPlayerObserver, IMusicContentCenterEventHandler {

    private val TAG: String = "BGM_MANAGER_LOG"

    var remoteVolume: Int = 40 // 远端音频
    var mpkPlayerVolume: Int = 50
    var mpkPublishVolume: Int = 50

    var selectSongCode: Long = 0

    private val musicCollectionCallbackMap = mutableMapOf<String, (list: Array<out Music>?) -> Unit>()

    private val loadMusicCallbackMap = mutableMapOf<String, (
        songCode: Long,
        percent: Int,
        status: Int,
        msg: String?,
        lyricUrl: String?
    ) -> Unit>() // (songNo, callback)

    private val mMusicCenter: IAgoraMusicContentCenter = IAgoraMusicContentCenter.create(mRtcEngine)
    private val mPlayer: IAgoraMusicPlayer = mMusicCenter.createMusicPlayer()

    init {
        val contentCenterConfiguration = MusicContentCenterConfiguration()
        contentCenterConfiguration.appId = mAppId
        contentCenterConfiguration.mccUid = mUid.toLong()
        contentCenterConfiguration.token = mRtmToken
        contentCenterConfiguration.maxCacheSize = 10

        mMusicCenter.initialize(contentCenterConfiguration)
        mPlayer.adjustPlayoutVolume(mpkPlayerVolume)
        mPlayer.adjustPublishSignalVolume(mpkPublishVolume)

        mPlayer.registerPlayerObserver(this)
        mMusicCenter.registerEventHandler(this)
    }

    fun fetchBGMList(complete: (list: Array<out Music>?) -> Unit) {
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        mMusicCenter.getMusicCollectionByMusicChartId(0, 0, 20, jsonOption)?.let { requestId ->
            musicCollectionCallbackMap[requestId] = complete
        }
    }

    fun loadAndAutoPlay(songCode: Long, complete: (Boolean) -> Unit) {
        Log.d(TAG, "loadMusic: $songCode")
        preLoadMusic() {song, percent, status, msg, lrcUrl ->
            mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
            mPlayer.open(songCode, 0)
        }
    }

    fun playerPause() {
        mPlayer.pause()
    }

    fun setSingerOn(isOn: Boolean) {
        mPlayer.selectAudioTrack(if (isOn) 0 else 1)
    }

    fun setVolume(value: Int) {
        mPlayer.adjustPublishSignalVolume(value)
        mPlayer.adjustPlayoutVolume(value)
    }

    private fun preLoadMusic(complete: (songCode: Long, percent: Int, status: Int, msg: String?, lyricUrl: String?) -> Unit) {
        val ret = mMusicCenter.isPreloaded(selectSongCode)
        if (ret == 0) {
            loadMusicCallbackMap.remove(selectSongCode.toString())
            complete(selectSongCode, 100, 0, null, null)
            return
        }

        val retPreload = mMusicCenter.preload(selectSongCode, null)
        if (retPreload != 0) {
            Log.e(TAG, "preLoadMusic failed: $retPreload")
            loadMusicCallbackMap.remove(selectSongCode.toString())
            complete(selectSongCode, 100, 1, null, null)
            return
        }
        loadMusicCallbackMap[selectSongCode.toString()] = complete
    }

    override fun onPlayerStateChanged(
        state: Constants.MediaPlayerState?,
        error: Constants.MediaPlayerError?
    ) {
        TODO("Not yet implemented")
    }

    override fun onPositionChanged(position_ms: Long) {
        TODO("Not yet implemented")
    }

    override fun onPlayerEvent(
        eventCode: Constants.MediaPlayerEvent?,
        elapsedTime: Long,
        message: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onMetaData(type: Constants.MediaPlayerMetadataType?, data: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onPlayBufferUpdated(playCachedBuffer: Long) {
        TODO("Not yet implemented")
    }

    override fun onPreloadEvent(src: String?, event: Constants.MediaPlayerPreloadEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAgoraCDNTokenWillExpire() {
        TODO("Not yet implemented")
    }

    override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {
        TODO("Not yet implemented")
    }

    override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {
        TODO("Not yet implemented")
    }

    override fun onAudioVolumeIndication(volume: Int) {
        TODO("Not yet implemented")
    }

    override fun onPreLoadEvent(
        songCode: Long,
        percent: Int,
        lyricUrl: String?,
        status: Int,
        errorCode: Int
    ) {
        val callback = loadMusicCallbackMap[songCode.toString()] ?: return
        if (status == 0 || status == 1) {
            loadMusicCallbackMap.remove(songCode.toString())
        }
        callback.invoke(songCode, percent, status, RtcEngineEx.getErrorDescription(errorCode), lyricUrl)
    }

    override fun onMusicCollectionResult(
        requestId: String?,
        page: Int,
        pageSize: Int,
        total: Int,
        list: Array<out Music>?,
        errorCode: Int
    ) {
        val id = requestId ?: return
        val callback = musicCollectionCallbackMap[id] ?: return
        musicCollectionCallbackMap.remove(id)
        callback.invoke(list)
    }

    override fun onMusicChartsResult(
        requestId: String?,
        list: Array<out MusicChartInfo>?,
        errorCode: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onLyricResult(requestId: String?, lyricUrl: String?, errorCode: Int) {
        TODO("Not yet implemented")
    }

}
package io.agora.scene.voice.rtckit

import android.util.Log
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.musiccontentcenter.*
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.RtcEngineEx

data class AgoraBGMParams (
    var isSingerOn: Boolean = true,
    var isAutoPlay: Boolean = false,
    var volume: Int = 50
){}
class AgoraBGMManager(
    private val mRtcEngine: RtcEngineEx,
    private val mAppId: String,
    private val mUid: Int,
    private val mRtmToken: String
) : IMediaPlayerObserver, IMusicContentCenterEventHandler {

    val params = AgoraBGMParams()

    var bgm: Music? = null

    private val TAG: String = "BGM_MANAGER_LOG"

    private var remoteVolume: Int = 40 // 远端音频
    private var mpkPlayerVolume: Int = 50
    private var mpkPublishVolume: Int = 50

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
        mPlayer.setLoopCount(Int.MAX_VALUE) // 单曲循环
        mPlayer.adjustPlayoutVolume(mpkPlayerVolume)
        mPlayer.adjustPublishSignalVolume(mpkPublishVolume)

        mPlayer.registerPlayerObserver(this)
        mMusicCenter.registerEventHandler(this)

        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio = true
        channelMediaOption.publishMediaPlayerId = mPlayer.mediaPlayerId
        channelMediaOption.publishMediaPlayerAudioTrack = true
        mRtcEngine.updateChannelMediaOptions(channelMediaOption)
    }

    fun fetchBGMList(complete: (list: Array<out Music>?) -> Unit) {
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        mMusicCenter.getMusicCollectionByMusicChartId(0, 0, 20, jsonOption)?.let { requestId ->
            musicCollectionCallbackMap[requestId] = complete
        }
    }

    fun loadMusic(music: Music?) {
        bgm = music
        mPlayer.stop()
        if (music == null) {
            return
        }
        Log.d(TAG, "loadMusic: ${music.songCode}, name: ${music.name}")
        preLoadMusic {songCode, percent, status, msg, lrcUrl ->
            if (bgm?.songCode == songCode) {
                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
                mPlayer.open(songCode, 0)
            }
        }
    }

    fun setAutoPlay(isPlay: Boolean) {
        params.isAutoPlay = isPlay
        if (mPlayer.state == Constants.MediaPlayerState.PLAYER_STATE_STOPPED) {
            return
        }
        if (isPlay) mPlayer.play() else mPlayer.pause()
    }

    fun setSingerOn(isOn: Boolean) {
        params.isSingerOn = isOn
        mPlayer.selectAudioTrack(if (isOn) 0 else 1)
    }

    fun setVolume(value: Int) {
        params.volume = value
        mPlayer.adjustPublishSignalVolume(value)
        mPlayer.adjustPlayoutVolume(value)
    }

    private fun preLoadMusic(complete: (songCode: Long, percent: Int, status: Int, msg: String?, lyricUrl: String?) -> Unit) {
        val target = bgm?.songCode ?: return
        val ret = mMusicCenter.isPreloaded(target)
        if (ret == 0) {
            loadMusicCallbackMap.remove(target.toString())
            complete(target, 100, 0, null, null)
            return
        }

        val retPreload = mMusicCenter.preload(target, null)
        if (retPreload != 0) {
            Log.e(TAG, "preLoadMusic failed: $retPreload")
            loadMusicCallbackMap.remove(target.toString())
            complete(target, 100, 1, null, null)
            return
        }
        loadMusicCallbackMap[target.toString()] = complete
    }

    override fun onPlayerStateChanged(
        state: Constants.MediaPlayerState?,
        error: Constants.MediaPlayerError?
    ) {
        val mediaPlayerState = state ?: return
        val mediaPlayerError = error ?: return
        Log.d(TAG, "onPlayerStateChanged called, state: $mediaPlayerState, error: $error")
        when (mediaPlayerState) {
            Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                if (params.isAutoPlay) {
                    mPlayer.play()
                }
                mPlayer.selectAudioTrack(if (params.isSingerOn) 0 else 1)
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
            }
            Constants.MediaPlayerState.PLAYER_STATE_PAUSED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
            }
            Constants.MediaPlayerState.PLAYER_STATE_STOPPED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                if (params.isAutoPlay) {
                    mPlayer.play()
                }
                mPlayer.selectAudioTrack(if (params.isSingerOn) 0 else 1)
            }
            else -> {}
        }
    }

    override fun onPositionChanged(position_ms: Long) {

    }

    override fun onPlayerEvent(
        eventCode: Constants.MediaPlayerEvent?,
        elapsedTime: Long,
        message: String?
    ) {

    }

    override fun onMetaData(type: Constants.MediaPlayerMetadataType?, data: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onPlayBufferUpdated(playCachedBuffer: Long) {

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

    }

    override fun onAudioVolumeIndication(volume: Int) {

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

    }

    override fun onLyricResult(requestId: String?, lyricUrl: String?, errorCode: Int) {

    }

}
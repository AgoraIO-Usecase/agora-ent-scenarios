package io.agora.scene.ktv.live

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.SurfaceView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.ktvapi.AudioTrackMode
import io.agora.ktvapi.IKTVApiEventHandler
import io.agora.ktvapi.ILrcView
import io.agora.ktvapi.IMusicLoadStateListener
import io.agora.ktvapi.ISwitchRoleStateListener
import io.agora.ktvapi.KTVApi
import io.agora.ktvapi.KTVApiConfig
import io.agora.ktvapi.KTVLoadMusicConfiguration
import io.agora.ktvapi.KTVLoadMusicFailReason
import io.agora.ktvapi.KTVLoadMusicMode
import io.agora.ktvapi.KTVMusicType
import io.agora.ktvapi.KTVSingRole
import io.agora.ktvapi.KTVType
import io.agora.ktvapi.MusicLoadStatus
import io.agora.ktvapi.SwitchRoleFailReason
import io.agora.ktvapi.createKTVApi
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.LogLevel
import io.agora.rtc2.DataStreamConfig
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection.CONNECTION_STATE_TYPE
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.ContentInspectModule
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.AudioModeration.moderationAudio
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.ktv.KTVLogger
import io.agora.scene.ktv.KtvCenter
import io.agora.scene.ktv.KtvCenter.rtcChorusChannelName
import io.agora.scene.ktv.R
import io.agora.scene.ktv.debugSettings.KTVDebugSettingBean
import io.agora.scene.ktv.debugSettings.KTVDebugSettingsDialog
import io.agora.scene.ktv.live.bean.JoinChorusStatus
import io.agora.scene.ktv.live.bean.LineScore
import io.agora.scene.ktv.live.bean.MusicSettingBean
import io.agora.scene.ktv.live.bean.NetWorkEvent
import io.agora.scene.ktv.live.bean.PlayerMusicStatus
import io.agora.scene.ktv.live.bean.ScoringAlgoControlModel
import io.agora.scene.ktv.live.bean.ScoringAverageModel
import io.agora.scene.ktv.live.bean.SoundCardSettingBean
import io.agora.scene.ktv.live.bean.VolumeModel
import io.agora.scene.ktv.live.fragmentdialog.MusicSettingCallback
import io.agora.scene.ktv.service.ChooseSongInputModel
import io.agora.scene.ktv.service.ChosenSongInfo
import io.agora.scene.ktv.service.KTVServiceProtocol.Companion.getImplInstance
import io.agora.scene.ktv.service.KtvServiceListenerProtocol
import io.agora.scene.ktv.service.PlayStatus
import io.agora.scene.ktv.service.RoomChoristerInfo
import io.agora.scene.ktv.service.RoomMicSeatInfo
import io.agora.scene.ktv.widget.lrcView.LrcControlView
import io.agora.scene.ktv.widget.song.SongItem
import io.agora.scene.widget.toast.CustomToast
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The type Room living view model.
 */
class RoomLivingViewModel constructor(val mRoomInfo: AUIRoomInfo) : ViewModel() {
    private val TAG = "KTV_Scene_LOG"
    private var mainHandler: Handler? = null
    private fun runOnMainThread(runnable: Runnable) {
        if (mainHandler == null) {
            mainHandler = Handler(Looper.getMainLooper())
        }
        if (Thread.currentThread() == mainHandler?.looper?.thread) {
            runnable.run()
        } else {
            mainHandler?.post(runnable)
        }
    }

    private val ktvServiceProtocol = getImplInstance()
    private lateinit var ktvApiProtocol: KTVApi

    // 房间销毁
    val roomDestroyLiveData = MutableLiveData<Boolean>()

    // 房间超时
    val roomExpireLiveData = MutableLiveData<Boolean>()

    // 房间人数
    val userCountLiveData = MutableLiveData<Int>()

    // 麦位集合
    val seatListLiveData = MutableLiveData<MutableList<RoomMicSeatInfo>>()

    // 麦位更新
    val seatUpdateLiveData = MutableLiveData<RoomMicSeatInfo>()

    // 当前用户麦位
    val localSeatInfo: RoomMicSeatInfo? get() = seatListLiveData.value?.firstOrNull { it.owner?.userId == KtvCenter.mUser.id.toString() }

    // 已选歌单
    val chosenSongListLiveData = MutableLiveData<List<ChosenSongInfo>?>()

    // 合唱列表
    private val chorusInfoList = mutableListOf<RoomChoristerInfo>()

    // 获取合唱用户
    fun getSongChorusInfo(userId: String, songCode: String): RoomChoristerInfo? {
        return chorusInfoList.firstOrNull { it.userId == userId && it.chorusSongNo == songCode }
    }

    // 音量
    val volumeLiveData = MutableLiveData<VolumeModel>()

    // 当前播放歌曲
    val songPlayingLiveData = MutableLiveData<ChosenSongInfo?>()

    // 主唱分数
    val mainSingerScoreLiveData = MutableLiveData<LineScore>()

    // rtc stream id
    var streamId = 0

    // 音乐播放状态
    val playerMusicStatusLiveData = MutableLiveData<PlayerMusicStatus>()

    // 加载音乐进度
    val loadMusicProgressLiveData = MutableLiveData<Int>()

    // 当前用户合唱状态
    val joinchorusStatusLiveData = MutableLiveData<JoinChorusStatus>()

    // 无歌词
    val noLrcLiveData = MutableLiveData<Boolean>()

    // 音乐时长
    val playerMusicOpenDurationLiveData = MutableLiveData<Long>()

    // 音乐播放完后分数
    val playerMusicPlayCompleteLiveData = MutableLiveData<ScoringAverageModel>()

    // 网络状态
    val networkStatusLiveData = MutableLiveData<NetWorkEvent>()

    // 当分难度
    val scoringAlgoControlLiveData = MutableLiveData<ScoringAlgoControlModel>()

    // 打分难度
    val scoringAlgoLiveData = MutableLiveData<Int>()

    // rtc 引擎
    private var mRtcEngine: RtcEngineEx? = null

    // 主版本的音频设置
    private val mainChannelMediaOption = ChannelMediaOptions()

    // 播放器配置
    var mMusicSetting: MusicSettingBean? = null

    // debug 配置
    var mDebugSetting: KTVDebugSettingBean? = null

    // 音效配置
    var mSoundCardSettingBean: SoundCardSettingBean? = null

    // 是否房主
    val isRoomOwner: Boolean get() = mRoomInfo.roomOwner?.userId == KtvCenter.mUser.id.toString()

    /**
     * Init.
     */
    fun initData() {
        initSettings()
        initRTCPlayer()
        initRoom()
    }

    /**
     * Release boolean.
     *
     * @return the boolean
     */
    private fun innerRelease(): Boolean {
        ktvServiceProtocol.unsubscribeListener(serviceListenerProtocol)
        KTVLogger.d(TAG, "release called")
        streamId = 0
        mRtcEngine?.let {
            ktvApiProtocol.release()
            mSoundCardSettingBean?.enable(false, true) { }
            it.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE)
            it.leaveChannel()
            RtcEngineEx.destroy()
            mRtcEngine = null
            return true
        }
        return false
    }

    val sDKBuildNum: String get() = RtcEngineEx.getSdkVersion()

    private val serviceListenerProtocol = object : KtvServiceListenerProtocol {

        private fun updateLocalEnterSeat(seatInfo: RoomMicSeatInfo) {
            if (seatInfo.owner?.userId == KtvCenter.mUser.id.toString()) {
                mRtcEngine?.let {
                    mainChannelMediaOption.publishCameraTrack = false
                    mainChannelMediaOption.publishMicrophoneTrack = !seatInfo.isAudioMuted
                    mainChannelMediaOption.enableAudioRecordingOrPlayout = true
                    mainChannelMediaOption.autoSubscribeVideo = true
                    mainChannelMediaOption.autoSubscribeAudio = true
                    mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    it.updateChannelMediaOptions(mainChannelMediaOption)
                }
            }
        }

        override fun onRoomDestroy() {
            innerRelease()
            roomDestroyLiveData.value = true
        }

        override fun onRoomExpire() {
            innerRelease()
            roomExpireLiveData.value = true
        }


        override fun onUserCountUpdate(userCount: Int) {
            userCountLiveData.value = userCount
        }

        override fun onMicSeatSnapshot(seatMap: Map<Int, RoomMicSeatInfo>) {
            val seatList = mutableListOf<RoomMicSeatInfo>()
            seatMap.values.forEach { roomMicSeatInfo ->
                seatList.add(roomMicSeatInfo)
            }
            seatList.sortBy { it.seatIndex }
            seatListLiveData.value = seatList

            // fix ENT-1826 主唱杀进程再次进入房间，rtc 角色不对导致不能同步歌词
            seatList.firstOrNull { it.owner?.userId == KtvCenter.mUser.id.toString() }?.let { originSeat ->
                updateLocalEnterSeat(originSeat)
            }

        }

        override fun onUserSeatUpdate(seatInfo: RoomMicSeatInfo) {
            seatUpdateLiveData.value = seatInfo
        }

        override fun onUserEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
            val originSeat = seatListLiveData.value?.firstOrNull { it.seatIndex == seatIndex } ?: return
            originSeat.owner = user
            seatListLiveData.value?.set(seatIndex, originSeat)
            if (user.userId == KtvCenter.mUser.id.toString()) {
                updateLocalEnterSeat(originSeat)
            }
        }

        override fun onUserLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
            val originSeat = seatListLiveData.value?.firstOrNull { it.seatIndex == seatIndex } ?: return
            originSeat.owner = user
            originSeat.owner = AUIUserThumbnailInfo()
            seatListLiveData.value?.set(seatIndex, originSeat)
            if (user.userId == KtvCenter.mUser.id.toString()) {
                mRtcEngine?.let {
                    mainChannelMediaOption.publishCameraTrack = false
                    mainChannelMediaOption.publishMicrophoneTrack = false
                    mainChannelMediaOption.enableAudioRecordingOrPlayout = true
                    mainChannelMediaOption.autoSubscribeVideo = true
                    mainChannelMediaOption.autoSubscribeAudio = true
                    mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                    it.updateChannelMediaOptions(mainChannelMediaOption)
                }
                ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null)
            }
        }

        override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
            val originSeat = seatListLiveData.value?.firstOrNull { it.seatIndex == seatIndex } ?: return
            originSeat.isAudioMuted = isMute
            seatListLiveData.value?.set(seatIndex, originSeat)
            if (originSeat.owner?.userId == KtvCenter.mUser.id.toString()) {// 开关麦克风
                toggleSelfAudioBySign(!isMute)
            }
        }

        override fun onSeatVideoMute(seatIndex: Int, isMute: Boolean) {
            val originSeat = seatListLiveData.value?.firstOrNull { it.seatIndex == seatIndex } ?: return
            originSeat.isVideoMuted = isMute
            seatListLiveData.value?.set(seatIndex, originSeat)
            if (originSeat.owner?.userId == KtvCenter.mUser.id.toString()) {// 开关摄像头
                toggleSelfVideoBySign(!isMute)
            }
        }

        override fun onChoristerDidEnter(chorister: RoomChoristerInfo) {
            val lastChorusNum = chorusInfoList.size
            chorusInfoList.removeIf { it.userId == chorister.userId }
            chorusInfoList.add(chorister)
            soloSingerJoinChorusMode(lastChorusNum)

            val originSeat = seatListLiveData.value?.firstOrNull { it.owner?.userId == chorister.userId }
            if (originSeat != null) {
                onUserSeatUpdate(originSeat)
            }
            val songPlaying = songPlayingLiveData.getValue() ?: return
            if (chorister.userId == KtvCenter.mUser.id.toString() && songPlaying.songNo == chorister.chorusSongNo) {
                // Fix for ENT-2031:
                // When clicking to join the chorus:
                // 1. Load the song.
                // 2. Call `rtm joinChorus`.
                // 3. Switch the KTV API role to "chorus".
                //
                // Note: When an audience member joins the chorus, the sequence of publishing RTM messages and handling `onMetaData` may not be guaranteed.
                // Thus, after successfully joining the chorus, it's necessary to update the role to "chorus".
                innerRtmOnSelfJoinedChorus()
            }
        }

        override fun onChoristerDidLeave(chorister: RoomChoristerInfo) {
            val lastChorusNum = chorusInfoList.size
            chorusInfoList.removeIf { it.userId == chorister.userId }
            soloSingerJoinChorusMode(lastChorusNum)

            val originSeat = seatListLiveData.value?.firstOrNull { it.owner?.userId == chorister.userId }
            if (originSeat != null) {
                onUserSeatUpdate(originSeat)
            }
            val songPlaying = songPlayingLiveData.getValue() ?: return
            if (chorister.userId == KtvCenter.mUser.id.toString() && songPlaying.songNo == chorister.chorusSongNo) {
                joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
            }
        }

        override fun onChosenSongListDidChanged(chosenSongList: List<ChosenSongInfo>) {
            songPlayingLiveData.value?.let { currentSong ->
                val firstSong = chosenSongList.firstOrNull()
                if (currentSong.owner?.userId == KtvCenter.mUser.id.toString() && firstSong?.songNo != currentSong.songNo) {
                    if (loadingMusic.get()){ // 正在加载前一首歌曲，则移除（musicCenter 是串行的）,
                        KTVLogger.d(TAG, "RoomLivingViewModel remove music: ${currentSong.songNo}")
                        ktvApiProtocol.removeMusic(currentSong.songNo.toLong())
                    }
                }
            }
            chosenSongListLiveData.value = chosenSongList
            onUpdateAllChooseSongs(chosenSongList)
        }
    }

    private fun initRoom() {
        ktvServiceProtocol.subscribeListener(serviceListenerProtocol)
        // 获取已点歌单
        ktvServiceProtocol.getChosenSongList { error, songList ->
            if (error == null) {
                chosenSongListLiveData.value = songList
            }
        }
    }

    /**
     * Exit room
     *
     */
    fun exitRoom() {
        KTVLogger.d(TAG, "RoomLivingViewModel.exitRoom() called")
        ktvServiceProtocol.leaveRoom { e: Exception? ->
            if (e == null) { // success
                KTVLogger.d(TAG, "RoomLivingViewModel.exitRoom() success")
            } else { // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed: $e")
                e.message?.let { error ->
                    CustomToast.show(error, Toast.LENGTH_SHORT)
                }
            }
        }
        innerRelease()
    }

    /**
     * 上麦
     *
     * @param onSeatIndex the on seat index
     */
    fun enterSeat(onSeatIndex: Int) {
        KTVLogger.d(TAG, "RoomLivingViewModel.haveSeat() called: $onSeatIndex")
        ktvServiceProtocol.enterSeat(onSeatIndex) { error ->
            if (error != null) { // failure
                CustomToast.show(R.string.ktv_enter_seat_failed, error.message ?: "")
            }
        }
    }

    /**
     * 离开麦位
     *
     * @param seatModel the seat model
     */
    fun leaveSeat(seatModel: RoomMicSeatInfo) {
        KTVLogger.d(TAG, "RoomLivingViewModel.leaveSeat() called $seatModel")
        ktvServiceProtocol.leaveSeat() { error ->
            if (error != null) { // failure
                CustomToast.show(R.string.ktv_leave_seat_failed, error.message ?: "")
            }
        }
    }

    /**
     * Kick seat
     *
     * @param seatModel
     */
    fun kickSeat(seatModel: RoomMicSeatInfo) {
        KTVLogger.d(TAG, "RoomLivingViewModel.kickSeat() called $seatModel")
        ktvServiceProtocol.kickSeat(seatModel.seatIndex) { error ->
            if (error != null) { // failure
                CustomToast.show(R.string.ktv_kick_seat_failed, error.message ?: "")
            }
        }
    }

    /**
     * Is camera opened
     */
    private var isCameraOpened = false

    /**
     * Update seat video mute status
     *
     * @param mute
     */
    fun updateSeatVideoMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        KTVLogger.d(TAG, "RoomLivingViewModel.updateSeatVideoMuteStatus() called mute：$mute")
        ktvServiceProtocol.updateSeatVideoMuteStatus(mute) { error ->
            if (error != null) { // failure
                error.message?.let {
                    CustomToast.show(it, Toast.LENGTH_SHORT)
                }
            }
            completion.invoke(error)
        }
    }

    // 根据信令开关摄像头
    private fun toggleSelfVideoBySign(isOpen: Boolean) {
        KTVLogger.d(TAG, "RoomLivingViewModel.toggleSelfVideoBySign() isOpen:$isOpen")
        isCameraOpened = isOpen
        mRtcEngine?.enableLocalVideo(isOpen)
        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.publishCameraTrack = isOpen
        mRtcEngine?.updateChannelMediaOptions(channelMediaOption)
    }

    // 根据信令开关麦克风
    private fun toggleSelfAudioBySign(isOpen: Boolean) {
        KTVLogger.d(TAG, "RoomLivingViewModel.toggleSelfAudioBySign() isOpen:$isOpen")
        ktvApiProtocol.muteMic(!isOpen)
        if (!isOpen && mMusicSetting?.mEarBackEnable == true) {
            mRtcEngine?.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE)
        } else if (isOpen && mMusicSetting?.mEarBackEnable == true) {
            mRtcEngine?.enableInEarMonitoring(true, Constants.EAR_MONITORING_FILTER_NONE)
        }
        if (isOpen) {
            KTVLogger.d(TAG, "RoomLivingViewModel unmute! setMicVolume: $micOldVolume")
            mRtcEngine?.adjustRecordingSignalVolume(micOldVolume)
        }
    }

    /**
     * Update seat audio mute status
     *
     * @param mute
     */
    fun updateSeatAudioMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        KTVLogger.d(TAG, "RoomLivingViewModel.updateSeatAudioMuteStatus() called mute：$mute")
        ktvServiceProtocol.updateSeatAudioMuteStatus(mute) { error ->
            if (error != null) { // failure
                error.message?.let {
                    CustomToast.show(it, Toast.LENGTH_SHORT)
                }
            }
            completion.invoke(error)
        }
    }

    /**
     * Song chosen list
     */
    fun getSongChosenList() {
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() called")
        ktvServiceProtocol.getChosenSongList { error, chosenSongList ->
            if (error == null) { // success
                chosenSongListLiveData.value = chosenSongList
            }
        }
    }

    private fun onUpdateAllChooseSongs(songList: List<ChosenSongInfo>?) {
        if (!songList.isNullOrEmpty()) {
            val value = songPlayingLiveData.getValue()
            val songPlaying = songList[0]
            if (value == null) {
                // 无已点歌曲， 直接将列表第一个设置为当前播放歌曲
                KTVLogger.d(TAG, "RoomLivingViewModel.onUpdateAllChooseSongs() chosen song list is empty")
                songPlayingLiveData.postValue(songPlaying)
            } else if (value.songNo != songPlaying.songNo) {
                // 当前有已点歌曲, 且更新歌曲和之前歌曲非同一首
                KTVLogger.d(TAG, "RoomLivingViewModel.onUpdateAllChooseSongs() single or first chorus")
                songPlayingLiveData.postValue(songPlaying)
            }
        } else {
            KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList")
            songPlayingLiveData.postValue(null)
        }
    }

    /**
     * Song types
     */
    fun getSongTypes(): LiveData<LinkedHashMap<Int, String>> {
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongTypes() called")
        val liveData = MutableLiveData<LinkedHashMap<Int, String>>()
        ktvApiProtocol.fetchMusicCharts { id: String?, status: Int, list: Array<out MusicChartInfo>? ->
            KTVLogger.d(TAG, "RoomLivingViewModel.getSongTypes() return")
            val musicList: List<MusicChartInfo> = if (list == null) emptyList() else listOf(*list)
            val types = LinkedHashMap<Int, String>()
            // 重新排序 ----> 按照（嗨唱推荐、抖音热歌、热门新歌、KTV必唱）这个顺序进行怕苦
            for (i in 0..3) {
                for (musicChartInfo in musicList) {
                    if ((i == 0 && musicChartInfo.type == 3) ||
                        (i == 1 && musicChartInfo.type == 4) ||
                        (i == 2 && musicChartInfo.type == 2) ||
                        (i == 3 && musicChartInfo.type == 6)
                    ) {
                        types[musicChartInfo.type] = musicChartInfo.name
                    }
                }
            }
            // 将剩余的插到尾部
            for (musicChartInfo in musicList) {
                if (!types.containsKey(musicChartInfo.type)) {
                    types[musicChartInfo.type] = musicChartInfo.name
                }
            }
            // 因为榜单基本是固化的，防止拉取列表失败，直接写入配置
            if (musicList.isEmpty()) {
                types[3] = "嗨唱推荐"
                types[4] = "抖音热歌"
                types[2] = "新歌榜"
                types[6] = "KTV必唱"
                types[0] = "项目热歌榜单"
                types[1] = "声网热歌榜"
                types[5] = "古风热歌"
            }
            liveData.postValue(types)
            null
        }
        return liveData
    }

    /**
     * Get song list
     *
     * @param type
     * @param page
     * @return
     */
    fun getSongList(type: Int, page: Int): LiveData<List<ChosenSongInfo>> {
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongList() called, type:$type page:$page")
        val liveData = MutableLiveData<List<ChosenSongInfo>>()
        val jsonOption = "{\"pitchType\":2,\"needLyric\":true}"
        ktvApiProtocol.searchMusicByMusicChartId(
            0,
            page,
            30,
            jsonOption
        ) { id: String?, status: Int, p: Int, size: Int, total: Int, list: Array<out Music>? ->
            KTVLogger.d(TAG, "RoomLivingViewModel.getSongList() return")
            val musicList: List<Music> = if (list == null) emptyList() else listOf(*list)
            if (status != 0) {
                CustomToast.show(R.string.ktv_get_songs_failed, "mcc reason:$status")
            }
            val songs: MutableList<ChosenSongInfo> = ArrayList()
            // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
            ktvServiceProtocol.getChosenSongList { e: Exception?, songsChosen: List<ChosenSongInfo>? ->
                if (e == null && songsChosen != null) { // success
                    for (music in musicList) {
                        var songItem = songsChosen.firstOrNull { it.songNo == music.songCode.toString() }
                        if (songItem == null) {
                            songItem = ChosenSongInfo(
                                songName = music.name,
                                songNo = music.songCode.toString(),
                                singer = music.singer,
                                imageUrl = music.poster,
                                owner = AUIUserThumbnailInfo(),
                                status = PlayStatus.idle,
                            )
                        }
                        songs.add(songItem)
                    }
                    liveData.postValue(songs)
                }
                return@getChosenSongList
            }
            return@searchMusicByMusicChartId
        }
        return liveData
    }

    /**
     * 搜索歌曲
     *
     * @param condition the condition
     * @return the live data
     */
    fun searchSong(condition: String): LiveData<List<ChosenSongInfo>> {
        // 从RTC中搜索歌曲
        KTVLogger.d(TAG, "RoomLivingViewModel.searchSong() called, condition:$condition")
        val liveData = MutableLiveData<List<ChosenSongInfo>>()

        // 过滤没有歌词的歌曲
        val jsonOption = if (KTVApi.debugMode) {
            "{\"pitchType\":1,\"needLyric\":true}"
        } else {
            "{\"pitchType\":2,\"needLyric\":true}"
        }
        ktvApiProtocol.searchMusicByKeyword(
            condition, 0, 50, jsonOption
        ) { id: String?, status: Int, p: Int, size: Int, total: Int, list: Array<out Music>? ->
            val musicList: List<Music> = if (list == null) emptyList() else listOf(*list)

            val songs: MutableList<ChosenSongInfo> = ArrayList()

            // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
            ktvServiceProtocol.getChosenSongList { e: Exception?, songsChosen: List<ChosenSongInfo>? ->
                if (e == null && songsChosen != null) {
                    // success
                    for (music in musicList) {
                        var songItem = songsChosen.firstOrNull { it.songNo == music.songCode.toString() }
                        if (songItem == null) {
                            songItem = ChosenSongInfo(
                                songName = music.name,
                                songNo = music.songCode.toString(),
                                singer = music.singer,
                                imageUrl = music.poster,
                                owner = AUIUserThumbnailInfo(),
                                status = PlayStatus.idle,
                            )
                        }
                        songs.add(songItem)
                    }
                    liveData.postValue(songs)
                }
            }
        }
        return liveData
    }

    /**
     * 点歌
     *
     * @param songItem the song model
     * @return the live data
     */
    fun chooseSong(songItem: SongItem): LiveData<Boolean> {
        KTVLogger.d(TAG, "RoomLivingViewModel.chooseSong() called,songNo:${songItem.songNo}")
        val liveData = MutableLiveData<Boolean>()
        val chosenSong = ChooseSongInputModel(
            songName = songItem.songName,
            songNo = songItem.songNo,
            singer = songItem.singer,
            imageUrl = songItem.imageUrl,
        )
        ktvServiceProtocol.chooseSong(chosenSong) { error ->
            if (error != null) { // failure
                CustomToast.show(R.string.ktv_choose_song_failed, error.message ?: "")
            }
            liveData.postValue(error == null)
        }
        return liveData
    }

    /**
     * Delete song
     *
     * @param songModel the song model
     */
    fun deleteSong(songModel: ChosenSongInfo) {
        KTVLogger.d(TAG, "RoomLivingViewModel.deleteSong() called, songNo:${songModel.songNo}")
        ktvServiceProtocol.removeSong(songModel.songNo) { error ->
            if (error != null) { // failure
                CustomToast.show(R.string.ktv_remove_song_failed, error.message ?: "")
            }
        }
    }

    /**
     * 置顶歌曲
     *
     * @param songModel the song model
     */
    fun pinSong(songModel: ChosenSongInfo) {
        KTVLogger.d(TAG, "RoomLivingViewModel.pinSong() called, songNo:${songModel.songNo}")
        ktvServiceProtocol.pinSong(songModel.songNo) { error ->
            if (error != null) { // failure
                CustomToast.show(R.string.ktv_pin_song_failed, error.message ?: "")
            }
        }
    }

    /**
     * 点击加入合唱
     */
    fun joinChorus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.joinChorus() viewClick called")
        if (mRtcEngine?.connectionState != CONNECTION_STATE_TYPE.getValue(CONNECTION_STATE_TYPE.CONNECTION_STATE_CONNECTED)) {
            joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            return
        }
        val musicModel = songPlayingLiveData.getValue()
        if (musicModel == null) {
            KTVLogger.e(TAG, "RoomLivingViewModel.joinChorus() failed, no song playing now")
            joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            return
        }
        if (localSeatInfo == null) { // 不在麦上， 自动上麦
            ktvServiceProtocol.enterSeat(null) { err: Exception? ->
                if (err == null) {
                    innerJoinChorus(musicModel.songNo)
                } else {
                    joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                }
            }
        } else { // 在麦上，直接加入合唱
            innerJoinChorus(musicModel.songNo)
        }
    }

    /**
     * 加入合唱
     *
     * @param songCode
     */
    private fun innerJoinChorus(songCode: String) {
        loadingMusic.set(true)
        ktvApiProtocol.loadMusic(songCode.toLong(),
            KTVLoadMusicConfiguration(
                songCode,
                songPlayingLiveData.getValue()?.owner?.userId?.toIntOrNull() ?: -1,
                KTVLoadMusicMode.LOAD_MUSIC_ONLY,
                false
            ),
            object : IMusicLoadStateListener {
                override fun onMusicLoadProgress(
                    songCode: Long,
                    percent: Int,
                    status: MusicLoadStatus,
                    msg: String?,
                    lyricUrl: String?
                ) {
                    loadMusicProgressLiveData.postValue(percent)
                }

                override fun onMusicLoadSuccess(songCode: Long, lyricUrl: String) {
                    loadingMusic.set(false)
                    KTVLogger.d(TAG, "joinChorus onMusicLoadSuccess,songCode:$songCode,lyricUrl:$lyricUrl")
                    KTVLogger.d(TAG, "RoomLivingViewModel.joinChorus called")
                    val songModel = songPlayingLiveData.value ?: run {
                        KTVLogger.d(TAG, "RoomLivingViewModel.joinChorus songPlayingLiveData is null")
                        return
                    }
                    ktvServiceProtocol.joinChorus(songModel.songNo) { e: Exception? ->
                        if (e == null) {
                            innerRtmOnSelfJoinedChorus()
                        } else { // failure
                            // fix publish message 回调时间比 rtm onMetaData 提前
                            if (joinchorusStatusLiveData.value==JoinChorusStatus.ON_JOIN_CHORUS) return@joinChorus
                            joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                            e.message?.let { error ->
                                CustomToast.show(error, Toast.LENGTH_SHORT)
                            }
                        }
                    }
                }

                override fun onMusicLoadFail(songCode: Long, reason: KTVLoadMusicFailReason) {
                    loadingMusic.set(false)
                    KTVLogger.e(TAG, "joinChorus onMusicLoadFail,songCode:$songCode,reason:$reason")
                    joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                }
            })
    }

    private fun innerRtmOnSelfJoinedChorus() {
        if (joinchorusStatusLiveData.value == JoinChorusStatus.ON_JOIN_CHORUS) return
        ktvApiProtocol.switchSingerRole(KTVSingRole.CoSinger, object : ISwitchRoleStateListener {
            override fun onSwitchRoleFail(reason: SwitchRoleFailReason) {
                KTVLogger.e(TAG, "RoomLivingViewModel.onSwitchRoleFail(CoSinger) reason:$reason")
                if (reason == SwitchRoleFailReason.JOIN_CHANNEL_FAIL) {
                    joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                }
            }

            override fun onSwitchRoleSuccess() {
                if (localSeatInfo != null && songPlayingLiveData.value!=null) {
                    KTVLogger.d(TAG, "RoomLivingViewModel.onSwitchRoleSuccess(CoSinger)")
                    joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_CHORUS)
                } else {
                    KTVLogger.d(TAG, "RoomLivingViewModel.onSwitchRoleSuccess(CoSinger) but localSeat or songPlayingLiveData is null")
                    ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null)
                    joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                }
            }
        })
    }

    /**
     * 退出合唱
     */
    fun leaveChorus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called")
        val songCode = songPlayingLiveData.value?.songNo
        if (localSeatInfo != null && songCode != null) {
            ktvServiceProtocol.leaveChorus(songCode) { e: Exception? ->
                if (e == null) { // success
                    KTVLogger.d(TAG, "RoomLivingViewModel.leaveChorus.switchSingerRole called KTVSingRole.Audience")
                    ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null)
                    joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
                } else { // failure
                    e.message?.let { error ->
                        CustomToast.show(error, Toast.LENGTH_SHORT)
                    }
                }
            }
        } else {
            KTVLogger.d(TAG, "RoomLivingViewModel.switchSingerRole called KTVSingRole.Audience")
            ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null)
            joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
        }
    }

    /**
     * 开始切歌
     */
    fun changeMusic() {
        KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() called")
        val musicModel = songPlayingLiveData.getValue() ?: run {
            KTVLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed, no song is playing now!")
            return
        }
        //ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, "", null);
        playerMusicStatusLiveData.value = PlayerMusicStatus.ON_CHANGING_START
        ktvServiceProtocol.removeSong(musicModel.songNo) { error ->
            if (error != null) { // failed
                CustomToast.show(R.string.ktv_change_song_failed, error.message ?: "")
            }
            playerMusicStatusLiveData.value = PlayerMusicStatus.ON_CHANGING_END
        }
    }

    /**
     * 设置歌词view
     *
     * @param view the view
     */
    fun setLrcView(view: ILrcView) {
        ktvApiProtocol.setLrcView(view)
        mMusicSetting?.let { setting ->
            ktvApiProtocol.enableProfessionalStreamerMode(setting.mProfessionalModeEnable)
        }
    }

    // ======================= Player/RTC/MPK相关 =======================
    // ------------------ 初始化音乐播放设置面版 ------------------
    private fun initSettings() {
        // debug 设置
        mDebugSetting = KTVDebugSettingBean(object : KTVDebugSettingsDialog.Callback {
            override fun onAudioDumpEnable(enable: Boolean) {
                if (enable) {
                    mRtcEngine?.setParameters("{\"rtc.debug.enable\": true}")
                    mRtcEngine?.setParameters(
                        "{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\"," +
                                "\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"
                    )
                } else {
                    mRtcEngine?.setParameters("{\"rtc.debug.enable\": false}")
                }
            }

            override fun onScoringControl(level: Int, offset: Int) {
                scoringAlgoControlLiveData.postValue(ScoringAlgoControlModel(level, offset))
            }

            override fun onSetParameters(parameters: String) {
                mRtcEngine?.setParameters(parameters)
            }
        })

        // 音乐设置
        mMusicSetting = MusicSettingBean(object : MusicSettingCallback {
            override fun onEarChanged(earBackEnable: Boolean) {
                KTVLogger.d(TAG, "onEarChanged: $earBackEnable")
                val isAudioMuted = localSeatInfo?.isAudioMuted ?: true
                if (isAudioMuted) {
                    return
                }
                mRtcEngine?.enableInEarMonitoring(earBackEnable, Constants.EAR_MONITORING_FILTER_NONE)
            }

            override fun onEarBackVolumeChanged(volume: Int) {
                KTVLogger.d(TAG, "onEarBackVolumeChanged: $volume")
                mRtcEngine?.setInEarMonitoringVolume(volume)
            }

            override fun onEarBackModeChanged(mode: Int) {
                KTVLogger.d(TAG, "onEarBackModeChanged: $mode")
                if (mode == 1) { // OpenSL
                    mRtcEngine?.setParameters("{\"che.audio.opensl.mode\": 0}")
                } else if (mode == 2) { // Oboe
                    mRtcEngine?.setParameters("{\"che.audio.oboe.enable\": true}")
                }
            }

            override fun onMicVolChanged(vol: Int) {
                setMicVolume(vol)
            }

            override fun onAccVolChanged(vol: Int) {
                setMusicVolume(vol)
            }

            override fun onRemoteVolChanged(volume: Int) {
                KTVApi.remoteVolume = volume
                mRtcEngine?.adjustPlaybackSignalVolume(volume)
            }

            override fun onAudioEffectChanged(audioEffect: Int) {
                KTVLogger.d(TAG, "onAudioEffectChanged: $audioEffect")
                mRtcEngine?.setAudioEffectPreset(audioEffect)
            }

            override fun onScoringDifficultyChanged(difficulty: Int) {
                KTVLogger.d(TAG, "onScoringDifficultyChanged: $difficulty")
                scoringAlgoLiveData.postValue(difficulty)
            }

            override fun onProfessionalModeChanged(enable: Boolean) {
                KTVLogger.d(TAG, "onProfessionalModeChanged: $enable")
                ktvApiProtocol.enableProfessionalStreamerMode(enable)
            }

            override fun onMultiPathChanged(enable: Boolean) {
                KTVLogger.d(TAG, "onMultiPathChanged: $enable")
                ktvApiProtocol.enableMulitpathing(enable)
            }

            override fun onAECLevelChanged(level: Int) {
                KTVLogger.d(TAG, "onAECLevelChanged: $level")
                // aiaec关闭的情况下音质选项才能生效
                when (level) {
                    0 -> mRtcEngine?.setParameters("{\"che.audio.aec.split_srate_for_48k\": 16000}")
                    1 -> mRtcEngine?.setParameters("{\"che.audio.aec.split_srate_for_48k\": 24000}")
                    2 -> mRtcEngine?.setParameters("{\"che.audio.aec.split_srate_for_48k\": 48000}")
                }
            }

            override fun onLowLatencyModeChanged(enable: Boolean) {
                KTVLogger.d(TAG, "onLowLatencyModeChanged: $enable")
                if (enable) {
                    mRtcEngine?.setParameters("{\"che.audio.ains_mode\": -1}")
                } else {
                    mRtcEngine?.setParameters("{\"che.audio.ains_mode\": 0}")
                }
            }

            override fun onAINSModeChanged(mode: Int) {
                KTVLogger.d(TAG, "onAINSModeChanged: $mode")
                when (mode) {
                    0 -> { // 关闭
                        mRtcEngine?.setParameters("{\"che.audio.ains_mode\": 0}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.lowerBound\": 80}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.lowerMask\": 50}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.statisticalbound\": 5}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.finallowermask\": 30}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.enhfactorstastical\": 200}")
                    }

                    1 -> { // 中
                        mRtcEngine?.setParameters("{\"che.audio.ains_mode\": 2}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.lowerBound\": 80}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.lowerMask\": 50}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.statisticalbound\": 5}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.finallowermask\": 30}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.enhfactorstastical\": 200}")
                    }

                    2 -> { // 高
                        mRtcEngine?.setParameters("{\"che.audio.ains_mode\": 2}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.lowerBound\": 10}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.lowerMask\": 10}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.statisticalbound\": 0}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.finallowermask\": 8}")
                        mRtcEngine?.setParameters("{\"che.audio.nsng.enhfactorstastical\": 200}")
                    }
                }
            }

            override fun onAIAECChanged(enable: Boolean) {
                KTVLogger.d(TAG, "onAIAECChanged: $enable")
                if (enable) {
                    mRtcEngine?.setParameters("{\"che.audio.aiaec.working_mode\": 1}")
                } else {
                    mRtcEngine?.setParameters("{\"che.audio.aiaec.working_mode\": 0}")
                }
            }

            override fun onAIAECStrengthSelect(strength: Int) {
                KTVLogger.d(TAG, "onAIAECStrengthSelect: $strength")
                mRtcEngine?.setParameters("{\"che.audio.aiaec.postprocessing_strategy\":$strength}")
            }
        })
        mSoundCardSettingBean = SoundCardSettingBean { presetValue: Int, gainValue: Float, gender: Int, effect: Int ->
            KTVLogger.d(TAG, "SoundCard parameter preset:$presetValue,gain:$gainValue,gender:$gender,effect:$effect")
            mRtcEngine?.setParameters(
                "{\"che.audio.virtual_soundcard\":{\"preset\":" + presetValue +
                        ",\"gain\":" + gainValue +
                        ",\"gender\":" + gender +
                        ",\"effect\":" + effect + "}}"
            )
        }
    }

    private fun initRTCPlayer() {
        val rtcAppId = KtvCenter.mAppId
        if (TextUtils.isEmpty(rtcAppId)) {
            throw NullPointerException("please check \"gradle.properties\"")
        }
        if (mRtcEngine != null) return
        // ------------------ 初始化RTC ------------------
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = rtcAppId
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                // 网络状态回调, 本地user uid = 0
                if (uid == 0) {
                    networkStatusLiveData.postValue(NetWorkEvent(txQuality, rxQuality))
                }
            }

            override fun onContentInspectResult(result: Int) {
                super.onContentInspectResult(result)
                if (result > 1) {
                    CustomToast.show(R.string.ktv_content, Toast.LENGTH_SHORT)
                }
            }

            override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray) {
                val jsonMsg: JSONObject
                try {
                    val strMsg = String(data)
                    jsonMsg = JSONObject(strMsg)
                    if (jsonMsg.getString("cmd") == "singleLineScore") {
                        val score = jsonMsg.getInt("score")
                        val index = jsonMsg.getInt("index")
                        val cumulativeScore = jsonMsg.getInt("cumulativeScore")
                        val total = jsonMsg.getInt("total")
                        val lineScore = LineScore(
                            score = score,
                            index = index,
                            cumulativeScore = cumulativeScore,
                            total = total
                        )
                        mainSingerScoreLiveData.postValue(lineScore)
                    } else if (jsonMsg.getString("cmd") == "SingingScore") {
                        val score = jsonMsg.getDouble("score").toFloat()
                        playerMusicPlayCompleteLiveData.postValue(ScoringAverageModel(false, score.toInt()))
                    }
                } catch (exp: JSONException) {
                    KTVLogger.e(TAG, "onStreamMessage:$exp")
                }
            }

            override fun onAudioRouteChanged(routing: Int) { // 0\2\5 earPhone
                super.onAudioRouteChanged(routing)
                KTVLogger.d(TAG, "onAudioRouteChanged, routing:$routing")
                mMusicSetting?.let { setting ->
                    if (routing == 0 || routing == 2 || routing == 5 || routing == 6) {
                        setting.mHasEarPhone = true
                    } else {
                        if (songPlayingLiveData.getValue() != null && setting.mEarBackEnable) {
                            CustomToast.show(R.string.ktv_earphone_close_tip, Toast.LENGTH_SHORT)
                            setting.mEarBackEnable = false
                        }
                        setting.mHasEarPhone = false
                    }
                }
            }

            override fun onLocalAudioStats(stats: LocalAudioStats) {
                super.onLocalAudioStats(stats)
                mMusicSetting?.mEarBackDelay = stats.earMonitorDelay
            }

            override fun onAudioVolumeIndication(speakers: Array<AudioVolumeInfo>, totalVolume: Int) {
                super.onAudioVolumeIndication(speakers, totalVolume)
                for (speaker in speakers) {
                    volumeLiveData.postValue(VolumeModel(speaker.uid, speaker.volume))
                }
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING
        config.addExtension("agora_ai_echo_cancellation_extension")
        config.addExtension("agora_ai_noise_suppression_extension")
        try {
            mRtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: Exception) {
            e.printStackTrace()
            KTVLogger.e(TAG, "RtcEngine.create() called error: $e")
        }
        mRtcEngine?.apply {
            loadExtensionProvider("agora_drm_loader")
            setParameters("{\"che.audio.ains_mode\": -1}")
            setParameters("{\"che.audio.input_sample_rate\" : 48000}")
        }

        // ------------------ 场景化api初始化 ------------------
        KTVApi.debugMode = AgoraApplication.the().isDebugModeOpen
        if (AgoraApplication.the().isDebugModeOpen) {
            KTVLogger.d(TAG, "isDebugModeOpen: true")
            KTVApi.mccDomain = "api-test.agora.io"
        }
        ktvApiProtocol = createKTVApi(
            KTVApiConfig(
                appId = rtcAppId,
                rtmToken = KtvCenter.mRtmToken,
                engine = mRtcEngine!!,
                channelName = mRoomInfo.roomId,
                localUid = KtvCenter.mUser.id.toInt(),
                chorusChannelName = mRoomInfo.rtcChorusChannelName,
                chorusChannelToken = KtvCenter.mRtcToken,
                maxCacheSize = 10,
                type = KTVType.Normal,
                musicType = KTVMusicType.SONG_CODE
            )
        )
        ktvApiProtocol.addEventHandler(object : IKTVApiEventHandler() {
            override fun onMusicPlayerStateChanged(
                state: MediaPlayerState, error: MediaPlayerReason, isLocal: Boolean
            ) {
                KTVLogger.d(TAG, "onMusicPlayerStateChanged, state:$state error:$error isLocal:$isLocal")
                when (state) {
                    MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        runOnMainThread {
                            playerMusicOpenDurationLiveData.value = ktvApiProtocol.getMediaPlayer().duration
                        }
                    }

                    MediaPlayerState.PLAYER_STATE_PLAYING -> {
                        runOnMainThread {
                            playerMusicStatusLiveData.value = PlayerMusicStatus.ON_PLAYING
                            mMusicSetting?.let { setting ->
                                // 若身份是主唱和伴唱，在演唱时，人声音量、伴泰音量保持原先设置，远端音量自动切为30
                                setting.mRemoteVolume = MusicSettingBean.DEFAULT_REMOTE_SINGER_VOL
                                //主唱/合唱 开始唱歌: 默认关闭 aiaec
                                setting.mAIAECEnable = false
                            }
                        }
                    }

                    MediaPlayerState.PLAYER_STATE_PAUSED -> {
                        runOnMainThread {
                            playerMusicStatusLiveData.value = PlayerMusicStatus.ON_PAUSE
                            // 若身份是主唱和伴唱，演唱暂停/切歌，人声音量、伴奏音量保持原先设置，远端音量自动转为100
                            mMusicSetting?.mRemoteVolume = MusicSettingBean.DEFAULT_REMOTE_VOL
                        }
                    }

                    MediaPlayerState.PLAYER_STATE_STOPPED -> {
                        runOnMainThread {
                            playerMusicStatusLiveData.value = PlayerMusicStatus.ON_STOP
                            mMusicSetting?.let { setting ->
                                // 若身份是主唱和伴唱，演唱暂停/切歌，人声音量、伴奏音量保持原先设置，远端音量自动转为100
                                setting.mRemoteVolume = MusicSettingBean.DEFAULT_REMOTE_VOL
                                // 主唱/合唱 歌曲结束/退出合唱: 默认开启 aiaec, 强度为1
                                setting.mAIAECEnable = true
                                setting.mAIAECStrength = MusicSettingBean.DEFAULT_AIAEC_STRENGTH
                            }
                        }
                    }

                    MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> if (isLocal) {
                        runOnMainThread {
                            playerMusicPlayCompleteLiveData.value = ScoringAverageModel(true, 0)
                            playerMusicStatusLiveData.value = PlayerMusicStatus.ON_LRC_RESET
                        }
                    }

                    else -> {}
                }
            }

            override fun onTokenPrivilegeWillExpire() {
                super.onTokenPrivilegeWillExpire()
                KTVLogger.d(TAG, "ktvapi onTokenPrivilegeWillExpire")
            }
        }
        )
        if (isRoomOwner) {
            ktvApiProtocol.muteMic(false)
        }

        // ------------------ 加入频道 ------------------
        mRtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            enableVideo()
            enableLocalVideo(false)
            enableAudio()
            setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO, Constants.AUDIO_SCENARIO_GAME_STREAMING)
            enableAudioVolumeIndication(50, 10, true)
            setClientRole(if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)
            setAudioEffectPreset(mMusicSetting?.mAudioEffect ?: Constants.ROOM_ACOUSTICS_KTV)
            val ret = joinChannel(KtvCenter.mRtcToken, mRoomInfo.roomId, null, KtvCenter.mUser.id.toInt())
            if (ret != Constants.ERR_OK) {
                KTVLogger.e(TAG, "joinRTC() called error: $ret")
            }
        }
        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "ktv")
            jsonObject.put("id", KtvCenter.mUser.id.toString())
            jsonObject.put("userNo", KtvCenter.mUser.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectModule()
            module.interval = 30
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            mRtcEngine?.enableContentInspect(true, contentInspectConfig)
        } catch (e: JSONException) {
            KTVLogger.e(TAG, "enableContentInspect:$e")
        }

        // ------------------ Enable voice identification service ------------------
        moderationAudio(mRoomInfo.roomId, KtvCenter.mUser.id, AudioModeration.AgoraChannelType.Rtc, "ktv",
            success = {
                KTVLogger.d(TAG, "moderationAudio success")
            },
            failure = {
                KTVLogger.e(TAG, "moderationAudio failure:$it")
            })

        // External StreamId
        if (streamId == 0) {
            val cfg = DataStreamConfig()
            cfg.syncWithAudio = false
            cfg.ordered = false
            streamId = mRtcEngine?.createDataStream(cfg) ?: 0
        }
    }

    // ======================= settings =======================
    private var micOldVolume = 100
    private fun setMusicVolume(v: Int) {
        ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(v)
        ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(v)
    }

    private fun setMicVolume(v: Int) {
        val isAudioMuted = localSeatInfo?.isAudioMuted ?: true
        if (isAudioMuted) {
            KTVLogger.d(TAG, "muted! setMicVolume: $v")
            micOldVolume = v
            return
        }
        KTVLogger.d(TAG, "unmute! setMicVolume: $v")
        mRtcEngine?.adjustRecordingSignalVolume(v)
    }

    /**
     * Music toggle original.
     *
     * @param audioTrack the audio track
     */
    fun musicToggleOriginal(audioTrack: LrcControlView.AudioTrack) {
        KTVLogger.d(TAG, "musicToggleOriginal called, aim:$audioTrack")
        ktvApiProtocol.switchAudioTrack(getAudioTrackMode(audioTrack))
    }

    /**
     * Get audio track mode
     *
     * @param audioTrack
     * @return
     */
    private fun getAudioTrackMode(audioTrack: LrcControlView.AudioTrack): AudioTrackMode {
        return when (audioTrack) {
            LrcControlView.AudioTrack.Acc -> AudioTrackMode.BAN_ZOU
            LrcControlView.AudioTrack.DaoChang -> AudioTrackMode.DAO_CHANG
            else -> AudioTrackMode.YUAN_CHANG
        }
    }

    /**
     * Music toggle start.
     */
    fun musicToggleStart() {
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            KTVLogger.d(TAG, "musicToggleStart called, pauseSing")
            ktvApiProtocol.pauseSing()
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            KTVLogger.d(TAG, "musicToggleStart called, resumeSing")
            ktvApiProtocol.resumeSing()
        }
    }

    /**
     * Render local camera video.
     *
     * @param surfaceView the surface view
     */
    fun renderLocalCameraVideo(surfaceView: SurfaceView?) {
        KTVLogger.d(TAG, "renderLocalCameraVideo called")
        mRtcEngine?.startPreview()
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, 0))
    }

    /**
     * Render remote camera video.
     *
     * @param surfaceView the surface view
     * @param uid         the uid
     */
    fun renderRemoteCameraVideo(surfaceView: SurfaceView?, uid: Int) {
        KTVLogger.d(TAG, "RoomLivingViewModel.renderRemoteCameraVideo() called uid:$uid")
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, uid))
    }

    /**
     * Reset music status.
     */
    fun resetMusicStatus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.resetMusicStatus() called")
        retryTimes = 0
        joinchorusStatusLiveData.postValue(JoinChorusStatus.ON_IDLE)
        ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null)
    }

    private var retryTimes = 0

    /**
     * Music start play.
     *
     * @param music the music
     */
    fun musicStartPlay(music: ChosenSongInfo) {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called music:$music")
        if (music.owner?.userId.isNullOrEmpty()) return
        playerMusicStatusLiveData.value = PlayerMusicStatus.ON_PREPARE
        val isOwnSong = music.owner?.userId == KtvCenter.mUser.id.toString()
        val songCode = music.songNo.toLong()
        val mainSingerUid = music.owner?.userId?.toIntOrNull() ?: -1
        if (isOwnSong) {
            // 主唱加载歌曲
            loadMusic(
                KTVLoadMusicConfiguration(
                    music.songNo, mainSingerUid, KTVLoadMusicMode.LOAD_MUSIC_AND_LRC, false
                ), songCode, true
            )
        } else {
            val choristerInfo = getSongChorusInfo(KtvCenter.mUser.id.toString(), music.songNo)
            if (choristerInfo != null) {
                // 合唱者
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo, mainSingerUid, KTVLoadMusicMode.LOAD_LRC_ONLY, false
                    ), songCode, false
                )
                // 加入合唱
                innerJoinChorus(music.songNo)
            } else {
                // 观众
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo, mainSingerUid,
                        KTVLoadMusicMode.LOAD_LRC_ONLY, false
                    ), songCode, false
                )
            }
        }

        if (music.owner?.userId == KtvCenter.mUser.id.toString()) {
            // 标记歌曲为播放中
            ktvServiceProtocol.makeSongDidPlay(music.songNo) { e ->
                e?.message?.let { error ->
                    CustomToast.show(error, Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private var loadingMusic:AtomicBoolean = AtomicBoolean(false)

    private fun loadMusic(config: KTVLoadMusicConfiguration, songCode: Long, isOwnSong: Boolean) {
        loadingMusic.set(true)
        ktvApiProtocol.loadMusic(songCode, config, object : IMusicLoadStateListener {
            override fun onMusicLoadProgress(
                songCode: Long,
                percent: Int,
                status: MusicLoadStatus,
                msg: String?,
                lyricUrl: String?
            ) {
                loadMusicProgressLiveData.postValue(percent)
            }

            override fun onMusicLoadSuccess(songCode: Long, lyricUrl: String) {
                loadingMusic.set(false)
                KTVLogger.d(TAG, "onMusicLoadSuccess, songCode: $songCode lyricUrl: $lyricUrl")
                // 当前已被切歌
                if (songPlayingLiveData.getValue() == null) {
                    CustomToast.show(R.string.ktv_load_failed_no_song, Toast.LENGTH_LONG)
                    return
                }
                if (isOwnSong) {
                    // 需要判断此时是否有合唱者，如果有需要切换成LeaderSinger身份
                    if (chorusInfoList.size == 0) {
                        ktvApiProtocol.switchSingerRole(KTVSingRole.SoloSinger, null)
                    } else if (chorusInfoList.size > 0) {
                        ktvApiProtocol.switchSingerRole(KTVSingRole.LeadSinger, null)
                    }
                    ktvApiProtocol.startSing(songCode, 0)
                }

                // 重置settings
                retryTimes = 0
                runOnMainThread {
                    playerMusicStatusLiveData.value = PlayerMusicStatus.ON_PLAYING
                }
            }

            override fun onMusicLoadFail(songCode: Long, reason: KTVLoadMusicFailReason) {
                loadingMusic.set(false)
                KTVLogger.e(TAG, "onMusicLoadFail，songCode:$songCode, reason:$reason")
                // 当前已被切歌
                if (songPlayingLiveData.getValue() == null) {
                    CustomToast.show(R.string.ktv_load_failed_no_song, Toast.LENGTH_LONG)
                    return
                }
                if (reason == KTVLoadMusicFailReason.NO_LYRIC_URL) { // 未获取到歌词 正常播放
                    retryTimes = 0
                    runOnMainThread {
                        playerMusicStatusLiveData.value = PlayerMusicStatus.ON_PLAYING
                        noLrcLiveData.value = true
                    }
                } else if (reason == KTVLoadMusicFailReason.MUSIC_PRELOAD_FAIL) { // 歌曲加载失败 ，重试3次
                    CustomToast.show(R.string.ktv_load_failed, Toast.LENGTH_LONG)
                    retryTimes += 1
                    if (retryTimes < 3) {
                        loadMusic(config, songCode, isOwnSong)
                    } else {
                        runOnMainThread {
                            playerMusicStatusLiveData.value = PlayerMusicStatus.ON_PLAYING
                        }
                        CustomToast.show(R.string.ktv_try, Toast.LENGTH_LONG)
                    }
                } else if (reason == KTVLoadMusicFailReason.CANCELED) { // 当前已被切歌
                    CustomToast.show(R.string.ktv_load_failed_another_song, Toast.LENGTH_LONG)
                }
            }
        })
    }

    private fun soloSingerJoinChorusMode(lastChorusNum: Int) {
        if (songPlayingLiveData.getValue() == null || seatListLiveData.getValue() == null) return
        if (songPlayingLiveData.value?.owner?.userId == KtvCenter.mUser.id.toString()) {
            if (lastChorusNum == 0 && chorusInfoList.size > 0) { // 有人加入合唱
                ktvApiProtocol.switchSingerRole(KTVSingRole.LeadSinger, null)
            } else if (lastChorusNum > 0 && chorusInfoList.size == 0) { // 最后一人退出合唱
                ktvApiProtocol.switchSingerRole(KTVSingRole.SoloSinger, null)
            }
        }
    }

    /**
     * Re get lrc url.
     */
    fun reGetLrcUrl() {
        KTVLogger.e(TAG, "reGetLrcUrl，called")
        val songPlayingValue = songPlayingLiveData.getValue() ?: return
        val isOwnSong = songPlayingValue.owner?.userId == KtvCenter.mUser.id.toString()
        loadMusic(
            KTVLoadMusicConfiguration(
                songPlayingValue.songNo,
                songPlayingValue.owner?.userId?.toIntOrNull() ?: -1,
                KTVLoadMusicMode.LOAD_LRC_ONLY,
                false
            ), songPlayingValue.songNo.toLongOrNull() ?: -1, isOwnSong
        )
    }

    /**
     * Music seek.
     *
     * @param time the time
     */
    fun musicSeek(time: Long) {
        ktvApiProtocol.seekSing(time)
    }

    /**
     * Song duration
     */
    val songDuration: Long get() = ktvApiProtocol.getMediaPlayer().duration

    /**
     * Music stop.
     */
    fun musicStop() {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStop() called")
        // 列表中无歌曲， 还原状态
        resetMusicStatus()
    }

    /**
     * Sync single line score.
     *
     * @param score           the score
     * @param cumulativeScore the cumulative score
     * @param index           the index
     * @param total           the total
     */
    // ------------------ 歌词组件相关 ------------------
    fun syncSingleLineScore(score: Int, cumulativeScore: Int, index: Int, total: Int) {
        val rtcEngine = mRtcEngine ?: return
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "singleLineScore"
        msg["score"] = score
        msg["index"] = index
        msg["cumulativeScore"] = cumulativeScore
        msg["total"] = total
        val jsonMsg = JSONObject(msg)
        val ret = rtcEngine.sendStreamMessage(streamId, jsonMsg.toString().toByteArray())
        if (ret < 0) {
            KTVLogger.e(TAG, "syncSingleLineScore() sendStreamMessage called returned: $ret")
        }
    }

    /**
     * Sync singing average score.
     *
     * @param score the score
     */
    fun syncSingingAverageScore(score: Double) {
        val rtcEngine = mRtcEngine ?: return
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "SingingScore"
        msg["score"] = score
        val jsonMsg = JSONObject(msg)
        val ret = rtcEngine.sendStreamMessage(streamId, jsonMsg.toString().toByteArray())
        if (ret < 0) {
            KTVLogger.e(TAG, "syncSingingAverageScore() sendStreamMessage called returned: $ret")
        }
    }
}

package io.agora.scene.cantata.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.mediaplayer.Constants.MediaPlayerError
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.musiccontentcenter.Music
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection.CONNECTION_STATE_TYPE
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.ContentInspectModule
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.AudioModeration.moderationAudio
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.event.NetWorkEvent
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.cantata.CantataLogger
import io.agora.scene.cantata.R
import io.agora.scene.cantata.ktvapi.IKTVApiEventHandler
import io.agora.scene.cantata.ktvapi.ILrcView
import io.agora.scene.cantata.ktvapi.IMusicLoadStateListener
import io.agora.scene.cantata.ktvapi.ISwitchRoleStateListener
import io.agora.scene.cantata.ktvapi.KTVApiConfig
import io.agora.scene.cantata.ktvapi.KTVApiImpl
import io.agora.scene.cantata.ktvapi.KTVLoadMusicConfiguration
import io.agora.scene.cantata.ktvapi.KTVLoadMusicMode
import io.agora.scene.cantata.ktvapi.KTVLoadSongFailReason
import io.agora.scene.cantata.ktvapi.KTVSingRole
import io.agora.scene.cantata.ktvapi.KTVType
import io.agora.scene.cantata.ktvapi.MusicLoadStatus
import io.agora.scene.cantata.ktvapi.SwitchRoleFailReason
import io.agora.scene.cantata.service.CantataServiceProtocol
import io.agora.scene.cantata.service.ChooseSongInputModel
import io.agora.scene.cantata.service.JoinRoomOutputModel
import io.agora.scene.cantata.service.MakeSongTopInputModel
import io.agora.scene.cantata.service.OnSeatInputModel
import io.agora.scene.cantata.service.OutSeatInputModel
import io.agora.scene.cantata.service.RemoveSongInputModel
import io.agora.scene.cantata.service.RoomListModel
import io.agora.scene.cantata.service.RoomSeatModel
import io.agora.scene.cantata.service.RoomSelSongModel
import io.agora.scene.cantata.service.ScoringAlgoControlModel
import io.agora.scene.cantata.service.ScoringAverageModel
import io.agora.scene.cantata.ui.dialog.MusicSettingBean
import org.json.JSONException
import org.json.JSONObject

class LineScore {
    var score = 0
    var index = 0
    var cumulativeScore = 0
    var total = 0
}

enum class PlayerMusicStatus {
    ON_PREPARE,
    ON_PLAYING,
    ON_PAUSE,
    ON_STOP,
    ON_LRC_RESET,
    ON_CHANGING_START,
    ON_CHANGING_END
}

enum class JoinChorusStatus {
    ON_IDLE,
    ON_JOIN_CHORUS,
    ON_JOIN_FAILED,
    ON_LEAVE_CHORUS
}

enum class KTVPlayerTrackMode {
    Origin, Acc
}

class RoomLivingViewModel constructor(joinRoomOutputModel: JoinRoomOutputModel) : ViewModel() {

    companion object {
        private const val TAG = "Cantata_Scene_Log"
    }

    private val mCantataServiceProtocol = CantataServiceProtocol.getImplInstance()

    private val mKtvApi = KTVApiImpl()

    // loading dialog
    private val _loadingDialogVisible = MutableLiveData(false)
    val mLoadingDialogVisible: LiveData<Boolean> = _loadingDialogVisible

    /**
     * 房间信息
     */
    val mRoomInfoLiveData: MutableLiveData<JoinRoomOutputModel> by lazy {
        MutableLiveData(joinRoomOutputModel)
    }
    val mRoomDeleteLiveData = MutableLiveData<Boolean>()
    val mRoomTimeUpLiveData = MutableLiveData<Boolean>()
    val mRoomUserCountLiveData = MutableLiveData<Int>()

    /**
     * 麦位信息
     */
    var mIsOnSeat = false
    val mSeatListLiveData: MutableLiveData<List<RoomSeatModel>> = MutableLiveData<List<RoomSeatModel>>(emptyList())
    val mSeatLocalLiveData: MutableLiveData<RoomSeatModel> = MutableLiveData<RoomSeatModel>()

    /**
     * 歌词信息
     */
    val mSongsOrderedLiveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()
    val mSongPlayingLiveData: MutableLiveData<RoomSelSongModel> = MutableLiveData<RoomSelSongModel>()


    val mMainSingerScoreLiveData: MutableLiveData<LineScore> = MutableLiveData<LineScore>()

    /**
     * Player/RTC信息
     */
    var mStreamId = 0

    val mPlayerMusicStatusLiveData = MutableLiveData<PlayerMusicStatus>()

    val mJoinChorusStatusLiveData: MutableLiveData<JoinChorusStatus> = MutableLiveData<JoinChorusStatus>()
    val mNoLrcLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    val mPlayerMusicOpenDurationLiveData: MutableLiveData<Long> = MutableLiveData<Long>()
    val mPlayerMusicPlayCompleteLiveData: MutableLiveData<ScoringAverageModel> = MutableLiveData<ScoringAverageModel>()
    val mPlayerMusicCountDownLiveData: MutableLiveData<Int> = MutableLiveData<Int>()
    val mNetworkStatusLiveData = MutableLiveData<NetWorkEvent>()

    val mScoringAlgoControlLiveData: MutableLiveData<ScoringAlgoControlModel> =
        MutableLiveData<ScoringAlgoControlModel>()

    /**
     * Rtc引擎
     */
    private var mRtcEngine: RtcEngineEx? = null

    /**
     * 主版本的音频设置
     */
    private val mMainChannelMediaOption = ChannelMediaOptions()

    /**
     * 播放器配置
     */
    var mMusicSetting: MusicSettingBean? = null

    /**
     * 是否开启后台播放
     */
    private var mIsBackPlay = false

    /**
     * 是否开启耳返
     */
    private var mIsOpnEar = false

    /**
     * 合唱人数
     */
    var mChorusNum = 0

    private var mRetryTimes = 0

    fun isRoomOwner(): Boolean {
        return mRoomInfoLiveData.value?.creatorNo == UserManager.getInstance().user.id.toString()
    }

    fun initViewModel() {
        if (isRoomOwner()) {
            mKtvApi.setMicStatus(true)
            mIsOnSeat = true
        }
        initRTCPlayer()
        initRoom()
        initSeats()
        initSongs()
        initReConnectEvent()
    }

    // 释放
    fun release(): Boolean {
        CantataLogger.d(TAG, "release called")
        mStreamId = 0
        mRtcEngine?.let {
            mKtvApi.release()
            it.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE)
            it.leaveChannel()
            RtcEngineEx.destroy()
            mRtcEngine = null
            return true
        }
        return false
    }

    // ======================= 断网重连相关 =======================
    private fun initReConnectEvent() {
        mCantataServiceProtocol.subscribeReConnectEvent {
            reFetchUserNum()
            reFetchSeatStatus()
            reFetchSongStatus()
        }
    }

    private fun reFetchUserNum() {
        CantataLogger.d(TAG, "reFetchUserNum: call")
        mCantataServiceProtocol.getAllUserList({ num: Int ->
            mRoomUserCountLiveData.postValue(num)
        }, null)
    }

    private fun reFetchSeatStatus() {
        CantataLogger.d(TAG, "reFetchSeatStatus: call")
        mCantataServiceProtocol.getSeatStatusList { e: Exception?, data: List<RoomSeatModel>? ->
            if (e == null && data != null) {
                CantataLogger.d(TAG, "getSeatStatusList: return$data")
                mSeatListLiveData.setValue(data)
            }
        }
    }

    private fun reFetchSongStatus() {
        CantataLogger.d(TAG, "reFetchSongStatus: call")
        onSongChanged()
    }

    // ======================= 房间相关 =======================
    private fun initRoom() {
        val roomInfo: JoinRoomOutputModel = mRoomInfoLiveData.value
            ?: throw RuntimeException("The roomInfo must be not null before initSeats method calling!")
        mRoomUserCountLiveData.postValue(roomInfo.roomPeopleNum)
        mCantataServiceProtocol.subscribeRoomStatus { ktvSubscribe: CantataServiceProtocol.KTVSubscribe,
                                                      vlRoomListModel: RoomListModel? ->
            if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted")
                mRoomDeleteLiveData.postValue(true)
            } else if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                // 当房间内状态发生改变时触发
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated")
                vlRoomListModel ?: return@subscribeRoomStatus
                if (vlRoomListModel.bgOption != roomInfo.bgOption) {
                    mRoomInfoLiveData.postValue(
                        JoinRoomOutputModel(
                            roomInfo.roomName,
                            roomInfo.roomNo,
                            roomInfo.creatorNo,
                            vlRoomListModel.bgOption,
                            roomInfo.seatsArray,
                            roomInfo.roomPeopleNum,
                            roomInfo.agoraRTMToken,
                            roomInfo.agoraRTCToken,
                            roomInfo.agoraChorusToken,
                            roomInfo.createdAt
                        )
                    )
                }
            }
        }
        mCantataServiceProtocol.subscribeUserListCount { count: Int ->
            mRoomUserCountLiveData.postValue(count)
        }
        mCantataServiceProtocol.subscribeRoomTimeUp {
            mRoomTimeUpLiveData.postValue(true)
        }
    }

    /**
     * 退出房间
     */
    fun exitRoom() {
        CantataLogger.d(TAG, "RoomLivingViewModel.exitRoom() called")
        mCantataServiceProtocol.leaveRoom { e: Exception? ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.exitRoom() success")
                mRoomDeleteLiveData.postValue(false)
                mRoomTimeUpLiveData.postValue(false)
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed:${e.message} ")
                ToastUtils.showToast(e.message)
            }
        }
    }

    // ======================= 麦位相关 =======================

    // ======================= 麦位相关 =======================
    fun initSeats() {
        val roomInfo: JoinRoomOutputModel = mRoomInfoLiveData.value
            ?: throw java.lang.RuntimeException("The roomInfo must be not null before initSeats method calling!")
        val seatsArray: List<RoomSeatModel> = roomInfo.seatsArray ?: emptyList()
        mSeatListLiveData.postValue(seatsArray)
        for (roomSeatModel in seatsArray) {
            if (roomSeatModel.userNo == UserManager.getInstance().user.id.toString()) {
                mSeatLocalLiveData.setValue(roomSeatModel)
                mIsOnSeat = true
                if (mRtcEngine != null) {
                    mMainChannelMediaOption.publishCameraTrack =
                        roomSeatModel.isVideoMuted == RoomSeatModel.MUTED_VALUE_FALSE
                    mMainChannelMediaOption.publishMicrophoneTrack = true
                    mMainChannelMediaOption.enableAudioRecordingOrPlayout = true
                    mMainChannelMediaOption.autoSubscribeVideo = true
                    mMainChannelMediaOption.autoSubscribeAudio = true
                    mMainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    mRtcEngine?.updateChannelMediaOptions(mMainChannelMediaOption)
                    updateVolumeStatus(roomSeatModel.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                }
                break
            }
        }
        if (mSeatLocalLiveData.value == null) {
            mSeatLocalLiveData.setValue(null)
        }
        mCantataServiceProtocol.subscribeSeatList { ktvSubscribe: CantataServiceProtocol.KTVSubscribe,
                                                    roomSeatModel: RoomSeatModel? ->
            val roomSeat = roomSeatModel ?: return@subscribeSeatList
            if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeCreated) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeCreated")
                val oValue: List<RoomSeatModel> = mSeatListLiveData.value ?: return@subscribeSeatList
                val value: MutableList<RoomSeatModel> = ArrayList(oValue)
                value.add(roomSeat)

                mSeatListLiveData.postValue(value)
                if (roomSeat.userNo == UserManager.getInstance().user.id.toString()) {
                    mSeatLocalLiveData.setValue(roomSeat)
                    updateVolumeStatus(roomSeat.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                }
            } else if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated")
                val oValue: List<RoomSeatModel> = mSeatListLiveData.value ?: return@subscribeSeatList
                val value: MutableList<RoomSeatModel> = ArrayList<RoomSeatModel>(oValue)
                var index = -1
                for (i in value.indices) {
                    if (value[i].seatIndex == roomSeat.seatIndex) {
                        index = i
                        break
                    }
                }
                if (index != -1) {
                    value[index] = roomSeat
                    value.removeAt(index)
                    value.add(index, roomSeat)
                    mSeatListLiveData.postValue(value)
                    if (roomSeat.userNo == UserManager.getInstance().user.id.toString()) {
                        mSeatLocalLiveData.value = roomSeat
                        updateVolumeStatus(roomSeat.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                    }
                }
            } else if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted")
                val oValue: List<RoomSeatModel> = mSeatListLiveData.value ?: return@subscribeSeatList
                val value: MutableList<RoomSeatModel> = ArrayList(oValue)
                val iterator: MutableIterator<RoomSeatModel> = value.iterator()
                while (iterator.hasNext()) {
                    val next: RoomSeatModel = iterator.next()
                    if (next.userNo == roomSeat.userNo) {
                        iterator.remove()
                    }
                }
                mSeatListLiveData.postValue(value)
                if (roomSeat.userNo == UserManager.getInstance().user.id.toString()) {
                    mSeatLocalLiveData.postValue(null)
                    mIsOnSeat = false
                    mRtcEngine?.let {
                        mMainChannelMediaOption.publishCameraTrack = false
                        mMainChannelMediaOption.publishMicrophoneTrack = false
                        mMainChannelMediaOption.enableAudioRecordingOrPlayout = true
                        mMainChannelMediaOption.autoSubscribeVideo = true
                        mMainChannelMediaOption.autoSubscribeAudio = true
                        mMainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        it.updateChannelMediaOptions(mMainChannelMediaOption)
                    }

                    updateVolumeStatus(false)
                    val songPlayingData: RoomSelSongModel = mSongPlayingLiveData.value ?: return@subscribeSeatList
                    if (roomSeat.chorusSongCode == songPlayingData.songNo + songPlayingData.createAt) {
                        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
                        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
                    }
                }
            }
        }
    }

    fun soloSingerJoinChorusMode(isJoin: Boolean) {
        val songPlayingData = mSongPlayingLiveData.value ?: return
        val seatListData = mSeatListLiveData.value ?: return
        if (songPlayingData.userNo == UserManager.getInstance().user.id.toString()) {
            if (isJoin) {
                // 有人加入合唱
                mKtvApi.switchSingerRole(KTVSingRole.LeadSinger, null)
            } else {
                // 最后一人退出合唱
                mKtvApi.switchSingerRole(KTVSingRole.SoloSinger, null)
            }
        }
    }

    /**
     * 上麦
     */
    fun haveSeat(onSeatIndex: Int) {
        CantataLogger.d(TAG, "RoomLivingViewModel.haveSeat() called: $onSeatIndex")
        mCantataServiceProtocol.onSeat(OnSeatInputModel(onSeatIndex)) { e: Exception? ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.haveSeat() success")
                mIsOnSeat = true
                mRtcEngine?.let {
                    mMainChannelMediaOption.publishCameraTrack = false
                    mMainChannelMediaOption.publishMicrophoneTrack = true
                    mMainChannelMediaOption.enableAudioRecordingOrPlayout = true
                    mMainChannelMediaOption.autoSubscribeVideo = true
                    mMainChannelMediaOption.autoSubscribeAudio = true
                    mMainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    it.updateChannelMediaOptions(mMainChannelMediaOption)
                }
                toggleMic(false)
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.haveSeat() failed: " + e.message)
                ToastUtils.showToast(e.message)
            }
        }
    }

    /**
     * 离开麦位
     */
    fun leaveSeat(seatModel: RoomSeatModel) {
        CantataLogger.d(TAG, "RoomLivingViewModel.leaveSeat() called")
        mCantataServiceProtocol.outSeat(
            OutSeatInputModel(
                seatModel.userNo,
                seatModel.rtcUid,
                seatModel.name,
                seatModel.headUrl,
                seatModel.seatIndex
            )
        ) { e: java.lang.Exception? ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.leaveSeat() success")
                if (seatModel.userNo == UserManager.getInstance().user.id.toString()) {
                    mIsOnSeat = false
                    if (seatModel.isAudioMuted == RoomSeatModel.MUTED_VALUE_TRUE) {
                        if (mRtcEngine != null) {
                            mMainChannelMediaOption.publishCameraTrack = false
                            mMainChannelMediaOption.publishMicrophoneTrack = false
                            mMainChannelMediaOption.enableAudioRecordingOrPlayout = true
                            mMainChannelMediaOption.autoSubscribeVideo = true
                            mMainChannelMediaOption.autoSubscribeAudio = true
                            mMainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            mRtcEngine?.updateChannelMediaOptions(mMainChannelMediaOption)
                        }
                        updateVolumeStatus(false)
                    }
                }
                mSongPlayingLiveData.value?.let { songPlayingData ->
                    val isJoinChorus = seatModel.chorusSongCode == songPlayingData.songNo + songPlayingData.createAt
                    if (isJoinChorus && seatModel.userNo == UserManager.getInstance().user.id.toString()) {
                        leaveChorus()
                    }
                }

            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.leaveSeat() failed: " + e.message)
                ToastUtils.showToast(e.message)
            }
        }
    }

    /**
     * 静音
     */
    fun toggleMic(isUnMute: Boolean) {
        CantataLogger.d(TAG, "RoomLivingViewModel.toggleMic() called：$isUnMute")
        mCantataServiceProtocol.updateSeatAudioMuteStatus(!isUnMute) { e: java.lang.Exception? ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.toggleMic() success")
                updateVolumeStatus(isUnMute)
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.toggleMic() failed:${e.message}")
                ToastUtils.showToast(e.message)
            }
        }
    }

    private fun updateVolumeStatus(isUnMute: Boolean) {
        mKtvApi.setMicStatus(isUnMute)
        if (!isUnMute) {
            if (mMusicSetting?.isEar() == true) {
                mIsOpnEar = true
                mMusicSetting?.setEar(false)
            } else {
                mIsOpnEar = false
            }
        } else {
            mMusicSetting?.setEar(mIsOpnEar)
        }

        // 静音时将本地采集音量改为0
        mRtcEngine?.let {
            if (!isUnMute) it.adjustRecordingSignalVolume(0)
        }
        setMicVolume(micOldVolume)
    }


    // ======================= 歌曲相关 =======================
    fun initSongs() {
        mCantataServiceProtocol.subscribeChooseSong { ktvSubscribe: CantataServiceProtocol.KTVSubscribe?,
                                                      songModel: RoomSelSongModel? ->
            // 歌曲信息发生变化时，重新获取歌曲列表动作
            CantataLogger.d(TAG, "subscribeChooseSong updateSongs")
            onSongChanged()
        }

        // 获取初始歌曲列表
        onSongChanged()
    }

    private fun onSongChanged() {
        mCantataServiceProtocol.getChoosedSongsList { e: Exception?, data: List<RoomSelSongModel>? ->
            if (e == null && data != null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() success")
                mSongsOrderedLiveData.postValue(data)
                if (data.isNotEmpty()) {
                    val value: RoomSelSongModel? = mSongPlayingLiveData.value
                    val songPlaying: RoomSelSongModel = data[0]
                    if (value == null) {
                        // 无已点歌曲， 直接将列表第一个设置为当前播放歌曲
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() chosen song list is empty")
                        mSongPlayingLiveData.postValue(songPlaying)
                    } else if (value.songNo != songPlaying.songNo) {
                        // 当前有已点歌曲, 且更新歌曲和之前歌曲非同一首
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() single or first chorus")
                        mSongPlayingLiveData.postValue(songPlaying)
                    }
                } else {
                    CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList")
                    mSongPlayingLiveData.postValue(null)
                }
            } else {
                // failed
                if (e != null) {
                    CantataLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed:${e.message}")
                    ToastUtils.showToast(e.message)
                }
            }
        }
    }

    // 获取已选歌曲
    fun getSongChosenList() {
        mCantataServiceProtocol.getChoosedSongsList { e: Exception?, data: List<RoomSelSongModel>? ->
            if (e == null && data != null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() success")
                mSongsOrderedLiveData.postValue(data)
            } else {
                // failed
                if (e != null) {
                    CantataLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed:${e.message}")
                    ToastUtils.showToast(e.message)
                }
            }
        }
    }

    /**
     * 获取歌曲类型
     * @return map key: 类型名称，value: 类型值
     */
    fun getSongTypes(): LiveData<LinkedHashMap<Int, String>>? {
        CantataLogger.d(TAG, "RoomLivingViewModel.getSongTypes() called")
        val liveData = MutableLiveData<LinkedHashMap<Int, String>>()
        mKtvApi.fetchMusicCharts { requestId, status, list ->
            CantataLogger.d(TAG, "RoomLivingViewModel.getSongTypes() return")
            val types = LinkedHashMap<Int, String>()
            // 重新排序 ----> 按照（嗨唱推荐、抖音热歌、热门新歌、KTV必唱）这个顺序进行怕苦
            list?.let { resultList ->
                for (i in 0..3) {
                    for (musicChartInfo in resultList) {
                        if (i == 0 && musicChartInfo.type == 3 || i == 1 && musicChartInfo.type == 4 || i == 2 && musicChartInfo.type == 2 || i == 3 && musicChartInfo.type == 6
                        ) {
                            types[musicChartInfo.type] = musicChartInfo.name
                        }
                    }
                }
                // 将剩余的插到尾部
                for (musicChartInfo in resultList) {
                    if (!types.containsKey(musicChartInfo.type)) {
                        types[musicChartInfo.type] = musicChartInfo.name
                    }
                }
            }
            liveData.postValue(types)
        }
        return liveData
    }

    /**
     * 获取歌曲列表
     */
    fun getSongList(type: Int, page: Int): LiveData<List<RoomSelSongModel>>? {
        // 从RTC中获取歌曲列表
        CantataLogger.d(TAG, "RoomLivingViewModel.getSongList() called, type:$type page:$page")
        val liveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        mKtvApi.searchMusicByMusicChartId(type, page, 30, jsonOption)
        { requestId, status, page, pageSize, total, list ->
            CantataLogger.d(TAG, "RoomLivingViewModel.getSongList() return")
            list?.let {

            }
            val musicList: List<Music> = if (list.isNullOrEmpty()) emptyList() else ArrayList(listOf(*list))
            val songs: MutableList<RoomSelSongModel> = ArrayList()

            // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
            mCantataServiceProtocol.getChoosedSongsList { e: Exception?, songsChosen: List<RoomSelSongModel>? ->
                if (e == null && songsChosen != null) {
                    // success
                    for (music in musicList) {
                        var songItem: RoomSelSongModel? = null
                        for (roomSelSongModel in songsChosen) {
                            if (roomSelSongModel.songNo == music.songCode.toString()) {
                                songItem = roomSelSongModel
                                break
                            }
                        }
                        if (songItem == null) {
                            songItem = RoomSelSongModel(
                                music.name, music.songCode.toString(),
                                music.singer,
                                music.poster,
                                "",
                                "",
                                0,
                                0,
                                0,
                                0.0
                            )
                        }
                        songs.add(songItem)
                    }
                    liveData.postValue(songs)
                } else {
                    e?.let {
                        ToastUtils.showToast(e.message)
                    }
                }
            }
        }
        return liveData
    }

    /**
     * 搜索歌曲
     */
    fun searchSong(condition: String): LiveData<List<RoomSelSongModel>> {
        // 从RTC中搜索歌曲
        CantataLogger.d(TAG, "RoomLivingViewModel.searchSong() called, condition:$condition")
        val liveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()

        // 过滤没有歌词的歌曲
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        mKtvApi.searchMusicByKeyword(condition, 0, 50, jsonOption)
        { requestId, status, page, pageSize, total, list ->

            val musicList: List<Music> = if (list.isNullOrEmpty()) emptyList() else ArrayList(listOf(*list))
            val songs: MutableList<RoomSelSongModel> = ArrayList<RoomSelSongModel>()

            // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
            mCantataServiceProtocol.getChoosedSongsList { e: Exception?, songsChosen: List<RoomSelSongModel>? ->
                if (e == null && songsChosen != null) {
                    // success
                    for (music in musicList) {
                        var songItem: RoomSelSongModel? = null
                        for (roomSelSongModel in songsChosen) {
                            if (roomSelSongModel.songNo == music.songCode.toString()) {
                                songItem = roomSelSongModel
                                break
                            }
                        }
                        if (songItem == null) {
                            songItem = RoomSelSongModel(
                                music.name, music.songCode.toString(),
                                music.singer,
                                music.poster,
                                "",
                                "",
                                0,
                                0,
                                0,
                                0.0
                            )
                        }
                        songs.add(songItem)
                    }
                    liveData.postValue(songs)
                } else {
                    e?.let {
                        ToastUtils.showToast(it.message)
                    }
                }
            }

        }
        return liveData
    }

    /**
     * 点歌
     */
    fun chooseSong(songModel: RoomSelSongModel, isChorus: Boolean): LiveData<Boolean> {
        CantataLogger.d(TAG, "RoomLivingViewModel.chooseSong() called,name:${songModel.name},isChorus:$isChorus")
        val liveData = MutableLiveData<Boolean>()
        mCantataServiceProtocol.chooseSong(
            ChooseSongInputModel(
                songModel.songName,
                songModel.songNo,
                songModel.singer,
                songModel.imageUrl
            )
        ) { e: Exception? ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.chooseSong() success")
                liveData.postValue(true)
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.chooseSong() failed:${e.message}")
                ToastUtils.showToast(e.message)
                liveData.postValue(false)
            }
        }
        return liveData
    }

    /**
     * 删歌
     */
    fun deleteSong(songModel: RoomSelSongModel) {
        CantataLogger.d(TAG, "RoomLivingViewModel.deleteSong() called, name:" + songModel.name)
        mCantataServiceProtocol.removeSong(false, RemoveSongInputModel(songModel.songNo))
        { e: Exception? ->
            if (e == null) {
                // success: do nothing for subscriber dealing with the event already
                CantataLogger.d(TAG, "RoomLivingViewModel.deleteSong() success")
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.deleteSong() failed:${e.message}")
                ToastUtils.showToast(e.message)
            }
        }
    }

    /**
     * 置顶歌曲
     */
    fun topUpSong(songModel: RoomSelSongModel) {
        CantataLogger.d(TAG, "RoomLivingViewModel.topUpSong() called, name:${songModel.name}")
        mCantataServiceProtocol.makeSongTop(MakeSongTopInputModel(songModel.songNo)) { e: Exception? ->
            if (e == null) {
                // success: do nothing for subscriber dealing with the event already
                CantataLogger.d(TAG, "RoomLivingViewModel.topUpSong() success")
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.topUpSong() failed:${e.message}")
                ToastUtils.showToast(e.message)
            }
        }
    }

    /**
     * 点击加入合唱
     */
    fun joinChorus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.joinChorus() called")
        if (mRtcEngine!!.connectionState != CONNECTION_STATE_TYPE.getValue(CONNECTION_STATE_TYPE.CONNECTION_STATE_CONNECTED)) {
            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            ToastUtils.showToast(R.string.cantata_join_chorus_failed)
            return
        }
        val musicModel: RoomSelSongModel? = mSongPlayingLiveData.getValue()
        if (musicModel == null) {
            CantataLogger.e(TAG, "RoomLivingViewModel.joinChorus() failed, no song playing now")
            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            return
        }
        if (!mIsOnSeat) {
            // 不在麦上， 自动上麦
            mCantataServiceProtocol.autoOnSeat { err: Exception? ->
                if (err == null) {
                    mIsOnSeat = true
                    //自动开麦
                    mMainChannelMediaOption.publishMicrophoneTrack = true
                    mMainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    mRtcEngine?.updateChannelMediaOptions(mMainChannelMediaOption)
                    innerJoinChorus(musicModel.songNo)
                } else {
                    mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                    ToastUtils.showToast(err.message)
                }
            }
        } else {
            // 在麦上，直接加入合唱
            innerJoinChorus(musicModel.songNo)
        }
    }

    private fun innerJoinChorus(songCode: String) {
        val mainSingerUid = mSongPlayingLiveData.value?.userNo?.toInt() ?: return
        mKtvApi.loadMusic(songCode.toLong(),
            KTVLoadMusicConfiguration(
                songCode,
                false,
                mainSingerUid,
                KTVLoadMusicMode.LOAD_MUSIC_ONLY
            ),
            object : IMusicLoadStateListener {

                override fun onMusicLoadProgress(
                    songCode: Long,
                    percent: Int,
                    status: MusicLoadStatus,
                    msg: String?,
                    lyricUrl: String?
                ) {
                    CantataLogger.d(
                        TAG, "onMusicLoadProgress, songCode: $songCode percent: $percent lyricUrl: $lyricUrl"
                    )
                }

                override fun onMusicLoadFail(songCode: Long, reason: KTVLoadSongFailReason) {
                    ToastUtils.showToastLong(R.string.cantata_join_chorus_failed)
                    mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                }

                override fun onMusicLoadSuccess(songCode: Long, lyricUrl: String) {
                    mKtvApi.switchSingerRole(KTVSingRole.CoSinger, object : ISwitchRoleStateListener {
                        override fun onSwitchRoleFail(reason: SwitchRoleFailReason) {
                            ToastUtils.showToastLong(R.string.cantata_join_chorus_failed)
                            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                        }

                        override fun onSwitchRoleSuccess() {
                            if (mIsOnSeat) {
                                // 成为合唱成功
                                mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_CHORUS)

                                val inputModel = mSongPlayingLiveData.value ?: return
                                // 麦位UI 同步
                                mCantataServiceProtocol.joinChorus(inputModel) { e: Exception? ->
                                    if (e == null) {
                                        // success
                                        CantataLogger.d(TAG, "RoomLivingViewModel.joinChorus() success")
                                    } else {
                                        // failure
                                        CantataLogger.e(TAG, "RoomLivingViewModel.joinChorus() failed:${e.message}")
                                        ToastUtils.showToast(e.message)
                                    }
                                }
                            } else {
                                ToastUtils.showToastLong(R.string.cantata_join_chorus_failed)
                                mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
                                mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                            }
                        }
                    })
                }
            })
    }


    /**
     * 退出合唱
     */
    fun leaveChorus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called")
        if (mIsOnSeat) {
            mCantataServiceProtocol.leaveChorus { e: Exception? ->
                if (e == null) {
                    // success
                    CantataLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called")
                    mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
                    mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
                } else {
                    // failure
                    CantataLogger.e(TAG, "RoomLivingViewModel.leaveChorus() failed:${e.message}")
                    ToastUtils.showToast(e.message)
                }
                null
            }
        } else {
            mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
        }
    }

    /**
     * 开始切歌
     */
    fun changeMusic() {
        CantataLogger.d(TAG, "RoomLivingViewModel.changeMusic() called")
        val musicModel: RoomSelSongModel? = mSongPlayingLiveData.value
        if (musicModel == null) {
            CantataLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed, no song is playing now!")
            return
        }

        //ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, "", null);
        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_START)
        mCantataServiceProtocol.removeSong(true, RemoveSongInputModel(musicModel.songNo)) { e: Exception? ->
            if (e == null) {
                // success do nothing for dealing in song subscriber
                CantataLogger.d(TAG, "RoomLivingViewModel.changeMusic() success")
                mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END)
            } else {
                // failed
                CantataLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed:${e.message}")
                ToastUtils.showToast(e.message)
                mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END)
            }
        }
    }

    /**
     * 设置歌词view
     */
    fun setLrcView(view: ILrcView) {
        mKtvApi.setLrcView(view)
    }

    // ======================= Player/RTC/MPK相关 =======================
    private fun initRTCPlayer() {
        if (TextUtils.isEmpty(BuildConfig.AGORA_APP_ID)) {
            throw NullPointerException("please check \"strings_config.xml\"")
        }
        if (mRtcEngine != null) return
        // ------------------ 初始化RTC ------------------
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                // 网络状态回调, 本地user uid = 0
                if (uid == 0) {
                    mNetworkStatusLiveData.postValue(NetWorkEvent(txQuality, rxQuality))
                }
            }

            override fun onContentInspectResult(result: Int) {
                super.onContentInspectResult(result)
                if (result > 1) {
                    ToastUtils.showToast(R.string.cantata_content)
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
                        val lineScore: LineScore = LineScore()
                        lineScore.score = score
                        lineScore.index = index
                        lineScore.cumulativeScore = cumulativeScore
                        lineScore.total = total
                        mMainSingerScoreLiveData.postValue(lineScore)
                    } else if (jsonMsg.getString("cmd") == "SingingScore") {
                        val score = jsonMsg.getDouble("score").toFloat()
                        mPlayerMusicPlayCompleteLiveData.postValue(ScoringAverageModel(false, score.toInt()))
                    }
                } catch (exp: JSONException) {
                    CantataLogger.e(TAG, "onStreamMessage:$exp")
                }
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = Constants.AUDIO_SCENARIO_CHORUS
        try {
            mRtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            CantataLogger.e(TAG, "RtcEngine.create() called error: $e")
        }
        mRtcEngine?.loadExtensionProvider("agora_drm_loader")

        // ------------------ 场景化api初始化 ------------------
        mKtvApi.initialize(
            KTVApiConfig(
                BuildConfig.AGORA_APP_ID,
                mRoomInfoLiveData.value!!.agoraRTMToken,
                mRtcEngine!!,
                mRoomInfoLiveData.value!!.roomNo,
                UserManager.getInstance().user.id.toInt(),
                mRoomInfoLiveData.value!!.roomNo + "_ex",
                mRoomInfoLiveData.value!!.agoraChorusToken, 10, KTVType.Normal
            )
        )

        mKtvApi.addEventHandler(object : IKTVApiEventHandler() {
            override fun onMusicPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError, isLocal: Boolean) {
                when (state) {
                    MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        mPlayerMusicOpenDurationLiveData.postValue(mKtvApi.getMediaPlayer().duration)
                    }

                    MediaPlayerState.PLAYER_STATE_PLAYING -> {
                        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                    }

                    MediaPlayerState.PLAYER_STATE_PAUSED -> {
                        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE)
                    }

                    MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                        if (isLocal) {
                            mPlayerMusicPlayCompleteLiveData.postValue(ScoringAverageModel(true, 0))
                            mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET)
                        }
                    }

                    else -> {}
                }
            }
        })

        mKtvApi.renewInnerDataStreamId()

        // ------------------ 加入频道 ------------------
        mRtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            enableVideo()
            enableLocalVideo(false)
            enableAudio()
            setAudioProfile(
                Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
                Constants.AUDIO_SCENARIO_GAME_STREAMING
            )
            enableAudioVolumeIndication(50, 10, true)
            setClientRole(if (mIsOnSeat) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)
            val ret = joinChannel(
                mRoomInfoLiveData.value!!.agoraRTCToken,
                mRoomInfoLiveData.value!!.roomNo,
                null,
                UserManager.getInstance().user.id.toInt()
            )
            if (ret != Constants.ERR_OK) {
                CantataLogger.e(TAG, "joinRTC() called error: $ret")
            }
        }

        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "ktv")
            jsonObject.put("id", UserManager.getInstance().user.id.toString())
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectModule()
            module.interval = 30
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            mRtcEngine!!.enableContentInspect(true, contentInspectConfig)
        } catch (e: JSONException) {
            CantataLogger.e(TAG, e.toString())
        }

        // ------------------ 开启语音鉴定服务 ------------------
        moderationAudio(
            mRoomInfoLiveData.value!!.roomNo,
            UserManager.getInstance().user.id,
            AudioModeration.AgoraChannelType.rtc,
            "ktv",
            null,
            null
        )
    }

    private fun setAudioEffectPreset(effect: Int) {
        mRtcEngine?.setAudioEffectPreset(effect)
    }

    // ======================= settings =======================
    // ------------------ 音效调整 ------------------
    private fun getEffectIndex(index: Int): Int {
        when (index) {
            0 -> return Constants.AUDIO_EFFECT_OFF
            1 -> return Constants.ROOM_ACOUSTICS_KTV
            2 -> return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT
            3 -> return Constants.ROOM_ACOUSTICS_STUDIO
            4 -> return Constants.ROOM_ACOUSTICS_PHONOGRAPH
            5 -> return Constants.ROOM_ACOUSTICS_SPACIAL
            6 -> return Constants.ROOM_ACOUSTICS_ETHEREAL
            7 -> return Constants.STYLE_TRANSFORMATION_POPULAR
            8 -> return Constants.STYLE_TRANSFORMATION_RNB
        }
        // 原声
        return Constants.AUDIO_EFFECT_OFF
    }

    // ------------------ 音量调整 ------------------
    private var micVolume = 100
    private var micOldVolume = 100

    private fun setMusicVolume(v: Int) {
        mKtvApi.getMediaPlayer().adjustPlayoutVolume(v)
        mKtvApi.getMediaPlayer().adjustPublishSignalVolume(v)
    }

    private fun setMicVolume(v: Int) {
        val value: RoomSeatModel? = mSeatLocalLiveData.value
        val isMuted: Int = value?.isAudioMuted ?: RoomSeatModel.MUTED_VALUE_TRUE
        if (isMuted == RoomSeatModel.MUTED_VALUE_TRUE) {
            micOldVolume = v
            CantataLogger.d(TAG, "muted! setMicVolume: $v")
            return
        }
        CantataLogger.d(TAG, "unmute! setMicVolume: $v")
        micVolume = v
        mRtcEngine?.adjustRecordingSignalVolume(v)
    }

    // ------------------ 原唱/伴奏 ------------------
    private var mAudioTrackMode = KTVPlayerTrackMode.Acc

    fun musicToggleOriginal() {
        mAudioTrackMode = if (mAudioTrackMode == KTVPlayerTrackMode.Origin) {
            mKtvApi.getMediaPlayer().selectAudioTrack(1)
            KTVPlayerTrackMode.Acc
        } else {
            mKtvApi.getMediaPlayer().selectAudioTrack(0)
            KTVPlayerTrackMode.Origin
        }
    }

    fun isOriginalMode(): Boolean {
        return mAudioTrackMode == KTVPlayerTrackMode.Origin
    }

    // ------------------ 暂停/播放 ------------------
    fun musicToggleStart() {
        if (mPlayerMusicStatusLiveData.value == PlayerMusicStatus.ON_PLAYING) {
            mKtvApi.pauseSing()
        } else if (mPlayerMusicStatusLiveData.value == PlayerMusicStatus.ON_PAUSE) {
            mKtvApi.resumeSing()
        }
    }

    // ------------------ 重置歌曲状态(歌曲切换时) ------------------
    fun resetMusicStatus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.resetMusicStatus() called")
        mChorusNum = 0
        mRetryTimes = 0
        mAudioTrackMode = KTVPlayerTrackMode.Acc
        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_IDLE)
        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
    }

    // ------------------ 歌曲开始播放 ------------------
    fun musicStartPlay(music: RoomSelSongModel) {
        CantataLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called")
        if (music.userNo == null) return
        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE)
        val isOwnSong = music.userNo == UserManager.getInstance().user.id.toString()
        val songCode: Long = music.songNo.toLong()
        val mainSingerUid: Int = music.userNo.toInt()
        if (isOwnSong) {
            // 主唱加载歌曲
            loadMusic(
                KTVLoadMusicConfiguration(
                    music.songNo,
                    true,
                    mainSingerUid,
                    KTVLoadMusicMode.LOAD_MUSIC_AND_LRC
                ), songCode
            )
        } else {
            if (mSeatLocalLiveData.value != null &&
                mSeatLocalLiveData.value!!.chorusSongCode == music.songNo + music.createAt
            ) {
                // 合唱者
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo,
                        false,
                        mainSingerUid,
                        KTVLoadMusicMode.LOAD_LRC_ONLY
                    ), songCode
                )
                // 加入合唱
                innerJoinChorus(music.songNo)
            } else {
                // 观众
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo,
                        false,
                        mainSingerUid,
                        KTVLoadMusicMode.LOAD_LRC_ONLY
                    ), songCode
                )
            }
        }

        // 标记歌曲为播放中
        mCantataServiceProtocol.makeSongDidPlay(music) { e: java.lang.Exception? ->
            e?.let { // failure
                ToastUtils.showToast(it.message)
            }
        }
    }

    private fun loadMusic(config: KTVLoadMusicConfiguration, songCode: Long) {
        mKtvApi.loadMusic(songCode, config, object : IMusicLoadStateListener {
            override fun onMusicLoadProgress(
                songCode: Long,
                percent: Int,
                status: MusicLoadStatus,
                msg: String?,
                lyricUrl: String?
            ) {
                CantataLogger.d(TAG, "onMusicLoadProgress, songCode: $songCode percent: $percent lyricUrl: $lyricUrl")
            }

            override fun onMusicLoadSuccess(songCode: Long, lyricUrl: String) {
                // 当前已被切歌
                if (mSongPlayingLiveData.value == null) {
                    ToastUtils.showToastLong(R.string.cantata_load_failed_no_song)
                    return
                }

                // 重置settings
                mRetryTimes = 0
                mMusicSetting?.setVolMic(100)
                mMusicSetting?.setVolMusic(50)
                mKtvApi.getMediaPlayer().adjustPlayoutVolume(50)
                mKtvApi.getMediaPlayer().adjustPublishSignalVolume(50)
                mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
            }

            override fun onMusicLoadFail(songCode: Long, reason: KTVLoadSongFailReason) {
                // 当前已被切歌
                if (mSongPlayingLiveData.value == null) {
                    ToastUtils.showToastLong(R.string.cantata_load_failed_no_song)
                    return
                }
                CantataLogger.e(TAG, "onMusicLoadFail， reason: $reason")
                if (reason == KTVLoadSongFailReason.NO_LYRIC_URL) {
                    // 未获取到歌词 正常播放
                    mRetryTimes = 0
                    mMusicSetting?.setVolMic(100)
                    mMusicSetting?.setVolMusic(50)
                    mKtvApi.getMediaPlayer().adjustPlayoutVolume(50)
                    mKtvApi.getMediaPlayer().adjustPublishSignalVolume(50)
                    mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                    mNoLrcLiveData.postValue(true)
                } else if (reason == KTVLoadSongFailReason.MUSIC_PRELOAD_FAIL) {
                    // 歌曲加载失败 ，重试3次
                    ToastUtils.showToastLong(R.string.cantata_load_failed)
                    mRetryTimes += 1
                    if (mRetryTimes < 3) {
                        loadMusic(config, songCode)
                    } else {
                        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                        ToastUtils.showToastLong(R.string.cantata_try)
                    }
                } else if (reason == KTVLoadSongFailReason.CANCELED) {
                    // 当前已被切歌
                    ToastUtils.showToastLong(R.string.cantata_load_failed_another_song)
                }
            }
        })
    }

    // ------------------ 重新获取歌词url ------------------
    fun reGetLrcUrl() {
        val songPlayingModel = mSongPlayingLiveData.value ?: return
        loadMusic(
            KTVLoadMusicConfiguration(
                songPlayingModel.songNo,
                true,
                songPlayingModel.userNo!!.toInt(),
                KTVLoadMusicMode.LOAD_LRC_ONLY
            ), songPlayingModel.songNo.toLong()
        )
    }

    // ------------------ 歌曲seek ------------------
    fun musicSeek(time: Long) {
        mKtvApi.seekSing(time)
    }

    fun getSongDuration(): Long? {
        return mKtvApi.getMediaPlayer().getDuration()
    }

    // ------------------ 歌曲结束播放 ------------------
    fun musicStop() {
        CantataLogger.d(TAG, "RoomLivingViewModel.musicStop() called")
        // 列表中无歌曲， 还原状态
        resetMusicStatus()
    }

    fun onStart() {
        if (mIsBackPlay) {
            mKtvApi.getMediaPlayer().mute(false)
        }
    }

    fun onStop() {
        if (mIsBackPlay) {
            mKtvApi.getMediaPlayer().mute(true)
        }
    }

    // ------------------ 歌词组件相关 ------------------
    fun syncSingleLineScore(score: Int, cumulativeScore: Int, index: Int, total: Int) {
        if (mRtcEngine == null) return
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "singleLineScore"
        msg["score"] = score
        msg["index"] = index
        msg["cumulativeScore"] = cumulativeScore
        msg["total"] = total
        val jsonMsg = JSONObject(msg)
        val ret = mRtcEngine!!.sendStreamMessage(mStreamId, jsonMsg.toString().toByteArray())
        if (ret < 0) {
            CantataLogger.e(TAG, "syncSingleLineScore() sendStreamMessage called returned: $ret")
        }
    }

    fun syncSingingAverageScore(score: Double) {
        if (mRtcEngine == null) return
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "SingingScore"
        msg["score"] = score
        val jsonMsg = JSONObject(msg)
        val ret = mRtcEngine!!.sendStreamMessage(mStreamId, jsonMsg.toString().toByteArray())
        if (ret < 0) {
            CantataLogger.e(TAG, "syncSingingAverageScore() sendStreamMessage called returned: $ret")
        }
    }
}
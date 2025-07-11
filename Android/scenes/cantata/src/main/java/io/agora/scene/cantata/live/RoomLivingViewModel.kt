package io.agora.scene.cantata.live

import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.musiccontentcenter.Music
import io.agora.rtc2.*
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.ContentInspectModule
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.AudioModeration.moderationAudio
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.event.NetWorkEvent
import io.agora.scene.base.manager.UserManager
import io.agora.scene.cantata.CantataLogger
import io.agora.scene.cantata.R
import io.agora.scene.cantata.debugSettings.CantataDebugSettingBean
import io.agora.scene.cantata.debugSettings.CantataDebugSettingsDialog
import io.agora.ktvapi.*
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.cantata.live.bean.MusicSettingBean
import io.agora.scene.cantata.live.fragmentdialog.MusicSettingCallback
import io.agora.scene.cantata.service.*
import io.agora.scene.cantata.widget.rankList.RankItem
import io.agora.scene.widget.toast.CustomToast
import org.json.JSONException
import org.json.JSONObject

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

/**
 * 房间详情 viewModel
 */
class RoomLivingViewModel constructor(joinRoomOutputModel: JoinRoomOutputModel) : ViewModel() {

    companion object {
        private const val TAG = "Cantata_Scene_Log"
    }

    private val mCantataServiceProtocol = CantataServiceProtocol.getImplInstance()

    private lateinit var mKtvApi: KTVApi

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
    val mRoomNoSongsLiveData = MutableLiveData<Boolean>()

    val mRoomUserCountLiveData = MutableLiveData<Int>()

    /**
     * 麦位信息
     */
    var mIsOnSeat = false
    val mSeatListLiveData: MutableLiveData<List<RoomSeatModel>> = MutableLiveData<List<RoomSeatModel>>(emptyList())
    val scoreMap = mutableMapOf<String, UserModel>()
    val mSeatLocalLiveData: MutableLiveData<RoomSeatModel> = MutableLiveData<RoomSeatModel>()

    /**
     * 歌词信息
     */
    val mSongsOrderedLiveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()
    val mSongPlayingLiveData: MutableLiveData<RoomSelSongModel> = MutableLiveData<RoomSelSongModel>()

    /**
     * Player/RTC信息
     */
    var mStreamId = 0

    val mPlayerMusicStatusLiveData = MutableLiveData<PlayerMusicStatus>()

    // 加载音乐进度
    val loadMusicProgressLiveData = MutableLiveData<Int>()

    val mJoinChorusStatusLiveData = MutableLiveData<JoinChorusStatus>()
    val mNoLrcLiveData = MutableLiveData<Boolean>()

    val mPlayerMusicOpenDurationLiveData = MutableLiveData<Long>()
    val mNetworkStatusLiveData: MutableLiveData<NetWorkEvent> = MutableLiveData<NetWorkEvent>()

    val mScoringAlgoControlLiveData: MutableLiveData<ScoringAlgoControlModel> =
        MutableLiveData<ScoringAlgoControlModel>()

    // 是否显示结算页面
    val mRoundRankListLiveData = MutableLiveData<Boolean>()

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
     * 开发者模式
     */
    var mDebugSetting: CantataDebugSettingBean? = null

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

    fun getSDKBuildNum(): String? {
        return RtcEngineEx.getSdkVersion()
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
                mSeatListLiveData.postValue(data?: emptyList())
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
        mCantataServiceProtocol.subscribeRoomStatusChanged { ktvSubscribe: CantataServiceProtocol.KTVSubscribe,
                                                             vlRoomListModel: RoomListModel? ->
            if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted")
                mRoomDeleteLiveData.postValue(true)
            } else if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                // 当房间内状态发生改变时触发
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated")
                vlRoomListModel ?: return@subscribeRoomStatusChanged
                if (vlRoomListModel.bgOption != roomInfo.bgOption) {
                    mRoomInfoLiveData.postValue(
                        JoinRoomOutputModel(
                            roomInfo.roomName,
                            roomInfo.roomNo,
                            roomInfo.creatorNo,
                            roomInfo.creatorAvatar,
                            vlRoomListModel.bgOption,
                            roomInfo.seatsArray,
                            roomInfo.roomPeopleNum,
                            roomInfo.agoraRTMToken,
                            roomInfo.agoraRTCToken,
                            roomInfo.agoraChorusToken,
                            roomInfo.agoraMusicToken,
                            roomInfo.createdAt,
                            roomInfo.steamMode
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
                mRoomNoSongsLiveData.postValue(false)
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed:${e.message} ")
                e.message?.let {
                    CustomToast.show(it)
                }
            }
        }
    }

    // ======================= 麦位相关 =======================
    private fun initSeats() {
        mCantataServiceProtocol.getSeatStatusList { e, list ->
            if (e == null && list != null) {
                mSeatListLiveData.value = list
                for (roomSeatModel in list) {
                    if (roomSeatModel.userNo == UserManager.getInstance().user.id.toString()) {
                        mSeatLocalLiveData.postValue(roomSeatModel)
                        mIsOnSeat = true
                        if (mRtcEngine != null) {
                            updateVolumeStatus(roomSeatModel.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                        }

                        mSongPlayingLiveData.value?.let {
                            innerJoinChorus(it.songNo, it.userNo?.toInt())
                        }
                        break
                    }
                }
            }
        }
        mCantataServiceProtocol.subscribeSeatListChanged { ktvSubscribe: CantataServiceProtocol.KTVSubscribe,
                                                           roomSeatModel: RoomSeatModel? ->
            val roomSeat = roomSeatModel ?: return@subscribeSeatListChanged
            onMicSeatChange()
            if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeCreated) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeCreated")
                val oValue: List<RoomSeatModel> = mSeatListLiveData.value ?: return@subscribeSeatListChanged
                val value: MutableList<RoomSeatModel> = ArrayList(oValue)
                value.add(roomSeat)

                //mSeatListLiveData.postValue(value)
                if (roomSeat.userNo == UserManager.getInstance().user.id.toString()) {
                    mSeatLocalLiveData.value = roomSeat
                    updateVolumeStatus(roomSeat.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                }
            } else if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated")
                val oValue: List<RoomSeatModel> = mSeatListLiveData.value ?: return@subscribeSeatListChanged
                val value: MutableList<RoomSeatModel> = ArrayList<RoomSeatModel>(oValue)
                var index = -1
                for (i in value.indices) {
                    if (value[i].rtcUid == roomSeat.rtcUid) {
                        index = i
                        break
                    }
                }
                if (index != -1) {
                    value[index] = roomSeat
                    value.removeAt(index)
                    value.add(index, roomSeat)
                    //mSeatListLiveData.value = value
                    if (roomSeat.userNo == UserManager.getInstance().user.id.toString()) {
                        mSeatLocalLiveData.value = roomSeat
                        updateVolumeStatus(roomSeat.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                    }
                }
            } else if (ktvSubscribe == CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                CantataLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted")
                val oValue: List<RoomSeatModel> = mSeatListLiveData.value ?: return@subscribeSeatListChanged
                val value: MutableList<RoomSeatModel> = ArrayList(oValue)
                val iterator: MutableIterator<RoomSeatModel> = value.iterator()
                while (iterator.hasNext()) {
                    val next: RoomSeatModel = iterator.next()
                    if (next.userNo == roomSeat.userNo) {
                        iterator.remove()
                    }
                }
                //mSeatListLiveData.value = value
                if (roomSeat.userNo == UserManager.getInstance().user.id.toString()) {
                    if (!leaveSeatBySelf) {
                        CustomToast.show(R.string.cantata_kick_off)
                    } else {
                        leaveSeatBySelf = false
                    }
                    mSeatLocalLiveData.value = null
                    mIsOnSeat = false

                    updateVolumeStatus(false)
                    val songPlayingData: RoomSelSongModel =
                        mSongPlayingLiveData.value ?: return@subscribeSeatListChanged
                    if (roomSeat.userNo != songPlayingData.userNo) {
                        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
                        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
                    }
                }
            }
        }
    }

    private fun onMicSeatChange() {
        mCantataServiceProtocol.getSeatStatusList { e, list ->
            if (e == null && list != null) {
                mSeatListLiveData.value = list
                list.forEach {
                    scoreMap[it.rtcUid] = UserModel(
                        it.name,
                        it.headUrl,
                        it.score
                    )
                }
            }
        }
    }

    /**
     * 上麦
     */
    fun haveSeat() {
        CantataLogger.d(TAG, "RoomLivingViewModel.haveSeat() called")
        val score = if (scoreMap.containsKey(UserManager.getInstance().user.id.toString())) {
            scoreMap[UserManager.getInstance().user.id.toString()]!!.score
        } else {
            0
        }
        mCantataServiceProtocol.onSeat(OnSeatInputModel(score)) { e: Exception? ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.haveSeat() success")
                mIsOnSeat = true
                toggleMic(true)
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.haveSeat() failed: " + e.message)
                e.message?.let {
                    CustomToast.show(it)
                }
            }
        }
    }

    /**
     * 离开麦位
     */
    fun leaveSeat(seatModel: RoomSeatModel) {
        CantataLogger.d(TAG, "RoomLivingViewModel.leaveSeat() called")
        if (seatModel.userNo == UserManager.getInstance().user.id.toString()) {
            leaveSeatBySelf = true
        }
        mCantataServiceProtocol.leaveSeat(
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
                e.message?.let {
                    CustomToast.show(it)
                }
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
                e.message?.let {
                    CustomToast.show(it)
                }
            }
        }
    }

    private fun updateVolumeStatus(isUnMute: Boolean) {
        CantataLogger.d(TAG, "RoomLivingViewModel.updateVolumeStatus() isUnMute:$isUnMute")
        mKtvApi.muteMic(!isUnMute)
//        if (!isUnMute) {
//            if (mMusicSetting?.isEar() == true) {
//                mIsOpnEar = true
//                mMusicSetting?.setEar(false)
//            } else {
//                mIsOpnEar = false
//            }
//        } else {
//            mMusicSetting?.setEar(mIsOpnEar)
//        }

        // 静音时将本地采集音量改为0
        mRtcEngine?.let {
            if (!isUnMute) it.adjustRecordingSignalVolume(0)
        }
        setMicVolume(micOldVolume)
    }


    // ======================= 歌曲相关 =======================
    private fun initSongs() {
        mCantataServiceProtocol.subscribeChooseSongChanged { ktvSubscribe: CantataServiceProtocol.KTVSubscribe?,
                                                             songModel: RoomSelSongModel? ->
            // 歌曲信息发生变化时，重新获取歌曲列表动作
            CantataLogger.d(TAG, "subscribeChooseSong updateSongs")
            onSongChanged()
            mSeatListLiveData.postValue(mSeatListLiveData.value)
        }

        // 获取初始歌曲列表
        mCantataServiceProtocol.getChoosedSongsList { e: Exception?, data: List<RoomSelSongModel>? ->
            if (e == null && data != null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() success")
                mSongsOrderedLiveData.postValue(data)
                if (data.isNotEmpty()) {
                    val value: RoomSelSongModel? = mSongPlayingLiveData.value
                    val songPlaying: RoomSelSongModel = data[0]
                    if (value == null && !songPlaying.musicEnded) {
                        // 无已点歌曲， 直接将列表第一个设置为当前播放歌曲
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() chosen song list is empty")
                        mSongPlayingLiveData.value = songPlaying
                        if (mIsOnSeat && songPlaying.userNo?.equals(UserManager.getInstance().user.id.toString()) != true) {
                            innerJoinChorus(songPlaying.songNo, songPlaying.userNo?.toInt())
                        }
                    } else if (songPlaying.musicEnded) {
                        // 音乐结束
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() music ended")
                        mRoundRankListLiveData.postValue(true)
                    }
                }
                mSeatListLiveData.postValue(mSeatListLiveData.value)
            } else {
                // failed
                CantataLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed:${e?.message}")
                e?.message?.let {
                    CustomToast.show(it)
                }
            }
        }
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
                        resetMusicStatus()
                        mSongPlayingLiveData.postValue(songPlaying)
                    } else if (!value.musicEnded && songPlaying.musicEnded) {
                        // 音乐结束
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() music ended")
                        mRoundRankListLiveData.postValue(true)
                    }
                } else {
                    CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList")
                    mSongPlayingLiveData.postValue(null)
                }
            } else {
                // failed
                CantataLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed:${e?.message}")
                e?.message?.let {
                    CustomToast.show(it)
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
                CantataLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed:${e?.message}")
                e?.message?.let {
                    CustomToast.show(it)
                }
            }
        }
    }

    /**
     * 获取歌曲类型
     * @return map key: 类型名称，value: 类型值
     */
    fun getSongTypes(): LiveData<LinkedHashMap<Int, String>> {
        CantataLogger.d(TAG, "RoomLivingViewModel.getSongTypes() called")
        val liveData = MutableLiveData<LinkedHashMap<Int, String>>()
        mKtvApi.fetchMusicCharts { requestId, status, list ->
            CantataLogger.d(TAG, "RoomLivingViewModel.getSongTypes() return, requestId:$requestId, status:$status")
            val types = LinkedHashMap<Int, String>()
            // 重新排序 ----> 按照（嗨唱推荐、抖音热歌、热门新歌、KTV必唱）这个顺序进行排序
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
            // 因为榜单基本是固化的，防止拉取列表失败，直接写入配置
            if (list == null || list.isEmpty()) {
                types[3] = "嗨唱推荐"
                types[4] = "抖音热歌"
                types[2] = "新歌榜"
                types[6] = "KTV必唱"
                types[0] = "项目热歌榜单"
                types[1] = "声网热歌榜"
                types[5] = "古风热歌"
            }
            liveData.postValue(types)
        }
        return liveData
    }

    /**
     * 获取歌曲列表
     */
    fun getSongList(type: Int, page: Int): LiveData<List<RoomSelSongModel>> {
        // 从RTC中获取歌曲列表
        CantataLogger.d(TAG, "RoomLivingViewModel.getSongList() called, type:$type page:$page")
        val liveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()
        val jsonOption = "{\"pitchType\":2,\"needLyric\":true}"
        mKtvApi.searchMusicByMusicChartId(0, page, 30, jsonOption)
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
                    e?.message?.let {
                        CustomToast.show(it)
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
        val jsonOption = if (KTVApi.debugMode) {
            "{\"pitchType\":1,\"needLyric\":true}"
        } else {
            "{\"pitchType\":2,\"needLyric\":true}"
        }
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
                    e?.message?.let {
                        CustomToast.show(it)
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
                e.message?.let {
                    CustomToast.show(it)
                }
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
                e?.message?.let {
                    CustomToast.show(it)
                }
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
                e.message?.let {
                    CustomToast.show(it)
                }
            }
        }
    }

    /**
     * 点击加入合唱
     */
    fun joinChorus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.joinChorus() called")
        val musicModel: RoomSelSongModel? = mSongPlayingLiveData.value
        if (musicModel == null) {
            CantataLogger.e(TAG, "RoomLivingViewModel.joinChorus() failed, no song playing now")
            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            return
        }

        innerJoinChorus(musicModel.songNo, musicModel.userNo?.toInt())
    }

    private fun innerJoinChorus(songCode: String, uid: Int?) {
        val mainSingerUid = uid ?: return
        mKtvApi.loadMusic(songCode.toLong(),
            KTVLoadMusicConfiguration(
                songCode,
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
                    loadMusicProgressLiveData.postValue(percent)
                }

                override fun onMusicLoadFail(songCode: Long, reason: KTVLoadMusicFailReason) {
                    CustomToast.show(R.string.cantata_join_chorus_failed, Toast.LENGTH_LONG)
                    mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                }

                override fun onMusicLoadSuccess(songCode: Long, lyricUrl: String) {
                    mKtvApi.switchSingerRole(KTVSingRole.CoSinger, object : ISwitchRoleStateListener {
                        override fun onSwitchRoleFail(reason: SwitchRoleFailReason) {
                            CustomToast.show(R.string.cantata_join_chorus_failed, Toast.LENGTH_LONG)
                            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                        }

                        override fun onSwitchRoleSuccess() {
                            if (mIsOnSeat) {
                                // 成为合唱成功
                                mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_CHORUS)
                                mAudioTrackMode = KTVPlayerTrackMode.Acc
                            } else {
                                // 不在麦上， 自动上麦
                                mCantataServiceProtocol.onSeat(OnSeatInputModel(0)) { err: Exception? ->
                                    if (err == null) {
                                        CantataLogger.d(TAG, "RoomLivingViewModel.onSeat() success")
                                        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_CHORUS)
                                        mAudioTrackMode = KTVPlayerTrackMode.Acc
                                        mIsOnSeat = true
                                    } else {
                                        CantataLogger.d(TAG, "RoomLivingViewModel.onSeat() failed: ${err.message}" )
                                        CustomToast.show(R.string.cantata_join_chorus_failed, Toast.LENGTH_LONG)
                                        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
                                        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                                    }
                                }
                            }
                        }
                    })
                }
            })
    }


    /**
     * 退出合唱
     */
    private var leaveSeatBySelf = false
    fun leaveChorus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called")
        if (mIsOnSeat) {
            // 下麦
            mSeatLocalLiveData.value?.let { leaveSeat(it) }
        }
        // 离开合唱
        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
        mMusicSetting?.mEarBackEnable = false
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
                e.message?.let {
                    CustomToast.show(it)
                }
                mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END)
            }
        }
    }

    // 点击合唱
    fun clickChorusUser() {

    }

    /**
     * 设置歌词view
     */
    fun setLrcView(view: ILrcView) {
        mKtvApi.setLrcView(view)
    }

    // ======================= Player/RTC/MPK相关 =======================
    private fun initRTCPlayer() {
        // ------------------ 初始化音乐播放设置面版 ------------------
        mMusicSetting = MusicSettingBean(
            object : MusicSettingCallback {
                override fun onEarChanged(isEar: Boolean) {
                    val isMuted: Int =
                        if (mSeatLocalLiveData.value != null) mSeatLocalLiveData.value!!.isAudioMuted else return
                    if (isMuted == 1) {
                        mIsOpnEar = isEar
                        return
                    }
                    mRtcEngine?.enableInEarMonitoring(isEar, Constants.EAR_MONITORING_FILTER_NONE)
                }

                override fun onMicVolChanged(vol: Int) {
                    setMicVolume(vol)
                }

                override fun onAccVolChanged(vol: Int) {
                    setMusicVolume(vol)
                }

                override fun onAudioEffectChanged(audioEffect: Int) {
                    setAudioEffectPreset(audioEffect)
                }

                override fun onRemoteVolumeChanged(volume: Int) {
                    KTVApi.remoteVolume = volume
                    mRtcEngine?.adjustPlaybackSignalVolume(volume)
                }

                override fun onEarBackVolumeChanged(volume: Int) {
                    CantataLogger.d(TAG, "onEarBackVolumeChanged: $volume")
                    mRtcEngine?.setInEarMonitoringVolume(volume)
                }

                override fun onEarBackModeChanged(mode: Int) {
                    CantataLogger.d(TAG, "onEarBackModeChanged: $mode")
                    if (mode == 1) {
                        // OpenSL
                        mRtcEngine?.setParameters("{\"che.audio.opensl.mode\": 0}")
                    } else if (mode == 2) {
                        // Oboe
                        mRtcEngine?.setParameters("{\"che.audio.oboe.enable\": true}")
                    }
                }
            })

        if (mRtcEngine != null) return
        // ------------------ 初始化RTC ------------------
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = if (SceneConfigManager.cantataAppId == "") BuildConfig.AGORA_APP_ID else SceneConfigManager.cantataAppId
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
                    CustomToast.show(R.string.cantata_content)
                }
            }

            override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray) {

            }

            override fun onAudioRouteChanged(routing: Int) {
                super.onAudioRouteChanged(routing)
                CantataLogger.d(TAG, "onAudioRouteChanged, routing:$routing")
                mMusicSetting?.let { setting->
                    // 0\2\5\6\10 earPhone
                    if (routing == Constants.AUDIO_ROUTE_HEADSET ||
                        routing == Constants.AUDIO_ROUTE_HEADSETNOMIC ||
                        routing == Constants.AUDIO_ROUTE_BLUETOOTH_DEVICE_HFP ||
                        routing == Constants.AUDIO_ROUTE_USBDEVICE ||
                        routing == Constants.AUDIO_ROUTE_BLUETOOTH_DEVICE_A2DP
                    ) {
                        setting.mHasEarPhone = true
                    } else {
                        if (mSongPlayingLiveData.value != null && setting.mEarBackEnable) {
                            CustomToast.show(R.string.cantat_earback_close_tip, Toast.LENGTH_SHORT)
                            setting.mEarBackEnable = false
                        }
                        setting.mHasEarPhone = false
                    }
                }
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING
        try {
            mRtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            CantataLogger.e(TAG, "RtcEngine.create() called error: $e")
        }
        mRtcEngine?.loadExtensionProvider("agora_drm_loader")

        // ------------------ 场景化api初始化 ------------------
        KTVApi.debugMode = AgoraApplication.the().isDebugModeOpen
        if (AgoraApplication.the().isDebugModeOpen) {
            KTVApi.mccDomain = "api-test.agora.io"
        }
        mKtvApi = createKTVGiantChorusApi(
            KTVGiantChorusApiConfig(
                if (SceneConfigManager.cantataAppId == "") BuildConfig.AGORA_APP_ID else SceneConfigManager.cantataAppId,
                mRoomInfoLiveData.value!!.agoraRTMToken,
                mRtcEngine!!,
                UserManager.getInstance().user.id.toInt(),
                mRoomInfoLiveData.value!!.roomNo + "_ad",
                mRoomInfoLiveData.value!!.agoraRTCToken,
                mRoomInfoLiveData.value!!.roomNo,
                mRoomInfoLiveData.value!!.agoraChorusToken,
                2023,
                mRoomInfoLiveData.value!!.agoraMusicToken,
                10,
                KTVMusicType.SONG_CODE
            )
        )

        when (mRoomInfoLiveData.value!!.steamMode) {
            0 -> KTVApi.routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.RANDOM, 6)
            1 -> KTVApi.routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.BY_DELAY, 6)
            2 -> KTVApi.routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.TOP_N, 6)
            3 -> KTVApi.routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.BY_DELAY_AND_TOP_N, 6)
        }
        CantataLogger.d("hugohugo", "GiantChorusRouteSelectionConfig: ${KTVApi.routeSelectionConfig}")

        mKtvApi.addEventHandler(object : IKTVApiEventHandler() {
            override fun onMusicPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerReason, isLocal: Boolean) {
                when (state) {
                    MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        if (!isLocal || (mSongPlayingLiveData.value != null && mSongPlayingLiveData.value!!.userNo == UserManager.getInstance().user.id.toString())) {
                            scoreMap.clear()
                        }
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
                            mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET)
                        }
                        mSongPlayingLiveData.value?.let { roomSelSongModel ->
                            if (roomSelSongModel.userNo == UserManager.getInstance().user.id.toString()) {
                                mCantataServiceProtocol.markSongEnded(roomSelSongModel, completion = {
                                    //mRoundRankListLiveData.postValue(true)
                                })
                            }
                        }
                    }

                    else -> {}
                }
            }
        })

        // ------------------ 加入频道 ------------------
        mRtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            enableVideo()
            enableLocalVideo(false)
            enableAudio()
            setAudioProfile(
                Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO,
                Constants.AUDIO_SCENARIO_GAME_STREAMING
            )
            setClientRole(if (mIsOnSeat) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)
            val options = ChannelMediaOptions()
            options.autoSubscribeAudio = true
            val ret = joinChannelEx(
                mRoomInfoLiveData.value!!.agoraRTCToken,
                RtcConnection(mRoomInfoLiveData.value!!.roomNo + "_ad", UserManager.getInstance().user.id.toInt()),
                options,
                object : IRtcEngineEventHandler() {
                    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                        // 网络状态回调, 本地user uid = 0
                        if (uid == 0) {
                            mNetworkStatusLiveData.postValue(NetWorkEvent(txQuality, rxQuality))
                        }
                    }

                    override fun onContentInspectResult(result: Int) {
                        super.onContentInspectResult(result)
                        if (result > 1) {
                            CustomToast.show(R.string.cantata_content)
                        }
                    }

                    override fun onAudioMetadataReceived(uid: Int, data: ByteArray?) {
                        (mKtvApi as KTVGiantChorusApiImpl).setAudienceAudioMetadataReceived(uid, data)
                    }
                }
            )
            setParametersEx("{\"rtc.use_audio4\": true}", RtcConnection(mRoomInfoLiveData.value!!.roomNo + "_ad", UserManager.getInstance().user.id.toInt()))
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
            mRtcEngine?.enableContentInspect(true, contentInspectConfig)
        } catch (e: JSONException) {
            CantataLogger.e(TAG, e.toString())
        }

        // ------------------ Start voice moderation service ------------------
        moderationAudio(
            mRoomInfoLiveData.value!!.roomNo,
            UserManager.getInstance().user.id,
            AudioModeration.AgoraChannelType.Rtc,
            "ktv",
            null,
            null
        )

        // -------------------  debug mode settings
        mDebugSetting = CantataDebugSettingBean(object :
            CantataDebugSettingsDialog.Callback {
            override fun onAudioDumpEnable(enable: Boolean) {
                if (enable) {
                    mRtcEngine?.setParameters("{\"rtc.debug.enable\": true}")
                    mRtcEngine?.setParameters("{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}")
                } else {
                    mRtcEngine?.setParameters("{\"rtc.debug.enable\": false}")
                }
            }

            override fun onScoringControl(level: Int, offset: Int) {
                mScoringAlgoControlLiveData.postValue(ScoringAlgoControlModel(level, offset))
            }

            override fun onSetParameters(parameters: String?) {
                mRtcEngine?.setParameters(parameters)
            }
        })

        mMusicSetting?.mAudioEffect?.let {
            setAudioEffectPreset(it)
        }
    }

    private fun setAudioEffectPreset(effect: Int) {
        mRtcEngine?.setAudioEffectPreset(effect)
    }

    // ======================= settings =======================
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
            mKtvApi.getMediaPlayer().selectMultiAudioTrack(1, 1)
            KTVPlayerTrackMode.Acc
        } else {
            mKtvApi.getMediaPlayer().selectMultiAudioTrack(0, 1)
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
    private fun resetMusicStatus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.resetMusicStatus() called")
        mChorusNum = 0
        mRetryTimes = 0
        mAudioTrackMode = KTVPlayerTrackMode.Acc
        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_IDLE)
        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)

        // 歌曲结束自动下麦
        mSeatLocalLiveData.value?.let { leaveSeat(it) }
        scoreMap.clear()
    }

    // ------------------ 歌曲开始播放 ------------------
    fun musicStartPlay(music: RoomSelSongModel) {
        mRoundRankListLiveData.postValue(false)
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
                    mainSingerUid,
                    KTVLoadMusicMode.LOAD_MUSIC_AND_LRC
                ), songCode, true
            )
        } else {
            if (mSeatLocalLiveData.value != null &&
                mSeatLocalLiveData.value!!.chorusSongCode == music.songNo + music.createAt
            ) {
                // 合唱者
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo,
                        mainSingerUid,
                        KTVLoadMusicMode.LOAD_LRC_ONLY
                    ), songCode, false
                )
                // 加入合唱
                innerJoinChorus(music.songNo, music.userNo.toInt())
            } else {
                // 观众
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo,
                        mainSingerUid,
                        KTVLoadMusicMode.LOAD_LRC_ONLY
                    ), songCode, false
                )
            }
        }

        // 标记歌曲为播放中
        mCantataServiceProtocol.makeSongDidPlay(music) { e: java.lang.Exception? ->
            e?.message?.let { // failure
                CustomToast.show(it)
            }
        }
    }

    private fun loadMusic(config: KTVLoadMusicConfiguration, songCode: Long, isOwnSong: Boolean) {
        mKtvApi.loadMusic(songCode, config, object : IMusicLoadStateListener {
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
                // 当前已被切歌
                if (mSongPlayingLiveData.value == null) {
                    CustomToast.show(R.string.cantata_load_failed_no_song, Toast.LENGTH_LONG)
                    return
                }

                if (isOwnSong) {
                    mKtvApi.switchSingerRole(KTVSingRole.LeadSinger, null)
                    mKtvApi.startSing(songCode, 0)
                }

                // 重置settings
                mRetryTimes = 0
                mMusicSetting?.mMicVolume = MusicSettingBean.DEFAULT_MIC_VOL
                mMusicSetting?.mAccVolume = MusicSettingBean.DEFAULT_ACC_VOL
                mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
            }

            override fun onMusicLoadFail(songCode: Long, reason: KTVLoadMusicFailReason) {
                // 当前已被切歌
                if (mSongPlayingLiveData.value == null) {
                    CustomToast.show(R.string.cantata_load_failed_no_song, Toast.LENGTH_LONG)
                    return
                }
                CantataLogger.e(TAG, "onMusicLoadFail， reason: $reason")
                if (reason == KTVLoadMusicFailReason.NO_LYRIC_URL) {
                    // 未获取到歌词 正常播放
                    mRetryTimes = 0
                    mMusicSetting?.mMicVolume = MusicSettingBean.DEFAULT_MIC_VOL
                    mMusicSetting?.mAccVolume = MusicSettingBean.DEFAULT_ACC_VOL
                    mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                    mNoLrcLiveData.postValue(true)
                } else if (reason == KTVLoadMusicFailReason.MUSIC_PRELOAD_FAIL) {
                    // 歌曲加载失败 ，重试3次
                    CustomToast.show(R.string.cantata_load_failed, Toast.LENGTH_LONG)
                    mRetryTimes += 1
                    if (mRetryTimes < 3) {
                        loadMusic(config, songCode, isOwnSong)
                    } else {
                        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                        CustomToast.show(R.string.cantata_try, Toast.LENGTH_LONG)
                    }
                } else if (reason == KTVLoadMusicFailReason.CANCELED) {
                    // 当前已被切歌
                    CustomToast.show(R.string.cantata_load_failed_another_song, Toast.LENGTH_LONG)
                }
            }
        })
    }

    // ------------------ 重新获取歌词url ------------------
    fun reGetLrcUrl() {
        mSongPlayingLiveData.value?.let {
            val isOwnSong = it.userNo == UserManager.getInstance().user.id.toString()
            loadMusic(
                KTVLoadMusicConfiguration(
                    it.songNo,
                    it.userNo?.toIntOrNull() ?: 0,
                    KTVLoadMusicMode.LOAD_LRC_ONLY
                ), it.songNo.toLong(), isOwnSong
            )
        }
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
    /**
     * 演唱者唱完一句更新麦位中 score 数据
     */
    fun updateSeatScoreStatus(score: Int, cumulativeScore: Int) {
        (mKtvApi as KTVGiantChorusApiImpl).setSingingScore(score)
        mCantataServiceProtocol.updateSeatScoreStatus(cumulativeScore) { e ->
            if (e == null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.updateSeatScoreStatus() success")
                // TODO:  
            } else {
                // failure
                CantataLogger.e(TAG, "RoomLivingViewModel.updateSeatScoreStatus() failed:${e.message}")
                e.message?.let {
                    CustomToast.show(it)
                }
            }
        }
    }

    fun getRankList(): List<RankItem> {
        val rankItemList: MutableList<RankItem> = mutableListOf()
        scoreMap.forEach { (_, model) ->
            val item = RankItem()
            item.userName = model.name
            item.score = model.score
            item.avatar = model.headUrl
            rankItemList.add(item)
        }
        return rankItemList.sortedByDescending { it.score }
    }
}
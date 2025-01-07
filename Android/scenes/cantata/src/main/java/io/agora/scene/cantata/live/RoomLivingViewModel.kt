package io.agora.scene.cantata.live

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
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
import io.agora.scene.base.utils.resourceManager.DownloadManager
import io.agora.scene.cantata.live.bean.MusicSettingBean
import io.agora.scene.cantata.live.fragmentdialog.MusicSettingCallback
import io.agora.scene.cantata.live.listener.SongLoadFailReason
import io.agora.scene.cantata.live.listener.SongLoadStateListener
import io.agora.scene.cantata.service.*
import io.agora.scene.cantata.service.api.KtvApiManager
import io.agora.scene.cantata.service.api.KtvSongApiModel
import io.agora.scene.cantata.widget.rankList.RankItem
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

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
 * Room details viewModel
 */
class RoomLivingViewModel constructor(joinRoomOutputModel: JoinRoomOutputModel) : ViewModel() {

    companion object {
        private const val TAG = "Cantata_Scene_Log"
    }

    private val mCantataServiceProtocol = CantataServiceProtocol.getImplInstance()

    private lateinit var mKtvApi: KTVApi

    private val ktvApiManager = KtvApiManager()

    // loading dialog
    private val _loadingDialogVisible = MutableLiveData(false)
    val mLoadingDialogVisible: LiveData<Boolean> = _loadingDialogVisible

    /**
     * Room information
     */
    val mRoomInfoLiveData: MutableLiveData<JoinRoomOutputModel> by lazy {
        MutableLiveData(joinRoomOutputModel)
    }
    val mRoomDeleteLiveData = MutableLiveData<Boolean>()
    val mRoomTimeUpLiveData = MutableLiveData<Boolean>()
    val mRoomNoSongsLiveData = MutableLiveData<Boolean>()

    val mRoomUserCountLiveData = MutableLiveData<Int>()

    /**
     * Seat information
     */
    var mIsOnSeat = false
    val mSeatListLiveData: MutableLiveData<List<RoomSeatModel>> = MutableLiveData<List<RoomSeatModel>>(emptyList())
    val scoreMap = mutableMapOf<String, UserModel>()
    val mSeatLocalLiveData: MutableLiveData<RoomSeatModel> = MutableLiveData<RoomSeatModel>()

    /**
     * Lyrics information
     */
    val mSongsOrderedLiveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()
    val mSongPlayingLiveData: MutableLiveData<RoomSelSongModel> = MutableLiveData<RoomSelSongModel>()

    /**
     * Player/RTC information
     */
    var mStreamId = 0

    val mPlayerMusicStatusLiveData = MutableLiveData<PlayerMusicStatus>()

    // Loading music progress
    val loadMusicProgressLiveData = MutableLiveData<Int>()

    val mJoinChorusStatusLiveData = MutableLiveData<JoinChorusStatus>()
    val mNoLrcLiveData = MutableLiveData<Boolean>()

    val mPlayerMusicOpenDurationLiveData = MutableLiveData<Long>()
    val mNetworkStatusLiveData: MutableLiveData<NetWorkEvent> = MutableLiveData<NetWorkEvent>()

    val mScoringAlgoControlLiveData: MutableLiveData<ScoringAlgoControlModel> =
        MutableLiveData<ScoringAlgoControlModel>()

    // Whether to display the settlement page
    val mRoundRankListLiveData = MutableLiveData<Boolean>()

    /**
     * Rtc engine
     */
    private var mRtcEngine: RtcEngineEx? = null

    /**
     * Main version audio settings
     */
    private val mMainChannelMediaOption = ChannelMediaOptions()

    /**
     * Player configuration
     */
    var mMusicSetting: MusicSettingBean? = null

    /**
     * Developer mode
     */
    var mDebugSetting: CantataDebugSettingBean? = null

    /**
     * Whether to enable background playback
     */
    private var mIsBackPlay = false

    /**
     * Whether to enable ear monitor
     */
    private var mIsOpnEar = false

    /**
     * Number of chorus participants
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

    // Release
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


    // ======================= Reconnection related =======================
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

    // ======================= Room related =======================
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
                // Triggered when the room status changes
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
     * Exit room
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

    // ======================= Seat related =======================
    private fun initSeats() {
        mCantataServiceProtocol.getSeatStatusList { e, list ->
            if (e == null && list != null) {
                mSeatListLiveData.value = list?: emptyList()
                for (roomSeatModel in list) {
                    if (roomSeatModel.userNo == UserManager.getInstance().user.id.toString()) {
                        mSeatLocalLiveData.postValue(roomSeatModel)
                        mIsOnSeat = true
                        if (mRtcEngine != null) {
                            updateVolumeStatus(roomSeatModel.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE)
                        }

                        mSongPlayingLiveData.value?.let {
                            innerJoinChorus(it)
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
                mSeatListLiveData.value = list?: emptyList()
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
     * Take the stage
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
     * Leave the stage
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
     * Mute
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

        // Mute local recording volume to 0 when muted
        mRtcEngine?.let {
            if (!isUnMute) it.adjustRecordingSignalVolume(0)
        }
        setMicVolume(micOldVolume)
    }


    // ======================= Song Related =======================
    private fun initSongs() {
        mCantataServiceProtocol.subscribeChooseSongChanged { ktvSubscribe: CantataServiceProtocol.KTVSubscribe?,
                                                             songModel: RoomSelSongModel? ->
            // When song information changes, refresh song list
            CantataLogger.d(TAG, "subscribeChooseSong updateSongs")
            onSongChanged()
            mSeatListLiveData.postValue(mSeatListLiveData.value)
        }

        // Get initial song list
        mCantataServiceProtocol.getChoosedSongsList { e: Exception?, data: List<RoomSelSongModel>? ->
            if (e == null && data != null) {
                // success
                CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() success")
                mSongsOrderedLiveData.postValue(data?: emptyList())
                if (data.isNotEmpty()) {
                    val value: RoomSelSongModel? = mSongPlayingLiveData.value
                    val songPlaying: RoomSelSongModel = data[0]
                    if (value == null && !songPlaying.musicEnded) {
                        // No songs selected, set first song in list as current playing song
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() chosen song list is empty")
                        mSongPlayingLiveData.value = songPlaying
                        if (mIsOnSeat && songPlaying.userNo?.equals(UserManager.getInstance().user.id.toString()) != true) {
                            innerJoinChorus(songPlaying)
                        }
                    } else if (songPlaying.musicEnded) {
                        // Music ended
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
                        // No songs selected, set first song in list as current playing song
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() chosen song list is empty")
                        mSongPlayingLiveData.postValue(songPlaying)
                    } else if (value.songNo != songPlaying.songNo) {
                        // Current song has been changed
                        CantataLogger.d(TAG, "RoomLivingViewModel.onSongChanged() single or first chorus")
                        resetMusicStatus()
                        mSongPlayingLiveData.postValue(songPlaying)
                    } else if (!value.musicEnded && songPlaying.musicEnded) {
                        // Music ended
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

    private val songList = mutableListOf<KtvSongApiModel>()

    private fun getRestfulSongList(completion: (error: Exception?) -> Unit) {
        if (songList.isNotEmpty()) {
            completion.invoke(null)
            return
        }
        ktvApiManager.getSongList { error, musicList ->
            CantataLogger.d(TAG, "RoomLivingViewModel.getSongList() return error:$error")
            if (error != null) {
                CustomToast.show(R.string.cantata_get_songs_failed, error.message ?: "")
                completion.invoke(error)
            } else {
                songList.apply {
                    clear()
                    addAll(musicList)
                }
                completion.invoke(null)
            }
        }
    }

    fun getSongList(): LiveData<List<RoomSelSongModel>> {
        // Get song list from RTC
        CantataLogger.d(TAG, "RoomLivingViewModel.getSongList() called")
        val liveData: MutableLiveData<List<RoomSelSongModel>> = MutableLiveData<List<RoomSelSongModel>>()
        getRestfulSongList{
            CantataLogger.d(TAG, "RoomLivingViewModel.getSongList() return error：$it")
            val songs: MutableList<RoomSelSongModel> = ArrayList()
            // Need to call another API to get current selected song list to supplement list information
            mCantataServiceProtocol.getChoosedSongsList { e: Exception?, songsChosen: List<RoomSelSongModel>? ->
                if (e == null && songsChosen != null) {
                    // success
                    for (music in songList) {
                        var songItem: RoomSelSongModel? = null
                        for (roomSelSongModel in songsChosen) {
                            if (roomSelSongModel.songNo == music.songCode.toString()) {
                                songItem = roomSelSongModel
                                break
                            }
                        }
                        if (songItem == null) {
                            songItem = RoomSelSongModel(
                                songName = music.name,
                                songNo = music.songCode,
                                singer = music.singer,
                                imageUrl = "",
                                userNo = "",
                                name = "",
                                isOriginal = 0,
                                status = 0,
                                createAt = 0L,
                                pinAt = 0.0,
                                musicEnded = false
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

    fun joinChorus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.joinChorus() called")
        val musicModel: RoomSelSongModel? = mSongPlayingLiveData.value
        if (musicModel == null) {
            CantataLogger.e(TAG, "RoomLivingViewModel.joinChorus() failed, no song playing now")
            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            return
        }

        getRestfulSongList {
            innerJoinChorus(musicModel)
        }
    }

    private fun innerJoinChorus(songInfo: RoomSelSongModel) {
        loadingMusic.set(true)
        val mainSingerUid = songInfo.userNo?.toIntOrNull() ?: return

        val config = KTVLoadMusicConfiguration(
            songInfo.songNo,
            mainSingerUid,
            KTVLoadMusicMode.LOAD_MUSIC_ONLY
        )

        innerLoadMusic(config, songInfo, object : SongLoadStateListener {
            override fun onMusicLoadSuccess(songCode: String, musicUri: String, lyricUrl: String) {
                loadingMusic.set(false)
                CantataLogger.d(TAG, "joinChorus onMusicLoadSuccess,songCode:$songCode,lyricUrl:$lyricUrl")
                mKtvApi.loadMusic(musicUri, config)
                mKtvApi.switchSingerRole(KTVSingRole.CoSinger, object : ISwitchRoleStateListener {
                    override fun onSwitchRoleFail(reason: SwitchRoleFailReason) {
                        CustomToast.show(R.string.cantata_join_chorus_failed, Toast.LENGTH_LONG)
                        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                    }

                    override fun onSwitchRoleSuccess() {

                        if (mIsOnSeat) {
                            // Joined chorus successfully
                            mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_CHORUS)
                            mAudioTrackMode = KTVPlayerTrackMode.Acc
                        } else {
                            // Not on stage, automatically take the stage
                            mCantataServiceProtocol.onSeat(OnSeatInputModel(0)) { err: Exception? ->
                                if (err == null) {
                                    mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_CHORUS)
                                    mAudioTrackMode = KTVPlayerTrackMode.Acc
                                    mIsOnSeat = true
                                } else {
                                    CustomToast.show(R.string.cantata_join_chorus_failed, Toast.LENGTH_LONG)
                                    mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
                                    mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
                                }
                            }
                        }
                    }
                })
            }

            override fun onMusicLoadFail(songCode: String, reason: SongLoadFailReason) {
                CustomToast.show(R.string.cantata_join_chorus_failed, Toast.LENGTH_LONG)
                mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_JOIN_FAILED)
            }

            override fun onMusicLoadProgress(
                songCode: String,
                percent: Int,
                status: MusicLoadStatus,
                lyricUrl: String?
            ) {
                loadMusicProgressLiveData.postValue(percent)
            }
        })
    }

    /**
     * Leave chorus
     */
    private var leaveSeatBySelf = false
    fun leaveChorus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called")
        if (mIsOnSeat) {
            // Leave the stage
            mSeatLocalLiveData.value?.let { leaveSeat(it) }
        }
        // Leave chorus
        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)
        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_LEAVE_CHORUS)
        mMusicSetting?.mEarBackEnable = false
    }

    /**
     * Start changing song
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


    private var mLrcControlView: WeakReference<ILrcView>? = null

    /**
     * Set lyrics view
     */
    fun setLrcView(view: ILrcView) {
        mLrcControlView = WeakReference(view)
        mKtvApi.setLrcView(view)
    }

    // ======================= Player/RTC/MPK Related =======================
    private fun initRTCPlayer() {
        // ------------------ Initialize music playback settings panel ------------------
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
        // ------------------ Initialize RTC ------------------
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId =
            if (SceneConfigManager.cantataAppId == "") BuildConfig.AGORA_APP_ID else SceneConfigManager.cantataAppId
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                // Network status callback, local user uid = 0
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
                mMusicSetting?.let { setting ->
                    // 0\2\5 earPhone
                    if (routing == 0 || routing == 2 || routing == 5 || routing == 6) {
                        setting.mHasEarPhone = true
                    } else {
                        if (mSongPlayingLiveData.value != null && setting.mEarBackEnable) {
                            CustomToast.show(R.string.cantata_earback_close_tip, Toast.LENGTH_SHORT)
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

        // ------------------ Initialize Scene API ------------------
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
                KTVMusicType.SONG_URL
            )
        )

        when (mRoomInfoLiveData.value!!.steamMode) {
            0 -> KTVApi.routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.RANDOM, 6)
            1 -> KTVApi.routeSelectionConfig =
                GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.BY_DELAY, 6)

            2 -> KTVApi.routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.TOP_N, 6)
            3 -> KTVApi.routeSelectionConfig =
                GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.BY_DELAY_AND_TOP_N, 6)
        }
        CantataLogger.d("hugohugo", "GiantChorusRouteSelectionConfig: ${KTVApi.routeSelectionConfig}")

        mKtvApi.addEventHandler(object : IKTVApiEventHandler() {
            override fun onMusicPlayerStateChanged(
                state: MediaPlayerState,
                error: MediaPlayerReason,
                isLocal: Boolean
            ) {
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

        // ------------------ Join Channel ------------------
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
                        // Network status callback, local user uid = 0
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
            setParametersEx(
                "{\"rtc.use_audio4\": true}",
                RtcConnection(mRoomInfoLiveData.value!!.roomNo + "_ad", UserManager.getInstance().user.id.toInt())
            )
            if (ret != Constants.ERR_OK) {
                CantataLogger.e(TAG, "joinRTC() called error: $ret")
            }
        }

        // ------------------ Start content inspection service ------------------
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
    // ------------------ Volume adjustment ------------------
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

    // ------------------ Original/Accompaniment ------------------
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

    // ------------------ Pause/Play ------------------
    fun musicToggleStart() {
        if (mPlayerMusicStatusLiveData.value == PlayerMusicStatus.ON_PLAYING) {
            mKtvApi.pauseSing()
        } else if (mPlayerMusicStatusLiveData.value == PlayerMusicStatus.ON_PAUSE) {
            mKtvApi.resumeSing()
        }
    }

    // ------------------ Reset song status (when switching songs) ------------------
    private fun resetMusicStatus() {
        CantataLogger.d(TAG, "RoomLivingViewModel.resetMusicStatus() called")
        mChorusNum = 0
        mRetryTimes = 0
        mAudioTrackMode = KTVPlayerTrackMode.Acc
        mJoinChorusStatusLiveData.postValue(JoinChorusStatus.ON_IDLE)
        mKtvApi.switchSingerRole(KTVSingRole.Audience, null)

        // Automatically leave stage when song ends
        mSeatLocalLiveData.value?.let { leaveSeat(it) }
        scoreMap.clear()
    }

    // ------------------ Start playing song ------------------
    fun musicStartPlay(music: RoomSelSongModel) {
        mRoundRankListLiveData.postValue(false)
        CantataLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called")
        if (music.userNo == null) return
        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE)
        val isOwnSong = music.userNo == UserManager.getInstance().user.id.toString()
        val songCode: Long = music.songNo.toLong()
        val mainSingerUid: Int = music.userNo.toInt()

        getRestfulSongList {
            if (isOwnSong) {
                // Lead singer loads song
                loadMusic(
                    KTVLoadMusicConfiguration(
                        music.songNo,
                        mainSingerUid,
                        KTVLoadMusicMode.LOAD_MUSIC_AND_LRC
                    ), music, true
                )
            } else {
                if (mSeatLocalLiveData.value != null &&
                    mSeatLocalLiveData.value!!.chorusSongCode == music.songNo + music.createAt
                ) {
                    // Chorus participant
                    loadMusic(
                        KTVLoadMusicConfiguration(
                            music.songNo,
                            mainSingerUid,
                            KTVLoadMusicMode.LOAD_LRC_ONLY
                        ), music, false
                    )
                    // Join chorus
                    innerJoinChorus(music)
                } else {
                    // Audience
                    loadMusic(
                        KTVLoadMusicConfiguration(
                            music.songNo,
                            mainSingerUid,
                            KTVLoadMusicMode.LOAD_LRC_ONLY
                        ), music, false
                    )
                }
            }
            // Mark song as playing
            mCantataServiceProtocol.makeSongDidPlay(music) { e: java.lang.Exception? ->
                e?.message?.let { // failure
                    CustomToast.show(it)
                }
            }
        }
    }

    private var loadingMusic: AtomicBoolean = AtomicBoolean(false)

    private fun loadMusic(config: KTVLoadMusicConfiguration, songInfo: RoomSelSongModel, isOwnSong: Boolean) {
        loadingMusic.set(true)
        innerLoadMusic(config, songInfo, object : SongLoadStateListener {
            override fun onMusicLoadSuccess(songCode: String, musicUri: String, lyricUrl: String) {
                loadingMusic.set(false)
                // Current song has been switched
                if (mSongPlayingLiveData.value == null) {
                    CustomToast.show(R.string.cantata_load_failed_no_song, Toast.LENGTH_LONG)
                    return
                }

                mKtvApi.loadMusic(musicUri,config)
                if (isOwnSong) {
                    mKtvApi.switchSingerRole(KTVSingRole.LeadSinger, null)
                    mKtvApi.startSing(musicUri, 0)
                }

                // Reset settings
                mRetryTimes = 0
                mMusicSetting?.mMicVolume = MusicSettingBean.DEFAULT_MIC_VOL
                mMusicSetting?.mAccVolume = MusicSettingBean.DEFAULT_ACC_VOL
                mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
            }

            override fun onMusicLoadFail(songCode: String, reason: SongLoadFailReason) {
                loadingMusic.set(false)

                // Current song has been switched
                if (mSongPlayingLiveData.value == null) {
                    CustomToast.show(R.string.cantata_load_failed_no_song, Toast.LENGTH_LONG)
                    return
                }
                CantataLogger.e(TAG, "onMusicLoadFail， reason: $reason")
                /*    if (reason == KTVLoadMusicFailReason.NO_LYRIC_URL) {
                        // No lyrics, play normally
                        mRetryTimes = 0
                        mMusicSetting?.mMicVolume = MusicSettingBean.DEFAULT_MIC_VOL
                        mMusicSetting?.mAccVolume = MusicSettingBean.DEFAULT_ACC_VOL
                        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                        mNoLrcLiveData.postValue(true)
                    } else*/
                if (reason == SongLoadFailReason.MUSIC_DOWNLOAD_FAIL) {
                    // Song loading failed, retry 3 times
                    CustomToast.show(R.string.cantata_load_failed, Toast.LENGTH_LONG)
                    mRetryTimes += 1
                    if (mRetryTimes < 3) {
                        loadMusic(config, songInfo, isOwnSong)
                    } else {
                        mPlayerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING)
                        CustomToast.show(R.string.cantata_try, Toast.LENGTH_LONG)
                    }
                } else if (reason == SongLoadFailReason.CANCELED) {
                    // Current song has been switched
                    CustomToast.show(R.string.cantata_load_failed_another_song, Toast.LENGTH_LONG)
                } else {
                    CustomToast.show(R.string.cantata_load_failed, Toast.LENGTH_LONG)
                }
            }

            override fun onMusicLoadProgress(
                songCode: String,
                percent: Int,
                status: MusicLoadStatus,
                lyricUrl: String?
            ) {
                loadMusicProgressLiveData.postValue(percent)
            }
        })
    }

    // ------------------ Get lyrics url again ------------------
    fun reGetLrcUrl() {
        mSongPlayingLiveData.value?.let { songPlaying ->
            val isOwnSong = songPlaying.userNo == UserManager.getInstance().user.id.toString()
            loadMusic(
                KTVLoadMusicConfiguration(
                    songPlaying.songNo,
                    songPlaying.userNo?.toIntOrNull() ?: 0,
                    KTVLoadMusicMode.LOAD_LRC_ONLY
                ), songPlaying, isOwnSong
            )
        }
    }

    // ------------------ Song seek ------------------
    fun musicSeek(time: Long) {
        mKtvApi.seekSing(time)
    }

    fun getSongDuration(): Long? {
        return mKtvApi.getMediaPlayer().getDuration()
    }

    // ------------------ Stop playing song ------------------
    fun musicStop() {
        CantataLogger.d(TAG, "RoomLivingViewModel.musicStop() called")
        // No songs in list, reset status
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

    // ------------------ Lyrics component related ------------------
    /**
     * Update score data in mic position after singer finishes a sentence
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

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private fun getMusicFolder(): String? {
        val folder = AgoraApplication.the().getExternalFilesDir("musics")
        return folder?.absolutePath
    }

    private fun innerLoadMusic(
        config: KTVLoadMusicConfiguration,
        songInfo: RoomSelSongModel,
        songLoadStateListener: SongLoadStateListener
    ) {
        if (config.mode == KTVLoadMusicMode.LOAD_NONE) {
            return
        }
        val song = songList.firstOrNull { it.songCode == songInfo.songNo } ?: return
        if (config.mode == KTVLoadMusicMode.LOAD_LRC_ONLY) {
            if (mSongPlayingLiveData.value?.songNo != songInfo.songNo) {
                // The current song has changed; the latest loaded song will prevail.
                songLoadStateListener.onMusicLoadFail(songInfo.songNo, SongLoadFailReason.CANCELED)
                return
            }

            mLrcControlView?.get()?.onDownloadLrcData(song.lyric)
            songLoadStateListener.onMusicLoadSuccess(songInfo.songNo, "", song.lyric)
            return
        }

        val path =
            getMusicFolder() ?: return songLoadStateListener.onMusicLoadFail(songInfo.songNo, SongLoadFailReason.UNKNOW)

        scope.launch(Dispatchers.IO) {
            DownloadManager.instance.download(
                url = song.music,
                destinationPath = path,
                callback = object : DownloadManager.FileDownloadCallback {
                    override fun onProgress(file: File, progress: Int) {
                        songLoadStateListener.onMusicLoadProgress(
                            songCode = songInfo.songNo,
                            percent = progress,
                            status = MusicLoadStatus.INPROGRESS,
                            lyricUrl = song.lyric
                        )
                    }

                    override fun onSuccess(file: File) {
                        // Current song has been switched
                        if (mSongPlayingLiveData.value?.songNo != songInfo.songNo) {
                            songLoadStateListener.onMusicLoadFail(songInfo.songNo, SongLoadFailReason.CANCELED)
                            return
                        }
                        val musicUri = path + File.separator + song.music.substringAfterLast("/")
                        if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_AND_LRC) {
                            mLrcControlView?.get()?.onDownloadLrcData(song.lyric)
                            songLoadStateListener.onMusicLoadProgress(
                                songCode = songInfo.songNo,
                                percent = 100,
                                status = MusicLoadStatus.INPROGRESS,
                                lyricUrl = song.lyric
                            )
                            songLoadStateListener.onMusicLoadSuccess(
                                songInfo.songNo, musicUri, song.lyric
                            )
                        } else if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_ONLY) {
                            songLoadStateListener.onMusicLoadProgress(
                                songCode = songInfo.songNo,
                                percent = 100,
                                status = MusicLoadStatus.INPROGRESS,
                                lyricUrl = song.lyric
                            )
                            songLoadStateListener.onMusicLoadSuccess(
                                songInfo.songNo,
                                musicUri,
                                song.lyric
                            )
                        }
                    }

                    override fun onFailed(exception: Exception) {
                        songLoadStateListener.onMusicLoadFail(songInfo.songNo, SongLoadFailReason.MUSIC_DOWNLOAD_FAIL)
                    }
                }
            )
        }
    }
}
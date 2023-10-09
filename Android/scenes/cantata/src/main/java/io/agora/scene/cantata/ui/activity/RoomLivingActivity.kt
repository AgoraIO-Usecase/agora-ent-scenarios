package io.agora.scene.cantata.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.agora.rtc2.Constants
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.event.NetWorkEvent
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.LiveDataUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.cantata.CantataLogger
import io.agora.scene.cantata.R
import io.agora.scene.cantata.api.ApiManager
import io.agora.scene.cantata.databinding.CantataActivityRoomLivingBinding
import io.agora.scene.cantata.service.*
import io.agora.scene.cantata.ui.dialog.CantataCommonDialog
import io.agora.scene.cantata.ui.dialog.ChorusSingerDialog
import io.agora.scene.cantata.ui.dialog.MusicSettingDialog
import io.agora.scene.cantata.ui.dialog.UserLeaveSeatMenuDialog
import io.agora.scene.cantata.ui.viewmodel.JoinChorusStatus
import io.agora.scene.cantata.ui.viewmodel.PlayerMusicStatus
import io.agora.scene.cantata.ui.viewmodel.RoomLivingViewModel
import io.agora.scene.cantata.ui.widget.LrcActionListenerImpl
import io.agora.scene.cantata.ui.widget.OnClickJackingListener
import io.agora.scene.cantata.ui.widget.lrcView.LrcControlView
import io.agora.scene.cantata.ui.widget.song.SongActionListenerImpl
import io.agora.scene.cantata.ui.widget.song.SongDialog
import io.agora.scene.widget.dialog.CommonDialog
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import java.util.concurrent.Executors

class RoomLivingActivity : BaseViewBindingActivity<CantataActivityRoomLivingBinding>() {

    companion object {
        private const val TAG = "RoomLivingActivity"
        private const val EXTRA_ROOM_INFO = "roomInfo"
        fun launch(context: Context, roomInfo: JoinRoomOutputModel) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            context.startActivity(intent)
        }
    }

    private val mRoomLivingViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                return RoomLivingViewModel(intent.getSerializableExtra(EXTRA_ROOM_INFO) as JoinRoomOutputModel) as T
            }
        })[RoomLivingViewModel::class.java]
    }

    private var mCreatorExitDialog: CantataCommonDialog? = null
    private var mExitDialog: CommonDialog? = null

    // 房间存活时间，单位ms
    private var mTimeUpExitDialog: CantataCommonDialog? = null
    private var musicSettingDialog: MusicSettingDialog? = null
    private var mChangeMusicDialog: CommonDialog? = null
    private var mUserLeaveSeatMenuDialog: UserLeaveSeatMenuDialog? = null
    private var mChorusSingerDialog: ChorusSingerDialog? = null

    // 点歌台
    private var mChooseSongDialog: SongDialog? = null
    private var showChooseSongDialogTag = false

    private var toggleAudioRun: Runnable? = null

    private val scheduledThreadPool = Executors.newScheduledThreadPool(5)

    override fun getPermissions() {
        toggleAudioRun?.let {
            it.run()
            toggleAudioRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) {
            launchAppSetting(permission)
        }
    }


    override fun getViewBinding(inflater: LayoutInflater): CantataActivityRoomLivingBinding {
        return CantataActivityRoomLivingBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (savedInstanceState != null) {
            finish()
            return
        }
        window.decorView.keepScreenOn = true
        setOnApplyWindowInsetsListener(binding.superLayout)
        mRoomLivingViewModel.setLrcView(binding.lrcControlView)

        binding.lrcControlView.mRole = LrcControlView.Role.Listener
        binding.lrcControlView.post {
            // TODO workaround 先强制申请权限， 避免首次安装无声
            if (mRoomLivingViewModel.isRoomOwner()) {
                toggleAudioRun = Runnable { mRoomLivingViewModel.initViewModel() }
                requestRecordPermission()
            } else {
                mRoomLivingViewModel.initViewModel()
            }
        }
        mRoomLivingViewModel.mRoomInfoLiveData.value?.apply {
            binding.tvRoomName.text = roomName
        }

        binding.ivChatroomMore.setOnClickListener(object : OnClickJackingListener {
            override fun onClickJacking(view: View) {
                TopFunctionDialog(this@RoomLivingActivity).show()
            }
        })

        if (mRoomLivingViewModel.isRoomOwner()) {
            scheduledThreadPool.execute {
                ApiManager.getInstance()
                    .fetchStartCloud(mRoomLivingViewModel.mRoomInfoLiveData.value!!.roomNo, 20232023)
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.ivChatroomBack.setOnClickListener(object : OnClickJackingListener {
            override fun onClickJacking(view: View) {
                mRoomLivingViewModel.exitRoom()
            }
        })
        binding.cbMic.setOnCheckedChangeListener { compoundButton, b ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            val seatLocal: RoomSeatModel =
                mRoomLivingViewModel.mSeatLocalLiveData.value ?: return@setOnCheckedChangeListener
            if (b) {
                toggleAudioRun = Runnable { mRoomLivingViewModel.toggleMic(true) }
                requestRecordPermission(true)
            } else {
                mRoomLivingViewModel.toggleMic(false)
            }
        }
        binding.iBtnChooseSong.setOnClickListener(object : OnClickJackingListener {
            override fun onClickJacking(view: View) {
                showChooseSongDialog()
            }
        })
        val lrcActionListenerImpl: LrcActionListenerImpl = object : LrcActionListenerImpl(
            this, mRoomLivingViewModel, binding.lrcControlView
        ) {
            override fun onMenuClick() {
                super.onMenuClick()
                showMusicSettingDialog()
            }

            override fun onChangeMusicClick() {
                super.onChangeMusicClick()
                showChangeMusicDialog()
            }

            override fun onChorusUserClick() {
                super.onChorusUserClick()
                showChorusSingerDialog()
            }
        }
        binding.lrcControlView.setOnLrcClickListener(lrcActionListenerImpl)

        //============ observe ============
        mRoomLivingViewModel.mLoadingDialogVisible.observe(this) { visible: Boolean ->
            if (visible) {
                showLoadingView()
            } else {
                hideLoadingView()
            }
        }
        mRoomLivingViewModel.mRoomInfoLiveData.observe(this) { joinRoomOutputModel ->
            // TODO:
        }
        mRoomLivingViewModel.mRoomDeleteLiveData.observe(this) { deletedByCreator ->
            if (deletedByCreator) {
                showCreatorExitDialog()
            } else {
                finish()
            }
        }
        mRoomLivingViewModel.mRoomUserCountLiveData.observe(this) { count ->
            binding.tvUserOnline.text = getString(R.string.cantata_room_count, count.toString())
        }
        mRoomLivingViewModel.mRoomTimeUpLiveData.observe(this) { isTimeUp ->
            if (mRoomLivingViewModel.release() && isTimeUp) {
                showTimeUpExitDialog()
            }
        }


        // 麦位相关
        mRoomLivingViewModel.mSeatLocalLiveData.observe(this) { seatModel: RoomSeatModel? ->
            val isOnSeat = seatModel != null && seatModel.seatIndex >= 0
            val isAudioChecked = seatModel != null && seatModel.isAudioMuted == RoomSeatModel.MUTED_VALUE_FALSE
            binding.cbMic.isEnabled = seatModel != null
            binding.cbMic.isChecked = isAudioChecked
            binding.lrcControlView.onSeat(seatModel != null)
            binding.lrcControlView.updateLocalCumulativeScore(seatModel)
        }
        mRoomLivingViewModel.mSeatListLiveData.observe(this) { seatModels: List<RoomSeatModel>? ->
            seatModels ?: return@observe
            CantataLogger.d(TAG, "mSeatListLiveData: $seatModels")
            // TODO 前8个默认占座
//            if (mRoomLivingViewModel.mSongsOrderedLiveData.value?.size != 0) {
//                val seat = seatModels.filter { it.rtcUid == mRoomLivingViewModel.mSongsOrderedLiveData.value?.get(0)?.userNo }.getOrNull(0) ?: return@observe
//                val seats = seatModels.filter { it.rtcUid != mRoomLivingViewModel.mSongsOrderedLiveData.value?.get(0)?.userNo }
//                binding.lrcControlView.updateMicSeatModels(seat, seats)
//            }
            if (seatModels.size == 9) {
                binding.lrcControlView.onSeat(true)
            } else if (seatModels.size < 9) {
                binding.lrcControlView.onSeat(false)
            }
        }

        // 歌词相关
        mRoomLivingViewModel.mSongsOrderedLiveData.observe(this) { models: List<RoomSelSongModel>? ->
            if (models.isNullOrEmpty()) {
                // songs empty
                binding.lrcControlView.role = LrcControlView.Role.Listener
                binding.lrcControlView.onIdleStatus()
            }
            mChooseSongDialog?.resetChosenSongList(SongActionListenerImpl.transSongModel(models))
        }
        mRoomLivingViewModel.mSongPlayingLiveData.observe(this) { model: RoomSelSongModel? ->
            if (model == null) {
                mRoomLivingViewModel.musicStop()
                return@observe
            }
            onMusicChanged(model)
        }
        mRoomLivingViewModel.mScoringAlgoControlLiveData.observe(this) { model: ScoringAlgoControlModel? ->
            model ?: return@observe
            binding.lrcControlView.karaokeView?.scoringLevel = model.level
            binding.lrcControlView.karaokeView?.scoringCompensationOffset = model.offset
        }
        mRoomLivingViewModel.mNoLrcLiveData.observe(this) { isNoLrc: Boolean ->
            if (isNoLrc) {
                binding.lrcControlView.onNoLrc()
            }
        }
        mRoomLivingViewModel.mPlayerMusicStatusLiveData.observe(this) { status: PlayerMusicStatus ->
            if (status == PlayerMusicStatus.ON_PREPARE) {
                binding.lrcControlView.onPrepareStatus(mRoomLivingViewModel.isRoomOwner())
            } else if (status == PlayerMusicStatus.ON_PLAYING) {
                binding.lrcControlView.onPlayStatus(mRoomLivingViewModel.mSongPlayingLiveData.value)
            } else if (status == PlayerMusicStatus.ON_PAUSE) {
                binding.lrcControlView.onPauseStatus()
            } else if (status == PlayerMusicStatus.ON_LRC_RESET) {
                binding.lrcControlView.lyricsView.reset()
                if (binding.lrcControlView.role == LrcControlView.Role.Singer) {
                    mRoomLivingViewModel.changeMusic()
                }
            } else if (status == PlayerMusicStatus.ON_CHANGING_START) {
                binding.lrcControlView.isEnabled = false
            } else if (status == PlayerMusicStatus.ON_CHANGING_END) {
                binding.lrcControlView.isEnabled = true
            }
        }
        mRoomLivingViewModel.mJoinChorusStatusLiveData.observe(this) { status: JoinChorusStatus ->
            when (status) {
                JoinChorusStatus.ON_JOIN_CHORUS -> {
                    binding.cbMic.isChecked = true
                    binding.lrcControlView.onSelfJoinedChorus()
                }

                JoinChorusStatus.ON_JOIN_FAILED -> {
                    binding.lrcControlView.onSelfJoinedChorusFailed()
                }

                JoinChorusStatus.ON_LEAVE_CHORUS -> {
                    binding.cbMic.isChecked = false
                    binding.lrcControlView.onSelfLeavedChorus()
                }

                else -> {}
            }
        }
        mRoomLivingViewModel.mPlayerMusicOpenDurationLiveData.observe(this) { duration: Long ->
            binding.lrcControlView.lyricsView.setDuration(duration)
        }
        mRoomLivingViewModel.mNetworkStatusLiveData.observe(this) { netWorkStatus: NetWorkEvent ->
            setNetWorkStatus(netWorkStatus.txQuality, netWorkStatus.rxQuality)
        }
        mRoomLivingViewModel.mRoundRankListLiveData.observe(this) { showRank: Boolean ->
            binding.rankListView.isVisible = showRank
            if (showRank) {
                binding.rankListView.resetRankList(mRoomLivingViewModel.getRankList() ?: emptyList())
            }
        }
    }

    private fun setNetWorkStatus(txQuality: Int, rxQuality: Int) {
        if (txQuality == Constants.QUALITY_BAD || txQuality == Constants.QUALITY_POOR || rxQuality == Constants.QUALITY_BAD || rxQuality == Constants.QUALITY_POOR) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_yellow)
            binding.tvNetStatus.setText(R.string.cantata_net_status_m)
        } else if (txQuality == Constants.QUALITY_VBAD || txQuality == Constants.QUALITY_DOWN || rxQuality == Constants.QUALITY_VBAD || rxQuality == Constants.QUALITY_DOWN) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_red)
            binding.tvNetStatus.setText(R.string.cantata_net_status_low)
        } else if (txQuality == Constants.QUALITY_EXCELLENT || txQuality == Constants.QUALITY_GOOD || rxQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_GOOD) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_green)
            binding.tvNetStatus.setText(R.string.cantata_net_status_good)
        } else if (txQuality == Constants.QUALITY_UNKNOWN || rxQuality == Constants.QUALITY_UNKNOWN) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_red)
            binding.tvNetStatus.setText(R.string.cantata_net_status_un_know)
        } else {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_green)
            binding.tvNetStatus.setText(R.string.cantata_net_status_good)
        }
    }

    private fun onMusicChanged(music: RoomSelSongModel) {
        mRoomLivingViewModel.resetMusicStatus()
        binding.lrcControlView.setMusic(music)
        if (UserManager.getInstance().user.id.toString() == music.userNo) {
            binding.lrcControlView.role = LrcControlView.Role.Singer
        } else {
            binding.lrcControlView.role = LrcControlView.Role.Listener
        }
        mRoomLivingViewModel.musicStartPlay(music)
    }

    private fun filterSongTypeMap(typeMap: LinkedHashMap<Int, String>): LinkedHashMap<Int, String> {
        // 0 -> "项目热歌榜单"
        // 1 -> "声网热歌榜"
        // 2 -> "新歌榜" ("热门新歌")
        // 3 -> "嗨唱推荐"
        // 4 -> "抖音热歌"
        // 5 -> "古风热歌"
        // 6 -> "KTV必唱"
        val ret = LinkedHashMap<Int, String>()
        for (entry in typeMap.entries) {
            val key = entry.key
            var value = entry.value
            if (key == 2) {
                value = getString(R.string.cantata_song_rank_7)
                ret[key] = value
            } else if (key == 3 || key == 4 || key == 6) {
                ret[key] = value
            }
        }
        return ret
    }

    private fun onMemberLeave(member: RoomSeatModel) {
        if (member.userNo == UserManager.getInstance().user.id.toString()) {
            binding.groupBottomView.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        mRoomLivingViewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        mRoomLivingViewModel.onStop()
    }

    override fun isBlackDarkStatus(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        CantataLogger.d(TAG, "onResume() $isBlackDarkStatus")
        setDarkStatusIcon(isBlackDarkStatus)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mRoomLivingViewModel.isRoomOwner()) {
            scheduledThreadPool.execute {
                ApiManager.getInstance().fetchStopCloud()
            }
        }
        mRoomLivingViewModel.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    //======================== dialog 相关 =============================
    private fun showCreatorExitDialog() {
        if (mCreatorExitDialog == null) {
            mCreatorExitDialog = CantataCommonDialog(this).apply {
                setDescText(getString(R.string.room_has_close))
                setDialogBtnText("", getString(R.string.cantata_iknow))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {}
                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        finish()
                    }
                }
            }
        }
        mCreatorExitDialog?.show()
    }

    private fun showExitDialog() {
        if (mExitDialog == null) {
            mExitDialog = CommonDialog(this).apply {
                if (mRoomLivingViewModel.isRoomOwner()) {
                    setDialogTitle(getString(R.string.dismiss_room))
                    setDescText(getString(R.string.confirm_to_dismiss_room))
                } else {
                    setDialogTitle(getString(R.string.exit_room))
                    setDescText(getString(R.string.confirm_to_exit_room))
                }
                setDialogBtnText(getString(R.string.cantata_cancel), getString(R.string.cantata_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                    }

                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        mRoomLivingViewModel.exitRoom()
                        finish()
                    }
                }
            }
        }
        mExitDialog?.show()
    }

    private fun showTimeUpExitDialog() {
        if (mTimeUpExitDialog == null) {
            mTimeUpExitDialog = CantataCommonDialog(this).apply {
                if (mRoomLivingViewModel.isRoomOwner()) {
                    setDescText(getString(R.string.time_up_exit_room))
                } else {
                    setDescText(getString(R.string.expire_exit_room))
                }
                setDialogBtnText("", getString(R.string.cantata_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {}
                    override fun onRightButtonClick() {
                        mRoomLivingViewModel.exitRoom()
                    }
                }
            }

        }
        mTimeUpExitDialog?.show()
    }

    private fun showMusicSettingDialog() {
        mRoomLivingViewModel.mMusicSetting?.let { musicSetting ->
            musicSettingDialog = MusicSettingDialog(
                musicSetting,
                mRoomLivingViewModel.mPlayerMusicStatusLiveData.value == PlayerMusicStatus.ON_PAUSE
            )
            musicSettingDialog?.show(supportFragmentManager, MusicSettingDialog.TAG)
        }
    }

    // 切歌
    private fun showChangeMusicDialog() {
        if (mChangeMusicDialog == null) {
            mChangeMusicDialog = CommonDialog(this).apply {
                setDialogTitle(getString(R.string.cantata_room_change_music_title))
                setDescText(getString(R.string.cantata_room_change_music_msg))
                setDialogBtnText(getString(R.string.cantata_cancel), getString(R.string.cantata_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                    }

                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        mRoomLivingViewModel.changeMusic()
                    }
                }
            }
        }
        mChangeMusicDialog?.show()
    }

    // 合唱
    private fun showChorusSingerDialog() {
        val isRoomOwner = mRoomLivingViewModel.isRoomOwner()
        val seatList = mRoomLivingViewModel.mSeatListLiveData.value ?: emptyList()
        val songModel = mRoomLivingViewModel.mSongPlayingLiveData.value
        mChorusSingerDialog = ChorusSingerDialog(isRoomOwner, songModel, seatList)
        mChorusSingerDialog?.show(supportFragmentManager, ChorusSingerDialog.TAG)
        mChorusSingerDialog?.onKickingCallback = {
            ToastUtils.showToast("on kicking ${it.name}")
        }
        mChorusSingerDialog?.updateAllData()
    }

    /**
     * 下麦提示
     */
    private fun showUserLeaveSeatMenuDialog(seatModel: RoomSeatModel) {
        if (mUserLeaveSeatMenuDialog == null) {
            mUserLeaveSeatMenuDialog = UserLeaveSeatMenuDialog(this).apply {
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        mRoomLivingViewModel.leaveChorus()
                    }

                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        mRoomLivingViewModel.leaveSeat(seatModel)
                    }
                }
                setAgoraMember(seatModel.name, seatModel.headUrl)
            }
        }

        mUserLeaveSeatMenuDialog?.show()
    }

    private fun showChooseSongDialog() {
        if (showChooseSongDialogTag) return
        showChooseSongDialogTag = true
        if (mChooseSongDialog == null) {
            mChooseSongDialog = SongDialog()
            mChooseSongDialog?.setChosenControllable(mRoomLivingViewModel.isRoomOwner())
            showLoadingView()
            LiveDataUtils.observerThenRemove<LinkedHashMap<Int, String>>(
                this, mRoomLivingViewModel.getSongTypes()
            ) { typeMap: LinkedHashMap<Int, String> ->
                val chooseSongListener: SongActionListenerImpl = SongActionListenerImpl(
                    this,
                    mRoomLivingViewModel,
                    filterSongTypeMap(typeMap),
                    false
                )
                mChooseSongDialog?.setChooseSongTabsTitle(
                    chooseSongListener.getSongTypeTitles(this),
                    chooseSongListener.songTypeList,
                    0
                )
                mChooseSongDialog?.setChooseSongListener(chooseSongListener)
                hideLoadingView()
                if (mChooseSongDialog?.isAdded == false) {
                    mRoomLivingViewModel.getSongChosenList()
                    mChooseSongDialog?.show(supportFragmentManager, "ChooseSongDialog")
                }
                binding.root.post { showChooseSongDialogTag = false }
            }
            return
        }
        if (mChooseSongDialog?.isAdded == false) {
            mRoomLivingViewModel.getSongChosenList()
            mChooseSongDialog?.show(supportFragmentManager, "ChooseSongDialog")
        }
        binding.root.post { showChooseSongDialogTag = false }
    }
}
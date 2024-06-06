package io.agora.scene.ktv.live

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import io.agora.rtc2.Constants
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.base.utils.LiveDataUtils
import io.agora.scene.ktv.KTVLogger.d
import io.agora.scene.ktv.KtvCenter
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvActivityRoomLivingBinding
import io.agora.scene.ktv.databinding.KtvItemRoomSpeakerBinding
import io.agora.scene.ktv.debugSettings.KTVDebugSettingsDialog
import io.agora.scene.ktv.live.bean.JoinChorusStatus
import io.agora.scene.ktv.live.bean.LineScore
import io.agora.scene.ktv.live.bean.NetWorkEvent
import io.agora.scene.ktv.live.bean.PlayerMusicStatus
import io.agora.scene.ktv.live.bean.ScoringAlgoControlModel
import io.agora.scene.ktv.live.bean.ScoringAverageModel
import io.agora.scene.ktv.live.bean.VolumeModel
import io.agora.scene.ktv.live.fragmentdialog.MusicSettingDialog
import io.agora.scene.ktv.live.fragmentdialog.UserLeaveSeatMenuDialog
import io.agora.scene.ktv.live.listener.LrcActionListenerImpl
import io.agora.scene.ktv.live.listener.SongActionListenerImpl
import io.agora.scene.ktv.service.RoomMicSeatInfo
import io.agora.scene.ktv.service.ChosenSongInfo
import io.agora.scene.ktv.service.KTVParameters
import io.agora.scene.ktv.service.fullHeadUrl
import io.agora.scene.ktv.widget.KtvCommonDialog
import io.agora.scene.ktv.widget.lrcView.LrcControlView
import io.agora.scene.ktv.widget.song.SongDialog
import io.agora.scene.widget.DividerDecoration
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.dialog.CommonDialog
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.UiUtils

/**
 * 房间主页
 */
class RoomLivingActivity : BaseViewBindingActivity<KtvActivityRoomLivingBinding>() {

    companion object {
        private const val EXTRA_ROOM_INFO = "roomInfo"

        /**
         * Launch
         *
         * @param context
         * @param roomInfo
         */
        fun launch(context: Context, roomInfo: AUIRoomInfo) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            context.startActivity(intent)
        }

        private fun fillWithRenderView(container: ViewGroup): SurfaceView {
            val context = container.context
            val cardView = MaterialCardView(context, null, R.attr.materialCardViewStyle)
            cardView.cardElevation = 0f
            cardView.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
                cardView.radius = (right - left) / 2f
            }
            val surfaceView = SurfaceView(context)
            surfaceView.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            cardView.addView(surfaceView)
            container.addView(cardView)
            return surfaceView
        }
    }

    private var musicSettingDialog: MusicSettingDialog? = null
    private var mRoomSpeakerAdapter: SpeakerAdapter? = null
    private var creatorExitDialog: KtvCommonDialog? = null
    private var exitDialog: CommonDialog? = null
    private var mUserLeaveSeatMenuDialog: UserLeaveSeatMenuDialog? = null
    private var mChooseSongDialog: SongDialog? = null

    private val roomLivingViewModel: RoomLivingViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val roomInfo = (intent.getSerializableExtra(EXTRA_ROOM_INFO) as AUIRoomInfo?)!!
                return RoomLivingViewModel(roomInfo) as T
            }
        })[RoomLivingViewModel::class.java]
    }

    // 房间存活时间，单位ms
    private var timeUpExitDialog: KtvCommonDialog? = null
    override fun getViewBinding(inflater: LayoutInflater): KtvActivityRoomLivingBinding {
        return KtvActivityRoomLivingBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            finish()
            return
        }
        window.decorView.keepScreenOn = true
        setOnApplyWindowInsetsListener(binding.superLayout)
        mRoomSpeakerAdapter = SpeakerAdapter(roomLivingViewModel.mRoomInfo)
        binding.rvUserMember.addItemDecoration(DividerDecoration(4, 24, 8))
        binding.rvUserMember.adapter = mRoomSpeakerAdapter
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvUserMember.setOverScrollMode(View.OVER_SCROLL_NEVER)
        }
        binding.lrcControlView.setRole(LrcControlView.Role.Listener)
        binding.lrcControlView.post {
            // TODO workaround 先强制申请权限， 避免首次安装无声
            if (roomLivingViewModel.isRoomOwner) {
                toggleAudioRun = Runnable {
                    roomLivingViewModel.initData()
                    roomLivingViewModel.setLrcView(binding.lrcControlView)
                }
                requestRecordPermission()
            } else {
                roomLivingViewModel.initData()
                roomLivingViewModel.setLrcView(binding.lrcControlView)
            }
        }
        val roomModel = roomLivingViewModel.mRoomInfo
        binding.tvRoomName.text = roomModel.roomName
        val userCount = roomModel.customPayload[KTVParameters.ROOM_USER_COUNT] as? Int
        val showCount = (userCount ?: 0) + KtvCenter.userAddMore
        binding.tvRoomMCount.text = getString(R.string.ktv_room_count, showCount)
        GlideApp.with(binding.getRoot())
            .load(roomModel.roomOwner?.fullHeadUrl)
            .error(R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivOwnerAvatar)
        binding.btnDebug.isVisible = AgoraApplication.the().isDebugModeOpen
        binding.btnDebug.setOnClickListener { v: View? ->
            val dialog = KTVDebugSettingsDialog(
                roomLivingViewModel.mDebugSetting,
                roomModel.roomId,
                roomLivingViewModel.sDKBuildNum
            )
            dialog.show(supportFragmentManager, "debugSettings")
        }
        binding.ivMore.setOnClickListener { v: View? -> TopFunctionDialog(this).show() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val params = binding.rvUserMember.layoutParams as ConstraintLayout.LayoutParams
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val heightPixels: Int // current window
        val widthPixels: Int // current window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val rect = windowManager.currentWindowMetrics.bounds
            heightPixels = rect.height()
            widthPixels = rect.width()
        } else {
            val point = Point()
            windowManager.defaultDisplay.getSize(point)
            heightPixels = point.y
            widthPixels = point.x
        }
        if (heightPixels * 1.0 / widthPixels > 16.0 / 9) { // 2K/Slim/> 16:9 screens
            // TODO(HAI_GUO) Flip/Fold/Split screens and One-handed mode may not supported well
            params.bottomMargin = (heightPixels * (1.0 - 16.0 * widthPixels / (9 * heightPixels))).toInt()
            binding.rvUserMember.setLayoutParams(params)
        }
        // density 4.0 densityDpi 640 system resources 2560 1440 display real 2560 1440 current window 2560 1440 HUAWEI V9
        // density 3.0 densityDpi 480 system resources 2297 1080 display real 2400 1080 current window 2400 1080 1+ 9R
    }

    override fun initListener() {
        binding.ivExit.setOnClickListener { view: View? -> showExitDialog() }
        binding.superLayout.setOnClickListener { view: View? -> setDarkStatusIcon(isBlackDarkStatus) }
        binding.cbMic.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            val seatLocal = roomLivingViewModel.localSeatInfo
            if (seatLocal != null) {
                if (b) {
                    toggleAudioRun = Runnable { roomLivingViewModel.updateSeatAudioMuteStatus(false) }
                    requestRecordPermission(true)
                } else {
                    roomLivingViewModel.updateSeatAudioMuteStatus(true)
                }
            }
        }
        binding.iBtnChooseSong.setOnClickListener { v: View? -> showChooseSongDialog() }
        binding.btnMenu.setOnClickListener { v: View? -> showMusicSettingDialog() }
        binding.btnOK.setOnClickListener { view: View? -> binding.groupResult.visibility = View.GONE }
        val lrcActionListenerImpl: LrcActionListenerImpl =
            object : LrcActionListenerImpl(this, roomLivingViewModel, binding.lrcControlView) {
                override fun onChangeMusicClick() {
                    super.onChangeMusicClick()
                    showChangeMusicDialog()
                }
            }
        binding.lrcControlView.setOnLrcClickListener(lrcActionListenerImpl)
        binding.cbVideo.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            toggleSelfVideo(b)
        }

        initObserver()
    }

    private fun initObserver() {
        roomLivingViewModel.roomDestroyLiveData.observe(this) { roomDestroy ->
            if (roomDestroy) {
                showCreatorExitDialog()
            }
        }
        roomLivingViewModel.roomExpireLiveData.observe(this) { roomExpire ->
            if (roomExpire) {
                showTimeUpExitDialog()
            }
        }
        roomLivingViewModel.userCountLiveData.observe(this) { userCount ->
            val showCount = (userCount ?: 0) + KtvCenter.userAddMore
            binding.tvRoomMCount.text = getString(R.string.ktv_room_count, showCount)
        }
        roomLivingViewModel.seatListLiveData.observe(this) { seaInfoList ->
            mRoomSpeakerAdapter?.resetAll(seaInfoList)
            val localSeat = seaInfoList.firstOrNull { it.owner?.userId == KtvCenter.mUser.id.toString() }
            if (localSeat != null) {
                binding.groupBottomView.isVisible = true
                binding.groupEmptyPrompt.isVisible = false
                binding.cbMic.setChecked(!localSeat.isAudioMuted)
                binding.cbVideo.setChecked(!localSeat.isVideoMuted)
                binding.lrcControlView.onSeat(true)
            } else {
                binding.groupBottomView.isVisible = false
                binding.groupEmptyPrompt.isVisible = true
                binding.cbMic.setChecked(false)
                binding.cbVideo.setChecked(false)
                binding.lrcControlView.onSeat(false)
            }
        }
        roomLivingViewModel.seatUpdateLiveData.observe(this) { seatInfo ->
            if (seatInfo.seatIndex < 0 || seatInfo.seatIndex >= mRoomSpeakerAdapter?.itemCount ?: 8) {
                return@observe
            }
            if (seatInfo.owner?.userId == KtvCenter.mUser.id.toString()) { // 上麦
                binding.groupBottomView.isVisible = true
                binding.groupEmptyPrompt.isVisible = false
                binding.cbMic.setChecked(!seatInfo.isAudioMuted)
                binding.cbVideo.setChecked(!seatInfo.isVideoMuted)
                binding.lrcControlView.onSeat(true)
            }
            if (seatInfo.owner?.userId.isNullOrEmpty()) { //有人离开麦位
                val oldLocalSeat = mRoomSpeakerAdapter?.oldLocalCurrentSeat
                if (oldLocalSeat != null && seatInfo.seatIndex == oldLocalSeat.seatIndex) { // 自己离开麦位
                    binding.groupBottomView.isVisible = false
                    binding.groupEmptyPrompt.isVisible = true
                    binding.cbMic.setChecked(false)
                    binding.cbVideo.setChecked(false)
                    binding.lrcControlView.onSeat(false)
                }
            }
            mRoomSpeakerAdapter?.replace(seatInfo.seatIndex, seatInfo)
        }
        roomLivingViewModel.chosenSongListLiveData.observe(this) { chosenSongList ->
            if (chosenSongList.isNullOrEmpty()) { // songs empty
                binding.lrcControlView.setRole(LrcControlView.Role.Listener)
                binding.lrcControlView.onIdleStatus()
                mRoomSpeakerAdapter?.notifyDataSetChanged()
            }
            mChooseSongDialog?.resetChosenSongList(SongActionListenerImpl.transSongModel(chosenSongList))

        }
        roomLivingViewModel.volumeLiveData.observe(this) { value: VolumeModel ->
            var volumeModel = value
            if (volumeModel.uid == 0) {
                volumeModel = VolumeModel(KtvCenter.mUser.id.toInt(), volumeModel.volume)
            }
            mRoomSpeakerAdapter?.let { roomSpeakerAdapter ->
                for (i in 0 until roomSpeakerAdapter.itemCount) {
                    val seatInfo = roomSpeakerAdapter.getItem(i)
                    if (seatInfo != null && seatInfo.owner?.userId?.toIntOrNull() == volumeModel.uid) {
                        val holder =
                            binding.rvUserMember.findViewHolderForAdapterPosition(i) as BindingViewHolder<KtvItemRoomSpeakerBinding>?
                                ?: return@observe
                        if (volumeModel.volume == 0 || seatInfo.isAudioMuted) {
                            holder.binding.vMicWave.endWave()
                        } else {
                            holder.binding.vMicWave.startWave()
                        }
                    }
                }
            }
        }

        // 歌词相关
        roomLivingViewModel.mainSingerScoreLiveData.observe(this) { score: LineScore ->
            binding.lrcControlView.onReceiveSingleLineScore(
                score.score,
                score.index,
                score.cumulativeScore,
                score.total
            )
        }
        roomLivingViewModel.songPlayingLiveData.observe(this) { model: ChosenSongInfo? ->
            if (model == null) {
                roomLivingViewModel.musicStop()
                return@observe
            }
            onMusicChanged(model)
        }
        roomLivingViewModel.scoringAlgoControlLiveData.observe(this) { model: ScoringAlgoControlModel? ->
            model ?: return@observe
            binding.lrcControlView.karaokeView.scoringLevel = model.level
            binding.lrcControlView.karaokeView.scoringCompensationOffset = model.offset
        }
        roomLivingViewModel.noLrcLiveData.observe(this) { isNoLrc: Boolean ->
            if (isNoLrc) {
                binding.lrcControlView.onNoLrc()
            }
        }
        roomLivingViewModel.playerMusicStatusLiveData.observe(this) { status: PlayerMusicStatus ->
            if (status == PlayerMusicStatus.ON_PREPARE) {
                binding.lrcControlView.onPrepareStatus(roomLivingViewModel.isRoomOwner)
            } else if (status == PlayerMusicStatus.ON_PLAYING) {
                binding.lrcControlView.onPlayStatus(roomLivingViewModel.songPlayingLiveData.getValue())
            } else if (status == PlayerMusicStatus.ON_PAUSE) {
                binding.lrcControlView.onPauseStatus()
            } else if (status == PlayerMusicStatus.ON_LRC_RESET) {
                binding.lrcControlView.lyricsView.reset()
                if (binding.lrcControlView.role == LrcControlView.Role.Singer) {
                    roomLivingViewModel.changeMusic()
                }
            } else if (status == PlayerMusicStatus.ON_CHANGING_START) {
                binding.lrcControlView.setEnabled(false)
            } else if (status == PlayerMusicStatus.ON_CHANGING_END) {
                binding.lrcControlView.setEnabled(true)
            }
        }
        roomLivingViewModel.joinchorusStatusLiveData.observe(this) { status: JoinChorusStatus ->
            when (status) {
                JoinChorusStatus.ON_JOIN_CHORUS -> {
                    binding.lrcControlView.onSelfJoinedChorus()
                }

                JoinChorusStatus.ON_JOIN_FAILED -> {
                    binding.lrcControlView.onSelfJoinedChorusFailed()
                    val yOfChorusBtn = binding.lrcControlView.getYOfChorusBtn()
                    CustomToast.showByPosition(R.string.ktv_join_chorus_failed, Gravity.TOP, yOfChorusBtn)
                }

                JoinChorusStatus.ON_LEAVE_CHORUS -> {
                    binding.lrcControlView.onSelfLeavedChorus()
                }

                JoinChorusStatus.ON_IDLE -> {
                    //nothing
                }
            }
        }
        roomLivingViewModel.playerMusicOpenDurationLiveData.observe(this) { duration: Long ->
            binding.lrcControlView.lyricsView.setDuration(duration)
        }
        roomLivingViewModel.playerMusicPlayCompleteLiveData.observe(this) { (isLocal, score1): ScoringAverageModel ->
            if (isLocal) {
                val sc = binding.lrcControlView.cumulativeScoreInPercentage
                binding.tvResultScore.text = sc.toString()
                if (sc >= 90) {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_s)
                } else if (sc >= 80) {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_a)
                } else if (sc >= 70) {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_b)
                } else {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_c)
                }
                binding.groupResult.visibility = View.VISIBLE
                if (binding.lrcControlView.role == LrcControlView.Role.Singer) {
                    roomLivingViewModel.syncSingingAverageScore(sc.toDouble())
                }
            } else {
                if (binding.lrcControlView.role != LrcControlView.Role.Listener) return@observe
                binding.tvResultScore.text = score1.toString()
                if (score1 >= 90) {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_s)
                } else if (score1 >= 80) {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_a)
                } else if (score1 >= 70) {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_b)
                } else {
                    binding.ivResultLevel.setImageResource(R.mipmap.ic_c)
                }
                binding.groupResult.visibility = View.VISIBLE
            }
        }
        roomLivingViewModel.networkStatusLiveData.observe(this) { netWorkStatus: NetWorkEvent ->
            setNetWorkStatus(netWorkStatus.txQuality, netWorkStatus.rxQuality)
        }
        roomLivingViewModel.loadMusicProgressLiveData.observe(this) { percent: Int ->
            binding.lrcControlView.onMusicLoadProgress(percent)
        }
        roomLivingViewModel.scoringAlgoLiveData.observe(this) { difficulty: Int ->
            binding.lrcControlView.karaokeView.scoringLevel = difficulty
        }
    }

    private fun setNetWorkStatus(txQuality: Int, rxQuality: Int) {
        if (txQuality == Constants.QUALITY_BAD || txQuality == Constants.QUALITY_POOR || rxQuality == Constants.QUALITY_BAD || rxQuality == Constants.QUALITY_POOR) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_yellow)
            binding.tvNetStatus.setText(R.string.ktv_net_status_m)
        } else if (txQuality == Constants.QUALITY_VBAD || txQuality == Constants.QUALITY_DOWN || rxQuality == Constants.QUALITY_VBAD || rxQuality == Constants.QUALITY_DOWN) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_red)
            binding.tvNetStatus.setText(R.string.ktv_net_status_low)
        } else if (txQuality == Constants.QUALITY_EXCELLENT || txQuality == Constants.QUALITY_GOOD || rxQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_GOOD) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_green)
            binding.tvNetStatus.setText(R.string.ktv_net_status_good)
        } else if (txQuality == Constants.QUALITY_UNKNOWN || rxQuality == Constants.QUALITY_UNKNOWN) {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_red)
            binding.tvNetStatus.setText(R.string.ktv_net_status_un_know)
        } else {
            binding.ivNetStatus.setImageResource(R.drawable.bg_round_green)
            binding.tvNetStatus.setText(R.string.ktv_net_status_good)
        }
    }

    override fun onResume() {
        super.onResume()
        d("ktv", "onResume() $isBlackDarkStatus")
        setDarkStatusIcon(isBlackDarkStatus)
    }

    /**
     * 下麦提示
     */
    private fun showUserLeaveSeatMenuDialog(setInfo: RoomMicSeatInfo, kickSeat: Boolean) {
        if (mUserLeaveSeatMenuDialog == null) {
            mUserLeaveSeatMenuDialog = UserLeaveSeatMenuDialog(this)
        }
        mUserLeaveSeatMenuDialog?.onButtonClickListener = object : OnButtonClickListener {
            override fun onLeftButtonClick() {
                setDarkStatusIcon(isBlackDarkStatus)
                roomLivingViewModel.leaveChorus()
            }

            override fun onRightButtonClick() {
                setDarkStatusIcon(isBlackDarkStatus)
                if (kickSeat) {
                    roomLivingViewModel.kickSeat(setInfo)
                } else {
                    roomLivingViewModel.leaveSeat(setInfo)
                }
            }
        }
        mUserLeaveSeatMenuDialog?.setAgoraMember(setInfo.owner?.userName ?: "", setInfo.owner?.fullHeadUrl ?: "")
        mUserLeaveSeatMenuDialog?.show()
    }

    private fun showTimeUpExitDialog() {
        if (timeUpExitDialog == null) {
            timeUpExitDialog = KtvCommonDialog(this).apply {
                if (roomLivingViewModel.isRoomOwner) {
                    setDescText(getString(R.string.time_up_exit_room))
                } else {
                    setDescText(getString(R.string.expire_exit_room))
                }
                setDialogBtnText("", getString(R.string.ktv_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {}
                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        finish()
                    }
                }
            }
        }
        timeUpExitDialog?.show()
    }

    private fun showExitDialog() {
        if (exitDialog == null) {
            exitDialog = CommonDialog(this).apply {
                if (roomLivingViewModel.isRoomOwner) {
                    setDialogTitle(getString(R.string.dismiss_room))
                    setDescText(getString(R.string.confirm_to_dismiss_room))
                } else {
                    setDialogTitle(getString(R.string.exit_room))
                    setDescText(getString(R.string.confirm_to_exit_room))
                }
                setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                    }

                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        roomLivingViewModel.exitRoom()
                        finish()
                    }
                }
            }
        }
        exitDialog?.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onMusicChanged(music: ChosenSongInfo) {
        hideMusicSettingDialog()
        binding.lrcControlView.setMusic(music)
        if (KtvCenter.mUser.id.toString() == music.owner?.userId) {
            binding.lrcControlView.setRole(LrcControlView.Role.Singer)
        } else {
            binding.lrcControlView.setRole(LrcControlView.Role.Listener)
        }
        roomLivingViewModel.resetMusicStatus()
        roomLivingViewModel.musicStartPlay(music)
        mRoomSpeakerAdapter?.notifyDataSetChanged()
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
                value = getString(R.string.ktv_song_rank_7)
                ret[key] = value
            } else if (key == 3 || key == 4 || key == 6) {
                ret[key] = value
            }
        }
        return ret
    }

    private var showChooseSongDialogTag = false
    private fun showChooseSongDialog() {
        if (showChooseSongDialogTag) {
            return
        }
        showChooseSongDialogTag = true
        if (mChooseSongDialog == null) {
            mChooseSongDialog = SongDialog()
            mChooseSongDialog?.setChosenControllable(roomLivingViewModel.isRoomOwner)
            showLoadingView()
            LiveDataUtils.observerThenRemove(this, roomLivingViewModel.getSongTypes()) { typeMap ->
                val chooseSongListener =
                    SongActionListenerImpl(this, roomLivingViewModel, filterSongTypeMap(typeMap), false)
                mChooseSongDialog?.setChooseSongTabsTitle(
                    chooseSongListener.getSongTypeTitles(this),
                    chooseSongListener.getSongTypeList(),
                    0
                )
                mChooseSongDialog?.setChooseSongListener(chooseSongListener)
                hideLoadingView()
                if (mChooseSongDialog?.isAdded == false) {
                    roomLivingViewModel.getSongChosenList()
                    mChooseSongDialog?.show(supportFragmentManager, "ChooseSongDialog")
                }
                binding.getRoot().post { showChooseSongDialogTag = false }
            }
            return
        }
        if (mChooseSongDialog?.isAdded == false) {
            roomLivingViewModel.getSongChosenList()
            mChooseSongDialog?.show(supportFragmentManager, "ChooseSongDialog")
        }
        binding.getRoot().post { showChooseSongDialogTag = false }
    }

    private fun showMusicSettingDialog() {
        if (musicSettingDialog == null) {
            musicSettingDialog = MusicSettingDialog(
                roomLivingViewModel.mMusicSetting!!,
                roomLivingViewModel.mSoundCardSettingBean!!,
                binding.lrcControlView.role == LrcControlView.Role.Listener,
                roomLivingViewModel.songPlayingLiveData.getValue()
            )
        }
        if (musicSettingDialog?.isAdded == false) {
            musicSettingDialog?.show(supportFragmentManager, MusicSettingDialog.TAG)
        }
    }

    private fun hideMusicSettingDialog() {
        musicSettingDialog?.dismiss()
        musicSettingDialog = null
    }

    /**
     * Close music settings dialog.
     */
    fun closeMusicSettingsDialog() {
        setDarkStatusIcon(isBlackDarkStatus)
        hideMusicSettingDialog()
    }

    private var changeMusicDialog: CommonDialog? = null
    private fun showChangeMusicDialog() {
        if (UiUtils.isFastClick(2000)) {
            CustomToast.show(R.string.ktv_too_fast, Toast.LENGTH_SHORT)
            return
        }
        if (changeMusicDialog == null) {
            changeMusicDialog = CommonDialog(this).apply {
                setDialogTitle(getString(R.string.ktv_room_change_music_title))
                setDescText(getString(R.string.ktv_room_change_music_msg))
                setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                    }

                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        roomLivingViewModel.changeMusic()
                    }
                }
            }
        }
        changeMusicDialog?.show()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun isBlackDarkStatus(): Boolean {
        return false
    }

    private var toggleVideoRun: Runnable? = null
    private var toggleAudioRun: Runnable? = null

    //开启 关闭摄像头
    private fun toggleSelfVideo(isOpen: Boolean) {
        if (isOpen) {
            toggleVideoRun = Runnable { roomLivingViewModel.updateSeatVideoMuteStatus(false) }
            requestCameraPermission(true)
        } else {
            roomLivingViewModel.updateSeatVideoMuteStatus(true)
        }
    }

    override fun getPermissions() {
        toggleVideoRun?.let {
            it.run()
            toggleVideoRun = null
        }
        toggleAudioRun?.let {
            it.run()
            toggleAudioRun = null
        }
    }

    override fun onPermissionDined(permission: String) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }
        ) { launchAppSetting(permission) }
    }


    private fun showCreatorExitDialog() {
        if (creatorExitDialog == null) {
            creatorExitDialog = KtvCommonDialog(this).apply {
                setDescText(getString(R.string.room_has_close))
                setDialogBtnText("", getString(R.string.ktv_iknow))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {}
                    override fun onRightButtonClick() {
                        setDarkStatusIcon(isBlackDarkStatus)
                        finish()
                    }
                }
            }
        }
        creatorExitDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private inner class SpeakerAdapter constructor(val roomInfo: AUIRoomInfo) : BindingSingleAdapter<RoomMicSeatInfo,
            KtvItemRoomSpeakerBinding>() {

        val oldLocalCurrentSeat: RoomMicSeatInfo? get() = mDataList.firstOrNull { it.owner?.userId == KtvCenter.mUser.id.toString() }

        override fun onBindViewHolder(holder: BindingViewHolder<KtvItemRoomSpeakerBinding>, position: Int) {
            val seatInfo = getItem(position) ?: return
            val isIdleSeat = seatInfo.owner?.userId.isNullOrEmpty()
            setSeatView(holder.binding, seatInfo)
            holder.binding.root.setOnClickListener { v: View? ->
                if (!isIdleSeat) { // 下麦
                    if (roomLivingViewModel.isRoomOwner) { // 房主踢他人下麦
                        if (seatInfo.owner?.userId != KtvCenter.mUser.id.toString()) {
                            showUserLeaveSeatMenuDialog(seatInfo, kickSeat = true)
                        }
                    } else if (seatInfo.owner?.userId == KtvCenter.mUser.id.toString()) { // 自己下麦
                        showUserLeaveSeatMenuDialog(seatInfo, kickSeat = false)
                    }
                } else { // 上麦
                    if (roomLivingViewModel.localSeatInfo == null) {
                        toggleAudioRun = Runnable {
                            roomLivingViewModel.enterSeat(position)
                            binding.cbMic.setChecked(false)
                            binding.cbVideo.setChecked(false)
                        }
                        requestRecordPermission()
                    }
                }
            }
        }

        private fun setSeatView(binding: KtvItemRoomSpeakerBinding, seatInfo: RoomMicSeatInfo) {
            if (seatInfo.seatIndex < 0 || seatInfo.seatIndex >= itemCount) return

            val isOutSeat = TextUtils.isEmpty(seatInfo.owner?.userId)
            if (isOutSeat) {
                binding.vMicWave.endWave()
                binding.vMicWave.visibility = View.INVISIBLE
                binding.avatarItemRoomSpeaker.setImageResource(R.mipmap.ktv_ic_seat)
                binding.avatarItemRoomSpeaker.setVisibility(View.VISIBLE)
                binding.tvZC.visibility = View.GONE
                binding.tvHC.visibility = View.GONE
                binding.tvRoomOwner.visibility = View.GONE
                binding.ivMute.setVisibility(View.GONE)
                binding.tvUserName.text = getString(R.string.ktv_seat_num, (seatInfo.seatIndex + 1).toString())
                binding.flVideoContainer.removeAllViews()
            } else {
                binding.vMicWave.visibility = View.VISIBLE
                binding.tvUserName.text = seatInfo.owner?.userName
                binding.tvRoomOwner.isVisible =
                    seatInfo.owner?.userId == roomInfo.roomOwner?.userId && seatInfo.seatIndex == 0
                // microphone
                if (seatInfo.isAudioMuted) {
                    binding.vMicWave.endWave()
                    binding.ivMute.setVisibility(View.VISIBLE)
                } else {
                    binding.ivMute.setVisibility(View.GONE)
                }
                // video
                if (seatInfo.isVideoMuted) {
                    binding.avatarItemRoomSpeaker.setVisibility(View.VISIBLE)
                    binding.flVideoContainer.removeAllViews()
                    GlideApp.with(binding.getRoot())
                        .load(seatInfo.owner?.fullHeadUrl)
                        .error(R.mipmap.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.avatarItemRoomSpeaker)
                } else {
                    binding.avatarItemRoomSpeaker.setVisibility(View.INVISIBLE)
                    binding.flVideoContainer.removeAllViews()
                    val surfaceView = fillWithRenderView(binding.flVideoContainer)
                    if (seatInfo.owner?.userId == KtvCenter.mUser.id.toString()) { // 是本人
                        roomLivingViewModel.renderLocalCameraVideo(surfaceView)
                    } else {
                        val uid = seatInfo.owner?.userId?.toIntOrNull() ?: -1
                        roomLivingViewModel.renderRemoteCameraVideo(surfaceView, uid)
                    }
                }
                val songModel = roomLivingViewModel.songPlayingLiveData.getValue()
                val choristerInfo =
                    roomLivingViewModel.getSongChorusInfo(seatInfo.owner?.userId ?: "null", songModel?.songNo ?: "null")
                if (songModel != null) {
                    if (seatInfo.owner?.userId == songModel.owner?.userId) {
                        binding.tvZC.setText(R.string.ktv_zc)
                        binding.tvHC.visibility = View.GONE
                        binding.tvZC.visibility = View.VISIBLE
                    } else if (!choristerInfo?.userId.isNullOrEmpty() && choristerInfo?.chorusSongNo == songModel.songNo) {
                        binding.tvHC.setText(R.string.ktv_hc)
                        binding.tvZC.visibility = View.GONE
                        binding.tvHC.visibility = View.VISIBLE
                    } else {
                        binding.tvZC.visibility = View.GONE
                        binding.tvHC.visibility = View.GONE
                    }
                } else {
                    binding.tvZC.visibility = View.GONE
                    binding.tvHC.visibility = View.GONE
                }
            }
        }
    }
}
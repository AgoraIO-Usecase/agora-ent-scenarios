package io.agora.scene.cantata.widget.lrcView

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import io.agora.karaoke_view.v11.KaraokeEvent
import io.agora.karaoke_view.v11.KaraokeView
import io.agora.karaoke_view.v11.LyricsView
import io.agora.karaoke_view.v11.constants.DownloadError
import io.agora.karaoke_view.v11.downloader.LyricsFileDownloader
import io.agora.karaoke_view.v11.downloader.LyricsFileDownloaderCallback
import io.agora.karaoke_view.v11.model.LyricsLineModel
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.cantata.BuildConfig
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataLayoutLrcControlViewBinding
import io.agora.scene.cantata.databinding.CantataLayoutLrcPrepareBinding
import io.agora.ktvapi.ILrcView
import io.agora.scene.cantata.service.RoomSeatModel
import io.agora.scene.cantata.service.RoomSelSongModel
import io.agora.scene.cantata.widget.OnClickJackingListener
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.UiUtils
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * 歌词控制View
 */
class LrcControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), ILrcView, OnClickJackingListener {

    private companion object {
        private const val TAG = "LrcControlView"
    }

    private val mScoreFormat: NumberFormat by lazy {
        DecimalFormat("#,###")
    }

    private val mBinding: CantataLayoutLrcControlViewBinding by lazy {
        CantataLayoutLrcControlViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private val mPrepareBinding: CantataLayoutLrcPrepareBinding by lazy {
        CantataLayoutLrcPrepareBinding.bind(mBinding.root)
    }
    var karaokeView: KaraokeView? = null
    val lyricsView: LyricsView
        get() = mBinding.ilActive.lyricsView

    override fun onHighPartTime(highStartTime: Long, highEndTime: Long) {}
    enum class Role {
        Singer, Listener, CoSinger
    }

    var mRole = Role.Listener
    private var mOnKaraokeActionListener: OnKaraokeEventListener? = null

    init {
        mBinding.ilIdle.root.visibility = VISIBLE
        mBinding.ilActive.root.visibility = GONE
        mPrepareBinding.statusPrepareViewLrc.visibility = GONE
        karaokeView = KaraokeView(mBinding.ilActive.lyricsView, null)

        initListener()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private fun initListener() {
        mBinding.ilActive.switchOriginal.setOnClickListener(this)
        mBinding.ilActive.ivMusicMenu.setOnClickListener(this)
        mBinding.ilActive.ivMusicStart.setOnClickListener(this)
        mBinding.ilActive.ivChangeSong.setOnClickListener(this)
        mBinding.ilActive.ivSkipPostludeSkip.setOnClickListener(this)
        mBinding.ilActive.ivSkipPreludeSkip.setOnClickListener(this)
        mBinding.ilActive.ivSkipPostludeCancel.setOnClickListener(this)
        mBinding.ilActive.ivSkipPreludeCancel.setOnClickListener(this)
        mBinding.ilActive.ivJoinChorusBtn.setOnClickListener(this)
        mBinding.ilActive.ivLeaveChorus.setOnClickListener(this)
        mBinding.ilActive.downloadLrcFailedBtn.setOnClickListener(this)
        mBinding.tvCoNumber.setOnClickListener(this)
        karaokeView?.setKaraokeEvent(object : KaraokeEvent {
            override fun onDragTo(view: KaraokeView, position: Long) {
                mOnKaraokeActionListener?.onDragTo(position)
            }

            override fun onRefPitchUpdate(refPitch: Float, numberOfRefPitches: Int) {
                mOnKaraokeActionListener?.onRefPitchUpdate(refPitch, numberOfRefPitches)
            }

            override fun onLineFinished(
                view: KaraokeView,
                line: LyricsLineModel,
                score: Int,
                cumulativeScore: Int,
                index: Int,
                total: Int
            ) {
                Log.d(TAG, "onLineFinished $score $cumulativeScore $index $total")
                if (mRole == Role.Singer || mRole == Role.CoSinger) {
                    mOnKaraokeActionListener?.onLineFinished(line, score, cumulativeScore, index, total)
                }
            }
        })
    }

    fun setOnLrcClickListener(karaokeActionListener: OnKaraokeEventListener?) {
        mOnKaraokeActionListener = karaokeActionListener
    }

    fun onSelfJoinedChorus() {
        mBinding.tvCumulativeScore.text = resources.getString(R.string.cantata_score_formatter, "0")
        mRole = Role.CoSinger
        mBinding.ilActive.ivMusicStart.visibility = INVISIBLE
        mBinding.ilActive.switchOriginal.visibility = VISIBLE
        mBinding.ilActive.switchOriginal.isChecked = false // reset ui icon for mAudioTrackMode
        mBinding.ilActive.switchOriginal.setIconResource(io.agora.scene.widget.R.mipmap.ic_play_original_off)
        mBinding.ilActive.ivMusicMenu.visibility = VISIBLE
        mBinding.ilActive.ivJoinChorusBtn.visibility = INVISIBLE
        mBinding.ilActive.ivLeaveChorus.visibility = VISIBLE
        mBinding.ilActive.ivJoinChorusLoading.visibility = INVISIBLE
        mPrepareBinding.statusPrepareViewLrc.visibility = GONE
        if (isMineOwner) {
            mBinding.ilActive.ivChangeSong.visibility = VISIBLE
        }
    }

    fun onSelfJoinedChorusFailed() {
        mBinding.ilActive.ivJoinChorusBtn.visibility = VISIBLE
        mBinding.ilActive.ivJoinChorusLoading.visibility = INVISIBLE
        mPrepareBinding.statusPrepareViewLrc.visibility = GONE
    }

    fun onSelfLeavedChorus() {
        mBinding.tvCumulativeScore.text = String.format(resources.getString(R.string.cantata_score_formatter), "0")
        mRole = Role.Listener
        mBinding.ilActive.ivMusicStart.visibility = GONE
        mBinding.ilActive.switchOriginal.visibility = INVISIBLE
        mBinding.ilActive.ivMusicMenu.visibility = INVISIBLE
        mBinding.ilActive.ivJoinChorusBtn.visibility = VISIBLE
        mBinding.ilActive.ivLeaveChorus.visibility = INVISIBLE
    }

    private var isMineOwner = false
    fun onPrepareStatus(isMineOwner: Boolean) {
        this.isMineOwner = isMineOwner
        mBinding.ilIdle.root.visibility = GONE
        mBinding.ilActive.root.visibility = GONE
        mBinding.clActive.visibility = VISIBLE
        mBinding.clActive.setBackgroundResource(backgroundResId)
        mPrepareBinding.tvContent.text = String.format(resources.getString(R.string.cantata_loading_music), "0%")
        mPrepareBinding.pbLoadingMusic.progress = 0
        mPrepareBinding.statusPrepareViewLrc.visibility = VISIBLE
        changeViewByRole()
    }

    fun onMusicLoadProgress(percent: Int) {
        mPrepareBinding.tvContent.text = String.format(resources.getString(R.string.cantata_loading_music), "$percent%")
        mPrepareBinding.pbLoadingMusic.progress = percent
    }

    private var songPlaying: RoomSelSongModel? = null
    fun onPlayStatus(songPlaying: RoomSelSongModel?) {
        this.songPlaying = songPlaying
        mBinding.ilIdle.root.visibility = GONE
        mBinding.clActive.visibility = VISIBLE
        mBinding.clActive.setBackgroundResource(backgroundResId)
        mPrepareBinding.statusPrepareViewLrc.visibility = GONE
        mBinding.ilActive.root.visibility = VISIBLE
        mBinding.ilActive.ivMusicStart.setIconResource(R.drawable.cantata_ic_pause)
        mBinding.ilActive.ivMusicStart.setText(R.string.cantata_room_player_pause)
    }

    private fun changeViewByRole() {
        mBinding.ilActive.downloadLrcFailedView.visibility = INVISIBLE
        mBinding.ilActive.downloadLrcFailedBtn.visibility = INVISIBLE
        if (mRole == Role.Singer) {
            mBinding.ilActive.lyricsView.enableDragging(BuildConfig.DEBUG)
            mBinding.ilActive.ivMusicStart.visibility = VISIBLE
            mBinding.ilActive.switchOriginal.visibility = VISIBLE
            mBinding.ilActive.ivMusicMenu.visibility = VISIBLE
            mBinding.ilActive.rlMusicControlMenu.visibility = VISIBLE
            mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
            mBinding.ilActive.switchOriginal.isChecked = false // reset ui icon for mAudioTrackMode
            mBinding.ilActive.switchOriginal.setIconResource(io.agora.scene.widget.R.mipmap.ic_play_original_off)
            mBinding.ilActive.ivJoinChorusBtn.visibility = INVISIBLE
            mBinding.ilActive.ivLeaveChorus.visibility = INVISIBLE
        } else if (mRole == Role.Listener) {
            mBinding.ilActive.lyricsView.enableDragging(false)
            mBinding.ilActive.rlMusicControlMenu.visibility = GONE
            mBinding.ilActive.ivJoinChorusBtn.visibility = VISIBLE
            mBinding.ilActive.ivLeaveChorus.visibility = INVISIBLE
        }
        if (isMineOwner) {
            mBinding.ilActive.rlMusicControlMenu.visibility = VISIBLE
            mBinding.ilActive.ivMusicStart.visibility = VISIBLE
            mBinding.ilActive.switchOriginal.visibility = VISIBLE
            mBinding.ilActive.ivMusicMenu.visibility = VISIBLE
            mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
            if (mRole == Role.Listener) {
                mBinding.ilActive.ivMusicStart.visibility = GONE
                mBinding.ilActive.switchOriginal.visibility = INVISIBLE
                mBinding.ilActive.ivMusicMenu.visibility = INVISIBLE
                mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
                mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
            }
        }
    }

    fun onPauseStatus() {
        mBinding.ilIdle.root.visibility = GONE
        mBinding.clActive.visibility = VISIBLE
        mBinding.clActive.setBackgroundResource(backgroundResId)
        mPrepareBinding.statusPrepareViewLrc.visibility = GONE
        mBinding.ilActive.root.visibility = VISIBLE
        mBinding.ilActive.ivMusicStart.setIconResource(R.drawable.cantata_ic_play)
        mBinding.ilActive.ivMusicStart.setText(R.string.cantata_room_player_play)
    }

    fun onIdleStatus() {
        mBinding.ilIdle.root.visibility = VISIBLE
        mBinding.clActive.visibility = GONE
        mBinding.clActive.setBackgroundResource(backgroundResId)
        mPrepareBinding.statusPrepareViewLrc.visibility = GONE
        mBinding.ilActive.root.visibility = GONE
    }

    fun onLrcResetStatus() {
        lyricsView.reset()
        mBinding.ilActive.root.visibility = GONE
    }

    var role: Role
        get() = mRole
        set(mRole) {
            this.mRole = mRole
            lrcUrl = null
            changeViewByRole()
        }

    fun setMusic(mMusic: RoomSelSongModel) {
        karaokeView?.reset()
        mBinding.tvMusicName.text = mMusic.songName + "-" + mMusic.singer
        mBinding.tvCumulativeScore.text = resources.getString(R.string.cantata_score_formatter, "0")
    }

    // 更新总分
    fun updateLocalCumulativeScore(seatModel: RoomSeatModel?) {
        val formattedScore = mScoreFormat.format(seatModel?.score ?: 0)
        mBinding.tvCumulativeScore.text = resources.getString(R.string.cantata_score_formatter, formattedScore)
    }


    private var backgroundResId = R.drawable.cantata_mv_bg
    fun setLrcViewBackground(@DrawableRes resId: Int) {
        backgroundResId = resId
        val mBitmap = BitmapFactory.decodeResource(resources, resId)
        Palette.from(mBitmap).generate { palette: Palette? ->
            if (palette == null) return@generate

            var defaultColor = ContextCompat.getColor(context, io.agora.scene.widget.R.color.pink_b4)
            mBinding.ilActive.lyricsView.setCurrentLineHighlightedTextColor(defaultColor)
            defaultColor = ContextCompat.getColor(context, io.agora.scene.widget.R.color.white)
            mBinding.ilActive.lyricsView.setCurrentLineTextColor(defaultColor)
        }
        mBinding.clActive.setBackgroundResource(resId)
    }

    override fun onClickJacking(v: View) {
        if (v == mBinding.ilActive.switchOriginal) {
            mOnKaraokeActionListener?.onSwitchOriginalClick()
            val withOriginal = mBinding.ilActive.switchOriginal.isChecked
            mBinding.ilActive.switchOriginal.setIconResource(if (withOriginal) io.agora.scene.widget.R.mipmap.ic_play_original_on else io.agora.scene.widget.R.mipmap.ic_play_original_off)
        } else if (v == mBinding.ilActive.ivMusicMenu) {
            mOnKaraokeActionListener?.onMenuClick()
        } else if (v == mBinding.ilActive.ivMusicStart) {
            mOnKaraokeActionListener?.onPlayClick()
        } else if (v == mBinding.ilActive.ivChangeSong) {
            mOnKaraokeActionListener?.onChangeMusicClick()
        } else if (v == mBinding.ilActive.ivSkipPreludeSkip) {
            mOnKaraokeActionListener?.onSkipPreludeClick()
            mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
        } else if (v == mBinding.ilActive.ivSkipPostludeSkip) {
            mOnKaraokeActionListener?.onSkipPostludeClick()
        } else if (v == mBinding.ilActive.ivSkipPreludeCancel) {
            mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
        } else if (v == mBinding.ilActive.ivSkipPostludeCancel) {
            mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
        } else if (v == mBinding.ilActive.ivJoinChorusBtn) {
            if (UiUtils.isFastClick(2000)) {
                CustomToast.show(R.string.cantata_too_fast)
                return
            }
            mOnKaraokeActionListener?.onJoinChorus()
            mBinding.ilActive.ivJoinChorusLoading.visibility = VISIBLE
            mPrepareBinding.tvContent.text = String.format(resources.getString(R.string.cantata_loading_music), "0%")
            mPrepareBinding.pbLoadingMusic.progress = 0
            mPrepareBinding.statusPrepareViewLrc.visibility = VISIBLE
            if (isMineOwner) {
                mBinding.ilActive.ivChangeSong.visibility = INVISIBLE
            }
        } else if (v == mBinding.ilActive.ivLeaveChorus) {
            mOnKaraokeActionListener?.onLeaveChorus()
        } else if (v == mBinding.ilActive.downloadLrcFailedBtn) {
            mBinding.ilActive.downloadLrcFailedView.visibility = INVISIBLE
            mBinding.ilActive.downloadLrcFailedBtn.visibility = INVISIBLE
            if (lrcUrl == null) {
                mOnKaraokeActionListener?.onReGetLrcUrl()
            } else {
                downloadAndSetLrcData()
            }
        } else if (v == mBinding.tvCoNumber) {
            mOnKaraokeActionListener?.onChorusUserClick()
        }
    }

    fun setSwitchOriginalChecked(checked: Boolean) {
        mBinding.ilActive.switchOriginal.isChecked = checked
    }

    // ------------------ ILrcView ------------------
    override fun onUpdatePitch(pitch: Float?) {
        pitch?.let {
            karaokeView?.setPitch(it)
        }
    }

    override fun onUpdateProgress(progress: Long?) {
        progress?.let {
            karaokeView?.apply {
                if (lyricsData == null) return
                if (mRole == Role.Singer) {
                    if (it >= lyricsData.startOfVerse - 2000) {
                        mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
                    }
                    if (it >= lyricsData.duration) {
                        mBinding.ilActive.ivSkipPostlude.visibility = VISIBLE
                    } else {
                        mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
                    }
                }
                setProgress(it)
            }
        }
    }

    private var lrcUrl: String? = null


    override fun onDownloadLrcData(url: String?) {
        lrcUrl = url
        downloadAndSetLrcData()
    }

    private fun downloadAndSetLrcData() {
        val context: Context = AgoraApplication.the()
        LyricsFileDownloader.getInstance(context)
            .setLyricsFileDownloaderCallback(object : LyricsFileDownloaderCallback {
                override fun onLyricsFileDownloadProgress(requestId: Int, progress: Float) {}

                override fun onLyricsFileDownloadCompleted(requestId: Int, fileData: ByteArray, error: DownloadError?) {
                    if (error == null) {
                        val lyricsModel = KaraokeView.parseLyricsData(fileData)
                        if (lyricsModel == null) {
                            CustomToast.show("Unexpected parseLyricsData")
                            mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                            mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
                            return
                        }
                        karaokeView?.let {
                            mBinding.ilActive.downloadLrcFailedView.visibility = INVISIBLE
                            karaokeView?.lyricsData = lyricsModel
                        }
                    } else {
                        mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                        mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
                        error.message?.let {
                            CustomToast.show(it)
                        }
                    }
                }
            })
        LyricsFileDownloader.getInstance(context).download(lrcUrl)
    }

    fun onNoLrc() {
        lrcUrl = null
        mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
        mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
    }


    fun updateMicSeatModels(leadSingerModel: RoomSeatModel, list: List<RoomSeatModel>) {
        mBinding.ilActive.chorusMicView.updateAllMics(leadSingerModel, list)
        mBinding.tvCoNumber.text = resources.getString(R.string.cantata_on_chorus_user, (list.size + 1))
    }

    fun updateAllSeatScore(list: List<RoomSeatModel>) {
        if (mRole == Role.Listener) { // 观众计算总分
            var totalScore = 0
            list.forEach { roomSeat ->
                if (roomSeat.score >= 0) {
                    totalScore += roomSeat.score
                }
            }
            val formattedScore = mScoreFormat.format(totalScore)
            mBinding.tvCumulativeScore.text = resources.getString(R.string.cantata_score_formatter, formattedScore)
        }
    }

    private var mInitialYOfChorus = 0f

    val getYOfChorusBtn: Int
        get() {
            if (mInitialYOfChorus == 0f) {
                mInitialYOfChorus = mBinding.ilActive.ivJoinChorusBtn.y
            }
            return mInitialYOfChorus.toInt()
        }

    interface OnKaraokeEventListener {
        fun onSwitchOriginalClick() {}
        fun onMenuClick() {}
        fun onPlayClick() {}
        fun onChangeMusicClick() {}
        fun onStartSing() {}
        fun onJoinChorus() {}
        fun onLeaveChorus() {}
        fun onDragTo(position: Long) {}
        fun onRefPitchUpdate(refPitch: Float, numberOfRefPitches: Int) {}
        fun onLineFinished(line: LyricsLineModel?, score: Int, cumulativeScore: Int, index: Int, total: Int) {}
        fun onSkipPreludeClick() {}
        fun onSkipPostludeClick() {}
        fun onReGetLrcUrl() {}

        fun onChorusUserClick() {}
    }
}
package io.agora.scene.cantata.ui.widget.lrcView

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.agora.karaoke_view.v11.KaraokeEvent
import io.agora.karaoke_view.v11.KaraokeView
import io.agora.karaoke_view.v11.LyricsView
import io.agora.karaoke_view.v11.model.LyricsLineModel
import io.agora.scene.base.utils.DownloadUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.ZipUtils
import io.agora.scene.base.utils.ZipUtils.UnZipCallback
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataLayoutLrcControlViewBinding
import io.agora.scene.cantata.databinding.CantataLayoutLrcPrepareBinding
import io.agora.scene.cantata.ktvapi.ILrcView
import io.agora.scene.cantata.service.RoomSelSongModel
import io.agora.scene.cantata.ui.widget.OnClickJackingListener
import io.agora.scene.widget.basic.OutlineSpan
import io.agora.scene.widget.utils.UiUtils
import java.io.File

/**
 * 歌词控制View
 */
class LrcControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), ILrcView {
    private val mBinding: CantataLayoutLrcControlViewBinding by lazy {
        CantataLayoutLrcControlViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private val mPrepareBinding: CantataLayoutLrcPrepareBinding by lazy {
        CantataLayoutLrcPrepareBinding.bind(mBinding.root)
    }
    var karaokeView: KaraokeView? = null
    var cumulativeScoreInPercentage = 0
    private var mComboControl: ComboControl? = null
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

    private var chorusScore = 0
    private fun initListener() {
        mBinding.ilActive.switchOriginal.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivMusicMenu.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivMusicStart.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivChangeSong.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivSkipPostludeSkip.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivSkipPreludeSkip.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivSkipPostludeCancel.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivSkipPreludeCancel.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivJoinChorusBtn.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.ivLeaveChorus.setOnClickListener(onClickJackingListener)
        mBinding.ilActive.downloadLrcFailedBtn.setOnClickListener(onClickJackingListener)
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
                if (mRole == Role.Singer) {
                    mOnKaraokeActionListener?.onLineFinished(line, score, cumulativeScore, index, total)
                } else if (mRole == Role.CoSinger) {
                    chorusScore += score
                    updateScore(
                        score.toDouble(), chorusScore.toDouble(),
                        /** Workaround(Hai_Guo) */
                        (total * 100).toDouble()
                    )
                }
            }
        })
    }

    fun setOnLrcClickListener(karaokeActionListener: OnKaraokeEventListener?) {
        mOnKaraokeActionListener = karaokeActionListener
    }

    fun onSelfJoinedChorus() {
        mBinding.tvCumulativeScore.text = String.format(
            resources.getString(R.string.cantata_score_formatter),
            "" + chorusScore
        )
        mRole = Role.CoSinger
        mBinding.ilActive.ivMusicStart.visibility = INVISIBLE
        mBinding.ilActive.switchOriginal.visibility = VISIBLE
        mBinding.ilActive.switchOriginal.isChecked = false // reset ui icon for mAudioTrackMode
        mBinding.ilActive.switchOriginal.setIconResource(R.mipmap.ic_play_original_off)
        mBinding.ilActive.ivMusicMenu.visibility = VISIBLE
        mBinding.ilActive.ivJoinChorusBtn.visibility = INVISIBLE
        mBinding.ilActive.ivLeaveChorus.visibility = VISIBLE
        mBinding.ilActive.ivJoinChorusLoading.visibility = INVISIBLE
        if (isMineOwner) {
            mBinding.ilActive.ivChangeSong.visibility = VISIBLE
        }
    }

    fun onSelfJoinedChorusFailed() {
        mBinding.ilActive.ivJoinChorusBtn.visibility = VISIBLE
        mBinding.ilActive.ivJoinChorusLoading.visibility = INVISIBLE
    }

    fun onSelfLeavedChorus() {
        mBinding.tvCumulativeScore.text =
            String.format(resources.getString(R.string.cantata_score_formatter), "" + 0)
        mRole = Role.Listener
        mBinding.ilActive.ivMusicStart.visibility = GONE
        mBinding.ilActive.switchOriginal.visibility = INVISIBLE
        mBinding.ilActive.ivMusicMenu.visibility = INVISIBLE
        mBinding.ilActive.ivJoinChorusBtn.visibility = VISIBLE
        mBinding.ilActive.ivLeaveChorus.visibility = INVISIBLE
    }

    private var isOnSeat = false
    fun onSeat(isOnSeat: Boolean) {
        this.isOnSeat = isOnSeat
    }

    private var isMineOwner = false
    fun onPrepareStatus(isMineOwner: Boolean) {
        chorusScore = 0
        this.isMineOwner = isMineOwner
        mBinding.ilIdle.root.visibility = GONE
        mBinding.clActive.visibility = VISIBLE
        mBinding.clActive.setBackgroundResource(backgroundResId)
        mPrepareBinding.statusPrepareViewLrc.visibility = VISIBLE
        mBinding.ilActive.root.visibility = GONE
        changeViewByRole()
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
            mBinding.ilActive.lyricsView.enableDragging(false)
            mBinding.ilActive.ivMusicStart.visibility = VISIBLE
            mBinding.ilActive.switchOriginal.visibility = VISIBLE
            mBinding.ilActive.ivMusicMenu.visibility = VISIBLE
            mBinding.ilActive.rlMusicControlMenu.visibility = VISIBLE
            mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
            mBinding.ilActive.switchOriginal.isChecked = false // reset ui icon for mAudioTrackMode
            mBinding.ilActive.switchOriginal.setIconResource(R.mipmap.ic_play_original_off)
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

    var role: Role
        get() = mRole
        set(mRole) {
            this.mRole = mRole
            lrcUrl = null
            changeViewByRole()
        }

    fun setMusic(mMusic: RoomSelSongModel) {
        karaokeView?.reset()
        mComboControl?.reset(mBinding)
        mBinding.tvMusicName.text = mMusic.songName + "-" + mMusic.singer
        mBinding.tvCumulativeScore.text =
            String.format(resources.getString(R.string.cantata_score_formatter), "0")
    }

    fun setCountDown(time: Int) {

    }


    private var backgroundResId = R.drawable.cantata_mv_bg
    fun setLrcViewBackground(@DrawableRes resId: Int) {
        backgroundResId = resId
        val mBitmap = BitmapFactory.decodeResource(resources, resId)
        Palette.from(mBitmap).generate { palette: Palette? ->
            if (palette == null) return@generate

            var defaultColor = ContextCompat.getColor(context, R.color.pink_b4)
            mBinding.ilActive.lyricsView.setCurrentLineHighlightedTextColor(defaultColor)
            defaultColor = ContextCompat.getColor(context, R.color.white)
            mBinding.ilActive.lyricsView.setCurrentLineTextColor(defaultColor)
        }
        mBinding.clActive.setBackgroundResource(resId)
    }

    fun updateScore(score: Double, cumulativeScore: Double, perfectScore: Double) {
        cumulativeScoreInPercentage = (cumulativeScore / perfectScore * 100).toInt()
        mBinding.tvCumulativeScore.text = String.format(
            resources.getString(R.string.cantata_score_formatter),
            "" + cumulativeScore.toInt()
        )
        if (mComboControl == null) {
            mComboControl = ComboControl()
        }
        mComboControl?.checkAndShowCombos(mBinding, score.toInt(), cumulativeScore.toInt())
    }

    class ComboControl {
        private var mComboIconDrawable: GifDrawable? = null
        private var mNumberOfCombos = 0
        fun reset(binding: CantataLayoutLrcControlViewBinding) {
            mNumberOfCombos = 0
            binding.comboView.root.visibility = INVISIBLE
        }

        fun checkAndShowCombos(binding: CantataLayoutLrcControlViewBinding, score: Int, cumulativeScore: Int) {
            binding.comboView.root.visibility = VISIBLE
            showComboAnimation(binding.comboView.root, score)
        }

        private var mComboOfLastTime = 0 // Only for showComboAnimation
        private fun showComboAnimation(comboView: View, score: Int) {
            var comboIconRes = 0
            if (score >= 90) {
                comboIconRes = R.drawable.cantata_combo_excellent
            } else if (score >= 75) {
                comboIconRes = R.drawable.cantata_combo_good
            } else if (score >= 60) {
                comboIconRes = R.drawable.cantata_combo_fair
            }
            val comboIcon = comboView.findViewById<ImageView>(R.id.combo_icon)
            val comboText = comboView.findViewById<TextView>(R.id.combo_text)
            val sameWithLastTime = comboIconRes == mComboOfLastTime
            mComboOfLastTime = comboIconRes
            if (comboIconRes > 0) {
                if (sameWithLastTime) {
                    mNumberOfCombos++
                } else {
                    mNumberOfCombos = 1
                }
                val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                val outlineSpan = OutlineSpan(
                    Color.parseColor("#368CFF"), 10f
                )
                Glide.with(comboView.context).asGif().load(comboIconRes).apply(options)
                    .addListener(object : RequestListener<GifDrawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<GifDrawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return true
                        }

                        override fun onResourceReady(
                            resource: GifDrawable,
                            model: Any,
                            target: Target<GifDrawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            mComboIconDrawable = resource
                            resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                                override fun onAnimationStart(drawable: Drawable) {
                                    super.onAnimationStart(drawable)
                                }

                                override fun onAnimationEnd(drawable: Drawable) {
                                    super.onAnimationEnd(drawable)
                                    comboText.alpha = 0f
                                    comboIcon.visibility = INVISIBLE
                                    comboText.visibility = INVISIBLE
                                    mComboIconDrawable!!.unregisterAnimationCallback(this)
                                }
                            })
                            resource.setLoopCount(1)
                            comboIcon.visibility = VISIBLE
                            comboText.alpha = 0f
                            comboText.visibility =
                                if (mNumberOfCombos == 1) INVISIBLE else VISIBLE // Per request from product team, do not show `+X` view for first one
                            if (mNumberOfCombos != 1) {
                                val text = "x$mNumberOfCombos"
                                val spannable = SpannableString(text)
                                spannable.setSpan(outlineSpan, 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                comboText.text = spannable
                                comboText.animate().alpha(1f).setDuration(500).setStartDelay(0).start()
                            }
                            return false
                        }
                    }).into(comboIcon)
            } else {
                mNumberOfCombos = 0
                comboIcon.visibility = INVISIBLE
                comboText.visibility = INVISIBLE
            }
        }
    }

    private val onClickJackingListener = object : OnClickJackingListener() {

        override fun onClickJacking(v: View) {
            if (v === mBinding.ilActive.switchOriginal) {
                mOnKaraokeActionListener?.onSwitchOriginalClick()
                val withOriginal = mBinding.ilActive.switchOriginal.isChecked
                mBinding.ilActive.switchOriginal.setIconResource(if (withOriginal) R.mipmap.ic_play_original_on else R.mipmap.ic_play_original_off)
            } else if (v === mBinding.ilActive.ivMusicMenu) {
                mOnKaraokeActionListener?.onMenuClick()
            } else if (v === mBinding.ilActive.ivMusicStart) {
                mOnKaraokeActionListener?.onPlayClick()
            } else if (v === mBinding.ilActive.ivChangeSong) {
                mOnKaraokeActionListener?.onChangeMusicClick()
            } else if (v === mBinding.ilActive.ivSkipPreludeSkip) {
                mOnKaraokeActionListener?.onSkipPreludeClick()
                mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
            } else if (v === mBinding.ilActive.ivSkipPostludeSkip) {
                mOnKaraokeActionListener?.onSkipPostludeClick()
            } else if (v === mBinding.ilActive.ivSkipPreludeCancel) {
                mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
            } else if (v === mBinding.ilActive.ivSkipPostludeCancel) {
                mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
            } else if (v === mBinding.ilActive.ivJoinChorusBtn) {
                if (UiUtils.isFastClick(2000)) {
                    ToastUtils.showToast(R.string.cantata_too_fast)
                    return
                }
                mOnKaraokeActionListener?.onJoinChorus()
                mBinding.ilActive.ivJoinChorusLoading.visibility = VISIBLE
                if (isMineOwner) {
                    mBinding.ilActive.ivChangeSong.visibility = INVISIBLE
                }
            } else if (v === mBinding.ilActive.ivLeaveChorus) {
                mOnKaraokeActionListener?.onLeaveChorus()
            } else if (v === mBinding.ilActive.downloadLrcFailedBtn) {
                mBinding.ilActive.downloadLrcFailedView.visibility = INVISIBLE
                mBinding.ilActive.downloadLrcFailedBtn.visibility = INVISIBLE
                if (lrcUrl == null) {
                    mOnKaraokeActionListener!!.onReGetLrcUrl()
                } else {
                    downloadAndSetLrcData()
                }
            }
        }}

        fun setSwitchOriginalChecked(checked: Boolean) {
            mBinding.ilActive.switchOriginal.isChecked = checked
        }

        // ------------------ ILrcView ------------------
        override fun onUpdatePitch(pitch: Float) {
            karaokeView?.setPitch(pitch)
        }

        override fun onUpdateProgress(progress: Long) {
            if (karaokeView?.lyricsData == null) return
            if (mRole == Role.Singer) {
                if (progress >= karaokeView!!.lyricsData.startOfVerse - 2000) {
                    mBinding.ilActive.ivSkipPrelude.visibility = INVISIBLE
                }
                if (progress >= karaokeView!!.lyricsData.duration) {
                    mBinding.ilActive.ivSkipPostlude.visibility = VISIBLE
                } else {
                    mBinding.ilActive.ivSkipPostlude.visibility = INVISIBLE
                }
            }
            karaokeView!!.setProgress(progress)
        }

        private var lrcUrl: String? = null


        override fun onDownloadLrcData(url: String?) {
            lrcUrl = url
            downloadAndSetLrcData()
        }

        private fun downloadAndSetLrcData() {
            DownloadUtils.getInstance().download(context, lrcUrl, { file: File ->
                if (file.name.endsWith(".zip")) {
                    ZipUtils.unzipOnlyPlainXmlFilesAsync(file.absolutePath,
                        file.absolutePath.replace(".zip", ""),
                        object : UnZipCallback {
                            override fun onFileUnZipped(unZipFilePaths: List<String>) {
                                var xmlPath = ""
                                for (path in unZipFilePaths) {
                                    if (path.endsWith(".xml")) {
                                        xmlPath = path
                                        break
                                    }
                                }
                                if (TextUtils.isEmpty(xmlPath)) {
                                    ToastUtils.showToast("The xml file not exist!")
                                    mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                                    mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
                                    return
                                }
                                val xmlFile = File(xmlPath)
                                val lyricsModel = KaraokeView.parseLyricsData(xmlFile)
                                if (lyricsModel == null) {
                                    ToastUtils.showToast("Unexpected content from $xmlPath")
                                    mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                                    mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
                                    return
                                }
                                if (karaokeView != null) {
                                    mBinding.ilActive.downloadLrcFailedView.visibility = INVISIBLE
                                    karaokeView?.lyricsData = lyricsModel
                                }
                            }

                            override fun onError(e: Exception) {
                                mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                                mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
                                ToastUtils.showToast(e.message)
                            }
                        })
                } else {
                    val lyricsModel = KaraokeView.parseLyricsData(file)
                    if (lyricsModel == null) {
                        ToastUtils.showToast("Unexpected content from $file")
                        mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                        mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
                        return@download
                    }
                    if (karaokeView != null) {
                        mBinding.ilActive.downloadLrcFailedView.visibility = INVISIBLE
                        mBinding.ilActive.downloadLrcFailedBtn.visibility = INVISIBLE
                        karaokeView!!.lyricsData = lyricsModel
                    }
                }
            }) { exception: Exception ->
                ToastUtils.showToast(exception.message)
                mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
                mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
            }
        }

        fun onNoLrc() {
            lrcUrl = null
            mBinding.ilActive.downloadLrcFailedView.visibility = VISIBLE
            mBinding.ilActive.downloadLrcFailedBtn.visibility = VISIBLE
        }

        fun onReceiveSingleLineScore(score: Int, index: Int, cumulativeScore: Int, total: Int) {
            if (mRole == Role.Listener) {
                updateScore(
                    score.toDouble(), cumulativeScore.toDouble(),
                    /** Workaround(Hai_Guo) */
                    total.toDouble()
                )
            }
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
        }
    }
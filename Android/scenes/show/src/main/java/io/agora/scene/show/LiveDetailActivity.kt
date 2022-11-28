package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.databinding.ShowLiveDetailMessageItemBinding
import io.agora.scene.show.service.ShowMessage
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.show.widget.*
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.StatusBarUtil
import java.text.SimpleDateFormat
import java.util.*

class LiveDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun launch(context: Context, roomDetail: ShowRoomDetailModel) {
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO, roomDetail)
            })
        }
    }

    private val mRoomInfo by lazy { intent.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as ShowRoomDetailModel }
    private val mBinding by lazy { ShowLiveDetailActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val isRoomOwner by lazy { mRoomInfo.ownerId == UserManager.getInstance().user.id.toString() }

    private var mMessageAdapter: BindingSingleAdapter<ShowMessage, ShowLiveDetailMessageItemBinding>? =
        null

    private lateinit var mPermissionHelp: PermissionHelp
    private lateinit var mRtcEngine: RtcEngineEx

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPermissionHelp = PermissionHelp(this)
        initView()
        initService()
        initRtcEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyService()
        destroyRtcEngine()
    }

    //================== UI Operation ===============

    private fun initView() {
        initTopLayout()
        initBottomLayout()
        initMessageLayout()
    }

    private fun initTopLayout() {
        val topLayout = mBinding.topLayout
        topLayout.ivOwnerAvatar.setImageResource(R.mipmap.portrait03)
        topLayout.tvRoomName.text = mRoomInfo.roomName
        topLayout.tvRoomId.text = getString(R.string.show_room_id, mRoomInfo.roomId)
        topLayout.tvUserCount.text = mRoomInfo.roomUserCount.toString()
        topLayout.ivClose.setOnClickListener { onBackPressed() }

        // Start Timer counter
        val dataFormat =
            SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT+0") }
        topLayout.tvTimer.post(object : Runnable {
            override fun run() {
                topLayout.tvTimer.text =
                    dataFormat.format(System.currentTimeMillis() - mRoomInfo.createAt)
                topLayout.tvTimer.postDelayed(this, 1000)
            }
        })

    }

    private fun initBottomLayout() {
        val bottomLayout = mBinding.bottomLayout
        bottomLayout.tvChat.setOnClickListener {
            showMessageInputDialog()
        }
        bottomLayout.ivSetting.setOnClickListener {
            showSettingDialog()
        }
        bottomLayout.ivBeauty.setOnClickListener{
            showBeautyDialog()
        }
        bottomLayout.ivMusic.setOnClickListener {
            showMusicEffectDialog()
        }
    }

    private fun initMessageLayout() {
        val messageLayout = mBinding.messageLayout
        mMessageAdapter =
            object : BindingSingleAdapter<ShowMessage, ShowLiveDetailMessageItemBinding>() {
                override fun onBindViewHolder(
                    holder: BindingViewHolder<ShowLiveDetailMessageItemBinding>, position: Int
                ) {
                    val item = getItem(position) ?: return
                    holder.binding.text.text = SpannableStringBuilder().append(
                        "${item.userName}: ",
                        ForegroundColorSpan(Color.parseColor("#A6C4FF")),
                        SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
                    ).append(
                        item.message,
                        ForegroundColorSpan(Color.WHITE),
                        SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
        (messageLayout.rvMessage.layoutManager as LinearLayoutManager).let {
            it.stackFromEnd = true
        }
        messageLayout.rvMessage.adapter = mMessageAdapter
    }

    private fun showMessageInputDialog() {
        TextInputDialog(this).setOnInsertHeightChangeListener {
            mBinding.messageLayout.root.layoutParams =
                (mBinding.messageLayout.root.layoutParams as MarginLayoutParams).apply {
                    bottomMargin = it
                }
        }.setOnSentClickListener { dialog, msg ->
            mService.sendChatMessage(msg)
        }.show()
    }

    private fun updateTopUserCount(count: Int) =
        runOnUiThread { mBinding.topLayout.tvUserCount.text = count.toString() }

    private fun showPermissionLeakDialog(yes: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.show_live_perms_leak_tip)
            setCancelable(false)
            setPositiveButton(R.string.show_live_yes) { dialog, _ ->
                dialog.dismiss()
                checkRequirePerms(true, yes)
            }
            setNegativeButton(R.string.show_live_no) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            show()
        }
    }

    private fun insertMessageItem(msg: ShowMessage) = runOnUiThread {
        mMessageAdapter?.let {
            it.insertLast(msg)
            mBinding.messageLayout.rvMessage.scrollToPosition(it.itemCount - 1)
        }
    }

    private fun showSettingDialog() {
        SettingDialog(this).apply {
            setHostView(isRoomOwner)
            setItemActivated(SettingDialog.ITEM_ID_VIDEO, true)
            setItemActivated(SettingDialog.ITEM_ID_MIC, true)
            setOnItemActivateChangedListener{dialog, itemId, activated ->
                when(itemId){
                    SettingDialog.ITEM_ID_CAMERA -> mRtcEngine.switchCamera()
                    SettingDialog.ITEM_ID_QUALITY -> showPictureQualityDialog(this)
                    SettingDialog.ITEM_ID_VIDEO -> mRtcEngine.enableLocalVideo(activated)
                    SettingDialog.ITEM_ID_MIC -> mRtcEngine.enableLocalAudio(activated)
                }
            }
            show()
        }
    }

    private fun showPictureQualityDialog(parentDialog: SettingDialog) {
        PictureQualityDialog(this).apply {
            setOnQualitySelectListener { dialog, qualityIndex ->
                when(qualityIndex){
                    PictureQualityDialog.QUALITY_INDEX_1080P -> {}
                    PictureQualityDialog.QUALITY_INDEX_720P -> {}
                    PictureQualityDialog.QUALITY_INDEX_540P -> {}
                    PictureQualityDialog.QUALITY_INDEX_360P -> {}
                }
            }

            setOnShowListener { parentDialog.dismiss() }
            setOnDismissListener { parentDialog.show() }
            show()
        }
    }

    private fun showBeautyDialog(){
        BeautyDialog(this).apply {
            show()
        }
    }

    private fun showMusicEffectDialog(){
        MusicEffectDialog(this).apply {
            show()
        }
    }


    //================== Service Operation ===============

    private fun initService() {
        mService.subscribeUser { _, _ ->
            mService.getAllUserList({
                updateTopUserCount(it.size)
            })
        }
        mService.subscribeMessage { _, showMessage ->
            insertMessageItem(showMessage)
        }
    }

    private fun destroyService() {
        mService.leaveRoom()
    }


    //================== RTC Operation ===================

    private fun initRtcEngine() {
        val config = RtcEngineConfig()
        config.mContext = this
        config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {

            override fun onError(err: Int) {
                super.onError(err)
                ToastUtils.showToast(RtcEngine.getErrorDescription(err))
            }

        }
        mRtcEngine = RtcEngine.create(config) as RtcEngineEx
        mRtcEngine.enableVideo()

        checkRequirePerms {
            joinChannel()
        }
    }

    private fun destroyRtcEngine() {
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
    }

    private fun joinChannel() {
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId
        TokenGenerator.generateTokens(
            channelName,
            uid.toString(),
            TokenGenerator.TokenGeneratorType.token006,
            arrayOf(TokenGenerator.AgoraTokenType.rtc),
            {
                val channelMediaOptions = ChannelMediaOptions()
                channelMediaOptions.clientRoleType =
                    if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
                mRtcEngine.joinChannel(
                    it[TokenGenerator.AgoraTokenType.rtc],
                    channelName,
                    uid.toInt(),
                    channelMediaOptions
                )

                // Render host video
                val videoView = SurfaceView(this)
                mBinding.videoSinglehostLayout.videoContainer.addView(videoView)
                if (isRoomOwner) {
                    mRtcEngine.setupLocalVideo(VideoCanvas(videoView))
                } else {
                    mRtcEngine.setupRemoteVideo(
                        VideoCanvas(
                            videoView,
                            Constants.RENDER_MODE_HIDDEN,
                            mRoomInfo.ownerId.toInt()
                        )
                    )
                }
            })
    }

    private fun checkRequirePerms(force: Boolean = false, granted: () -> Unit) {
        if (!isRoomOwner) {
            granted.invoke()
            return
        }
        mPermissionHelp.checkCameraAndMicPerms(
            {
                granted.invoke()
            },
            {
                showPermissionLeakDialog(granted)
            },
            force
        )
    }


}
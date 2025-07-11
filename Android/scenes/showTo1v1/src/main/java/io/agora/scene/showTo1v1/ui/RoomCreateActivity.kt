package io.agora.scene.showTo1v1.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomCreateActivityBinding
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.StatusBarUtil
import java.util.Random

/*
 * Live streaming host preview page activity
 */
class RoomCreateActivity : BaseViewBindingActivity<ShowTo1v1RoomCreateActivityBinding>() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, RoomCreateActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private val mService by lazy { mShowTo1v1Manger.mService }

    private val mTextureView by lazy { TextureView(this) }

    private lateinit var roomNameArray: Array<String>
    private val random = Random()

    private var isFinishToLiveDetail = false

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomCreateActivityBinding {
        return ShowTo1v1RoomCreateActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        StatusBarUtil.hideStatusBar(window, true)
        toggleVideoRun = Runnable {
            initRtcEngine()
        }
        requestCameraPermission(true)
    }

    private fun initRtcEngine() {
        mRtcEngine.startPreview()
        mRtcEngine.setupLocalVideo(VideoCanvas(mTextureView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        binding.flVideoContainer.addView(mTextureView)
    }

    private fun setOnApplyWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(0, 0, 0, inset.bottom)
            binding.titleView.setPaddingRelative(0, inset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            cleanupAndFinish()
        }
    }

    private fun cleanupAndFinish() {
        mRtcEngine.stopPreview()
        finish()
    }

    override fun finish() {
        onBackPressedCallback.remove()
        super.finish()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        roomNameArray = resources.getStringArray(R.array.show_to1v1_room_name)
        enableCrateRoomButton(true)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding.titleView.setRightIconClick {
            cleanupAndFinish()
        }
        binding.tvContent.text = getString(R.string.show_to1v1_create_room_tips, SceneConfigManager.oneOnOneExpireTime/60)
        binding.tvRandom.setOnClickListener {
            val nameIndex = random.nextInt(roomNameArray.size)
            val nameSufIndex = random.nextInt(1000000)
            binding.etRoomName.setText("${roomNameArray[nameIndex]}$nameSufIndex")
        }
        binding.etRoomName.doAfterTextChanged {

        }
        binding.layoutRoomCreating.setOnClickListener {
            val roomName = binding.etRoomName.text.toString()
            if (roomName.isEmpty()) {
                CustomToast.show(R.string.show_to1v1_room_name_empty_tips)
                return@setOnClickListener
            }
            enableCrateRoomButton(false)
            mShowTo1v1Manger.renewTokens {
                if (it) {
                    mShowTo1v1Manger.setup(this) { e ->
                        if (e == null) {
                            mService?.createRoom(roomName, completion = { error, roomInfo ->
                                if (error == null && roomInfo != null) { // success
                                    isFinishToLiveDetail = true
                                    RoomDetailActivity.launch(this, false, roomInfo)
                                    finish()
                                } else { //failed
                                    CustomToast.show(error?.message?:"create room failed!")
                                    enableCrateRoomButton(true)
                                }
                            })
                        } else {
                            CustomToast.show(e.msg)
                            enableCrateRoomButton(true)
                        }
                    }

                } else {
                    CustomToast.show("fetch token failed!")
                    enableCrateRoomButton(true)
                }
            }
        }

    }

    private fun enableCrateRoomButton(enable: Boolean) {
        if (enable) {
            binding.tvCreate.isVisible = true
            binding.progressLoading.isVisible = false
            binding.tvRoomCreating.isVisible = false
            binding.layoutRoomCreating.isEnabled = true
            binding.layoutRoomCreating.alpha = 1.0f
        } else {
            binding.layoutRoomCreating.isEnabled = false
            binding.layoutRoomCreating.alpha = 0.6f
            binding.tvCreate.isVisible = false
            binding.progressLoading.isVisible = true
            binding.tvRoomCreating.isVisible = true
        }
    }

    private var toggleVideoRun: Runnable? = null

    override fun getPermissions() {
        toggleVideoRun?.let {
            it.run()
            toggleVideoRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishToLiveDetail) {
            mRtcEngine.stopPreview()
        }
    }
}
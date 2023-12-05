package io.agora.scene.joy.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.agora.rtc2.Constants
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.base.DataState
import io.agora.scene.joy.databinding.JoyActivityLivePrepareBinding
import io.agora.scene.joy.databinding.JoyItemGameBannerLayoutBinding
import io.agora.scene.joy.network.JoyGameListResult
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil
import java.util.Random
import java.util.Timer
import java.util.TimerTask

class LivePrepareActivity : BaseViewBindingActivity<JoyActivityLivePrepareBinding>() {

    companion object {
        private const val TAG = "Joy_LivePrepareActivity"

        fun launch(context: Context) {
            val intent = Intent(context, LivePrepareActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mJoyViewModel: JoyViewModel by lazy {
        ViewModelProvider(this)[JoyViewModel::class.java]
    }

    private val mJoyService by lazy { JoyServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private lateinit var mRoomNameArray: Array<String>
    private val mRandom = Random()

    private var mIsFinishToLiveDetail = false

    private val mTextureView by lazy {
        TextureView(this)
    }

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var mCurrentPos = 1
    private var mTimer: Timer? = null

    private val mGameInfoAdapter by lazy {
        GameInfoAdapter(emptyList(), itemClick = {
            ToastUtils.showToast("click $it")
        })
    }

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityLivePrepareBinding {
        return JoyActivityLivePrepareBinding.inflate(inflater)
    }

    override fun init() {
        super.init()
        mRoomNameArray = resources.getStringArray(R.array.joy_roomName_array)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.ivTitleBack.setOnClickListener { onBackPressed() }
        binding.iBtnRefresh.setOnClickListener {
            val nameIndex = mRandom.nextInt(mRoomNameArray.size)
            binding.etRoomName.setText(mRoomNameArray[nameIndex])
        }
        binding.btnStartLive.setOnClickListener {
            val roomName = binding.etRoomName.text.toString()
            if (roomName.isEmpty()) {
                ToastUtils.showToast(R.string.joy_room_name_empty_tips)
                return@setOnClickListener
            }
            enableCrateRoomButton(false)
            mJoyService.createRoom(roomName, completion = { error, roomInfo ->
                if (error == null && roomInfo != null) { // success
                    if (mGameInfoAdapter.gameInfoList.isEmpty()) {
                        ToastUtils.showToast("游戏资源获取中...")
                    } else {
                        mIsFinishToLiveDetail = true
                        RoomLivingActivity.launch(this, roomInfo, mGameInfoAdapter.gameInfoList)
                        finish()
                    }
                } else { //failed
                    ToastUtils.showToast(error?.message)
                    enableCrateRoomButton(true)
                }
            })
        }
        mToggleVideoRun = Runnable {
            binding.flVideoContainer.post {
                mRtcEngine.setupLocalVideo(VideoCanvas(mTextureView, Constants.RENDER_MODE_HIDDEN))
                binding.flVideoContainer.addView(mTextureView)
            }
            initRtcEngine()
        }
        requestCameraPermission(true)

        binding.vpGame.adapter = mGameInfoAdapter
        binding.dotIndicator.setViewPager2(binding.vpGame, true)
        binding.vpGame.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mCurrentPos = position
                Log.e("zhangw", "onPageSelected-1: pos:$position")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("zhangw", "onPageScrollStateChanged-3: state:$state")
                //只有在空闲状态，才让自动滚动
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (mCurrentPos == 0) {
                        binding.vpGame.setCurrentItem(mGameInfoAdapter.itemCount - 2, false)
                    }
                    if (mCurrentPos == mGameInfoAdapter.itemCount - 1) {
                        binding.vpGame.setCurrentItem(1, false)
                    }
                }
            }
        })
        binding.vpGame.getChildAt(0).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> stopAutoScroll()
                MotionEvent.ACTION_UP -> startAutoScroll()
            }
            return@setOnTouchListener false
        }
        binding.vpGame.currentItem = 1
        startAutoScroll()
    }

    override fun requestData() {
        super.requestData()
        mJoyViewModel.getGames()
        mJoyViewModel.mGameListLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    val list = it.data?.list
                    if (!list.isNullOrEmpty()) {
                        mGameInfoAdapter.setDataList(list)
                    }

                }
            }
        }
    }

    private fun startAutoScroll() {
        mTimer?.cancel()
        mTimer = Timer()
        mTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mMainHandler.post {
                    binding.vpGame.apply {
                        if (currentItem + 1 == mGameInfoAdapter.itemCount - 1) {
                            setCurrentItem(1, false)
                        } else if (currentItem + 1 < mGameInfoAdapter.itemCount - 1) {
                            setCurrentItem(currentItem + 1, true)
                        }
                    }
                }
            }
        }, 3000, 3000)
    }

    private fun stopAutoScroll() {
        mTimer?.cancel()
    }

    private fun initRtcEngine() {
        mRtcEngine.setCameraCapturerConfiguration(
            CameraCapturerConfiguration(
                CameraCapturerConfiguration.CaptureFormat(
                    540,
                    960,
                    15
                )
            )
        )
    }


    private fun enableCrateRoomButton(enable: Boolean) {
        if (enable) {
            binding.btnStartLive.isEnabled = true
            binding.btnStartLive.alpha = 1.0f
        } else {
            binding.btnStartLive.isEnabled = false
            binding.btnStartLive.alpha = 0.6f
        }
    }

    private var mToggleVideoRun: Runnable? = null

    override fun getPermissions() {
        mToggleVideoRun?.let {
            it.run()
            mToggleVideoRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun onResume() {
        super.onResume()
        mRtcEngine.startPreview()
    }

    override fun onPause() {
        super.onPause()
        if (mIsFinishToLiveDetail) {
            mRtcEngine.stopPreview()
        }
    }

    override fun onBackPressed() {
        mRtcEngine.stopPreview()
        super.onBackPressed()
    }


    class GameInfoAdapter constructor(
        var gameInfoList: List<JoyGameListResult>,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.Adapter<GameInfoAdapter.GameViewHolder>() {

        inner class GameViewHolder(val binding: JoyItemGameBannerLayoutBinding) : RecyclerView.ViewHolder(binding.root)

        fun setDataList(list: List<JoyGameListResult>) {
            gameInfoList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
            return GameViewHolder(
                JoyItemGameBannerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int {
            return gameInfoList.size
        }

        override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
            val data = gameInfoList[position]
            // TODO:
            holder.binding.ivGuide.setImageResource(R.mipmap.joy_img_room_item_bg_0)
            holder.binding.root.setOnClickListener {
                itemClick.invoke(position)
            }
        }
    }
}

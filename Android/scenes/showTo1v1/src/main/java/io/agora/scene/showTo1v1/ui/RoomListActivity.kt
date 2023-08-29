package io.agora.scene.showTo1v1.ui

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import io.agora.scene.base.GlideApp
import io.agora.scene.base.GlideOptions
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.RtcEngineInstance
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.ui.fragment.RoomListFragment
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.BlurTransformation
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>() {

    companion object {
        private const val TAG = "RoomListActivity"

        private const val POSITION_NONE = -1
    }

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }
    private val mRtcVideoSwitcher by lazy { RtcEngineInstance.videoSwitcher }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var fragmentAdapter: FragmentStateAdapter? = null
    private val mRoomInfoList = mutableListOf<ShowTo1v1RoomInfo>()

    private val vpFragments = SparseArray<RoomListFragment>()
    private var currLoadPosition = POSITION_NONE

    private var toggleVideoRun: Runnable? = null
    private var toggleAudioRun: Runnable? = null

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

    fun toggleSelfVideo(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleVideoRun = Runnable { callback.invoke() }
            requestCameraPermission(true)
        } else {
            callback.invoke()
        }
    }

    fun toggleSelfAudio(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleAudioRun = Runnable {
                callback.invoke()
            }
            requestRecordPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomListActivityBinding {
        return ShowTo1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        StatusBarUtil.hideStatusBar(window, true)
        fetchService()
    }

    private fun setOnApplyWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(0, 0, 0, inset.bottom)
            binding.titleView.setPaddingRelative(0, inset.top, 0, 0)
            binding.emptyInclude.root.setPaddingRelative(0, inset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleView.setLeftClick { finish() }
        binding.titleView.setRightIconClick {
            fetchRoomList()
        }
    }

    private fun initViewPage() {
        if (fragmentAdapter == null) {
            // 设置预加载
            val preloadCount = 3
            binding.viewPager2.offscreenPageLimit = preloadCount - 2
            fragmentAdapter = object : FragmentStateAdapter(this) {
                override fun getItemCount() = Int.MAX_VALUE

                override fun createFragment(position: Int): Fragment {
                    val roomInfo = mRoomInfoList[position % mRoomInfoList.size]

                    return RoomListFragment.newInstance(roomInfo).also {
                        vpFragments.put(position, it)
                        if (position == binding.viewPager2.currentItem) {
                            it.startLoadPageSafely()
                        }
                    }
                }
            }
            binding.viewPager2.adapter = fragmentAdapter
            binding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
        }
    }

    private val onPageChangeCallback = object : OnPageChangeCallback() {

        private val PRE_LOAD_OFFSET = 0.01f
        private var preLoadPosition = POSITION_NONE
        private var lastOffset = 0f
        private var scrollStatus: Int = ViewPager2.SCROLL_STATE_IDLE

        private var hasPageSelected = false

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            Log.d(TAG, "PageChange onPageScrollStateChanged state=$state hasPageSelected=$hasPageSelected")
            when (state) {
                ViewPager2.SCROLL_STATE_SETTLING -> binding.viewPager2.isUserInputEnabled = false
                ViewPager2.SCROLL_STATE_IDLE -> {
                    binding.viewPager2.isUserInputEnabled = true
                    if (!hasPageSelected) {
                        if (preLoadPosition != POSITION_NONE) {
                            vpFragments[preLoadPosition]?.stopLoadPage(true)
                        }
                        preLoadPosition = POSITION_NONE
                        lastOffset = 0f
                    }
                    hasPageSelected = false
                }

                else -> {
                    // nothing
                }
            }
            scrollStatus = state
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            Log.d(TAG, "PageChange onPageScrolled positionOffset=$positionOffset")
            if (scrollStatus == ViewPager2.SCROLL_STATE_DRAGGING) {
                if (lastOffset > 0f) {
                    val isMoveUp = (positionOffset - lastOffset) > 0
                    if (isMoveUp && positionOffset >= PRE_LOAD_OFFSET && preLoadPosition == POSITION_NONE) {
                        preLoadPosition = currLoadPosition + 1
                        vpFragments[preLoadPosition]?.startLoadPageSafely()
                    } else if (!isMoveUp && positionOffset <= (1 - PRE_LOAD_OFFSET) && preLoadPosition == POSITION_NONE) {
                        preLoadPosition = currLoadPosition - 1
                        vpFragments[preLoadPosition]?.startLoadPageSafely()
                    }
                }
                lastOffset = positionOffset
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(
                TAG,
                "PageChange onPageSelected position=$position, currLoadPosition=$currLoadPosition, preLoadPosition=$preLoadPosition"
            )
            if (currLoadPosition != POSITION_NONE) {
                if (preLoadPosition != POSITION_NONE) {
                    if (position == preLoadPosition) {
                        vpFragments[currLoadPosition]?.stopLoadPage(true)
                    } else {
                        vpFragments[preLoadPosition]?.stopLoadPage(true)
                    }
                } else if (currLoadPosition != position) {
                    vpFragments[currLoadPosition]?.stopLoadPage(true)
                    vpFragments[position]?.startLoadPageSafely()
                }
            }
            currLoadPosition = position
            preLoadPosition = POSITION_NONE
            lastOffset = 0f
            hasPageSelected = true
        }
    }

    private fun fetchService() {
        //获取token
        val localUId = UserManager.getInstance().user.id.toInt()
        TokenGenerator.generateToken("", localUId.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            TokenGenerator.AgoraTokenType.rtc,
            success = {
                RtcEngineInstance.setupGeneralToken(it)
                ShowTo1v1Logger.d(TAG, "generateToken success：$it， uid：$localUId")
            },
            failure = {
                ShowTo1v1Logger.e(TAG, it, "generateToken failure：$it")
                ToastUtils.showToast(it?.message ?: "generate token failure")
            })
    }

    private fun fetchRoomList() {
        mService.getRoomList(completion = { error, roomList ->
            if (error == null) { // success
                mRoomInfoList.clear()
                mRoomInfoList.addAll(roomList)
                updateListView()
                if (mRoomInfoList.isNotEmpty()) {
                    initViewPage()
                }
            } else {
                updateListView()
            }
        })
    }

    private fun updateListView() {
        if (mRoomInfoList.isEmpty()) {
            StatusBarUtil.hideStatusBar(window, true)
            binding.emptyInclude.emptyRoot.isVisible = true
        } else {
            StatusBarUtil.hideStatusBar(window, false)
            binding.emptyInclude.emptyRoot.isVisible = false
        }
    }
}
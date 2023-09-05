package io.agora.scene.show

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.service.ROOM_AVAILABLE_DURATION
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.utils.RunnableWithDenied
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil


class LiveDetailActivity : BaseViewBindingActivity<ShowLiveDetailActivityBinding>(), LiveDetailFragment.OnMeLinkingListener {
    private val TAG = "LiveDetailActivity"

    companion object {
        private const val EXTRA_ROOM_DETAIL_INFO_LIST = "roomDetailInfoList"
        private const val EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX =
            "roomDetailInfoListSelectedIndex"
        private const val EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE = "roomDetailInfoListScrollable"


        fun launch(context: Context, roomDetail: ShowRoomDetailModel) {
            launch(context, arrayListOf(roomDetail), 0, false)
        }

        fun launch(
            context: Context,
            roomDetail: ArrayList<ShowRoomDetailModel>,
            selectedIndex: Int,
            scrollable: Boolean
        ) {
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST, roomDetail)
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, selectedIndex)
                putExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE, scrollable)
            })
        }
    }

    private val mRoomInfoList by lazy {
        intent.getParcelableArrayListExtra<ShowRoomDetailModel>(
            EXTRA_ROOM_DETAIL_INFO_LIST
        )!!
    }
    private val mScrollable by lazy {
        intent.getBooleanExtra(
            EXTRA_ROOM_DETAIL_INFO_LIST_SCROLLABLE,
            true
        )
    }

    private val POSITION_NONE = -1
    private val vpFragments = SparseArray<LiveDetailFragment>()
    private var currLoadPosition = POSITION_NONE

    private var toggleVideoRun: RunnableWithDenied? = null
    private var toggleAudioRun: Runnable? = null

    override fun getPermissions() {
        if (toggleVideoRun != null) {
            toggleVideoRun?.run()
            toggleVideoRun = null
        }
        if (toggleAudioRun != null) {
            toggleAudioRun?.run()
            toggleAudioRun = null
        }
    }


    fun toggleSelfVideo(isOpen: Boolean, callback : (result:Boolean) -> Unit) {
        if (isOpen) {
            toggleVideoRun = object :RunnableWithDenied(){
                override fun onDenied() {
                    callback.invoke(false)
                }

                override fun run() {
                    callback.invoke(true)
                }
            }
            requestCameraPermission(true)
        } else {
            callback.invoke(true)
        }
    }

    fun toggleSelfAudio(isOpen: Boolean, callback : () -> Unit) {
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
        if (toggleVideoRun != null && permission == Manifest.permission.CAMERA) {
            toggleVideoRun?.onDenied()
        }
        PermissionLeakDialog(this).show(permission, { getPermissions() }
        ) { launchAppSetting(permission) }
    }

    override fun onMeLinking(isLinking: Boolean) {
        // 连麦观众禁止切换房间
        binding.viewPager2.isUserInputEnabled = !isLinking
    }

    override fun getViewBinding(inflater: LayoutInflater): ShowLiveDetailActivityBinding {
        return  ShowLiveDetailActivityBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.viewPager2) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.viewPager2.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val selectedRoomIndex = intent.getIntExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, 0)

        // 设置token有效期为房间存活时长，到期后关闭并退出房间
        TokenGenerator.expireSecond =
            ROOM_AVAILABLE_DURATION / 1000 + 10 // 20min + 10s，加10s防止临界条件下报token无效

//        // 设置预加载
        val preloadCount = 3
//        mVideoSwitcher.setPreloadCount(preloadCount)
//        mVideoSwitcher.preloadConnections(mRoomInfoList.map {
//            RtcConnection(
//                it.roomId,
//                UserManager.getInstance().user.id.toInt()
//            )
//        })

        // 设置vp当前页面外的页面数
        binding.viewPager2.offscreenPageLimit = preloadCount - 2
        val fragmentAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = if (mScrollable) Int.MAX_VALUE else 1

            override fun createFragment(position: Int): Fragment {
                val roomInfo = if (mScrollable) {
                    mRoomInfoList[position % mRoomInfoList.size]
                } else {
                    mRoomInfoList[selectedRoomIndex]
                }
                return LiveDetailFragment.newInstance(roomInfo).apply {
                    vpFragments.put(position, this)
                    if (position == binding.viewPager2.currentItem) {
                        startLoadPageSafely()
                    }
                }
            }
        }
        binding.viewPager2.adapter = fragmentAdapter
        binding.viewPager2.isUserInputEnabled = mScrollable
        if (mScrollable) {
            binding.viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {

                private val PRE_LOAD_OFFSET = 0.01f

                private var preLoadPosition = POSITION_NONE
                private var lastOffset = 0f
                private var scrollStatus: Int = ViewPager2.SCROLL_STATE_IDLE

                private var hasPageSelected = false

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    Log.d(TAG, "PageChange onPageScrollStateChanged state=$state hasPageSelected=$hasPageSelected")
                    when(state){
                        ViewPager2.SCROLL_STATE_SETTLING -> binding.viewPager2.isUserInputEnabled = false
                        ViewPager2.SCROLL_STATE_IDLE -> {
                            binding.viewPager2.isUserInputEnabled = true
                            if(!hasPageSelected){
                                if(preLoadPosition != POSITION_NONE){
                                    vpFragments[preLoadPosition]?.stopLoadPage(true)
                                }
                                vpFragments[currLoadPosition]?.reLoadPage()
                                preLoadPosition = POSITION_NONE
                                lastOffset = 0f
                            }
                            hasPageSelected = false
                        }
                    }
                    scrollStatus = state
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
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
                                vpFragments[currLoadPosition]?.reLoadPage()
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

            })
            binding.viewPager2.setCurrentItem(
                Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % mRoomInfoList.size + selectedRoomIndex,
                false
            )
        } else {
            currLoadPosition = 0
        }
    }

    override fun finish() {
        vpFragments[currLoadPosition]?.stopLoadPage(false)
        VideoSetting.resetBroadcastSetting()
        VideoSetting.resetAudienceSetting()
        TokenGenerator.expireSecond = -1
        RtcEngineInstance.beautyProcessor.reset()
        RtcEngineInstance.cleanCache()
        super.finish()
    }
}
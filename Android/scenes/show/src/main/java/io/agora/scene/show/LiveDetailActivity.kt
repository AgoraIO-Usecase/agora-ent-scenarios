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
import io.agora.scene.base.manager.UserManager
import io.agora.scene.show.beauty.BeautyManager
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.utils.RunnableWithDenied
import io.agora.scene.show.videoLoaderAPI.AGSlicingType
import io.agora.scene.show.videoLoaderAPI.OnPageScrollEventHandler
import io.agora.scene.show.videoLoaderAPI.VideoLoader
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil


class LiveDetailActivity : BaseViewBindingActivity<ShowLiveDetailActivityBinding>(), LiveDetailFragment.OnMeLinkingListener {
    private val tag = "LiveDetailActivity"

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
    private var onPageScrollEventHandler: OnPageScrollEventHandler? = null

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
        return ShowLiveDetailActivityBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.viewPager2) { _: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.viewPager2.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val selectedRoomIndex = intent.getIntExtra(EXTRA_ROOM_DETAIL_INFO_LIST_SELECTED_INDEX, 0)

        onPageScrollEventHandler = object : OnPageScrollEventHandler(
            RtcEngineInstance.rtcEngine,
            UserManager.getInstance().user.id.toInt(),
            true,
            AGSlicingType.VISIABLE
        ) {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_SETTLING -> binding.viewPager2.isUserInputEnabled = false
                    ViewPager2.SCROLL_STATE_IDLE -> binding.viewPager2.isUserInputEnabled = true
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        // TODO 暂不支持
                    }
                }
                super.onPageScrollStateChanged(state)
            }

            override fun onPageStartLoading(position: Int) {
                Log.d(tag, "onPageLoad, position:$position")
                vpFragments[position]?.startLoadPageSafely()
            }

            override fun onPageLoaded(position: Int) {
                Log.d(tag, "onPageReLoad, position:$position")
                vpFragments[position]?.onPageLoaded()
            }

            override fun onPageLeft(position: Int) {
                Log.d(tag, "onPageHide, position:$position")
                vpFragments[position]?.stopLoadPage(true)
            }

            override fun onRequireRenderVideo(
                position: Int,
                info: VideoLoader.AnchorInfo
            ): VideoLoader.VideoCanvasContainer? {
                Log.d(tag, "onRequireRenderVideo, position:$position")
                return vpFragments[position]?.initAnchorVideoView(info)
            }
        }

        val list = ArrayList<VideoLoader.RoomInfo>()
        mRoomInfoList.forEach {
            val anchorList = arrayListOf(
                VideoLoader.AnchorInfo(
                    it.roomId,
                    it.ownerId.toInt(),
                    RtcEngineInstance.generalToken()
                )
            )
            list.add(
                VideoLoader.RoomInfo(it.roomId, anchorList)
            )
        }
        onPageScrollEventHandler?.updateRoomList(list)

        // 设置vp当前页面外的页面数
        binding.viewPager2.offscreenPageLimit = 1
        val fragmentAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = if (mScrollable) Int.MAX_VALUE else 1

            override fun createFragment(position: Int): Fragment {
                val roomInfo = if (mScrollable) {
                    mRoomInfoList[position % mRoomInfoList.size]
                } else {
                    mRoomInfoList[selectedRoomIndex]
                }
                return LiveDetailFragment.newInstance(
                    roomInfo,
                    onPageScrollEventHandler as OnPageScrollEventHandler, position
                ).apply {
                    Log.d(tag, "position：$position, room:${roomInfo.roomId}")
                    vpFragments.put(position, this)
                    if (roomInfo.ownerId != UserManager.getInstance().user.id.toString()) {
                        val anchorList = arrayListOf(
                            VideoLoader.AnchorInfo(
                                roomInfo.roomId,
                                roomInfo.ownerId.toInt(),
                                RtcEngineInstance.generalToken()
                            )
                        )
                        onPageScrollEventHandler?.onRoomCreated(position,
                            VideoLoader.RoomInfo(
                                roomInfo.roomId,
                                anchorList
                            ),position == binding.viewPager2.currentItem)
                    } else {
                        // 主播
                        startLoadPageSafely()
                    }
                }
            }
        }
        binding.viewPager2.adapter = fragmentAdapter
        binding.viewPager2.isUserInputEnabled = mScrollable
        if (mScrollable) {
            binding.viewPager2.registerOnPageChangeCallback(onPageScrollEventHandler as OnPageChangeCallback)
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
        RtcEngineInstance.cleanCache()
        RtcEngineInstance.resetVirtualBackground()
        BeautyManager.destroy()
        super.finish()
    }
}
package io.agora.scene.showTo1v1.ui

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.showTo1v1.CallRole
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.audioscenarioapi.AudioScenarioType
import io.agora.audioscenarioapi.SceneType
import io.agora.onetoone.*
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.RoomListFragment
import io.agora.scene.showTo1v1.ui.view.OnClickJackingListener
import io.agora.videoloaderapi.AGSlicingType
import io.agora.videoloaderapi.OnPageScrollEventHandler
import io.agora.videoloaderapi.VideoLoader
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.StatusBarUtil

/*
 * Live streaming room list activity
 */
class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>(),
    RoomListFragment.OnFragmentListener {

    companion object {
        private const val TAG = "ShowTo1v1_List"
        private const val kRoomListSwipeGuide = "showTo1v1_SwipeGuide"

        private const val POSITION_NONE = -1
    }

    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private val mService by lazy { mShowTo1v1Manger.mService }

    private var mRoomInfoList = mutableListOf<ShowTo1v1RoomInfo>()
    private val mRtcVideoLoaderApi by lazy { VideoLoader.getImplInstance(mRtcEngine) }

    private val mVpFragments = SparseArray<RoomListFragment>()
    private var mCurrLoadPosition = POSITION_NONE
    private var mLoadConnection = false

    // Current call state
    private var mCallState = CallStateType.Idle

    private var mCallDialog: CallDialog? = null

    // Current calling room
    private var mRoomInfo: ShowTo1v1RoomInfo? = null

    private var onPageScrollEventHandler: OnPageScrollEventHandler? = null

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomListActivityBinding {
        return ShowTo1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        binding.smartRefreshLayout.autoRefresh()
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

    override fun onRestart() {
        mVpFragments[mCurrLoadPosition]?.onResumePage()
        binding.smartRefreshLayout.autoRefresh()
        super.onRestart()
    }

    private var guided = SPUtil.getBoolean(kRoomListSwipeGuide, false)
    private fun mayShowGuideView() {
        if (mRoomInfoList.isEmpty() || guided) {
            return
        }
        binding.vGuidance.visibility = View.VISIBLE
        binding.vGuidance.setOnClickListener {
            SPUtil.putBoolean(kRoomListSwipeGuide, true)
            guided = true
            binding.vGuidance.visibility = View.GONE
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ShowTo1v1ServiceProtocol.ROOM_AVAILABLE_DURATION = SceneConfigManager.oneOnOneExpireTime * 1000L
        binding.titleView.setLeftClick { finish() }
        binding.btnCreateRoom.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                mShowTo1v1Manger.mCallApi.removeListener(callApiListener)
                mShowTo1v1Manger.deInitialize()
                RoomCreateActivity.launch(this@RoomListActivity)
            }
        })
        initOrUpdateViewPage()

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)
        binding.smartRefreshLayout.setOnRefreshListener {
            mShowTo1v1Manger.renewTokens {
                if (it) {
                    mShowTo1v1Manger.setup(this) { e ->
                        if (e == null) {
                            fetchRoomList()
                        } else {
                            CustomToast.show(getString(R.string.show_to1v1_room_list_refreshed, e.msg))
                            binding.smartRefreshLayout.finishRefresh()
                        }
                    }
                } else {
                    CustomToast.show(getString(R.string.show_to1v1_room_list_refreshed, "fetch token failed!"))
                    binding.smartRefreshLayout.finishRefresh()
                }
            }
        }
    }

    private fun initOrUpdateViewPage() {
        onPageScrollEventHandler = object : OnPageScrollEventHandler(
            mRtcEngine,
            UserManager.getInstance().user.id.toInt(),
            true,
            AGSlicingType.VISIBLE
        ) {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_SETTLING -> binding.viewPager2.isUserInputEnabled = false
                    ViewPager2.SCROLL_STATE_IDLE -> binding.viewPager2.isUserInputEnabled = true
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        // Not supported yet
                    }
                }
                super.onPageScrollStateChanged(state)
            }

            override fun onPageStartLoading(position: Int) {
                Log.d(TAG, "onPageLoad, position:$position")
                mVpFragments[position]?.startLoadPageSafely()
            }

            override fun onPageLoaded(position: Int) {
                Log.d(TAG, "onPageReLoad, position:$position")
                mVpFragments[position]?.onPageLoaded()
            }

            override fun onPageLeft(position: Int) {
                Log.d(TAG, "onPageHide, position:$position")
                mVpFragments[position]?.stopLoadPage(true)
            }

            override fun onRequireRenderVideo(
                position: Int,
                info: VideoLoader.AnchorInfo
            ): VideoLoader.VideoCanvasContainer? {
                Log.d(TAG, "onRequireRenderVideo, position:$position")
                return mVpFragments[position]?.initAnchorVideoView(info)
            }
        }
        onPageScrollEventHandler?.muteAudio()

        val list = ArrayList<VideoLoader.RoomInfo>()
        mRoomInfoList.forEach {
            val anchorList = arrayListOf(
                VideoLoader.AnchorInfo(
                    it.roomId,
                    it.userId.toInt(),
                    mShowTo1v1Manger.generalToken()
                )
            )
            list.add(
                VideoLoader.RoomInfo(it.roomId, anchorList)
            )
        }
        onPageScrollEventHandler?.updateRoomList(list)

        // Set the number of pages outside the current page of vp
        binding.viewPager2.offscreenPageLimit = 1
        val fragmentAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = mRoomInfoList.size

            override fun createFragment(position: Int): Fragment {
                val roomInfo = mRoomInfoList[position % mRoomInfoList.size]
                return RoomListFragment.newInstance(
                    roomInfo,
                    onPageScrollEventHandler as OnPageScrollEventHandler, position
                ).apply {
                    Log.d(TAG, "position：$position, room:${roomInfo.roomId}")
                    mVpFragments.put(position, this)
                    if (roomInfo.userId != UserManager.getInstance().user.id.toString()) {
                        val anchorList = arrayListOf(
                            VideoLoader.AnchorInfo(
                                roomInfo.roomId,
                                roomInfo.userId.toInt(),
                                mShowTo1v1Manger.generalToken()
                            )
                        )
                        onPageScrollEventHandler?.onRoomCreated(position,
                            VideoLoader.RoomInfo(
                                roomInfo.roomId,
                                anchorList
                            ),position == this@RoomListActivity.binding.viewPager2.currentItem)
                    } else {
                        // Host
                        startLoadPageSafely()
                    }
                }
            }

            override fun getItemId(position: Int): Long {
                // Prevent fragment from not refreshing if changed
                val roomInfo = mRoomInfoList[position % mRoomInfoList.size]
                return (roomInfo.roomId.hashCode() + position).toLong()
            }
        }
        binding.viewPager2.adapter = fragmentAdapter
        binding.viewPager2.registerOnPageChangeCallback(onPageScrollEventHandler as OnPageChangeCallback)
        binding.viewPager2.setCurrentItem(0, false)
        mCurrLoadPosition = binding.viewPager2.currentItem
    }

    private fun fetchRoomList() {
        mService?.getRoomList(completion = { error, roomList ->
            if (error != null) {
                CustomToast.show(getString(R.string.show_to1v1_room_list_refreshed, error.message))
                binding.smartRefreshLayout.finishRefresh()
                return@getRoomList
            }
            mRoomInfoList.clear()
            mRoomInfoList.addAll(roomList)
            updateListView()
            resetViewpage()
            initOrUpdateViewPage()

            mayShowGuideView()
            if (roomList.isNotEmpty()) {
                binding.viewPager2.setCurrentItem(0, false)
            }
            binding.smartRefreshLayout.finishRefresh()
        })
    }

    private fun resetViewpage(){
        mVpFragments.clear()
        mCurrLoadPosition = POSITION_NONE
        mLoadConnection = false
        if (onPageScrollEventHandler != null) {
            binding.viewPager2.unregisterOnPageChangeCallback(onPageScrollEventHandler as OnPageChangeCallback)
            onPageScrollEventHandler = null
        }
    }

    private fun updateListView() {
        if (mRoomInfoList.isEmpty()) {
            StatusBarUtil.hideStatusBar(window, true)
            binding.emptyInclude.root.isVisible = true
            binding.viewPager2.isVisible = false
        } else {
            StatusBarUtil.hideStatusBar(window, false)
            binding.emptyInclude.root.isVisible = false
            binding.viewPager2.isVisible = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVpFragments[mCurrLoadPosition]?.stopLoadPage(false)
        mShowTo1v1Manger.mCallApi.removeListener(callApiListener)
        mRtcVideoLoaderApi.cleanCache()
        VideoLoader.release()
        mShowTo1v1Manger.destroy()
        mService?.reset()
    }


    private var toggleVideoRun: Runnable? = null

    override fun getPermissions() {
        toggleVideoRun?.let {
            it.run()
            toggleVideoRun = null
        }
    }

    private fun toggleSelfVideo(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleVideoRun = Runnable { callback.invoke() }
            requestCameraPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun onFragmentClickCall(needCall: Boolean, roomInfo: ShowTo1v1RoomInfo) {

        mRoomInfo = roomInfo
        if (needCall) {
            toggleSelfVideo(true) {
                mShowTo1v1Manger.prepareCall(CallRole.CALLER, roomInfo.roomId, callback = {
                    if (it) {
                        mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                        mShowTo1v1Manger.mCallApi.call(roomInfo.getIntUserId(), completion = { error ->
                            if (error != null && mCallState == CallStateType.Calling) {
                                Toast.makeText(this, getString(R.string.show_to1v1_call_failed, error.code.toString()), Toast.LENGTH_SHORT).show()
                                // Call failed immediately, hang up
                                mShowTo1v1Manger.mCallApi.cancelCall {  }
                                mCallDialog?.let {
                                    if (it.isShowing) it.dismiss()
                                    mCallDialog = null
                                }
                            }
                        })
                    } else {
                        // Failed state needs to release resources and reinit
                        mShowTo1v1Manger.deInitialize()
                    }
                })
            }
        } else {
            mShowTo1v1Manger.mCallApi.removeListener(callApiListener)
            RoomDetailActivity.launch(this, false, roomInfo)
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { }) { launchAppSetting(permission) }
    }

    override fun onFragmentViewCreated() {
        if (mLoadConnection) return
        mLoadConnection = true
        mVpFragments[mCurrLoadPosition]?.startLoadPageSafely()
    }

    private fun onCallSend(user: ShowTo1v1UserInfo) {
        val dialog = CallSendDialog(this, user)
        dialog.setListener(object : CallSendDialog.CallSendDialogListener {
            override fun onSendViewDidClickHangup() {
                mShowTo1v1Manger.mCallApi.cancelCall(null)
            }
        })
        dialog.show()
        mCallDialog = dialog
    }

    private val callApiListener = object : ICallApiListener {

        override fun callDebugInfo(message: String, logLevel: CallLogLevel) {
            super.callDebugInfo(message, logLevel)
            when (logLevel) {
                CallLogLevel.Normal, CallLogLevel.Warning -> ShowTo1v1Logger.d(RoomDetailActivity.TAG, "callDebugInfo $message")
                CallLogLevel.Error -> ShowTo1v1Logger.e(RoomDetailActivity.TAG, null, "callDebugInfo $message")
            }
        }

        override fun tokenPrivilegeWillExpire() {
            super.tokenPrivilegeWillExpire()
            mShowTo1v1Manger.renewTokens {}
        }

        override fun onCallError(
            errorEvent: CallErrorEvent,
            errorType: CallErrorCodeType,
            errorCode: Int,
            message: String?
        ) {
            super.onCallError(errorEvent, errorType, errorCode, message)
            ShowTo1v1Logger.e(TAG, Exception(message),"onCallError: errorEvent$errorEvent, errorType:$errorType,errorCode:$errorCode")
        }

        override fun canJoinRtcOnCalling(eventInfo: Map<String, Any>): Boolean {
            return true
        }

        override fun onCallStateChanged(
            state: CallStateType,
            stateReason: CallStateReason,
            eventReason: String,
            eventInfo: Map<String, Any>
        ) {
            val publisher = eventInfo[CallApiImpl.kPublisher] ?: mShowTo1v1Manger.mCurrentUser.userId
            if (publisher != mShowTo1v1Manger.mCurrentUser.userId) return
            mCallState = state
            ShowTo1v1Logger.d(TAG, "RooList onCallStateChanged state:${state.name},stateReason:${stateReason.name},eventReason:${eventReason}")
            when (state) {
                CallStateType.Prepared -> {
                    if (stateReason == CallStateReason.CallingTimeout || stateReason == CallStateReason.RemoteRejected) {
                        mShowTo1v1Manger.mRemoteUser = null
                        CustomToast.show(getString(R.string.show_to1v1_no_answer))
                        mCallDialog?.let {
                            if (it.isShowing) it.dismiss()
                            mCallDialog = null
                        }
                    } else if (stateReason == CallStateReason.RemoteCallBusy) {
                        CustomToast.show(getString(R.string.show_to1v1_call_toast_remote_busy))
                        mCallDialog?.let {
                            if (it.isShowing) it.dismiss()
                            mCallDialog = null
                        }
                    } else if ((stateReason == CallStateReason.LocalHangup || stateReason == CallStateReason.RemoteHangup) && mShowTo1v1Manger.isCaller) {
                        fetchRoomList()
                    }
                }

                CallStateType.Calling -> {
                    val fromUserId = eventInfo[CallApiImpl.kFromUserId] as? Int ?: 0
                    val fromRoomId = eventInfo[CallApiImpl.kFromRoomId] as? String ?: ""
                    val toUserId = eventInfo[CallApiImpl.kRemoteUserId] as? Int ?: 0
                    // Process only if the triggering user is yourself
                    if (mShowTo1v1Manger.mCurrentUser.userId == toUserId.toString()) {
                        // Received caller
                        mShowTo1v1Manger.isCaller = false
                    } else if (mShowTo1v1Manger.mCurrentUser.userId == fromUserId.toString()) {
                        // Caller
                        mShowTo1v1Manger.isCaller = true
                        mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                        val remoteUser = mRoomInfoList.firstOrNull {
                            it.userId == toUserId.toString()
                        } ?: return
                        mShowTo1v1Manger.mRemoteUser = remoteUser
                        onCallSend(remoteUser)
                    }

                    mShowTo1v1Manger.mRtcEngine.setVideoEncoderConfigurationEx(
                        VideoEncoderConfiguration().apply {
                            dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1280)
                            frameRate = 24
                            degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
                        },
                        RtcConnection(mShowTo1v1Manger.mConnectedChannelId, mShowTo1v1Manger.mCurrentUser.userId.toInt())
                    )
                }

                CallStateType.Connecting -> {
                    if (stateReason == CallStateReason.LocalAccepted || stateReason == CallStateReason.RemoteAccepted) {
                        Log.d(TAG,"call Connecting LocalAccepted or RemoteAccepted")
                    }
                }

                CallStateType.Connected -> {
                    mCallDialog?.let {
                        if (it.isShowing) it.dismiss()
                        mCallDialog = null
                    }
                    mRoomInfo?.let { roomInfo ->
                        mShowTo1v1Manger.mCallApi.removeListener(this)
                        RoomDetailActivity.launch(this@RoomListActivity, true, roomInfo)
                    }

                    // Set audio best practice
                    if (mShowTo1v1Manger.isCaller) {
                        // Caller
                        mShowTo1v1Manger.scenarioApi.setAudioScenario(SceneType.Chat, AudioScenarioType.Chat_Caller)
                    } else {
                        // Called
                        mShowTo1v1Manger.scenarioApi.setAudioScenario(SceneType.Chat, AudioScenarioType.Chat_Callee)
                    }
                }

                CallStateType.Failed -> {
                    mCallDialog?.let {
                        if (it.isShowing) it.dismiss()
                        mCallDialog = null
                    }
                    mShowTo1v1Manger.mRemoteUser = null
                    CustomToast.show(eventReason)
                }
                else -> {}
            }
        }

    }
}
package io.agora.scene.showTo1v1.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.rtm.RtmClient
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.callAPI.CallApiImpl
import io.agora.scene.showTo1v1.callAPI.CallReason
import io.agora.scene.showTo1v1.callAPI.CallRole
import io.agora.scene.showTo1v1.callAPI.CallStateType
import io.agora.scene.showTo1v1.callAPI.ICallApi
import io.agora.scene.showTo1v1.callAPI.ICallApiListener
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialogState
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.RoomListFragment
import io.agora.scene.showTo1v1.ui.view.OnClickJackingListener
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>(),
    RoomListFragment.OnFragmentListener {

    companion object {
        private const val TAG = "ShowTo1v1_List"
        private const val kRoomListSwipeGuide = "showTo1v1_SwipeGuide"

        private const val POSITION_NONE = -1
    }

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }
    private val mCallApi by lazy { ICallApi.getImplInstance() }
    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }

    private var mFragmentAdapter: FragmentStateAdapter? = null
    private var mRoomInfoList = mutableListOf<ShowTo1v1RoomInfo>()

    private val mVpFragments = SparseArray<RoomListFragment>()
    private var mCurrLoadPosition = POSITION_NONE
    private var mLoadConnection = false

    // 当前呼叫状态
    private var mCallState = CallStateType.Idle

    private var mCallDialog: CallDialog? = null

    // 当前呼叫的房间
    private var mRoomInfo: ShowTo1v1RoomInfo? = null

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomListActivityBinding {
        return ShowTo1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        mShowTo1v1Manger.renewTokens {
            if (it) {
                fetchRoomList()
            }
        }
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

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRestart() {
        mVpFragments[mCurrLoadPosition]?.onResumePage()
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
        binding.titleView.setLeftClick { finish() }
        binding.titleView.setRightIconClick {
            mShowTo1v1Manger.renewTokens {
                if (it) {
                    fetchRoomList()
                }
            }
        }
        binding.emptyInclude.layoutCreateRoom.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                mCallApi.removeListener(callApiListener)
                mShowTo1v1Manger.deInitialize()
                RoomCreateActivity.launch(this@RoomListActivity)
            }
        })
        binding.layoutCreateRoom2.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                mCallApi.removeListener(callApiListener)
                mShowTo1v1Manger.deInitialize()
                RoomCreateActivity.launch(this@RoomListActivity)
            }
        })
        initOrUpdateViewPage()
    }

    private fun initOrUpdateViewPage() {
        if (mRoomInfoList.size > 0) {
            // 设置预加载
            val preloadCount = 3
            binding.viewPager2.offscreenPageLimit = preloadCount - 2
            mFragmentAdapter = object : FragmentStateAdapter(this) {

                override fun getItemCount(): Int {
                    return if (mRoomInfoList.size <= 1) mRoomInfoList.size else Int.MAX_VALUE
                }

                override fun createFragment(position: Int): Fragment {
                    val roomInfo = mRoomInfoList[position % mRoomInfoList.size]
                    return RoomListFragment.newInstance(roomInfo).also {
                        mVpFragments[position] = it
                    }
                }

                override fun getItemId(position: Int): Long {
                    // 防止 fragment 变了不刷新
                    val roomInfo = mRoomInfoList[position % mRoomInfoList.size]
                    return (roomInfo.roomId.hashCode() + position).toLong()

                }
            }
            binding.viewPager2.adapter = mFragmentAdapter
            binding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
            binding.viewPager2.setCurrentItem(Int.MAX_VALUE / 2, false)
            mCurrLoadPosition = binding.viewPager2.currentItem
        } else {
//            mFragmentAdapter?.let {
//                it.notifyDataSetChanged()
//                binding.viewPager2.setCurrentItem(Int.MAX_VALUE / 2, false)
//                mCurrLoadPosition = binding.viewPager2.currentItem
//                binding.viewPager2.postDelayed({
//                    mVpFragments[mCurrLoadPosition]?.onResetPage()
//                }, 500)
//            }
        }
    }

    private val onPageChangeCallback = object : OnPageChangeCallback() {

        private val PRE_LOAD_OFFSET = 0.01f
        private var preLoadPosition = POSITION_NONE
        private var lastOffset = 0f
        private var scrollStatus: Int = ViewPager2.SCROLL_STATE_IDLE

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            when (state) {
                ViewPager2.SCROLL_STATE_SETTLING -> binding.viewPager2.isUserInputEnabled = false
                ViewPager2.SCROLL_STATE_IDLE -> {
                    binding.viewPager2.isUserInputEnabled = true
                    if (preLoadPosition != POSITION_NONE) {
                        mVpFragments[preLoadPosition]?.stopLoadPage(true)
                    }
                    mVpFragments[mCurrLoadPosition]?.onReloadPage()
                    preLoadPosition = POSITION_NONE
                    lastOffset = 0f
                }

                else -> {
                    // nothing
                }
            }
            scrollStatus = state
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            if (scrollStatus == ViewPager2.SCROLL_STATE_DRAGGING) {
                if (lastOffset > 0f) {
                    val isMoveUp = (positionOffset - lastOffset) > 0
                    if (isMoveUp && positionOffset >= PRE_LOAD_OFFSET && preLoadPosition == POSITION_NONE) {
                        preLoadPosition = mCurrLoadPosition + 1
                        mVpFragments[preLoadPosition]?.startLoadPageSafely()
                    } else if (!isMoveUp && positionOffset <= (1 - PRE_LOAD_OFFSET) && preLoadPosition == POSITION_NONE) {
                        preLoadPosition = mCurrLoadPosition - 1
                        mVpFragments[preLoadPosition]?.startLoadPageSafely()
                    }
                }
                lastOffset = positionOffset
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (mCurrLoadPosition != POSITION_NONE) {
                if (preLoadPosition != POSITION_NONE) {
                    if (position == preLoadPosition) {
                        mVpFragments[mCurrLoadPosition]?.stopLoadPage(true)
                    } else {
                        mVpFragments[preLoadPosition]?.stopLoadPage(true)
                        mVpFragments[mCurrLoadPosition]?.onReloadPage()
                    }
                } else if (mCurrLoadPosition != position) {
                    mVpFragments[mCurrLoadPosition]?.stopLoadPage(true)
                    mVpFragments[position]?.startLoadPageSafely()
                }
            }
            mCurrLoadPosition = position
            preLoadPosition = POSITION_NONE
            lastOffset = 0f
        }
    }

    private fun fetchRoomList() {
        animateLoadingIcon()
        binding.titleView.rightIcon.isEnabled = false

        mService.getRoomList(completion = { error, roomList ->
            mRoomInfoList.clear()
            mRoomInfoList.addAll(roomList)
            updateListView()
            resetViewpage()
            initOrUpdateViewPage()
            ToastUtils.showToast(R.string.show_to1v1_room_list_refreshed)
            binding.root.postDelayed({
                binding.titleView.rightIcon.isEnabled = true
                rotateAnimator?.cancel()
            }, 500)
            mayShowGuideView()
        })
    }

    private fun resetViewpage(){
        mVpFragments.clear()
        mCurrLoadPosition = POSITION_NONE
        mLoadConnection = false
        binding.viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    private fun updateListView() {
        if (mRoomInfoList.isEmpty()) {
            StatusBarUtil.hideStatusBar(window, true)
            binding.emptyInclude.root.isVisible = true
            binding.viewPager2.isVisible = false
            binding.layoutCreateRoom2.isVisible = false
        } else {
            StatusBarUtil.hideStatusBar(window, false)
            binding.emptyInclude.root.isVisible = false
            binding.viewPager2.isVisible = true
            binding.layoutCreateRoom2.isVisible = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVpFragments[mCurrLoadPosition]?.stopLoadPage(false)
        mCallApi.removeListener(callApiListener)
        mShowTo1v1Manger.destroy()
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
                mShowTo1v1Manger.reInitCallApi(CallRole.CALLER, roomInfo.roomId, callback = {
                    mCallApi.addListener(callApiListener)
                    mCallApi.call(roomInfo.roomId, roomInfo.getIntUserId(), completion = {
                        if (it != null) {
                            mCallApi.removeListener(callApiListener)
                            mShowTo1v1Manger.deInitialize()
                        }
                    })
                })
            }
        } else {
            mCallApi.removeListener(callApiListener)
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
                mCallApi.cancelCall(null)
            }
        })
        dialog.show()
        mCallDialog = dialog
    }

    private val callApiListener = object : ICallApiListener {

        override fun tokenPrivilegeWillExpire() {
            super.tokenPrivilegeWillExpire()
            mShowTo1v1Manger.renewTokens {}
        }

        override fun onCallStateChanged(
            state: CallStateType,
            stateReason: CallReason,
            eventReason: String,
            elapsed: Long,
            eventInfo: Map<String, Any>
        ) {
            val publisher = eventInfo[CallApiImpl.kPublisher] ?: mShowTo1v1Manger.mCurrentUser.userId
            if (publisher != mShowTo1v1Manger.mCurrentUser.userId) return
            mCallState = state
            Log.d(TAG, "RooList state:${state.name},stateReason:${stateReason.name},eventReason:${eventReason}")
            when (state) {
                CallStateType.Prepared -> {
                    if (stateReason == CallReason.CallingTimeout || stateReason == CallReason.RemoteRejected) {
                        mShowTo1v1Manger.mRemoteUser = null
                        ToastUtils.showToast(getString(R.string.show_to1v1_no_answer))
                        mCallDialog?.let {
                            if (it.isShowing) it.dismiss()
                            mCallDialog = null
                        }
                    }
                }

                CallStateType.Calling -> {
                    val fromUserId = eventInfo[CallApiImpl.kFromUserId] as? Int ?: 0
                    val fromRoomId = eventInfo[CallApiImpl.kFromRoomId] as? String ?: ""
                    val toUserId = eventInfo[CallApiImpl.kRemoteUserId] as? Int ?: 0
                    // 触发状态的用户是自己才处理
                    if (mShowTo1v1Manger.mCurrentUser.userId == toUserId.toString()) {
                        // 收到大哥拨打电话
                        // nothing
                    } else if (mShowTo1v1Manger.mCurrentUser.userId == fromUserId.toString()) {
                        // 大哥拨打电话
                        mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                        val remoteUser = mRoomInfoList.firstOrNull {
                            it.userId == toUserId.toString()
                        } ?: return
                        mShowTo1v1Manger.mRemoteUser = remoteUser
                        onCallSend(remoteUser)
                    }
                }

                CallStateType.Connecting ->{
                    if (stateReason==CallReason.LocalAccepted||stateReason==CallReason.RemoteAccepted){
                        Log.d(TAG,"call Connecting LocalAccepted or RemoteAccepted")
                    }
                }
                CallStateType.Connected -> {
                    mCallDialog?.let {
                        if (it.isShowing) it.dismiss()
                        mCallDialog = null
                    }
                    mRoomInfo?.let { roomInfo ->
                        mCallApi.removeListener(this)
                        RoomDetailActivity.launch(this@RoomListActivity, true, roomInfo)
                    }
                }

                CallStateType.Failed -> {
                    mCallDialog?.let {
                        if (it.isShowing) it.dismiss()
                        mCallDialog = null
                    }
                    mShowTo1v1Manger.mRemoteUser = null
                    ToastUtils.showToast(eventReason)
                }
            }
        }

    }

    private fun createRotateAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(binding.titleView.rightIcon, View.ROTATION, 0f, 360f).apply {
            duration = 1200
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }
    }

    private var rotateAnimator: Animator? = null

    private fun animateLoadingIcon() {
        if (rotateAnimator?.isRunning == true) return // 判断动画是否正在运行
        rotateAnimator?.cancel() // 停止之前的动画
        rotateAnimator = createRotateAnimator().apply {
            start()
        }
    }
}
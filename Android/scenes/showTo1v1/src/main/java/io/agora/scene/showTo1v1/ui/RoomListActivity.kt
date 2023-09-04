package io.agora.scene.showTo1v1.ui

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.agora.scene.showTo1v1.callAPI.CallApiImpl
import io.agora.scene.showTo1v1.callAPI.CallConfig
import io.agora.scene.showTo1v1.callAPI.CallMode
import io.agora.scene.showTo1v1.callAPI.CallReason
import io.agora.scene.showTo1v1.callAPI.CallRole
import io.agora.scene.showTo1v1.callAPI.CallStateType
import io.agora.scene.showTo1v1.callAPI.ICallApi
import io.agora.scene.showTo1v1.callAPI.ICallApiListener
import io.agora.scene.showTo1v1.callAPI.PrepareConfig
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialogState
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.RoomListFragment
import io.agora.scene.widget.utils.StatusBarUtil
import org.json.JSONException
import org.json.JSONObject

class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>(), ICallApiListener,
    RoomListFragment.OnClickCallingListener {

    companion object {
        private const val TAG = "RoomListActivity"
        private const val kRoomListSwipeGuide = "showTo1v1_SwipeGuide"

        private const val POSITION_NONE = -1
    }

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }
    private val mCallApi by lazy { ICallApi.getImplInstance() }
    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private var mFragmentAdapter: FragmentStateAdapter? = null
    private val mRoomInfoList = mutableListOf<ShowTo1v1RoomInfo>()

    private val mVpFragments = SparseArray<RoomListFragment>()
    private var mCurrLoadPosition = POSITION_NONE

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
            fetchRoomList()
        }
        binding.emptyInclude.layoutCreateRoom.setOnClickListener {
            mCallApi.removeListener(this)
            mCallApi.deinitialize { }
            RoomCreateActivity.launch(this)
        }
    }

    private fun initViewPage() {
        if (mFragmentAdapter == null) {
            // 设置预加载
            val preloadCount = 3
            binding.viewPager2.offscreenPageLimit = preloadCount - 2
            mFragmentAdapter = object : FragmentStateAdapter(this) {
                override fun getItemCount() = Int.MAX_VALUE

                override fun createFragment(position: Int): Fragment {
                    val roomInfo = mRoomInfoList[position % mRoomInfoList.size]

                    return RoomListFragment.newInstance(roomInfo).also {
                        mVpFragments.put(position, it)
                        if (position == binding.viewPager2.currentItem) {
                            it.startLoadPageSafely()
                        }
                    }
                }
            }
            binding.viewPager2.adapter = mFragmentAdapter
            binding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
            binding.viewPager2.setCurrentItem(Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % mRoomInfoList.size, false)
        } else {
            mFragmentAdapter?.notifyDataSetChanged()
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
                            mVpFragments[preLoadPosition]?.stopLoadPage(true)
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
            Log.d(
                TAG,
                "PageChange onPageSelected position=$position, currLoadPosition=$mCurrLoadPosition, preLoadPosition=$preLoadPosition"
            )
            if (mCurrLoadPosition != POSITION_NONE) {
                if (preLoadPosition != POSITION_NONE) {
                    if (position == preLoadPosition) {
                        mVpFragments[mCurrLoadPosition]?.stopLoadPage(true)
                    } else {
                        mVpFragments[preLoadPosition]?.stopLoadPage(true)
                    }
                } else if (mCurrLoadPosition != position) {
                    mVpFragments[mCurrLoadPosition]?.stopLoadPage(true)
                    mVpFragments[position]?.startLoadPageSafely()
                }
            }
            mCurrLoadPosition = position
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
                mShowTo1v1Manger.setupGeneralToken(it)
                ShowTo1v1Logger.d(TAG, "generateToken success：$it， uid：$localUId")
                fetchRoomList()
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
                mVpFragments[mCurrLoadPosition]?.stopLoadPage(false)
                updateListView()
                initViewPage()
                ToastUtils.showToast(R.string.show_to1v1_room_list_refreshed)
            } else {
                updateListView()
            }
            mayShowGuideView()

        })
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
        mCallApi.removeListener(this)
        mShowTo1v1Manger.cleanCache()
    }

    private fun reInitCallApi(roomId: String, callback: () -> Unit) {
        mShowTo1v1Manger.reInitCallApi(CallRole.CALLER, roomId, callback = {
            callback.invoke()
            mCallApi.addListener(this)
        })
    }

    override fun onClickCall(roomInfo: ShowTo1v1RoomInfo) {
        mRoomInfo = roomInfo
        reInitCallApi(roomInfo.roomId, callback = {
            mCallApi.call(roomInfo.get1v1ChannelId(), roomInfo.getIntUserId(), null)
        })
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

    override fun onCallStateChanged(
        state: CallStateType, stateReason: CallReason, eventReason: String, elapsed: Long, eventInfo: Map<String, Any>
    ) {
        val publisher = eventInfo[CallApiImpl.kPublisher] ?: mShowTo1v1Manger.mCurrentUser.userId
        if (publisher != mShowTo1v1Manger.mCurrentUser.userId) return
        mCallState = state
        when (state) {
            CallStateType.Prepared -> {
                if (stateReason == CallReason.CallingTimeout || stateReason == CallReason.LocalRejected ||
                    stateReason == CallReason.RemoteRejected
                ) {
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
                if (mShowTo1v1Manger.mRemoteUser != null && mShowTo1v1Manger.mRemoteUser!!.userId != fromUserId.toString()) {
                    mCallApi.reject(fromRoomId, fromUserId, "already calling") { err ->
                    }
                    return
                }
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

            CallStateType.Connecting -> mCallDialog?.updateCallState(CallDialogState.Connecting)
            CallStateType.Connected -> {
                if (mShowTo1v1Manger.mRemoteUser == null) return
                mCallDialog?.let {
                    if (it.isShowing) it.dismiss()
                    mCallDialog = null
                }
                mRoomInfo?.let { roomInfo ->
                    RoomDetailActivity.launch(this, roomInfo)
                    // 开启鉴黄鉴暴
                    val channelId = mShowTo1v1Manger.mRemoteUser?.get1v1ChannelId() ?: ""
                    val localUid = mShowTo1v1Manger.mCurrentUser.userId.toInt()
                    enableContentInspectEx(RtcConnection(channelId, localUid))
                    val channelName = mShowTo1v1Manger.mConnectedChannelId ?: return
                    val uid = mShowTo1v1Manger.mCurrentUser.userId.toLong()
                    AudioModeration.moderationAudio(
                        channelName, uid, AudioModeration.AgoraChannelType.broadcast, "ShowTo1v1"
                    )
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

    private fun enableContentInspectEx(connection: RtcConnection) {
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "ShowTo1v1")
            jsonObject.put("id", UserManager.getInstance().user.id)
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectConfig.ContentInspectModule()
            module.interval = 30
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            val ret = mRtcEngine.enableContentInspectEx(true, contentInspectConfig, connection)
            Log.d(TAG, "enableContentInspectEx $ret")
        } catch (_: JSONException) {
        }
    }
}
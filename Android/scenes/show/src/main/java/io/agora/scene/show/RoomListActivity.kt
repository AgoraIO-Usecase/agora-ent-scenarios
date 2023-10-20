package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.GlideApp
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowRoomListActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.videoSwitcherAPI.VideoSwitcher
import io.agora.scene.show.widget.PresetAudienceDialog
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : AppCompatActivity() {

    private val mBinding by lazy { ShowRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private var mAdapter: RoomListAdapter? = null
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mRtcVideoSwitcher by lazy { VideoSwitcher.getImplInstance(mRtcEngine) }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private val roomDetailModelList = mutableListOf<ShowRoomDetailModel>()

    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, true)
        setContentView(mBinding.root)
        //启动机器人
        mService.startCloudPlayer()
        //获取万能token
        fetchUniversalToken ({
            preloadChannels()
        })
        initView()
        initVideoSettings()
    }

    private fun initView() {
        mBinding.titleView.setLeftClick { finish() }
        mBinding.titleView.setRightIconClick {
            showAudienceSetting()
        }
        mAdapter = RoomListAdapter(null, this) { roomInfo, view, position, event ->
            val rtcConnection =
                RtcConnection(roomInfo.roomId, UserManager.getInstance().user.id.toInt())
            val isRoomOwner = roomInfo.ownerId == UserManager.getInstance().user.id.toString()
            if (isRoomOwner) {
                if (event!!.action == MotionEvent.ACTION_UP) {
                    ToastUtils.showToast(R.string.show_broadcaster_bad_exit)
                }
            } else {
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mRtcVideoSwitcher.preloadConnections(roomDetailModelList.map {
                            RtcConnection(
                                it.roomId,
                                UserManager.getInstance().user.id.toInt()
                            )
                        })
                        if (RtcEngineInstance.generalToken() == "") {
                            fetchUniversalToken({
                            }, {
                                ToastUtils.showToast("Fetch Token Failed")
                            })
                        } else {
                            if (mRtcEngine.queryDeviceScore() < 75) {
                                mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
                                mRtcEngine.setParameters("{\"rtc.video.decoder_out_byte_frame\": true}")
                            }
                            val channelMediaOptions = ChannelMediaOptions()
                            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            channelMediaOptions.autoSubscribeVideo = true
                            channelMediaOptions.autoSubscribeAudio = true
                            channelMediaOptions.publishCameraTrack = false
                            channelMediaOptions.publishMicrophoneTrack = false
                            // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            mRtcVideoSwitcher.joinChannel(
                                rtcConnection,
                                channelMediaOptions,
                                RtcEngineInstance.generalToken(),
                                null,
                                true
                            )
                            mRtcVideoSwitcher.preJoinChannel(rtcConnection)
                            mRtcEngine.adjustUserPlaybackSignalVolumeEx(roomInfo.ownerId.toInt(), 0, rtcConnection)
                            mService.startCloudPlayer()
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        mRtcVideoSwitcher.leaveChannel(rtcConnection, true)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (RtcEngineInstance.generalToken() != "") {
                            goLiveDetailActivity(roomDetailModelList, position, roomInfo)
                        }
                    }
                }
            }
        }
        mBinding.rvRooms.adapter = mAdapter
        mBinding.rvRooms.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 停止状态
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition() // 第一个可见 item
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()  // 最后一个可见 item
                    Log.d("RoomListActivity", "firstVisible $firstVisibleItem, lastVisible $lastVisibleItem")
                    val firstPreloadPosition = if (firstVisibleItem - 7 < 0) 0 else firstVisibleItem - 7
                    val lastPreloadPosition = if (firstPreloadPosition + 19 >= roomDetailModelList.size)
                        roomDetailModelList.size - 1 else firstPreloadPosition + 19
                    preloadChannels(firstPreloadPosition, lastPreloadPosition)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }
        })

        mBinding.smartRefreshLayout.setEnableLoadMore(false)
        mBinding.smartRefreshLayout.setEnableRefresh(true)
        mBinding.smartRefreshLayout.setOnRefreshListener {
            mService.getRoomList(
                success = {
                    roomDetailModelList.clear()
                    roomDetailModelList.addAll(it)
                    if (isFirstLoad) {
                        preloadChannels()
                        isFirstLoad = false
                    }
                    updateList(it)
                },
                error = {
                    updateList(emptyList())
                }
            )
        }
        mBinding.smartRefreshLayout.autoRefresh()
        mBinding.btnCreateRoom.setOnClickListener { goLivePrepareActivity() }
    }

    private fun updateList(data: List<ShowRoomDetailModel>) {
        mBinding.tvTips1.isVisible = data.isEmpty()
        mBinding.ivBgMobile.isVisible = data.isEmpty()
        mBinding.btnCreateRoom.isVisible = data.isNotEmpty()
        mBinding.rvRooms.isVisible = data.isNotEmpty()
        mAdapter?.setDataList(data)

        mBinding.smartRefreshLayout.finishRefresh()

        // 设置预加载
        val preloadCount = 3
        mRtcVideoSwitcher.setPreloadCount(preloadCount)
        mRtcVideoSwitcher.preloadConnections(data.map {
            RtcConnection(
                it.roomId,
                UserManager.getInstance().user.id.toInt()
            )
        })
    }

    private fun goLivePrepareActivity() {
        Intent(this, LivePrepareActivity::class.java).let {
            startActivity(it)
        }
    }

    private fun goLiveDetailActivity(list: List<ShowRoomDetailModel>, position: Int, roomInfo: ShowRoomDetailModel) {
        // 进房前设置一些必要的设置
        LiveDetailActivity.launch(
            this,
            ArrayList(list),
            position,
            roomInfo.ownerId != UserManager.getInstance().user.id.toString()
        )
    }

    private fun showAudienceSetting() {
        PresetAudienceDialog(this).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mService.destroy()
        mRtcVideoSwitcher.unloadConnections()
        VideoSwitcher.release()
        RtcEngineInstance.destroy()
        RtcEngineInstance.setupGeneralToken("")
    }

    // 获取万能token
    private fun fetchUniversalToken(
        success: () -> Unit,
        error: ((Exception?) -> Unit)? = null
    ) {
        val localUId = UserManager.getInstance().user.id
        TokenGenerator.generateToken("", localUId.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            TokenGenerator.AgoraTokenType.rtc,
            success = {
                ShowLogger.d("RoomListActivity", "generateToken success：$it， uid：$localUId")
                RtcEngineInstance.setupGeneralToken(it)
                success.invoke()
            },
            failure = {
                ShowLogger.e("RoomListActivity", it, "generateToken failure：$it")
                ToastUtils.showToast(it?.message ?: "generate token failure")
                error?.invoke(it)
            })
    }

    private fun preloadChannels() {
        val generalToken = RtcEngineInstance.generalToken()
        if (roomDetailModelList.isNotEmpty() && generalToken.isNotEmpty()) {
            // sdk 最多 preload 20个频道，超过 20 个，sdk 内部维护最新的 20 个频道预加载
            roomDetailModelList.take(20).forEach { room ->
                val ret = RtcEngineInstance.rtcEngine.preloadChannel(
                    generalToken, room.roomId, UserManager.getInstance().user.id.toInt()
                )
                Log.d("RoomListActivity", "call rtc sdk preloadChannel ${room.roomId} ret:$ret")
            }
        }
    }

    private fun preloadChannels(from: Int, to: Int) {
        val generalToken = RtcEngineInstance.generalToken()
        if (roomDetailModelList.isNotEmpty() && generalToken.isNotEmpty()) {
            val size = roomDetailModelList.size
            for (i in from until to + 1) {
                if (i >= size) return
                val room = roomDetailModelList[i]
                val ret = RtcEngineInstance.rtcEngine.preloadChannel(
                    generalToken, room.roomId, UserManager.getInstance().user.id.toInt()
                )
                Log.d("RoomListActivity", "call rtc sdk preloadChannel ${room.roomId} ret:$ret")
            }
        }
    }

    private fun initVideoSettings() {
        val deviceScore = RtcEngineInstance.rtcEngine.queryDeviceScore()
        val deviceLevel = if (deviceScore >= 90) {
            VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_AUTO)
            VideoSetting.setCurrAudienceEnhanceSwitch(true)
            VideoSetting.DeviceLevel.High
        } else if (deviceScore >= 75) {
            VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_AUTO)
            VideoSetting.setCurrAudienceEnhanceSwitch(true)
            VideoSetting.DeviceLevel.Medium
        } else {
            VideoSetting.setCurrAudienceEnhanceSwitch(false)
            VideoSetting.DeviceLevel.Low
        }
        VideoSetting.updateBroadcastSetting(
            deviceLevel = deviceLevel,
            isByAudience = true
        )
    }

    private class RoomListAdapter constructor(
        private var mList: List<ShowRoomDetailModel>?,
        private val mContext: Context,
        private val mOnItemClick: ((ShowRoomDetailModel, View, Int, MotionEvent) -> Unit)? = null
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {

        fun setDataList(list: List<ShowRoomDetailModel>?) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.show_item_room_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val num = position % 5
            val resId: Int = mContext.resources.getIdentifier(
                "show_img_room_item_bg_$num",
                "mipmap",
                mContext.packageName
            )
            holder.ivBackground.setImageResource(resId)
            val data: ShowRoomDetailModel = mList!![position]
            GlideApp.with(holder.ivAvatar.context).load(data.ownerAvatar)
                .into(holder.ivAvatar)
            holder.tvRoomName.text = data.roomName
            holder.tvPersonNum.text = mContext.getString(R.string.show_user_count, data.roomUserCount)
            holder.tvUserName.text = mContext.getString(R.string.show_room_id, data.roomId)
            holder.ivLock.visibility = View.GONE
            holder.itemView.setOnTouchListener { view, event ->
                mOnItemClick?.invoke(data, view, position, event)
                true
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivBackground: ImageView
            var ivAvatar: ImageView
            var ivLock: ImageView
            var tvRoomName: TextView
            var tvUserName: TextView
            var tvPersonNum: TextView

            init {
                ivBackground = itemView.findViewById(R.id.ivBackground)
                ivAvatar = itemView.findViewById(R.id.ivAvatar)
                ivLock = itemView.findViewById(R.id.ivLock)
                tvRoomName = itemView.findViewById(R.id.tvRoomName)
                tvUserName = itemView.findViewById(R.id.tvUserName)
                tvPersonNum = itemView.findViewById(R.id.tvPersonNum)
            }
        }
    }
}
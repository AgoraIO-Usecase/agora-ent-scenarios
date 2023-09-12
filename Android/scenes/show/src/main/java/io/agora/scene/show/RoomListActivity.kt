package io.agora.scene.show

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowRoomItemBinding
import io.agora.scene.show.databinding.ShowRoomListActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.videoSwitcherAPI.VideoSwitcher
import io.agora.scene.show.widget.PresetAudienceDialog
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : AppCompatActivity() {

    private val mBinding by lazy { ShowRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private lateinit var mRoomAdapter: BindingSingleAdapter<ShowRoomDetailModel, ShowRoomItemBinding>
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
        mRoomAdapter = object : BindingSingleAdapter<ShowRoomDetailModel, ShowRoomItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowRoomItemBinding>,
                position: Int
            ) {
                updateRoomItem(mDataList, position, holder.binding, getItem(position) ?: return)
            }
        }
        mBinding.rvRooms.adapter = mRoomAdapter
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
        mBinding.btnCreateRoom2.setOnClickListener { goLivePrepareActivity() }
    }

    private fun updateList(data: List<ShowRoomDetailModel>) {
        mBinding.tvTips1.isVisible = data.isEmpty()
        mBinding.ivBgMobile.isVisible = data.isEmpty()
        mBinding.btnCreateRoom2.isVisible = data.isEmpty()
        mBinding.btnCreateRoom.isVisible = data.isNotEmpty()
        mBinding.rvRooms.isVisible = data.isNotEmpty()
        mRoomAdapter.resetAll(data)

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

    @SuppressLint("ClickableViewAccessibility")
    private fun updateRoomItem(
        list: List<ShowRoomDetailModel>,
        position: Int,
        binding: ShowRoomItemBinding,
        roomInfo: ShowRoomDetailModel
    ) {
        binding.tvRoomName.text = roomInfo.roomName
        binding.tvRoomId.text = getString(R.string.show_room_id, roomInfo.roomId)
        binding.tvUserCount.text = getString(R.string.show_user_count, roomInfo.roomUserCount)
        binding.ivCover.setImageResource(roomInfo.getThumbnailIcon())

        binding.root.setOnTouchListener { v, event ->
            val rtcConnection =
                RtcConnection(roomInfo.roomId, UserManager.getInstance().user.id.toInt())
            if (RtcEngineInstance.generalToken() == "") {
                fetchUniversalToken({
                    when (event!!.action) {
                        MotionEvent.ACTION_DOWN -> {
                            mRtcVideoSwitcher.preloadConnections(list.map {
                                RtcConnection(
                                    it.roomId,
                                    UserManager.getInstance().user.id.toInt()
                                )
                            })
                            ShowLogger.d("hugo", "ACTION_DOWN")

                            if (mRtcEngine.queryDeviceScore() < 75) {
                                // 低端机观众加入频道前默认开启硬解（解决看高分辨率卡顿问题），但是在410分支硬解码会带来200ms的秒开耗时增加
                                mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
                                // 低端机观众加入频道前默认开启下行零拷贝，下行零拷贝和超分有冲突， 低端机默认关闭超分
                                mRtcEngine.setParameters("{\"rtc.video.decoder_out_byte_frame\": true}")
                            }

                            val channelMediaOptions = ChannelMediaOptions()
                            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            channelMediaOptions.autoSubscribeVideo = false
                            channelMediaOptions.autoSubscribeAudio = false
                            channelMediaOptions.publishCameraTrack = false
                            channelMediaOptions.publishMicrophoneTrack = false
                            // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            ShowLogger.d("hugo", "click down join channel: $rtcConnection")
                            mRtcVideoSwitcher.preJoinChannel(
                                rtcConnection,
                                channelMediaOptions,
                                RtcEngineInstance.generalToken(),
                                null
                            )
                            //启动机器人
                            mService.startCloudPlayer()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            ShowLogger.d("hugo", "ACTION_CANCEL")
                            mRtcVideoSwitcher.leaveChannel(rtcConnection, true)
                        }
                        MotionEvent.ACTION_UP -> {
                            ShowLogger.d("hugo", "ACTION_UP")

                            val channelMediaOptions = ChannelMediaOptions()
                            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            channelMediaOptions.autoSubscribeVideo = true
                            channelMediaOptions.autoSubscribeAudio = true
                            channelMediaOptions.publishCameraTrack = false
                            channelMediaOptions.publishMicrophoneTrack = false
                            // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            ShowLogger.d("hugo", "click up join channel: $rtcConnection")
                            mRtcVideoSwitcher.joinChannel(
                                rtcConnection,
                                channelMediaOptions,
                                RtcEngineInstance.generalToken(),
                                null,
                                true
                            )
                            goLiveDetailActivity(list, position, roomInfo)
                        }
                    }
                })
            } else {
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mRtcVideoSwitcher.preloadConnections(list.map {
                            RtcConnection(
                                it.roomId,
                                UserManager.getInstance().user.id.toInt()
                            )
                        })
                        ShowLogger.d("hugo", "ACTION_DOWN")

                        if (mRtcEngine.queryDeviceScore() < 75) {
                            // 低端机观众加入频道前默认开启硬解（解决看高分辨率卡顿问题），但是在410分支硬解码会带来200ms的秒开耗时增加
                            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
                            // 低端机观众加入频道前默认开启下行零拷贝，下行零拷贝和超分有冲突， 低端机默认关闭超分
                            mRtcEngine.setParameters("{\"rtc.video.decoder_out_byte_frame\": true}")
                        }

                        val channelMediaOptions = ChannelMediaOptions()
                        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        channelMediaOptions.autoSubscribeVideo = false
                        channelMediaOptions.autoSubscribeAudio = false
                        channelMediaOptions.publishCameraTrack = false
                        channelMediaOptions.publishMicrophoneTrack = false
                        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                        channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        ShowLogger.d("hugo", "click down join channel: $rtcConnection")
                        mRtcVideoSwitcher.preJoinChannel(
                            rtcConnection,
                            channelMediaOptions,
                            RtcEngineInstance.generalToken(),
                            null
                        )
                        //启动机器人
                        mService.startCloudPlayer()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        ShowLogger.d("hugo", "ACTION_CANCEL")
                        mRtcVideoSwitcher.leaveChannel(rtcConnection, true)
                    }
                    MotionEvent.ACTION_UP -> {
                        ShowLogger.d("hugo", "ACTION_UP")
                        val channelMediaOptions = ChannelMediaOptions()
                        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        channelMediaOptions.autoSubscribeVideo = true
                        channelMediaOptions.autoSubscribeAudio = true
                        channelMediaOptions.publishCameraTrack = false
                        channelMediaOptions.publishMicrophoneTrack = false
                        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
                        channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        ShowLogger.d("hugo", "click up join channel: $rtcConnection")
                        mRtcVideoSwitcher.joinChannel(
                            rtcConnection,
                            channelMediaOptions,
                            RtcEngineInstance.generalToken(),
                            null,
                            true
                        )
                        goLiveDetailActivity(list, position, roomInfo)
                    }
                }
            }
            true
        }
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
}
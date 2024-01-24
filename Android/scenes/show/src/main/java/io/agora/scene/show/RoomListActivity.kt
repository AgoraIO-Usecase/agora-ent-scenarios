package io.agora.scene.show

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.GlideApp
import io.agora.scene.base.SceneAliveTime
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowRoomListActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.videoloaderapi.OnLiveRoomItemTouchEventHandler
import io.agora.videoloaderapi.OnRoomListScrollEventHandler
import io.agora.videoloaderapi.VideoLoader
import io.agora.scene.show.widget.PresetAudienceDialog
import io.agora.scene.widget.utils.StatusBarUtil

/*
 * 房间列表 activity
 */
class RoomListActivity : AppCompatActivity() {

    private val mBinding by lazy { ShowRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private var mAdapter: RoomListAdapter? = null
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private val roomDetailModelList = mutableListOf<ShowRoomDetailModel>()

    private var isFirstLoad = true
    private var onRoomListScrollEventHandler: OnRoomListScrollEventHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, true)
        setContentView(mBinding.root)
        //启动机器人
        mService.startCloudPlayer()
        //获取万能token
        fetchUniversalToken ({
            val roomList = arrayListOf<VideoLoader.RoomInfo>( )
            roomDetailModelList.forEach { room ->
                roomList.add(
                    VideoLoader.RoomInfo(
                        room.roomId,
                        arrayListOf(
                            VideoLoader.AnchorInfo(
                                room.roomId,
                                room.ownerId.toInt(),
                                RtcEngineInstance.generalToken()
                            )
                        )
                    )
                )
            }
            onRoomListScrollEventHandler?.updateRoomList(roomList)
        })
        initView()
        initVideoSettings()

        SceneAliveTime.fetchShowAliveTime ({ show, pk ->
            ShowLogger.d("RoomListActivity", "fetchShowAliveTime: show: $show, pk: $pk")
            ShowServiceProtocol.ROOM_AVAILABLE_DURATION = show * 1000L
            ShowServiceProtocol.PK_AVAILABLE_DURATION = pk * 1000L
        })
    }

    private fun initView() {
        onRoomListScrollEventHandler = object: OnRoomListScrollEventHandler(mRtcEngine, UserManager.getInstance().user.id.toInt()) {}
        mBinding.titleView.setLeftClick { finish() }
        mBinding.titleView.setRightIconClick {
            showAudienceSetting()
        }
        mAdapter = RoomListAdapter(null, this, {
            // 需要重新拉取Token
            fetchUniversalToken ({
                val roomList = arrayListOf<VideoLoader.RoomInfo>( )
                roomDetailModelList.forEach { room ->
                    roomList.add(
                        VideoLoader.RoomInfo(
                            room.roomId,
                            arrayListOf(
                                VideoLoader.AnchorInfo(
                                    room.roomId,
                                    room.ownerId.toInt(),
                                    RtcEngineInstance.generalToken()
                                )
                            )
                        )
                    )
                }
                onRoomListScrollEventHandler?.updateRoomList(roomList)
            })
        }, {
            //启动机器人
            mService.startCloudPlayer()
        }, { position, roomInfo ->
            goLiveDetailActivity(roomDetailModelList, position, roomInfo)
        })

        mBinding.rvRooms.adapter = mAdapter
        mBinding.rvRooms.addOnScrollListener(onRoomListScrollEventHandler as OnRoomListScrollEventHandler)

        mBinding.smartRefreshLayout.setEnableLoadMore(false)
        mBinding.smartRefreshLayout.setEnableRefresh(true)
        mBinding.smartRefreshLayout.setOnRefreshListener {
            mService.getRoomList(
                success = {
                    roomDetailModelList.clear()
                    roomDetailModelList.addAll(it)
                    if (isFirstLoad) {
                        val roomList = arrayListOf<VideoLoader.RoomInfo>( )
                        it.forEach { room ->
                            roomList.add(
                                VideoLoader.RoomInfo(
                                    room.roomId,
                                    arrayListOf(
                                        VideoLoader.AnchorInfo(
                                            room.roomId,
                                            room.ownerId.toInt(),
                                            RtcEngineInstance.generalToken()
                                        )
                                    )
                                )
                            )
                        }
                        onRoomListScrollEventHandler?.updateRoomList(roomList)
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
                ShowLogger.d("RoomListActivity", "generateToken success， uid：$localUId")
                RtcEngineInstance.setupGeneralToken(it)
                success.invoke()
            },
            failure = {
                ShowLogger.e("RoomListActivity", it, "generateToken failure：$it")
                ToastUtils.showToast(it?.message ?: "generate token failure")
                error?.invoke(it)
            })
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
        private val mOnNeedFetchToken: (() -> Unit)? = null,
        private val mOnNeedStartCloudPlayer: (() -> Unit)? = null,
        private val mOnGotoRoom: ((position: Int, info: ShowRoomDetailModel) -> Unit)? = null
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

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
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

            val onTouchEventHandler = object : OnLiveRoomItemTouchEventHandler(
                RtcEngineInstance.rtcEngine,
                VideoLoader.RoomInfo(
                    data.roomId,
                    arrayListOf(
                        VideoLoader.AnchorInfo(
                            data.roomId,
                            data.ownerId.toInt(),
                            RtcEngineInstance.generalToken()
                        )
                    )
                ),
                UserManager.getInstance().user.id.toInt()) {
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (v == null || event == null) return true
                    val isRoomOwner = data.ownerId == UserManager.getInstance().user.id.toString()
                    if (isRoomOwner) {
                        if (event.action == MotionEvent.ACTION_UP) {
                            ToastUtils.showToast(R.string.show_broadcaster_bad_exit)
                        }
                    } else {
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (RtcEngineInstance.generalToken() == "") {
                                    mOnNeedFetchToken?.invoke()
                                } else {
                                    if (RtcEngineInstance.rtcEngine.queryDeviceScore() < 75) {
                                        RtcEngineInstance.rtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
                                        RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.decoder_out_byte_frame\": true}")
                                    }
                                    super.onTouch(v, event)
                                    mOnNeedStartCloudPlayer?.invoke()
                                }
                            }
                            MotionEvent.ACTION_CANCEL -> {
                                super.onTouch(v, event)
                            }
                            MotionEvent.ACTION_UP -> {
                                if (RtcEngineInstance.generalToken() != "") {
                                    super.onTouch(v, event)
                                    mOnGotoRoom?.invoke(position, data)
                                }
                            }
                        }
                    }
                    return true
                }

                override fun onRequireRenderVideo(info: VideoLoader.AnchorInfo): VideoLoader.VideoCanvasContainer? {
                    ShowLogger.d("RoomListActivity", "onRequireRenderVideo")
                    return null
                }
            }
            holder.itemView.setOnTouchListener(onTouchEventHandler)
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
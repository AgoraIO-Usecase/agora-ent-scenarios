package io.agora.scene.joy.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.GlideApp
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.JoyLogger
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.databinding.JoyActivityRoomListBinding
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.videoLoaderAPI.OnLiveRoomItemTouchEventHandler
import io.agora.scene.joy.videoLoaderAPI.OnRoomListScrollEventHandler
import io.agora.scene.joy.videoLoaderAPI.VideoLoader

class RoomListActivity : BaseViewBindingActivity<JoyActivityRoomListBinding>() {

    companion object {
        private const val TAG = "Joy_RoomListActivity"
    }

    private val mJoyService by lazy { JoyServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private val mJoyRoomInfoList = mutableListOf<JoyRoomInfo>()
    private var mJoyListAdapter: RoomListAdapter? = null

    private var mIsFirstLoad = true
    private var mOnRoomListScrollEventHandler: OnRoomListScrollEventHandler? = null

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityRoomListBinding {
        return JoyActivityRoomListBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
        //获取万能token
        fetchUniversalToken({
            val roomList = arrayListOf<VideoLoader.RoomInfo>()
            mJoyRoomInfoList.forEach { room ->
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
            mOnRoomListScrollEventHandler?.updateRoomList(roomList)
        })
    }

    // 获取万能token
    private fun fetchUniversalToken(success: () -> Unit, error: ((Exception?) -> Unit)? = null) {
        val localUId = UserManager.getInstance().user.id
        TokenGenerator.generateToken("", localUId.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            TokenGenerator.AgoraTokenType.rtc,
            success = {
                JoyLogger.d(TAG, "generateToken success：$it， uid：$localUId")
                RtcEngineInstance.setupGeneralToken(it)
                success.invoke()
            },
            failure = {
                JoyLogger.e(TAG, "generateToken failure：${it?.message}")
                ToastUtils.showToast(it?.message ?: "generate token failure")
                error?.invoke(it)
            })
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mOnRoomListScrollEventHandler = object : OnRoomListScrollEventHandler(
            mRtcEngine, UserManager.getInstance()
                .user.id.toInt()
        ) {}
        binding.titleView.setLeftClick { finish() }
        mJoyListAdapter = RoomListAdapter(emptyList(), this, {
            // 需要重新拉取Token
            fetchUniversalToken({
                val roomList = arrayListOf<VideoLoader.RoomInfo>()
                mJoyRoomInfoList.forEach { room ->
                    roomList.add(
                        VideoLoader.RoomInfo(
                            room.roomId,
                            arrayListOf(
                                VideoLoader.AnchorInfo(
                                    room.roomId,
                                    room.ownerId,
                                    RtcEngineInstance.generalToken()
                                )
                            )
                        )
                    )
                }
                mOnRoomListScrollEventHandler?.updateRoomList(roomList)
            })
        }, { position, roomInfo ->
            RoomLivingActivity.launch(this, roomInfo)
        })

        binding.rvRooms.adapter = mJoyListAdapter
        binding.rvRooms.addOnScrollListener(mOnRoomListScrollEventHandler as OnRoomListScrollEventHandler)

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)
        binding.smartRefreshLayout.setOnRefreshListener {
            mJoyService.getRoomList(
                completion = {
                    mJoyRoomInfoList.clear()
                    mJoyRoomInfoList.addAll(it)
                    if (mIsFirstLoad) {
                        val roomList = arrayListOf<VideoLoader.RoomInfo>()
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
                        mOnRoomListScrollEventHandler?.updateRoomList(roomList)
                        mIsFirstLoad = false
                    }
                    updateList(it)
                },
            )
        }
        binding.smartRefreshLayout.autoRefresh()
        binding.btnCreateRoom.setOnClickListener {
            LivePrepareActivity.launch(this)
        }
    }

    private fun updateList(data: List<JoyRoomInfo>) {
        binding.tvTips1.isVisible = data.isEmpty()
        binding.ivBgMobile.isVisible = data.isEmpty()
        binding.rvRooms.isVisible = data.isNotEmpty()
        mJoyListAdapter?.setDataList(data)

        binding.smartRefreshLayout.finishRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJoyService.reset()
        RtcEngineInstance.destroy()
        RtcEngineInstance.setupGeneralToken("")
    }

    private class RoomListAdapter constructor(
        private var mList: List<JoyRoomInfo>,
        private val mContext: Context,
        private val mOnNeedFetchToken: (() -> Unit)? = null,
        private val mOnGotoRoom: ((position: Int, info: JoyRoomInfo) -> Unit)? = null
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {

        fun setDataList(list: List<JoyRoomInfo>) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.joy_item_room_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val num = position % 5
            val resId: Int =
                mContext.resources.getIdentifier("joy_img_room_item_bg_$num", "mipmap", mContext.packageName)
            holder.layoutBackground.setBackgroundResource(resId)
            val data: JoyRoomInfo = mList[position]
            GlideApp.with(holder.ivAvatar.context).load(data.ownerAvatar)
                .into(holder.ivAvatar)
            holder.tvRoomName.text = data.roomName
            holder.tvPersonNum.text = mContext.getString(R.string.joy_user_count, data.roomUserCount)
            holder.tvRoomId.text = mContext.getString(R.string.joy_room_id, data.roomId)

            val onTouchEventHandler = object : OnLiveRoomItemTouchEventHandler(
                RtcEngineInstance.rtcEngine,
                VideoLoader.RoomInfo(
                    data.roomId,
                    arrayListOf(
                        VideoLoader.AnchorInfo(
                            data.roomId,
                            data.ownerId,
                            RtcEngineInstance.generalToken()
                        )
                    )
                ),
                UserManager.getInstance().user.id.toInt()
            ) {
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (v == null || event == null) return true
                    val isRoomOwner = data.ownerId == UserManager.getInstance().user.id.toInt()
                    if (isRoomOwner) {
                        if (event.action == MotionEvent.ACTION_UP) {
                            ToastUtils.showToast(R.string.joy_broadcaster_bad_exit)
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
                    JoyLogger.d("RoomListActivity", "onRequireRenderVideo")
                    return null
                }
            }
            holder.itemView.setOnTouchListener(onTouchEventHandler)
        }

        override fun getItemCount(): Int {
            return mList.size
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var layoutBackground: ViewGroup
            var ivAvatar: ImageView
            var tvGameTag: TextView
            var tvRoomName: TextView
            var tvRoomId: TextView
            var tvPersonNum: TextView

            init {
                layoutBackground = itemView.findViewById(R.id.layoutBackground)
                ivAvatar = itemView.findViewById(R.id.ivAvatar)
                tvGameTag = itemView.findViewById(R.id.tvGameTag)
                tvRoomName = itemView.findViewById(R.id.tvRoomName)
                tvRoomId = itemView.findViewById(R.id.tvRoomId)
                tvPersonNum = itemView.findViewById(R.id.tvPersonNum)
            }
        }
    }
}
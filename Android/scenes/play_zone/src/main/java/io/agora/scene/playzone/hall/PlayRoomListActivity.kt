package io.agora.scene.playzone.hall

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.ISingleCallback
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityRoomListLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemRoomListBinding
import io.agora.scene.playzone.live.PlayRoomGameActivity
import io.agora.scene.playzone.service.PlayChatRoomService
import io.agora.scene.playzone.service.PlayZoneParameters
import io.agora.scene.playzone.service.PlayZoneServiceProtocol
import io.agora.scene.widget.dialog.InputPasswordDialog
import io.agora.scene.widget.utils.UiUtils

class PlayRoomListActivity : BaseViewBindingActivity<PlayZoneActivityRoomListLayoutBinding>() {

    companion object {
        private const val TAG = "Joy_RoomListActivity"
    }

    private val mRoomViewModel: PlayCreateViewModel by lazy {
        ViewModelProvider(this)[PlayCreateViewModel::class.java]
    }


    private var mPlayZoneListAdapter: PlayRoomListAdapter? = null

    private var inputPasswordDialog: InputPasswordDialog? = null

    private var isJoining = false

    init {
        PlayZoneServiceProtocol.reset()
    }

    override fun getViewBinding(inflater: LayoutInflater): PlayZoneActivityRoomListLayoutBinding {
        return PlayZoneActivityRoomListLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleView.setLeftClick { finish() }
        mPlayZoneListAdapter = PlayRoomListAdapter(emptyList(), this) { position, roomInfo ->
            val password = roomInfo.customPayload[PlayZoneParameters.PASSWORD] as? String

            if (!password.isNullOrEmpty()) {
                showInputPwdDialog(roomInfo)
            } else {
                if (!isJoining) {
                    isJoining = true
                    mRoomViewModel.joinRoom(roomInfo, null)
                }
            }
        }

        binding.rvRooms.adapter = mPlayZoneListAdapter

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)

        binding.smartRefreshLayout.setOnRefreshListener {
            mRoomViewModel.getRoomList()
        }
        binding.smartRefreshLayout.autoRefresh()

    }

    private fun showInputPwdDialog(roomInfo: AUIRoomInfo) {
        if (inputPasswordDialog == null) {
            inputPasswordDialog = InputPasswordDialog(this)
        }
        inputPasswordDialog?.apply {
            clearContent()
            iSingleCallback = ISingleCallback<Int, Any> { type, data ->
                if (data is String) {
                    mRoomViewModel.joinRoom(roomInfo, data)
                }
            }
            show()
        }
    }

    override fun requestData() {
        super.requestData()
        mRoomViewModel.roomModelListLiveData.observe(this) { roomList ->
            hideLoadingView()
            binding.smartRefreshLayout.finishRefresh()
            if (roomList.isNullOrEmpty()) {
                binding.rvRooms.visibility = View.GONE
                binding.tvTips1.visibility = View.VISIBLE
                binding.ivBgMobile.setVisibility(View.VISIBLE)
            } else {
                mPlayZoneListAdapter?.setDataList(roomList)
                binding.rvRooms.visibility = View.VISIBLE
                binding.tvTips1.visibility = View.GONE
                binding.ivBgMobile.setVisibility(View.GONE)
            }
        }
        mRoomViewModel.joinRoomInfoLiveData.observe(this) { roomInfo ->
            isJoining = false
            if (roomInfo != null) {
                PlayRoomGameActivity.launch(this, roomInfo)
            } else {
                binding.smartRefreshLayout.autoRefresh()
            }
        }
    }


    override fun onRestart() {
        super.onRestart()
        binding.smartRefreshLayout.autoRefresh()
        Log.d(TAG, "joy roomList activity onRestart")
    }

    private class PlayRoomListAdapter constructor(
        private var mList: List<AUIRoomInfo>,
        private val mContext: Context,
        private val mOnGotoRoom: ((position: Int, info: AUIRoomInfo) -> Unit)? = null
    ) : RecyclerView.Adapter<PlayRoomListAdapter.ViewHolder?>() {

        @DrawableRes
        private fun getThumbnailIcon(thumbnailId: String?) = when (thumbnailId) {
            "0" -> R.drawable.play_zone_img_room_item_bg_0
            "1" -> R.drawable.play_zone_img_room_item_bg_1
            "2" -> R.drawable.play_zone_img_room_item_bg_2
            "3" -> R.drawable.play_zone_img_room_item_bg_3
            else -> R.drawable.play_zone_img_room_item_bg_4
        }

        inner class ViewHolder(val binding: PlayZoneItemRoomListBinding) : RecyclerView.ViewHolder(binding.root)

        fun setDataList(list: List<AUIRoomInfo>) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                PlayZoneItemRoomListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val data: AUIRoomInfo = mList[position]
            holder.binding.tvRoomName.text = data.roomName
            val userCount = data.customPayload[PlayZoneParameters.ROOM_USER_COUNT]
            if (userCount is Int) {
                holder.binding.tvUserCount.text = mContext.getString(R.string.play_zone_user_count, userCount.toInt())
            } else if (userCount is Long) {
                holder.binding.tvUserCount.text = mContext.getString(R.string.play_zone_user_count, userCount.toInt())
            }
            holder.binding.tvRoomId.text = mContext.getString(R.string.play_zone_room_id, data.roomId)
            val badgeTitle = data.customPayload[PlayZoneParameters.BADGE_TITLE] as String?
            holder.binding.tvGameTag.isGone = badgeTitle.isNullOrEmpty()
            holder.binding.tvGameTag.text = badgeTitle ?: ""
            (data.customPayload[PlayZoneParameters.THUMBNAIL_ID] as String?)?.let { thumbnail ->
                holder.binding.ivCover.setImageResource(getThumbnailIcon(thumbnail))
            }
            val password = data.customPayload[PlayZoneParameters.PASSWORD] as? String
            holder.binding.ivLock.isVisible = !password.isNullOrEmpty()
            holder.itemView.setOnClickListener {
                if (UiUtils.isFastClick()) return@setOnClickListener
                mOnGotoRoom?.invoke(position, data)
            }
            GlideApp.with(holder.binding.ivAvatar)
                .load(data.roomOwner?.userAvatar ?: "")
                .error(R.mipmap.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.binding.ivAvatar)
        }

        override fun getItemCount(): Int {
            return mList.size
        }
    }
}
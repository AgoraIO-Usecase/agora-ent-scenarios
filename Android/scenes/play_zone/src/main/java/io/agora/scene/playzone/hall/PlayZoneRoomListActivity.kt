package io.agora.scene.playzone.hall

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityRoomListBinding
import io.agora.scene.playzone.databinding.PlayZoneItemRoomListBinding
import io.agora.scene.playzone.service.PlayZoneParameters
import io.agora.scene.playzone.service.PlayZoneServiceProtocol
import io.agora.scene.widget.utils.UiUtils

class PlayZoneRoomListActivity : BaseViewBindingActivity<PlayZoneActivityRoomListBinding>() {

    companion object {
        private const val TAG = "Joy_RoomListActivity"
    }

    private val mPlayZoneService by lazy { PlayZoneServiceProtocol.serviceProtocol }

    private var mJoyListAdapter: RoomListAdapter? = null

    init {
        PlayZoneServiceProtocol.reset()
    }

    override fun getViewBinding(inflater: LayoutInflater): PlayZoneActivityRoomListBinding {
        return PlayZoneActivityRoomListBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleView.setLeftClick { finish() }
        mJoyListAdapter = RoomListAdapter(emptyList(), this) { position, roomInfo ->

//            mPlayZoneService.joinRoom(roomInfo.roomId) { error ->
//                if (error == null) {
//
//                } else {
//                    error.message?.let {
//                        ToastUtils.showToast(it)
//                    }
//                }
//            }
        }

        binding.rvRooms.adapter = mJoyListAdapter

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)

        binding.smartRefreshLayout.setOnRefreshListener {
//            mPlayZoneService.getRoomList { error, roomList ->
//                updateList(roomList ?: emptyList())
//            }
        }
        binding.smartRefreshLayout.autoRefresh()

    }

    private fun updateList(data: List<AUIRoomInfo>) {
        binding.tvTips1.isVisible = data.isEmpty()
        binding.ivBgMobile.isVisible = data.isEmpty()
        binding.rvRooms.isVisible = data.isNotEmpty()
        mJoyListAdapter?.setDataList(data)

        binding.smartRefreshLayout.finishRefresh()
    }

    override fun onRestart() {
        super.onRestart()
        binding.smartRefreshLayout.autoRefresh()
        Log.d(TAG, "joy roomList activity onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private class RoomListAdapter constructor(
        private var mList: List<AUIRoomInfo>,
        private val mContext: Context,
        private val mOnGotoRoom: ((position: Int, info: AUIRoomInfo) -> Unit)? = null
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {

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
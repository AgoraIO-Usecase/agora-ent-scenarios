package io.agora.scene.joy.create

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
import io.agora.scene.joy.R
import io.agora.scene.joy.JoyServiceManager
import io.agora.scene.joy.databinding.JoyActivityRoomListBinding
import io.agora.scene.joy.databinding.JoyItemRoomList4Binding
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.live.RoomLivingActivity
import io.agora.scene.joy.service.JoyParameters
import io.agora.scene.joy.service.TokenConfig
import io.agora.scene.joy.service.api.JoyApiManager
import io.agora.scene.widget.utils.UiUtils

class RoomListActivity : BaseViewBindingActivity<JoyActivityRoomListBinding>() {

    companion object {
        private const val TAG = "Joy_RoomListActivity"
    }

    private val mJoyService by lazy { JoyServiceProtocol.serviceProtocol }

    private var mJoyListAdapter: RoomListAdapter? = null

    init {
        JoyApiManager.reset()
        JoyServiceProtocol.reset()
    }

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityRoomListBinding {
        return JoyActivityRoomListBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
//        JoyServiceManager.renewTokens { tokenConfig: TokenConfig?, exception: Exception? ->
//            if (exception == null) {
//                binding.smartRefreshLayout.autoRefresh()
//            }
//        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleView.setLeftClick { finish() }
        mJoyListAdapter = RoomListAdapter(emptyList(), this) { position, roomInfo ->
            RoomLivingActivity.launch(this, roomInfo)
        }

        binding.rvRooms.adapter = mJoyListAdapter

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)

        binding.smartRefreshLayout.setOnRefreshListener {
            JoyServiceManager.renewTokens { tokenConfig: TokenConfig?, exception: Exception? ->
                if (exception == null) {
                    mJoyService.getRoomList { roomList ->
                        updateList(roomList)
                    }
                }
            }
        }
        binding.smartRefreshLayout.autoRefresh()

        binding.btnCreateRoom.setOnClickListener {
            LivePrepareActivity.launch(this)
        }
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
        JoyServiceManager.destroy()
        mJoyService.reset()
    }

    private class RoomListAdapter constructor(
        private var mList: List<AUIRoomInfo>,
        private val mContext: Context,
        private val mOnGotoRoom: ((position: Int, info: AUIRoomInfo) -> Unit)? = null
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {

        @DrawableRes
        private fun getThumbnailIcon(thumbnailId: String?) = when (thumbnailId) {
            "0" -> R.drawable.joy_img_room_item_bg_0
            "1" -> R.drawable.joy_img_room_item_bg_1
            "2" -> R.drawable.joy_img_room_item_bg_2
            "3" -> R.drawable.joy_img_room_item_bg_3
            else -> R.drawable.joy_img_room_item_bg_4
        }

        inner class ViewHolder(val binding: JoyItemRoomList4Binding) : RecyclerView.ViewHolder(binding.root)

        fun setDataList(list: List<AUIRoomInfo>) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                JoyItemRoomList4Binding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }


        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val data: AUIRoomInfo = mList[position]
            holder.binding.tvRoomName.text = data.roomName
            val userCount = data.customPayload[JoyParameters.ROOM_USER_COUNT]
            if (userCount is Int) {
                holder.binding.tvUserCount.text = mContext.getString(R.string.joy_user_count, userCount.toInt())
            } else if (userCount is Long) {
                holder.binding.tvUserCount.text = mContext.getString(R.string.joy_user_count, userCount.toInt())
            }
            holder.binding.tvRoomId.text = mContext.getString(R.string.joy_room_id, data.roomId)
            val badgeTitle = data.customPayload[JoyParameters.BADGE_TITLE] as String?
            holder.binding.tvGameTag.isGone = badgeTitle.isNullOrEmpty()
            holder.binding.tvGameTag.text = badgeTitle ?: ""
            (data.customPayload[JoyParameters.THUMBNAIL_ID] as String?)?.let { thumbnail ->
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
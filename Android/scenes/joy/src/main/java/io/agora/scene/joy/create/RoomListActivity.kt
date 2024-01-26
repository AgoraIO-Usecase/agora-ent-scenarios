package io.agora.scene.joy.create

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.JoyLogger
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.databinding.JoyActivityRoomListBinding
import io.agora.scene.joy.databinding.JoyItemRoomList4Binding
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.live.RoomLivingActivity
import io.agora.scene.widget.utils.UiUtils

class RoomListActivity : BaseViewBindingActivity<JoyActivityRoomListBinding>() {

    companion object {
        private const val TAG = "Joy_RoomListActivity"
    }

    private val mJoyService by lazy { JoyServiceProtocol.getImplInstance() }

    private var mJoyListAdapter: RoomListAdapter? = null

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityRoomListBinding {
        return JoyActivityRoomListBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.root)
        //获取万能token
        fetchUniversalToken({
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
        binding.titleView.setLeftClick { finish() }
        mJoyListAdapter = RoomListAdapter(emptyList(), this) { position, roomInfo ->
            RoomLivingActivity.launch(this, roomInfo)
        }

        binding.rvRooms.adapter = mJoyListAdapter

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)
        binding.smartRefreshLayout.setOnRefreshListener {
            mJoyService.getRoomList {
                updateList(it)
            }
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

    override fun onRestart() {
        super.onRestart()
        binding.smartRefreshLayout.autoRefresh()
        Log.d(TAG, "joy roomList activity onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngineInstance.destroy()
        RtcEngineInstance.setupGeneralToken("")
    }

    private class RoomListAdapter constructor(
        private var mList: List<JoyRoomInfo>,
        private val mContext: Context,
        private val mOnGotoRoom: ((position: Int, info: JoyRoomInfo) -> Unit)? = null
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

        fun setDataList(list: List<JoyRoomInfo>) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                JoyItemRoomList4Binding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val data: JoyRoomInfo = mList[position]
            holder.binding.tvRoomName.text = data.roomName
            holder.binding.tvUserCount.text = mContext.getString(R.string.joy_user_count, data.roomUserCount)
            holder.binding.tvRoomId.text = mContext.getString(R.string.joy_room_id, data.roomId)
            holder.binding.tvGameTag.isVisible = data.badgeTitle.isNotEmpty()
            holder.binding.tvGameTag.text = data.badgeTitle
            holder.binding.ivCover.setImageResource(getThumbnailIcon(data.thumbnailId))
            holder.itemView.setOnClickListener {
                if (UiUtils.isFastClick()) return@setOnClickListener
                mOnGotoRoom?.invoke(position, data)
            }
            GlideApp.with(holder.binding.ivAvatar)
                .load(data.ownerAvatar)
                .error(R.mipmap.userimage)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.binding.ivAvatar)
        }

        override fun getItemCount(): Int {
            return mList.size
        }
    }
}
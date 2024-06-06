package io.agora.scene.ktv.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.ktv.KtvCenter
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvActivityRoomListBinding
import io.agora.scene.ktv.databinding.KtvItemRoomListBinding
import io.agora.scene.ktv.live.RoomLivingActivity
import io.agora.scene.ktv.service.KTVParameters
import io.agora.scene.ktv.service.KTVServiceProtocol.Companion.getImplInstance
import io.agora.scene.ktv.service.fullHeadUrl
import io.agora.scene.widget.dialog.InputPasswordDialog
import io.agora.scene.widget.utils.UiUtils

/**
 * Room list activity
 *
 * @constructor Create empty Room list activity
 */
class RoomListActivity : BaseViewBindingActivity<KtvActivityRoomListBinding>() {

    private val mRoomCreateViewModel: RoomCreateViewModel by lazy {
        ViewModelProvider(this)[RoomCreateViewModel::class.java]
    }

    private var mAdapter: RoomListAdapter? = null
    private var inputPasswordDialog: InputPasswordDialog? = null
    private var isJoining = false
    override fun getViewBinding(inflater: LayoutInflater): KtvActivityRoomListBinding {
        return KtvActivityRoomListBinding.inflate(inflater)
    }

    /**
     * On resume
     *
     */
    override fun onResume() {
        super.onResume()
        setDarkStatusIcon(isBlackDarkStatus)
    }

    /**
     * On restart
     *
     */
    override fun onRestart() {
        super.onRestart()
        binding.smartRefreshLayout.autoRefresh()
    }

    /**
     * On destroy
     *
     */
    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Init view
     *
     * @param savedInstanceState
     */
    override fun initView(savedInstanceState: Bundle?) {
        mAdapter = RoomListAdapter(null, this, object : OnItemClickListener<AUIRoomInfo> {
            override fun onItemClick(data: AUIRoomInfo, view: View, position: Int, viewType: Long) {
                val password = data.customPayload[KTVParameters.PASSWORD] as? String
                if (!password.isNullOrEmpty()) {
                    showInputPwdDialog(data)
                } else {
                    if (!isJoining) {
                        isJoining = true
                        mRoomCreateViewModel.joinRoom(data, null)
                    }
                }
            }
        })
        binding.rvRooms.layoutManager = GridLayoutManager(this, 2)
        binding.rvRooms.adapter = mAdapter
        setOnApplyWindowInsetsListener(binding.root)
        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)

        binding.smartRefreshLayout.setOnRefreshListener {
            mRoomCreateViewModel.loadRooms()
        }
        binding.smartRefreshLayout.autoRefresh()
    }

    /**
     * Init listener
     *
     */
    override fun initListener() {
        binding.btnCreateRoom.setOnClickListener { view: View? ->
            if (UiUtils.isFastClick(1000)) {
                return@setOnClickListener
            }
            CreateRoomDialog(this).show(supportFragmentManager, "CreateRoomDialog")
        }
        mRoomCreateViewModel.roomModelList.observe(this) { vlRoomListModels: List<AUIRoomInfo>? ->
            hideLoadingView()
            binding!!.smartRefreshLayout.finishRefresh()
            if (vlRoomListModels.isNullOrEmpty()) {
                binding.rvRooms.visibility = View.GONE
                binding.tvTips1.visibility = View.VISIBLE
                binding.ivBgMobile.setVisibility(View.VISIBLE)
            } else {
                mAdapter?.setDataList(vlRoomListModels)
                binding.rvRooms.visibility = View.VISIBLE
                binding.tvTips1.visibility = View.GONE
                binding.ivBgMobile.setVisibility(View.GONE)
            }
        }
        mRoomCreateViewModel.roomInfoLiveData.observe(this) { roomInfo ->
            isJoining = false
            if (roomInfo == null) {
                setDarkStatusIcon(isBlackDarkStatus)
            } else {
                RoomLivingActivity.launch(this, roomInfo)
            }
        }
    }

    private fun showInputPwdDialog(data: AUIRoomInfo) {
        if (inputPasswordDialog == null) {
            inputPasswordDialog = InputPasswordDialog(this)
        }
        inputPasswordDialog?.apply {
            clearContent()
            onDefineClickListener = InputPasswordDialog.OnDefineClickListener { password ->
                mRoomCreateViewModel.joinRoom(data, password)
            }
            show()
        }
    }

    private inner class RoomListAdapter constructor(
        private var mList: List<AUIRoomInfo>?,
        private val mContext: Context,
        private val mListener: OnItemClickListener<AUIRoomInfo>
    ) : RecyclerView.Adapter<RoomListAdapter.ViewHolder>() {
        /**
         * Set data list
         *
         * @param list
         */
        fun setDataList(list: List<AUIRoomInfo>?) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(KtvItemRoomListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val num = position % 5
            val resId = getResources().getIdentifier("ktv_img_room_item_bg_$num", "mipmap", packageName)
            holder.binding.ivBackground.setImageResource(resId)
            val data = mList?.get(position) ?: return
            if (data.roomOwner != null) {
                GlideApp.with(mContext)
                    .load(data.roomOwner!!.fullHeadUrl)
                    .into(holder.binding.ivAvatar)
            } else {
                holder.binding.ivAvatar.setImageResource(R.mipmap.default_user_avatar)
            }
            holder.binding.tvRoomName.text = data.roomName
            val userCount = data.customPayload[KTVParameters.ROOM_USER_COUNT] as? Long
            val showCount = (userCount ?: 0) + KtvCenter.userAddMore
            holder.binding.tvPersonNum.text = mContext.getString(R.string.ktv_people_count, showCount)
            holder.binding.tvUserName.text = data.roomOwner?.userName ?: ""
            val password = data.customPayload[KTVParameters.PASSWORD] as? String
            holder.binding.ivLock.isVisible = !password.isNullOrEmpty()
            holder.itemView.setOnClickListener { view: View? ->
                mListener.onItemClick(
                    data,
                    view,
                    position,
                    getItemViewType(position).toLong()
                )
            }
        }

        override fun getItemCount(): Int = mList?.size ?: 0


        /**
         * View holder
         *
         * @property binding
         * @constructor Create empty View holder
         */
        inner class ViewHolder constructor(val binding: KtvItemRoomListBinding) : RecyclerView.ViewHolder(binding.root)
    }
}

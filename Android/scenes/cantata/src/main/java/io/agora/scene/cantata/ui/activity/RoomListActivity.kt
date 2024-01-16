package io.agora.scene.cantata.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.GlideApp
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.ISingleCallback
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataActivityRoomListBinding
import io.agora.scene.cantata.service.CantataServiceProtocol
import io.agora.scene.cantata.service.JoinRoomOutputModel
import io.agora.scene.cantata.service.RoomListModel
import io.agora.scene.cantata.ui.dialog.CantataCreateRoomDialog
import io.agora.scene.cantata.ui.viewmodel.RoomCreateViewModel
import io.agora.scene.widget.dialog.InputPasswordDialog
import io.agora.scene.widget.utils.UiUtils

class RoomListActivity : BaseViewBindingActivity<CantataActivityRoomListBinding>() {

    private val roomCreateViewModel by lazy {
        ViewModelProvider(this)[RoomCreateViewModel::class.java]
    }

    private var mAdapter: RoomListAdapter? = null

    private var isJoining = false

    private var inputPasswordDialog: InputPasswordDialog? = null

    override fun getViewBinding(inflater: LayoutInflater): CantataActivityRoomListBinding {
        return CantataActivityRoomListBinding.inflate(inflater)
    }

    override fun onResume() {
        super.onResume()
        setDarkStatusIcon(isBlackDarkStatus)
        loadRoomList()
    }

    override fun onDestroy() {
        super.onDestroy()
        CantataServiceProtocol.getImplInstance().reset()
    }


    private fun loadRoomList() {
        roomCreateViewModel.loadRooms()
    }

    override fun initView(savedInstanceState: Bundle?) {
        mAdapter = RoomListAdapter(null, this, object : OnItemClickListener<Any> {
                override fun onItemClick(data: Any, view: View, position: Int, viewType: Long) {
                    val model: RoomListModel = data as RoomListModel
                    if (model.isPrivate) {
                        showInputPwdDialog(model)
                    } else {
                        if (!isJoining) {
                            isJoining = true
                            roomCreateViewModel.joinRoom(model.roomNo, null)
                        }
                    }
                }
            })
        binding.rvRooms.layoutManager = GridLayoutManager(this, 2)
        binding.rvRooms.adapter = mAdapter
        binding.smartRefreshLayout.setEnableLoadMore(false)
        setOnApplyWindowInsetsListener(binding.root)
    }

    override fun initListener() {
        binding.btnCreateRoom.setOnClickListener { view ->
            if (UiUtils.isFastClick(1000)) {
                return@setOnClickListener
            }
            CantataCreateRoomDialog(this).show(supportFragmentManager, "CreateRoomDialog")
        }
        roomCreateViewModel.roomModelList.observe(this) { vlRoomListModels: List<RoomListModel>? ->
            hideLoadingView()
            binding.smartRefreshLayout.finishRefresh()
            if (vlRoomListModels.isNullOrEmpty()) {
                binding.rvRooms.visibility = View.GONE
                binding.tvTips1.visibility = View.VISIBLE
                binding.ivBgMobile.visibility = View.VISIBLE
            } else {
                mAdapter!!.setDataList(vlRoomListModels)
                binding.rvRooms.visibility = View.VISIBLE
                binding.tvTips1.visibility = View.GONE
                binding.ivBgMobile.visibility = View.GONE
            }
        }
        roomCreateViewModel.joinRoomResult.observe(this) { ktvJoinRoomOutputModel:
                                                           JoinRoomOutputModel? ->
            isJoining = false
            if (ktvJoinRoomOutputModel == null) {
                setDarkStatusIcon(isBlackDarkStatus)
            } else {
                RoomLivingActivity.launch(this@RoomListActivity, ktvJoinRoomOutputModel)
            }
        }
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout -> loadRoomList() }
    }

    private fun showInputPwdDialog(roomInfo: RoomListModel) {
        if (inputPasswordDialog == null) {
            inputPasswordDialog = InputPasswordDialog(this)
        }
        inputPasswordDialog?.clearContent()
        inputPasswordDialog?.iSingleCallback = ISingleCallback { type: Int?, o: Any? ->
            roomCreateViewModel.joinRoom(
                roomInfo.roomNo, (o as String?)
            )
        }
        inputPasswordDialog?.show()
    }


    private inner class RoomListAdapter  constructor(
        list: List<RoomListModel>?,
        context: Context,
        listener: OnItemClickListener<Any>
    ) :
        RecyclerView.Adapter<RoomListAdapter.ViewHolder?>() {
        private var mList: List<RoomListModel>?
        private val mContext: Context
        private val mListener: OnItemClickListener<Any>

        init {
            mList = list
            mContext = context
            mListener = listener
        }

        fun setDataList(list: List<RoomListModel>?) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.cantata_item_room_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val num = position % 5
            val resId: Int = resources.getIdentifier("cantata_img_room_item_bg_$num", "drawable", getPackageName())
            holder.ivBackground.setImageResource(resId)
            val data: RoomListModel = mList?.get(position) ?:return
            GlideApp.with(holder.ivAvatar.context).load(data.creatorAvatar)
                .into(holder.ivAvatar)
            holder.tvRoomName.text = data.name
            holder.tvPersonNum.text = holder.itemView.context.getString(R.string.cantata_people_count, data
                .roomPeopleNum)
            holder.tvUserName.text = data.creatorName
            if (data.isPrivate) {
                holder.ivLock.visibility = View.VISIBLE
            } else {
                holder.ivLock.visibility = View.GONE
            }
            holder.itemView.setOnClickListener { view: View? ->
                mListener.onItemClick(
                    data,
                    view,
                    position,
                    getItemViewType(position).toLong()
                )
            }
        }

        override fun getItemCount(): Int {
            return if (mList == null) {
                0
            } else {
                mList!!.size
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivBackground: ImageView
            var ivAvatar: ImageView
            var ivLock: ImageView
            var tvRoomName: TextView
            var tvUserName: TextView
            var tvPersonNum: TextView

            init {
                ivBackground = itemView.findViewById<ImageView>(R.id.ivBackground)
                ivAvatar = itemView.findViewById<ImageView>(R.id.ivAvatar)
                ivLock = itemView.findViewById<ImageView>(R.id.ivLock)
                tvRoomName = itemView.findViewById<TextView>(R.id.tvRoomName)
                tvUserName = itemView.findViewById<TextView>(R.id.tvUserName)
                tvPersonNum = itemView.findViewById<TextView>(R.id.tvPersonNum)
            }
        }
    }

}
package io.agora.scene.cantata.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import io.agora.scene.base.PagePathConstant
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.ISingleCallback
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.cantata.databinding.CantataActivityRoomListBinding
import io.agora.scene.cantata.databinding.CantataItemRoomListBinding
import io.agora.scene.cantata.service.CantataServiceProtocol
import io.agora.scene.cantata.service.JoinRoomOutputModel
import io.agora.scene.cantata.service.RoomListModel
import io.agora.scene.cantata.ui.holder.RoomHolder
import io.agora.scene.cantata.ui.viewmodel.RoomCreateViewModel
import io.agora.scene.widget.dialog.InputPasswordDialog
import io.agora.scene.widget.utils.UiUtils

@Route(path = PagePathConstant.pageGrandChorus)
class RoomListActivity : BaseViewBindingActivity<CantataActivityRoomListBinding>() {

    private val roomCreateViewModel by lazy {
        ViewModelProvider(this)[RoomCreateViewModel::class.java]
    }

    private var mAdapter: BaseRecyclerViewAdapter<CantataItemRoomListBinding, RoomListModel, RoomHolder>? = null

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
        mAdapter = BaseRecyclerViewAdapter(null, object : OnItemClickListener<RoomListModel> {

            override fun onItemClick(data: RoomListModel, view: View?, position: Int, viewType: Long) {
                if (data.isPrivate) {
                    showInputPwdDialog(data)
                } else {
                    // RoomManager.getInstance().setAgoraRoom(data);
                    if (!isJoining) {
                        isJoining = true
                        roomCreateViewModel.joinRoom(data.roomNo, null)
                    }
                }
            }

        }, RoomHolder::class.java)
        binding.rvRooms.layoutManager = GridLayoutManager(this, 2)
        binding.rvRooms.adapter = mAdapter
        binding.smartRefreshLayout.setEnableLoadMore(false)
        setOnApplyWindowInsetsListener(binding.root)
    }

    override fun initListener() {
        binding.btnCreateRoom.setOnClickListener { view ->
            if (UiUtils.isFastClick(2000)) {
                return@setOnClickListener
            }
            RoomCreateActivity.launch(this)
        }
        binding.btnCreateRoom2.setOnClickListener { view ->
            if (UiUtils.isFastClick(2000)) {
                return@setOnClickListener
            }
            RoomCreateActivity.launch(this)
        }
        roomCreateViewModel.roomModelList.observe(this) { vlRoomListModels: List<RoomListModel>? ->
            hideLoadingView()
            binding.smartRefreshLayout.finishRefresh()
            if (vlRoomListModels.isNullOrEmpty()) {
                binding.rvRooms.visibility = View.GONE
                binding.btnCreateRoom.visibility = View.GONE
                binding.tvTips1.visibility = View.VISIBLE
                binding.ivBgMobile.visibility = View.VISIBLE
                binding.btnCreateRoom2.visibility = View.VISIBLE
            } else {
                mAdapter!!.setDataList(vlRoomListModels)
                binding.rvRooms.visibility = View.VISIBLE
                binding.btnCreateRoom.visibility = View.VISIBLE
                binding.tvTips1.visibility = View.GONE
                binding.ivBgMobile.visibility = View.GONE
                binding.btnCreateRoom2.visibility = View.GONE
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
}
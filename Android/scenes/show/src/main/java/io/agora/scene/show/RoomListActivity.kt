package io.agora.scene.show

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.agora.scene.show.databinding.ShowRoomItemBinding
import io.agora.scene.show.databinding.ShowRoomListActivityBinding
import io.agora.scene.show.service.ShowRoomListModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : AppCompatActivity() {

    private val mBinding by lazy { ShowRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private lateinit var mRoomAdapter: BindingSingleAdapter<ShowRoomListModel, ShowRoomItemBinding>
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setDarkStatusIcon(window, true)
        setContentView(mBinding.root)
        initView()
    }

    private fun initView() {
        mBinding.titleView.setLeftClick { finish() }
        mRoomAdapter = object : BindingSingleAdapter<ShowRoomListModel, ShowRoomItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowRoomItemBinding>,
                position: Int
            ) {
                updateRoomItem(holder.binding, getItem(position))
            }
        }
        mBinding.rvRooms.adapter = mRoomAdapter

        mBinding.smartRefreshLayout.setEnableLoadMore(false)
        mBinding.smartRefreshLayout.setEnableRefresh(true)
        mBinding.smartRefreshLayout.setOnRefreshListener {
            mService.getRoomList({ runOnUiThread { updateList(it) } })
        }
        mBinding.smartRefreshLayout.autoRefresh()

        mBinding.btnCreateRoom.setOnClickListener { goLivePrepareActivity() }
        mBinding.btnCreateRoom2.setOnClickListener { goLivePrepareActivity() }
    }

    private fun updateList(data: List<ShowRoomListModel>) {
        mBinding.tvTips1.isVisible = data.isEmpty()
        mBinding.ivBgMobile.isVisible = data.isEmpty()
        mBinding.btnCreateRoom2.isVisible = data.isEmpty()
        mBinding.btnCreateRoom.isVisible = data.isNotEmpty()
        mBinding.rvRooms.isVisible = data.isNotEmpty()
        mRoomAdapter.resetAll(data)

        mBinding.smartRefreshLayout.finishRefresh()
    }

    private fun updateRoomItem(binding: ShowRoomItemBinding, roomInfo: ShowRoomListModel) {
        binding.tvRoomName.text = roomInfo.roomName
        binding.tvRoomId.text = getString(R.string.show_room_id, roomInfo.roomNo)
        binding.tvUserCount.text = getString(R.string.show_user_count, roomInfo.roomUserCount)
        binding.root.setOnClickListener {
            goLiveDetailActivity(roomInfo)
        }
    }

    private fun goLivePrepareActivity(){
        Intent(this, LivePrepareActivity::class.java).let {
            startActivity(it)
        }
    }

    private fun goLiveDetailActivity(roomInfo: ShowRoomListModel){
        Intent(this, LiveDetailActivity::class.java).let {
            it.putExtra("roomInfo", roomInfo)
            startActivity(it)
        }
    }
}
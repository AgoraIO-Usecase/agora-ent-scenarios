package io.agora.scene.show

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.agora.scene.show.databinding.ShowRoomItemBinding
import io.agora.scene.show.databinding.ShowRoomListActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.widget.AdvanceSettingDialog
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.StatusBarUtil

class RoomListActivity : AppCompatActivity() {

    private val mBinding by lazy { ShowRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private lateinit var mRoomAdapter: BindingSingleAdapter<ShowRoomDetailModel, ShowRoomItemBinding>
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, true)
        setContentView(mBinding.root)
        initView()
    }

    private fun initView() {
        mBinding.titleView.setLeftClick { finish() }
        mBinding.titleView.setRightIconClick {
            showAudioSetting()
        }
        mRoomAdapter = object : BindingSingleAdapter<ShowRoomDetailModel, ShowRoomItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowRoomItemBinding>,
                position: Int
            ) {
                updateRoomItem(holder.binding, getItem(position) ?: return)
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

    private fun updateList(data: List<ShowRoomDetailModel>) {
        mBinding.tvTips1.isVisible = data.isEmpty()
        mBinding.ivBgMobile.isVisible = data.isEmpty()
        mBinding.btnCreateRoom2.isVisible = data.isEmpty()
        mBinding.btnCreateRoom.isVisible = data.isNotEmpty()
        mBinding.rvRooms.isVisible = data.isNotEmpty()
        mRoomAdapter.resetAll(data)

        mBinding.smartRefreshLayout.finishRefresh()
    }

    private fun updateRoomItem(binding: ShowRoomItemBinding, roomInfo: ShowRoomDetailModel) {
        binding.tvRoomName.text = roomInfo.roomName
        binding.tvRoomId.text = getString(R.string.show_room_id, roomInfo.roomId)
        binding.tvUserCount.text = getString(R.string.show_user_count, roomInfo.roomUserCount)
        binding.ivCover.setImageResource(roomInfo.getThumbnailIcon())
        binding.root.setOnClickListener {
            goLiveDetailActivity(roomInfo)
        }
    }

    private fun goLivePrepareActivity(){
        Intent(this, LivePrepareActivity::class.java).let {
            startActivity(it)
        }
    }

    private fun goLiveDetailActivity(roomInfo: ShowRoomDetailModel){
        mService.joinRoom(roomInfo.roomId, {
            LiveDetailActivity.launch(this, it)
        })
    }

    private fun showAudioSetting(){
        AdvanceSettingDialog(this).apply {
            setShowPreset(false)
            hideAudioSetting()
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SWITCH_COLOR_ENHANCE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SWITCH_DARK_ENHANCE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SELECTOR_RESOLUTION, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SELECTOR_FRAMERATE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SEEKBAR_BITRATE, true)
            setOnSwitchChangeListener { _, itemId, isChecked ->
                when(itemId){
                    AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE -> {
                        RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":${isChecked}, \"mode\": 2}}")
                    }
                }
            }
            setOnDismissListener {
                StatusBarUtil.hideStatusBar(this@RoomListActivity.window,true)
            }
            show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngineInstance.destroy()
    }
}
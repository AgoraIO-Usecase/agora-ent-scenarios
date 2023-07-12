package io.agora.scene.show

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.agora.scene.base.Constant
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowRoomItemBinding
import io.agora.scene.show.databinding.ShowRoomListActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.widget.OnPresetAudienceDialogCallBack
import io.agora.scene.show.widget.PresetAudienceDialog
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
        fetchService()
        initView()
    }

    private fun initView() {
        mBinding.titleView.setLeftClick { finish() }
        mBinding.titleView.setRightIconClick {
            showAudienceSetting()
        }
        mRoomAdapter = object : BindingSingleAdapter<ShowRoomDetailModel, ShowRoomItemBinding>() {
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowRoomItemBinding>,
                position: Int
            ) {
                updateRoomItem(mDataList, position, holder.binding, getItem(position) ?: return)
            }
        }
        mBinding.rvRooms.adapter = mRoomAdapter

        mBinding.smartRefreshLayout.setEnableLoadMore(false)
        mBinding.smartRefreshLayout.setEnableRefresh(true)
        mBinding.smartRefreshLayout.setOnRefreshListener {
            mService.getRoomList(
                { runOnUiThread { updateList(it) } },
                { runOnUiThread { updateList(emptyList()) } })
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

    private fun updateRoomItem(list: List<ShowRoomDetailModel>, position: Int, binding: ShowRoomItemBinding, roomInfo: ShowRoomDetailModel) {
        binding.tvRoomName.text = roomInfo.roomName
        binding.tvRoomId.text = getString(R.string.show_room_id, roomInfo.roomId)
        binding.tvUserCount.text = getString(R.string.show_user_count, roomInfo.roomUserCount)
        binding.ivCover.setImageResource(roomInfo.getThumbnailIcon())
        binding.root.setOnClickListener {
            goLiveDetailActivity(list, position, roomInfo)
        }
    }

    private fun goLivePrepareActivity() {
        Intent(this, LivePrepareActivity::class.java).let {
            startActivity(it)
        }
    }

    private fun goLiveDetailActivity(list: List<ShowRoomDetailModel>, position: Int, roomInfo: ShowRoomDetailModel) {
        // 进房前设置一些必要的设置
        if (!SPUtil.getBoolean(Constant.IS_SET_SETTING, false)) {
            PresetAudienceDialog(this, false).apply {
                callBack = object : OnPresetAudienceDialogCallBack {
                    override fun onClickConfirm() {
                        SPUtil.putBoolean(Constant.IS_SET_SETTING, true)
                        goLiveDetailActivity(list, position, roomInfo)
                    }
                }
                show()
            }
            return
        }
        LiveDetailActivity.launch(this, ArrayList(list), position, roomInfo.ownerId != UserManager.getInstance().user.id.toString())
    }

    private fun showAudienceSetting() {
        PresetAudienceDialog(this).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mService.destroy()
        RtcEngineInstance.destroy()
        RtcEngineInstance.setupGeneralToken("")
    }


    private fun fetchService(){
        //启动机器人
        mService.startCloudPlayer()
        val localUId = UserManager.getInstance().user.id.toInt()
        //获取token
        TokenGenerator.generateToken("", localUId.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            TokenGenerator.AgoraTokenType.rtc,
            success = {
                RtcEngineInstance.setupGeneralToken(it)
                ShowLogger.d("RoomListActivity", "generateToken success：$it， uid：$localUId")
            },
            failure = {
                ShowLogger.e("RoomListActivity", it, "generateToken failure：$it")
                ToastUtils.showToast(it?.message?:"generate token failure")
            })
    }
}
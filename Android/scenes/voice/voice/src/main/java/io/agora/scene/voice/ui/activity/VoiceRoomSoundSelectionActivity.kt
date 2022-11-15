package io.agora.scene.voice.ui.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionConstructor.builderSoundSelectionList
import io.agora.scene.voice.VoiceConfigManager.getLifecycleCallbacks
import io.agora.scene.voice.ui.adapter.VoiceRoomSoundSelectionAdapter
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.baseui.utils.StatusBarCompat
import io.agora.voice.buddy.config.RouterParams
import io.agora.scene.voice.bean.SoundSelectionBean
import io.agora.scene.voice.service.VoiceRoomModel
import com.alibaba.android.arouter.launcher.ARouter
import io.agora.CallBack
import io.agora.scene.voice.R
import io.agora.voice.buddy.config.RouterPath
import io.agora.scene.voice.databinding.VoiceActivitySoundSelectionLayoutBinding
import io.agora.scene.voice.model.VoiceRoomViewModel
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.voice.baseui.BaseUiActivity
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.tool.FastClickTools
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.buddy.tool.ToastTools
import io.agora.voice.imkit.manager.ChatroomHelper

class VoiceRoomSoundSelectionActivity : BaseUiActivity<VoiceActivitySoundSelectionLayoutBinding>() {
    private var soundSelectAdapter: VoiceRoomSoundSelectionAdapter? = null
    private lateinit var voiceRoomViewModel: VoiceRoomViewModel
    private var isPublic = true
    private var roomName: String = ""
    private var encryption: String = ""
    private var roomType = 0
    private var soundEffect = ConfigConstants.SoundSelection.Social_Chat

    override fun getViewBinding(inflater: LayoutInflater): VoiceActivitySoundSelectionLayoutBinding {
        return VoiceActivitySoundSelectionLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarCompat.setLightStatusBar(this, true)
        super.onCreate(savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceRoomViewModel::class.java]
        initIntent()
        initAdapter()
        initListener()
        voiceRoomObservable()
    }

    private fun initIntent() {
        intent?.let {
            roomName = it.getStringExtra(RouterParams.KEY_CHATROOM_CREATE_NAME) ?: ""
            isPublic = it.getBooleanExtra(RouterParams.KEY_CHATROOM_CREATE_IS_PUBLIC, true)
            encryption = it.getStringExtra(RouterParams.KEY_CHATROOM_CREATE_ENCRYPTION) ?: ""
            roomType = it.getIntExtra(RouterParams.KEY_CHATROOM_CREATE_ROOM_TYPE, 0)
        }
    }

    private fun initAdapter() {
        val soundSelectionList: List<SoundSelectionBean> =
            builderSoundSelectionList(this, ConfigConstants.SoundSelection.Social_Chat)
        soundSelectAdapter =
            VoiceRoomSoundSelectionAdapter(soundSelectionList, object : OnItemClickListener<SoundSelectionBean> {
                override fun onItemClick(data: SoundSelectionBean, view: View, position: Int, viewType: Long) {
                    soundSelectAdapter?.setSelectedPosition(position)
                    soundEffect = data.soundSelectionType
                }
            }, VoiceRoomSoundSelectionAdapter.SoundSelectViewHolder::class.java)
        binding.list.layoutManager = LinearLayoutManager(this)
        val offsetPx = resources.getDimension(R.dimen.space_84dp)
        val bottomOffsetDecoration = BottomOffsetDecoration(offsetPx.toInt())
        binding.list.addItemDecoration(bottomOffsetDecoration)
        binding.list.adapter = soundSelectAdapter
    }

    private fun initListener() {
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.bottomLayout.setOnClickListener {
            if (roomType == 0) {
                if (FastClickTools.isFastClick(it)) return@setOnClickListener
                createNormalRoom(soundEffect)
            }
            //      else {
//         createSpatialRoom();
//      }
        }
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.createRoomObservable()
            .observe(this) { response: Resource<VoiceRoomModel> ->
                parseResource(response, object : OnResourceParseCallback<VoiceRoomModel?>() {
                    override fun onSuccess(voiceRoomModel: VoiceRoomModel?) {
                        val chatUsername = VoiceBuddyFactory.get().getVoiceBuddy().chatUsername()
                        val chatToken = VoiceBuddyFactory.get().getVoiceBuddy().chatToken()
                        "Voice create room chat_username:$chatUsername".logD()
                        "Voice create room im_token:$chatToken".logD()
                        ChatroomHelper.getInstance().login(chatUsername, chatToken, object : CallBack {
                            override fun onSuccess() {
                                // todo 设置kv
                                ThreadManager.getInstance().runOnMainThread {
                                    joinRoom(voiceRoomModel)
                                }
                            }

                            override fun onError(code: Int, desc: String) {
                                ThreadManager.getInstance().runOnMainThread {
                                    binding.bottomGoLive.isEnabled = true
                                    dismissLoading()
                                }

                            }
                        })
                    }
                })
            }
    }

    private fun createNormalRoom(sound_effect: Int) {
        showLoading(false)
        if (isPublic) {
            voiceRoomViewModel.createRoom(roomName, sound_effect, "")
        } else {
            if (!TextUtils.isEmpty(encryption) && encryption.length == 4) {
                voiceRoomViewModel.createRoom(roomName, sound_effect, encryption)
            } else {
                ToastTools.show(this, getString(R.string.voice_room_create_tips), Toast.LENGTH_LONG)
            }
        }
    }

    fun createSpatialRoom() {
        showLoading(false)
        if (isPublic) {
            voiceRoomViewModel.createSpatialRoom(roomName, 0, "")
        } else {
            if (!TextUtils.isEmpty(encryption) && encryption.length == 4) {
                voiceRoomViewModel.createSpatialRoom(roomName, 0, encryption)
            } else {
                ToastTools.show(this, getString(R.string.voice_room_create_tips), Toast.LENGTH_LONG)
            }
        }
    }

    fun joinRoom(voiceRoomModel: VoiceRoomModel?) {
        dismissLoading()
        // TODO:  zhangwei
        ToastTools.show(this,"TODO 房间详情")
//        ARouter.getInstance()
//            .build(RouterPath.ChatroomPath)
//            .withSerializable(RouterParams.KEY_VOICE_ROOM_MODEL, voiceRoomModel)
//            .navigation()
//        val intent = Intent(this, ChatroomLiveActivity::class.java).apply {
//            putExtra(RouterParams.KEY_VOICE_ROOM_MODEL, voiceRoomModel)
//        }
//        startActivity(intent)
//        finishCreateActivity()
//        finish()
    }

    /**
     * 结束创建activity
     */
    private fun finishCreateActivity() {
        val lifecycleCallbacks = getLifecycleCallbacks()
        val activities = lifecycleCallbacks.activityList
        if (activities == null || activities.isEmpty()) {
            finish()
            return
        }
        for (activity in activities) {
            if (activity !== lifecycleCallbacks.current() && activity is ChatroomCreateActivity) {
                activity.finish()
            }
        }
    }

    internal class BottomOffsetDecoration(private val mBottomOffset: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val dataSize = state.itemCount
            val position = parent.getChildAdapterPosition(view)
            if (dataSize > 0 && position == dataSize - 1) {
                outRect[0, 0, 0] = mBottomOffset
            } else {
                outRect[0, 0, 0] = 0
            }
        }
    }
}
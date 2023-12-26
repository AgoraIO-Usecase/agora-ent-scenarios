package io.agora.scene.voice.ui.activity

import android.app.Activity
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
import io.agora.CallBack
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceActivitySoundSelectionLayoutBinding
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.SoundSelectionBean
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.model.constructor.RoomSoundSelectionConstructor.builderSoundSelectionList
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.adapter.VoiceRoomSoundSelectionAdapter
import io.agora.scene.voice.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.BaseUiActivity
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.FastClickTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.StatusBarCompat
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools

class VoiceRoomSoundSelectionActivity : BaseUiActivity<VoiceActivitySoundSelectionLayoutBinding>() {

    companion object {
        const val KEY_CHATROOM_CREATE_NAME = "chatroom_create_name"
        const val KEY_CHATROOM_CREATE_IS_PUBLIC = "chatroom_create_is_public"
        const val KEY_CHATROOM_CREATE_ENCRYPTION = "chatroom_create_encryption"
        const val KEY_CHATROOM_CREATE_ROOM_TYPE = "chatroom_create_room_type"

        fun startActivity(activity: Activity, roomName: String, isPublic: Boolean, encryption: String, roomType: Int) {
            val intent = Intent(activity, VoiceRoomSoundSelectionActivity::class.java).apply {
                putExtra(KEY_CHATROOM_CREATE_NAME, roomName)
                putExtra(KEY_CHATROOM_CREATE_IS_PUBLIC, isPublic)
                if (!isPublic) {
                    putExtra(KEY_CHATROOM_CREATE_ENCRYPTION, encryption)
                }
                putExtra(KEY_CHATROOM_CREATE_ROOM_TYPE, roomType)
            }
            activity.startActivity(intent)
        }
    }

    private var soundSelectAdapter: VoiceRoomSoundSelectionAdapter? = null
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var isPublic = true
    private var roomName: String = ""
    private var encryption: String = ""
    private var roomType = 0
    private var soundEffect = ConfigConstants.SoundSelection.Social_Chat
    private var curVoiceRoomModel: VoiceRoomModel? = null

    override fun getViewBinding(inflater: LayoutInflater): VoiceActivitySoundSelectionLayoutBinding {
        return VoiceActivitySoundSelectionLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarCompat.setLightStatusBar(this, true)
        super.onCreate(savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        initIntent()
        initAdapter()
        initListener()
        voiceRoomObservable()
    }

    private fun initIntent() {
        intent?.let {
            roomName = it.getStringExtra(KEY_CHATROOM_CREATE_NAME) ?: ""
            isPublic = it.getBooleanExtra(KEY_CHATROOM_CREATE_IS_PUBLIC, true)
            encryption = it.getStringExtra(KEY_CHATROOM_CREATE_ENCRYPTION) ?: ""
            roomType = it.getIntExtra(KEY_CHATROOM_CREATE_ROOM_TYPE, 0)
        }
    }

    private fun initAdapter() {
        val soundSelectionList: List<SoundSelectionBean> =
            builderSoundSelectionList(this, ConfigConstants.SoundSelection.Social_Chat)
        soundSelectAdapter =
            VoiceRoomSoundSelectionAdapter(soundSelectionList, object :
                OnItemClickListener<SoundSelectionBean> {
                override fun onItemClick(data: SoundSelectionBean, view: View, position: Int, viewType: Long) {
                    soundSelectAdapter?.setSelectedPosition(position)
                    soundEffect = data.soundSelectionType
                }
            }, VoiceRoomSoundSelectionAdapter.SoundSelectViewHolder::class.java)
        binding.list.layoutManager = LinearLayoutManager(this)
        val offsetPx = resources.getDimension(R.dimen.voice_space_84dp)
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
                checkPrivate(soundEffect)
            }
            //      else {
//         createSpatialRoom();
//      }
        }
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.createRoomObservable().observe(this) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel>() {
                override fun onSuccess(voiceRoomModel: VoiceRoomModel?) {
                    curVoiceRoomModel = voiceRoomModel
                    "voiceRoomObservable memberCount: ${voiceRoomModel?.memberCount} ".logD()
                    voiceRoomModel?.let {
                        voiceRoomViewModel.joinRoom(it.roomId)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    dismissLoading()
                    when (code) {
                        VoiceServiceProtocol.ERR_LOGIN_ERROR -> {
                            ToastTools.show(this@VoiceRoomSoundSelectionActivity, getString(R.string.voice_room_login_exception))
                        }
                        VoiceServiceProtocol.ERR_ROOM_NAME_INCORRECT -> {
                            ToastTools.show(this@VoiceRoomSoundSelectionActivity, getString(R.string.voice_room_name_rule))
                        }
                        else -> {
                            ToastTools.show(this@VoiceRoomSoundSelectionActivity, getString(R.string.voice_room_create_error))
                        }
                    }
                }
            })
        }
        voiceRoomViewModel.joinRoomObservable().observe(this) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel?>() {
                override fun onSuccess(result: VoiceRoomModel?) {
                    curVoiceRoomModel = result ?: return
                    ThreadManager.getInstance().runOnMainThread {
                        goVoiceRoom()
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    dismissLoading()
                    "SoundSelectionActivity syncJoinRoom fail:$code $message".logD()
                }
            })
        }
    }

    private fun checkPrivate(sound_effect: Int){
        showLoading(false)
        if (isPublic) {
            if (ChatroomIMManager.getInstance().isLoggedIn){
                voiceRoomViewModel.createRoom(roomName, sound_effect, "")
            }else{
                ChatroomIMManager.getInstance().login(
                    VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(),
                    VoiceBuddyFactory.get().getVoiceBuddy().chatToken(), object : CallBack {
                    override fun onSuccess() {
                        voiceRoomViewModel.createRoom(roomName, sound_effect, "")
                    }

                    override fun onError(code: Int, desc: String) {
                        dismissLoading()
                        "checkPrivate login error code:$code,msg:$desc".logE()
                    }
                })
            }
        } else {
            if (!TextUtils.isEmpty(encryption) && encryption.length == 4) {
                voiceRoomViewModel.createRoom(roomName, sound_effect, encryption)
            } else {
                dismissLoading()
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

    private fun goVoiceRoom() {
        curVoiceRoomModel?.let {
            dismissLoading()
            "apex-wt VoiceRoomSoundSelectionActivity :${it.memberCount}".logD()
            ChatroomLiveActivity.startActivity(this, it)
            finish()
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
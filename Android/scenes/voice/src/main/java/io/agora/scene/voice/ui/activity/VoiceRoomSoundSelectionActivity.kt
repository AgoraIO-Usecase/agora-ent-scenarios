package io.agora.scene.voice.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceActivitySoundSelectionLayoutBinding
import io.agora.scene.voice.model.SoundSelectionBean
import io.agora.scene.voice.model.constructor.RoomSoundSelectionConstructor.builderSoundSelectionList
import io.agora.scene.voice.ui.adapter.VoiceRoomSoundSelectionAdapter
import io.agora.scene.voice.viewmodel.VoiceCreateViewModel
import io.agora.scene.widget.utils.UiUtils
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.widget.utils.StatusBarUtil

class VoiceRoomSoundSelectionActivity : BaseViewBindingActivity<VoiceActivitySoundSelectionLayoutBinding>() {

    companion object {
        const val KEY_CHATROOM_CREATE_NAME = "chatroom_create_name"
        const val KEY_CHATROOM_CREATE_IS_PUBLIC = "chatroom_create_is_public"
        const val KEY_CHATROOM_CREATE_ENCRYPTION = "chatroom_create_encryption"

        fun startActivity(activity: Activity, roomName: String, isPublic: Boolean, encryption: String) {
            val intent = Intent(activity, VoiceRoomSoundSelectionActivity::class.java).apply {
                putExtra(KEY_CHATROOM_CREATE_NAME, roomName)
                putExtra(KEY_CHATROOM_CREATE_IS_PUBLIC, isPublic)
                if (!isPublic) {
                    putExtra(KEY_CHATROOM_CREATE_ENCRYPTION, encryption)
                }
            }
            activity.startActivity(intent)
        }
    }

    private val soundSelectAdapter: VoiceRoomSoundSelectionAdapter by lazy {
        VoiceRoomSoundSelectionAdapter(
            builderSoundSelectionList(this, ConfigConstants.SoundSelection.Social_Chat),
            listener = object :
                OnItemClickListener<SoundSelectionBean> {
                override fun onItemClick(data: SoundSelectionBean, view: View, position: Int, viewType: Long) {
                    soundSelectAdapter.setSelectedPosition(position)
                    soundEffect = data.soundSelectionType
                }
            },
            VoiceRoomSoundSelectionAdapter.SoundSelectViewHolder::class.java
        )
    }
    private val voiceRoomViewModel: VoiceCreateViewModel by lazy {
        ViewModelProvider(this)[VoiceCreateViewModel::class.java]
    }
    private val isPublic by lazy {
        intent?.getBooleanExtra(KEY_CHATROOM_CREATE_IS_PUBLIC, true) ?: true
    }
    private val roomName: String by lazy {
        intent?.getStringExtra(KEY_CHATROOM_CREATE_NAME) ?: ""
    }
    private val encryption: String by lazy {
        intent?.getStringExtra(KEY_CHATROOM_CREATE_ENCRYPTION) ?: ""
    }
    private var soundEffect = ConfigConstants.SoundSelection.Social_Chat

    override fun getViewBinding(inflater: LayoutInflater): VoiceActivitySoundSelectionLayoutBinding {
        return VoiceActivitySoundSelectionLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.hideStatusBar(window, true)
        super.onCreate(savedInstanceState)
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.bottomLayout.setOnClickListener {
            if (UiUtils.isFastClick()) return@setOnClickListener
            checkPrivate(soundEffect)
        }
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.addItemDecoration(BottomOffsetDecoration(84.dp.toInt()))
        binding.list.adapter = soundSelectAdapter

        voiceRoomViewModel.createRoomObservable.observe(this) { roomInfo: AUIRoomInfo? ->
            hideLoadingView()
            roomInfo ?: return@observe
            gotoChatRoom(roomInfo)
        }
    }

    private fun checkPrivate(soundEffect: Int) {
        showLoadingView()
        if (isPublic) {
            voiceRoomViewModel.createRoom(roomName, soundEffect, "")
        } else {
            if (!TextUtils.isEmpty(encryption) && encryption.length == 4) {
                voiceRoomViewModel.createRoom(roomName, soundEffect, encryption)
            } else {
                hideLoadingView()
                ToastUtils.showToast(getString(R.string.voice_room_create_tips))
            }
        }
    }

    private fun gotoChatRoom(roomInfo: AUIRoomInfo) {
        hideLoadingView()
        ChatroomLiveActivity.startActivity(this, roomInfo)
        finish()
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
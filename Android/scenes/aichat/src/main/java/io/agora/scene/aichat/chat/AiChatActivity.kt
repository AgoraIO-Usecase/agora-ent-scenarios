package io.agora.scene.aichat.chat

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.agora.scene.aichat.databinding.AichatChatActivityBinding
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.widget.EaseChatPrimaryMenuListener
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.dp
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.StatusBarUtil

class AiChatActivity : BaseViewBindingActivity<AichatChatActivityBinding>() {

    companion object {

        private val TAG = AiChatActivity::class.java.simpleName

        const val EXTRA_CHAT_TYPE = "chatType"
        const val EXTRA_CONVERSATION_ID = "conversationId"

        fun start(context: Context, conversationId: String, chatType: ChatType) {
            Intent(context, AiChatActivity::class.java).apply {
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
                putExtra(EXTRA_CHAT_TYPE, chatType.ordinal)
                context.startActivity(this)
            }
        }
    }

    private var toggleAudioRun: Runnable? = null

    fun toggleSelfAudio(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleAudioRun = Runnable {
                callback.invoke()
            }
            requestRecordPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun getPermissions() {
        toggleAudioRun?.let {
            it.run()
            toggleAudioRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) {
            launchAppSetting(permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener(binding.rootView)

        binding.rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = binding.rootView.height
            val keypadHeight = screenHeight - rect.bottom - binding.rootView.paddingBottom

            if (keypadHeight > screenHeight * 0.15) {
                binding.chatChatInputMenu.translationY = -keypadHeight.toFloat()
            } else {
                binding.chatChatInputMenu.translationY = 0f
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): AichatChatActivityBinding {
        return AichatChatActivityBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initListener() {
        super.initListener()
        binding.titleView.setBackClickListener {
            finish()
        }
        binding.chatChatInputMenu.setEaseChatPrimaryMenuListener(object : EaseChatPrimaryMenuListener {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {

                return false
            }

            override fun onSendBtnClicked(content: String?) {
                CustomToast.show("onCallBtnClicked")
            }

            override fun onCallBtnClicked() {
                CustomToast.show("onCallBtnClicked")
            }

            override fun onToggleVoiceBtnClicked() {
                CustomToast.show("onToggleVoiceBtnClicked")
            }

            override fun onToggleTextBtnClicked() {
                CustomToast.show("onToggleTextBtnClicked")
            }

            override fun onEditTextHasFocus(hasFocus: Boolean) {
                CustomToast.show("onEditTextHasFocus $hasFocus")
            }
        })
    }
}
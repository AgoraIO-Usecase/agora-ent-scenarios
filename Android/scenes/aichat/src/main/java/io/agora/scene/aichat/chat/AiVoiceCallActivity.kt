package io.agora.scene.aichat.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatActivityVoiceCallBinding
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.widget.dialog.PermissionLeakDialog

class AiVoiceCallActivity : BaseViewBindingActivity<AichatActivityVoiceCallBinding>() {

    companion object {

        private val TAG = AiVoiceCallActivity::class.java.simpleName

        const val VOICE_CALL_TYPE = "VOICE_CALL_TYPE"

        const val EXTRA_CONVERSATION_ID = "conversationId"

        fun start(context: Context, conversationId: String) {
            Intent(context, AiVoiceCallActivity::class.java).apply {
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
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

    override fun getViewBinding(inflater: LayoutInflater): AichatActivityVoiceCallBinding {
        return AichatActivityVoiceCallBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: ""
        if (conversationId.isEmpty()) {
            finish()
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun initListener() {
        val navController = findNavController(R.id.nav_host_voice_call)
        val graph = navController.createGraph(startDestination = VOICE_CALL_TYPE) {
            fragment<AiChatVoiceCallFragment>(VOICE_CALL_TYPE) {}
        }
        // 传递参数并导航到 startDestination
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: ""
        val bundle = bundleOf(EXTRA_CONVERSATION_ID to conversationId)
        navController.setGraph(graph, bundle)
    }
}
package io.agora.scene.aichat.create

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatActivityAgentCreateBinding
import io.agora.scene.base.component.BaseViewBindingActivity

class AiChatAgentCreateActivity : BaseViewBindingActivity<AichatActivityAgentCreateBinding>() {

    companion object {
        const val CREATE_TYPE = "CREATE_AGENT_TYPE"
        const val PREVIEW_TYPE = "PREVIEW_AVATAR_TYPE"

        fun start(context: Context) {
            context.startActivity(Intent(context, AiChatAgentCreateActivity::class.java))
        }
    }

    override fun getViewBinding(inflater: LayoutInflater?): AichatActivityAgentCreateBinding {
        val binding = AichatActivityAgentCreateBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        return binding
    }

    override fun initListener() {
        val navController = findNavController(R.id.nav_host_agent_create)
        navController.graph = navController.createGraph(
            startDestination = CREATE_TYPE
        ) {
            fragment<AiChatAgentCreateFragment>(CREATE_TYPE) {

            }
            fragment<AiChatAgentCreatePreviewFragment>(PREVIEW_TYPE) {

            }
        }
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.nav_host_agent_create).popBackStack()) {
            finish()
        }
    }
}
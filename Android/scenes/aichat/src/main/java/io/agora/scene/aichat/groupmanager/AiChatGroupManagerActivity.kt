package io.agora.scene.aichat.groupmanager

import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatActivityGroupManagerBinding
import io.agora.scene.base.component.BaseViewBindingActivity

class AiChatGroupManagerActivity : BaseViewBindingActivity<AichatActivityGroupManagerBinding>() {

    companion object {
        const val DETAIL_TYPE = "DETAIL_TYPE"
        const val EDIT_TYPE = "EDIT_TYPE"
        const val ADD_TYPE = "ADD_TYPE"
        const val DELETE_TYPE = "DELETE_TYPE"

        const val EXTRA_CONVERSATION_ID = "conversationId"
    }

    override fun getViewBinding(inflater: LayoutInflater?): AichatActivityGroupManagerBinding {
        val binding = AichatActivityGroupManagerBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        return binding
    }

    override fun initListener() {
        val navController = findNavController(R.id.nav_host_group_manager)
        val graph = navController.createGraph(startDestination = DETAIL_TYPE) {
            fragment<AiChatGroupManagerFragment>(DETAIL_TYPE) {}
            fragment<AiChatGroupEditFragment>(EDIT_TYPE) {}
            fragment<AiChatGroupAddFragment>(ADD_TYPE) {}
            fragment<AiChatGroupDeleteFragment>(DELETE_TYPE) {}
        }
        // 传递参数并导航到 startDestination
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: ""
        val bundle = bundleOf(EXTRA_CONVERSATION_ID to conversationId)
        navController.setGraph(graph, bundle)
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.nav_host_group_manager).popBackStack()) {
            setResult(RESULT_OK)
            finish()
        }
    }
}
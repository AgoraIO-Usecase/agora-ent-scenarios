package io.agora.scene.aichat.create

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import io.agora.scene.aichat.R
import io.agora.scene.aichat.create.logic.AiChatRoomCreateViewModel
import io.agora.scene.aichat.databinding.AichatActivityRoomCreateBinding
import io.agora.scene.base.component.BaseViewBindingActivity

class AiChatRoomCreateActivity : BaseViewBindingActivity<AichatActivityRoomCreateBinding>() {

    companion object {
        const val CREATE_TYPE = "CREATE_TYPE"
        const val SELECT_TYPE = "SELECT_TYPE"
    }

    private val vm by viewModels<AiChatRoomCreateViewModel>()

    override fun getViewBinding(inflater: LayoutInflater?): AichatActivityRoomCreateBinding {
        val binding = AichatActivityRoomCreateBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        return binding
    }

    override fun initListener() {
        val navController = findNavController(R.id.nav_host_room_create)
        navController.graph = navController.createGraph(
            startDestination = CREATE_TYPE
        ) {
            fragment<AiChatRoomCreateFragment>(CREATE_TYPE) {

            }
            fragment<AiChatRoomAgentSelectFragment>(SELECT_TYPE) {

            }
        }
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.nav_host_room_create).popBackStack()) {
            finish()
        }
    }
}
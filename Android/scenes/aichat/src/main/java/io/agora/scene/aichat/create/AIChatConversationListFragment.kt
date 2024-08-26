package io.agora.scene.aichat.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.agora.scene.aichat.create.logic.AIAgentViewModel
import io.agora.scene.aichat.databinding.AichatConversationListFragmentBinding
import io.agora.scene.base.component.BaseViewBindingFragment

/**
 * 会话列表页面
 */
class AIChatConversationListFragment : BaseViewBindingFragment<AichatConversationListFragmentBinding>() {

    //viewModel
    private val aiAgentViewModel: AIAgentViewModel by viewModels()

    companion object {

        fun newInstance() = AIChatConversationListFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatConversationListFragmentBinding {
        return AichatConversationListFragmentBinding.inflate(inflater)
    }
}
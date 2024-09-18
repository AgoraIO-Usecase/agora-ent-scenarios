package io.agora.scene.aichat.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import io.agora.scene.aichat.create.logic.AiChatAgentCreateViewModel
import io.agora.scene.aichat.databinding.AichatCreateAgentPreviewFragmentBinding
import io.agora.scene.aichat.ext.loadImage
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatAgentCreatePreviewFragment : BaseViewBindingFragment<AichatCreateAgentPreviewFragmentBinding>() {

    //viewModel
    private val aiCreateAgentViewModel: AiChatAgentCreateViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // nothing 屏蔽返回键
            }
        })
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatCreateAgentPreviewFragmentBinding {
        return AichatCreateAgentPreviewFragmentBinding.inflate(inflater, container, false)
    }

    override fun initView() {
//        ViewCompat.setOnApplyWindowInsetsListener(binding.ivBackIcon) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(
//                binding.ivBackIcon.left,
//                binding.ivBackIcon.top + systemBars.top,
//                binding.ivBackIcon.right,
//                binding.ivBackIcon.bottom
//            )
//            insets
//        }
        binding.ivBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnChangeAvatar.setOnClickListener {
            aiCreateAgentViewModel.randomAvatar()
        }
    }

    override fun initListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                aiCreateAgentViewModel.curPreviewAvatar.collectLatest { preview ->
                    binding.ivBackground.loadImage(preview.background)
                }
            }
        }
    }
}
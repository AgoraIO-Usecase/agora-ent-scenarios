package io.agora.scene.aichat.groupmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.AiChatActivity
import io.agora.scene.aichat.create.AiChatGroupCreateFragment.Companion.MAX_CHAR_COUNT
import io.agora.scene.aichat.databinding.AichatFragmentGroupEditBinding
import io.agora.scene.aichat.groupmanager.logic.AIChatGroupManagerViewModel
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.launch

class AiChatGroupEditFragment : BaseViewBindingFragment<AichatFragmentGroupEditBinding>() {

    private val mGroupViewModel: AIChatGroupManagerViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = arguments?.getString(AiChatActivity.EXTRA_CONVERSATION_ID) ?: ""
                return AIChatGroupManagerViewModel(conversationId) as T
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentGroupEditBinding {
        return AichatFragmentGroupEditBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        binding.etGroupName.setText(mGroupViewModel.getChatName())
        binding.etGroupName.addTextChangedListener {
            val text = it?.toString() ?: ""
            binding.tvLeftCountNum.text = "${(it?.length ?: 0)}/$MAX_CHAR_COUNT"

            binding.tvConfirm.isEnabled = text.isNotEmpty()
            activity?.let { context ->
                val update = text.isNotEmpty() && text != mGroupViewModel.getChatName()
                binding.tvConfirm.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (update) R.color.aichat_text_blue_00 else R.color.def_text_grey_979
                    )
                )
            }

        }
        binding.tvBackTitle.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvConfirm.setOnClickListener {
            val groupName = binding.etGroupName.text.toString()
            if (groupName.isEmpty()) {
                CustomToast.show(getString(R.string.aichat_input_chat_group_name))
                return@setOnClickListener
            }
            mGroupViewModel.editGroupName(binding.etGroupName.text.toString())
        }
        lifecycleScope.launch {
            mGroupViewModel.updateGroupLiveData.observe(this@AiChatGroupEditFragment) { isSuccess ->
                if (isSuccess) {
                    CustomToast.show(R.string.aichat_already_save)
                    findNavController().navigateUp()
                }
            }
        }
        mGroupViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        mGroupViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }
    }
}
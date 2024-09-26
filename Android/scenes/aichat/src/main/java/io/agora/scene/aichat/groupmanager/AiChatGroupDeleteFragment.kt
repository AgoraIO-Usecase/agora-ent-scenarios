package io.agora.scene.aichat.groupmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.AiChatActivity
import io.agora.scene.aichat.create.QuickAdapter
import io.agora.scene.aichat.create.logic.ContactItem
import io.agora.scene.aichat.databinding.AichatFragmentGroupDeleteBuddyBinding
import io.agora.scene.aichat.databinding.AichatItemContactSelectBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.groupmanager.logic.AIChatGroupManagerViewModel
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatGroupDeleteFragment : BaseViewBindingFragment<AichatFragmentGroupDeleteBuddyBinding>() {

    private val mGroupViewModel: AIChatGroupManagerViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = arguments?.getString(AiChatActivity.EXTRA_CONVERSATION_ID) ?: ""
                return AIChatGroupManagerViewModel(conversationId) as T
            }
        }
    }

    private val rvDatas by lazy { mutableListOf<ContactItem>() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatFragmentGroupDeleteBuddyBinding {
        return AichatFragmentGroupDeleteBuddyBinding.inflate(inflater, container, false)
    }

    private val rvAdapter by lazy {
        object : QuickAdapter<AichatItemContactSelectBinding, ContactItem>(
            AichatItemContactSelectBinding::inflate,
            rvDatas
        ) {
            override fun onBind(
                binding: AichatItemContactSelectBinding,
                datas: List<ContactItem>,
                position: Int
            ) {
                val item = datas[position]
                binding.tvName.text = item.name
                binding.ivIcon.loadCircleImage(item.avatar ?: "")
                if (item.isCheck) {
                    binding.ivCheck.setImageResource(R.drawable.checked_on)
                } else {
                    binding.ivCheck.setImageResource(R.drawable.checked_off)
                }
            }
        }
    }

    override fun requestData() {
        super.requestData()
        mGroupViewModel.fetchDeleteContacts()
    }

    override fun initListener() {
        binding.tvBackTitle.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvConfirmSelect.setOnClickListener {
            mGroupViewModel.editDeleteGroupAgent(rvDatas.filter { !it.isCheck })
        }
        rvAdapter.onItemClickListener = { datas, position ->
            val item = datas[position]
            mGroupViewModel.updateDeleteContactByKey(item.userId, !item.isCheck)
        }
        binding.rv.adapter = rvAdapter
        //监听删除按钮的数据驱动
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mGroupViewModel.canDeleteContacts.observe(viewLifecycleOwner) {
                    rvDatas.clear()
                    rvDatas.addAll(it)
                    rvAdapter.notifyDataSetChanged()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mGroupViewModel.selectDeleteDatas.observe(viewLifecycleOwner) {
                    binding.tvConfirmSelect.apply {
                        val selectCount = it.count { item -> item.isCheck }
                        if (selectCount <= 0) {
                            isEnabled = false
                            setTextColor(ContextCompat.getColor(context, R.color.def_text_grey_979))
                        } else {
                            isEnabled = true
                            setTextColor(
                                ContextCompat.getColor(context, R.color.aichat_text_blue_00)
                            )
                        }
                        text = getString(R.string.aichat_delete) + "(${selectCount})"
                    }
                }
            }
        }
        lifecycleScope.launch {
            mGroupViewModel.deleteGroupAgentLiveData.observe(viewLifecycleOwner) { isSuccess ->
                if (isSuccess) {
                    findNavController().navigateUp()
                }
            }
        }
        mGroupViewModel.loadingChange.showDialog.observe(viewLifecycleOwner) {
            showLoadingView()
        }
        mGroupViewModel.loadingChange.dismissDialog.observe(viewLifecycleOwner) {
            hideLoadingView()
        }
    }
}
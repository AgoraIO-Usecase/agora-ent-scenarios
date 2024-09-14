package io.agora.scene.aichat.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.AiChatActivity
import io.agora.scene.aichat.create.logic.AiChatRoomCreateViewModel
import io.agora.scene.aichat.create.logic.ContactItem
import io.agora.scene.aichat.create.logic.toProfile
import io.agora.scene.aichat.databinding.AichatFragmentRoomCreateBinding
import io.agora.scene.aichat.databinding.AichatItemChatGroupCreateBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.list.logic.AIConversationViewModel
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatRoomCreateFragment : BaseViewBindingFragment<AichatFragmentRoomCreateBinding>() {
    companion object {
        const val MAX_CHAR_COUNT = 32
    }

    //viewModel
    private val mConversationViewModel: AIConversationViewModel by viewModels()

    private val vm by activityViewModels<AiChatRoomCreateViewModel>()
    private lateinit var layoutManager: GridLayoutManager
    private val selectUserDatas by lazy { mutableListOf<ContactItem>() }
    private val adapter by lazy {
        object : QuickAdapter<AichatItemChatGroupCreateBinding, ContactItem>(
            AichatItemChatGroupCreateBinding::inflate, selectUserDatas
        ) {
            override fun onBind(
                binding: AichatItemChatGroupCreateBinding,
                datas: List<ContactItem>,
                position: Int
            ) {
                val item = datas[position]
                when (position) {
                    0 -> {
                        binding.iv.visibility = View.VISIBLE
                        binding.ivDelete.visibility = View.GONE
                        binding.iv.loadCircleImage(item.avatar ?: "")
                        binding.tv.text = item.name
                    }

                    datas.size - 1 -> {
                        binding.iv.visibility = View.GONE
                        binding.ivDelete.visibility = View.GONE
                        binding.tv.text = getString(R.string.aichat_add_agent)
                    }

                    else -> {
                        binding.iv.visibility = View.VISIBLE
                        binding.ivDelete.visibility = View.VISIBLE
                        binding.iv.loadCircleImage(item.avatar ?: "")
                        binding.tv.text = item.name
                    }
                }

            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatFragmentRoomCreateBinding {
        return AichatFragmentRoomCreateBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        layoutManager = GridLayoutManager(requireContext(), 4)
        adapter.onItemClickListener = { datas, position ->
            when (position) {
                0 -> {
                    //如果是自己啥也不做
                }

                datas.size - 1 -> {
                    //跳转到智能体选择也没
                    findNavController().navigate(AiChatRoomCreateActivity.SELECT_TYPE)
                }

                else -> {
                    //删除
                    val item = datas[position]
                    item.isCheck = false
                    vm.updateContactByKey(item.userId, false)
                }
            }
        }
        binding.rv.let {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
        binding.etGroupName.addTextChangedListener {
            binding.tvLeftCountNum.text = "${MAX_CHAR_COUNT - (it?.length ?: 0)}/$MAX_CHAR_COUNT"
        }
    }

    override fun initListener() {
        binding.btnCreateAgent.setOnClickListener {
            val groupName = binding.etGroupName.text.toString()
            if (groupName.isEmpty()) {
                CustomToast.show(getString(R.string.aichat_input_chat_group_name))
                return@setOnClickListener
            }
            val realSelectUserList = selectUserDatas.filter {
                it.userId.isNotEmpty() && it.userId != EaseIM.getCurrentUser().id
            }
            if (realSelectUserList.isEmpty()) {
                CustomToast.show(getString(R.string.aichat_chat_group_select_atleast_desc))
                return@setOnClickListener
            }
            mConversationViewModel.createGroup(groupName, realSelectUserList.map { it.toProfile() })
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.selectDatas.collectLatest {
                    selectUserDatas.clear()
                    selectUserDatas.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        mConversationViewModel.createGroupLiveData.observe(this) { createGroupName ->
            if (createGroupName.isNotEmpty()) {
                activity?.let {
                    AiChatActivity.start(it, createGroupName)
                    it.finishAffinity()
                }
            }
        }
    }
}
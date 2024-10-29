package io.agora.scene.aichat.create

import android.app.Activity
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
import io.agora.scene.aichat.create.logic.AiChatGroupCreateViewModel
import io.agora.scene.aichat.create.logic.ContactItem
import io.agora.scene.aichat.create.logic.toProfile
import io.agora.scene.aichat.databinding.AichatFragmentGroupCreateBinding
import io.agora.scene.aichat.databinding.AichatItemChatGroupCreateBinding
import io.agora.scene.aichat.ext.hideSoftKeyboard
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.mainScope
import io.agora.scene.aichat.imkit.EaseFlowBus
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseEvent
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatGroupCreateFragment : BaseViewBindingFragment<AichatFragmentGroupCreateBinding>() {
    companion object {
        const val MAX_CHAR_COUNT = 32
    }

    private val mViewModel by activityViewModels<AiChatGroupCreateViewModel>()
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
                if (position == 0) {
                    binding.iv.visibility = View.VISIBLE
                    binding.ivDelete.visibility = View.GONE
                    binding.iv.loadCircleImage(item.avatar ?: "")
                    binding.tv.text = item.name
                } else {
                    if (item.name == "placeholder" && position == selectUserDatas.size - 1) {
                        binding.iv.visibility = View.GONE
                        binding.ivDelete.visibility = View.GONE
                        binding.tv.text = getString(R.string.aichat_add_agent)
                    } else {
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
    ): AichatFragmentGroupCreateBinding {
        return AichatFragmentGroupCreateBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.tvMaxAgents.text =
            getString(R.string.aichat_group_create_desc, AiChatGroupCreateViewModel.MAX_SELECT_COUNT)
        adapter.onItemClickListener = { datas, position ->
            if (position == 0) {
                // nothing 如果是自己啥也不做
            } else {
                val item = datas[position]
                if (item.name == "placeholder" && position == selectUserDatas.size - 1) {
                    //跳转到智能体选择选择
                    findNavController().navigate(AiChatGroupCreateActivity.SELECT_TYPE)
                } else {
                    //删除
                    item.isCheck = false
                    mViewModel.updateContactByKey(item.userId, false)
                }
            }
        }
        binding.rv.adapter = adapter
        binding.etGroupName.addTextChangedListener {
            binding.tvLeftCountNum.text = "${(it?.length ?: 0)}/$MAX_CHAR_COUNT"
        }
        binding.root.setOnClickListener {
            if (context is Activity) {
                val activity = context as Activity
                binding.etGroupName.hideSoftKeyboard(activity)
            }
        }
    }

    override fun initListener() {
        binding.cvAichatCreate.setOnClickListener {
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
            mViewModel.createGroup(groupName, realSelectUserList.map { it.toProfile() })
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.selectDatas.collectLatest {
                    selectUserDatas.clear()
                    selectUserDatas.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        mViewModel.createGroupLiveData.observe(viewLifecycleOwner) { createGroupName ->
            if (createGroupName.isNotEmpty()) {
                activity?.apply {
                    EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.REMOVE.name)
                        .post(this.mainScope(), EaseEvent(EaseEvent.EVENT.REMOVE.name, EaseEvent.TYPE.CONTACT))
                    AiChatActivity.start(this, createGroupName)
                    this.finish()
                }
            }
        }

        mViewModel.loadingChange.showDialog.observe(viewLifecycleOwner) {
            showLoadingView()
        }
        mViewModel.loadingChange.dismissDialog.observe(viewLifecycleOwner) {
            hideLoadingView()
        }
    }
}
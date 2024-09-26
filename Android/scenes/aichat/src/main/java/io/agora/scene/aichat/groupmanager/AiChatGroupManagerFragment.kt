package io.agora.scene.aichat.groupmanager

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.AiChatActivity
import io.agora.scene.aichat.create.QuickAdapter
import io.agora.scene.aichat.create.logic.AiChatGroupCreateViewModel
import io.agora.scene.aichat.create.logic.ContactItem
import io.agora.scene.aichat.databinding.AichatFragmentGroupDetailBinding
import io.agora.scene.aichat.databinding.AichatItemChatGroupManagerBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.groupmanager.logic.AIChatGroupManagerViewModel
import io.agora.scene.aichat.groupmanager.logic.isAddPlaceHolder
import io.agora.scene.aichat.groupmanager.logic.isDeletePlaceHolder
import io.agora.scene.aichat.list.AIChatListActivity
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatGroupManagerFragment : BaseViewBindingFragment<AichatFragmentGroupDetailBinding>() {

    private val mGroupViewModel: AIChatGroupManagerViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = arguments?.getString(AiChatActivity.EXTRA_CONVERSATION_ID) ?: ""
                return AIChatGroupManagerViewModel(conversationId) as T
            }
        }
    }

    private lateinit var layoutManager: GridLayoutManager
    private val selectUserDatas by lazy { mutableListOf<ContactItem>() }
    private val adapter by lazy {
        object : QuickAdapter<AichatItemChatGroupManagerBinding, ContactItem>(
            AichatItemChatGroupManagerBinding::inflate, selectUserDatas
        ) {
            override fun onBind(
                binding: AichatItemChatGroupManagerBinding,
                datas: List<ContactItem>,
                position: Int
            ) {
                val item = datas[position]
                if (item.isAddPlaceHolder()) {
                    binding.iv.visibility = View.GONE
                    binding.tv.text = getString(R.string.aichat_add_agent)
                    binding.ivAdd.setImageResource(R.drawable.aichat_add)
                } else if (item.isDeletePlaceHolder()) {
                    binding.iv.visibility = View.GONE
                    binding.tv.text = getString(R.string.aichat_delete_agent)
                    binding.ivAdd.setImageResource(R.drawable.aichat_delete)
                } else {
                    binding.iv.visibility = View.VISIBLE
                    binding.iv.loadCircleImage(item.avatar ?: "")
                    binding.tv.text = item.name
                }
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentGroupDetailBinding {
        return AichatFragmentGroupDetailBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.tvMaxAgents.text =
            getString(R.string.aichat_group_create_desc, AiChatGroupCreateViewModel.MAX_SELECT_COUNT)
        layoutManager = GridLayoutManager(requireContext(), 4)
        adapter.onItemClickListener = { datas, position ->
            val item = datas[position]
            if (item.isAddPlaceHolder()) {
                findNavController().navigate(AiChatGroupManagerActivity.ADD_TYPE)
            } else if (item.isDeletePlaceHolder()) {
                findNavController().navigate(AiChatGroupManagerActivity.DELETE_TYPE)
            } else {
                //nothing
            }
        }
        binding.rv.let {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
        binding.tvGroupName.text = mGroupViewModel.getChatName()
        binding.tvEdit.setOnClickListener {
            findNavController().navigate(AiChatGroupManagerActivity.EDIT_TYPE)
        }
        binding.titleView.setLeftClick {
            activity?.finish()
        }
        binding.titleView.setRightIconClick {
            showGroupManagerDialog()
        }
    }

    private var mGroupDialog: AIChatGroupManagerDialog? = null

    private fun showGroupManagerDialog() {
        mGroupDialog = AIChatGroupManagerDialog()
        mGroupDialog?.deleteListener = {
            showDeleteGroup()
        }
        mGroupDialog?.show(childFragmentManager,"AIChatGroupManagerDialog")
    }

    private fun showDeleteGroup() {
        AlertDialog.Builder(requireContext(), R.style.aichat_alert_dialog)
            .setTitle(getString(R.string.aichat_delete_group_title, mGroupViewModel.getChatName()))
            .setMessage(getString(R.string.aichat_delete_group_tips))
            .setPositiveButton(R.string.confirm) { dialog, id ->
                dialog.dismiss()
                mGroupViewModel.deleteGroup()
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    override fun initListener() {
        super.initListener()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mGroupViewModel.groupMemberDatas.observe(viewLifecycleOwner) {
                    selectUserDatas.clear()
                    selectUserDatas.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        mGroupViewModel.deleteGroupLivedata.observe(viewLifecycleOwner) {
            if (it){
                CustomToast.show(R.string.aichat_already_delete)
                activity?.apply {
                    val intent = Intent(this, AIChatListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
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

    override fun requestData() {
        super.requestData()
        mGroupViewModel.fetchGroupAgents()
    }
}
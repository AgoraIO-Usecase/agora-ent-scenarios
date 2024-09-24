package io.agora.scene.aichat.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.AiChatActivity
import io.agora.scene.aichat.list.logic.AIAgentViewModel
import io.agora.scene.aichat.databinding.AichatFragmentAgentListBinding
import io.agora.scene.aichat.databinding.AichatItemAgentListBinding
import io.agora.scene.aichat.ext.SwipeToDeleteCallback
import io.agora.scene.aichat.ext.getAgentItemBackground
import io.agora.scene.aichat.ext.getIdentifier
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.saveGreetingMessage
import io.agora.scene.aichat.imkit.impl.EaseContactListener
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.base.component.BaseViewBindingFragment

/**
 * 智能体列表页
 */
class AIChatAgentListFragment : BaseViewBindingFragment<AichatFragmentAgentListBinding>() {

    //viewModel
    private val mAIAgentViewModel: AIAgentViewModel by viewModels()

    private var isViewCreated = false
    private var hasLoadedData = false

    companion object {
        const val TAG = "AIChatAgentListFragment"
        const val agent_is_public = "agent_is_public"

        fun newInstance(public: Boolean) = AIChatAgentListFragment().apply {
            arguments = Bundle().apply {
                putBoolean(agent_is_public, public)
            }
        }
    }

    private val isPublic: Boolean by lazy {
        arguments?.getBoolean(agent_is_public) ?: false
    }

    private var mAgentAdapter: AIAgentAdapter? = null

    private var mSwipeToDeleteCallback: SwipeToDeleteCallback? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentAgentListBinding {
        return AichatFragmentAgentListBinding.inflate(inflater)
    }

    override fun onResume() {
        super.onResume()
        lazyLoadData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            lazyLoadData()
        }
    }

    private fun lazyLoadData() {
        if (isViewCreated && !hasLoadedData) {
            if (isPublic) {
                mAIAgentViewModel.getPublicAgent(true)
            } else {
                mAIAgentViewModel.getUserAgent(true)
            }
            hasLoadedData = true
        }
    }

    override fun initView() {
        super.initView()
        isViewCreated = true
        binding.tvTips1.text =
            if (isPublic) getString(R.string.aichat_public_agent_empty) else getString(R.string.aichat_private_agent_empty)

        mAgentAdapter = AIAgentAdapter(binding.root.context, mutableListOf(),
            onClickItemList = { position, info ->
                activity?.let {
                    if (isPublic) {
                        checkAddGreetingMessage(info)
                    }
                    AiChatActivity.start(it, info.id)
                }
            })

        binding.rvAgentList.layoutManager = LinearLayoutManager(context)
        binding.rvAgentList.adapter = mAgentAdapter
        binding.rvAgentList.itemAnimator = null

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)
        if (isPublic) {
            binding.smartRefreshLayout.setOnRefreshListener {
                mAIAgentViewModel.getPublicAgent(true)
            }
        } else {
            binding.smartRefreshLayout.setOnRefreshListener {
                mAIAgentViewModel.getUserAgent(true)
            }
            val deleteIcon = ContextCompat.getDrawable(binding.root.context, R.drawable.aichat_icon_delete) ?: return
            mSwipeToDeleteCallback = SwipeToDeleteCallback(binding.rvAgentList, deleteIcon).apply {
                onClickDeleteCallback = { viewHolder ->
                    val position = viewHolder.bindingAdapterPosition
                    mAgentAdapter?.mDataList?.get(position)?.let { aiAgentModel ->
                        showDeleteAgent(position, aiAgentModel)
                    }
                }
            }.apply {
                val itemTouchHelper = ItemTouchHelper(this)
                itemTouchHelper.attachToRecyclerView(binding.rvAgentList)
            }
        }
    }

    private fun checkAddGreetingMessage(info: EaseProfile) {
        val conversation = ChatClient.getInstance().chatManager()
            .getConversation(info.id, ChatConversationType.Chat, true)
        val message = if (info.id.contains("common-agent-001")) {
            getString(R.string.aichat_assistant_greeting)
        } else if (info.id.contains("common-agent-002")) {
            getString(R.string.aichat_programming_greeting)
        } else if (info.id.contains("common-agent-003")) {
            getString(R.string.aichat_attorney_greeting)
        } else if (info.id.contains("common-agent-004")) {
            getString(R.string.aichat_practitioner_greeting)
        } else {
            null
        }
        message?.run {
            conversation.saveGreetingMessage(this)?.let { chatMessage ->
                ChatClient.getInstance().chatManager().saveMessage(chatMessage)
            }
        }
    }

    private fun showDeleteAgent(position: Int, easeProfile: EaseProfile) {
        AlertDialog.Builder(requireContext(), R.style.aichat_alert_dialog)
            .setTitle(getString(R.string.aichat_delete_agent_title, easeProfile.name))
            .setMessage(getString(R.string.aichat_delete_agent_tips))
            .setPositiveButton(R.string.confirm) { dialog, id ->
                mSwipeToDeleteCallback?.clearCurrentSwipedView()
                dialog.dismiss()
                mAIAgentViewModel.deleteAgent(position, easeProfile)
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                mSwipeToDeleteCallback?.clearCurrentSwipedView()
                dialog.dismiss()
            }
            .show()
    }

    override fun initListener() {
        super.initListener()
        mAIAgentViewModel.publicAIAgentLiveData.observe(this) { aiAgentList ->
            if (!isPublic) return@observe
            binding.smartRefreshLayout.finishRefresh()
            mAgentAdapter?.submitList(aiAgentList)
            binding.rvAgentList.isVisible = aiAgentList.isNotEmpty()
            binding.groupEmpty.isVisible = aiAgentList.isEmpty()
        }
        mAIAgentViewModel.privateAIAgentLiveData.observe(this) { aiAgentList ->
            if (isPublic) return@observe
            binding.smartRefreshLayout.finishRefresh()
            mAgentAdapter?.submitList(aiAgentList)
            binding.rvAgentList.isVisible = aiAgentList.isNotEmpty()
            binding.groupEmpty.isVisible = aiAgentList.isEmpty()
        }
        mAIAgentViewModel.deleteAgentLivedata.observe(this) {
            if (it.second) {
                val position = it.first
                mAgentAdapter?.removeAt(position)
            }
        }
        mAIAgentViewModel.loadingChange.showDialog.observe(this) {
            showLoadingView()
        }
        mAIAgentViewModel.loadingChange.dismissDialog.observe(this) {
            hideLoadingView()
        }

        if (!isPublic) {
            EaseIM.addContactListener(contactListener)
        }
    }

    private val contactListener = object : EaseContactListener() {

        override fun onContactAdded(username: String?) {
            super.onContactAdded(username)
            mAIAgentViewModel.getUserAgent()
        }

        override fun onContactDeleted(username: String?) {
            super.onContactDeleted(username)
            username ?: return
            mAIAgentViewModel.getUserAgent()
        }
    }


    override fun requestData() {
        super.requestData()
    }

    override fun onDestroyView() {
        if (!isPublic) {
            EaseIM.removeContactListener(contactListener)
        }
        super.onDestroyView()
    }
}

class AIAgentAdapter constructor(
    private val mContext: Context,
    private var mList: MutableList<EaseProfile>,
    private val onClickItemList: ((position: Int, info: EaseProfile) -> Unit)? = null
) : RecyclerView.Adapter<AIAgentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AichatItemAgentListBinding) : RecyclerView.ViewHolder(binding.root)

    val mDataList: List<EaseProfile> get() = mList.toList()

    fun submitList(newList: List<EaseProfile>) {
        val diffCallback = AIAgentDiffCallback(mList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        mList.clear()
        mList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeAt(position: Int) {
        if (position >= mList.size) {
            return
        }
        mList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AichatItemAgentListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.binding.tvAgentName.text = item.getNotEmptyName()
        holder.binding.tvAgentDes.text = item.sign?.ifEmpty {
            mContext.getString(R.string.aichat_empty_description)
        }
        if (item.avatar.isNullOrEmpty()) {
            holder.binding.ivAvatar.setImageResource(R.drawable.aichat_agent_avatar_2)
        } else {
            holder.binding.ivAvatar.loadCircleImage(item.avatar!!)
        }
        val bgRes = position.getAgentItemBackground().getIdentifier(mContext)
        holder.binding.layoutBackground.setBackgroundResource(if (bgRes != 0) bgRes else R.drawable.aichat_agent_item_green_bg)

        holder.binding.root.setOnClickListener {
            onClickItemList?.invoke(position, item)
        }
    }
}

class AIAgentDiffCallback constructor(
    private val oldList: List<EaseProfile>,
    private val newList: List<EaseProfile>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
package io.agora.scene.aichat.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.AichatConversationListFragmentBinding
import io.agora.scene.aichat.databinding.AichatConversationListItemBinding
import io.agora.scene.aichat.ext.SwipeToDeleteCallback
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.extensions.getMessageDigest
import io.agora.scene.aichat.imkit.extensions.getSyncUserFromProvider
import io.agora.scene.aichat.imkit.extensions.isSingleChat
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.list.logic.AIConversationViewModel
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast

/**
 * 会话列表页面
 */
class AIChatConversationListFragment : BaseViewBindingFragment<AichatConversationListFragmentBinding>() {

    //viewModel
    private val aiConversationViewModel: AIConversationViewModel by viewModels()

    companion object {

        fun newInstance() = AIChatConversationListFragment()
    }

    private var mConversationAdapter: AIConversationAdapter? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatConversationListFragmentBinding {
        return AichatConversationListFragmentBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        mConversationAdapter = AIConversationAdapter(mutableListOf(), onClickItemList = { position, info ->
            CustomToast.show("点击了会话  $position")
        })

        binding.rvConversationList.layoutManager = LinearLayoutManager(context)
        binding.rvConversationList.adapter = mConversationAdapter

        val deleteIcon = ContextCompat.getDrawable(binding.root.context, R.drawable.aichat_icon_delete) ?: return
        val itemTouchHelperCallback = SwipeToDeleteCallback(binding.rvConversationList, deleteIcon).apply {
            onClickDeleteCallback = { viewHolder ->
                val position = viewHolder.bindingAdapterPosition
                CustomToast.show("点击了删除按钮 $position")
                mConversationAdapter?.mDataList?.get(position)?.let { conversation ->
                    showDeleteConversation(position, conversation)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvConversationList)
    }

    private fun showDeleteConversation(position: Int, conversation: EaseConversation) {
        // 单聊/群聊
        val title = if (conversation.conversationType == ChatConversationType.GroupChat ||
            conversation.conversationType == ChatConversationType.ChatRoom
        ) {
            getString(R.string.aichat_delete_group_title, "这是群聊")
        } else {
            getString(R.string.aichat_delete_conversation_title)
        }
        val message = if (conversation.conversationType == ChatConversationType.GroupChat ||
            conversation.conversationType == ChatConversationType.ChatRoom
        ) {
            getString(R.string.aichat_delete_group_tips)
        } else {
            getString(R.string.aichat_delete_conversation_tips)
        }
        AlertDialog.Builder(requireContext(), R.style.aichat_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.confirm) { dialog, id ->
                CustomToast.show("点击 确认")
                dialog.dismiss()
                mConversationAdapter?.removeAt(position)
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                CustomToast.show("点击 取消")
                dialog.dismiss()
            }
            .show()
    }

    override fun initListener() {
        super.initListener()
        aiConversationViewModel.chatConversationListLivedata.observe(this) { converstionList ->
            mConversationAdapter?.submitList(converstionList)
            binding.rvConversationList.isVisible = converstionList.isNotEmpty()
            binding.groupEmpty.isVisible = converstionList.isEmpty()
        }
    }

    override fun requestData() {
        super.requestData()
        aiConversationViewModel.getConversationList()
    }
}

class AIConversationAdapter constructor(
    private var mList: MutableList<EaseConversation>,
    private val onClickItemList: ((position: Int, info: EaseConversation) -> Unit)? = null
) : RecyclerView.Adapter<AIConversationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AichatConversationListItemBinding) : RecyclerView.ViewHolder(binding.root)

    val mDataList: List<EaseConversation> get() = mList.toList()

    fun submitList(list: List<EaseConversation>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position >= mList.size) {
            return
        }
        mList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AichatConversationListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        item.lastMessage?.let { lastMessage ->
            holder.binding.tvLastMessage.text = lastMessage.getMessageDigest()
            val easeProfile = lastMessage.getSyncUserFromProvider()
            holder.binding.tvConversationName.text = easeProfile?.getRemarkOrName()

            if (item.conversationType == ChatConversationType.Chat) {
                holder.binding.ivAvatar.isVisible = true
                holder.binding.overlayImage.isVisible = false
                val avatar = easeProfile?.avatar ?: ""
                if (avatar.isNotEmpty()) {
                    holder.binding.ivAvatar.loadCircleImage(avatar)
                } else {
                    holder.binding.ivAvatar.setImageResource(R.mipmap.default_user_avatar)
                }
            } else {
                holder.binding.ivAvatar.isVisible = false
                holder.binding.overlayImage.isVisible = true
            }
        }
        holder.binding.layoutBackground.setBackgroundResource(R.drawable.aichat_agent_bg_0)
        holder.binding.root.setOnClickListener {
            onClickItemList?.invoke(position, item)
        }
    }
}
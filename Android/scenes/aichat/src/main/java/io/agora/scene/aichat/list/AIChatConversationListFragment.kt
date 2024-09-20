package io.agora.scene.aichat.list

import android.content.Context
import android.graphics.Color
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
import io.agora.scene.aichat.databinding.AichatFragmentConversationListBinding
import io.agora.scene.aichat.databinding.AichatItemConversationListBinding
import io.agora.scene.aichat.ext.SwipeToDeleteCallback
import io.agora.scene.aichat.ext.getConversationItemBackground
import io.agora.scene.aichat.ext.getIdentifier
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.setGradientBackground
import io.agora.scene.aichat.imkit.EaseFlowBus
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.getDateFormat
import io.agora.scene.aichat.imkit.extensions.getMessageDigest
import io.agora.scene.aichat.imkit.extensions.isAlertMessage
import io.agora.scene.aichat.imkit.impl.EaseContactListener
import io.agora.scene.aichat.imkit.impl.EaseConversationListener
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.model.EaseEvent
import io.agora.scene.aichat.imkit.model.getChatAvatar
import io.agora.scene.aichat.imkit.model.getName
import io.agora.scene.aichat.imkit.model.getGroupAvatars
import io.agora.scene.aichat.imkit.model.getGroupLastUser
import io.agora.scene.aichat.imkit.model.isGroup
import io.agora.scene.aichat.list.logic.AIConversationViewModel
import io.agora.scene.base.component.BaseViewBindingFragment

/**
 * 会话列表页面
 */
class AIChatConversationListFragment : BaseViewBindingFragment<AichatFragmentConversationListBinding>() {

    //viewModel
    private val mConversationViewModel: AIConversationViewModel by viewModels()

    companion object {

        fun newInstance() = AIChatConversationListFragment()
    }

    private var mConversationAdapter: AIConversationAdapter? = null

    private var mSwipeToDeleteCallback: SwipeToDeleteCallback? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatFragmentConversationListBinding {
        return AichatFragmentConversationListBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        mConversationAdapter = AIConversationAdapter(binding.root.context, mutableListOf(),
            onClickItemList = { position, info ->
                activity?.let {
                    AiChatActivity.start(it, info.conversationId)
                }
            })

        binding.rvConversationList.layoutManager = LinearLayoutManager(context)
        binding.rvConversationList.adapter = mConversationAdapter

        val deleteIcon = ContextCompat.getDrawable(binding.root.context, R.drawable.aichat_icon_delete) ?: return
        mSwipeToDeleteCallback = SwipeToDeleteCallback(binding.rvConversationList, deleteIcon).apply {
            onClickDeleteCallback = { viewHolder ->
                val position = viewHolder.bindingAdapterPosition
                mConversationAdapter?.mDataList?.get(position)?.let { conversation ->
                    showDeleteConversation(position, conversation)
                }
            }
        }.apply {
            val itemTouchHelper = ItemTouchHelper(this)
            itemTouchHelper.attachToRecyclerView(binding.rvConversationList)
        }

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)
        binding.smartRefreshLayout.setOnRefreshListener {
            mConversationViewModel.getConversationList()
        }
    }

    private fun showDeleteConversation(position: Int, conversation: EaseConversation) {
        // 单聊/群聊
        val title = if (conversation.isGroup()) {
            getString(R.string.aichat_delete_group_title, "这是群聊")
        } else {
            getString(R.string.aichat_delete_conversation_title)
        }
        val message = if (conversation.isGroup()) {
            getString(R.string.aichat_delete_group_tips)
        } else {
            getString(R.string.aichat_delete_conversation_tips)
        }
        AlertDialog.Builder(requireContext(), R.style.aichat_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.confirm) { dialog, id ->
                mSwipeToDeleteCallback?.clearCurrentSwipedView()
                dialog.dismiss()
                mConversationViewModel.deleteConversation(position, conversation)
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    override fun initListener() {
        super.initListener()
        mConversationViewModel.chatConversationListLivedata.observe(this) { converstionList ->
            binding.smartRefreshLayout.finishRefresh()
            mConversationAdapter?.submitList(converstionList)
            binding.rvConversationList.isVisible = converstionList.isNotEmpty()
            binding.groupEmpty.isVisible = converstionList.isEmpty()
        }
        mConversationViewModel.deleteConversationLivedata.observe(this) {
            if (it.second) {
                val position = it.first
                mConversationAdapter?.removeAt(position)
            }
        }
        EaseIM.addContactListener(contactListener)
        EaseIM.addConversationListener(conversationListener)

        EaseFlowBus.withStick<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(viewLifecycleOwner) {
            if (it.isConversationChange) {
                mConversationViewModel.getConversationList()
            }
        }
    }

    private val contactListener = object : EaseContactListener() {

        override fun onContactDeleted(username: String?) {
            username ?: return
            mConversationAdapter?.mDataList?.forEach {
                if (it.conversationId == username) {
                    mConversationViewModel.getConversationList()
                }
            }
        }
    }

    private val conversationListener = object : EaseConversationListener() {
        override fun onConversationRead(from: String?, to: String?) {
            mConversationViewModel.getConversationList()
        }
    }

    override fun requestData() {
        super.requestData()
        mConversationViewModel.getConversationList()
    }

    override fun onDestroyView() {
        EaseIM.removeContactListener(contactListener)
        EaseIM.removeConversationListener(conversationListener)
        super.onDestroyView()
    }
}

class AIConversationAdapter constructor(
    private val mContext: Context,
    private var mList: MutableList<EaseConversation>,
    private val onClickItemList: ((position: Int, info: EaseConversation) -> Unit)? = null
) : RecyclerView.Adapter<AIConversationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AichatItemConversationListBinding) : RecyclerView.ViewHolder(binding.root)

    val mDataList: List<EaseConversation> get() = mList.toList()

    fun submitList(newList: List<EaseConversation>) {
        val diffCallback = AIConversationDiffCallback(mList, newList)
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
        return ViewHolder(
            AichatItemConversationListBinding.inflate(
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
        val easeConversation = mList[position]
        easeConversation.lastMessage?.let { lastMessage ->
            val conversationName = easeConversation.getName()
            holder.binding.tvConversationName.text = conversationName

            if (easeConversation.isGroup()) {
                val lastNickName = easeConversation.getGroupLastUser()
                if (lastMessage.isAlertMessage()) {
                    holder.binding.tvLastMessage.text = lastMessage.getMessageDigest()
                } else {
                    holder.binding.tvLastMessage.text = lastNickName + "：" + lastMessage.getMessageDigest()
                }

                holder.binding.ivAvatar.visibility = View.INVISIBLE
                holder.binding.overlayImage.visibility = View.VISIBLE

                val groupAvatar = easeConversation.getGroupAvatars()
                if (groupAvatar.isEmpty()) {
                    holder.binding.overlayImage.ivBaseImageView?.setImageResource(R.drawable.aichat_agent_avatar_2)
                    holder.binding.overlayImage.ivOverlayImageView?.setImageResource(R.drawable.aichat_agent_avatar_2)
                } else if (groupAvatar.size == 1) {
                    holder.binding.overlayImage.ivBaseImageView?.loadCircleImage(groupAvatar[0])
                    holder.binding.overlayImage.ivOverlayImageView?.setImageResource(R.drawable.aichat_agent_avatar_2)
                } else {
                    holder.binding.overlayImage.ivBaseImageView?.loadCircleImage(groupAvatar[0])
                    holder.binding.overlayImage.ivOverlayImageView?.loadCircleImage(groupAvatar[1])
                }

                holder.binding.overlayImage.ivBaseImageView?.setGradientBackground(
                    intArrayOf(
                        Color.parseColor("#C0F3CC"), // 起始颜色
                        Color.parseColor("#A6E5BE") // 结束颜色
                    )
                )

                holder.binding.overlayImage.ivOverlayImageView?.setGradientBackground(
                    intArrayOf(
                        Color.parseColor("#C6EEDB"), // 起始颜色
                        Color.parseColor("#B0E5C1") // 结束颜色
                    )
                )
            } else {
                holder.binding.tvLastMessage.text = lastMessage.getMessageDigest()

                holder.binding.ivAvatar.visibility = View.VISIBLE
                holder.binding.overlayImage.visibility = View.INVISIBLE

                val avatar = easeConversation.getChatAvatar()
                if (avatar.isNotEmpty()) {
                    holder.binding.ivAvatar.loadCircleImage(avatar)
                } else {
                    holder.binding.ivAvatar.setImageResource(R.drawable.aichat_agent_avatar_2)
                }
            }
            holder.binding.ivUnread.isVisible = easeConversation.unreadMsgCount > 0
            holder.binding.tvConversationTime.text = lastMessage.getDateFormat(false)
        }
        val bgRes = position.getConversationItemBackground().getIdentifier(mContext)
        holder.binding.layoutBackground.setBackgroundResource(if (bgRes != 0) bgRes else R.drawable.aichat_conversation_item_green_bg)
        holder.binding.root.setOnClickListener {
            onClickItemList?.invoke(position, easeConversation)
        }
    }
}

class AIConversationDiffCallback(
    private val oldList: List<EaseConversation>,
    private val newList: List<EaseConversation>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].lastMessage == newList[newItemPosition].lastMessage
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
package io.agora.scene.aichat.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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
import io.agora.scene.aichat.list.logic.AIAgentModel
import io.agora.scene.aichat.databinding.AichatAgentListFragmentBinding
import io.agora.scene.aichat.databinding.AichatAgentListItemBinding
import io.agora.scene.aichat.ext.SwipeToDeleteCallback
import io.agora.scene.aichat.ext.getIdentifier
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.model.EaseChatType
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 智能体列表页
 */
class AIChatAgentListFragment : BaseViewBindingFragment<AichatAgentListFragmentBinding>() {

    //viewModel
    private val aiAgentViewModel: AIAgentViewModel by viewModels()

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

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatAgentListFragmentBinding {
        return AichatAgentListFragmentBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding.tvTips1.text =
            if (isPublic) getString(R.string.aichat_public_agent_empty) else getString(R.string.aichat_private_agent_empty)

        mAgentAdapter = AIAgentAdapter(binding.root.context, mutableListOf(), onClickItemList = { position, info ->
            activity?.let {
                AiChatActivity.start(it, info.id, EaseChatType.SINGLE_CHAT)
            }
        })

        binding.rvAgentList.layoutManager = LinearLayoutManager(context)
        binding.rvAgentList.adapter = mAgentAdapter
        binding.rvAgentList.itemAnimator = null

        if (!isPublic) {
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

    private fun showDeleteAgent(position: Int, aiAgentModel: AIAgentModel) {
        AlertDialog.Builder(requireContext(), R.style.aichat_alert_dialog)
            .setTitle(getString(R.string.aichat_delete_agent_title, aiAgentModel.name))
            .setMessage(getString(R.string.aichat_delete_agent_tips))
            .setPositiveButton(R.string.confirm) { dialog, id ->
                mSwipeToDeleteCallback?.clearCurrentSwipedView()
                dialog.dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500L)
                    mAgentAdapter?.removeAt(position)
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                mSwipeToDeleteCallback?.clearCurrentSwipedView()
                dialog.dismiss()
            }
            .show()
    }

    override fun initListener() {
        super.initListener()
        aiAgentViewModel.publicAIAgentLiveData.observe(this) { aiAgentList ->
            mAgentAdapter?.submitList(aiAgentList)
            binding.rvAgentList.isVisible = aiAgentList.isNotEmpty()
            binding.groupEmpty.isVisible = aiAgentList.isEmpty()
        }
        aiAgentViewModel.privateAIAgentLiveData.observe(this) { aiAgentList ->
            mAgentAdapter?.submitList(aiAgentList)
            binding.rvAgentList.isVisible = aiAgentList.isNotEmpty()
            binding.groupEmpty.isVisible = aiAgentList.isEmpty()
        }
    }

    override fun requestData() {
        super.requestData()
        if (isPublic) {
            aiAgentViewModel.getPublicAgent()
        } else {
            aiAgentViewModel.getPublicAgent()
        }
    }
}

class AIAgentAdapter constructor(
    private val mContext: Context,
    private var mList: MutableList<AIAgentModel>,
    private val onClickItemList: ((position: Int, info: AIAgentModel) -> Unit)? = null
) : RecyclerView.Adapter<AIAgentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AichatAgentListItemBinding) : RecyclerView.ViewHolder(binding.root)

    val mDataList: List<AIAgentModel> get() = mList.toList()

    fun submitList(newList: List<AIAgentModel>) {
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
        return ViewHolder(AichatAgentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.binding.tvAgentName.text = item.name.ifEmpty { item.id }
        holder.binding.tvAgentDes.text = item.description.ifEmpty {
            mContext.getString(R.string.aichat_empty_description)
        }
        if (item.avatar.isNotEmpty()) {
            holder.binding.ivAvatar.loadCircleImage(item.avatar)
        } else {
            holder.binding.ivAvatar.setImageResource(R.mipmap.default_user_avatar)
        }
        val bgRes = item.background.getIdentifier(mContext)
        holder.binding.layoutBackground.setBackgroundResource(if (bgRes != 0) bgRes else R.drawable.aichat_agent_bg_0)

        holder.binding.root.setOnClickListener {
            onClickItemList?.invoke(position, item)
        }
    }
}

class AIAgentDiffCallback(
    private val oldList: List<AIAgentModel>,
    private val newList: List<AIAgentModel>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].prompt == newList[newItemPosition].prompt
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
package io.agora.scene.aichat.list

import android.view.LayoutInflater
import android.view.ViewGroup
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
import io.agora.scene.aichat.imkit.ChatConversation
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
                CustomToast.show("点击了删除按钮 ${viewHolder.bindingAdapterPosition}")
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvConversationList)
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
    private var mList: MutableList<ChatConversation>,
    private val onClickItemList: ((position: Int, info: ChatConversation) -> Unit)? = null
) : RecyclerView.Adapter<AIConversationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AichatConversationListItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun submitList(list: List<ChatConversation>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position >= mList.size) {
            return
        }
        mList.removeAt(position)
        notifyItemChanged(position)
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
        holder.binding.tvConversationName.text = item.lastMessage.userName
//            holder.binding.tvLastMessage.text = item.lastMessage.getMess
//        holder.binding.ivAvatar.loadCircleImage(item.fullHeadUrl)
//        holder.binding.ivCover.loadImage(item.fullBackgroundUrl)
        holder.binding.root.setOnClickListener {
            onClickItemList?.invoke(position, item)
        }
    }
}
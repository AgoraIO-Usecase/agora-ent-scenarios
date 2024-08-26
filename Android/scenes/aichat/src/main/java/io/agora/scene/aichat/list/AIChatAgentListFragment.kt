package io.agora.scene.aichat.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.dragswipe.QuickDragAndSwipe
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback
import com.chad.library.adapter4.dragswipe.setItemSwipeListener
import io.agora.scene.aichat.R
import io.agora.scene.aichat.list.AIChatAgentListFragment.Companion.TAG
import io.agora.scene.aichat.list.logic.AIAgentViewModel
import io.agora.scene.aichat.list.logic.model.AIAgentModel
import io.agora.scene.aichat.databinding.AichatAgentListFragmentBinding
import io.agora.scene.aichat.databinding.AichatAgentListItemBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.loadImage
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast

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

    private val mAgentAdapter: AIAgentAdapter by lazy { AIAgentAdapter() }

    private val quickDragAndSwipe: QuickDragAndSwipe by lazy {
        QuickDragAndSwipe()
            .setSwipeMoveFlags(ItemTouchHelper.LEFT)
            .setItemViewSwipeEnabled(true)
            .setLongPressDragEnabled(false) //关闭默认的长按拖拽功能，通过自定义长按事件进行拖拽
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatAgentListFragmentBinding {
        return AichatAgentListFragmentBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding.tvTips1.text =
            if (isPublic) getString(R.string.aichat_public_agent_empty) else getString(R.string.aichat_private_agent_empty)
        binding.rvAgentList.layoutManager = LinearLayoutManager(context)
        binding.rvAgentList.adapter = mAgentAdapter

        quickDragAndSwipe.attachToRecyclerView(binding.rvAgentList)
            .setDataCallback(mAgentAdapter)
            .setItemSwipeListener(
                onItemSwipeStart = { viewHolder, pos ->
                    Log.d(TAG, "onItemSwipeStart $pos")
                },
                onItemSwipeMoving = { canvas, viewHolder, dX, dY, isCurrentlyActive ->
                    Log.d(TAG, "onItemSwipeMoving ")
                },
                onItemSwiped = { viewHolder, _, pos ->
                    Log.d(TAG, "onItemSwiped $pos")
                },
                onItemSwipeEnd = { viewHolder, pos ->
                    Log.d(TAG, "onItemSwipeEnd $pos")
                }
            )

        mAgentAdapter.setOnItemClickListener { adapter, view, position ->
            CustomToast.show("点击了：$position，侧滑可进行删除$position")
//            quickDragAndSwipe.startSwipe(position)
        }
    }

    override fun initListener() {
        super.initListener()
        aiAgentViewModel.publicAIAgentLiveData.observe(this) { aiAgentList ->
            mAgentAdapter.submitList(aiAgentList)
            binding.rvAgentList.isVisible = aiAgentList.isNotEmpty()
            binding.groupEmpty.isVisible = aiAgentList.isEmpty()
        }
    }

    override fun requestData() {
        super.requestData()
        if (isPublic) {
            aiAgentViewModel.getPublicAgent()
        }
    }

}

class AiAgentListHolder constructor(val binding: AichatAgentListItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class AIAgentAdapter : BaseQuickAdapter<AIAgentModel, AiAgentListHolder>(), DragAndSwipeDataCallback {

    override fun onBindViewHolder(holder: AiAgentListHolder, position: Int, item: AIAgentModel?) {
        item ?: return
        holder.binding.tvAgentName.text = item.name
        holder.binding.tvAgentDes.text = item.description
        holder.binding.ivAvatar.loadCircleImage(item.fullHeadUrl)
        holder.binding.ivCover.loadImage(item.fullBackgroundUrl)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): AiAgentListHolder {
        return AiAgentListHolder(AichatAgentListItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun dataMove(fromPosition: Int, toPosition: Int) {
        Log.d(TAG, "dataMove fromPosition:$fromPosition toPosition:$toPosition")
    }

    override fun dataRemoveAt(position: Int) {
        Log.d(TAG, "dataRemoveAt $position")
    }
}


package io.agora.scene.aichat.imkit.widget

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.aichat.ext.mainScope
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.model.EaseLoadDataType
import io.agora.scene.aichat.imkit.widget.messageLayout.EaseChatRowViewHolder
import io.agora.scene.aichat.imkit.widget.messageLayout.EaseMessagesAdapter
import kotlinx.coroutines.launch

internal fun ConcatAdapter.getItemCountBeforeTarget(adapter: EaseMessagesAdapter): Int {
    var count = 0
    for (i in 0 until adapters.size) {
        val currentAdapter = adapters[i]
        if (currentAdapter == adapter) {
            break
        }
        count += currentAdapter.itemCount
    }
    return count
}

/**
 * The controller to control the scroll and data of the message list.
 */
class EaseChatMessageListScrollAndDataController(
    private val rvList: RecyclerView,
    private val adapter: EaseMessagesAdapter,
    private val context: Context
) {

    /**
     * Whether to scroll to the bottom when the message list changes.
     */
    private var isNeedScrollToBottomWhenChange = true

    /**
     * Whether the list can scroll to the bottom automatically.
     */
    private var isCanAutoScrollToBottom = true
    private var loadDataType: EaseLoadDataType = EaseLoadDataType.LOCAL
    private var recyclerViewLastHeight = 0
    private var targetScrollMsgId: String? = null

    init {
        rvList.addOnLayoutChangeListener { v, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom != oldBottom) {
                val height = rvList.height
                if (recyclerViewLastHeight == 0) recyclerViewLastHeight = height
                if (recyclerViewLastHeight != height) {
                    if (!adapter.mData.isNullOrEmpty()
                        && rvList.canScrollVertically(1)
                        && loadDataType != EaseLoadDataType.SEARCH
                        && isNeedScrollToBottomWhenChange
                        && isCanAutoScrollToBottom
                    ) {
                        smoothScrollToBottom()
                    } else {
                        if (!isCanAutoScrollToBottom) {
                            scrollToRelativePosition(bottom, oldBottom)
                        }
                    }
                }
                recyclerViewLastHeight = height
            }
        }
    }

    private var lastVisibleItemPosition = -1
    private var lastVisibleItemTop = 0
    private fun scrollToRelativePosition(bottom: Int, oldBottom: Int) {
        val dy = oldBottom - bottom
        val layoutManager = rvList.layoutManager
        if (layoutManager is LinearLayoutManager) {
            if (dy > 0) {
                rvList.scrollBy(0, dy)
                lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                layoutManager.findViewByPosition(lastVisibleItemPosition)?.let {
                    lastVisibleItemTop = it.top - bottom
                }
            } else {
                layoutManager.findViewByPosition(lastVisibleItemPosition)?.let {
                    rvList.scrollBy(0, it.top - bottom - lastVisibleItemTop)
                } ?: kotlin.run {
                    rvList.scrollBy(0, dy)
                }
            }
        }

    }

    fun setLoadDataType(loadDataType: EaseLoadDataType) {
        this.loadDataType = loadDataType
    }

    /**
     * Scroll to the bottom of the list.
     */
    fun scrollToBottom(isRefresh: Boolean = false) {
        context.mainScope().launch {
            if (!isRefresh || (isCanAutoScrollToBottom && isNeedScrollToBottomWhenChange)) {
                scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    fun smoothScrollToBottom() {
        context.mainScope().launch {
            if (isCanAutoScrollToBottom && isNeedScrollToBottomWhenChange) {
                rvList.post {
                    if (!rvList.canScrollVertically(1)) return@post
                    var position = adapter.itemCount - 1
                    if (rvList.adapter is ConcatAdapter) {
                        val count = (rvList.adapter as ConcatAdapter).getItemCountBeforeTarget(adapter)
                        position += count
                    }
                    rvList.smoothScrollToPosition(position)
                }
            }
        }
    }

    /**
     * Set whether the list can scroll to the bottom automatically.
     */
    fun setCanAutoScrollToBottom(canAutoScrollToBottom: Boolean) {
        this.isCanAutoScrollToBottom = canAutoScrollToBottom
    }

    /**
     * Whether the list can scroll to the bottom automatically.
     */
    fun isCanAutoScrollToBottom(): Boolean {
        return isCanAutoScrollToBottom
    }

    /**
     * Scroll to the target position.
     */
    fun scrollToPosition(position: Int) {
        if (position < 0 || position >= adapter.itemCount) return
        rvList.post {
            if (isLastPosition(position) && !rvList.canScrollVertically(1)) return@post
            val manager = rvList.layoutManager
            if (manager is LinearLayoutManager) {
                manager.scrollToPositionWithOffset(position, 0)
                checkIfMoveToBottom(position, manager)
            }
        }
    }

    /**
     * Smooth scroll to the target position.
     */
    fun smoothScrollToPosition(position: Int, isMoveToTop: Boolean = true) {
        if (position < 0 || position >= adapter.itemCount) return
        val manager = rvList.layoutManager
        if (manager !is LinearLayoutManager) return
        rvList.post {
            val moveHeight = if (isMoveToTop) -10 else 10
            rvList.smoothScrollBy(0, moveHeight, null, 10)
            rvList.postDelayed({
                val itemViewHeight: Int = getViewHeight(position, manager)
                if (itemViewHeight != -1) {
                    val excessHeight =
                        if (isMoveToTop) -itemViewHeight + 10 else itemViewHeight - 10
                    rvList.smoothScrollBy(0, excessHeight, null, 900)
                } else {
                    manager.scrollToPositionWithOffset(
                        position,
                        0
                    )
                }
            }, 100)
        }
    }

    private fun isLastPosition(position: Int): Boolean {
        return position == adapter.itemCount - 1
    }

    private fun checkIfMoveToBottom(position: Int, layoutManager: LinearLayoutManager) {
        if (position < 0 || position >= adapter.itemCount) return
        if (!rvList.canScrollVertically(1)) return
        if (!isLastPosition(position) || !isFullScreen()) return
        rvList.post {
            val rvPosition: Int =
                layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition()
            if (rvList.childCount > rvPosition) {
                val bottom = rvList.getChildAt(rvPosition).bottom
                val height = rvList.height
                layoutManager.scrollToPositionWithOffset(position, height - bottom)
            }
        }
    }

    private fun isFullScreen(): Boolean {
        var isOverOneScreen = false
        var totalHeight = 0
        for (i in 0 until rvList.childCount) {
            totalHeight += rvList.getChildAt(i).height
            if (rvList.height < totalHeight) {
                isOverOneScreen = true
                break
            }
        }
        return isOverOneScreen
    }

    private fun getViewHeight(position: Int, layoutManager: LinearLayoutManager): Int {
        val view = layoutManager.findViewByPosition(position)
        var height = -1
        if (view != null) {
            height = view.measuredHeight
        } else {
            val holder = rvList.findViewHolderForAdapterPosition(position)
            if (holder != null) {
                height = holder.itemView.height
            }
        }
        return height
    }

    fun refreshMessages(messages: List<ChatMessage>) {
        context.mainScope().launch {
            adapter.setData(messages.toMutableList())
        }
    }

    fun refreshMessage(message: ChatMessage?) {
        if (message == null) return
        context.mainScope().launch {
            val position = adapter.data?.indexOfLast { it.msgId == message.msgId } ?: -1
            if (position != -1) {
                adapter.changeData(position, message)
                adapter.notifyItemChanged(position, 0)
            }
        }
    }

    fun addMessageToLast(message: ChatMessage?) {
        if (message == null) return
        context.mainScope().launch {
            adapter.addData(message)
        }
    }

    fun removeMessage(message: ChatMessage?) {
        if (message == null) return
        context.mainScope().launch {
            var position = -1
            adapter.mData?.forEachIndexed { index, easeMessage ->
                if (easeMessage.msgId == message.msgId) {
                    position = index
                    return@forEachIndexed
                }
            }
            if (position != -1) {
                adapter.mData?.removeAt(position)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun setNeedScrollToBottomWhenViewChange(needToScrollBottom: Boolean) {
        this.isNeedScrollToBottomWhenChange = needToScrollBottom
    }

    /**
     * Set target scroll message id.
     */
    fun setTargetScrollMsgId(msgId: String?) {
        this.targetScrollMsgId = msgId
    }

    /**
     * Scroll to target message.
     */
    fun scrollToTargetMessage(position: Int = -1, highLightAction: (Int) -> Unit) {
        if (position != -1) {
            scrollToPosition(position)
            highLightAction(position)
        } else {
            adapter.mData?.indexOfFirst { it.msgId == targetScrollMsgId }?.let {
                smoothScrollToPosition(it)
                highLightAction(it)
            } ?: kotlin.run {
                Log.e(
                    "scrollController",
                    "moveToTarget failed: No original message was found within the scope of the query"
                )
            }
        }
    }

    fun onScrollStateChanged() {
        (rvList.layoutManager as? LinearLayoutManager)?.let {
            var lastVisibleItemPosition = it.findLastVisibleItemPosition()
            if (rvList.adapter is ConcatAdapter) {
                val count = (rvList.adapter as ConcatAdapter).getItemCountBeforeTarget(adapter)
                lastVisibleItemPosition -= count
            }
            isCanAutoScrollToBottom = lastVisibleItemPosition == adapter.itemCount - 1
        }
    }

    fun setAudioPlaying(message: ChatMessage, isPlaying: Boolean) {
        context.mainScope().launch {
            val position = adapter.data?.indexOfLast { it.msgId == message.msgId } ?: -1
            if (position != -1) {
                val holder = rvList.findViewHolderForAdapterPosition(position)
                if (holder is EaseChatRowViewHolder) {
                    holder.setAudioPlaying(isPlaying)
                }
            }
        }
    }

    fun setAudioRecognizing(message: ChatMessage, isRecognizing: Boolean) {
        context.mainScope().launch {
            val position = adapter.data?.indexOfLast { it.msgId == message.msgId } ?: -1
            if (position != -1) {
                val holder = rvList.findViewHolderForAdapterPosition(position)
                if (holder is EaseChatRowViewHolder) {
                    holder.setAudioRecognizing(isRecognizing)
                }
            }
        }
    }

    fun setAudioReset(message: ChatMessage) {
        context.mainScope().launch {
            val position = adapter.data?.indexOfLast { it.msgId == message.msgId } ?: -1
            if (position != -1) {
                val holder = rvList.findViewHolderForAdapterPosition(position)
                if (holder is EaseChatRowViewHolder) {
                    holder.setAudioReset()
                }
            }
        }
    }

}
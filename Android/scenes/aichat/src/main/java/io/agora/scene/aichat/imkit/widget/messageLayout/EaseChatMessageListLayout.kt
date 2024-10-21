package io.agora.scene.aichat.imkit.widget.messageLayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import io.agora.scene.aichat.databinding.EaseChatMessageListBinding
import io.agora.scene.aichat.ext.mainScope
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatSearchDirection
import io.agora.scene.aichat.imkit.Chatroom
import io.agora.scene.aichat.imkit.EaseFlowBus
import io.agora.scene.aichat.imkit.callback.OnChatErrorListener
import io.agora.scene.aichat.imkit.callback.OnEaseChatReactionErrorListener
import io.agora.scene.aichat.imkit.callback.OnMessageAckSendCallback
import io.agora.scene.aichat.imkit.callback.OnMessageAudioStatusCallback
import io.agora.scene.aichat.imkit.callback.OnMessageChatThreadClickListener
import io.agora.scene.aichat.imkit.callback.OnMessageListItemClickListener
import io.agora.scene.aichat.imkit.model.EaseEvent
import io.agora.scene.aichat.imkit.model.EaseLoadDataType
import io.agora.scene.aichat.imkit.model.isShouldStackFromEnd
import io.agora.scene.aichat.imkit.widget.EaseChatMessageListScrollAndDataController
import io.agora.scene.aichat.imkit.widget.EaseMessageListViewModel
import kotlinx.coroutines.launch

class EaseChatMessageListLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IChatMessageListLayout, IChatMessageListResultView {

    companion object {
        private val TAG = EaseChatMessageListLayout::class.java.simpleName
    }

    private val binding: EaseChatMessageListBinding by lazy {
        EaseChatMessageListBinding.inflate(LayoutInflater.from(context), this, true)
    }

    /**
     * Concat adapter
     */
    private val concatAdapter: ConcatAdapter by lazy {
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
            .build()
        ConcatAdapter(config)
    }

    private val listScrollController: EaseChatMessageListScrollAndDataController by lazy {
        EaseChatMessageListScrollAndDataController(binding.messageList, messagesAdapter!!, context)
    }

    /**
     * 加载数据的模式
     */
    private var loadDataType: EaseLoadDataType = EaseLoadDataType.LOCAL


    /**
     * The viewModel to request data.
     */
    private var viewModel: IChatMessageListRequest? = null

    /**
     * The conversation to handle messages.
     */
    private var conversation: ChatConversation? = null

    /**
     * The adapter to show messages.
     */
    private var messagesAdapter: EaseMessagesAdapter? = null

    /**
     * The item click listener.
     */
    private var itemMessageClickListener: OnMessageListItemClickListener? = null

    /**
     * The message thread view click listener.
     */
    private var messageThreadViewClickListener: OnMessageChatThreadClickListener? = null

    /**
     * The error listener in chat message list.
     */
    private var chatErrorListener: OnChatErrorListener? = null

    /**
     * The message ack send callback.
     */
    private var messageAckSendCallback: OnMessageAckSendCallback? = null

    private var messageAudioStatusCallback: OnMessageAudioStatusCallback? = null

    /**
     * The label that whether load the latest messages.
     */
    private var isSearchLatestMessages: Boolean = false

    private var baseSearchMessageId: String? = null

    /**
     * The label whether the first time to load data.
     */
    private var isFirstLoadData: Boolean = true


    init {
        initViews()
        initListener()
    }


    private fun initViews() {
        if (viewModel == null) {
            viewModel = if (context is AppCompatActivity) {
                ViewModelProvider(context)[EaseMessageListViewModel::class.java]
            } else {
                EaseMessageListViewModel()
            }
        }
        viewModel?.attach(this)

        binding.messageList.layoutManager = LinearLayoutManager(context)
        messagesAdapter = EaseMessagesAdapter()
        messagesAdapter?.setHasStableIds(true)
        concatAdapter.addAdapter(messagesAdapter!!)
        binding.messageList.adapter = concatAdapter

        // Set not enable to load more.
        binding.messagesRefresh.setEnableLoadMore(false)

        setListStackFromEnd()
    }

    private fun initListener() {
        binding.messagesRefresh.setOnRefreshListener {
            // load more older data
            loadMorePreviousData()
        }
        binding.messagesRefresh.setOnLoadMoreListener {
            // load more newer data
            loadMoreNewerData()
        }

        binding.messageList.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                listScrollController.onScrollStateChanged()
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.messagesRefresh.finishRefresh()
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isFirstLoadData = false
                }
            }
        })

        setAdapterListener()

        if (context is AppCompatActivity) {
            context.lifecycle.addObserver(object : DefaultLifecycleObserver {

                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    if (context.isFinishing) {
                        makeAllMessagesAsRead(true)
                        context.lifecycle.removeObserver(this)
                    }
                }
            })
        }

        binding.messageList.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                if (isFirstLoadData && loadDataType != EaseLoadDataType.SEARCH) {
                    listScrollController.smoothScrollToBottom()
                } else {
                    binding.messageList.removeOnLayoutChangeListener(this)
                }
            }
        })
    }

    private fun setAdapterListener() {
        messagesAdapter?.run {
            setOnMessageListItemClickListener(itemMessageClickListener)
            setOnMessageThreadEventListener(messageThreadViewClickListener)
            setOnMessageReactionErrorListener(object : OnEaseChatReactionErrorListener {

                override fun onError(messageId: String, errorCode: Int, errorMessage: String?) {
                    chatErrorListener?.onChatError(errorCode, errorMessage)
                }
            })
            setOnMessageAckSendCallback(object : OnMessageAckSendCallback {
                override fun onSendAckSuccess(message: ChatMessage?) {
                    super.onSendAckSuccess(message)
                    messageAckSendCallback?.onSendAckSuccess(message)
                }

                override fun onSendAckError(message: ChatMessage?, code: Int, errorMsg: String?) {
                    Log.e(TAG, "onSendAckError: $code, $errorMsg")
                    messageAckSendCallback?.onSendAckError(message, code, errorMsg)
                }
            })
        }
    }

    @JvmOverloads
    fun init(
        conversationId: String?,
        chatConversationType: ChatConversationType?,
        loadDataType: EaseLoadDataType = EaseLoadDataType.LOCAL
    ) {
        this.loadDataType = loadDataType
        conversation = ChatClient.getInstance().chatManager().getConversation(
            conversationId, chatConversationType, true
        )

        viewModel?.setupWithConversation(conversation)

        // If it is chat thread, no more data can be loaded when the layout is pulled down.
        if (loadDataType == EaseLoadDataType.THREAD) {
            binding.messagesRefresh.setEnableRefresh(false)
            isCanAutoScrollToBottom = false
        }

        if (loadDataType == EaseLoadDataType.THREAD || loadDataType == EaseLoadDataType.SEARCH) {
            binding.messagesRefresh.setEnableLoadMore(true)
        }

        // Set whether load data from top or from bottom.
        //setListStackFromEnd()
    }

    /**
     * Call this method to load data.
     * @param messageId When the loadDataType is history, the param needs to be set to search history message list.
     * @param pageSize If you want to change the amount of data pulled each time, you can set parameters.
     */
    fun loadData(messageId: String? = "", pageSize: Int = 10) {
        baseSearchMessageId = messageId
        viewModel?.pageSize = pageSize
        conversation?.run {
            // If is chatroom type, should join the chatroom first
            if (isChatroom(this)) {
                viewModel?.joinChatroom(conversationId())
            } else {
                loadMessages()
            }
        }
    }

    private fun loadMessages() {
        conversation?.run {
            // Mark all message as read
            makeAllMessagesAsRead()
            when (loadDataType) {
                EaseLoadDataType.ROAM -> viewModel?.fetchRoamMessages()
                EaseLoadDataType.SEARCH -> viewModel?.loadLocalHistoryMessages(
                    baseSearchMessageId,
                    ChatSearchDirection.DOWN,
                    true
                )

                EaseLoadDataType.THREAD -> viewModel?.fetchRoamMessages(direction = ChatSearchDirection.DOWN)
                else -> viewModel?.loadLocalMessages()
            }
        }
    }

    private fun makeAllMessagesAsRead(sendEvent: Boolean = false) {
        conversation?.run {
            markAllMessagesAsRead()
            if (sendEvent) {
                sendReadEvent(conversationId())
            }
        }
    }

    private fun sendReadEvent(conversationId: String) {
        // Send update event
        EaseFlowBus.withStick<EaseEvent>(EaseEvent.EVENT.UPDATE.name)
            .post(
                context.mainScope(), EaseEvent(
                    EaseEvent.EVENT.UPDATE.name, EaseEvent.TYPE.CONVERSATION, conversationId
                )
            )
    }

    private fun loadMorePreviousData() {
        if (loadDataType == EaseLoadDataType.THREAD) return
        val firstMsgId = messagesAdapter?.mData?.let {
            if (it.isNotEmpty()) {
                it.first().msgId
            } else ""
        }
        when (loadDataType) {
            EaseLoadDataType.ROAM -> {
                viewModel?.fetchMoreRoamMessages(firstMsgId)
            }

            EaseLoadDataType.SEARCH -> {
                viewModel?.loadLocalHistoryMessages(firstMsgId, ChatSearchDirection.UP)
            }

            else -> {
                viewModel?.loadMoreLocalMessages(firstMsgId)
            }
        }
    }

    private fun loadMoreNewerData() {
        messagesAdapter?.mData?.let {
            if (it.isNotEmpty()) {
                val lastMsgId = it.last().msgId
                when (loadDataType) {
                    EaseLoadDataType.SEARCH -> {
                        viewModel?.loadLocalHistoryMessages(lastMsgId, ChatSearchDirection.DOWN)
                    }

                    EaseLoadDataType.THREAD -> {
                        viewModel?.fetchMoreRoamMessages(lastMsgId, ChatSearchDirection.DOWN)
                    }

                    else -> {}
                }
            } else {
                finishRefresh()
            }
        }
    }

    private fun setListStackFromEnd() {
        binding.messageList.layoutManager?.let {
            if (it is LinearLayoutManager) {
                it.stackFromEnd = loadDataType.isShouldStackFromEnd()
            }
            if (it is EaseCustomLayoutManager) {
                it.setIsStackFromEnd(loadDataType.isShouldStackFromEnd())
            }
        }
    }

    override val currentConversation: ChatConversation?
        get() = this.conversation
    override var isCanAutoScrollToBottom: Boolean
        get() = listScrollController.isCanAutoScrollToBottom()
        set(value) = listScrollController.setCanAutoScrollToBottom(value)

    private fun isChatroom(conv: ChatConversation): Boolean {
        return conv.type == ChatConversationType.ChatRoom && loadDataType != EaseLoadDataType.THREAD
    }

    fun isGroupChat(conv: ChatConversation): Boolean {
        return conv.type == ChatConversationType.GroupChat && loadDataType != EaseLoadDataType.THREAD
    }

    private fun isSingleChat(conv: ChatConversation): Boolean {
        return conv.type == ChatConversationType.Chat
    }

    private fun finishRefresh() {
        context.mainScope().launch {
            binding.messagesRefresh.finishRefresh()
            binding.messagesRefresh.finishLoadMore()
        }
    }

    private fun enableLoadMore(enable: Boolean) {
        context.mainScope().launch {
            binding.messagesRefresh.setEnableLoadMore(enable)
        }
    }

    override val refreshLayout: SmartRefreshLayout?
        get() = binding.messagesRefresh
    override val messageListLayout: RecyclerView?
        get() = binding.messageList

    override fun setViewModel(viewModel: IChatMessageListRequest?) {
        this.viewModel = viewModel
        this.viewModel?.let {
            it.attach(this)
            it.setupWithConversation(conversation)
        }
    }

    override fun setMessagesAdapter(adapter: EaseMessagesAdapter?) {
//        adapter?.let {
//            if (this.messagesAdapter != null && concatAdapter.adapters
//                    .contains(this.messagesAdapter)
//            ) {
//                val index: Int = concatAdapter.adapters.indexOf(this.messagesAdapter)
//                concatAdapter.removeAdapter(messagesAdapter!!)
//                it.setHasStableIds(true)
//                concatAdapter.addAdapter(index, it)
//            } else {
//                it.setHasStableIds(true)
//                concatAdapter.addAdapter(it)
//            }
//            this.messagesAdapter = it
//            setAdapterListener()
//            notifyDataSetChanged()
//        }
    }

    override fun getMessagesAdapter(): EaseMessagesAdapter? {
        return messagesAdapter
    }

    override fun setOnMessageListItemClickListener(listener: OnMessageListItemClickListener?) {
        this.itemMessageClickListener = listener
        messagesAdapter?.let {
            it.setOnMessageListItemClickListener(listener)
            it.notifyDataSetChanged()
        }
    }

    override fun setOnMessageThreadViewClickListener(listener: OnMessageChatThreadClickListener?) {
        this.messageThreadViewClickListener = listener
        messagesAdapter?.let {
            it.setOnMessageThreadEventListener(listener)
            it.notifyDataSetChanged()
        }
    }

    fun setAudioPaying(message: ChatMessage, isPlaying: Boolean) {
        listScrollController.setAudioPlaying(message, isPlaying)
    }

    fun setAudioRecognizing(message: ChatMessage, isRecognizing: Boolean) {
        listScrollController.setAudioRecognizing(message, isRecognizing)
    }

    fun setAudioReset(message: ChatMessage) {
        listScrollController.setAudioReset(message)
    }

    override fun setOnMessageAudioStatusCallback(listener: OnMessageAudioStatusCallback?) {
        this.messageAudioStatusCallback = listener
    }

    override fun setOnMessageAckSendCallback(callback: OnMessageAckSendCallback?) {
        this.messageAckSendCallback = callback
    }

    override fun setOnChatErrorListener(listener: OnChatErrorListener?) {
        this.chatErrorListener = listener
    }

    override fun useDefaultRefresh(useDefaultRefresh: Boolean) {
        if (!useDefaultRefresh) {
            binding.messagesRefresh.setEnableLoadMore(false)
            binding.messagesRefresh.setEnableRefresh(false)
        }
    }

    override fun refreshMessages() {
        if (loadDataType != EaseLoadDataType.SEARCH || (loadDataType == EaseLoadDataType.SEARCH && isSearchLatestMessages)) {
            viewModel?.getAllCacheMessages()
        }
    }

    override fun refreshToLatest() {
        if (loadDataType != EaseLoadDataType.SEARCH || (loadDataType == EaseLoadDataType.SEARCH && isSearchLatestMessages)) {
            viewModel?.getAllCacheMessages()
        }
        listScrollController.scrollToBottom(true)
    }

    override fun scrollToBottom(isRefresh: Boolean) {
        if (isRefresh) {
            if (loadDataType != EaseLoadDataType.SEARCH || (loadDataType == EaseLoadDataType.SEARCH && isSearchLatestMessages)) {
                viewModel?.getAllCacheMessages()
            }
            listScrollController.scrollToBottom(true)
        } else {
            listScrollController.scrollToBottom()
        }
    }

    override fun refreshMessage(messageId: String?) {
        refreshMessage(messageId?.let { ChatClient.getInstance().chatManager().getMessage(it) })
    }

    override fun refreshMessage(message: ChatMessage?) {
        listScrollController.refreshMessage(message)
    }

    override fun removeMessage(message: ChatMessage?) {
        viewModel?.removeMessage(
            message,
            (loadDataType == EaseLoadDataType.THREAD || loadDataType == EaseLoadDataType.ROAM)
                    && message?.status() == ChatMessageStatus.SUCCESS
        )
    }

    override fun addMessageToLast(message: ChatMessage?) {
        listScrollController.addMessageToLast(message)
    }

    override fun moveToTarget(position: Int) {
        listScrollController.smoothScrollToPosition(position)
    }

    override fun moveToTarget(message: ChatMessage?) {
        if (message == null || messagesAdapter == null || messagesAdapter?.mData == null) {
            Log.e(TAG, "moveToTarget failed: message is null or messageAdapter is null")
            return
        }
        val position = messagesAdapter?.mData?.indexOfFirst {
            it.msgId == message.msgId
        } ?: -1
        if (position >= 0) {
            listScrollController.scrollToTargetMessage(position) {

            }
        } else {
            messagesAdapter?.mData?.get(0)?.msgId?.let { msgId ->
                viewModel?.loadMoreRetrievalsMessages(msgId, 100)
                listScrollController.setTargetScrollMsgId(message.msgId)
            }
        }
    }


    override fun setRefreshing(refreshing: Boolean) {
        binding.messagesRefresh.setEnableRefresh(refreshing)
    }

    override fun isNeedScrollToBottomWhenViewChange(isNeedToScrollBottom: Boolean) {
        listScrollController.setNeedScrollToBottomWhenViewChange(isNeedToScrollBottom)
    }


    override fun removeAdapter(adapter: RecyclerView.Adapter<*>?) {
        adapter?.let {
            concatAdapter.removeAdapter(it)
        }
    }

    override fun joinChatRoomSuccess(value: Chatroom?) {

    }

    override fun joinChatRoomFail(error: Int, errorMsg: String?) {
        chatErrorListener?.onChatError(error, errorMsg)
    }

    override fun leaveChatRoomSuccess() {
        loadMessages()
    }

    override fun leaveChatRoomFail(error: Int, errorMsg: String?) {
        chatErrorListener?.onChatError(error, errorMsg)
    }

    override fun getAllMessagesSuccess(messages: List<ChatMessage>) {
        listScrollController.refreshMessages(messages)
    }

    override fun getAllMessagesFail(error: Int, errorMsg: String?) {
        chatErrorListener?.onChatError(error, errorMsg)
    }

    override fun loadLocalMessagesSuccess(messages: List<ChatMessage>) {
        finishRefresh()
        viewModel?.getAllCacheMessages()
//        listScrollController.scrollToBottom()
    }

    override fun loadLocalMessagesFail(error: Int, errorMsg: String?) {
        finishRefresh()
    }

    override fun loadMoreLocalMessagesSuccess(messages: List<ChatMessage>) {
        finishRefresh()
        if (messages.isNotEmpty()) {
            messagesAdapter?.let {
                it.addData(0, messages.toMutableList(), false)
                it.notifyItemRangeInserted(0, messages.size)
            }
        }
    }

    override fun loadMoreLocalMessagesFail(error: Int, errorMsg: String?) {
        finishRefresh()
    }

    override fun fetchRoamMessagesSuccess(messages: List<ChatMessage>) {
        finishRefresh()
        if (loadDataType == EaseLoadDataType.THREAD) {
            if (messages.size < (viewModel?.pageSize ?: 10)) {
                enableLoadMore(false)
            }
            if (messages.isNotEmpty()) {
                listScrollController.refreshMessages(messages)
            }
        } else {
            viewModel?.getAllCacheMessages()
            listScrollController.scrollToBottom()
        }
    }

    override fun fetchRoamMessagesFail(error: Int, errorMsg: String?) {
        finishRefresh()
    }

    override fun fetchMoreRoamMessagesSuccess(messages: List<ChatMessage>) {
        finishRefresh()
        if (messages.isNotEmpty()) {
            if (loadDataType == EaseLoadDataType.THREAD) {
                if (messages.size < (viewModel?.pageSize ?: 10)) {
                    enableLoadMore(false)
                }
            }

            messagesAdapter?.let {
                val targetPosition = it.itemCount
                viewModel?.getAllCacheMessages()
                listScrollController.scrollToPosition(targetPosition)
            }

        }
    }

    override fun fetchMoreRoamMessagesFail(error: Int, errorMsg: String?) {
        finishRefresh()
    }

    override fun loadLocalHistoryMessagesSuccess(messages: List<ChatMessage>, direction: ChatSearchDirection) {
        finishRefresh()
        if (direction == ChatSearchDirection.UP) {
            messagesAdapter?.addData(0, messages.toMutableList())
            if (messages.isNotEmpty()) {
                listScrollController.scrollToPosition(messages.size - 1)
            }
        } else {
            messagesAdapter?.let {
                it.addData(messages.toMutableList())
            }
            if (messages.isEmpty() || messages.size < (viewModel?.pageSize ?: 10)) {
                enableLoadMore(false)
                isSearchLatestMessages = true
            }
        }
    }

    override fun loadLocalHistoryMessagesFail(error: Int, errorMsg: String?) {
        finishRefresh()
    }

    override fun loadMoreRetrievalsMessagesSuccess(messages: List<ChatMessage>) {
        viewModel?.getAllCacheMessages()
        listScrollController.scrollToTargetMessage {

        }
    }

    override fun removeMessageSuccess(message: ChatMessage?) {
        viewModel?.getAllCacheMessages()
        //listController.removeMessage(message)
    }

    override fun removeMessageFail(error: Int, errorMsg: String?) {
        chatErrorListener?.onChatError(error, errorMsg)
    }
}
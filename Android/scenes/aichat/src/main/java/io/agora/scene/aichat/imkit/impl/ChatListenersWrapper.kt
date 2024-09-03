package io.agora.scene.aichat.imkit.impl

import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConnectionListener
import io.agora.scene.aichat.imkit.ChatContactListener
import io.agora.scene.aichat.imkit.ChatConversationListener
import io.agora.scene.aichat.imkit.ChatEventResultListener
import io.agora.scene.aichat.imkit.ChatGroup
import io.agora.scene.aichat.imkit.ChatGroupChangeListener
import io.agora.scene.aichat.imkit.ChatGroupReadAck
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageListener
import io.agora.scene.aichat.imkit.ChatMessageReactionChange
import io.agora.scene.aichat.imkit.ChatMultiDeviceListener
import io.agora.scene.aichat.imkit.ChatPresence
import io.agora.scene.aichat.imkit.ChatPresenceListener
import io.agora.scene.aichat.imkit.ChatRoomChangeListener
import io.agora.scene.aichat.imkit.ChatShareFile
import io.agora.scene.aichat.imkit.ChatThreadChangeListener
import io.agora.scene.aichat.imkit.ChatThreadEvent
import io.agora.scene.aichat.imkit.extensions.getUserInfo
import io.agora.scene.aichat.imkit.helper.EaseAtMessageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

internal class ChatListenersWrapper : ChatConnectionListener, ChatMessageListener, ChatGroupChangeListener,
    ChatContactListener, ChatConversationListener, ChatPresenceListener,
    ChatMultiDeviceListener, ChatThreadChangeListener {

    private val chatConnectionListener = Collections.synchronizedList(mutableListOf<ChatConnectionListener>())
    private val chatMessageListener = Collections.synchronizedList(mutableListOf<ChatMessageListener>())
    private val chatGroupChangeListener = Collections.synchronizedList(mutableListOf<ChatGroupChangeListener>())
    private val chatContactListener = Collections.synchronizedList(mutableListOf<ChatContactListener>())
    private val chatConversationListener = Collections.synchronizedList(mutableListOf<ChatConversationListener>())
    private val chatPresenceListener = Collections.synchronizedList(mutableListOf<ChatPresenceListener>())
    private val chatRoomChangeListener = Collections.synchronizedList(mutableListOf<ChatRoomChangeListener>())
    private val chatMultiDeviceListener = Collections.synchronizedList(mutableListOf<ChatMultiDeviceListener>())
    private val eventResultListener = Collections.synchronizedList(mutableListOf<ChatEventResultListener>())
    private val chatThreadChangeListener = Collections.synchronizedList(mutableListOf<ChatThreadChangeListener>())

    fun addListeners() {
        ChatClient.getInstance().addConnectionListener(this)
        ChatClient.getInstance().chatManager().addMessageListener(this)
        ChatClient.getInstance().chatManager().addConversationListener(this)
        ChatClient.getInstance().groupManager().addGroupChangeListener(this)
        ChatClient.getInstance().contactManager().setContactListener(this)
        ChatClient.getInstance().presenceManager().addListener(this)
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(chatroomListener)
        ChatClient.getInstance().addMultiDeviceListener(this)
        ChatClient.getInstance().chatThreadManager().addChatThreadChangeListener(this)
    }

    fun removeListeners() {
        ChatClient.getInstance().removeConnectionListener(this)
        ChatClient.getInstance().chatManager().removeMessageListener(this)
        ChatClient.getInstance().chatManager().removeConversationListener(this)
        ChatClient.getInstance().groupManager().removeGroupChangeListener(this)
        ChatClient.getInstance().contactManager().removeContactListener(this)
        ChatClient.getInstance().presenceManager().removeListener(this)
        ChatClient.getInstance().chatroomManager().removeChatRoomListener(chatroomListener)
        ChatClient.getInstance().removeMultiDeviceListener(this)
        ChatClient.getInstance().chatThreadManager().removeChatThreadChangeListener(this)

        chatConnectionListener.clear()
        chatMessageListener.clear()
        chatGroupChangeListener.clear()
        chatContactListener.clear()
        chatConversationListener.clear()
        chatPresenceListener.clear()
        chatRoomChangeListener.clear()
        chatMultiDeviceListener.clear()
        chatThreadChangeListener.clear()
    }

    fun addConnectionListener(listener: ChatConnectionListener) {
        if (chatConnectionListener.contains(listener)) return
        chatConnectionListener.add(listener)
    }

    fun removeConnectionListener(listener: ChatConnectionListener) {
        chatConnectionListener.remove(listener)
    }

    fun addChatMessageListener(listener: ChatMessageListener) {
        if (chatMessageListener.contains(listener)) return
        chatMessageListener.add(listener)
    }

    fun removeChatMessageListener(listener: ChatMessageListener) {
        chatMessageListener.remove(listener)
    }

    fun addGroupChangeListener(listener: ChatGroupChangeListener) {
        if (chatGroupChangeListener.contains(listener)) return
        chatGroupChangeListener.add(listener)
    }

    fun removeGroupChangeListener(listener: ChatGroupChangeListener) {
        chatGroupChangeListener.remove(listener)
    }

    fun addContactListener(listener: ChatContactListener) {
        if (chatContactListener.contains(listener)) return
        chatContactListener.add(listener)
    }

    fun removeContactListener(listener: ChatContactListener) {
        chatContactListener.remove(listener)
    }

    fun addConversationListener(listener: ChatConversationListener) {
        if (chatConversationListener.contains(listener)) return
        chatConversationListener.add(listener)
    }

    fun removeConversationListener(listener: ChatConversationListener) {
        chatConversationListener.remove(listener)
    }

    fun addPresenceListener(listener: ChatPresenceListener) {
        if (chatPresenceListener.contains(listener)) return
        chatPresenceListener.add(listener)
    }

    fun removePresenceListener(listener: ChatPresenceListener) {
        chatPresenceListener.remove(listener)
    }

    fun addChatRoomChangeListener(listener: ChatRoomChangeListener) {
        if (chatRoomChangeListener.contains(listener)) return
        chatRoomChangeListener.add(listener)
    }

    fun removeChatRoomChangeListener(listener: ChatRoomChangeListener) {
        chatRoomChangeListener.remove(listener)
    }

    fun addMultiDeviceListener(listener: ChatMultiDeviceListener) {
        if (chatMultiDeviceListener.contains(listener)) return
        chatMultiDeviceListener.add(listener)
    }

    fun removeMultiDeviceListener(listener: ChatMultiDeviceListener) {
        chatMultiDeviceListener.remove(listener)
    }

    fun addEventResultListener(listener: ChatEventResultListener) {
        if (eventResultListener.contains(listener)) return
        eventResultListener.add(listener)
    }

    fun removeEventResultListener(listener: ChatEventResultListener) {
        eventResultListener.remove(listener)
    }

    fun addThreadChangeListener(listener: ChatThreadChangeListener) {
        chatThreadChangeListener.add(listener)
    }

    fun removeThreadChangeListener(listener: ChatThreadChangeListener) {
        chatThreadChangeListener.remove(listener)
    }

    companion object {

        private var instance: ChatListenersWrapper? = null

        fun getInstance(): ChatListenersWrapper {
            if (instance == null) {
                synchronized(ChatListenersWrapper::class.java) {
                    if (instance == null) {
                        instance = ChatListenersWrapper()
                    }
                }
            }
            return instance!!
        }
    }

    /**  ChatConnectionListener  */
    override fun onConnected() {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onConnected()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDisconnected(errorCode: Int) {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onDisconnected(errorCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onTokenWillExpire() {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onTokenWillExpire()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onTokenExpired() {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onTokenExpired()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onLogout(errorCode: Int) {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onLogout(errorCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ChatConnectionListener  */
    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        // Update message userinfo when receive messages.
        CoroutineScope(Dispatchers.IO).launch {
            messages?.forEach { msg ->
                msg.getUserInfo(true)
            }
            try {
                EaseAtMessageHelper.get().parseMessages(messages)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageReceived(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onCmdMessageReceived(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupMessageRead(groupReadAcks: MutableList<ChatGroupReadAck>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onGroupMessageRead(groupReadAcks)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageChanged(message: ChatMessage?, change: Any?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageChanged(message, change)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageRecalled(messages: MutableList<io.agora.chat.ChatMessage>?) {
        super.onMessageRecalled(messages)
    }

    override fun onMessageDelivered(messages: MutableList<ChatMessage>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageDelivered(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageRead(messages: MutableList<ChatMessage>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageRead(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onReactionChanged(messageReactionChangeList: MutableList<ChatMessageReactionChange>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onReactionChanged(messageReactionChangeList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onReadAckForGroupMessageUpdated() {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onReadAckForGroupMessageUpdated()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  GroupChangeListener  */
    override fun onInvitationReceived(
        groupId: String?,
        groupName: String?,
        inviter: String?,
        reason: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onInvitationReceived(groupId, groupName, inviter, reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinReceived(
        groupId: String?,
        groupName: String?,
        applicant: String?,
        reason: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinReceived(groupId, groupName, applicant, reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinAccepted(groupId: String?, groupName: String?, accepter: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinAccepted(groupId, groupName, accepter)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onInvitationAccepted(groupId: String?, invitee: String?, reason: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onInvitationAccepted(groupId, invitee, reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onInvitationDeclined(groupId: String?, invitee: String?, reason: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onInvitationDeclined(groupId, invitee, reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onUserRemoved(groupId: String?, groupName: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onUserRemoved(groupId, groupName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupDestroyed(groupId: String?, groupName: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onGroupDestroyed(groupId, groupName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAutoAcceptInvitationFromGroup(
        groupId: String?,
        inviter: String?,
        inviteMessage: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAutoAcceptInvitationFromGroup(groupId, inviter, inviteMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMuteListAdded(groupId: String?, mutes: MutableList<String>?, muteExpire: Long) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMuteListAdded(groupId, mutes, muteExpire)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMuteListRemoved(groupId: String?, mutes: MutableList<String>?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMuteListRemoved(groupId, mutes)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onWhiteListAdded(groupId: String?, whitelist: MutableList<String>?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onWhiteListAdded(groupId, whitelist)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onWhiteListRemoved(groupId: String?, whitelist: MutableList<String>?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onWhiteListRemoved(groupId, whitelist)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAllMemberMuteStateChanged(groupId: String?, isMuted: Boolean) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAllMemberMuteStateChanged(groupId, isMuted)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAdminAdded(groupId: String?, administrator: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAdminAdded(groupId, administrator)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAdminRemoved(groupId: String?, administrator: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAdminRemoved(groupId, administrator)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onOwnerChanged(groupId, newOwner, oldOwner)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMemberJoined(groupId: String?, member: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMemberJoined(groupId, member)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMemberExited(groupId: String?, member: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMemberExited(groupId, member)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAnnouncementChanged(groupId: String?, announcement: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAnnouncementChanged(groupId, announcement)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSharedFileAdded(groupId: String?, sharedFile: ChatShareFile?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onSharedFileAdded(groupId, sharedFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSharedFileDeleted(groupId: String?, fileId: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onSharedFileDeleted(groupId, fileId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSpecificationChanged(group: ChatGroup?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onSpecificationChanged(group)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinDeclined(
        groupId: String,
        groupName: String,
        decliner: String,
        reason: String,
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinDeclined(groupId, groupName, decliner, reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStateChanged(group: ChatGroup?, isDisabled: Boolean) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onStateChanged(group, isDisabled)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ContactListener  */
    override fun onContactAdded(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onContactAdded(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onContactDeleted(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onContactDeleted(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onContactInvited(username: String?, reason: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onContactInvited(username, reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFriendRequestAccepted(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onFriendRequestAccepted(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFriendRequestDeclined(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onFriendRequestDeclined(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ConversationListener  */
    override fun onConversationUpdate() {
        chatConversationListener.let {
            for (conversationListener in it) {
                try {
                    conversationListener.onConversationUpdate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onConversationRead(from: String?, to: String?) {
        chatConversationListener.let {
            for (conversationListener in it) {
                try {
                    conversationListener.onConversationRead(from, to)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  PresenceListener  */
    override fun onPresenceUpdated(presences: MutableList<ChatPresence>?) {
        chatPresenceListener.let {
            for (conversationListener in it) {
                try {
                    conversationListener.onPresenceUpdated(presences)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ChatRoomChangeListener  */
    private val chatroomListener by lazy {
        object : ChatRoomChangeListener {

            override fun onChatRoomDestroyed(roomId: String?, roomName: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onChatRoomDestroyed(roomId, roomName)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onMemberJoined(roomId: String?, participant: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onMemberJoined(roomId, participant)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onMemberExited(roomId, roomName, participant)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onRemovedFromChatRoom(
                reason: Int,
                roomId: String?,
                roomName: String?,
                participant: String?
            ) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onRemovedFromChatRoom(reason, roomId, roomName, participant)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onMuteListAdded(
                chatRoomId: String?,
                mutes: MutableList<String>?,
                expireTime: Long
            ) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onMuteListAdded(chatRoomId, mutes, expireTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onMuteListRemoved(chatRoomId, mutes)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onWhiteListAdded(chatRoomId: String?, whitelist: MutableList<String>?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onWhiteListAdded(chatRoomId, whitelist)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onWhiteListRemoved(chatRoomId: String?, whitelist: MutableList<String>?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onWhiteListRemoved(chatRoomId, whitelist)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onAllMemberMuteStateChanged(chatRoomId, isMuted)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onAdminAdded(chatRoomId: String?, admin: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onAdminAdded(chatRoomId, admin)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onAdminRemoved(chatRoomId: String?, admin: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onAdminRemoved(chatRoomId, admin)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onOwnerChanged(chatRoomId, newOwner, oldOwner)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onAnnouncementChanged(chatRoomId: String?, announcement: String?) {
                chatRoomChangeListener.let {
                    for (chatroomListener in it) {
                        try {
                            chatroomListener.onAnnouncementChanged(chatRoomId, announcement)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    /**  MultiDeviceListener  */
    override fun onContactEvent(event: Int, target: String?, ext: String?) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onContactEvent(event, target, ext)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupEvent(event: Int, target: String?, usernames: MutableList<String>?) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onGroupEvent(event, target, usernames)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageRemoved(conversationId: String?, deviceId: String?) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onMessageRemoved(conversationId, deviceId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadEvent(event: Int, target: String?, usernames: MutableList<String>?) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onChatThreadEvent(event, target, usernames)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ThreadChangeListener  */
    override fun onChatThreadCreated(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadCreated(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadUpdated(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadUpdated(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadDestroyed(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadDestroyed(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadUserRemoved(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadUserRemoved(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Synchronized
    internal fun callbackEvent(function: String, errorCode: Int, errorMessage: String?) {
        if (eventResultListener.isEmpty()) {
            return
        }
        eventResultListener.iterator().forEach { listener ->
            listener.onEventResult(function, errorCode, errorMessage)
        }
    }

}
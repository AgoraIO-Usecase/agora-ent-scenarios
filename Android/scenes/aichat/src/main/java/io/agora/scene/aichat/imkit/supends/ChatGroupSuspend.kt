package io.agora.scene.aichat.imkit.supends

import io.agora.scene.aichat.imkit.ChatCursorResult
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatGroup
import io.agora.scene.aichat.imkit.ChatGroupManager
import io.agora.scene.aichat.imkit.ChatGroupOptions
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspend method for [ChatGroupManager.createGroup()]
 * @param groupName
 * @param desc
 * @param members
 * @param reason
 * @param options
 * @return ChatGroup
 */
suspend fun ChatGroupManager.createChatGroup(
    groupName: String,
    desc: String,
    members: MutableList<String>,
    reason: String,
    options: ChatGroupOptions,
): ChatGroup {
    return suspendCoroutine { continuation ->
        asyncCreateGroup(groupName, desc, members.toTypedArray(), reason, options, ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}


/**
 * Suspend method for [ChatGroupManager.fetchGroupDetails()]
 * @param groupId
 * @return ChatGroup
 */
suspend fun ChatGroupManager.fetchGroupDetails(groupId: String): ChatGroup {
    return suspendCoroutine { continuation ->
        asyncGetGroupFromServer(groupId, ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.fetchGroupDetails()]
 * @param groupId
 * @return ChatGroup
 */
suspend fun ChatGroupManager.fetchChatGroupMembers(
    groupId: String,
    cursor: String?,
    pageSize: Int,
): ChatCursorResult<String> {
    return suspendCoroutine { continuation ->
        asyncFetchGroupMembers(groupId, cursor, pageSize, ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}


/**
 * Suspend method for [ChatGroupManager.fetchJoinedGroupsFromServer()]
 * @return MutableList<ChatGroup>
 */
suspend fun ChatGroupManager.fetchJoinedGroupsFromServer(
    page: Int, pageSize: Int, needMemberCount: Boolean, needRole: Boolean
): MutableList<ChatGroup> {
    return suspendCoroutine { continuation ->
        asyncGetJoinedGroupsFromServer(page, pageSize, needMemberCount, needRole, ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}


/**
 * Suspend method for [ChatGroupManager.joinChatGroup()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.joinChatGroup(groupId: String): Int {
    return suspendCoroutine { continuation ->
        asyncJoinGroup(groupId, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.leaveGroup()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.leaveChatGroup(groupId: String): Int {
    return suspendCoroutine { continuation ->
        asyncLeaveGroup(groupId, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.destroyChatGroup()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.destroyChatGroup(groupId: String): Int {
    return suspendCoroutine { continuation ->
        asyncDestroyGroup(groupId, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}


/**
 * Suspend method for [ChatGroupManager.addGroupMember()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.addGroupMember(
    groupId: String,
    members: MutableList<String>,
): Int {
    return suspendCoroutine { continuation ->
        asyncAddUsersToGroup(groupId, members.toTypedArray(), CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.removeGroupMember()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.removeChatGroupMember(
    groupId: String,
    members: MutableList<String>,
): Int {
    return suspendCoroutine { continuation ->
        asyncRemoveUsersFromGroup(groupId, members, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.changeChatGroupName()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.changeChatGroupName(
    groupId: String,
    newName: String,
): Int {
    return suspendCoroutine { continuation ->
        asyncChangeGroupName(groupId, newName, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}


/**
 * Suspend method for [ChatGroupManager.changeChatGroupDescription()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.changeChatGroupDescription(
    groupId: String,
    description: String,
): Int {
    return suspendCoroutine { continuation ->
        asyncChangeGroupDescription(groupId, description, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.changeChatGroupOwner()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.changeChatGroupOwner(
    groupId: String,
    newOwner: String,
): ChatGroup {
    return suspendCoroutine { continuation ->
        asyncChangeOwner(groupId, newOwner, ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}


/**
 * Suspend method for [ChatGroupManager.fetchGroupMemberAllAttributes()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.fetchGroupMemberAllAttributes(
    groupId: String,
    userList: List<String>,
    keyList: List<String>,
): MutableMap<String, MutableMap<String, String>> {
    return suspendCoroutine { continuation ->
        asyncFetchGroupMembersAttributes(groupId, userList, keyList, ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatGroupManager.setGroupMemberAttributes()]
 * @return The result of the request.
 */
suspend fun ChatGroupManager.setGroupMemberAttributes(
    groupId: String,
    userId: String,
    attributeMap: MutableMap<String, String>
): Int {
    return suspendCoroutine { continuation ->
        asyncSetGroupMemberAttributes(groupId, userId, attributeMap, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}
package io.agora.scene.aichat.imkit.supends

import io.agora.scene.aichat.imkit.ChatContactManager
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import io.agora.scene.aichat.imkit.model.EaseUser
import io.agora.scene.aichat.imkit.model.toUser
import io.agora.scene.aichat.imkit.provider.getSyncUser
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Suspend method for [ChatContactManager.fetchContactsFromServer()]
 * @return List<EaseUser> User Information List
 */
suspend fun ChatContactManager.fetchContactsFromServer():List<EaseUser>{
    return suspendCoroutine{ continuation ->
        asyncGetAllContactsFromServer(ValueCallbackImpl(
               onSuccess = { value ->
                   value?.let {
                       val list = it.map { contact ->
                           EaseIM.getUserProvider()?.getSyncUser(contact)?.toUser() ?: EaseUser(contact)
                       }
                       continuation.resume(list)
                   }
               },
               onError = {code,message-> continuation.resumeWithException(ChatException(code, message)) }
           ))
    }
}

/**
 * Suspend method for [ChatContactManager.addNewContact(userName,reason)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.addNewContact(userName:String, reason:String?): String{
    return suspendCoroutine{ continuation ->
        asyncAddContact(userName,reason, CallbackImpl(
            function = EaseConstant.API_ASYNC_ADD_CONTACT,
            onSuccess = { continuation.resume(userName) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

/**
 * Suspend method for [ChatContactManager.removeContact(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.removeContact(userName:String, keepConversation: Boolean?): Int{
    return suspendCoroutine{ continuation ->
        if (keepConversation == null){
            asyncDeleteContact(userName, CallbackImpl(
                onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
                onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
            ))
        }else{
            try {
                deleteContact(userName,keepConversation)
                continuation.resume(ChatError.EM_NO_ERROR)
            } catch (e: ChatException) {
                continuation.resumeWithException(e)
            }
        }

    }
}

/**
 * Suspend method for [ChatContactManager.getBlackListFromServer(userName)]
 * @return List<EaseUser> User Information List
 */
suspend fun ChatContactManager.fetchBlackListFromServer(): MutableList<EaseUser>{
    return suspendCoroutine{ continuation ->
        asyncGetBlackListFromServer(ValueCallbackImpl(
            onSuccess = { value ->
                value?.let {
                    val list = it.map { id ->
                        EaseIM.getUserProvider()?.getSyncUser(id)?.toUser() ?: EaseUser(id)
                    }.toMutableList()
                    continuation.resume(list)
                }
            },
            onError = {code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}


/**
 * Suspend method for [ChatContactManager.addUserToBlackList(userName,both)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.addToBlackList(userList:MutableList<String>): Int{
    return suspendCoroutine{ continuation ->
        asyncSaveBlackList(userList, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

/**
 * Suspend method for [ChatContactManager.removeUserFromBlackList(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.deleteUserFromBlackList(userName:String): Int{
    return suspendCoroutine{ continuation ->
        asyncRemoveUserFromBlackList(userName, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

/**
 * Suspend method for [ChatContactManager.acceptInvitation(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.acceptContactInvitation(userName:String): Int{
    return suspendCoroutine{ continuation ->
        asyncAcceptInvitation(userName, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}


/**
 * Suspend method for [ChatContactManager.declineInvitation(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.declineContactInvitation(userName:String): Int{
    return suspendCoroutine{ continuation ->
        asyncDeclineInvitation(userName, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

suspend fun ChatContactManager.searchContact(query:String):MutableList<EaseUser>{
    return suspendCoroutine{ continuation ->
        val localContact = contactsFromLocal
        val resultList = mutableListOf<EaseUser>()
        localContact.forEach{
            val userInfo = EaseIM.getCache().getUser(it)
            if (userInfo == null){
                if (it.contains(query)){
                    resultList.add(EaseUser(it))
                }
            }else{
                userInfo.let { user->
                    val nickname = user.getRemarkOrName()
                    if (nickname.contains(query)){
                        resultList.add(user.toUser())
                    }
                }
            }
        }
        continuation.resume(resultList)
    }
}

suspend fun ChatContactManager.searchBlockContact(query:String):MutableList<EaseUser>{
    return suspendCoroutine{ continuation ->
        val localBlockContact = blackListUsernames
        val resultList = mutableListOf<EaseUser>()
        localBlockContact.forEach{
            val userInfo = EaseIM.getCache().getUser(it)
            if (userInfo == null){
                if (it.contains(query)){
                    resultList.add(EaseUser(it))
                }
            }else{
                userInfo.let { user->
                    val nickname = user.getRemarkOrName()
                    if (nickname.contains(query)){
                        resultList.add(user.toUser())
                    }
                }
            }
        }
        continuation.resume(resultList)
    }
}

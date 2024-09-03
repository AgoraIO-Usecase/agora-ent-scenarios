package io.agora.scene.aichat.imkit.helper

import android.text.TextUtils
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatLog
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.extensions.isGroupChat
import org.json.JSONArray
import java.util.Locale

class EaseAtMessageHelper private constructor() {
    private val toAtUserList: MutableList<String> = ArrayList()
    private val atMeGroupList: MutableSet<String> by lazy {
        mutableSetOf()
    }
    private var groupId: String? = null

    /**
     * Set up with conversation.
     */
    fun setupWithConversation(conversationId: String?) {
        ChatClient.getInstance().chatManager().getConversation(conversationId)?.let {
            if (it.isGroupChat) {
                groupId = it.conversationId()
            }
        }
    }

    /**
     * add user you want to @
     * @param username
     */
    fun addAtUser(username: String) {
        synchronized(toAtUserList) {
            if (!toAtUserList.contains(username)) {
                toAtUserList.add(username)
            }
        }
    }

    /**
     * check if be mentioned(@) in the content
     * @param content
     * @return
     */
    fun containsAtUsername(content: String?): Boolean {
        if (TextUtils.isEmpty(content)) {
            return false
        }
        synchronized(toAtUserList) {
            for (username in toAtUserList) {
                var nick = groupId?.let {
                    EaseProfile.getGroupMember(it, username)?.getRemarkOrName()
                } ?: EaseIM.getUserProvider()?.getUser(username)?.name
                ?: username
                if (content!!.contains(nick)) {
                    return true
                }
            }
        }
        return false
    }

    fun containsAtAll(content: String?): Boolean {
        val atAll = "@All".uppercase()
        return content?.uppercase(Locale.getDefault())?.contains(atAll) ?: false
    }

    /**
     * get the users be mentioned(@)
     * @param content
     * @return
     */
    fun getAtMessageUsernames(content: String): List<String?>? {
        if (TextUtils.isEmpty(content)) {
            return null
        }
        synchronized(toAtUserList) {
            var list: MutableList<String?>? = null
            for (username in toAtUserList) {
                var nick = groupId?.let {
                    EaseProfile.getGroupMember(it, username)?.getRemarkOrName()
                } ?: EaseIM.getUserProvider()?.getUser(username)?.name
                ?: username
                if (content.contains(nick)) {
                    if (list == null) {
                        list = ArrayList()
                    }
                    list.add(username)
                }
            }
            return list
        }
    }

    /**
     * parse the message, get and save group id if I was mentioned(@)
     * @param messages
     */
    fun parseMessages(messages: List<ChatMessage?>?) {
        val size = atMeGroupList.size
        messages?.let {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                iterator.next()?.run {
                    if (isGroupChat()) {
                        val groupId: String = to
                        try {
                            val jsonArray: JSONArray =
                                getJSONArrayAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG)
                            for (i in 0 until jsonArray.length()) {
                                val username = jsonArray.getString(i)
                                if (ChatClient.getInstance().currentUser.equals(username)) {
                                    if (!atMeGroupList.contains(groupId)) {
                                        atMeGroupList.add(groupId)
                                        break
                                    }
                                }
                            }
                        } catch (e1: Exception) {
                            //Determine whether is @ all message
                            val usernameStr: String? =
                                getStringAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, null)
                            if (usernameStr != null) {
                                val s = usernameStr.uppercase(Locale.getDefault())
                                if (s == EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL) {
                                    if (!atMeGroupList.contains(groupId)) {
                                        atMeGroupList.add(groupId)
                                        ChatLog.d(
                                            "EaseAtMessageHelper",
                                            "atMeGroupList: $atMeGroupList"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val atMeGroups: Set<String>
        /**
         * get groups which I was mentioned
         * @return
         */
        get() = atMeGroupList

    /**
     * remove group from the list
     * @param groupId
     */
    fun removeAtMeGroup(groupId: String?) {
        if (atMeGroupList.contains(groupId)) {
            atMeGroupList.remove(groupId)
        }
    }

    /**
     * check if the input groupId in atMeGroupList
     * @param groupId
     * @return
     */
    fun hasAtMeMsg(groupId: String): Boolean {
        return atMeGroupList.contains(groupId)
    }

    fun isAtMeMsg(message: ChatMessage?): Boolean {
        message?.run {
            try {
                getJSONArrayAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG)?.let {
                    for (i in 0 until it.length()) {
                        val username = it.getString(i)
                        if (username == ChatClient.getInstance().currentUser) {
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                //perhaps is a @ all message
                getStringAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, null)?.let {
                    if (it.uppercase(Locale.getDefault()) == EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL) {
                        return true
                    }
                }
                return false
            }
        }
        return false
    }

    fun atListToJsonArray(atList: List<String?>?): JSONArray {
        val jArray = JSONArray()
        atList?.let { 
            for (i in it.indices) {
                it[i]?.let { username -> 
                    jArray.put(username)
                }
            }
        }
        return jArray
    }

    fun cleanToAtUserList() {
        synchronized(toAtUserList) { toAtUserList.clear() }
    }

    companion object {
        private var instance: EaseAtMessageHelper? = null
        @Synchronized
        fun get(): EaseAtMessageHelper {
            if (instance == null) {
                synchronized(EaseAtMessageHelper::class.java) {
                    if (instance == null) {
                        instance = EaseAtMessageHelper()
                    }
                }
            }
            return instance!!
        }
    }
}
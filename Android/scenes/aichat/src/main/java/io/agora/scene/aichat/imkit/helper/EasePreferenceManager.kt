package io.agora.scene.aichat.imkit.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import io.agora.scene.aichat.imkit.EaseIM

class EasePreferenceManager @SuppressLint("CommitPrefEdits") private constructor() {
    companion object {
        private const val KEY_LOADED_CONVS_FROM_SERVER = "key_loaded_convs_from_server_"
        private const val KEY_MSG_AUDIO = "key_msg_audio_"

        private var instance: EasePreferenceManager? = null

        fun getInstance(): EasePreferenceManager {
            if (instance == null) {
                synchronized(EasePreferenceManager::class.java) {
                    if (instance == null) {
                        instance = EasePreferenceManager()
                    }
                }
            }
            return instance!!
        }
    }

    private val editor: SharedPreferences.Editor?
    private val mSharedPreferences: SharedPreferences? = EaseIM.getContext()
        ?.getSharedPreferences("imkit_preferences", Context.MODE_PRIVATE)

    init {
        editor = mSharedPreferences?.edit()
    }


    fun putString(key: String?, value: String?) {
        editor?.putString(key, value)?.apply()
    }

    fun getString(key: String?): String? {
        return mSharedPreferences?.getString(key, "")
    }

    fun putBoolean(key: String?, value: Boolean) {
        editor?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String?): Boolean {
        return mSharedPreferences?.getBoolean(key, false) ?: false
    }

    /**
     * Set whether the conversation list has been loaded from the server
     */
    internal fun setLoadedConversationsFromServer(value: Boolean) {
        val userId = EaseIM.getCurrentUser().id
        editor?.putBoolean(KEY_LOADED_CONVS_FROM_SERVER + userId, value)?.apply()
    }

    /**
     * Get whether the conversation list has been loaded from the server
     */
    internal fun isLoadedConversationsFromServer(): Boolean {
        val userId = EaseIM.getCurrentUser().id
        return mSharedPreferences?.getBoolean(KEY_LOADED_CONVS_FROM_SERVER + userId, false) ?: false
    }

    /**
     * Switch account clearing load contact status
     */
    internal fun removeLoadedContactDataStatus(key: String?) {
        key?.let {
            editor?.remove(it)?.apply()
        }
    }

    internal fun loadedChatMessageAudioList(conversationId: String): Map<String, String> {
        val userId = EaseIM.getCurrentUser().id

        // key： key_msg_audio_userId_conversationId； value：$messageId:$audioPath
        val storedSet = mSharedPreferences?.getStringSet(KEY_MSG_AUDIO + userId+"_" + conversationId, emptySet())?: emptySet()
        // 将数据转换回 Map 格式
        val messageAudioMap = storedSet.map {
            val (messageId, audioPath) = it.split(":", limit = 2) // 使用 ":" 进行拆分
            messageId to audioPath
        }.toMap()
        return messageAudioMap
    }

    internal fun storeChatMessageAudio(conversationId: String, messageId: String, audioPath: String) {
        val userId = EaseIM.getCurrentUser().id
        val newMessageAudioMap = mapOf(
            messageId to audioPath,
        )
        val existingMap = loadedChatMessageAudioList(conversationId)
        val mergedMap = existingMap + newMessageAudioMap
        // 将合并后的数据转换回 Set<String> 格式
        val mergedSet = mergedMap.map { (messageId, audioPath) ->
            "$messageId:$audioPath"
        }.toSet()
        editor?.putStringSet(KEY_MSG_AUDIO + userId+"_" + conversationId, mergedSet)?.apply()
    }
}
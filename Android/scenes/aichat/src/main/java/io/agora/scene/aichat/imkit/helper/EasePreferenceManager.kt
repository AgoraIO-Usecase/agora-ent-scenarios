package io.agora.scene.aichat.imkit.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import io.agora.scene.aichat.imkit.EaseIM

class EasePreferenceManager @SuppressLint("CommitPrefEdits") private constructor() {
    private val editor: SharedPreferences.Editor?
    private val mSharedPreferences: SharedPreferences? = EaseIM.getContext()
        ?.getSharedPreferences("imkit_preferences", Context.MODE_PRIVATE)

    init {
        editor = mSharedPreferences?.edit()
    }


    fun putString(key: String?, value: String?) {
        editor?.putString(key, value)
        editor?.commit()
    }

    fun getString(key: String?): String? {
        return mSharedPreferences?.getString(key, "")
    }

    fun putBoolean(key: String?, value: Boolean) {
        editor?.putBoolean(key, value)
        editor?.commit()
    }

    fun getBoolean(key: String?): Boolean {
        return mSharedPreferences?.getBoolean(key, false) ?: false
    }

    /**
     * Set whether the conversation list has been loaded from the server
     */
    internal fun setLoadedConversationsFromServer(value: Boolean) {
        EaseIM.getCurrentUser().let {
            editor?.putBoolean(KEY_LOADED_CONVS_FROM_SERVER+it.id, value)
            editor?.commit()
        }
    }

    /**
     * Get whether the conversation list has been loaded from the server
     */
    internal fun isLoadedConversationsFromServer(): Boolean {
        EaseIM.getCurrentUser().let {
            return mSharedPreferences?.getBoolean(KEY_LOADED_CONVS_FROM_SERVER+it.id, false) ?: false
        }
        return false
    }

    /**
     * Switch account clearing load contact status
     */
    internal fun removeLoadedContactDataStatus(key: String?){
        key?.let {
            editor?.remove(it)
            editor?.apply()
        }
    }
    companion object {
        private const val KEY_LOADED_CONVS_FROM_SERVER = "key_loaded_convs_from_server_"

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
}
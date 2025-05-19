package io.agora.scene.voice.imkit.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Nullable
import io.agora.scene.base.utils.GsonTools
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.VoiceRankUserModel
import java.io.*

class ChatroomCacheManager {

    private val mSharedPreferences: SharedPreferences by lazy {
        ChatroomConfigManager.getInstance().context.getSharedPreferences(
            "SP_AT_PROFILE",
            Context.MODE_PRIVATE
        )
    }
    private val mEditor: SharedPreferences.Editor
        get() = mSharedPreferences.edit()

    private val mMicInfoMap = mutableMapOf<String, String>()
    private val allInfoMap = mutableMapOf<String, String>()

    private val submitMicList = mutableListOf<VoiceMemberModel>()
    private val submitMicMap = mutableMapOf<String, VoiceMemberModel>()

    private val roomMemberList = mutableListOf<VoiceMemberModel>()
    private val roomMemberMap = mutableMapOf<String, VoiceMemberModel>()

    private val invitationList = mutableListOf<VoiceMemberModel>()
    private val invitationMap = mutableMapOf<String, VoiceMemberModel>()

    private var rankingList = mutableListOf<VoiceRankUserModel>()
    private val rankingMap = mutableMapOf<String, VoiceRankUserModel>()

    private var giftAmount: Int = 0

    companion object {
        const val TAG = "ChatroomCacheManager"
        val cacheManager = ChatroomCacheManager()
    }

    /**
     * Set gift amount from server data directly
     */
    fun setGiftAmountCache(amount: Int) {
        giftAmount = amount
    }

    /**
     * Update total gift amount in room
     */
    fun updateGiftAmountCache(amount: Int) {
        giftAmount += amount
        VoiceLogger.d(TAG, "updateGiftAmountCache(${giftAmount}) ")
    }

    /**
     * Get total gift amount in room
     */
    fun getGiftAmountCache(): Int {
        VoiceLogger.d(TAG, "getGiftAmountCache(${giftAmount}) ")
        return giftAmount
    }

    var clickCountCache: Int = 0
        set(value) {
            field = value
            VoiceLogger.d(TAG, "updateClickCountCache($field) ")
        }
        get() {
            VoiceLogger.d(TAG, "getClickCache($field) ")
            return field
        }

    /**
     * Cache all key-value properties
     */
    fun setKvInfo(kvMap: Map<String, String>) {
        for (entry in kvMap.entries) {
            allInfoMap[entry.key] = entry.value
        }
    }

    /**
     * Get property from cache by key
     */
    fun getKvInfo(key: String?): String? {
        return allInfoMap[key]
    }

    /**
     * Clear all caches
     */
    fun clearAllCache() {
        allInfoMap.clear()
        clearMemberList()
        clearMicInfo()
        clearSubmitList()
        clearRankList()
        giftAmount = 0
    }

    /**
     * Set mic information
     */
    fun setMicInfo(kvMap: Map<String, String>) {
        if (mMicInfoMap.isEmpty()) {
            mMicInfoMap.putAll(kvMap)
        } else {
            for (mutableEntry in kvMap) {
                if (mutableEntry.key.contains("mic_")) {
                    mMicInfoMap[mutableEntry.key] = mutableEntry.value
                }
            }
        }
    }

    /**
     * Clear local mic information
     */
    fun clearMicInfo() {
        mMicInfoMap.clear()
    }

    /**
     * Get mic information
     */
    fun getMicInfoMap(): MutableMap<String, String>? {
        return mMicInfoMap
    }

    /**
     * Get mic information for specified mic position
     */
    fun getMicInfoByIndex(micIndex: Int): VoiceMicInfoModel? {
        val indexTag = "mic_$micIndex"
        if (mMicInfoMap.isNotEmpty() && mMicInfoMap.containsKey(indexTag)) {
            return GsonTools.toBean(mMicInfoMap[indexTag], VoiceMicInfoModel::class.java)
        }
        return null
    }

    /**
     * Get VoiceMicInfoModel by chatUid
     */
    fun getMicInfoByChatUid(chatUid: String): VoiceMicInfoModel? {
        for (entry in mMicInfoMap) {
            val micInfoBean = GsonTools.toBean(entry.value, VoiceMicInfoModel::class.java)
            if (micInfoBean?.member?.chatUid.equals(chatUid)) {
                return micInfoBean
            }
        }
        return null
    }

    /**
     * Set mic application list
     */
    fun setSubmitMicList(voiceMemberBean: VoiceMemberModel) {
        val chatUid = voiceMemberBean.chatUid
        if (chatUid != null) {
            submitMicMap[chatUid] = voiceMemberBean
            submitMicList.clear()
            for (entry in submitMicMap.entries) {
                submitMicList.add(entry.value)
            }
        }
    }

    /**
     * Get list of members who applied for mic
     */
    fun getSubmitMicList(): MutableList<VoiceMemberModel> {
        return submitMicList
    }

    /**
     * Get specific member model from mic application list
     */
    fun getSubmitMic(chatUid: String): VoiceMemberModel? {
        return if (submitMicMap.containsKey(chatUid)) {
            submitMicMap[chatUid]
        } else {
            null
        }
    }

    /**
     * Remove specific member from application list
     */
    fun removeSubmitMember(chatUid: String) {
        submitMicMap.remove(chatUid)
        submitMicList.clear()
        for (entry in submitMicMap.entries) {
            submitMicList.add(entry.value)
        }
    }

    /**
     * Clear local application list
     */
    private fun clearSubmitList() {
        submitMicMap.clear()
        submitMicList.clear()
    }

    /**
     * Set member list
     */
    fun setMemberList(member: VoiceMemberModel) {
        val chatUid = member.chatUid
        if (chatUid != null) {
            roomMemberMap[chatUid] = member
            roomMemberList.clear()
            for (entry in roomMemberMap.entries) {
                roomMemberList.add(entry.value)
            }
        }
    }

    /**
     * Get corresponding entity by chatUid
     */
    fun getMember(chatUid: String): VoiceMemberModel? {
        VoiceLogger.d(TAG, "roomMemberMap(${roomMemberMap}) getMember: $chatUid ")
        return roomMemberMap[chatUid]
    }

    /**
     * Get member list
     */
    fun getMemberList(): MutableList<VoiceMemberModel> {
        return roomMemberList
    }

    /**
     * Get invitation list (filtering members already on mic)
     */
    fun getInvitationList(): MutableList<VoiceMemberModel> {
        invitationMap.clear()
        invitationList.clear()
        invitationMap.putAll(roomMemberMap)
        for (entry in getMicInfoMap()?.entries!!) {
            val micInfo = GsonTools.toBean(entry.value, VoiceMicInfoModel::class.java)
            micInfo?.member?.chatUid.let {
                VoiceLogger.d(TAG, "invitationMap remove(${it})")
                invitationMap.remove(it)
            }
        }
        for (entry in invitationMap.entries) {
            invitationList.add(entry.value)
        }
        VoiceLogger.d(TAG, "invitationList(${invitationList})")
        return invitationList
    }

    /**
     * Check if member in invitation list is already on mic
     */
    fun checkInvitationByChatUid(chatUid: String): Boolean {
        for (entry in getMicInfoMap()?.entries!!) {
            val micInfo = GsonTools.toBean(entry.value, VoiceMicInfoModel::class.java)
            micInfo?.member?.chatUid.let {
                if (it.equals(chatUid)) return true
            }
        }
        return false
    }

    /**
     * Remove specific member from member list (called in member exit callback)
     */
    fun removeMember(chatUid: String) {
        roomMemberMap.remove(chatUid)
        roomMemberList.clear()
        for (entry in roomMemberMap.entries) {
            roomMemberList.add(entry.value)
        }
    }

    /**
     * Clear member list
     */
    private fun clearMemberList() {
        roomMemberList.clear()
        roomMemberMap.clear()
    }

    /**
     * Set ranking list
     */
    fun setRankList(rankBean: VoiceRankUserModel) {
        val chatUid = rankBean.chatUid
        if (chatUid != null) {
            rankingMap[chatUid] = rankBean
            rankingList.clear()
            for (entry in rankingMap.entries) {
                rankingList.add(entry.value)
            }
        }
    }

    /**
     * Get ranking list
     */
    fun getRankList(): MutableList<VoiceRankUserModel> {
        val comparator: Comparator<VoiceRankUserModel> = Comparator { o1, o2 ->
            o2.amount.compareTo(o1.amount)
        }
        rankingList.sortWith(comparator)
        VoiceLogger.d(TAG, "getRankList (${rankingList})")
        return rankingList
    }

    fun getRankMap(): MutableMap<String, VoiceRankUserModel> {
        return rankingMap
    }

    /**
     * Clear ranking list
     */
    private fun clearRankList() {
        rankingList.clear()
        rankingMap.clear()
    }

    @SuppressLint("ApplySharedPref")
    fun putString(key: String?, value: String?) {
        mEditor?.putString(key, value)
        mEditor?.commit()
    }

    fun getString(key: String?): String? {
        return getString(key, "")
    }

    fun getString(key: String?, defValue: String?): String? {
        return mSharedPreferences.getString(key, defValue)
    }

    @SuppressLint("ApplySharedPref")
    fun putBoolean(key: String?, value: Boolean) {
        mEditor.putBoolean(key, value)
        mEditor.commit()
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(key, defValue) ?: defValue
    }

    @SuppressLint("ApplySharedPref")
    fun putInt(key: String?, value: Int) {
        mEditor.putInt(key, value)
        mEditor.commit()
    }

    fun putList(key: String?, list: List<Serializable?>?) {
        putString(key, obj2Base64(list))
    }

    @Nullable
    fun <E : Serializable?> getList(key: String?): List<E>? {
        return base64ToObj(getString(key)!!) as List<E>?
    }

    fun getInt(key: String?, defValue: Int): Int {
        return mSharedPreferences.getInt(key, defValue) ?: defValue
    }

    private fun <K : Serializable?, V> putMap(key: String?, map: MutableMap<K, V>?) {
        putString(key, obj2Base64(map))
    }

    @Nullable
    fun <K : Serializable?, V> getMap(key: String?): MutableMap<K, V>? {
        return base64ToObj(getString(key)!!) as MutableMap<K, V>?
    }

    private fun obj2Base64(obj: Any?): String? {
        if (obj == null) {
            return null
        }
        var baos: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        var objectStr: String? = null
        try {
            baos = ByteArrayOutputStream()
            oos = ObjectOutputStream(baos)
            oos.writeObject(obj)
            objectStr = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (baos != null) {
                try {
                    baos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (oos != null) {
                try {
                    oos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return objectStr
    }

    private fun <T> base64ToObj(base64: String): T? {
        if (TextUtils.isEmpty(base64)) {
            return null
        }
        val objBytes = Base64.decode(base64.toByteArray(), Base64.DEFAULT)
        var bais: ByteArrayInputStream? = null
        var ois: ObjectInputStream? = null
        var t: T? = null
        try {
            bais = ByteArrayInputStream(objBytes)
            ois = ObjectInputStream(bais)
            t = ois.readObject() as T
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (bais != null) {
                try {
                    bais.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (ois != null) {
                try {
                    ois.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return t
    }

}
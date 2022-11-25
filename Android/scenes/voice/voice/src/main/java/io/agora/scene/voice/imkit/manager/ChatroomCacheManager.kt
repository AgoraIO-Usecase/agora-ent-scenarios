package io.agora.scene.voice.imkit.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Nullable
import io.agora.scene.voice.service.VoiceMemberModel
import io.agora.scene.voice.service.VoiceMicInfoModel
import io.agora.voice.buddy.tool.GsonTools
import java.io.*

class ChatroomCacheManager {
    private var mEditor: SharedPreferences.Editor? = null
    private var mSharedPreferences: SharedPreferences? = null
    private val mMicInfoMap = mutableMapOf<String, String>()

    private val submitMicList = mutableListOf<VoiceMemberModel>()
    private val submitMicMap = mutableMapOf<String, VoiceMemberModel>()

    private val roomMemberList = mutableListOf<VoiceMemberModel>()
    private val roomMemberMap = mutableMapOf<String, VoiceMemberModel>()

    private val giftContributeList = mutableListOf<VoiceMemberModel>()
    private val giftContributeMap = mutableMapOf<String, VoiceMemberModel>()

    companion object {
        val cacheManager = ChatroomCacheManager().apply {
            mSharedPreferences = ChatroomConfigManager.getInstance().context.getSharedPreferences(
                "SP_AT_PROFILE",
                Context.MODE_PRIVATE
            )
            mSharedPreferences.let {
                mEditor = it?.edit()
            }
        }
    }

    /**
     * 设置Mic信息
     */
    fun setMicInfo(kvMap: MutableMap<String,String>){
        if (mMicInfoMap.isEmpty()){
            mMicInfoMap.putAll(kvMap)
        }else{
            for (mutableEntry in kvMap) {
                if ( mutableEntry.key.contains("mic_")){
                    mMicInfoMap[mutableEntry.key] = mutableEntry.value
                }
            }
        }
    }

    /**
     * 清除本地MicInfo信息
     */
    fun clearMicInfo(){
        mMicInfoMap.clear()
    }

    /**
     * 获取Mic信息
     */
    fun getMicInfoMap(): MutableMap<String, String>? {
        return mMicInfoMap
    }

    /**
     * 获取指定麦位的Mic信息
     */
    fun getMicInfoByIndex(micIndex: Int): VoiceMicInfoModel?{
        val indexTag = "mic_$micIndex"
        if (mMicInfoMap.isNotEmpty() && mMicInfoMap.containsKey(indexTag)){
            return GsonTools.toBean(mMicInfoMap[indexTag], VoiceMicInfoModel::class.java)
        }
        return null
    }

    /**
     * 设置申请上麦列表
     */
    fun setSubmitMicList(voiceMemberBean: VoiceMemberModel){
        val chatUid = voiceMemberBean.chatUid
        if (chatUid != null){
            submitMicMap[chatUid] = voiceMemberBean
            submitMicList.clear()
            for (entry in submitMicMap.entries) {
                submitMicList.add(entry.value)
            }
        }
    }

    /**
     * 获取申请上麦成员列表
     */
    fun getSubmitMicList():MutableList<VoiceMemberModel>{
        return submitMicList
    }

    /**
     * 从申请列表移除指定成员对象
     */
    fun removeSubmitMember(chatUid: String){
        submitMicMap.remove(chatUid)
        submitMicList.clear()
        for (entry in submitMicMap.entries) {
            submitMicList.add(entry.value)
        }
    }

    /**
     * 清除本地申请列表
     */
    fun clearSubmitList(){
        submitMicMap.clear()
        submitMicList.clear()
    }

    /**
     * 设置成员列表
     */
    fun setMemberList(member:VoiceMemberModel){
        val chatUid = member.chatUid
        if (chatUid != null){
            roomMemberMap[chatUid] = member
            roomMemberList.clear()
            for (entry in roomMemberMap.entries) {
                roomMemberList.add(entry.value)
            }
        }
    }

    /**
     * 获取成员列表
     */
    fun getMemberList():MutableList<VoiceMemberModel>{
        return roomMemberList
    }

    /**
     * 从成员列表中移除指定成员
     */
    fun removeMember(chatUid: String){
        roomMemberMap.remove(chatUid)
        roomMemberList.clear()
        for (entry in roomMemberMap.entries) {
            roomMemberList.add(entry.value)
        }
    }

    /**
     * 清除成员列表
     */
    fun clearMemberList(){
        roomMemberList.clear()
        roomMemberMap.clear()
    }

    /**
     * 存入字符串
     * @param key     字符串的键
     * @param value   字符串的值
     */
    @SuppressLint("ApplySharedPref")
    fun putString(key: String?, value: String?) {
        //存入数据
        mEditor?.putString(key, value)
        mEditor?.commit()
    }

    /**
     * 获取字符串
     * @param key     字符串的键
     * @return 得到的字符串
     */
    fun getString(key: String?): String? {
        return getString(key, "")
    }

    /**
     * 获取字符串
     * @param key     字符串的键
     * @param defValue   字符串的默认值
     * @return 得到的字符串
     */
    fun getString(key: String?, defValue: String?): String? {
        return mSharedPreferences?.getString(key, defValue)
    }

    /**
     * 保存布尔值
     * @param key     键
     * @param value   值
     */
    @SuppressLint("ApplySharedPref")
    fun putBoolean(key: String?, value: Boolean) {
        mEditor?.putBoolean(key, value)
        mEditor?.commit()
    }

    /**
     * 获取布尔值
     * @param key      键
     * @param defValue 默认值
     * @return 返回保存的值
     */
    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mSharedPreferences?.getBoolean(key, defValue) ?: defValue
    }

    /**
     * 保存int值
     * @param key     键
     * @param value   值
     */
    @SuppressLint("ApplySharedPref")
    fun putInt(key: String?, value: Int) {
        mEditor?.putInt(key, value)
        mEditor?.commit()
    }

    /**
     * 存储List集合
     * @param key 存储的键
     * @param list 存储的集合
     */
    fun putList(key: String?, list: List<Serializable?>?) {
        putString(key, obj2Base64(list))
    }

    /**
     * 获取List集合
     * @param key 键
     * @param <E> 指定泛型
     * @return List集合
    </E> */
    @Nullable
    fun <E : Serializable?> getList(key: String?): List<E>? {
        return base64ToObj(getString(key)!!) as List<E>?
    }

    /**
     * 获取int值
     * @param key      键
     * @param defValue 默认值
     * @return 保存的值
     */
    fun getInt(key: String?, defValue: Int): Int {
        return mSharedPreferences?.getInt(key, defValue) ?: defValue
    }

    /**
     * 存储Map集合
     * @param key 键
     * @param map 存储的集合
     * @param <K> 指定Map的键
     * @param <V> 指定Map的值
    </V></K> */
    private fun <K : Serializable?, V> putMap(key: String?, map: MutableMap<K, V>?) {
        putString(key, obj2Base64(map))
    }

    /**
     * 获取map集合
     * @param key 键
     * @param <K> 指定Map的键
     * @param <V> 指定Map的值
     * @return 存储的集合
    </V></K> */
    @Nullable
    fun <K : Serializable?, V> getMap(key: String?): MutableMap<K, V>? {
        return base64ToObj(getString(key)!!) as MutableMap<K, V>?
    }

    /**
     * 对象转字符串
     * @param obj 任意对象
     * @return base64字符串
     */
    private fun obj2Base64(obj: Any?): String? {
        //判断对象是否为空
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
            // 将对象放到OutputStream中
            // 将对象转换成byte数组，并将其进行base64编码
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

    /**
     * base64转对象
     * @param base64 字符串
     * @param <T> 指定转成的类型
     * @return 指定类型对象 失败返回null
    </T> */
    private fun <T> base64ToObj(base64: String): T? {
        // 将base64格式字符串还原成byte数组
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
package io.agora.scene.voice.imkit.manager

import io.agora.CallBack
import io.agora.ValueCallBack
import io.agora.chat.ChatClient
import io.agora.chat.ChatRoomManager
import io.agora.voice.buddy.tool.GsonTools
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.imkit.bean.ChatMicMemberBean
import io.agora.scene.voice.imkit.bean.ChatroomMicBean
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper
import io.agora.scene.voice.imkit.custorm.CustomMsgType
import io.agora.scene.voice.imkit.custorm.OnMsgCallBack

class ChatroomProtocolDelegate constructor(
    private val roomId: String
) {
    companion object {
        private const val TAG = "ChatroomProtocolDelegate"
    }
    var roomManager  : ChatRoomManager = ChatClient.getInstance().chatroomManager()
    lateinit var ownerBean : ChatMicMemberBean

    /////////////////////// mic ///////////////////////////

    /**
     * 初始化麦位信息 （这里缺少 房间类型参数  3D空间音频初始化麦位信息和普通房间有区别）
     */
    fun initMicInfo(ownerBean:ChatMicMemberBean,callBack: CallBack){
        val attributeMap = mutableMapOf<String, String>()
        this@ChatroomProtocolDelegate.ownerBean = ownerBean
        for (i in 1..7) {
            var key = "mic_"
            var status = -1
            key += i
            if (i >= 6) status = -2
            var mBean = GsonTools.beanToString(ChatroomMicBean(status,i,null))
            if (mBean != null){
                attributeMap[key] = mBean
            }
        }
        var oBean = GsonTools.beanToString(ChatroomMicBean(0,0,ownerBean))
        if (oBean != null){
            attributeMap["mic_0"] = oBean
            roomManager.asyncSetChatroomAttributesForced(roomId,attributeMap,true
            ) { code, result_map -> resultCallback(code,result_map.toString(),callBack) }
        }
    }

    /**
     * 从服务端获取所有麦位信息
     */
    fun getMicInfoFromServer() : MutableMap<String, ChatroomMicBean>{
        var micInfoMap = mutableMapOf<String, ChatroomMicBean>()
        roomManager.asyncFetchChatRoomAllAttributesFromServer(roomId,object :
            ValueCallBack<MutableMap<String, String>>{
            override fun onSuccess(value: MutableMap<String, String>?) {
                for (entry in value?.entries!!) {
                    var bean = GsonTools.toBean(entry.value, ChatroomMicBean::class.java)
                    if (bean != null){
                        micInfoMap[entry.key] = bean
                    }
                }
            }

            override fun onError(error: Int, desc: String?) {
                "onError: $error $desc".logE("asyncFetchChatRoomAllAttributesFromServer")
            }

        })
        return micInfoMap
    }

    /**
     * 从本地缓存获取所有麦位信息
     */
    fun getMicInfo(): MutableMap<String, ChatroomMicBean>{
        val micInfoMap = mutableMapOf<String, ChatroomMicBean>()
        var localMap =  ChatroomCacheManager.cacheManager.getMicInfoMap()
        if (localMap != null){
            for (entry in localMap.entries) {
                var bean = GsonTools.toBean(entry.value, ChatroomMicBean::class.java)
                if (bean != null){
                    micInfoMap[entry.key] = bean
                }
            }
        }
        return micInfoMap
    }

    /**
     * 从本地获取指定麦位信息
     */
    fun getMicInfo(micIndex:Int): ChatroomMicBean? {
        return ChatroomCacheManager.cacheManager.getMicInfoByIndex(micIndex)
    }

     /**
     * 从服务端获取指定麦位信息
     */
    fun getMicInfoByIndexFromServer(micIndex: Int) : ChatroomMicBean{
        val keyList: MutableList<String> = java.util.ArrayList()
        var micBean = ChatroomMicBean(-99,-99,null)
        keyList.add(getMicIndex(micIndex))
        roomManager.asyncFetchChatroomAttributesFromServer(roomId,keyList,object :
            ValueCallBack<MutableMap<String, String>>{
            override fun onSuccess(value: MutableMap<String, String>?) {
                for (entry in value?.entries!!) {
                    micBean = GsonTools.toBean(entry.value, ChatroomMicBean::class.java)!!
                    "getMicInfoByIndex onSuccess: ".logE(TAG)
                }
            }

            override fun onError(error: Int, desc: String?) {
                "getMicInfoByIndex onError: $error $desc".logE(TAG)
            }
        })
        return micBean
    }

    /**
     * 下麦
     */
    fun leaveMicMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,-1,false,callback)
    }

    /**
     * 交换麦位
     */
    fun changeMic(fromMicIndex: Int,toMicIndex: Int,callback: CallBack){
        val attributeMap = ChatroomCacheManager.cacheManager.getMicInfoMap()
        var fromKey = getMicIndex(fromMicIndex)
        var toKey = getMicIndex(toMicIndex)
        var fromBean = getMicInfo(fromMicIndex)
        var toMicBean =  getMicInfo(toMicIndex)
        var fromBeanValue = GsonTools.beanToString(fromBean)
        var toBeanValue = GsonTools.beanToString(toMicBean)
        if (toMicBean != null && toMicBean.status == -1){
            if (toBeanValue != null){
                attributeMap?.put(fromKey,toBeanValue )
            }
            if (fromBeanValue != null){
                attributeMap?.put(toKey, fromBeanValue)
            }
            roomManager.asyncSetChatroomAttributes(roomId,attributeMap,true
            ) { code, result_map -> resultCallback(code,result_map.toString(),callback) }
        }
    }

    /**
     * 关麦
     */
    fun closeMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,0,false,callback)
    }

    /**
     * 取消关麦
     */
    fun cancelCloseMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,1,false,callback)
    }

    /**
     * 禁言指定麦位
     */
    fun muteMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,2,true,callback)
    }
    /**
     * 取消指定麦位禁言
     */
    fun cancelMuteMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,-1,true,callback)
    }

    /**
     * 踢用户下麦
     */
    fun kickMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,-1,true,callback)
    }

    /**
     * 锁麦
     */
    fun lockMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,3,true,callback)
    }

    /**
     * 取消锁麦
     */
    fun cancelLockMic(micIndex: Int,callback: CallBack){
        updateMic(micIndex,-1,true,callback)
    }


    /////////////////////////// user ////////////////////////
    /**
     * 获取上麦申请列表（需要用到 ApplyListBean 等imkit 挪动到voice下 加上）
     */
    fun getApplyMicList(){

    }

    /**
     * 申请上麦
     */
    fun submitMic(micIndex:Int? = null, callback: CallBack){
        val attributeMap = mutableMapOf<String, String>()
        // TODO:  这里的user bean对象 等im模块移动到 voice下面 复用ProfileManager存的VRUserBean 或者考虑将存储当前用户信息整体挪到im kit
        attributeMap["user"] = ""
        if (micIndex != null){
            attributeMap["mic_index"] = micIndex.toString()
        }
        sendChatroomEvent(ownerBean.chat_uid, CustomMsgType.CHATROOM_APPLY_SITE,attributeMap,callback)
    }

    /**
     * 同意上麦申请
     */
    fun applySubmitMic(micIndex:Int? = null ,callback: CallBack){
        if (micIndex != null){
            updateMic(micIndex,0,false,callback)
        }else{
            updateMic(getFirstFreeMic(),0,false,callback)
        }
    }

    /**
     * 拒绝上麦申请
     */
    fun rejectSubmitMic(){
        // TODO: 本期暂无 拒绝上麦申请
    }

    /**
     * 撤销上麦申请
     */
    fun cancelSubmitMic(callback: CallBack){
        val attributeMap = mutableMapOf<String, String>()
        // TODO:  这里的user bean对象 等im模块移动到 voice下面 复用ProfileManager存的VRUserBean 或者考虑将存储当前用户信息整体挪到im kit
        attributeMap["user"] = ""
        sendChatroomEvent(ownerBean.chat_uid,CustomMsgType.CHATROOM_APPLY_SITE,attributeMap,callback)
    }

    /**
     * 邀请上麦列表
     */
    fun invitationMicList(){
        // TODO:  需要完成房间信息协议 拿到 memberList
    }

    /**
     * 邀请上麦
     */
    fun invitationMic(callback: CallBack){
        val attributeMap = mutableMapOf<String, String>()
        // TODO:  从邀请列表获取到 被邀请人的 user bean对象 ownerBean只是临时写的 后面替换
        attributeMap["user"] = ""
        sendChatroomEvent(ownerBean.chat_uid,CustomMsgType.CHATROOM_INVITE_SITE,attributeMap,callback)
    }

    /**
     * 用户拒绝上麦邀请
     */
    fun rejectMicInvitation(callback: CallBack){
        // TODO:  ios 没实现 需要确认是否需要实现
        val attributeMap = mutableMapOf<String, String>()
        // TODO:  从邀请列表获取到 被邀请人的 user bean对象 ownerBean只是临时写的 后面替换
        attributeMap["user"] = ""
        sendChatroomEvent(ownerBean.chat_uid,CustomMsgType.CHATROOM_INVITE_REFUSED_SITE,attributeMap,callback)
    }

    /**
     * 用户同意上麦邀请
     */
    fun agreeMicInvitation(micIndex:Int? = null,callback: CallBack){
        if (micIndex != null){
            updateMic(micIndex,0,false,callback)
        }else{
            updateMic( getFirstFreeMic() ,0,false,callback)
        }
    }

    /////////////////////////// room ///////////////////////////////


    /**
     * 更新指定麦位信息
     */
    private fun updateMic(micIndex: Int, status: Int,isForced:Boolean,callback: CallBack){
        val chatroomMicBean = getMicInfo(micIndex) ?: return
        chatroomMicBean.status = status
        chatroomMicBean.mic_index = micIndex
        var value = GsonTools.beanToString(chatroomMicBean)
        if (value != null && isForced){
            roomManager.asyncSetChatroomAttribute(roomId,getMicIndex(micIndex),
                value, true,object : CallBack{
                    override fun onSuccess() {
                        callback.onSuccess()
                        "updateMic onSuccess: ".logE(TAG)
                    }

                    override fun onError(code: Int, desc: String?) {
                        callback.onError(code,desc)
                        "updateMic onError: $code $desc".logE(TAG)
                    }
                })
        }else{
            roomManager.asyncSetChatroomAttributeForced(roomId,getMicIndex(micIndex),
                value, true,object : CallBack{
                    override fun onSuccess() {
                        callback.onSuccess()
                        "Forced updateMic onSuccess: ".logE(TAG)
                    }

                    override fun onError(code: Int, desc: String?) {
                        callback.onError(code,desc)
                        "Forced updateMic onError: $code $desc".logE(TAG)
                    }
                })
        }
    }

    private fun sendChatroomEvent(chatUid:String,eventType:CustomMsgType,
                          params:MutableMap<String,String>,callback: CallBack){
        CustomMsgHelper.getInstance().sendCustomSingleMsg(chatUid,
            eventType.getName(),params,object : OnMsgCallBack() {
                override fun onSuccess(message: ChatMessageData?) {
                    callback.onSuccess()
                    "sendCustomSingleMsg onSuccess:".logE(TAG)
                }

                override fun onError(messageId: String?, code: Int, desc: String?) {
                    callback.onError(code,desc)
                    "sendCustomSingleMsg onError: $code $desc".logE(TAG)
                }
            })
    }

    private fun resultCallback(code:Int,desc:String,callback:CallBack){
        if (code == 200){
            callback.onSuccess()
            "update result onSuccess: ".logE(TAG)
        }else{
            callback.onError(code,desc)
            "update result onError: $code $desc ".logE(TAG)
        }
    }

    /**
     *  按麦位顺序查询空麦位
     */
    private fun getFirstFreeMic():Int{
        var indexList: MutableList<Int> =  mutableListOf<Int>()
        var micInfo = ChatroomCacheManager.cacheManager.getMicInfoMap() as MutableMap<String, String>
        for (mutableEntry in micInfo) {
            var bean =  GsonTools.toBean(mutableEntry.value,ChatroomMicBean::class.java)
            if (bean != null && bean.status == -1){
                indexList.add(bean.mic_index)
            }
        }
        indexList.sortBy { it }
        return indexList[indexList.lastIndex]
    }

    fun getMicIndex(index : Int): String {
        var micIndex = ""
        when(index){
            0 -> { micIndex = "mic_0" }
            1 -> { micIndex = "mic_1" }
            2 -> { micIndex = "mic_2" }
            3 -> { micIndex = "mic_3" }
            4 -> { micIndex = "mic_4" }
            5 -> { micIndex = "mic_5" }
            6 -> { micIndex = "mic_6" }
            7 -> { micIndex = "mic_7" }
        }
        return micIndex
    }
}
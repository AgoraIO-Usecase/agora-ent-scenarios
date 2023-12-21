package io.agora.scene.voice.netkit

import android.content.Context
import com.google.gson.reflect.TypeToken
import io.agora.scene.base.BuildConfig
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.voice.common.net.VRHttpClientManager
import io.agora.voice.common.net.callback.VRHttpCallback
import io.agora.voice.common.net.callback.VRValueCallBack
import io.agora.voice.common.utils.GsonTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.ThreadManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * @author create by zhangwei03
 */
class VoiceToolboxServerHttpManager {

    private val TAG = "VoiceToolboxServerHttpManager"

    private fun context(): Context {
        return VoiceBuddyFactory.get().getVoiceBuddy().application().applicationContext
    }

    companion object {
        @JvmStatic
        fun get(): VoiceToolboxServerHttpManager {
            val sSingle = InstanceHelper.sSingle
            return sSingle
        }
    }

    internal object InstanceHelper {
        val sSingle = VoiceToolboxServerHttpManager()
    }

    /**
     * 生成RTC/RTM/Chat等Token007
     * @param channelName 频道名
     * @param expire 过期时间, 单位秒
     * @param src 来源/请求方 android
     * @param types 类型 1: RTC Token，2: RTM Token,3: Chat Token
     * @param uid 用户ID
     */
    fun generateToken(
        channelName: String,
        uid: String,
        expire: Int = 3600,
        src: String = "android",
        types: Array<Int> = arrayOf(1, 3),
        callBack: VRValueCallBack<VRGenerateTokenResponse>
    ) {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        val requestBody = JSONObject()

        try {
            requestBody.putOpt("appCertificate", BuildConfig.AGORA_APP_CERTIFICATE)
            requestBody.putOpt("appId", BuildConfig.AGORA_APP_ID)
            requestBody.putOpt("channelName", channelName)
            requestBody.putOpt("expire", expire)
            requestBody.putOpt("src", src)
            val requestTypes = JSONArray()
            types.forEach {
                requestTypes.put(it)
            }
            requestBody.putOpt("types", requestTypes)
            requestBody.putOpt("uid", uid)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        VRHttpClientManager.Builder(context())
            .setUrl(VoiceToolboxRequestApi.get().generateToken())
            .setHeaders(headers)
            .setParams(requestBody.toString())
            .setRequestMethod(VRHttpClientManager.Method_POST)
            .asyncExecute(object : VRHttpCallback {
                override fun onSuccess(result: String) {
                    "voice generateToken success".logD(TAG)
                    val bean = GsonTools.toBean<VRGenerateTokenResponse>(
                        result,
                        object : TypeToken<VRGenerateTokenResponse>() {}.type
                    )
                    if (bean?.isSuccess() == true) {
                        callBack.onSuccess(bean.data)
                    } else {
                        callBack.onError(bean?.code ?: -1, bean?.msg)
                    }
                }

                override fun onError(code: Int, msg: String) {
                    "voice generateToken onError: $code msg: $msg".logE(TAG)
                    callBack.onError(code, msg)
                }
            })
    }

    /**
     * 创建环信聊天室
     * @param chatroomName 聊天室名称，最大长度为 128 字符。
     * @param chatroomNameDesc 聊天室描述，最大长度为 512 字符。
     * @param chatroomOwner 聊天室的管理员。
     * @param src 来源/请求方
     * @param traceId 请求ID
     * @param username 用户 ID，长度不可超过 64 个字节长度。不可设置为空。支持以下字符集：
     *  - 26 个小写英文字母a-z；
     *  - 26 个大写英文字母A-Z；
     *  - 10 个数字 0-9；
     *  - “_”, “-”,“.”。
     * @param password 用户的登录密码，长度不可超过 64 个字符。
     * @param nickname 推送消息时，在消息推送通知栏内显示的用户昵称，并非用户个人信息的昵称。长度不可超过 100 个字符。支持以下字符集：
     * - 26 个小写英文字母a-z；
     * - 26 个大写英文字母A-Z；
     * - 10 个数字 0-9；
     * - 中文；
     * - 特殊字符。
     * @param chatroomId 环信聊天室roomId
     */
    fun createImRoom(
        roomName: String,
        roomOwner: String,
        chatroomId: String,
        type:Int? = 0,
        callBack: VRValueCallBack<VRCreateRoomResponse>
    ) {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        val requestBody = JSONObject()
        try {
            val requestChat = JSONObject()
            if (chatroomId.isNotEmpty()) {
                requestChat.putOpt("id", chatroomId)
            } else {
                requestChat.putOpt("name", roomName)
                requestChat.putOpt("description", "Welcome!")
                requestChat.putOpt("owner", roomOwner)
            }

            val requestUser = JSONObject()
            requestUser.putOpt("nickname", VoiceBuddyFactory.get().getVoiceBuddy().nickName())
            requestUser.putOpt("username", VoiceBuddyFactory.get().getVoiceBuddy().chatUserName())
            requestUser.putOpt("password", "12345678")

            val requestIM = JSONObject()
            requestIM.putOpt("appKey", io.agora.scene.voice.BuildConfig.IM_APP_KEY)
            requestIM.putOpt("clientId", io.agora.scene.voice.BuildConfig.IM_APP_CLIENT_ID)
            requestIM.putOpt("clientSecret", io.agora.scene.voice.BuildConfig.IM_APP_CLIENT_SECRET)
            requestBody.putOpt("im", requestIM)

            requestBody.putOpt("appId", BuildConfig.AGORA_APP_ID)
            requestBody.putOpt("appCertificate", BuildConfig.AGORA_APP_CERTIFICATE)
            requestBody.putOpt("src", "Android")
            requestBody.putOpt("traceId", UUID.randomUUID().toString())
            requestBody.putOpt("type",type)

            when (type) {
                1 -> {
                    requestBody.putOpt("user", requestUser)
                }
                2 -> {
                    requestBody.putOpt("chat", requestChat)
                }
                else -> {
                    requestBody.putOpt("user", requestUser)
                    requestBody.putOpt("chat", requestChat)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        VRHttpClientManager.Builder(context())
            .setUrl(VoiceToolboxRequestApi.get().createImRoom())
            .setHeaders(headers)
            .setParams(requestBody.toString())
            .setRequestMethod(VRHttpClientManager.Method_POST)
            .asyncExecute(object : VRHttpCallback {
                override fun onSuccess(result: String) {
                    "voice createImRoom success: $result".logD(TAG)
                    val bean = GsonTools.toBean<VRCreateRoomResponse>(
                        result,
                        object : TypeToken<VRCreateRoomResponse>() {}.type
                    )
                    if (bean?.isSuccess() == true) {
                        callBack.onSuccess(bean.data)
                    } else {
                        callBack.onError(bean?.code ?: -1, bean?.msg)
                    }
                }

                override fun onError(code: Int, msg: String) {
                    "voice createImRoom onError: $code msg: $msg".logE(TAG)
                    callBack.onError(code, msg)
                }
            })
    }

    /**
     * toolbox service api 置换token, 获取im 配置
     * @param channelId rtc 频道号
     * @param chatroomId im roomId
     * @param chatroomName im 房间名
     * @param chatOwner im 房间房主
     */
    fun requestToolboxService(
        channelId: String,
        chatroomId: String,
        chatroomName: String,
        chatOwner: String,
        completion: (error: Int, chatroomId: String) -> Unit,
    ) {
        ThreadManager.getInstance().runOnIOThread {
            val latch = CountDownLatch(2)
            var roomId = chatroomId
            var code = VoiceServiceProtocol.ERR_FAILED
            generateToken(
                channelId,
                VoiceBuddyFactory.get().getVoiceBuddy().rtcUid().toString(),
                callBack = object :
                    VRValueCallBack<VRGenerateTokenResponse> {
                    override fun onSuccess(response: VRGenerateTokenResponse?) {
                        response?.let {
                            VoiceBuddyFactory.get().getVoiceBuddy().setupRtcToken(it.token)
                            code = VoiceServiceProtocol.ERR_OK
                        }
                        latch.countDown()
                    }

                    override fun onError(var1: Int, var2: String?) {
                        "SyncToolboxService generate token error code:$var1,msg:$var2".logE()
                        latch.countDown()
                        code = VoiceServiceProtocol.ERR_FAILED
                    }
                })
            createImRoom(
                roomName = chatroomName,
                roomOwner = chatOwner,
                chatroomId = chatroomId,
                type = 2,
                callBack = object :
                    VRValueCallBack<VRCreateRoomResponse> {
                    override fun onSuccess(response: VRCreateRoomResponse?) {
                        response?.let {
                            if (roomId.isEmpty()) roomId = response.chatId
                            code = VoiceServiceProtocol.ERR_OK
                        }
                        latch.countDown()
                    }

                    override fun onError(var1: Int, var2: String?) {
                        "SyncToolboxService create room error code:$var1,msg:$var2".logE()
                        if (roomId.isEmpty()) {
                            code = VoiceServiceProtocol.ERR_ROOM_NAME_INCORRECT
                        } else {
                            code = VoiceServiceProtocol.ERR_FAILED
                        }
                        latch.countDown()
                    }
                })

            try {
                latch.await()
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(code, roomId)
                }
            } catch (e: Exception) {
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, roomId)
                }
            }
        }
    }
}
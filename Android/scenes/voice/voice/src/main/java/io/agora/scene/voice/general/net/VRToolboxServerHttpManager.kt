package io.agora.scene.voice.general.net

import android.content.Context
import com.google.gson.reflect.TypeToken
import io.agora.scene.base.BuildConfig
import io.agora.voice.buddy.tool.GsonTools
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.network.http.VRHttpCallback
import io.agora.voice.network.http.VRHttpClientManager
import io.agora.voice.network.http.toolbox.VRCreateRoomResponse
import io.agora.voice.network.http.toolbox.VRGenerateTokenResponse
import io.agora.voice.network.http.toolbox.VoiceToolboxRequestApi
import io.agora.voice.network.tools.VRValueCallBack
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * @author create by zhangwei03
 */
class VRToolboxServerHttpManager {

    private val TAG = "VoiceToolboxServerHttpManager"
    private lateinit var context: Context

    companion object {
        @JvmStatic
        fun get(context: Context): VRToolboxServerHttpManager {
            val sSingle = InstanceHelper.sSingle
            sSingle.context = context
            return sSingle
        }
    }

    internal object InstanceHelper {
        val sSingle = VRToolboxServerHttpManager()
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
        VRHttpClientManager.Builder(context)
            .setUrl(VoiceToolboxRequestApi.get().generateToken())
            .setHeaders(headers)
            .setParams(requestBody.toString())
            .setRequestMethod(VRHttpClientManager.Method_POST)
            .asyncExecute(object : VRHttpCallback {
                override fun onSuccess(result: String) {
                    "voice generateToken success: $result".logE(TAG)
                    val bean = GsonTools.toBean<VRGenerateTokenResponse>(result,
                        object : TypeToken<VRGenerateTokenResponse>() {}.type)
                    callBack.onSuccess(bean)
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
     */
    fun createImRoom(
        chatroomName: String,
        chatroomNameDesc: String,
        chatroomOwner: String,
        src: String,
        traceId: String,
        username: String,
        password: String,
        nickname: String,
        callBack: VRValueCallBack<VRCreateRoomResponse>
    ) {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        val requestBody = JSONObject()

        try {
            requestBody.putOpt("appId", BuildConfig.AGORA_APP_ID)
            val requestChat = JSONObject()
            requestChat.putOpt("name", chatroomName)
            requestChat.putOpt("description", chatroomNameDesc)
            requestChat.putOpt("owner", chatroomOwner)
            requestBody.putOpt("chat", requestChat)
            requestBody.putOpt("src", src)
            requestBody.putOpt("traceId", traceId)
            val requestUser = JSONObject()
            requestUser.putOpt("nickname", nickname)
            requestUser.putOpt("username", username)
            requestUser.putOpt("password", password)
            requestBody.putOpt("user", requestUser)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        VRHttpClientManager.Builder(context)
            .setUrl(VoiceToolboxRequestApi.get().createImRoom())
            .setHeaders(headers)
            .setParams(requestBody.toString())
            .setRequestMethod(VRHttpClientManager.Method_POST)
            .asyncExecute(object : VRHttpCallback {
                override fun onSuccess(result: String) {
                    "voice generateToken success: $result".logE(TAG)
                    val bean = GsonTools.toBean<VRCreateRoomResponse>(
                        result,
                        object : TypeToken<VRCreateRoomResponse>() {}.type
                    )
                    callBack.onSuccess(bean)
                }

                override fun onError(code: Int, msg: String) {
                    "voice generateToken onError: $code msg: $msg".logE(TAG)
                    callBack.onError(code, msg)
                }
            })
    }
}
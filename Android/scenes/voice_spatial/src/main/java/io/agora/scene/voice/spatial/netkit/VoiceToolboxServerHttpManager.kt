package io.agora.scene.voice.spatial.netkit

import android.content.Context
import com.google.gson.reflect.TypeToken
import io.agora.scene.base.BuildConfig
import io.agora.scene.voice.spatial.global.VoiceBuddyFactory
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
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
                    "voice generateToken success: $result".logD(TAG)
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
package io.agora.voice.rtckit.internal

import android.os.Handler
import android.os.Looper
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.rtckit.annotation.RtcNetWorkQuality
import io.agora.voice.rtckit.constants.RtcKitConstant
import io.agora.voice.rtckit.open.status.RtcAudioChangeStatus
import io.agora.voice.rtckit.open.status.RtcAudioVolumeIndicationStatus
import io.agora.voice.rtckit.open.status.RtcAudioVolumeInfo
import io.agora.voice.rtckit.open.status.RtcErrorStatus

/**
 * @author create by zhangwei03
 *
 */
internal class AgoraRtcEventHandler(var rtcListener: IRtcClientListener?) : IRtcEngineEventHandler() {

    companion object {
        const val TAG = "${RtcKitConstant.RTC_TAG} AgoraRtcEventHandler"
    }

    private var handler = Handler(Looper.getMainLooper())

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(channel, uid, elapsed)
        "onJoinChannelSuccess channel:$channel,uid:$uid,elapsed:$elapsed".logD(TAG)
        rtcListener?.onJoinChannelSuccess(channel, uid, elapsed)
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        super.onLeaveChannel(stats)
        "onLeaveChannel stats:${stats?.totalDuration}".logE(RtcBaseClientEx.TAG)
        rtcListener?.onLeaveChannel()
    }

    override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
        super.onClientRoleChanged(oldRole, newRole)
        "onClientRoleChanged oldRole:${getClientRole(oldRole)},newRole:${getClientRole(newRole)}".logD(TAG)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)
        rtcListener?.onUserJoined(uid, true)
        "onUserJoined uid:$uid,elapsed:$elapsed".logD(TAG)
    }

    private fun getUserOfflineReason(reason: Int): String {
        return when (reason) {
            Constants.USER_OFFLINE_QUIT -> "When the user leaves the channel, the user sends a goodbye message. When this message is received, the SDK determines that the user leaves the channel."
            Constants.USER_OFFLINE_DROPPED -> "When no data packet of the user is received for a certain period of time, the SDK assumes that the user drops offline. A poor network connection may lead to false detection, so we recommend using the RTM SDK for reliable offline detection."
            Constants.USER_OFFLINE_BECOME_AUDIENCE -> "The user switches the user role from a broadcaster to an audience."
            else -> "Unknown"
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        super.onUserOffline(uid, reason)
        rtcListener?.onUserJoined(uid, false)
        "onUserOffline uid:$uid,reason:${getUserOfflineReason(reason)}".logD(TAG)
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        super.onNetworkQuality(uid, txQuality, rxQuality)
//        val status = RtcNetWorkStatus(
//            userId = uid.toString(),
//            txQuality = coverNetworkQuality(txQuality),
//            rxQuality = coverNetworkQuality(rxQuality)
//        )
//        if (0 == uid) {//自己
//            rtcListener?.onNetWorkStatus(status)
//            "onNetworkQuality uid:$uid,txQuality:${getNetWorkQualityValue(txQuality)}," +
//                    "rxQuality:${getNetWorkQualityValue(rxQuality)}".logD(TAG)
//        }
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
        super.onUserMuteAudio(uid, muted)
        rtcListener?.onAudioStatus(RtcAudioChangeStatus.RemoteAudio(uid.toString(), muted))
        "onUserMuteAudio uid:$uid,muted:$muted".logD(TAG)
    }

    /**
     * 音乐文件的播放状态已改变回调。
     * state
     * 音乐文件播放状态。
     * AUDIO_MIXING_STATE_PLAYING (710): 音乐文件正常播放。
     * AUDIO_MIXING_STATE_PAUSED (711): 音乐文件暂停播放。
     * AUDIO_MIXING_STATE_STOPPED (713): 音乐文件停止播放。
     * AUDIO_MIXING_STATE_FAILED (714): 音乐文件报错。SDK 会在 errorCode 参数中返回具体的报错原因。
     * reasonCode
     * 错误码。
     * AUDIO_MIXING_REASON_OK(0): 正常。
     * AUDIO_MIXING_REASON_CAN_NOT_OPEN (701): 音乐文件打开出错。
     * AUDIO_MIXING_REASON_TOO_FREQUENT_CALL (702): 音乐文件打开太频繁。
     * AUDIO_MIXING_REASON_INTERRUPTED_EOF (703): 音乐文件播放异常中断。
     * AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED(721): 音乐文件完成一次循环播放。
     * AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED(723): 音乐文件完成所有循环播放。
     * AUDIO_MIXING_REASON_STOPPED_BY_USER(724): 成功调用 pauseAudioMixing 暂停播放音乐文件。
     */
    override fun onAudioMixingStateChanged(state: Int, reasonCode: Int) {
        super.onAudioMixingStateChanged(state, reasonCode)
        if (state == Constants.AUDIO_MIXING_STATE_STOPPED && reasonCode == Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED) {
            rtcListener?.onAudioMixingFinished()
        }
        "onAudioMixingStateChanged stat:$state,reasonCode:$reasonCode".logD(TAG)
    }

    override fun onLocalAudioStateChanged(state: Int, error: Int) {
        super.onLocalAudioStateChanged(state, error)
        "onLocalAudioStateChanged state:$state,error:$error".logD(TAG)
    }

    override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        "onRemoteAudioStateChanged uid:$uid,state:$state,reason:$reason,elapsed:$elapsed".logD(TAG)
    }

    override fun onError(err: Int) {
        super.onError(err)
        rtcListener?.onError(RtcErrorStatus(err, "An error occurred during SDK runtime."))
        "onError err:$err,see:\n https://docs.agora.io/cn/voice-call-4.x/API%20Reference/java_ng/API/class_irtcengineeventhandler.html?platform=Android#callback_onerror".logE(
            TAG
        )
    }

    /**
     * state	当前的网络连接状态：
    CONNECTION_STATE_DISCONNECTED(1)：网络连接断开
    CONNECTION_STATE_CONNECTING(2)：建立网络连接中
    CONNECTION_STATE_CONNECTED(3)：网络已连接
    CONNECTION_STATE_RECONNECTING(4)：重新建立网络连接中
    CONNECTION_STATE_FAILED(5)：网络连接失败
    reason	引起当前网络连接状态发生改变的原因：
    CONNECTION_CHANGED_CONNECTING(0)：建立网络连接中
    CONNECTION_CHANGED_JOIN_SUCCESS(1)：成功加入频道
    CONNECTION_CHANGED_INTERRUPTED(2)：网络连接中断
    CONNECTION_CHANGED_BANNED_BY_SERVER(3)：网络连接被服务器禁止。可能服务端踢人场景时会报这个错。
    CONNECTION_CHANGED_JOIN_FAILED(4)：加入频道失败
    CONNECTION_CHANGED_LEAVE_CHANNEL(5)：离开频道
    CONNECTION_CHANGED_INVALID_APP_ID(6)：不是有效的 APP ID。请更换有效的 APP ID 重新加入频道
    CONNECTION_CHANGED_INVALID_CHANNEL_NAME(7)：不是有效的频道名。请更换有效的频道名重新加入频道
    CONNECTION_CHANGED_INVALID_TOKEN(8)：生成的 Token 无效。一般有以下原因：
    在控制台上启用了 App Certificate，但加入频道未使用 Token。当启用了 App Certificate，必须使用 Token
    在调用 joinChannel 加入频道时指定的 uid 与生成 Token 时传入的 uid 不一致
    CONNECTION_CHANGED_TOKEN_EXPIRED(9)：当前使用的 Token 过期，不再有效，需要重新在你的服务端申请生成 Token
    CONNECTION_CHANGED_REJECTED_BY_SERVER(10)：此用户被服务器禁止。一般有以下原因：
    用户已进入频道，再次调用加入频道的 API，例如 joinChannel。
    用户在调用 startEchoTest 进行通话测试时尝试加入频道。等待通话测试结束后再加入频道即可。
    CONNECTION_CHANGED_SETTING_PROXY_SERVER(11)：由于设置了代理服务器，SDK 尝试重连
    CONNECTION_CHANGED_RENEW_TOKEN(12)：更新 Token 引起网络连接状态改变
    CONNECTION_CHANGED_CLIENT_IP_ADDRESS_CHANGED(13)：客户端 IP 地址变更，可能是由于网络类型，或网络运营商的 IP 或端口发生改变引起
    CONNECTION_CHANGED_KEEP_ALIVE_TIMEOUT(14)：SDK 和服务器连接保活超时，进入自动重连状态
     */
    override fun onConnectionStateChanged(state: Int, reason: Int) {
        super.onConnectionStateChanged(state, reason)
        "onConnectionStateChanged state:$state,reason:$reason".logD(TAG)
    }

    /**
     * 用户音量提示回调。
     * 该回调默认禁用。可以通过 enableAudioVolumeIndication 方法开启。
     * 开启后，只要频道内有发流用户，SDK 会在加入频道后按 enableAudioVolumeIndication 中设置的时间间隔触发 onAudioVolumeIndication 回调。
     * 每次会触发两个 onAudioVolumeIndication 回调，一个报告本地发流用户的音量相关信息，另一个报告瞬时音量最高的远端用户（最多 3 位）的音量相关信息。
     */
    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        if (!speakers.isNullOrEmpty()) {
            val speakerInfoList = mutableListOf<RtcAudioVolumeInfo>()

            speakers.forEachIndexed { index, audioVolumeInfo ->
                speakerInfoList.add(
                    RtcAudioVolumeInfo(uid = audioVolumeInfo.uid, volume = audioVolumeInfo.volume)
                )
//                "onAudioVolumeIndication uid:${audioVolumeInfo.uid},volume:${audioVolumeInfo.volume}".logD(TAG)
            }
            rtcListener?.onAudioVolumeIndication(RtcAudioVolumeIndicationStatus(speakerInfoList))
        }
    }

    private fun getClientRole(role: Int): String {
        return if (role == Constants.CLIENT_ROLE_BROADCASTER) "BROADCASTER" else "AUDIENCE"
    }

    /**
     *  QUALITY_UNKNOWN(0)：质量未知
    QUALITY_EXCELLENT(1)：质量极好
    QUALITY_GOOD(2)：用户主观感觉和极好差不多，但码率可能略低于极好
    QUALITY_POOR(3)：用户主观感受有瑕疵但不影响沟通
    QUALITY_BAD(4)：勉强能沟通但不顺畅
    QUALITY_VBAD(5)：网络质量非常差，基本不能沟通
    QUALITY_DOWN(6)：网络连接断开，完全无法沟通
     */
    @RtcNetWorkQuality
    private fun coverNetworkQuality(quality: Int): Int {
        return when (quality) {
            Constants.QUALITY_EXCELLENT -> RtcNetWorkQuality.QualityExcellent
            Constants.QUALITY_GOOD -> RtcNetWorkQuality.QualityGood
            Constants.QUALITY_POOR -> RtcNetWorkQuality.QualityPoor
            Constants.QUALITY_BAD -> RtcNetWorkQuality.QualityBad
            Constants.QUALITY_VBAD -> RtcNetWorkQuality.QualityVBad
            Constants.QUALITY_DOWN -> RtcNetWorkQuality.QualityDown
            else -> RtcNetWorkQuality.QualityUnknown
        }
    }

    private fun getNetWorkQualityValue(@RtcNetWorkQuality netWorkQuality: Int): String {
        return when (netWorkQuality) {
            RtcNetWorkQuality.QualityExcellent -> "quality excellent"
            RtcNetWorkQuality.QualityGood -> "quality good"
            RtcNetWorkQuality.QualityPoor -> "quality poor"
            RtcNetWorkQuality.QualityBad -> "quality bad"
            RtcNetWorkQuality.QualityVBad -> "quality vbad"
            RtcNetWorkQuality.QualityDown -> "qualitydown"
            else -> "quality unknown"
        }
    }

    fun destroy() {
        rtcListener = null
        handler.removeCallbacksAndMessages(null)
    }
}
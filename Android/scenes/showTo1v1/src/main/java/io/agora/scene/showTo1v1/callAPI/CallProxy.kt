package io.agora.scene.showTo1v1.callAPI

import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler

class CallProxy: IRtcEngineEventHandler() {

    private val listeners = mutableListOf<IRtcEngineEventHandler>()

    fun addListener(listener: IRtcEngineEventHandler) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: IRtcEngineEventHandler) {
        listeners.remove(listener)
    }

    fun removeAllListeners() {
        listeners.clear()
    }

    // IRtcEngineEventHandler
    override fun onConnectionStateChanged(state: Int, reason: Int) {
        listeners.forEach { listener ->
            listener.onConnectionStateChanged(state, reason)
        }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        listeners.forEach { listener ->
            listener.onUserJoined(uid, elapsed)
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        listeners.forEach { listener ->
            listener.onUserOffline(uid, reason)
        }
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        listeners.forEach { listener ->
            listener.onLeaveChannel(stats)
        }
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        listeners.forEach { listener ->
            listener.onJoinChannelSuccess(channel, uid, elapsed)
        }
    }

    override fun onError(err: Int) {
        listeners.forEach { listener ->
            listener.onError(err)
        }
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        listeners.forEach { listener ->
            listener.onRemoteVideoStateChanged(uid, state, reason, elapsed)
        }
    }

    override fun onRtcStats(stats: RtcStats?) {
        listeners.forEach { listener ->
            listener.onRtcStats(stats)
        }
    }
    override fun onLocalVideoStats(source: Constants.VideoSourceType?, stats: LocalVideoStats?) {
        listeners.forEach { listener ->
            listener.onLocalVideoStats(source, stats)
        }
    }
    override fun onLocalAudioStats(stats: LocalAudioStats?) {
        listeners.forEach { listener ->
            listener.onLocalAudioStats(stats)
        }
    }
    override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
        listeners.forEach { listener ->
            listener.onRemoteVideoStats(stats)
        }
    }
    override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
        listeners.forEach { listener ->
            listener.onRemoteAudioStats(stats)
        }
    }
    override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo?) {
        listeners.forEach { listener ->
            listener.onUplinkNetworkInfoUpdated(info)
        }
    }
    override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo?) {
        listeners.forEach { listener ->
            listener.onDownlinkNetworkInfoUpdated(info)
        }
    }

    override fun onContentInspectResult(result: Int) {
        listeners.forEach { listener ->
            listener.onContentInspectResult(result)
        }
    }
}
package io.agora.scene.pure1v1.ui.living

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1DashboardFragmentBinding
import io.agora.scene.pure1v1.CallServiceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext

/*
 * 1v1 Interactive data panel
 */
class DashboardFragment : Fragment() {

    private lateinit var binding: Pure1v1DashboardFragmentBinding

    private var handler: IRtcEngineEventHandler? = null

    private var connection: RtcConnection? = null

    private var isBoardVisible = false

    private var rtcStats: IRtcEngineEventHandler.RtcStats? = null

    private var localVideoStats: IRtcEngineEventHandler.LocalVideoStats? = null

    private var localAudioStats: IRtcEngineEventHandler.LocalAudioStats? = null

    private var remoteVideoStats: IRtcEngineEventHandler.RemoteVideoStats? = null

    private var remoteAudioStats: IRtcEngineEventHandler.RemoteAudioStats? = null

    private var uplinkNetworkInfo: IRtcEngineEventHandler.UplinkNetworkInfo? = null

    private var downlinkNetworkInfo: IRtcEngineEventHandler.DownlinkNetworkInfo? = null

    override fun onDestroy() {
        handler?.let {
            CallServiceManager.instance.rtcEngine?.removeHandlerEx(it, connection)
            handler = null
        }
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = Pure1v1DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateVisible(b: Boolean) {
        isBoardVisible = b
        if (b) {
            refreshDashboard()
        }
    }

    fun setupRTCListener(channelId: String, localUid: Int) {
        val rtcListener = object: IRtcEngineEventHandler() {
            override fun onRtcStats(stats: RtcStats?) {
                stats?.let {
                    rtcStats = it
                    debounceRefreshDashboard()
                }
            }
            override fun onLocalVideoStats(source: Constants.VideoSourceType?, stats: LocalVideoStats?) {
                stats?.let {
                    localVideoStats = it
                    debounceRefreshDashboard()
                }
            }
            override fun onLocalAudioStats(stats: LocalAudioStats?) {
                stats?.let {
                    localAudioStats = it
                    debounceRefreshDashboard()
                }
            }
            override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
                stats?.let {
                    remoteVideoStats = it
                    debounceRefreshDashboard()
                }
            }
            override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
                stats?.let {
                    remoteAudioStats = it
                    debounceRefreshDashboard()
                }
            }
            override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo?) {
                info?.let {
                    uplinkNetworkInfo = info
                    debounceRefreshDashboard()
                }
            }
            override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo?) {
                info?.let {
                    downlinkNetworkInfo = info
                    debounceRefreshDashboard()
                }
            }
        }
        connection = RtcConnection(channelId, localUid)
        handler = rtcListener
        CallServiceManager.instance.rtcEngine?.addHandlerEx(rtcListener, connection)
        refreshDashboard()
    }

    private var debounceJob: Job? = null
    private fun debounceRefreshDashboard() {
        debounceJob?.cancel()
        debounceJob = CoroutineScope(coroutineContext).launch {
            delay(1000)
            refreshDashboard()
        }
    }

    private fun refreshDashboard() {
        activity ?: return
        if (!isBoardVisible) return
        // Encoded resolution
        localVideoStats?.let { binding.tvEncodeResolution.text = getString(R.string.pure1v1_dashboard_encode_resolution, "${it.encodedFrameHeight}x${it.encodedFrameWidth}") }
        if (binding.tvEncodeResolution.text.isEmpty()) binding.tvEncodeResolution.text = getString(R.string.pure1v1_dashboard_encode_resolution, "--")
        // Received resolution
        remoteVideoStats?.let { binding.tvReceiveResolution.text = getString(R.string.pure1v1_dashboard_receive_resolution, "${it.height}x${it.width}") }
        if (binding.tvReceiveResolution.text.isEmpty()) binding.tvReceiveResolution.text = getString(R.string.pure1v1_dashboard_receive_resolution, "--")
        // Encoded frame rate
        localVideoStats?.encoderOutputFrameRate.let { binding.tvStatisticEncodeFPS.text = getString(R.string.pure1v1_dashboard_encode_fps, it.toString()) }
        if (binding.tvStatisticEncodeFPS.text.isEmpty()) binding.tvStatisticEncodeFPS.text = getString(R.string.pure1v1_dashboard_encode_fps, "--")
        // Received frame rate
        remoteVideoStats?.decoderOutputFrameRate.let { binding.tvStatisticReceiveFPS.text = getString(R.string.pure1v1_dashboard_receive_fps, it.toString()) }
        if (binding.tvStatisticReceiveFPS.text.isEmpty()) binding.tvStatisticReceiveFPS.text = getString(R.string.pure1v1_dashboard_receive_fps, "--")
        // Downlink delay
        remoteVideoStats?.delay.let { binding.tvStatisticDownDelay.text = getString(R.string.pure1v1_dashboard_delay, it.toString()) }
        if (binding.tvStatisticDownDelay.text.isEmpty()) binding.tvStatisticDownDelay.text = getString(R.string.pure1v1_dashboard_delay, "--")
        // Uplink loss rate
        localVideoStats?.txPacketLossRate.let { binding.tvStatisticUpLossPackage.text = getString(R.string.pure1v1_dashboard_up_loss_package, it.toString()) }
        if (binding.tvStatisticUpLossPackage.text.isEmpty()) binding.tvStatisticUpLossPackage.text = getString(R.string.pure1v1_dashboard_up_loss_package, "--")
        // Downlink loss rate
        remoteVideoStats?.packetLossRate.let { binding.tvStatisticDownLossPackage.text = getString(R.string.pure1v1_dashboard_down_loss_package, it.toString()) }
        if (binding.tvStatisticDownLossPackage.text.isEmpty()) binding.tvStatisticDownLossPackage.text = getString(R.string.pure1v1_dashboard_down_loss_package, "--")
        // Uplink bitrate
        localVideoStats?.sentBitrate.let { binding.tvStatisticUpBitrate.text = getString(R.string.pure1v1_dashboard_up_bitrate, it.toString()) }
        if (binding.tvStatisticUpBitrate.text.isEmpty()) binding.tvStatisticUpBitrate.text = getString(R.string.pure1v1_dashboard_up_bitrate, "--")
        // Downlink bitrate
        remoteVideoStats?.receivedBitrate.let { binding.tvStatisticDownBitrate.text = getString(R.string.pure1v1_dashboard_down_bitrate, it.toString()) }
        if (binding.tvStatisticDownBitrate.text.isEmpty()) binding.tvStatisticDownBitrate.text = getString(R.string.pure1v1_dashboard_down_bitrate, "--")
        // Uplink network
        uplinkNetworkInfo?.video_encoder_target_bitrate_bps?.let { binding.tvStatisticUpNet.text = getString(R.string.pure1v1_dashboard_up_net_speech, (it / 8192).toString()) }
        if (binding.tvStatisticUpNet.text.isEmpty()) binding.tvStatisticUpNet.text = getString(R.string.pure1v1_dashboard_up_net_speech, "--")
        // Downlink network
        downlinkNetworkInfo?.bandwidth_estimation_bps?.let { binding.tvStatisticDownNet.text = getString(R.string.pure1v1_dashboard_down_net_speech, (it / 8192).toString()) }
        if (binding.tvStatisticDownNet.text.isEmpty()) binding.tvStatisticDownNet.text = getString(R.string.pure1v1_dashboard_down_net_speech, "--")

        // Channel name
        connection?.let {
            binding.tvChannelName.text = getString(R.string.pure1v1_dashboard_channel_name, it.channelId)
        }
        if (binding.tvChannelName.text.isEmpty()) binding.tvChannelName.text = getString(R.string.pure1v1_dashboard_channel_name, "--")
    }
}
package io.agora.scene.pure1v1.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1DashboardFragmentBinding
import io.agora.scene.pure1v1.service.CallServiceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext

class DashboardFragment : Fragment() {

    private lateinit var binding: Pure1v1DashboardFragmentBinding

    private var handler: IRtcEngineEventHandler? = null

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
            CallServiceManager.instance.callApi?.removeRTCListener(it)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRTCListener()
    }

    fun updateVisible(b: Boolean) {
        isBoardVisible = b
        if (b) {
            refreshDashboard()
        }
    }

    private fun setupRTCListener() {
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
        CallServiceManager.instance.callApi?.addRTCListener(rtcListener)
        handler = rtcListener
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
        // 编码分辨率
        localVideoStats?.let { binding.tvEncodeResolution.text = getString(R.string.pure1v1_dashboard_encode_resolution, "${it.encodedFrameHeight}x${it.encodedFrameWidth}") }
        if (binding.tvEncodeResolution.text.isEmpty()) binding.tvEncodeResolution.text = getString(R.string.pure1v1_dashboard_encode_resolution, "--")
        // 接收分辨率
        remoteVideoStats?.let { binding.tvReceiveResolution.text = getString(R.string.pure1v1_dashboard_receive_resolution, "${it.height}x${it.width}") }
        if (binding.tvReceiveResolution.text.isEmpty()) binding.tvReceiveResolution.text = getString(R.string.pure1v1_dashboard_receive_resolution, "--")
        // 编码帧率
        localVideoStats?.encoderOutputFrameRate.let { binding.tvStatisticEncodeFPS.text = getString(R.string.pure1v1_dashboard_encode_fps, it.toString()) }
        if (binding.tvStatisticEncodeFPS.text.isEmpty()) binding.tvStatisticEncodeFPS.text = getString(R.string.pure1v1_dashboard_encode_fps, "--")
        // 接收帧率
        remoteVideoStats?.decoderOutputFrameRate.let { binding.tvStatisticReceiveFPS.text = getString(R.string.pure1v1_dashboard_receive_fps, it.toString()) }
        if (binding.tvStatisticReceiveFPS.text.isEmpty()) binding.tvStatisticReceiveFPS.text = getString(R.string.pure1v1_dashboard_receive_fps, "--")
        // 下行延迟
        remoteVideoStats?.delay.let { binding.tvStatisticDownDelay.text = getString(R.string.pure1v1_dashboard_delay, it.toString()) }
        if (binding.tvStatisticDownDelay.text.isEmpty()) binding.tvStatisticDownDelay.text = getString(R.string.pure1v1_dashboard_delay, "--")
        // 上行丢包率
        localVideoStats?.txPacketLossRate.let { binding.tvStatisticUpLossPackage.text = getString(R.string.pure1v1_dashboard_up_loss_package, it.toString()) }
        if (binding.tvStatisticUpLossPackage.text.isEmpty()) binding.tvStatisticUpLossPackage.text = getString(R.string.pure1v1_dashboard_up_loss_package, "--")
        // 下行丢包率
        remoteVideoStats?.packetLossRate.let { binding.tvStatisticDownLossPackage.text = getString(R.string.pure1v1_dashboard_down_loss_package, it.toString()) }
        if (binding.tvStatisticDownLossPackage.text.isEmpty()) binding.tvStatisticDownLossPackage.text = getString(R.string.pure1v1_dashboard_down_loss_package, "--")
        // 上行码率
        localVideoStats?.sentBitrate.let { binding.tvStatisticUpBitrate.text = getString(R.string.pure1v1_dashboard_up_bitrate, it.toString()) }
        if (binding.tvStatisticUpBitrate.text.isEmpty()) binding.tvStatisticUpBitrate.text = getString(R.string.pure1v1_dashboard_up_bitrate, "--")
        // 下行码率
        remoteVideoStats?.receivedBitrate.let { binding.tvStatisticDownBitrate.text = getString(R.string.pure1v1_dashboard_down_bitrate, it.toString()) }
        if (binding.tvStatisticDownBitrate.text.isEmpty()) binding.tvStatisticDownBitrate.text = getString(R.string.pure1v1_dashboard_down_bitrate, "--")
        // 上行网络
        uplinkNetworkInfo?.video_encoder_target_bitrate_bps?.let { binding.tvStatisticUpNet.text = getString(R.string.pure1v1_dashboard_up_net_speech, (it / 8192).toString()) }
        if (binding.tvStatisticUpNet.text.isEmpty()) binding.tvStatisticUpNet.text = getString(R.string.pure1v1_dashboard_up_net_speech, "--")
        // 下行网络
        downlinkNetworkInfo?.bandwidth_estimation_bps?.let { binding.tvStatisticDownNet.text = getString(R.string.pure1v1_dashboard_down_net_speech, (it / 8192).toString()) }
        if (binding.tvStatisticDownNet.text.isEmpty()) binding.tvStatisticDownNet.text = getString(R.string.pure1v1_dashboard_down_net_speech, "--")
    }
}
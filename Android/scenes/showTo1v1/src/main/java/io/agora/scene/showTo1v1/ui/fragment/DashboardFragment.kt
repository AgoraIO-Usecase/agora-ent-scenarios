package io.agora.scene.showTo1v1.ui.fragment

import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.callAPI.CallStateType
import io.agora.scene.showTo1v1.databinding.ShowTo1v1DashboardFragmentBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo

class DashboardFragment : Fragment() {

    companion object {

        private const val TAG = "ShowTo1v1_DashboardFragment"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun newInstance(romInfo: ShowTo1v1RoomInfo) = DashboardFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_ROOM_DETAIL_INFO, romInfo)
            }
        }
    }

    private lateinit var binding: ShowTo1v1DashboardFragmentBinding


    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }

    private val mRoomInfo by lazy { (arguments?.getParcelable(EXTRA_ROOM_DETAIL_INFO) as? ShowTo1v1RoomInfo)!! }

    private val mShowTo1v1RtcConnection by lazy {
        RtcConnection("", mShowTo1v1Manger.mCurrentUser.getIntUserId())
    }

    private val mMainRtcConnection by lazy {
        RtcConnection(mRoomInfo.roomId, mShowTo1v1Manger.mCurrentUser.getIntUserId())
    }

    private var isBoardVisible = false

    private var mCallState = CallStateType.Idle

    override fun onDestroy() {
        mRtcEngine.removeHandlerEx(showTo1v1RtcListener, mShowTo1v1RtcConnection)
        mRtcEngine.removeHandlerEx(mainRtcListener, mShowTo1v1RtcConnection)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ShowTo1v1DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRtcEngine.addHandlerEx(mainRtcListener, mMainRtcConnection)
    }

    fun updateVisible(b: Boolean) {
        isBoardVisible = b
    }

    fun updateCallState(callState: CallStateType) {
        mCallState = callState
        if (mCallState == CallStateType.Connected) {
            mShowTo1v1Manger.mConnectedChannelId?.let {
                mShowTo1v1RtcConnection.channelId = it
                mRtcEngine.addHandlerEx(showTo1v1RtcListener, mShowTo1v1RtcConnection)
            }
        } else {
            mRtcEngine.removeHandlerEx(showTo1v1RtcListener, mShowTo1v1RtcConnection)
        }
    }

    private val mainRtcListener = object : IRtcEngineEventHandler() {
        override fun onRtcStats(stats: RtcStats?) {
            stats ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    cpuAppUsage = stats.cpuAppUsage,
                    cpuTotalUsage = stats.cpuTotalUsage,
                )
            }
        }

        override fun onLocalVideoStats(source: Constants.VideoSourceType?, stats: LocalVideoStats?) {
            stats ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    upBitrate = stats.sentBitrate,
                    encodeFps = stats.encoderOutputFrameRate,
                    upLossPackage = stats.txPacketLossRate,
                    encodeVideoSize = Size(stats.encodedFrameWidth, stats.encodedFrameHeight)
                )
            }
        }

        override fun onLocalAudioStats(stats: LocalAudioStats?) {
            stats ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    audioBitrate = stats.sentBitrate,
                    audioLossPackage = stats.txPacketLossRate
                )
            }
        }

        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
            stats ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    downBitrate = stats.receivedBitrate,
                    receiveFPS = stats.decoderOutputFrameRate,
                    downLossPackage = stats.packetLossRate,
                    receiveVideoSize = Size(stats.width, stats.height),
                    downDelay = stats.delay
                )
            }
        }

        override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
            stats ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    audioBitrate = stats.receivedBitrate,
                    audioLossPackage = stats.audioLossRate
                )
            }
        }

        override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo?) {
            info ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(upLinkBps = info.video_encoder_target_bitrate_bps)
            }
        }

        override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo?) {
            info ?: return
            if (mCallState == CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(downLinkBps = info.bandwidth_estimation_bps)
            }
        }
    }

    private val showTo1v1RtcListener = object : IRtcEngineEventHandler() {

        override fun onRtcStats(stats: RtcStats?) {
            stats ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    cpuAppUsage = stats.cpuAppUsage,
                    cpuTotalUsage = stats.cpuTotalUsage,
                )
            }
        }

        override fun onLocalVideoStats(source: Constants.VideoSourceType?, stats: LocalVideoStats?) {
            stats ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    upBitrate = stats.sentBitrate,
                    encodeFps = stats.encoderOutputFrameRate,
                    upLossPackage = stats.txPacketLossRate,
                    encodeVideoSize = Size(stats.encodedFrameWidth, stats.encodedFrameHeight)
                )
            }
        }

        override fun onLocalAudioStats(stats: LocalAudioStats?) {
            stats ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    audioBitrate = stats.sentBitrate,
                    audioLossPackage = stats.txPacketLossRate
                )
            }
        }

        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
            stats ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    downBitrate = stats.receivedBitrate,
                    receiveFPS = stats.decoderOutputFrameRate,
                    downLossPackage = stats.packetLossRate,
                    receiveVideoSize = Size(stats.width, stats.height),
                    downDelay = stats.delay
                )
            }
        }

        override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
            stats ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(
                    audioBitrate = stats.receivedBitrate,
                    audioLossPackage = stats.audioLossRate
                )
            }
        }

        override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo?) {
            info ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(upLinkBps = info.video_encoder_target_bitrate_bps)
            }
        }

        override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo?) {
            info ?: return
            if (mCallState != CallStateType.Connected) return
            activity?.runOnUiThread {
                refreshDashboardInfo(downLinkBps = info.bandwidth_estimation_bps)
            }
        }
    }

    fun refreshDashboardInfo(
        upLinkBps: Int? = null, downLinkBps: Int? = null,
        audioBitrate: Int? = null, audioLossPackage: Int? = null,
        cpuAppUsage: Double? = null, cpuTotalUsage: Double? = null,
        // 编码分辨率、接收分辨率
        encodeVideoSize: Size? = null, receiveVideoSize: Size? = null,
        // 编码帧率、接收帧率
        encodeFps: Int? = null, receiveFPS: Int? = null,
        // 下行延迟
        downDelay: Int? = null,
        // 上行丢包率、下行丢包率
        upLossPackage: Int? = null, downLossPackage: Int? = null,
        // 上行码率、下行码率
        upBitrate: Int? = null, downBitrate: Int? = null
    ) {
        Log.d("tastt", "refreshDashboardInfo1")
        activity ?: return
        if (!isBoardVisible) {
            return
        }
        Log.d("tastt", "refreshDashboardInfo2")
        // 编码分辨率
        encodeVideoSize?.let {
            binding.tvEncodeResolution.text =
                getString(R.string.show_to1v1_dashboard_encode_resolution, "${it.height}x${it.width}")
        }
        if (binding.tvEncodeResolution.text.isEmpty()) binding.tvEncodeResolution.text =
            getString(R.string.show_to1v1_dashboard_encode_resolution, "--")
        // 接收分辨率
        receiveVideoSize?.let {
            binding.tvReceiveResolution.text =
                getString(R.string.show_to1v1_dashboard_receive_resolution, "${it.height}x${it.width}")
        }
        if (binding.tvReceiveResolution.text.isEmpty()) binding.tvReceiveResolution.text =
            getString(R.string.show_to1v1_dashboard_receive_resolution, "--")
        // 编码帧率
        encodeFps?.let {
            binding.tvStatisticEncodeFPS.text = getString(R.string.show_to1v1_dashboard_encode_fps, it.toString())
        }
        if (binding.tvStatisticEncodeFPS.text.isEmpty()) binding.tvStatisticEncodeFPS.text =
            getString(R.string.show_to1v1_dashboard_encode_fps, "--")
        // 接收帧率
        receiveFPS?.let {
            binding.tvStatisticReceiveFPS.text = getString(R.string.show_to1v1_dashboard_receive_fps, it.toString())
        }
        if (binding.tvStatisticReceiveFPS.text.isEmpty()) binding.tvStatisticReceiveFPS.text =
            getString(R.string.show_to1v1_dashboard_receive_fps, "--")
        // 下行延迟
        downDelay?.let {
            binding.tvStatisticDownDelay.text = getString(R.string.show_to1v1_dashboard_delay, it.toString())
        }
        if (binding.tvStatisticDownDelay.text.isEmpty()) binding.tvStatisticDownDelay.text =
            getString(R.string.show_to1v1_dashboard_delay, "--")
        // 上行丢包率
        upLossPackage?.let {
            binding.tvStatisticUpLossPackage.text =
                getString(R.string.show_to1v1_dashboard_up_loss_package, it.toString())
        }
        if (binding.tvStatisticUpLossPackage.text.isEmpty()) binding.tvStatisticUpLossPackage.text =
            getString(R.string.show_to1v1_dashboard_up_loss_package, "--")
        // 下行丢包率
        downLossPackage?.let {
            binding.tvStatisticDownLossPackage.text =
                getString(R.string.show_to1v1_dashboard_down_loss_package, it.toString())
        }
        if (binding.tvStatisticDownLossPackage.text.isEmpty()) binding.tvStatisticDownLossPackage.text =
            getString(R.string.show_to1v1_dashboard_down_loss_package, "--")
        // 上行码率
        upBitrate?.let {
            binding.tvStatisticUpBitrate.text = getString(R.string.show_to1v1_dashboard_up_bitrate, it.toString())
        }
        if (binding.tvStatisticUpBitrate.text.isEmpty()) binding.tvStatisticUpBitrate.text =
            getString(R.string.show_to1v1_dashboard_up_bitrate, "--")
        // 下行码率
        downBitrate?.let {
            binding.tvStatisticDownBitrate.text = getString(R.string.show_to1v1_dashboard_down_bitrate, it.toString())
        }
        if (binding.tvStatisticDownBitrate.text.isEmpty()) binding.tvStatisticDownBitrate.text =
            getString(R.string.show_to1v1_dashboard_down_bitrate, "--")
        // 上行网络
        upLinkBps?.let {
            binding.tvStatisticUpNet.text =
                getString(R.string.show_to1v1_dashboard_up_net_speech, (it / 8192).toString())
        }
        if (binding.tvStatisticUpNet.text.isEmpty()) binding.tvStatisticUpNet.text =
            getString(R.string.show_to1v1_dashboard_up_net_speech, "--")
        // 下行网络
        downLinkBps?.let {
            binding.tvStatisticDownNet.text =
                getString(R.string.show_to1v1_dashboard_down_net_speech, (it / 8192).toString())
        }
        if (binding.tvStatisticDownNet.text.isEmpty()) binding.tvStatisticDownNet.text =
            getString(R.string.show_to1v1_dashboard_down_net_speech, "--")
    }
}
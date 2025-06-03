package io.agora.scene.showTo1v1.ui.fragment

import android.os.Bundle
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.manager.UserManager
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.onetoone.CallStateType
import io.agora.scene.showTo1v1.databinding.ShowTo1v1DashboardFragmentBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo

class DashboardFragment : Fragment() {

    companion object {

        private const val TAG = "ShowTo1v1_DashboardFragment"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"
        private const val CALL_ROOM_DETAIL_INFO = "1v1ChannelId"

        fun newInstance(roomInfo: ShowTo1v1RoomInfo, callChannelId: String?) = DashboardFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_ROOM_DETAIL_INFO, roomInfo)
                putString(CALL_ROOM_DETAIL_INFO, callChannelId)
            }
        }
    }

    private lateinit var binding: ShowTo1v1DashboardFragmentBinding


    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }

    private val mRoomInfo by lazy { (arguments?.getParcelable(EXTRA_ROOM_DETAIL_INFO) as? ShowTo1v1RoomInfo)!! }
    private val callChannelId by lazy { arguments?.getString(CALL_ROOM_DETAIL_INFO) }

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

        mRtcEngine.addHandlerEx(object :IRtcEngineEventHandler() {

        }, RtcConnection())

        mRtcEngine.addHandlerEx(mainRtcListener, mMainRtcConnection)
        if (callChannelId == null) {
            renewCallChannel(mRoomInfo.roomId)
        } else {
            renewCallChannel(callChannelId!!)
        }
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
            renewCallChannel(mRoomInfo.roomId)
            mRtcEngine.removeHandlerEx(showTo1v1RtcListener, mShowTo1v1RtcConnection)
        }
        if (::binding.isInitialized) {
            if (mCallState != CallStateType.Connected && mRoomInfo.userId != UserManager.getInstance().user.id.toString()) {
                binding.tvEncodeResolution.text = getString(R.string.show_to1v1_dashboard_encode_resolution, "--")
                binding.tvStatisticEncodeFPS.text = getString(R.string.show_to1v1_dashboard_encode_fps, "--")
                binding.tvStatisticUpBitrate.text = getString(R.string.show_to1v1_dashboard_up_bitrate, "--")
                binding.tvStatisticUpLossPackage.text = getString(R.string.show_to1v1_dashboard_up_loss_package, "--")
                binding.tvStatisticUpNet.text = getString(R.string.show_to1v1_dashboard_up_net_speech, "--")
            }
            if (mCallState != CallStateType.Connected && mRoomInfo.userId == UserManager.getInstance().user.id.toString()) {
                binding.tvReceiveResolution.text = getString(R.string.show_to1v1_dashboard_receive_resolution, "--")
                binding.tvStatisticReceiveFPS.text = getString(R.string.show_to1v1_dashboard_receive_fps, "--")
                binding.tvStatisticDownBitrate.text = getString(R.string.show_to1v1_dashboard_down_bitrate, "--")
                binding.tvStatisticDownLossPackage.text = getString(R.string.show_to1v1_dashboard_down_loss_package, "--")
                binding.tvStatisticDownNet.text = getString(R.string.show_to1v1_dashboard_down_net_speech, "--")
                binding.tvStatisticDownDelay.text = getString(R.string.show_to1v1_dashboard_delay, "--")
            }
        }
    }

    fun renewCallChannel(channelId: String) {
        refreshDashboardInfo(channelId = channelId)
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
        // Encoding resolution, receiving resolution
        encodeVideoSize: Size? = null, receiveVideoSize: Size? = null,
        // Encoding frame rate, receiving frame rate
        encodeFps: Int? = null, receiveFPS: Int? = null,
        // Downlink delay
        downDelay: Int? = null,
        // Uplink packet loss rate, downlink packet loss rate
        upLossPackage: Int? = null, downLossPackage: Int? = null,
        // Uplink bitrate, downlink bitrate
        upBitrate: Int? = null, downBitrate: Int? = null,
        // Channel name
        channelId: String? = null
    ) {
        activity ?: return
        if (isBoardVisible) {
            // Encoding resolution
            encodeVideoSize?.let {
                binding.tvEncodeResolution.text =
                    getString(R.string.show_to1v1_dashboard_encode_resolution, "${it.height}x${it.width}")
            }
            if (binding.tvEncodeResolution.text.isEmpty()) binding.tvEncodeResolution.text =
                getString(R.string.show_to1v1_dashboard_encode_resolution, "--")
            // Receiving resolution
            receiveVideoSize?.let {
                binding.tvReceiveResolution.text =
                    getString(R.string.show_to1v1_dashboard_receive_resolution, "${it.height}x${it.width}")
            }
            if (binding.tvReceiveResolution.text.isEmpty()) binding.tvReceiveResolution.text =
                getString(R.string.show_to1v1_dashboard_receive_resolution, "--")
            // Encoding frame rate
            encodeFps?.let {
                binding.tvStatisticEncodeFPS.text = getString(R.string.show_to1v1_dashboard_encode_fps, it.toString())
            }
            if (binding.tvStatisticEncodeFPS.text.isEmpty()) binding.tvStatisticEncodeFPS.text =
                getString(R.string.show_to1v1_dashboard_encode_fps, "--")
            // Receiving frame rate
            receiveFPS?.let {
                binding.tvStatisticReceiveFPS.text = getString(R.string.show_to1v1_dashboard_receive_fps, it.toString())
            }
            if (binding.tvStatisticReceiveFPS.text.isEmpty()) binding.tvStatisticReceiveFPS.text =
                getString(R.string.show_to1v1_dashboard_receive_fps, "--")
            // Downlink delay
            downDelay?.let {
                binding.tvStatisticDownDelay.text = getString(R.string.show_to1v1_dashboard_delay, it.toString())
            }
            if (binding.tvStatisticDownDelay.text.isEmpty()) binding.tvStatisticDownDelay.text =
                getString(R.string.show_to1v1_dashboard_delay, "--")
            // Uplink packet loss rate
            upLossPackage?.let {
                binding.tvStatisticUpLossPackage.text =
                    getString(R.string.show_to1v1_dashboard_up_loss_package, it.toString())
            }
            if (binding.tvStatisticUpLossPackage.text.isEmpty()) binding.tvStatisticUpLossPackage.text =
                getString(R.string.show_to1v1_dashboard_up_loss_package, "--")
            // Downlink packet loss rate
            downLossPackage?.let {
                binding.tvStatisticDownLossPackage.text =
                    getString(R.string.show_to1v1_dashboard_down_loss_package, it.toString())
            }
            if (binding.tvStatisticDownLossPackage.text.isEmpty()) binding.tvStatisticDownLossPackage.text =
                getString(R.string.show_to1v1_dashboard_down_loss_package, "--")
            // Uplink bitrate
            upBitrate?.let {
                binding.tvStatisticUpBitrate.text = getString(R.string.show_to1v1_dashboard_up_bitrate, it.toString())
            }
            if (binding.tvStatisticUpBitrate.text.isEmpty()) binding.tvStatisticUpBitrate.text =
                getString(R.string.show_to1v1_dashboard_up_bitrate, "--")
            // Downlink bitrate
            downBitrate?.let {
                binding.tvStatisticDownBitrate.text = getString(R.string.show_to1v1_dashboard_down_bitrate, it.toString())
            }
            if (binding.tvStatisticDownBitrate.text.isEmpty()) binding.tvStatisticDownBitrate.text =
                getString(R.string.show_to1v1_dashboard_down_bitrate, "--")
            // Uplink network
            upLinkBps?.let {
                binding.tvStatisticUpNet.text =
                    getString(R.string.show_to1v1_dashboard_up_net_speech, (it / 8192).toString())
            }
            if (binding.tvStatisticUpNet.text.isEmpty()) binding.tvStatisticUpNet.text =
                getString(R.string.show_to1v1_dashboard_up_net_speech, "--")
            // Downlink network
            downLinkBps?.let {
                binding.tvStatisticDownNet.text =
                    getString(R.string.show_to1v1_dashboard_down_net_speech, (it / 8192).toString())
            }
            if (binding.tvStatisticDownNet.text.isEmpty()) binding.tvStatisticDownNet.text =
                getString(R.string.show_to1v1_dashboard_down_net_speech, "--")
        }

        // Channel name
        channelId?.let {
            binding.tvChannelName.text = getString(R.string.show_to1v1_dashboard_channel_name, it)
        }
        if (binding.tvChannelName.text.isEmpty()) binding.tvChannelName.text = getString(R.string.show_to1v1_dashboard_channel_name, "--")
    }
}
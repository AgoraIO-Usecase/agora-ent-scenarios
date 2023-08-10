//
//  ShowStatisticsInfo.swift
//  AgoraEntScenarios
//
//  Created by HeZhengQing on 2022/11/9.
//

import Foundation
import AgoraRtcKit

struct ShowPanelData {
    let left: String
    let right: String
}

class ShowDataPanelPresenter {
    
    private var channelStats = AgoraChannelStats()
    private var localVideoStats = AgoraRtcLocalVideoStats()
    private var localAudioStats = AgoraRtcLocalAudioStats()
    private var remoteVideoStats = AgoraRtcRemoteVideoStats()
    private var remoteAudioStats = AgoraRtcRemoteAudioStats()
    private var dimension = CGSize.zero
    private var fps: UInt = 0
    private var uplink: Int32 = 0
    private var downlink: Int32 = 0
    private var callTs: Int = 0

    func updateChannelStats(_ stats: AgoraChannelStats) {
        channelStats = stats
    }
    
    func updateLocalVideoStats(_ stats: AgoraRtcLocalVideoStats) {
        localVideoStats = stats
        dimension = CGSize(width: Int(stats.encodedFrameWidth),
                           height: Int(stats.encodedFrameHeight))
        fps = stats.sentFrameRate
    }
    
    func updateLocalAudioStats(_ stats: AgoraRtcLocalAudioStats) {
        localAudioStats = stats
    }
    
    func updateUplinkNetworkInfo(_ uplinkNetworkInfo: AgoraUplinkNetworkInfo) {
        uplink = uplinkNetworkInfo.videoEncoderTargetBitrateBps / 8 / 1024
    }
    
    func updateDownlinkNetworkInfo(_ downlinkNetworkInfo: AgoraDownlinkNetworkInfo) {
        downlink = downlinkNetworkInfo.bandwidthEstimationBps / 8 / 1024
    }
    
    func updateVideoStats(_ stats: AgoraRtcRemoteVideoStats) {
        remoteVideoStats = stats
        dimension = CGSize(width: Int(stats.width),
                           height: Int(stats.height))
        fps = UInt(stats.rendererOutputFrameRate)
    }
    
    func updateAudioStats(_ stats: AgoraRtcRemoteAudioStats) {
        remoteAudioStats = stats
    }
    
    func updateTimestamp(_ ts: TimeInterval) {
        callTs = Int(ts)
    }
    
    func generatePanelData(audioOnly: Bool, send: Bool, receive: Bool) -> ShowPanelData {
        let sendPanel = send ? sendData(audioOnly: audioOnly) : cleanSendData()
        let receivePanel = receive ? receiveData(audioOnly: audioOnly) : cleanReceiveData()
        let otherPanel = otherData(send: send, receive: receive)
        return ShowPanelData(
            left: [sendPanel.left, receivePanel.left, otherPanel.left].joined(separator: "\n"),
            right: [sendPanel.right, receivePanel.right, otherPanel.right].joined(separator: "\n")
        )
    }
    
    private func cleanReceiveData() -> ShowPanelData {
        let sendTitle = "show_statistic_receive_title".show_localized
        let videoSize = "show_statistic_receive_resolution".show_localized+": --"
        let videoSend = "show_statistic_bitrate".show_localized+": --"
        let downlink = "show_statistic_down_net_speech".show_localized+": \(downlink) KB/s"

        let fps = "show_statistic_receive_fps".show_localized+": --"
        let vSendLoss = "show_statistic_down_loss_package".show_localized+": --"
        let lastmile = "show_statistic_delay".show_localized+": --"
        
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n") + "\n"
        let rightInfo = ["   ",     fps,        vSendLoss,  lastmile].joined(separator: "\n") + "\n"

        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func cleanSendData() -> ShowPanelData {
        let sendTitle = "show_statistic_send_title".show_localized
        let videoSize = "show_statistic_encode_resolution".show_localized+": --"
        let videoSend = "show_statistic_up_bitrate".show_localized+": --"
        let uplink = "show_statistic_up_net_speech".show_localized+": \(uplink) KB/s"
        
        let fps = "show_advance_setting_FPS_title".show_localized+": --"
        let vSendLoss = "show_statistic_up_loss_package".show_localized+": --"
        
        let leftInfo =  [sendTitle, videoSize, videoSend,   uplink ].joined(separator: "\n") + "\n"
        let rightInfo = ["  ",     fps,       vSendLoss,   " " ].joined(separator: "\n") + "\n"
        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func sendData(audioOnly: Bool) -> ShowPanelData {
        let sendTitle = "show_statistic_send_title".show_localized
        let videoSize = "show_statistic_encode_resolution".show_localized+": \(localVideoStats.encodedFrameHeight) x \(localVideoStats.encodedFrameWidth)"
        let videoSend = "show_statistic_up_bitrate".show_localized+": \(localVideoStats.sentBitrate) kbps"
        let uplink = "show_statistic_up_net_speech".show_localized+": \(uplink) KB/s"
        
        let fps = "show_advance_setting_FPS_title".show_localized+": \(fps) fps"
        let vSendLoss = "show_statistic_up_loss_package".show_localized+": \(localVideoStats.txPacketLossRate) %"
        
        let audioSend = "ASend: \(localAudioStats.sentBitrate) kbps"
        let cpu = "CPU: \(channelStats.cpuAppUsage)%/\(channelStats.cpuTotalUsage) %"
        let aSendLoss = "ASend Loss: \(localAudioStats.txPacketLossRate) %"
        
        if audioOnly {
            return ShowPanelData(left: [audioSend, cpu, aSendLoss].joined(separator: "\n"), right: "")
        }
        let leftInfo =  [sendTitle, videoSize, videoSend,   uplink ].joined(separator: "\n") + "\n"
        let rightInfo = ["   ",     fps,       vSendLoss,   " " ].joined(separator: "\n") + "\n"

        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func receiveData(audioOnly: Bool) -> ShowPanelData {
        let sendTitle = "show_statistic_receive_title".show_localized
        let videoSize = "show_statistic_receive_resolution".show_localized+": \(remoteVideoStats.height) x \(remoteVideoStats.width)"
        let videoSend = "show_statistic_bitrate".show_localized+": \(remoteVideoStats.receivedBitrate) kbps"
        let downlink = "show_statistic_down_net_speech".show_localized+": \(downlink) KB/s"

        let fps = "show_statistic_receive_fps".show_localized+": \(fps) fps"
        let vSendLoss = "show_statistic_down_loss_package".show_localized+": \(remoteVideoStats.packetLossRate) %"
        let lastmile = "show_statistic_delay".show_localized+": \(remoteVideoStats.delay) ms"
        
        let audioRecv = "ARecv: \(remoteAudioStats.receivedBitrate) kbps"
        let audioLoss = "ALoss: \(remoteAudioStats.audioLossRate) %"
        
        if audioOnly {
            return ShowPanelData(left: [audioRecv, audioLoss, vSendLoss].joined(separator: "\n") + "\n", right: "\n")
        }
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n") + "\n"
        let rightInfo = ["  ",     fps,        vSendLoss,  lastmile].joined(separator: "\n") + "\n"

        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func otherData(send: Bool, receive: Bool) -> ShowPanelData {
        let params = ShowAgoraKitManager.shared.rtcParam
        let onStr = "show_setting_switch_on".show_localized
        let offStr = "show_setting_switch_off".show_localized
        // left:
        // 其他
        let title = "show_statistic_title_other".show_localized
        // 秒开耗时
        let startup = receive ? "\(callTs) ms" : "--"
        let startupStr = "show_statistic_startup_time".show_localized + ": " + startup
        // h265开关
        let h265 = receive ? onStr : "--"
        let h265Str = "H265" + ": " + h265
        // 超分开关
        let sr = receive ? (params.sr ? onStr : offStr) : "--"
        let srStr = "show_statistic_SR_switch".show_localized + ": " + sr
        // 小流开关
        let microStream = send ? (params.simulcast ? onStr : offStr) : "--"
        let microStreamStr = "show_statistic_micro_stream_switch".show_localized + ": " + microStream
        // right:
        //机型等级
        let levelStr = "show_statistic_device_level".show_localized
        + ": "
        + ShowAgoraKitManager.shared.deviceLevel.description()
        + "(\(ShowAgoraKitManager.shared.deviceScore))"
        //pvc开关
        let pvc = send ? (params.pvc ? onStr : offStr) : "--"
        let pvcStr = "show_statistic_pvc_switch".show_localized + ": " + pvc
        //svc开关
        let svc = send ? (params.svc ? onStr : offStr) : "--"
        let svcStr = "show_statistic_svc_switch".show_localized + ": " + svc
        let left = [title, startupStr, h265Str, srStr,  microStreamStr].joined(separator: "\n") + "\n"
        let right = ["  ", levelStr,  pvcStr, svcStr].joined(separator: "\n") + "\n"
        return ShowPanelData(left: left, right: right)
    }
}

//
//  ShowStatisticsInfo.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/9.
//

import Foundation
import AgoraRtcKit

struct ShowStatisticsInfo {
    struct LocalInfo {
        var channelStats = AgoraChannelStats()
        var videoStats = AgoraRtcLocalVideoStats()
        var audioStats = AgoraRtcLocalAudioStats()
    }
    
    struct RemoteInfo {
        var videoStats = AgoraRtcRemoteVideoStats()
        var audioStats = AgoraRtcRemoteAudioStats()
    }
    
    enum StatisticsType {
        case local(LocalInfo), remote(RemoteInfo)
        
        var isLocal: Bool {
            switch self {
            case .local:  return true
            case .remote: return false
            }
        }
    }
    
    private var dimension = CGSize.zero
    private var fps: UInt = 0
    private var uplink: Int32 = 0
    private var downlink: Int32 = 0
    private var type: StatisticsType
    
    init(type: StatisticsType) {
        self.type = type
    }
    
    mutating func updateChannelStats(_ stats: AgoraChannelStats) {
        guard type.isLocal else { return }
        switch type {
        case .local(let info):
            var new = info
            new.channelStats = stats
            type = .local(new)
            
        default:
            break
        }
    }
    
    mutating func updateLocalVideoStats(_ stats: AgoraRtcLocalVideoStats) {
        guard type.isLocal else { return }
        switch type {
        case .local(let info):
            var new = info
            new.videoStats = stats
            type = .local(new)
            
        default:
            break
        }
        dimension = CGSize(width: Int(stats.encodedFrameWidth),
                           height: Int(stats.encodedFrameHeight))
        fps = stats.sentFrameRate
    }
    
    mutating func updateLocalAudioStats(_ stats: AgoraRtcLocalAudioStats) {
        guard type.isLocal else { return }
        switch type {
        case .local(let info):
            var new = info
            new.audioStats = stats
            type = .local(new)
            
        default: break
        }
    }
    
    mutating func updateUplinkNetworkInfo(_ uplinkNetworkInfo: AgoraUplinkNetworkInfo) {
        uplink = uplinkNetworkInfo.videoEncoderTargetBitrateBps / 8 / 1024
    }
    
    mutating func updateDownlinkNetworkInfo(_ downlinkNetworkInfo: AgoraDownlinkNetworkInfo) {
        downlink = downlinkNetworkInfo.bandwidthEstimationBps / 8 / 1024
    }
    
    mutating func updateVideoStats(_ stats: AgoraRtcRemoteVideoStats) {
        switch type {
        case .remote(let info):
            var new = info
            new.videoStats = stats
            dimension = CGSize(width: Int(stats.width),
                               height: Int(stats.height))
            fps = UInt(stats.rendererOutputFrameRate)
            self.type = .remote(new)
            
        default: break
        }
    }
    
    mutating func updateAudioStats(_ stats: AgoraRtcRemoteAudioStats) {
        switch type {
        case .remote(let info):
            var new = info
            new.audioStats = stats
            self.type = .remote(new)
            
        default: break
        }
    }
    
    mutating func cleanRemoteDescription() -> (String, String){
        let sendTitle = "show_statistic_receive_title".show_localized + "\n"
        let videoSize = "show_statistic_receive_resolution"+": 0 x 0"
        let videoSend = "show_statistic_bitrate".show_localized+": \(0) kbps"
        let downlink = "show_statistic_down_net_speech".show_localized+": \(downlink) KB/s"

        let fps = "show_statistic_receive_fps".show_localized+": \(0) fps"
        let vSendLoss = "show_statistic_down_loss_package".show_localized+": \(0) %"
        let lastmile = "show_statistic_delay".show_localized+": \(0) ms"
        
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n\n")
        let rightInfo = [" \n",     fps,        vSendLoss,  lastmile].joined(separator: "\n\n")

        return (leftInfo, rightInfo)
    }
    
    mutating func cleanLocalDescription() -> (String, String){
        let sendTitle = "show_statistic_send_title".show_localized + "\n"
        let videoSize = "show_statistic_encode_resolution".show_localized+": 0 x 0"
        let videoSend = "show_statistic_up_bitrate".show_localized+": 0 kbps"
        let uplink = "show_statistic_up_net_speech".show_localized+": \(uplink) KB/s"
        
        let fps = "show_advance_setting_FPS_title".show_localized+": 0 fps"
        let vSendLoss = "show_statistic_up_loss_package".show_localized+": 0 %"
        
        let leftInfo =  [sendTitle, videoSize, videoSend,   uplink ].joined(separator: "\n\n")
        let rightInfo = [" \n",     fps,       vSendLoss,   " " ].joined(separator: "\n\n")

        return (leftInfo, rightInfo)

    }
    
    func description(audioOnly: Bool) -> (String, String) {
        switch type {
        case .local(let info):  return localDescription(info: info, audioOnly: audioOnly)
        case .remote(let info): return remoteDescription(info: info, audioOnly: audioOnly)
        }
    }
    
    private func localDescription(info: LocalInfo, audioOnly: Bool) -> (String, String) {
        let sendTitle = "show_statistic_send_title".show_localized + "\n"
        let videoSize = "show_statistic_encode_resolution".show_localized+": \(info.videoStats.encodedFrameWidth) x \(info.videoStats.encodedFrameHeight)"
        let videoSend = "show_statistic_up_bitrate".show_localized+": \(info.videoStats.sentBitrate) kbps"
        let uplink = "show_statistic_up_net_speech".show_localized+": \(uplink) KB/s"
        
        let fps = "show_advance_setting_FPS_title".show_localized+": \(fps) fps"
        let vSendLoss = "show_statistic_up_loss_package".show_localized+": \(info.videoStats.txPacketLossRate) %"
        
        let audioSend = "ASend: \(info.audioStats.sentBitrate) kbps"
        let cpu = "CPU: \(info.channelStats.cpuAppUsage)%/\(info.channelStats.cpuTotalUsage) %"
        let aSendLoss = "ASend Loss: \(info.audioStats.txPacketLossRate) %"
        
        if audioOnly {
            return ([audioSend, cpu, aSendLoss].joined(separator: "\n"), "")
        }
        let leftInfo =  [sendTitle, videoSize, videoSend,   uplink ].joined(separator: "\n\n")
        let rightInfo = [" \n",     fps,       vSendLoss,   " " ].joined(separator: "\n\n")

        return (leftInfo, rightInfo)
    }
    
    private func remoteDescription(info: RemoteInfo, audioOnly: Bool) -> (String, String) {
        let sendTitle = "show_statistic_receive_title".show_localized + "\n"
        let videoSize = "show_statistic_receive_resolution".show_localized+": \(info.videoStats.width) x \(info.videoStats.height)"
        let videoSend = "show_statistic_bitrate".show_localized+": \(info.videoStats.receivedBitrate) kbps"
        let downlink = "show_statistic_down_net_speech".show_localized+": \(downlink) KB/s"

        let fps = "show_statistic_receive_fps".show_localized+": \(fps) fps"
        let vSendLoss = "show_statistic_down_loss_package".show_localized+": \(info.videoStats.packetLossRate) %"
        let lastmile = "show_statistic_delay".show_localized+": \(info.videoStats.delay) ms"
        
        let audioRecv = "ARecv: \(info.audioStats.receivedBitrate) kbps"
        let audioLoss = "ALoss: \(info.audioStats.audioLossRate) %"
        
        if audioOnly {
            return ([audioRecv, audioLoss, vSendLoss].joined(separator: "\n"), "")
        }
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n\n")
        let rightInfo = [" \n",     fps,        vSendLoss,  lastmile].joined(separator: "\n\n")

        return (leftInfo, rightInfo)
    }
}

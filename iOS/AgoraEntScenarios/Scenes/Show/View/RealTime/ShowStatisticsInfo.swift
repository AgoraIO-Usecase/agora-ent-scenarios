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
        uplink = uplinkNetworkInfo.videoEncoderTargetBitrateBps / 1000
    }
    
    mutating func updateDownlinkNetworkInfo(_ downlinkNetworkInfo: AgoraDownlinkNetworkInfo) {
        downlink = downlinkNetworkInfo.bandwidthEstimationBps / 1000
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
        let sendTitle = "接受\n".show_localized
        let videoSize = "接收分辨率"+": 0 x 0"
        let videoSend = "码率".show_localized+": \(0) kbps"
        let downlink = "下行网络".show_localized+": \(downlink) KB/s"

        let fps = "接收帧率".show_localized+": \(0) fps"
        let vSendLoss = "下行丢包率".show_localized+": \(0) %"
        let lastmile = "延迟".show_localized+": \(0) ms"
        
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n\n")
        let rightInfo = [" \n",     fps,        vSendLoss,  lastmile].joined(separator: "\n\n")

        return (leftInfo, rightInfo)
    }
    
    func description(audioOnly: Bool) -> (String, String) {
        switch type {
        case .local(let info):  return localDescription(info: info, audioOnly: audioOnly)
        case .remote(let info): return remoteDescription(info: info, audioOnly: audioOnly)
        }
    }
    
    private func localDescription(info: LocalInfo, audioOnly: Bool) -> (String, String) {
        let sendTitle = "发送\n".show_localized
        let videoSize = "编码分辨率"+": \(info.videoStats.encodedFrameWidth) x \(info.videoStats.encodedFrameHeight)"
        let videoSend = "发送码率".show_localized+": \(info.videoStats.sentBitrate) kbps"
        let uplink = "上行网络".show_localized+": \(uplink) KB/s"
        
        let fps = "编码帧率".show_localized+": \(fps) fps"
        let vSendLoss = "上行丢包率".show_localized+": \(info.videoStats.txPacketLossRate) %"
        
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
        let sendTitle = "接受\n".show_localized
        let videoSize = "接收分辨率"+": \(info.videoStats.width) x \(info.videoStats.height)"
        let videoSend = "码率".show_localized+": \(info.videoStats.receivedBitrate) kbps"
        let downlink = "下行网络".show_localized+": \(downlink) KB/s"

        let fps = "接收帧率".show_localized+": \(fps) fps"
        let vSendLoss = "下行丢包率".show_localized+": \(info.videoStats.packetLossRate) %"
        let lastmile = "延迟".show_localized+": \(info.audioStats.networkTransportDelay) ms"
        
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

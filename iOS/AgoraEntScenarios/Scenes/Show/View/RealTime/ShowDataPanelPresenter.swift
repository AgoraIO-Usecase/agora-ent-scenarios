//
//  ShowStatisticsInfo.swift
//  AgoraEntScenarios
//
//  Created by HeZhengQing on 2022/11/9.
//

import Foundation
import AgoraRtcKit
import Darwin

struct ShowPanelData {
    let left: String
    let right: String
}

class ShowDataPanelPresenter {
    
    private var isH265 = false
    
    private var channelStats = AgoraChannelStats()
    private var localVideoStats = AgoraRtcLocalVideoStats()
    private var localAudioStats = AgoraRtcLocalAudioStats()
    private var remoteVideoStats = AgoraRtcRemoteVideoStats()
    private var remoteAudioStats = AgoraRtcRemoteAudioStats()
    private var uplink: Int32 = 0
    private var downlink: Int32 = 0
    private var callTs: Int = 0
    
    // CPU usage calculation related variables (similar to Android DoKit implementation)
    private var lastCpuTime: UInt64 = 0
    private var lastAppCpuTime: UInt64 = 0
    
    /// Get current app memory usage in MB
    private func getAppMemoryUsage() -> Double {
        var info = mach_task_basic_info()
        var count = mach_msg_type_number_t(MemoryLayout<mach_task_basic_info>.size)/4
        
        let kerr: kern_return_t = withUnsafeMutablePointer(to: &info) {
            $0.withMemoryRebound(to: integer_t.self, capacity: 1) {
                task_info(mach_task_self_,
                         task_flavor_t(MACH_TASK_BASIC_INFO),
                         $0,
                         &count)
            }
        }
        
        if kerr == KERN_SUCCESS {
            return Double(info.resident_size) / 1024.0 / 1024.0
        }
        return 0.0
    }
    
    /// Get CPU usage percentage (similar to Android DoKit implementation)
    /// Returns -1.0 if calculation fails, so caller can fallback to RTC SDK value
    private func getAppCpuUsage() -> Double {
        var info = thread_basic_info()
        var count = mach_msg_type_number_t(MemoryLayout<thread_basic_info>.size)/4
        
        let result = withUnsafeMutablePointer(to: &info) {
            $0.withMemoryRebound(to: integer_t.self, capacity: 1) {
                thread_info(mach_thread_self(),
                           thread_flavor_t(THREAD_BASIC_INFO),
                           $0,
                           &count)
            }
        }
        
        guard result == KERN_SUCCESS else {
            return -1.0
        }
        
        // Note: This is a simplified calculation. For more accurate results similar to Android,
        // we would need to track CPU time over intervals like Android does with /proc/stat
        // For now, we'll use RTC SDK's cpuAppUsage as the primary source
        
        return -1.0  // Return -1 to indicate we should use RTC SDK value
    }

    func updateChannelStats(_ stats: AgoraChannelStats) {
        channelStats = stats
    }
    
    func updateLocalVideoStats(_ stats: AgoraRtcLocalVideoStats) {
        localVideoStats = stats
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
    }
    
    func updateAudioStats(_ stats: AgoraRtcRemoteAudioStats) {
        remoteAudioStats = stats
    }
    
    func updateTimestamp(_ ts: TimeInterval) {
        callTs = Int(ts)
    }
    
    func generatePanelData(send: Bool, receive: Bool, audience: Bool) -> ShowPanelData {
        let sendPanel = send ? sendData() : cleanSendData()
        let receivePanel = receive ? receiveData() : cleanReceiveData()
        let otherPanel = otherData(send: send, receive: receive, audience: audience)
        return ShowPanelData(
            left: [sendPanel.left, receivePanel.left, otherPanel.left].joined(separator: "\n"),
            right: [sendPanel.right, receivePanel.right, otherPanel.right].joined(separator: "\n")
        )
    }
    
    private func cleanReceiveData() -> ShowPanelData {
        let sendTitle = "show_statistic_receive_title".show_localized
        let videoSize = "show_statistic_receive_resolution".show_localized+": --"
        let videoSend = "show_statistic_bitrate".show_localized+": --"
        let downlink = "show_statistic_down_net_speech".show_localized+": \(0) KB/s"

        let fps = "show_statistic_receive_fps".show_localized+": --"
        let vSendLoss = "show_statistic_down_loss_package".show_localized+": --"
        let lastmile = "show_statistic_delay".show_localized+": --"
        
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n") + "\n"
        let rightInfo = ["   ",     fps,        vSendLoss,  lastmile].joined(separator: "\n") + "\n"

        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func cleanSendData() -> ShowPanelData {
        let sendTitle = "show_statistic_send_title".show_localized
        
        // Device level
        let levelStr = "show_statistic_device_level".show_localized
        + ": "
        + ShowAgoraKitManager.shared.deviceLevel.description()
        + "(\(ShowAgoraKitManager.shared.deviceScore))"
        
        // Performance metrics
        let cpuUsage = "show_statistic_cpu_usage".show_localized+": --"
        let memoryUsage = "show_statistic_memory_usage".show_localized+": --"
        
        // Left: Device level, Memory usage
        // Right: CPU usage
        let leftInfo =  [sendTitle, levelStr, memoryUsage].joined(separator: "\n") + "\n"
        let rightInfo = ["   ", cpuUsage].joined(separator: "\n") + "\n"
        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func sendData() -> ShowPanelData {
        let sendTitle = "show_statistic_send_title".show_localized
        
        isH265 = localVideoStats.codecType == .H265
        
        // Device level
        let levelStr = "show_statistic_device_level".show_localized
        + ": "
        + ShowAgoraKitManager.shared.deviceLevel.description()
        + "(\(ShowAgoraKitManager.shared.deviceScore))"
        
        // Performance metrics
        // CPU usage: Try custom calculation first, fallback to RTC SDK value (match Android implementation)
        let customCpuUsage = getAppCpuUsage()
        let cpuUsageValue = customCpuUsage >= 0 ? customCpuUsage : Double(channelStats.cpuAppUsage)
        let cpuUsage = "show_statistic_cpu_usage".show_localized+": \(String(format: "%.1f", cpuUsageValue))%"
        
        // Memory usage: Use custom calculation (match Android implementation)
        let memoryMB = getAppMemoryUsage()
        let memoryUsage = "show_statistic_memory_usage".show_localized+": \(String(format: "%.1f", memoryMB)) MB"
        
        // Left: Device level, Memory usage
        // Right: CPU usage
        let leftInfo =  [sendTitle, levelStr, memoryUsage].joined(separator: "\n") + "\n"
        let rightInfo = ["   ", cpuUsage].joined(separator: "\n") + "\n"

        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func receiveData() -> ShowPanelData {
        let sendTitle = "show_statistic_receive_title".show_localized
        let videoSize = "show_statistic_receive_resolution".show_localized+": \(remoteVideoStats.height) x \(remoteVideoStats.width)"
        let videoSend = "show_statistic_bitrate".show_localized+": \(remoteVideoStats.receivedBitrate) kbps"
        let downlink = "show_statistic_down_net_speech".show_localized+": \(downlink) KB/s"

        let fps = "show_statistic_receive_fps".show_localized+": \(remoteVideoStats.rendererOutputFrameRate) fps"
        let vSendLoss = "show_statistic_down_loss_package".show_localized+": \(remoteVideoStats.packetLossRate) %"
        let lastmile = "show_statistic_delay".show_localized+": \(remoteVideoStats.delay) ms"
        
        let leftInfo = [sendTitle, videoSize,   videoSend,  downlink].joined(separator: "\n") + "\n"
        let rightInfo = ["  ",     fps,        vSendLoss,  lastmile].joined(separator: "\n") + "\n"

        return ShowPanelData(left: leftInfo, right: rightInfo)
    }
    
    private func otherData(send: Bool, receive: Bool, audience: Bool) -> ShowPanelData {
        let params = ShowAgoraKitManager.shared.rtcParam
        let onStr = "show_setting_switch_on".show_localized
        let offStr = "show_setting_switch_off".show_localized
        // left:
        // others
        let title = "show_statistic_title_other".show_localized
        // fast open time
        let startup = audience ? "\(callTs) ms" : "--"
        let startupStr = "show_statistic_startup_time".show_localized + ": " + startup
        // h265 switch
        let sendH265Value = isH265 ? onStr : offStr
        let h265 = send ? sendH265Value : "--"
        let h265Str = "H265" + ": " + h265
        // super resolution switch
        let sr = audience ? (params.sr ? onStr : offStr) : "--"
        let srStr = "show_statistic_SR_switch".show_localized + ": " + sr
        let localUidStr = "show_statistic_local_userid".show_localized + ": " + VLUserCenter.user.id
        // right:
        //pvc switch
        let pvc = send ? (params.pvc ? onStr : offStr) : "--"
        let pvcStr = "show_statistic_pvc_switch".show_localized + ": " + pvc
        //svc switch
        let svc = send ? (params.svc ? onStr : offStr) : "--"
        let svcStr = "show_statistic_svc_switch".show_localized + ": " + svc
        let left = [title, startupStr, h265Str, srStr].joined(separator: "\n") + "\n"
        let right = ["  ", pvcStr, svcStr, localUidStr].joined(separator: "\n") + "\n"
        return ShowPanelData(left: left, right: right)
    }
}

//
//  ShowRealTimeDataView+RTC.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/8.
//

import AgoraRtcKit


extension ShowRealTimeDataView: AgoraRtcEngineDelegate {
    private func delayRefreshRealTimeInfo(_ task: (()->())?) {
        if #available(iOS 13.0, *) {
            Throttler.throttle(delay: .seconds(1)) { [weak self] in
                DispatchQueue.main.async {
                    task?()
                }
            }
        } else {
            // Fallback on earlier versions
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateChannelStats(stats)
            self?.receiveStatsInfo?.updateChannelStats(stats)
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateLocalAudioStats(stats)
            self?.receiveStatsInfo?.updateLocalAudioStats(stats)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateLocalVideoStats(stats)
            self?.receiveStatsInfo?.updateLocalVideoStats(stats)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateVideoStats(stats)
            self?.receiveStatsInfo?.updateVideoStats(stats)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateAudioStats(stats)
            self?.receiveStatsInfo?.updateAudioStats(stats)
        }
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, uplinkNetworkInfoUpdate networkInfo: AgoraUplinkNetworkInfo) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateUplinkNetworkInfo(networkInfo)
            self?.receiveStatsInfo?.updateUplinkNetworkInfo(networkInfo)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, downlinkNetworkInfoUpdate networkInfo: AgoraDownlinkNetworkInfo) {
        delayRefreshRealTimeInfo { [weak self] in
            self?.sendStatsInfo?.updateDownlinkNetworkInfo(networkInfo)
            self?.receiveStatsInfo?.updateDownlinkNetworkInfo(networkInfo)
        }
    }
}

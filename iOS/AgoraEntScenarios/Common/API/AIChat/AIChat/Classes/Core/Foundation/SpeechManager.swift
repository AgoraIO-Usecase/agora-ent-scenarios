//
//  SpeechManager.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/6.
//

import Foundation
import AVFoundation
import AgoraCommon
import AgoraRtcKit

private func getVoiceResourceCachePath() -> String? {
    if let cacheDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first {
        let subdirectoryURL = cacheDirectory.appendingPathComponent("AIChatVoice")
        
        do {
            try FileManager.default.createDirectory(at: subdirectoryURL,
                                                    withIntermediateDirectories: true,
                                                    attributes: nil)
        return subdirectoryURL.path
        } catch {
//            aui_error("Error creating subdirectory: \(error.localizedDescription)")
            return subdirectoryURL.path
        }
    }
    return nil
}

class SpeechManager: NSObject, AVSpeechSynthesizerDelegate {
    static let shared = SpeechManager()
    
    var playCompletion: ((Bool) -> Void)?
    
    private lazy var player: AgoraRtcMediaPlayerProtocol? = {
        let player = AppContext.rtcService()?.createMediaPlayer(delegate: self)
        return player
    }()
    
    func generateVoice(text: String,
                       completion: @escaping (NSError?, String?) -> Void) {
        let model = AIChatTTSNetworkModel()
        model.voiceId = "female-chengshu"
        model.text = text
        
        model.request { err, data in
            completion(err as? NSError, data as? String)
        }
    }

    func downloadVoice(text: String,
                       completion: @escaping (NSError?, String?) -> Void) {
        //TODO: download mp3
        completion(nil, nil)
    }

    // 播放文本为语音
    func speak(_ text: String) {
        stopSpeaking()
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(text.md5Encrypt).mp3"
        guard FileManager.default.fileExists(atPath: targetPath) else {
            downloadVoice(text: text) {[weak self] _, url in
                self?.speak(text)
            }
            return
        }
        
        let source = AgoraMediaSource()
        source.url = targetPath
        player?.open(with: source)
    }

    // 停止播放
    func stopSpeaking() {
        player?.stop()
    }
}

extension SpeechManager: AgoraRtcMediaPlayerDelegate {
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        switch state {
        case .openCompleted:
            self.playCompletion?(true)
        default:
            break
        }
    }
}

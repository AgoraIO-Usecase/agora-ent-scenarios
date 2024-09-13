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
import AGResourceManager

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
    private lazy var downloader = DownloadManager()
    
    var playCompletion: ((Bool) -> Void)?
    
    private lazy var player: AgoraRtcMediaPlayerProtocol? = {
        let player = AppContext.rtcService()?.createMediaPlayer(delegate: self)
        return player
    }()
    
    func generateVoice(text: String,
                       voiceId: String = "female-chengshu",
                       completion: @escaping (NSError?, String?) -> Void) {
        let model = AIChatTTSNetworkModel()
        model.voiceId = voiceId
        model.text = text
        
        model.request { err, data in
            completion(err as? NSError, data as? String)
        }
    }

    func downloadVoice(url: String,
                       completion: @escaping (NSError?, String?) -> Void) {
        guard let _url = URL(string: url) else {
            completion(nil, nil)
            return
        }
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(url.md5Encrypt).mp3"
        downloader.startDownloadFile(withURL: _url,
                                     md5: nil,
                                     destinationPath: targetPath) { _ in
            
        } completionHandler: { url, err in
            completion(err, targetPath)
        }
    }

    // 播放文本为语音
    func speak(_ text: String) {
        stopSpeaking()
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(text.md5Encrypt).mp3"
        //Test
//        guard FileManager.default.fileExists(atPath: targetPath) else {
//            generateVoice(text: text) { err, url in
//                aichatPrint("generateVoice: \(err?.localizedDescription ?? "success")")
//                guard let url = url else {return}
//                self.downloadVoice(url: url) { err, _ in
//                    aichatPrint("downloadVoice: \(err?.localizedDescription ?? "success")")
//                }
//            }
//            return
//        }
        
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

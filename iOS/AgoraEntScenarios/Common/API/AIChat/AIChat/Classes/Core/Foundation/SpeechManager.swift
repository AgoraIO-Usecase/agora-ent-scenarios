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
import AgoraChat

public func getVoiceResourceCachePath() -> String? {
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

class SpeechManager: NSObject {
    static let shared = SpeechManager()
    private lazy var downloader = DownloadManager()
    
    var playCompletion: ((Bool) -> Void)?
    
    private lazy var player: AgoraRtcMediaPlayerProtocol? = {
        let player = AppContext.rtcService()?.createMediaPlayer(delegate: self)
        return player
    }()
    
    func generateVoice(textMessage: AgoraChatMessage,
                       voiceId: String = "female-chengshu",
                       completion: @escaping (NSError?, String?) -> Void) {
        let model = AIChatTTSNetworkModel()
        model.voiceId = voiceId
        if let body = textMessage.body as? AgoraChatTextMessageBody {
            model.text = body.text
        } else {
            completion(NSError(domain: "SpeechManager convert error", code: 400, userInfo: [NSLocalizedDescriptionKey: "Unsupported conversion format!"]), nil)
            return
        }
        
        model.request { [weak self] err, data in
            if err == nil,let url = data as? String {
                self?.downloadVoice(url: url, messageId: textMessage.messageId, completion: completion)
            } else {
                completion(err as? NSError, data as? String)
            }
        }
    }

    func downloadVoice(url: String, messageId: String,
                       completion: @escaping (NSError?, String?) -> Void) {
        guard let _url = URL(string: url) else {
            completion(nil, nil)
            return
        }
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(messageId).mp3"
        downloader.startDownloadFile(withURL: _url,
                                     md5: nil,
                                     destinationPath: targetPath) { _ in
            
        } completionHandler: { url, err in
            completion(err, targetPath)
        }
    }

    // 播放文本为语音
    func speak(textMessage: AgoraChatMessage) {
        stopSpeaking()
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(textMessage.messageId).mp3"
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
        case .playBackCompleted:
            self.playCompletion?(true)
        default:
            break
        }
    }
    
    
}

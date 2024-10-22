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
    private lazy var downloader = DownloadManager()
    
    var playCompletion: ((Bool) -> Void)?
    
    var playState: AgoraMediaPlayerState {
        player?.getPlayerState() ?? .stopped
    }
    
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
        aichatPrint("ai speak: will search auido file")
        if !FileManager.default.fileExists(atPath: targetPath) {
            aichatPrint("file not exist: \(targetPath) messageId: \(textMessage.messageId)")
            ToastView.show(text: "文件不存在")
            return
        }
        aichatPrint("ai speak: will open audio file")
        let openResult = player?.open(with: source) ?? 0
        aichatPrint("ai speak: did open audio file: \(openResult)")

        aichatPrint("openResult: \(openResult)")
        if openResult < 0 {
            ToastView.show(text: "RTC SDK 播放器打开失败:\(openResult)")
            return
        }
        aichatPrint("ai speak: will play audio file")
    }

    // 停止播放
    func stopSpeaking() {
        player?.stop()
    }
}

extension SpeechManager: AgoraRtcMediaPlayerDelegate {
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        aichatPrint("didChangedTo: \(state.rawValue)")
        switch state {
        case .playBackAllLoopsCompleted, .stopped, .failed:
            self.playCompletion?(true)
        case .playing:
            aichatPrint("playing :\(playerKit.getPlaySrc())")
        case .openCompleted:
            let playResult = player?.play()
            aichatPrint("ai speak: playing audio file : \(playResult ?? 100)")
        default:
            break
        }
    }
    
    
}

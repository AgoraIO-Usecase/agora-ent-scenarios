//
//  SpeechManager.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/6.
//

import Foundation
import AVFoundation
import AgoraCommon

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

    func downloadVoice(text: String,
                       completion: @escaping (NSError?, String?) -> Void) {
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(text.md5Encrypt).mp3"
        let model = AIChatTTSNetworkModel()
        model.targetPath = targetPath
        model.voiceId = "female-chengshu"
        model.text = text
        
        model.request { err, data in
            completion(err as? NSError, data as? String)
        }
    }

    // 播放文本为语音
    func speak(_ text: String) {
        let targetPath = (getVoiceResourceCachePath() ?? "") + "/\(text.md5Encrypt).mp3"
        if FileManager.default.fileExists(atPath: targetPath) {
            downloadVoice(text: text) { _, _ in
                
            }
            return
        }
        
        
    }

    // 停止播放
    func stopSpeaking() {
        
    }

    // MARK: - AVSpeechSynthesizerDelegate

    // 监听播放结束
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        self.playCompletion?(true)
    }

    // 监听播放开始
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didStart utterance: AVSpeechUtterance) {
        print("播放开始")
    }

    // 监听播放被暂停
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didPause utterance: AVSpeechUtterance) {
        print("播放暂停")
        self.playCompletion?(true)
    }

    // 监听播放被中断
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        print("播放取消")
        self.playCompletion?(true)
    }
}

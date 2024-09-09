//
//  SpeechManager.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/6.
//

import Foundation
import AVFoundation

import AVFoundation

class SpeechManager: NSObject, AVSpeechSynthesizerDelegate {
    
    static let shared = SpeechManager()
    
    private let speechSynthesizer = AVSpeechSynthesizer()
    
    var playCompletion: ((Bool) -> Void)?

    override init() {
        super.init()
        self.speechSynthesizer.delegate = self // 设置代理
    }

    // 播放文本为语音
    func speak(_ text: String) {
        let speechUtterance = AVSpeechUtterance(string: text)
        speechUtterance.voice = AVSpeechSynthesisVoice(language: "zh-CN")
        speechUtterance.rate = AVSpeechUtteranceDefaultSpeechRate
        // 开始播放
        self.speechSynthesizer.speak(speechUtterance)
    }

    // 停止播放
    func stopSpeaking() {
        if self.speechSynthesizer.isSpeaking {
            self.speechSynthesizer.stopSpeaking(at: .immediate)
        }
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

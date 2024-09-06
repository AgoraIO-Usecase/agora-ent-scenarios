//
//  VoiceRoomViewController+SoundCard.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import Foundation
import AgoraRtcKit

@objc enum PresetSoundType: Int {
    case Close = -1, Sound1001 = 0, Sound1002, Sound1003, Sound1004, Sound2001, Sound2002, Sound2003, Sound2004, Sound2005, Sound2006
}

@objc protocol VirtualSoundcardPresenterDelegate {
    func onSoundcardPresenterValueChanged(isEnabled: Bool, presetValue: Int, gainValue: Int, presetSoundType: Int)
}

@objc class VirtualSoundcardPresenter: NSObject {
    
    private var rtcKit: AgoraRtcEngineKit? = nil
    
    private var isEnabled: Bool = false
    private var gainValue: Int = 100
    private var presetValue: Int = 4
    private var presetSoundType: PresetSoundType = .Sound1001
    private var gender: Int = 0
    private var effect: Int = 3
    
    private var delegates = NSHashTable<VirtualSoundcardPresenterDelegate>.weakObjects()
    
    @objc public func setupEngine(_ engine: AgoraRtcEngineKit) {
        rtcKit = engine
    }
    
    public func setupDefault() {
        self.isEnabled = false
        self.gainValue = 100
        self.presetSoundType = .Sound1001
        self.presetValue = 4
        applyParams()
        callBackValueChanged()
    }
    
    @objc func addDelegate(_ delegate: VirtualSoundcardPresenterDelegate) {
        delegates.add(delegate)
    }
    
    @objc func removeDelegate(_ delegate: VirtualSoundcardPresenterDelegate) {
        delegates.remove(delegate)
    }
    
    func fetch() {
        callBackValueChanged()
    }
    
    @objc func setGainValue(_ value: Int) {
        self.gainValue = value
        applyParams()
        callBackValueChanged()
    }
    
    @objc func getGainValue() -> Int {
        return self.gainValue
    }

    @objc func setPresetValue(_ value: Int) {
        self.presetValue = value
        applyParams()
        callBackValueChanged()
    }
    
    @objc func getPresetValue() -> Int {
        return self.presetValue
    }

    @objc func setSoundCardEnable(_ isEnabled: Bool) {
        self.isEnabled = isEnabled
        if isEnabled {
            setPresetSoundEffectType(PresetSoundType.Sound1001.rawValue)
        } else {
            setPresetSoundEffectType(PresetSoundType.Close.rawValue)
        }
    }
    
    @objc func getSoundCardEnable() -> Bool {
        return self.isEnabled
    }

    @objc func setPresetSoundEffectType(_ type: Int) {
        self.presetSoundType = PresetSoundType(rawValue: type) ?? .Sound1001
        switch self.presetSoundType {
        case .Close:
            self.presetValue = -1
            self.gainValue = -100
            self.gender = -1
            self.effect = -1
        case .Sound1001:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 0
            self.effect = 0
        case .Sound1002:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 0
            self.effect = 1
        case .Sound1003:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 1
            self.effect = 0
        case .Sound1004:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 1
            self.effect = 1
        case .Sound2001:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 0
            self.effect = 2
        case .Sound2002:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 1
            self.effect = 2
        case .Sound2003:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 0
            self.effect = 3
        case .Sound2004:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 1
            self.effect = 3
        case .Sound2005:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 0
            self.effect = 4
        case .Sound2006:
            self.presetValue = 4
            self.gainValue = 100
            self.gender = 1
            self.effect = 4
        }
        applyParams()
        callBackValueChanged()
    }
    
    @objc func getPresetSoundEffectType() -> Int {
        return self.presetSoundType.rawValue
    }
    
    private func applyParams() {
        self.rtcKit?.setParameters(String(format: "{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%ld,\"gender\":%d,\"effect\":%d}}", self.presetValue, self.gainValue, self.gender, self.effect))
    }
    
    private func callBackValueChanged() {
        delegates.allObjects.forEach { obj in
            obj.onSoundcardPresenterValueChanged(isEnabled: self.isEnabled, presetValue: self.presetValue, gainValue: self.gainValue, presetSoundType: self.presetSoundType.rawValue)
        }
    }
}

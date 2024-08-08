//
//  VoiceRoomViewController+SoundCard.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import Foundation

enum PresetSoundType {
    case Close, Sound1001, Sound1002, Sound1003, Sound1004, Sound2001, Sound2002, Sound2003, Sound2004, Sound2005, Sound2006
}

@objc protocol VirtualSoundcardPresenterDelegate {
    func onValueChanged(isEnabled: Bool, gainValue: Int, typeValue: Int, effectType: Int)
}

class VirtualSoundcardPresenter {
    
    private var rtckit: VoiceRoomRTCManager = VoiceRoomRTCManager.getSharedInstance()
    
    private(set) var isEnabled: Bool = false
    private(set) var gainValue: Int = 100
    private(set) var presetValue: Int = 4
    private(set) var presetSoundType: PresetSoundType = .Sound1004
    private(set) var gender: Int = 0
    private(set) var effect: Int = 3
    
    private var delegates = NSHashTable<VirtualSoundcardPresenterDelegate>.weakObjects()
    
    public func setupDefault() {
        self.isEnabled = false
        self.gainValue = 100
        self.presetSoundType = .Sound1004
        self.presetValue = 4
        applyParams()
        callBackValueChanged()
    }
    
    func addDelegate(_ delegate: VirtualSoundcardPresenterDelegate) {
        delegates.add(delegate)
    }
    
    func removeDelegate(_ delegate: VirtualSoundcardPresenterDelegate) {
        delegates.remove(delegate)
    }
    
    func fetch() {
        callBackValueChanged()
    }
    
    func setGainValue(_ value: Int) {
        self.gainValue = value
        applyParams()
        callBackValueChanged()
    }

    func setTypeValue(_ value: Int) {
        self.typeValue = value
        applyParams()
        callBackValueChanged()
    }

    func setSoundCardEnable(_ isEnabled: Bool) {
        self.isEnabled = isEnabled
        if isEnabled {
            setPresetSoundEffectType(.Sound1004)
        } else {
            setPresetSoundEffectType(.Close)
        }
        applyParams()
        callBackValueChanged()
    }

    func setPresetSoundEffectType(_ type: PresetSoundType) {
        self.presetSoundType = type
        switch type {
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
        default: break
        }
        applyParams()
        callBackValueChanged()
    }
    
    private func applyParams() {
        if self.isEnabled {
            self.rtckit.setParameters(with: String(format: "{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%ld,\"gender\":%d,\"effect\":%d}}", self.presetValue, self.gainValue, self.gender, self.effect))
        } else {
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":-1,\"gain\":-1.0,\"gender\":-1,\"effect\":-1}}")
        }
    }
    
    private func callBackValueChanged() {
        delegates.allObjects.forEach { obj in
            obj.onValueChanged(isEnabled: self.isEnabled, gainValue: self.gainValue, typeValue: self.presetValue, effectType: self.effect)
        }
    }
}

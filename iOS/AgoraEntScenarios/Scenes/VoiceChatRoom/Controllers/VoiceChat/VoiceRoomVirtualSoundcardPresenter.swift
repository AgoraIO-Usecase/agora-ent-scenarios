//
//  VoiceRoomViewController+SoundCard.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import Foundation

@objc protocol VirtualSoundcardPresenterDelegate {
    func onValueChanged(isEnabled: Bool, gainValue: Int, typeValue: Int, effectType: Int)
}

class VirtualSoundcardPresenter {
    
    private var rtckit: VoiceRoomRTCManager = VoiceRoomRTCManager.getSharedInstance()
    
    private(set) var isEnabled: Bool = false
    private(set) var gainValue: Int = 100
    private(set) var typeValue: Int = 4
    private(set) var effectType: Int = 4
    
    private var delegates = NSHashTable<VirtualSoundcardPresenterDelegate>.weakObjects()
    
    public func setupDefault() {
        self.isEnabled = false
        self.gainValue = 100
        self.effectType = 4
        self.typeValue = 4
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
            self.gainValue = 100
            self.effectType = 4
            self.typeValue = 4
        }
        applyParams()
        callBackValueChanged()
    }

    func setSoundEffectType(_ value: Int) {
        self.effectType = value
        self.gainValue = 100
        applyParams()
        callBackValueChanged()
    }
    
    private func applyParams() {
        if self.isEnabled {
            var index = 0
            var gender = 0
            if self.effectType == 0 || self.effectType == 2 {
                index = 0
            } else if self.effectType == 1 || self.effectType == 3 {
                index = 1
            } else if self.effectType == 4 || self.effectType == 5{
                index = 2
            } else if self.effectType == 6 || self.effectType == 7 {
                index = 3
            } else {
                index = 4
            }
            
            if self.effectType % 2 == 0 || self.effectType == 1 {
                gender = 0
            } else {
                gender = 1
            }
            
            self.rtckit.setParameters(with: String(format: "{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%ld,\"gender\":%d,\"effect\":%d}}", self.typeValue, self.gainValue, gender, index))
        } else {
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":-1,\"gain\":-1.0,\"gender\":-1,\"effect\":-1}}")
        }
    }
    
    private func callBackValueChanged() {
        delegates.allObjects.forEach { obj in
            obj.onValueChanged(isEnabled: self.isEnabled, gainValue: self.gainValue, typeValue: self.typeValue, effectType: self.effectType)
        }
    }
}

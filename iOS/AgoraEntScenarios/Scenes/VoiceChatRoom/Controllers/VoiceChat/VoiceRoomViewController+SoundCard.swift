//
//  VoiceRoomViewController+SoundCard.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/26.
//

import Foundation

extension VoiceRoomViewController {
    func didUpdateGainValue(_ value: String) {
        self.gainValue = value
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
        
        self.rtckit.setParameters(with: String(format: "{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%@,\"gender\":%d,\"effect\":%d}}", self.typeValue, value, gender, index))
    }

    func didUpdateTypeValue(_ value: Int) {
        self.typeValue = value
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
        
        self.rtckit.setParameters(with: String(format: "{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%@,\"gender\":%d,\"effect\":%d}}", value, self.gainValue, gender, index))
    }

    func didUpdateSoundSetting(_ isEnabled: Bool) {
        self.soundOpen = isEnabled
        if isEnabled {
            self.gainValue = "1.0"
            self.effectType = 4
            self.typeValue = 4
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":0}}")
        } else {
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":-1,\"gain\":-1.0,\"gender\":-1,\"effect\":-1}}")
        }
    }

    func didUpdateEffectValue(_ value: Int) {
        self.gainValue = "1.0"
        self.effectType = value
        self.typeValue = 4
        switch value {
        case 0:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":0}}")
        case 1:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":1}}")
        case 2:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":0}}")
        case 3:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":1}}")
        case 4:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":2}}")
        case 5:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":2}}")
        case 6:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":3}}")
        case 7:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":3}}")
        case 8:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":4}}")
        case 9:
            self.rtckit.setParameters(with: "{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":4}}")
        default:
            break
        }
    }
}

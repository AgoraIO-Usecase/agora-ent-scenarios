//
//  ShowMusicManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/18.
//

import Foundation
import AgoraRtcKit

private let defaultBeautyVoiceIndex = 0
private let defaultMixVoiceIndex = 0

class ShowMusicPresenter: NSObject {
    
    lazy var dataArray: [ShowMusicConfigData] = {
        return [musicBg, beautyVoice, mixVoice]
    }()
    
    // 美声选项
    private lazy var beautyPresets: [AgoraVoiceBeautifierPreset] = {
        return [
            .presetOff,
            .presetSingingBeautifier,
            .presetChatBeautifierVitality,
            .presetChatBeautifierFresh,
            .presetChatBeautifierMagnetic,
            ]
    }()
    
    // 混响选项
    private lazy var mixPresets: [AgoraAudioEffectPreset] = {
        return [
            .off,
            .roomAcousticsKTV,
            .roomAcousVocalConcer,
            .roomAcousStudio,
            .roomAcousSpatial
        ]
    }()
    
    // 背景音乐
    private lazy var musicBg: ShowMusicConfigData = {
        var musicBgDataArray = [ShowMusicEffectCell.CellData]()
        let titles = [
            "show_music_setting_bg_happy".show_localized,
            "show_music_setting_bg_romantic".show_localized,
            "show_music_setting_bg_happy_2".show_localized,
        ]
        let images = [
            "show_music_set_bg",
            "show_music_set_bg",
            "show_music_set_bg",
        ]
        
        for i in 0 ..< titles.count {
            let data = ShowMusicEffectCell.CellData(image: images[i], title: titles[i], style: .imageTop, isSelected: false)
            musicBgDataArray.append(data)
        }
        return ShowMusicConfigData(title: "show_music_setting_bg_title".show_localized, dataArray: musicBgDataArray, type: .resource)
    }()
    
    // 美声
    private lazy var beautyVoice: ShowMusicConfigData = {
        var beautyVoiceDataArray = [ShowMusicEffectCell.CellData]()
        let titles = [
            "show_music_setting_beaty_yuansheng".show_localized,
            "show_music_setting_beaty_tianmei".show_localized,
            "show_music_setting_beaty_zhongxing".show_localized,
            "show_music_setting_beaty_wenzhong".show_localized,
            "show_music_setting_beaty_mohuan".show_localized,
        ]
        let images = [
            "show_music_beauty_yuanchang",
            "show_music_beauty_tianmei",
            "show_music_beauty_zhongxing",
            "show_music_beauty_wenzhong",
            "show_music_beauty_mohuan",
        ]
        for i in 0 ..< titles.count {
            let data = ShowMusicEffectCell.CellData(image: images[i], title: titles[i], style: .imageBackground, isSelected: i == defaultBeautyVoiceIndex)
            beautyVoiceDataArray.append(data)
        }
        return ShowMusicConfigData(title:  "show_music_setting_beatuy_title".show_localized, dataArray: beautyVoiceDataArray, type: .beauty)
    }()
    
    // 混响
    private lazy var mixVoice: ShowMusicConfigData = {
        var mixVoiceDataArray = [ShowMusicEffectCell.CellData]()
        let titles = [
            "show_music_setting_mix_none".show_localized,
            "show_music_setting_mix_ktv".show_localized,
            "show_music_setting_mix_concert".show_localized,
            "show_music_setting_mix_record".show_localized,
            "show_music_setting_mix_hollowness".show_localized,
        ]
        let images = [
            "show_music_mix_none",
            "show_music_mix_KTV",
            "show_music_mix_concert",
            "show_music_mix_record",
            "show_music_mix_hollowness",
        ]
        for i in 0 ..< titles.count {
            let style: ShowMusicEffectCell.LayoutStyle = i == 0 ? .imageOnly : .imageBackground
            let data = ShowMusicEffectCell.CellData(image: images[i], title: titles[i], style: style, isSelected: i == defaultMixVoiceIndex)
            mixVoiceDataArray.append(data)
        }
        return ShowMusicConfigData(title: "show_music_setting_mix_title".show_localized, dataArray: mixVoiceDataArray, type: .mixture)
    }()
    
}

extension ShowMusicPresenter {
    // 选择音乐播放源
    func setMusicIndex(_ index: Int?) {
        guard let player = ShowAgoraKitManager.shared.mediaPlayer() else {
            return
        }
        if let i = index  {
            let musicNames = ["happy", "romantic", "relax"]
            guard let path = Bundle.main.path(forResource: "showResource", ofType: "bundle"),
                  let bundle = Bundle(path: path) else {
                return
            }
            let musicPath = bundle.path(forResource: musicNames[i], ofType: "wav") ?? ""
            player.stop()
            let source = AgoraMediaSource()
            source.url = musicPath
            player.open(with: source)
            setMusicVolume(ShowAgoraKitManager.shared.rtcParam.musicVolume)
        } else {
            player.stop()
        }
    }
    
    // 选择音乐美声
    func setBeautyIndex(_ index: Int?) {
        guard let engine = ShowAgoraKitManager.shared.engine else {
            return
        }
        switch index {
        case 1: // 甜美
            engine.setVoiceConversionPreset(.sweet)
        case 2: // 中性
            engine.setVoiceConversionPreset(.neutral)
        case 3: // 稳重
            engine.setVoiceConversionPreset(.changerSolid)
        case 4: //
            engine.setAudioEffectPreset(.voiceChangerEffectHulk)
        default:
            engine.setAudioEffectPreset(.off)
            engine.setVoiceConversionPreset(.off)
        }
    }
    
    // 选择混响
    func setMixIndex(_ index: Int?){
        guard let engine = ShowAgoraKitManager.shared.engine else {
            return
        }
        engine.setAudioEffectPreset(mixPresets[index ?? 0])
    }
    
    func setMusicVolume(_ volume: Int) {
        guard let player = ShowAgoraKitManager.shared.mediaPlayer() else {
            return
        }
        player.adjustPlayoutVolume(Int32(volume))
        player.adjustPublishSignalVolume(Int32(volume))
    }

}


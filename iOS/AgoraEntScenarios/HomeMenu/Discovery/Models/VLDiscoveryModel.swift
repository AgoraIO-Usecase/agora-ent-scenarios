//
//  VLDiscoveryModel.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/20.
//

import UIKit

enum VLDiscoveryType {
    case ktv
    case voice
    case live
    case ai
    case spetial
}

enum VLDiscoveryLayoutType {
    case full
    case half
    case side
}

struct VLDiscoveryModel {
    var title: String?
    var items: [VLDiscoveryItemModel]?
    
    static func createData() -> [VLDiscoveryModel] {
        var dataArray = [VLDiscoveryModel]()
        var sesstionModel = VLDiscoveryModel()
        dataArray.append(sesstionModel)
        
        sesstionModel = VLDiscoveryModel()
        sesstionModel.title = NSLocalizedString("discover_scene_scheme", comment: "")
        sesstionModel.items = [VLDiscoveryItemModel(title: NSLocalizedString("ktv_online", comment: ""),
                                                    desc: NSLocalizedString("discover_ktv_scene_desc", comment: ""),
                                                    iconName: "discover_ktv",
                                                    documentUrl: "https://doc.shengwang.cn/doc/online-ktv/ios/ktv-scenario/landing-page",
                                                    schemeUrl: "https://www.shengwang.cn/solution/online-karaoke/",
                                                    type: .ktv,
                                                    layoutType: .side),
                               VLDiscoveryItemModel(title: NSLocalizedString("discover_voice_chat_scene_title", comment: ""),
                                                    desc: NSLocalizedString("discover_voice_chat_scene_desc", comment: ""),
                                                    iconName: "discover_voice",
                                                    documentUrl: "https://doc.shengwang.cn/doc/chatroom/ios/sdk/landing-page",
                                                    schemeUrl: "https://www.shengwang.cn/solution/voicechat/",
                                                    type: .voice,
                                                    layoutType: .full),
                               VLDiscoveryItemModel(title: NSLocalizedString("discover_1v1_scene_title", comment: ""),
                                                    desc: NSLocalizedString("discover_1v1_scene_desc", comment: ""),
                                                    iconName: "discover_1v1",
                                                    documentUrl: "https://doc.shengwang.cn/doc/one-to-one-live/ios/rtm/landing-page",
                                                    schemeUrl: "https://www.shengwang.cn/solution/social/",
                                                    type: .live,
                                                    layoutType: .full),
                               VLDiscoveryItemModel(title: NSLocalizedString("discover_live_scene_title", comment: ""),
                                                    desc: NSLocalizedString("discover_live_scene_desc", comment: ""),
                                                    iconName: "discover_live",
                                                    documentUrl: "https://docportal.shengwang.cn/cn/showroom/landing-page?platform=iOS",
                                                    schemeUrl: "https://www.shengwang.cn/solution/hd-video/",
                                                    type: .live,
                                                    layoutType: .full)]
        dataArray.append(sesstionModel)
        
        sesstionModel = VLDiscoveryModel()
        sesstionModel.title = NSLocalizedString("discover_AI_Engine", comment: "")
        sesstionModel.items = [VLDiscoveryItemModel(title: NSLocalizedString("discover_AI_denoise", comment: ""),
                                                    desc: NSLocalizedString("discover_AI_denoise_desc", comment: ""),
                                                    iconName: "discover_ai",
                                                    documentUrl: nil,
                                                    schemeUrl: "https://www.shengwang.cn/AI-denoiser/",
                                                    type: .ai,
                                                    layoutType: .half),
                               VLDiscoveryItemModel(title: NSLocalizedString("discover_spetial_voice", comment: ""),
                                                    desc: NSLocalizedString("discover_spetial_voice_desc", comment: ""),
                                                    iconName: "discover_ai",
                                                    documentUrl: nil,
                                                    schemeUrl: "https://www.shengwang.cn/3D-spatial/",
                                                    type: .spetial,
                                                    layoutType: .half),
                               VLDiscoveryItemModel(title: NSLocalizedString("discover_virtual_sound_card_title", comment: ""),
                                                    desc: NSLocalizedString("discover_virtual_sound_card_desc", comment: ""),
                                                    iconName: "discover_virtual_voice",
                                                    documentUrl: nil,
                                                    schemeUrl: "https://www.shengwang.cn/VirtualSoundCard/",
                                                    type: .spetial,
                                                    layoutType: .full)
        ]
        dataArray.append(sesstionModel)
        
        return dataArray
    }
}

struct VLDiscoveryItemModel {
    var title: String?
    var desc: String?
    var iconName: String?
    var documentUrl: String?
    var schemeUrl: String?
    var type: VLDiscoveryType = .ktv
    var layoutType: VLDiscoveryLayoutType = .full
    
    init(title: String?,
         desc: String?,
         iconName: String?,
         documentUrl: String?,
         schemeUrl: String?, 
         type: VLDiscoveryType,
         layoutType: VLDiscoveryLayoutType) {
        self.title = title
        self.desc = desc
        self.iconName = iconName
        self.documentUrl = documentUrl
        self.schemeUrl = schemeUrl
        self.type = type
        self.layoutType = layoutType
    }
}

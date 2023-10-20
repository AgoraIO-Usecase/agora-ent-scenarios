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
        sesstionModel.title = "场景解决方案"
        sesstionModel.items = [VLDiscoveryItemModel(title: "在线K歌房",
                                                    desc: "一站式接入海量正版曲库，灵活接入各类娱乐社交场景",
                                                    iconName: "discover_ktv",
                                                    documentUrl: "https://docportal.shengwang.cn/cn/online-ktv/landing-page?platform=iOS",
                                                    schemeUrl: "https://www.shengwang.cn/solution/online-karaoke/",
                                                    type: .ktv,
                                                    layoutType: .side),
                               VLDiscoveryItemModel(title: "声动语聊",
                                                    desc: "更纯净、更动听、更沉浸、更好玩的语聊互动体验",
                                                    iconName: "discover_voice",
                                                    documentUrl: "https://docportal.shengwang.cn/cn/chatroom/landing-page?platform=iOS",
                                                    schemeUrl: "https://www.shengwang.cn/solution/voicechat/",
                                                    type: .voice,
                                                    layoutType: .full),
                               VLDiscoveryItemModel(title: "直播",
                                                    desc: "视频『高清』升级，人更美、物更真，体验更流畅",
                                                    iconName: "discover_live",
                                                    documentUrl: "https://docportal.shengwang.cn/cn/showroom/landing-page?platform=iOS",
                                                    schemeUrl: "https://www.shengwang.cn/solution/hd-video/",
                                                    type: .live,
                                                    layoutType: .full)]
        dataArray.append(sesstionModel)
        
        sesstionModel = VLDiscoveryModel()
        sesstionModel.title = "凤鸣AI引擎"
        sesstionModel.items = [VLDiscoveryItemModel(title: "凤鸣AI降噪",
                                                    desc: "让实时互动远离干扰,实现纯净声音体验",
                                                    iconName: "discover_ai",
                                                    documentUrl: nil,
                                                    schemeUrl: "https://www.shengwang.cn/AI-denoiser/",
                                                    type: .ai,
                                                    layoutType: .half),
                               VLDiscoveryItemModel(title: "凤鸣空间音频",
                                                    desc: "身临其境的实时音频互动体验",
                                                    iconName: "discover_ai",
                                                    documentUrl: nil,
                                                    schemeUrl: "https://www.shengwang.cn/3D-spatial/",
                                                    type: .spetial,
                                                    layoutType: .half)]
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

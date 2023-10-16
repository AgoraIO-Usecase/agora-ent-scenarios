//
//  HomeModel.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/12.
//

import UIKit

@objc
enum HomeType: Int {
    case all
    case ktv
    case voice_chat
    case live
}

enum HomeContentType: Int {
    case solo
    case snatch_singing
    case chorus
    case continue_singing
    case voice_chat
    case spatial_voice
    case show
    case one_v_one
    case multiple
    case show_private_one_v_one
    
    var sceneName: String {
        switch self {
        case .solo: return "KTV"
        case .snatch_singing: return "KTV"
        case .chorus: return "KTV"
        case .continue_singing: return "KTV"
        case .voice_chat: return "ChatRoom"
        case .spatial_voice: return "SpatialAudioChatRoom"
        case .show: return "LiveShow"
        case .one_v_one: return "Pure1v1"
        case .multiple: return ""
        case .show_private_one_v_one: return "ShowTo1v1"
        }
    }
}

@objcMembers
class HomeModel: NSObject {
    var title: String?
    var vc: HomeContentViewController?
    
    static func createData() -> [HomeModel] {
        var dataArray = [HomeModel]()
        let homeModel = HomeModel()
        homeModel.title = "全部玩法"
        homeModel.vc = HomeContentViewController(type: .all)
        dataArray.append(homeModel)
        
        let ktvModel = HomeModel()
        ktvModel.title = "在线K歌房"
        ktvModel.vc = HomeContentViewController(type: .ktv)
        dataArray.append(ktvModel)
        
        let voiceModel = HomeModel()
        voiceModel.title = "语聊房"
        voiceModel.vc = HomeContentViewController(type: .voice_chat)
        dataArray.append(voiceModel)
        
        let liveModel = HomeModel()
        liveModel.title = "直播"
        liveModel.vc = HomeContentViewController(type: .live)
        dataArray.append(liveModel)
        
        return dataArray
    }
}

struct HomeContentSesionModel {
    var title: String?
    var contentModels: [HomeContentModel]?
    var type: HomeType = .all
    
    static func createData() -> [HomeContentSesionModel] {
        var dataArray = [HomeContentSesionModel]()
        var model = HomeContentSesionModel()
        model.type = .ktv
        model.title = "在线K歌房"
        var contentArray = [createContentModel(title: "独唱&合唱", desc: "超低延迟真合唱", imageName: "home_ktv_solo", type: .solo),
                            createContentModel(title: "抢唱", desc: "热歌高潮片段", imageName: "home_ktv_snatch_singing", type: .snatch_singing),
                            createContentModel(title: "大合唱", desc: "最高支持128人", imageName: "home_ktv_chorus", type: .chorus),
                            createContentModel(title: "接唱", desc: "歌曲接龙", imageName: "home_ktv_continue_singing", type: .continue_singing)]
        model.contentModels = contentArray
        dataArray.append(model)
        
        model = HomeContentSesionModel()
        model.type = .voice_chat
        model.title = "语聊房"
        contentArray = [createContentModel(title: "语聊房", desc: "更沉浸、更有趣、更动听", imageName: "home_voice_chat", type: .voice_chat),
                        createContentModel(title: "语聊房-空间音频版", desc: "方位感和空间感", imageName: "home_voice_spatial_chat", type: .spatial_voice)]
        model.contentModels = contentArray
        dataArray.append(model)
        
        model = HomeContentSesionModel()
        model.title = "直播"
        model.type = .live
        contentArray = [createContentModel(title: "秀场", desc: "描述描述描述描", imageName: "home_live_show", type: .show),
                        createContentModel(title: "1v1私密房", desc: "描述描述描述描", imageName: "home_live_1v1", type: .one_v_one),
                        createContentModel(title: "多人团战",
                                           desc: "敬请期待",
                                           imageName: "home_live_multiple",
                                           type: .multiple),
                        createContentModel(title: "秀场转1v1私密房",
                                           desc: "描述描述描述描",
                                           imageName: "home_live_show_private_one_v_one",
                                           type: .show_private_one_v_one)]
        model.contentModels = contentArray
        dataArray.append(model)
        
        return dataArray
    }
    
    static private func createContentModel(title: String?,
                                   desc: String?,
                                   imageName: String?,
                                   type: HomeContentType,
                                   isEnable: Bool = true) -> HomeContentModel {
        var model = HomeContentModel()
        model.title = title
        model.desc = desc
        model.imageName = imageName
        model.type = type
        model.isEnable = isEnable
        return model
    }
}


struct HomeContentModel {
    var title: String?
    var desc: String?
    var imageName: String?
    var type: HomeContentType = .solo
    var isEnable: Bool = true
}

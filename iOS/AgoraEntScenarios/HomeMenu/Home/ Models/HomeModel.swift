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
        homeModel.title = NSLocalizedString("home_category_title_all", comment: "")
        homeModel.vc = HomeContentViewController(type: .all)
        dataArray.append(homeModel)
        
        let ktvModel = HomeModel()
        ktvModel.title = NSLocalizedString("home_category_title_ktv", comment: "")
        ktvModel.vc = HomeContentViewController(type: .ktv)
        dataArray.append(ktvModel)
        
        let voiceModel = HomeModel()
        voiceModel.title = NSLocalizedString("home_category_title_voiceChat", comment: "")
        voiceModel.vc = HomeContentViewController(type: .voice_chat)
        dataArray.append(voiceModel)
        
        let liveModel = HomeModel()
        liveModel.title = NSLocalizedString("home_category_title_live", comment: "")
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
        model.title = NSLocalizedString("home_category_title_ktv", comment: "")
        var contentArray = [createContentModel(title: NSLocalizedString("home_content_item_ktv_title_solo", comment: ""),
                                               desc: NSLocalizedString("home_content_item_ktv_desc_solo", comment: ""),
                                               imageName: "home_ktv_solo", type: .solo),
                            createContentModel(title: NSLocalizedString("home_content_item_ktv_title_snatchsinging", comment: ""),
                                               desc: NSLocalizedString("home_content_item_ktv_desc_snatchsinging", comment: ""),
                                               imageName: "home_ktv_snatch_singing", type: .snatch_singing),
                            createContentModel(title: NSLocalizedString("home_content_item_ktv_title_cantata", comment: ""),
                                               desc: NSLocalizedString("home_content_item_ktv_desc_cantata", comment: ""),
                                               imageName: "home_ktv_chorus", type: .chorus),
                            createContentModel(title: NSLocalizedString("home_content_item_ktv_title_takesong", comment: ""),
                                               desc: NSLocalizedString("home_content_item_ktv_desc_takesong", comment: ""),
                                               imageName: "home_ktv_continue_singing", type: .continue_singing)]
        model.contentModels = contentArray
        dataArray.append(model)
        
        model = HomeContentSesionModel()
        model.type = .voice_chat
        model.title = NSLocalizedString("home_category_title_voiceChat", comment: "")
        contentArray = [createContentModel(title: NSLocalizedString("home_category_title_voiceChat", comment: ""),
                                           desc: NSLocalizedString("home_content_item_voice_desc_immersive", comment: ""),
                                           imageName: "home_voice_chat", type: .voice_chat),
                        createContentModel(title: NSLocalizedString("home_content_item_voice_title_spatial", comment: ""),
                                           desc: NSLocalizedString("home_content_item_voice_desc_spatial", comment: ""),
                                           imageName: "home_voice_spatial_chat", type: .spatial_voice)]
        model.contentModels = contentArray
        dataArray.append(model)
        
        model = HomeContentSesionModel()
        model.title = NSLocalizedString("home_category_title_live", comment: "")
        model.type = .live
        contentArray = [createContentModel(title: NSLocalizedString("home_content_item_live_title_show", comment: ""),
                                           desc: NSLocalizedString("home_content_item_live_desc_show", comment: ""),
                                           imageName: "home_live_show", type: .show),
                        createContentModel(title: NSLocalizedString("home_content_item_live_title_1v1_private", comment: ""),
                                           desc: NSLocalizedString("home_content_item_live_desc_1v1_private", comment: ""),
                                           imageName: "home_live_1v1", type: .one_v_one),
                        createContentModel(title: NSLocalizedString("home_content_item_live_title_multiple", comment: ""),
                                           desc: NSLocalizedString("home_content_item_live_desc_multiple", comment: ""),
                                           imageName: "home_live_multiple",
                                           type: .multiple),
                        createContentModel(title: NSLocalizedString("home_content_item_live_title_show_1v1_private", comment: ""),
                                           desc: NSLocalizedString("home_content_item_live_desc_show_1v1_private", comment: ""),
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

//
//  ByteBeautyModel.swift
//  testNewAgoraSDK
//
//  Created by zhaoyongqiang on 2022/11/17.
//

import UIKit

@objc
enum BeautyFactoryType: Int, CaseIterable {
    // 字节
    case byte
    // 商汤
    case sense
    // 相芯
    case fu
    // 声网
    case agora
    
    var title: String {
        switch self {
        case .byte: return "火山引擎"
        case .sense: return "商汤"
        case .fu: return "相芯"
        case .agora: return "声网"
        }
    }
}

class BeautyBaseModel: NSObject, Codable {
    var icon: String?
    var name: String?
    var isSelected: Bool = false
    /// 是否双向调节
    var enableNegative: Bool = false
}

class BeautyModel: BeautyBaseModel {
    static var beautyType: BeautyFactoryType = .sense
    /// 特效素材相对于 ComposeMakeup.bundle/ComposeMakeup 的路径
    var path: String?
    /// key 素材中的功能 key
    var key: String?
    /// 特效强度 （0~1）
    var value: CGFloat = 0
    
    static func createBeautyData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createByteBeautyData()
        case .sense: return createSenseBeautyData()
        case .fu: return createFUBeautyData()
        case .agora: return createAgoraBeautyData()
        }
    }
    
    static func createStyleData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesStyleData()
        case .sense: return createSenseStyleData()
        case .fu: return createFUStyleData()
        case .agora: return []
        }
    }
    
    static func createAnimojiData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return []
        case .sense: return []
        case .fu: return createFUAnimojiData()
        case .agora: return []
        }
    }
    
    static func createFilterData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesFilterData()
        case .sense: return []
        case .fu: return []
        case .agora: return []
        }
    }
    
    static func createStickerData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesStickerData()
        case .sense: return createSenseStickerData()
        case .fu: return createFUStickerData()
        case .agora: return []
        }
    }
    
    static func createAdjustData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesAdjustData()
        case .sense: return createSenseAdjustData()
        case .fu: return createFUAdjustData()
        case .agora: return createAgoraAdjustData()
        }
    }
    
    static func createBackgroundData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.key = "blur"
        model.path = "xuhua"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_xuhua".show_localized
        model.icon = "show_bg_blur"
        dataArray.append(model)
        
        model = BeautyModel()
        model.key = "show_live_mritual_bg"
        model.path = "show_live_mritual_bg"
        model.name = "show_beauty_item_filter_mitao".show_localized
        model.icon = "show_bg_peach"
        model.value = 0.5
        dataArray.append(model)
        
        return dataArray
    }
}

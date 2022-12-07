//
//  ByteBeautyModel.swift
//  testNewAgoraSDK
//
//  Created by zhaoyongqiang on 2022/11/17.
//

import UIKit

class ByteBaseModel: Codable {
    var icon: String?
    var name: String?
    var isSelected: Bool = false
    /// 是否双向调节
    var enableNegative: Bool = false
}

class ByteBeautyModel: ByteBaseModel {
    /// 特效素材相对于 ComposeMakeup.bundle/ComposeMakeup 的路径
    var path: String?
    /// key 素材中的功能 key
    var key: String?
    /// 特效强度 （0~1）
    var value: CGFloat = 0
    /// 美妆
    var makupValue: CGFloat = 0
    
    static func createBeautyData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/beauty_IOS_lite"
        model.key = "smooth"
        model.value = 0.3
        model.name = "磨皮".show_localized
        model.icon = "meiyan_icon_mopi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/beauty_IOS_lite"
        model.key = "whiten"
        model.value = 0.5
        model.name = "美白".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Overall"
        model.value = 0.15
        model.name = "瘦脸".show_localized
        model.icon = "meiyan_icon_shoulian"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Zoom_Cheekbone"
        model.value = 0.3
        model.name = "瘦颧骨".show_localized
        model.icon = "meiyan_icon_shouquangu"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Eye"
        model.value = 0.15
        model.name = "大眼".show_localized
        model.icon = "meiyan_icon_dayan"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Nose"
        model.value = 0.15
        model.name = "瘦鼻".show_localized
        model.icon = "meiyan_icon_shoubi"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Chin"
        model.value = 0.06
        model.name = "下巴".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Zoom_Jawbone"
        model.value = 0.06
        model.name = "下颌骨".show_localized
        model.icon = "meiyan_icon_xiahegu"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Forehead"
        model.value = 0.4
        model.name = "额头".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_ZoomMouth"
        model.value = 0.16
        model.name = "嘴型".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
    
        model = ByteBeautyModel()
        model.path = "/beauty_4Items"
        model.key = "BEF_BEAUTY_WHITEN_TEETH"
        model.value = 0.2
        model.name = "美牙".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createStyleData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/baixi"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "白皙".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/tianmei"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "甜美".show_localized
        model.icon = "meiyan_fgz_tianmei"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/cwei"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "C位".show_localized
        model.icon = "meiyan_fgz_cwei"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/yuanqi"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "元气".show_localized
        model.icon = "meiyan_fgz_yuanqi"
        dataArray.append(model)
    
        return dataArray
    }
    
    static func createFilterData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_02_14"
        model.value = 0.4
        model.name = "奶油".show_localized
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_07_06"
        model.value = 0.4
        model.name = "马卡龙".show_localized
        model.icon = "meiyan_lj_makalong"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_03_20"
        model.value = 0.4
        model.name = "氧气".show_localized
        model.icon = "meiyan_lj_yangqi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_11_09"
        model.value = 0.4
        model.name = "物语".show_localized
        model.icon = "meiyan_lj_wuyu"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_31_Po9"
        model.value = 0.4
        model.name = "海边人像".show_localized
        model.icon = "meiyan_lj_haibian"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_05_10"
        model.value = 0.4
        model.name = "洛丽塔".show_localized
        model.icon = "meiyan_lj_luolita"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_06_03"
        model.value = 0.4
        model.name = "蜜桃".show_localized
        model.icon = "meiyan_lj_mitao"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_09_19"
        model.value = 0.4
        model.name = "樱花".show_localized
        model.icon = "meiyan_lj_yinghua"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_12_08"
        model.value = 0.4
        model.name = "北海道".show_localized
        model.icon = "meiyan_lj_beihaidao"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_45_S3"
        model.value = 0.4
        model.name = "旅途".show_localized
        model.icon = "meiyan_lj_lvtu"
        dataArray.append(model)
    
        return dataArray
    }
    
    static func createStickerData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "biti"
        model.name = "鼻涕".show_localized
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "test_sticker"
        model.name = "周年狂欢".show_localized
        model.icon = "meiyan_lj_makalong"
        dataArray.append(model)
        
        return dataArray
    }
}

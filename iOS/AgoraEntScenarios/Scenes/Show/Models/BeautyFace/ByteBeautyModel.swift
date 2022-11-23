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
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/beauty_IOS_lite"
        model.key = "smooth"
        model.value = 0.8
        model.name = "磨皮"
        model.icon = "meiyan_icon_mopi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/beauty_IOS_lite"
        model.key = "whiten"
        model.value = 0.35
        model.name = "美白"
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Overall"
        model.value = 0.35
        model.name = "瘦脸"
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Zoom_Cheekbone"
        model.value = 0.2
        model.name = "瘦颧骨"
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Eye"
        model.value = 0.4
        model.name = "大眼"
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Nose"
        model.value = 0.2
        model.name = "瘦鼻"
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Chin"
        model.value = 0.6
        model.name = "下巴"
        model.icon = "meiyan_icon_xiaba"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Zoom_Jawbone"
        model.value = 0.2
        model.name = "下颌骨"
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Forehead"
        model.value = 0
        model.name = "额头"
        model.icon = "meiyan_icon_etou"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_ZoomMouth"
        model.value = 0.1
        model.name = "嘴型"
        model.icon = "meiyan_icon_zuixing"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/beauty_4Items"
        model.key = "BEF_BEAUTY_BRIGHTEN_EYE"
        model.value = 0.5
        model.name = "亮眼"
        model.icon = "meiyan_icon_liangyan"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/beauty_4Items"
        model.key = "BEF_BEAUTY_WHITEN_TEETH"
        model.value = 0.6
        model.name = "美牙"
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createStyleData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/baixi"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "白皙"
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/tianmei"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "甜美"
        model.icon = "meiyan_fgz_tianmei"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/cwei"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "C位"
        model.icon = "meiyan_fgz_cwei"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "/style_makeup/yuanqi"
        model.key = "Filter_ALL"
        model.value = 0.5
        model.makupValue = 0.6
        model.name = "元气"
        model.icon = "meiyan_fgz_yuanqi"
        dataArray.append(model)
    
        return dataArray
    }
    
    static func createFilterData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_02_14"
        model.value = 0.4
        model.name = "奶油"
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_07_06"
        model.value = 0.4
        model.name = "马卡龙"
        model.icon = "meiyan_lj_makalong"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_03_20"
        model.value = 0.4
        model.name = "氧气"
        model.icon = "meiyan_lj_yangqi"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_11_09"
        model.value = 0.4
        model.name = "物语"
        model.icon = "meiyan_lj_wuyu"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_31_Po9"
        model.value = 0.4
        model.name = "海边人像"
        model.icon = "meiyan_lj_haibian"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_05_10"
        model.value = 0.4
        model.name = "洛丽塔"
        model.icon = "meiyan_lj_luolita"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_06_03"
        model.value = 0.4
        model.name = "蜜桃"
        model.icon = "meiyan_lj_mitao"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_09_19"
        model.value = 0.4
        model.name = "樱花"
        model.icon = "meiyan_lj_yinghua"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_12_08"
        model.value = 0.4
        model.name = "北海道"
        model.icon = "meiyan_lj_beihaidao"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "Filter_45_S3"
        model.value = 0.4
        model.name = "旅途"
        model.icon = "meiyan_lj_lvtu"
        dataArray.append(model)
    
        return dataArray
    }
    
    static func createStickerData() -> [ByteBeautyModel] {
        var dataArray = [ByteBeautyModel]()
        var model = ByteBeautyModel()
        model.name = "无"
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "biti"
        model.name = "鼻涕"
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        model = ByteBeautyModel()
        model.path = "test_sticker"
        model.name = "周年狂欢"
        model.icon = "meiyan_lj_makalong"
        dataArray.append(model)
        
        return dataArray
    }
}

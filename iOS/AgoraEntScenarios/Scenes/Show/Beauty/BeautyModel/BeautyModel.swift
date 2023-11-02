//
//  ByteBeautyModel.swift
//  testNewAgoraSDK
//
//  Created by zhaoyongqiang on 2022/11/17.
//

import UIKit

@objc
enum BeautyFactoryType: Int {
    // 字节
    case byte
    // 商汤
    case sense
    // 相芯
    case fu
}

class BeautyBaseModel: NSObject, Codable {
    var icon: String?
    var name: String?
    var isSelected: Bool = false
    /// 是否双向调节
    var enableNegative: Bool = false
}

@objc
class BeautyModel: BeautyBaseModel {
    @objc
    static var beautyType: BeautyFactoryType = .byte
    /// 特效素材相对于 ComposeMakeup.bundle/ComposeMakeup 的路径
    var path: String?
    /// key 素材中的功能 key
    var key: String?
    /// 特效强度 （0~1）
    var value: CGFloat = 0
    
    static func createBeautyData() -> [BeautyModel] {
        switch beautyType {
        case .byte:
            return createByteBeautyData()
        case .sense:
            return createSenseBeautyData()
        case .fu:
            return createFUBeautyData()
        }
    }
    
    static func createByteBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/beauty_IOS_lite"
        model.key = "smooth"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/beauty_IOS_lite"
        model.key = "whiten"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_whiten".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Overall"
        model.value = 0.15
        model.enableNegative = true
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Zoom_Cheekbone"
        model.value = 0.3
        model.enableNegative = true
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Eye"
        model.value = 0.15
        model.enableNegative = true
        model.name = "show_beauty_item_beauty_eye".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Nose"
        model.value = 0.15
        model.enableNegative = true
        model.name = "show_beauty_item_beauty_nose".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Chin"
        model.value = 0.46
        model.name = "show_beauty_item_beauty_chin".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Zoom_Jawbone"
        model.value = 0.46
        model.enableNegative = true
        model.name = "show_beauty_item_beauty_jawbone".show_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_Forehead"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_forehead".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "/reshape_lite"
        model.key = "Internal_Deform_ZoomMouth"
        model.value = 0.16
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
    
        model = BeautyModel()
        model.path = "/beauty_4Items"
        model.key = "BEF_BEAUTY_WHITEN_TEETH"
        model.value = 0.2
        model.name = "show_beauty_item_beauty_teeth".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        return dataArray
    }
    
    static func createSenseBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "103"
        model.value = 0.55
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "101"
        model.value = 0.2
        model.name = "show_beauty_item_beauty_whiten".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "201"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "318"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "202"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_eye".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "306"
        model.value = 0
        model.name = "show_beauty_item_beauty_nose".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "303"
        model.value = 0
        model.name = "show_beauty_item_beauty_chin".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "320"
        model.value = 0
        model.name = "show_beauty_item_beauty_jawbone".show_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "304"
        model.value = 0
        model.name = "show_beauty_item_beauty_forehead".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "309"
        model.value = 0
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
    
        model = BeautyModel()
        model.path = ""
        model.key = "317"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "blurLevel"
        model.value = 0.7
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "rosy"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_rosy".show_localized
        model.icon = "meiyan_icon_meibai_hongrun"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "contouring"
        model.value = 0
        model.name = "show_beauty_item_beauty_contouring".show_localized
        model.icon = "meiyan_icon_contouring"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "thin"
        model.value = 0
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheekV"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_cheekV".show_localized
        model.icon = "meiyan_icon_cheekV"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheekNarrow"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekNarrow".show_localized
        model.icon = "meiyan_icon_cheekNarrow"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheekSmall"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekSmall".show_localized
        model.icon = "meiyan_icon_cheekSmall"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheek"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "chin"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_chin".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "forehead"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_forehead".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "enlarge"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_eye".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeBright"
        model.value = 0
        model.name = "show_beauty_item_beauty_clarity".show_localized
        model.icon = "meiyan_icon_liangyan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeCircle"
        model.value = 0
        model.name = "show_beauty_item_beauty_eyeCircle".show_localized
        model.icon = "meiyan_icon_eyeCircle"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeSpace"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_eyeSpace".show_localized
        model.icon = "meiyan_icon_eyeSpace"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeLid"
        model.value = 0
        model.name = "show_beauty_item_beauty_eyeLid".show_localized
        model.icon = "meiyan_icon_eyeLid"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "pouchStrength"
        model.value = 0
        model.name = "show_beauty_item_beauty_pouchStrength".show_localized
        model.icon = "meiyan_icon_pouchStrength"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "browHeight"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_browPosition".show_localized
        model.icon = "meiyan_icon_browPosition"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "browThick"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_browThickness".show_localized
        model.icon = "meiyan_icon_browThickness"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "nose"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_nose".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "wrinkles"
        model.value = 0
        model.name = "show_beauty_item_beauty_wrinkles".show_localized
        model.icon = "meiyan_icon_wrinkles"
        dataArray.append(model)

        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "philtrum"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_mouthPosition".show_localized
        model.icon = "meiyan_icon_mouthPosition"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "longNose"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_noseLift".show_localized
        model.icon = "meiyan_icon_noseLift"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "lowerJaw"
        model.value = 0
        model.name = "show_beauty_item_beauty_jawbone".show_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouth"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "lipThick"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_lipThick".show_localized
        model.icon = "meiyan_icon_lipThick"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "toothWhiten"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        if beautyType == .byte {
            model = BeautyModel()
            model.path = "/style_makeup/baixi"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_baixi".show_localized
            model.icon = "meiyan_fgz_baixi"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "/style_makeup/tianmei"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_tianmei".show_localized
            model.icon = "meiyan_fgz_tianmei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "/style_makeup/cwei"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_cwei".show_localized
            model.icon = "meiyan_fgz_cwei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "/style_makeup/yuanqi"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_yuanqi".show_localized
            model.icon = "meiyan_fgz_yuanqi"
            dataArray.append(model)
            
        } else if beautyType == .sense {
            model = BeautyModel()
            model.path = "qise.zip"
            model.key = "Makeup_ALL"
            model.value = 0.5
            model.name = "show_beauty_item_effect_cwei".show_localized
            model.icon = "meiyan_fgz_cwei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.key = "Makeup_ALL"
            model.path = "wanneng.zip"
            model.value = 0.5
            model.name = "show_beauty_item_effect_yuanqi".show_localized
            model.icon = "meiyan_fgz_yuanqi"
            dataArray.append(model)
            
        } else if beautyType == .fu {
            model = BeautyModel()
            model.path = "graphics/face_makeup"
            model.key = "makeup/xinggan"
            model.value = 0.69
            model.name = "show_beauty_item_effect_sexy".show_localized
            model.icon = "demo_combination_sexy"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "graphics/face_makeup"
            model.key = "makeup/tianmei"
            model.value = 0.69
            model.name = "show_beauty_item_effect_sweet".show_localized
            model.icon = "demo_combination_sweet"
            dataArray.append(model)
            
//            model = BeautyModel()
//            model.path = "graphics/face_makeup"
//            model.key = "style/jingdian"
//            model.value = 0.5
//            model.name = "show_beauty_item_effect_jingdian".show_localized
//            model.icon = "meiyan_fgz_jingdian"
//            dataArray.append(model)
//
//            model = BeautyModel()
//            model.path = "graphics/face_makeup"
//            model.key = "style/yuansheng"
//            model.value = 0.5
//            model.name = "show_beauty_item_effect_yuansheng".show_localized
//            model.icon = "meiyan_fgz_yuansheng"
//            dataArray.append(model)
        }
    
        return dataArray
    }
    
    static func createAnimojiData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .fu {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".show_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "kaola_Animoji"
            model.name = "show_beauty_item_ar_kaola".show_localized
            model.icon = "kaola_Animoji"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "hashiqi_Animoji"
            model.value = 0.4
            model.name = "show_beauty_item_ar_hashiqi".show_localized
            model.icon = "hashiqi_Animoji"
            dataArray.append(model)
        }
        return dataArray
    }
    
    static func createFilterData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .byte {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".show_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_02_14"
            model.value = 0.4
            model.name = "show_beauty_item_filter_cream".show_localized
            model.icon = "meiyan_lj_naiyou"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_07_06"
            model.value = 0.4
            model.name = "show_beauty_item_filter_mokalong".show_localized
            model.icon = "meiyan_lj_makalong"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_03_20"
            model.value = 0.4
            model.name = "show_beauty_item_filter_oxgen".show_localized
            model.icon = "meiyan_lj_yangqi"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_11_09"
            model.value = 0.4
            model.name = "show_beauty_item_filter_wuyu".show_localized
            model.icon = "meiyan_lj_wuyu"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_31_Po9"
            model.value = 0.4
            model.name = "show_beauty_item_filter_po9".show_localized
            model.icon = "meiyan_lj_haibian"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_05_10"
            model.value = 0.4
            model.name = "show_beauty_item_filter_lolita".show_localized
            model.icon = "meiyan_lj_luolita"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_06_03"
            model.value = 0.4
            model.name = "show_beauty_item_filter_mitao".show_localized
            model.icon = "meiyan_lj_mitao"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_09_19"
            model.value = 0.4
            model.name = "show_beauty_item_filter_yinhua".show_localized
            model.icon = "meiyan_lj_yinghua"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_12_08"
            model.value = 0.4
            model.name = "show_beauty_item_filter_beihaidao".show_localized
            model.icon = "meiyan_lj_beihaidao"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_45_S3"
            model.value = 0.4
            model.name = "show_beauty_item_filter_s3".show_localized
            model.icon = "meiyan_lj_lvtu"
            dataArray.append(model)
        }
        return dataArray
    }
    
    static func createStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .byte {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".show_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "huahua"
            model.name = "show_beauty_item_sticker_huahua".show_localized
            model.icon = "meiyan_lj_naiyou"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "wochaotian"
            model.name = "show_beauty_item_sticker_huahua".show_localized
            model.icon = "meiyan_lj_makalong"
            dataArray.append(model)
            
        } else if beautyType == .sense {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".show_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "lianxingface.zip"
            model.name = "show_beauty_item_sticker_huahua".show_localized
            model.icon = "meiyan_lj_naiyou"
            dataArray.append(model)
            
        } else if beautyType == .fu {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".show_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "CatSparks"
            model.name = "show_beauty_item_sticker_cat".show_localized
            model.icon = "CatSparks"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "sdlu"
            model.name = "show_beauty_item_sticker_milu".show_localized
            model.icon = "sdlu"
            dataArray.append(model)
        }
        return dataArray
    }
    
    static func createAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .sense {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".show_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_contrast".show_localized
            model.icon = "show_beauty_ic_adjust_contrast"
            model.path = ""
            model.key = "601"
            model.value = 0
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_saturation".show_localized
            model.icon = "show_beauty_ic_adjust_saturation"
            model.path = ""
            model.key = "602"
            model.value = 0
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_sharpen".show_localized
            model.icon = "show_beauty_ic_adjust_sharp"
            model.path = ""
            model.key = "603"
            model.value = 0.5
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_clarity".show_localized
            model.icon = "show_beauty_ic_adjust_clear"
            model.path = ""
            model.key = "604"
            model.value = 1.0
            dataArray.append(model)
        }
        return dataArray
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

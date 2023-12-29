//
//  BeautyModel+Bytes.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/6.
//

import Foundation

extension BeautyModel {
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
    
    static func createBytesStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
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
        
        return dataArray
    }
    
    static func createBytesFilterData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
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
        
        return dataArray
    }
    
    static func createBytesStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
     
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "matting_bg"
        model.name = "show_beauty_item_sticker_matting".show_localized
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "background_blur"
        model.name = "show_beauty_item_sticker_background".show_localized
        model.icon = "meiyan_lj_makalong"
        dataArray.append(model)
        
        return dataArray
    }
}

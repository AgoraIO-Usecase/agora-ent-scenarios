//
//  BeautyModel+Agora.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/10.
//

import Foundation

extension BeautyModel {
    static func createAgoraBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "smoothnessLevel"
        model.value = 0.75
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "lighteningLevel"
        model.value = 0.75
        model.name = "show_beauty_item_beauty_whiten".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "rednessLevel"
        model.value = 0
        model.name = "show_beauty_item_beauty_rosy".show_localized
        model.icon = "meiyan_icon_meibai_hongrun"
        dataArray.append(model)
             
        return dataArray
    }

    static func createAgoraShapeData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        // 1. Eye enlarging
        model = BeautyModel()
        model.path = ""
        model.key = "eyescale"
        model.value = 0.53
        model.name = "show_beauty_item_beauty_eye".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        // 2. Chin lengthening
        model = BeautyModel()
        model.path = ""
        model.key = "chin"
        model.value = 0
        model.name = "show_beauty_item_beauty_agora_chin".show_localized
        model.icon = "meiyan_icon_xiaba"
        dataArray.append(model)
        
        // 3. Face thinning
        model = BeautyModel()
        model.path = ""
        model.key = "facewidth"
        model.value = 0.1
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        // 4. Cheekbone thinning
        model = BeautyModel()
        model.path = ""
        model.key = "cheekbone"
        model.value = 0.43
        model.name = "show_beauty_item_beauty_cheekbones_chin".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        // 5. Nose lengthening
        model = BeautyModel()
        model.path = ""
        model.key = "noselength"
        model.value = 0
        model.name = "show_beauty_item_beauty_noselength".show_localized
        model.icon = "meiyan_icon_changbi"
        dataArray.append(model)
        
        // 6. Nose thinning
        model = BeautyModel()
        model.path = ""
        model.key = "nosewidth"
        model.value = 0.72
        model.name = "show_beauty_item_beauty_nose".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 7. Mouth shape
        model = BeautyModel()
        model.path = ""
        model.key = "mouthscale"
        model.value = 0.2
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        dataArray.append(model)
        
//        // 8. Jawline
//        model = BeautyModel()
//        model.path = ""
//        model.key = "facecontour"
//        model.value = 0.1
//        model.name = "show_beauty_item_beauty_jawbone".show_localized
//        model.icon = "meiyan_icon_contouring"
//        dataArray.append(model)
        
        // 9. Jaw
        model = BeautyModel()
        model.path = ""
        model.key = "cheek"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_jawbone".show_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        // 10. Hairline
        model = BeautyModel()
        model.path = ""
        model.key = "forehead"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_hairline".show_localized
        model.icon = "meiyan_icon_etou"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "gentlemaneface"
        model.value = 0.5
        model.name = "show_beauty_item_beauty_male".show_localized
        model.icon = "meiyan_icon_male"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "ladyface"
        model.value = 0.8
        model.name = "show_beauty_item_beauty_female".show_localized
        model.icon = "meiyan_icon_female"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createAgoraAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.name = "show_beauty_item_adjust_sharpen".show_localized
        model.icon = "show_beauty_ic_adjust_sharp"
        model.path = ""
        model.key = "sharpnessLevel"
        model.value = 0
        dataArray.append(model)
                
        return dataArray
    }
    
    static func createAgoraFilterData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        // Original
        model = BeautyModel()
        model.path = ""
        model.key = "yuansheng"
        model.value = 0.7
        model.name = "show_beauty_item_effect_lut_yuansheng".show_localized
        model.icon = "meiyan_icon_yuansheng"
        dataArray.append(model)
        
        // Fresh white
        model = BeautyModel()
        model.path = ""
        model.key = "nenbai"
        model.value = 0.7
        model.name = "show_beauty_item_effect_lut_nenbai".show_localized
        model.icon = "meiyan_icon_nenbai"
        dataArray.append(model)
        
        // Cool white
        model = BeautyModel()
        model.path = ""
        model.key = "lengbai"
        model.value = 0.7
        model.name = "show_beauty_item_effect_lut_lengbai".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createAgoraStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        // Mixed blood
        model = BeautyModel()
        model.path = ""
        model.key = "makeup1"
        model.value = 0.5
        model.name = "show_beauty_item_effect_hunxue".show_localized
        model.icon = "meiyan_hunxue"
        dataArray.append(model)
        
        // European and American
        model = BeautyModel()
        model.path = ""
        model.key = "makeup2"
        model.value = 0.5
        model.name = "show_beauty_item_effect_oumei".show_localized
        model.icon = "meiyan_oumei"
        dataArray.append(model)
        
        return dataArray
    }
}

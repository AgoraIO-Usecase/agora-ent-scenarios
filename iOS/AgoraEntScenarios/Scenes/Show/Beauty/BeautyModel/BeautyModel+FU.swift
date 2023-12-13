//
//  BeautyModel+FU.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/6.
//

import Foundation

extension BeautyModel {
    static func createFUBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "blurLevel"
        model.value = 0.65
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "whiten"
        model.value = 0.75
        model.name = "show_beauty_item_beauty_whiten".show_localized
        model.icon = "meiyan_icon_meibai"
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
        model.value = 0.3
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)

        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheek"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "enlarge"
        model.value = 0
        model.name = "show_beauty_item_beauty_eye".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "nose"
        model.value = 0
        model.name = "show_beauty_item_beauty_nose".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "chin"
        model.value = 0
        model.name = "show_beauty_item_beauty_chin".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
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
        model.key = "forehead"
        model.value = 0
        model.name = "show_beauty_item_beauty_forehead".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouth"
        model.value = 0
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "toothWhiten"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeBright"
        model.value = 0
        model.name = "show_beauty_item_beauty_liangyan".show_localized
        model.icon = "meiyan_icon_liangyan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "wrinkles"
        model.value = 0
        model.name = "show_beauty_item_beauty_qufalingwen".show_localized
        model.icon = "meiyan_icon_qufalingwen"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "pouchStrength"
        model.value = 0
        model.name = "show_beauty_item_beauty_heiyanquan".show_localized
        model.icon = "meiyan_icon_heiyanquan"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "makeup/diadiatu"
        model.value = 0.8
        model.name = "show_beauty_item_effect_diadiatu".show_localized
        model.icon = "meiyan_makeup_diadiatu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "makeup/hunxue"
        model.value = 0.8
        model.name = "show_beauty_item_effect_hunxue".show_localized
        model.icon = "meiyan_makeup_mixed_race"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUAnimojiData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
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
        
        return dataArray
    }
    
    static func createFUStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
     
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
                
        model = BeautyModel()
        model.path = "sdlu"
        model.name = "show_beauty_item_sticker_milu".show_localized
        model.icon = "sdlu"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.name = "show_beauty_item_adjust_sharpen".show_localized
        model.icon = "show_beauty_ic_adjust_sharp"
        model.path = "face_beautification"
        model.key = "sharpen"
        model.value = 0
        dataArray.append(model)
        
        return dataArray
    }
}

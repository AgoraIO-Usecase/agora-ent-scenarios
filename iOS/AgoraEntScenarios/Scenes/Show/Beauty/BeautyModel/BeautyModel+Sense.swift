//
//  BeautyModel+Scene.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/6.
//

import Foundation

extension BeautyModel {
    static func createSenseBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "103"
        model.value = 0.75
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "101"
        model.value = 0.75
        model.name = "show_beauty_item_beauty_whiten".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "201"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "318"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "202"
        model.value = 0
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
        
        model = BeautyModel()
        model.path = ""
        model.key = "314"
        model.value = 0
        model.name = "show_beauty_item_beauty_liangyan".show_localized
        model.icon = "meiyan_icon_liangyan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "316"
        model.value = 0
        model.name = "show_beauty_item_beauty_qufalingwen".show_localized
        model.icon = "meiyan_icon_qufalingwen"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "315"
        model.value = 0
        model.name = "show_beauty_item_beauty_heiyanquan".show_localized
        model.icon = "meiyan_icon_heiyanquan"
        dataArray.append(model)
        
        
        return dataArray
    }
    
    static func createSenseStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "hunxue.zip"
        model.key = "Makeup_ALL"
        model.value = 0.8
        model.name = "show_beauty_item_effect_hunxue".show_localized
        model.icon = "meiyan_hunxue"
        dataArray.append(model)
        
        model = BeautyModel()
        model.key = "Makeup_ALL"
        model.path = "oumei.zip"
        model.value = 0.8
        model.name = "show_beauty_item_effect_oumei".show_localized
        model.icon = "meiyan_oumei"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createSenseStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
     
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "ShangBanLe.zip"
        model.name = "show_beauty_item_sticker_shangbanle".show_localized
        model.icon = "ShangBanLe"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createSenseAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
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
        model.value = 0
        dataArray.append(model)
        
        model = BeautyModel()
        model.name = "show_beauty_item_adjust_clarity".show_localized
        model.icon = "show_beauty_ic_adjust_clear"
        model.path = ""
        model.key = "604"
        model.value = 0
        dataArray.append(model)
     
        return dataArray
    }
}

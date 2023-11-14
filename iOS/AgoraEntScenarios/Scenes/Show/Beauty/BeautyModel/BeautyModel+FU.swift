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
    
    static func createFUStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_makeup"
        model.key = "makeup/xinggan"
        model.value = 0.5
        model.name = "show_beauty_item_effect_sexy".show_localized
        model.icon = "meiyan_fgz_cwei"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_makeup"
        model.key = "makeup/tianmei"
        model.value = 0.5
        model.name = "show_beauty_item_effect_sweet".show_localized
        model.icon = "meiyan_fgz_cwei"
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
        model.path = "CatSparks"
        model.name = "show_beauty_item_sticker_cat".show_localized
        model.icon = "CatSparks"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "sdlu"
        model.name = "show_beauty_item_sticker_milu".show_localized
        model.icon = "sdlu"
        dataArray.append(model)
        
        return dataArray
    }
    
}

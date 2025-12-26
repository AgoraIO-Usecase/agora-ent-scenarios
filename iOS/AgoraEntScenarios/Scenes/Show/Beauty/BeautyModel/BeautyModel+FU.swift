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
        
        // 无特效
        model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        // 重置按钮
        model = BeautyModel()
        model.name = "show_beauty_item_beauty_reset".show_localized
        model.icon = "meiyan_icon_chongzhi"
        dataArray.append(model)
        
        // 1. 磨皮
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "blurLevel"
        model.value = 50  // Android: 0.5 * 100 = 50
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        // 2. 美白
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "whiten"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_beauty_whiten".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        // 3. 红润
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "redness"
        model.value = 30  // Android: 0.3 * 100 = 30
        model.name = "show_beauty_item_beauty_redden".show_localized
        model.icon = "meiyan_icon_meibai_hongrun"
        dataArray.append(model)
        
        // 4. 锐化
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "sharpen"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_adjust_sharpen".show_localized
        model.icon = "show_beauty_ic_adjust_sharp"
        dataArray.append(model)
        
        // 5. 清晰度
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "clarity"
        model.value = 0
        model.name = "show_beauty_item_adjust_clarity".show_localized
        model.icon = "show_beauty_ic_adjust_clear"
        dataArray.append(model)
        
        // 6. 瘦脸
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "thin"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        // 7. 小脸
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "faceSmall"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_faceSmall".show_localized
        model.icon = "meiyan_icon_cheekSmall"
        dataArray.append(model)
        
        // 8. 窄脸
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "faceWidth"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_narrowFace".show_localized
        model.icon = "meiyan_icon_cheekNarrow"
        dataArray.append(model)
        
        // 9. 长脸
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "faceLength"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_longFace".show_localized
        model.icon = "meiyan_icon_cheekShort"
        dataArray.append(model)
        
        // 10. 瘦颧骨
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheek"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        // 11. 下颌线
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "lowerJaw"
        model.value = 10  // Android: 0.1f * 100 = 10
        model.name = "show_beauty_item_beauty_jawbone".show_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        // 12. v脸
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mandible"
        model.value = 50  // Android: 0.5f * 100 = 50
        model.name = "show_beauty_item_beauty_mandible".show_localized
        model.icon = "meiyan_icon_cheekV"
        dataArray.append(model)
        
        // 13. 瘦下巴
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "chin"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_chin_length".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        // 14. 发际线
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "forehead"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_forehead".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        // 15. 去法令纹
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "wrinkles"
        model.value = 80  // Android: 0.8f * 100 = 80
        model.name = "show_beauty_item_beauty_remove_nasolabial_folds".show_localized
        model.icon = "meiyan_icon_qufalingwen"
        dataArray.append(model)
        
        // 16. 大眼
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "enlarge"
        model.value = 40  // Android: 0.4f * 100 = 40
        model.name = "show_beauty_item_beauty_eye_big".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        // 17. 亮眼
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeBright"
        model.value = 30  // Android: 0.3f * 100 = 30
        model.name = "show_beauty_item_beauty_bright_eye".show_localized
        model.icon = "meiyan_icon_liangyan"
        dataArray.append(model)
        
        // 19. 去黑眼圈
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "pouchStrength"
        model.value = 80  // Android: 0.8f * 100 = 80
        model.name = "show_beauty_item_beauty_remove_dark_circles".show_localized
        model.icon = "meiyan_icon_heiyanquan"
        dataArray.append(model)
        
        // 20. 眼移动
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyePosition"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_eye_position".show_localized
        model.icon = "meiyan_icon_intensityEyeHeight"
        model.enableNegative = true
        dataArray.append(model)
        
        // 21. 眼距
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeDistance"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_eye_distance".show_localized
        model.icon = "meiyan_icon_eyeSpace"
        model.enableNegative = true
        dataArray.append(model)
        
        // 23. 眼睑
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyeLid"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_eyelid".show_localized
        model.icon = "meiyan_icon_eyeLid"
        dataArray.append(model)
        
        // 24. 眼角
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyecorner"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_eye_corner".show_localized
        model.icon = "meiyan_icon_intensityCanthus"
        model.enableNegative = true
        dataArray.append(model)
        
        // 26. 瘦鼻
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "nose"
        model.value = 0  // Android: 0.0f * 100 = 0
        model.name = "show_beauty_item_beauty_nose_width".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 27. 长鼻
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "noseLength"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_nose_long".show_localized
        model.icon = "meiyan_icon_changbi"
        model.enableNegative = true
        dataArray.append(model)
        
        // 33. 嘴型
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouth"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
        
        // 34. 缩人中
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouthPosition"
        model.value = 50  // Android: 0.5 * 100 = 50
        model.name = "show_beauty_item_beauty_mouth_position".show_localized
        model.icon = "meiyan_icon_mouthPosition"
        dataArray.append(model)
        
        // 35. 微笑唇
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouthSmile"
        model.value = 50  // Android: 0.5 * 100 = 50
        model.name = "show_beauty_item_beauty_mouth_smile".show_localized
        model.icon = "meiyan_icon_smile"
        dataArray.append(model)
        
        // 36. 丰唇
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouthLip"
        model.value = 50  // Android: 0.5 * 100 = 50
        model.name = "show_beauty_item_beauty_mouth_lip".show_localized
        model.icon = "meiyan_icon_lipThick"
        dataArray.append(model)
        
        // 37. 白牙
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "toothWhiten"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth2".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        // 38. 眉上下
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyebrowPosition"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_eyebrow_position".show_localized
        model.icon = "meiyan_icon_browPosition"
        model.enableNegative = true
        dataArray.append(model)
        
        // 39. 眉粗细
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "eyebrowThickness"
        model.value = 0  // Android: 0.5f * 100 - 50 = 0 (-50~50 range center)
        model.name = "show_beauty_item_beauty_eyebrow_thickness".show_localized
        model.icon = "meiyan_icon_browThickness"
        model.enableNegative = true
        dataArray.append(model)
        
        // 五官立体
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "faceThree"
        model.value = 0
        model.name = "show_beauty_item_beauty_face_three".show_localized
        model.icon = "meiyan_icon_contouring"
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
    
}

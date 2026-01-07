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
        
        // 无
        model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        // 重置
        model = BeautyModel()
        model.name = "show_beauty_item_beauty_reset".show_localized
        model.icon = "meiyan_icon_chongzhi"
        dataArray.append(model)
        
        // 1. 磨皮
        model = BeautyModel()
        model.path = ""
        model.key = "smoothnessLevel"
        model.value = 70  // Android: 0.7 * 100 = 70
        model.name = "show_beauty_item_beauty_smooth".show_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        // 2. 美白自然白
        model = BeautyModel()
        model.path = ""
        model.key = "lighteningLevel"
        model.value = 70  // Android: 0.7 * 100 = 70
        model.name = "show_beauty_item_beauty_whitenNatural".show_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        // 3. 红润
        model = BeautyModel()
        model.path = ""
        model.key = "rednessLevel"
        model.value = 0  // Updated default: 0
        model.name = "show_beauty_item_beauty_redden".show_localized
        model.icon = "meiyan_icon_meibai_hongrun"
        dataArray.append(model)
        
        // 4. 锐化
        model = BeautyModel()
        model.path = ""
        model.key = "sharpnessLevel"
        model.value = 30  // Updated default: 30
        model.name = "show_beauty_item_adjust_sharpen".show_localized
        model.icon = "show_beauty_ic_adjust_sharp"
        dataArray.append(model)
        
        // 5. 清晰度
        model = BeautyModel()
        model.path = ""
        model.key = "clarity"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_adjust_clarity".show_localized
        model.icon = "show_beauty_ic_adjust_clear"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 6. 瘦脸
        model = BeautyModel()
        model.path = ""
        model.key = "facecontour"
        model.value = 10  // Android: 10
        model.name = "show_beauty_item_beauty_overall".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        // 7. 小头 - 使用瘦脸图标
        model = BeautyModel()
        model.path = ""
        model.key = "headscale"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_headScale".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        // 8. 窄脸 - 使用瘦脸图标
        model = BeautyModel()
        model.path = ""
        model.key = "facewidth"
        model.value = 10  // Android: 10
        model.name = "show_beauty_item_beauty_narrowFace".show_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        // 9. 长脸 - 使用和小头一样的图标
        model = BeautyModel()
        model.path = ""
        model.key = "facelength"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_longFace".show_localized
        model.icon = "meiyan_icon_shoulian"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 10. 瘦颧骨
        model = BeautyModel()
        model.path = ""
        model.key = "cheekbone"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_cheekbone".show_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        // 11. 下颌线 - 保持当前图标
        model = BeautyModel()
        model.path = ""
        model.key = "shrinkcheek"
        model.value = 10  // Android: 10
        model.name = "show_beauty_item_beauty_xiahexian".show_localized
        model.icon = "meiyan_icon_mopi"  // 保持当前图标
        dataArray.append(model)
        
        // 12. v脸 - 使用下颌图标
        model = BeautyModel()
        model.path = ""
        model.key = "mandible"
        model.value = 50  // Android: 50
        model.name = "show_beauty_item_beauty_mandible".show_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        // 13. 瘦下巴
        model = BeautyModel()
        model.path = ""
        model.key = "chinlength"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_chin_length".show_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 14. 发际线
        model = BeautyModel()
        model.path = ""
        model.key = "hairlineheight"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_fajixian".show_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 15. 去法令纹
        model = BeautyModel()
        model.path = ""
        model.key = "nasolabialfolds"
        model.value = 80  // Android: 0.8 * 100 = 80
        model.name = "show_beauty_item_beauty_remove_nasolabial_folds".show_localized
        model.icon = "meiyan_icon_qufalingwen"
        dataArray.append(model)
        
        // 16. 大眼
        model = BeautyModel()
        model.path = ""
        model.key = "enlargeeye"
        model.value = 40  // Android: 40
        model.name = "show_beauty_item_beauty_eye_big".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        // 17. 亮眼
        model = BeautyModel()
        model.path = ""
        model.key = "brighteneye"
        model.value = 80  // Android: 0.8 * 100 = 80
        model.name = "show_beauty_item_beauty_bright_eye".show_localized
        model.icon = "meiyan_icon_liangyan"
        dataArray.append(model)
        
        // 18. 去黑眼圈
        model = BeautyModel()
        model.path = ""
        model.key = "darkcircle"
        model.value = 80  // Android: 0.8 * 100 = 80
        model.name = "show_beauty_item_beauty_remove_dark_circles".show_localized
        model.icon = "meiyan_icon_heiyanquan"
        dataArray.append(model)
        
        // 19. 眼移动 - 使用大眼图标
        model = BeautyModel()
        model.path = ""
        model.key = "eyeposition"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_eye_position".show_localized
        model.icon = "meiyan_icon_dayan"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 20. 眼距
        model = BeautyModel()
        model.path = ""
        model.key = "eyedistance"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_eye_distance".show_localized
        model.icon = "meiyan_icon_face_yanju"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 21. 瞳孔 - 使用大眼图标
        model = BeautyModel()
        model.path = ""
        model.key = "eyepupil"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_eye_pupil".show_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        // 22. 眼睑下至
        model = BeautyModel()
        model.path = ""
        model.key = "eyelid"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_eyelid".show_localized
        model.icon = "meiyan_icon_face_xiazhi"
        dataArray.append(model)
        
        // 23. 内眼角 - 使用大眼图标
        model = BeautyModel()
        model.path = ""
        model.key = "eyeinnercorner"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_eye_innercorner".show_localized
        model.icon = "meiyan_icon_dayan"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 24. 外眼角 - 使用大眼图标
        model = BeautyModel()
        model.path = ""
        model.key = "eyeoutercorner"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_eye_outercorner".show_localized
        model.icon = "meiyan_icon_dayan"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 25. 瘦鼻
        model = BeautyModel()
        model.path = ""
        model.key = "narrownose"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_nose_width".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 26. 长鼻
        model = BeautyModel()
        model.path = ""
        model.key = "noselength"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_nose_long".show_localized
        model.icon = "meiyan_icon_changbi"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 27. 鼻翼 - 使用瘦鼻图标
        model = BeautyModel()
        model.path = ""
        model.key = "nosewing"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_nose_wing".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 28. 鼻梁 - 使用瘦鼻图标
        model = BeautyModel()
        model.path = ""
        model.key = "nosebridge"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_nose_bridge".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 29. 山根 - 使用瘦鼻图标
        model = BeautyModel()
        model.path = ""
        model.key = "noseroot"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_nose_root".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 30. 鼻尖 - 使用瘦鼻图标
        model = BeautyModel()
        model.path = ""
        model.key = "nosetip"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_nose_tip".show_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        // 31. 鼻综合 - 使用瘦鼻图标
        model = BeautyModel()
        model.path = ""
        model.key = "nosegeneral"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_nose_general".show_localized
        model.icon = "meiyan_icon_shoubi"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 32. 嘴型
        model = BeautyModel()
        model.path = ""
        model.key = "mouthsize"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_mouth".show_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 33. 缩人中
        model = BeautyModel()
        model.path = ""
        model.key = "mouthposition"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_mouth_position".show_localized
        model.icon = "meiyan_icon_face_renzhong"
        dataArray.append(model)
        
        // 34. 微笑唇 - 使用嘴型图标
        model = BeautyModel()
        model.path = ""
        model.key = "mouthsmile"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_mouth_smile".show_localized
        model.icon = "meiyan_icon_zuixing"
        dataArray.append(model)
        
        // 35. 丰唇 - 使用嘴型图标
        model = BeautyModel()
        model.path = ""
        model.key = "mouthlip"
        model.value = 0  // Android: 0
        model.name = "show_beauty_item_beauty_mouth_lip".show_localized
        model.icon = "meiyan_icon_zuixing"
        dataArray.append(model)
        
        // 36. 白牙 - 使用牙齿图标
        model = BeautyModel()
        model.path = ""
        model.key = "whitenteeth"
        model.value = 0  // Android: 0.0 * 100 = 0
        model.name = "show_beauty_item_beauty_white_teeth".show_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        // 37. 眉上下
        model = BeautyModel()
        model.path = ""
        model.key = "eyebrowposition"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_eyebrow_position".show_localized
        model.icon = "meiyan_icon_meimaoshangxia"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 38. 眉粗细
        model = BeautyModel()
        model.path = ""
        model.key = "eyebrowthickness"
        model.value = 0  // Android: 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_eyebrow_thickness".show_localized
        model.icon = "meiyan_icon_meimaocuxi"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 确保所有项都有 icon，如果缺少则使用默认 icon
        // 使用确保存在的通用图标：meiyan_icon_mopi (磨皮)、show_beauty_ic_adjust_clear (清晰度) 等
        let defaultIcons = ["meiyan_icon_mopi", "meiyan_icon_meibai", "show_beauty_ic_adjust_clear", "meiyan_icon_shoulian"]
        var iconIndex = 0
        dataArray.forEach { model in
            if model.icon == nil || model.icon?.isEmpty == true {
                model.icon = defaultIcons[iconIndex % defaultIcons.count]
                iconIndex += 1
            }
        }
             
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
        
        // 确保所有项都有 icon，如果缺少则使用默认 icon
        // 使用确保存在的通用图标：meiyan_icon_shoulian (瘦脸)、meiyan_icon_dayan (大眼) 等
        let defaultIcons = ["meiyan_icon_shoulian", "meiyan_icon_dayan", "meiyan_icon_shoubi", "meiyan_icon_zuixing"]
        var iconIndex = 0
        dataArray.forEach { model in
            if model.icon == nil || model.icon?.isEmpty == true {
                model.icon = defaultIcons[iconIndex % defaultIcons.count]
                iconIndex += 1
            }
        }
        
        return dataArray
    }
    
    static func createAgoraAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        
        // 无
        model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        // 色调 - 保持当前图标
        model = BeautyModel()
        model.path = ""
        model.key = "hue"
        model.value = 0  // Updated default: 0 (-50~50 range)
        model.name = "show_beauty_item_beauty_hue".show_localized
        model.icon = "meiyan_icon_mopi"  // 保持当前图标
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 色温 - 保持当前图标
        model = BeautyModel()
        model.path = ""
        model.key = "temperature"
        model.value = 0  // Updated default: 0 (-50~50 range)
        model.name = "show_beauty_item_beauty_temp".show_localized
        model.icon = "meiyan_icon_mopi"  // 保持当前图标
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 饱和度
        model = BeautyModel()
        model.path = ""
        model.key = "saturation"
        model.value = 0  // Updated default: 0 (-50~50 range)
        model.name = "show_beauty_item_adjust_saturation".show_localized
        model.icon = "meiyan_icon_adjust_saturation"
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 亮度 - 保持当前图标
        model = BeautyModel()
        model.path = ""
        model.key = "brightness"
        model.value = 0  // Android: 0.0 * 50 = 0 (-50~50 range, center)
        model.name = "show_beauty_item_beauty_brightness".show_localized
        model.icon = "meiyan_icon_mopi"  // 保持当前图标
        model.enableNegative = true  // -50~50 range
        dataArray.append(model)
        
        // 确保所有项都有 icon，如果缺少则使用默认 icon
        // 使用确保存在的通用图标：show_beauty_ic_adjust_clear (清晰度)、show_beauty_ic_adjust_saturation (饱和度) 等
        let defaultIcons = ["show_beauty_ic_adjust_clear", "show_beauty_ic_adjust_saturation"]
        var iconIndex = 0
        dataArray.forEach { model in
            if model.icon == nil || model.icon?.isEmpty == true {
                model.icon = defaultIcons[iconIndex % defaultIcons.count]
                iconIndex += 1
            }
        }
                
        return dataArray
    }
    
    static func createAgoraFilterData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        
        // 无
        model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        // 1. 沉稳 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "沉稳"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_chenwen".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 2. 都市 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "都市"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_dushi".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 3. 流光 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "流光"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_liuguang".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 4. 流金 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "流金"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_liujin".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 5. 奶油 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "奶油"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_naiyou".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 6. 拿铁 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "拿铁"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_natie".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 7. 柠夏 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "柠夏"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_ningxia".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 8. 日常 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "日常"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_richang".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 9. 绅士 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "绅士"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_shenshi".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 10. 香草 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "香草"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_xiangcao".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 11. 白瓷 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "白瓷"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_baici".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 12. 白桃 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "白桃"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_baitao".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 13. 苍墨 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "苍墨"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_cangmo".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 14. 胶片 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "胶片"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_jiaopian".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 15. 霁晴 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "霁晴"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_jiqin".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 16. 漫画 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "漫画"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_manhua".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 17. 梦幻 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "梦幻"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_menghuan".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 18. 棉绒 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "棉绒"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_mianrong".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 19. 苏打 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "苏打"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_suda".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 20. 月白 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "月白"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_yuebai".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 21. 白茶 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "白茶"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_baicha".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 22. 沉谧 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "沉谧"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_chenmi".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 23. ins风 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "ins风"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_ins".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 24. 老街 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "老街"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_laojie".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 25. 泡芙 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "泡芙"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_paofu".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 26. 私藏 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "私藏"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_sicang".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 27. 盐汽水 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "盐汽水"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_yanqishui".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 28. 质感 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "质感"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_zhigan".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 29. 气色 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "气色"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_qise".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 30. 初雪 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "初雪"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_chuxue".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 31. 粉霞 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "粉霞"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_fenxia".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 32. 怀旧 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "怀旧"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_huaijiu".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 33. 焦糖 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "焦糖"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_jiaotang".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 34. 微醺 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "微醺"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_weixun".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 35. 薰衣草 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "薰衣草"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_xunyicao".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 36. 胭脂 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "胭脂"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_yanzhi".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 37. 氤氲 - 新增项，使用磨皮图标
        model = BeautyModel()
        model.path = ""
        model.key = "氤氲"
        model.value = 40  // Android: 0.4 * 100 = 40
        model.name = "show_beauty_item_filter_yinyun".show_localized
        model.icon = "meiyan_icon_lengbai"
        dataArray.append(model)
        
        // 确保所有项都有 icon，如果缺少则使用默认 icon
        // 使用冷白图标作为默认图标
        let defaultIcons = ["meiyan_icon_lengbai"]
        var iconIndex = 0
        dataArray.forEach { model in
            if model.icon == nil || model.icon?.isEmpty == true {
                model.icon = defaultIcons[iconIndex % defaultIcons.count]
                iconIndex += 1
            }
        }
        
        return dataArray
    }
    
    static func createAgoraStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        
        // 无
        model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        // 1. 学妹妆
        model = BeautyModel()
        model.path = ""
        model.key = "学妹妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_xuemei".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 2. 学姐妆
        model = BeautyModel()
        model.path = ""
        model.key = "学姐妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_xuejie".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 3. 气质妆
        model = BeautyModel()
        model.path = ""
        model.key = "气质妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_qizhi".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 4. 白皙妆
        model = BeautyModel()
        model.path = ""
        model.key = "白皙妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_baixi".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 5. 优雅妆
        model = BeautyModel()
        model.path = ""
        model.key = "优雅妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_youya".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 6. 粉晕妆
        model = BeautyModel()
        model.path = ""
        model.key = "粉晕妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_fenyun".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 7. 俏皮妆
        model = BeautyModel()
        model.path = ""
        model.key = "俏皮妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_qiaopi".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 8. 少女妆
        model = BeautyModel()
        model.path = ""
        model.key = "少女妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_shaonv".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 9. 深邃妆
        model = BeautyModel()
        model.path = ""
        model.key = "深邃妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_shenshui".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 10. 氤氲妆
        model = BeautyModel()
        model.path = ""
        model.key = "氤氲妆"
        model.value = 60  // Android: 0.6 * 100 = 60
        model.name = "show_beauty_item_effect_yinyun".show_localized
        model.icon = "meiyan_fgz_baixi"
        dataArray.append(model)
        
        // 确保所有项都有 icon，如果缺少则使用默认 icon
        // 使用旧代码中的图标名称：meiyan_hunxue (混血)、meiyan_oumei (欧美)
        let defaultIcons = ["meiyan_hunxue", "meiyan_oumei"]
        var iconIndex = 0
        dataArray.forEach { model in
            if model.icon == nil || model.icon?.isEmpty == true {
                model.icon = defaultIcons[iconIndex % defaultIcons.count]
                iconIndex += 1
            }
        }
        
        return dataArray
    }
    
    static func createAgoraStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        
        // 无
        model = BeautyModel()
        model.name = "show_beauty_item_none".show_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        // 圣诞节
        model = BeautyModel()
        model.path = ""
        model.key = "圣诞节"
        model.name = "show_beauty_item_christmas".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 章鱼
        model = BeautyModel()
        model.path = ""
        model.key = "章鱼"
        model.name = "show_beauty_item_squid".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 猪可爱
        model = BeautyModel()
        model.path = ""
        model.key = "猪可爱"
        model.name = "show_beauty_item_piggy".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 辫子猫
        model = BeautyModel()
        model.path = ""
        model.key = "辫子猫"
        model.name = "show_beauty_item_long_cat".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 粉色发箍
        model = BeautyModel()
        model.path = ""
        model.key = "粉色发箍"
        model.name = "show_beauty_item_hairhoop".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 没有烦恼
        model = BeautyModel()
        model.path = ""
        model.key = "没有烦恼"
        model.name = "show_beauty_item_relax_time".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 卡通猫
        model = BeautyModel()
        model.path = ""
        model.key = "卡通猫"
        model.name = "show_beauty_item_cartoon_cat".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 蝴蝶
        model = BeautyModel()
        model.path = ""
        model.key = "蝴蝶"
        model.name = "show_beauty_item_cartoon_butterfly".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 粉刷时光
        model = BeautyModel()
        model.path = ""
        model.key = "粉刷时光"
        model.name = "show_beauty_item_cartoon_brush".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 赛博眼镜
        model = BeautyModel()
        model.path = ""
        model.key = "赛博眼镜"
        model.name = "show_beauty_item_cartoon_cyber_glass".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 霓虹皇冠
        model = BeautyModel()
        model.path = ""
        model.key = "霓虹皇冠"
        model.name = "show_beauty_item_cartoon_cyber_neon_tiara".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 爱心眼镜
        model = BeautyModel()
        model.path = ""
        model.key = "爱心眼镜"
        model.name = "show_beauty_item_cartoon_cyber_neon_love_glass".show_localized
        model.icon = "meiyan_sticker_zhaocaimao"
        dataArray.append(model)
        
        // 确保所有项都有 icon，如果缺少则使用默认 icon
        // 使用旧代码中的图标名称：meiyan_sticker_zhaocaimao (招财猫贴纸)
        let defaultIcon = "meiyan_sticker_zhaocaimao"
        dataArray.forEach { model in
            if model.icon == nil || model.icon?.isEmpty == true {
                model.icon = defaultIcon
            }
        }
        
        return dataArray
    }
}

//
//  ShowSettingSwitchModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit



protocol ShowSettingModel {
    
}

struct ShowSettingSwitchModel: ShowSettingModel {
    let title: String
    let isOn: Bool
    let valueChangedAction: ((_ isOn: Bool)->())
    let clickDetailButonAction: (()->())
}

struct ShowSettingSegmentModel: ShowSettingModel {
    let title: String
    let selectedIndex: Int
    let items: [String]
    let selectedIndexChangedAction: ((_ index: Int)->())
}

struct ShowSettingSliderModel: ShowSettingModel {
    let title: String
    let value: Float
    let minValue: Float
    let maxValue: Float
    let sliderValueChangingAction: ((_ value: Float)->())   // 正在变化
    let sliderValueChangedAction: ((_ value: Float)->())    // 变化结束
}

struct ShowSettingLabelModel: ShowSettingModel {
    let title: String
    let value: String
    let cellDidSelectedAction: (()->())
}



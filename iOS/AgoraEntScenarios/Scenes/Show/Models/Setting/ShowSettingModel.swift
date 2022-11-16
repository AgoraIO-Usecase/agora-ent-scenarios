//
//  ShowSettingSwitchModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit



class ShowSettingModel {
    var title: String = ""
    init(title: String) {
        self.title = title
    }
}

class ShowSettingSwitchModel: ShowSettingModel {
    
    var isOn: Bool = false
    var valueChangedAction: ((_ isOn: Bool)->())?
    var clickDetailButonAction: (()->())?
    init(title: String, isOn: Bool, valueChangedAction: ( (_: Bool) -> Void)? = nil, clickDetailButonAction: (() -> Void)? = nil) {
        super.init(title: title)
        self.isOn = isOn
        self.valueChangedAction = valueChangedAction
        self.clickDetailButonAction = clickDetailButonAction
    }
}

class ShowSettingSegmentModel: ShowSettingModel {
    var selectedIndex: Int = 0
    var items: [String] = [String]()
    var selectedIndexChangedAction: ((_ index: Int)->())?
    
    init(title: String,selectedIndex: Int, items: [String], selectedIndexChangedAction: ((_: Int) -> Void)? = nil) {
        super.init(title: title)
        self.selectedIndex = selectedIndex
        self.items = items
        self.selectedIndexChangedAction = selectedIndexChangedAction
    }
}

class ShowSettingSliderModel: ShowSettingModel {
    var value: Float = 0
    var minValue: Float = 0
    var maxValue: Float = 1
    var sliderValueChangedAction: ((_ value: Float)->())?    // 变化结束
    
    init(title: String,value: Float, minValue: Float, maxValue: Float, sliderValueChangedAction: ((_: Float) -> Void)? = nil) {
        super.init(title: title)
        self.value = value
        self.minValue = minValue
        self.maxValue = maxValue
        self.sliderValueChangedAction = sliderValueChangedAction
    }
}

class ShowSettingLabelModel: ShowSettingModel {
    var value: String = ""
    var cellDidSelectedAction: ((_ index: Int)->())?
    
    init(title: String,value: String, cellDidSelectedAction: ((_ index: Int) -> Void)? = nil) {
        super.init(title: title)
        self.value = value
        self.cellDidSelectedAction = cellDidSelectedAction
    }
}



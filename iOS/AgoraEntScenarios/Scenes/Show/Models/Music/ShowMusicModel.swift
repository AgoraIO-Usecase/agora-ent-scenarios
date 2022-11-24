//
//  ShowMusicModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/18.
//

import Foundation

class ShowMusicConfigData {
    
    enum DataType {
        case resource // 音乐资源
        case beauty  // 美声
        case mixture // 混响
    }
    
    let title: String
    let dataArray: [ShowMusicEffectCell.CellData]
    let type: DataType
    var selectedIndex: Int? {
        didSet {
            switch type {
            case .resource:
                if (oldValue == selectedIndex) {
                    selectedIndex = nil
                }
                for (index, item) in dataArray.enumerated() {
                    item.isSelected = index == selectedIndex
                }
            case .beauty, .mixture:
                for (index, item) in dataArray.enumerated() {
                    item.isSelected = index == selectedIndex
                }
            }
        }
    }
    
    init(title: String, dataArray: [ShowMusicEffectCell.CellData], type: DataType, selectedIndex: Int? = nil) {
        self.title = title
        self.dataArray = dataArray
        self.type = type
        self.selectedIndex = selectedIndex
    }
}


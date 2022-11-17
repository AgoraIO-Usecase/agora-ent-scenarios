//
//  ShowPresettingModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation

class ShowPresettingModel {
    var title: String
    var desc: String
    var isSelected = false
    
    init(title: String, desc: String, isSelected: Bool = false) {
        self.title = title
        self.desc = desc
        self.isSelected = isSelected
    }
}
